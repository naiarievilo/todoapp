package dev.naiarievilo.todoapp.todolists;

import dev.naiarievilo.todoapp.security.exceptions.UnauthorizedDataAccessException;
import dev.naiarievilo.todoapp.todolists.dtos.TodoListDTO;
import dev.naiarievilo.todoapp.todolists.dtos.TodoListMapper;
import dev.naiarievilo.todoapp.todolists.exceptions.TodoListNotFoundException;
import dev.naiarievilo.todoapp.todolists.todos.Todo;
import dev.naiarievilo.todoapp.todolists.todos.TodoService;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.TodoNotFoundException;
import dev.naiarievilo.todoapp.users.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static dev.naiarievilo.todoapp.todolists.ListTypes.CALENDAR;
import static dev.naiarievilo.todoapp.todolists.ListTypes.INBOX;

@Service
@Transactional(readOnly = true)
public class TodoListServiceImpl implements TodoListService {

    public static final DateTimeFormatter CALENDAR_LIST_TITLE = DateTimeFormatter.ofPattern("EEEE, d MMM yyyy");

    private final TodoListRepository listRepository;
    private final TodoListMapper listMapper;
    private final TodoService todoService;


    public TodoListServiceImpl(TodoListRepository listRepository, TodoListMapper listMapper, TodoService todoService) {
        this.listRepository = listRepository;
        this.listMapper = listMapper;
        this.todoService = todoService;
    }

    @Override
    @Transactional
    public TodoList getInboxList(User user) {
        Optional<TodoList> list = listRepository.findByType(INBOX, user);
        if (list.isPresent()) {
            return list.get();
        }

        TodoList newInboxList = new TodoList();
        newInboxList.setType(INBOX);
        newInboxList.setUser(user);
        listRepository.persist(newInboxList);

        return newInboxList;
    }

    @Override
    @Transactional
    public TodoList getTodayList(User user) {
        LocalDate today = LocalDate.now();
        return listRepository.findByTypeAndDueDate(CALENDAR, today, user)
            .orElseGet(() -> {
                TodoList newTodayList = new TodoList();
                newTodayList.setTitle(today.format(CALENDAR_LIST_TITLE));
                newTodayList.setType(CALENDAR);
                newTodayList.setUser(user);
                newTodayList.setDueDate(today);

                listRepository.persist(newTodayList);
                return newTodayList;
            });
    }

    @Override
    @Transactional
    public Set<TodoList> getWeeklyLists(User user) {
        LocalDate today = LocalDate.now();
        LocalDate startOftWeek = today.minusDays((today.getDayOfWeek().getValue() - 1));
        LocalDate nextWeek = startOftWeek.plusWeeks(1);

        Set<TodoList> weeklyLists = new LinkedHashSet<>();
        LocalDate dayOfWeek = startOftWeek;
        while (dayOfWeek.isBefore(nextWeek)) {
            Optional<TodoList> list = listRepository.findByTypeAndDueDate(CALENDAR, dayOfWeek, user);
            if (list.isPresent()) {
                weeklyLists.add(list.get());
                dayOfWeek = dayOfWeek.plusDays(1);
                continue;
            }

            TodoList newList = new TodoList();
            newList.setTitle(dayOfWeek.format(CALENDAR_LIST_TITLE));
            newList.setType(CALENDAR);
            newList.setDueDate(dayOfWeek);
            newList.setUser(user);
            listRepository.persist(newList);
            weeklyLists.add(newList);
            dayOfWeek = dayOfWeek.plusDays(1);
        }

        return weeklyLists;
    }

    @Override
    public TodoList getListByIdEagerly(Long userId, Long listId) {
        TodoList list = listRepository.findByIdEagerly(listId).orElseThrow(() -> new TodoListNotFoundException(listId));
        validateUserAccess(list, userId);
        return list;
    }

    @Override
    @Transactional
    public TodoList createList(User user, TodoListDTO listDTO, ListTypes listType) {
        TodoList newList = listMapper.toEntity(listDTO);
        newList.setType(listType);
        newList.setUser(user);
        listRepository.persist(newList);
        return newList;
    }

    @Override
    @Transactional
    public void updateList(Long userId, Long listId, TodoListDTO listDTO) {
        TodoList list = getListByIdEagerly(userId, listId);
        listMapper.updateEntityFromDTO(list, listDTO);

        Set<TodoDTO> todosDTO = listDTO.todos();
        if (todosDTO != null) {
            todoService.updateTodos(list.getTodos(), todosDTO, list);
        }

        listRepository.update(list);
    }

    @Override
    @Transactional
    public void deleteList(Long userId, Long listId) {
        TodoList list = getListByIdEagerly(userId, listId);
        listRepository.delete(list);
    }

    @Override
    @Transactional
    public Todo addNewTodoToList(Long userId, Long listId, TodoDTO todoDTO) {
        TodoList list = getListByIdEagerly(userId, listId);
        return todoService.createTodo(todoDTO, list);
    }

    @Override
    @Transactional
    public void updateTodoFromList(Long userId, Long listId, Long todoId, TodoDTO todoDTO) {
        TodoList list = getListByIdEagerly(userId, listId);
        Todo todo = getTodoFromList(todoId, list);
        todoService.updateTodo(todo, todoDTO);
    }

    @Override
    @Transactional
    public void removeTodoFromList(Long userId, Long listId, Long todoId) {
        TodoList list = getListByIdEagerly(userId, listId);
        Todo todo = getTodoFromList(todoId, list);
        todoService.deleteTodo(todo, list);
    }

    private Todo getTodoFromList(Long todoId, TodoList parent) {
        return parent.getTodos().stream()
            .filter(todo -> todo.getId().equals(todoId))
            .findFirst().orElseThrow(() -> new TodoNotFoundException(todoId));
    }

    private void validateUserAccess(TodoList list, Long userId) {
        if (!userId.equals(list.getUser().getId())) {
            throw new UnauthorizedDataAccessException();
        }
    }

}
