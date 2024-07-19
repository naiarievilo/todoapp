package dev.naiarievilo.todoapp.todolists;

import dev.naiarievilo.todoapp.security.exceptions.UnauthorizedDataAccessException;
import dev.naiarievilo.todoapp.todolists.dtos.TodoListDTO;
import dev.naiarievilo.todoapp.todolists.dtos.TodoListMapper;
import dev.naiarievilo.todoapp.todolists.exceptions.TodoListNotFoundException;
import dev.naiarievilo.todoapp.todolists.todos.Todo;
import dev.naiarievilo.todoapp.todolists.todos.TodoService;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.ImmutableListException;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.PositionExceedsMaxAllowedException;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.TodoNotFoundException;
import dev.naiarievilo.todoapp.users.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static dev.naiarievilo.todoapp.todolists.ListTypes.*;

@Service
@Transactional(readOnly = true)
public class TodoListService {

    public static final DateTimeFormatter CALENDAR_LIST_TITLE = DateTimeFormatter.ofPattern("EEEE, d MMM yyyy");

    private final TodoListRepository listRepository;
    private final TodoListMapper listMapper;
    private final TodoService todoService;


    public TodoListService(TodoListRepository listRepository, TodoListMapper listMapper, TodoService todoService) {
        this.listRepository = listRepository;
        this.listMapper = listMapper;
        this.todoService = todoService;
    }

    @Transactional
    public TodoList getInboxList(User user) {
        Optional<TodoList> list = listRepository.findByType(INBOX, user);
        if (list.isPresent()) {
            return list.get();
        }

        TodoList newInboxList = new TodoList();
        newInboxList.setTitle(INBOX.toString());
        newInboxList.setType(INBOX);
        newInboxList.setUser(user);
        listRepository.persist(newInboxList);

        return newInboxList;
    }

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

    public Set<TodoList> getAllCustomLists(User user) {
        List<TodoList> customLists = listRepository.findAllByType(CUSTOM, user);
        return new LinkedHashSet<>(customLists);
    }

    @Transactional
    public TodoList createList(User user, TodoListDTO listDTO, ListTypes listType) {
        TodoList newList = listMapper.toEntity(listDTO);
        newList.setType(listType);
        newList.setUser(user);
        listRepository.persist(newList);
        return newList;
    }

    @Transactional
    public void updateList(Long userId, Long listId, TodoListDTO listDTO) {
        TodoList list = getListByIdEagerly(userId, listId);

        ListTypes listType = listDTO.getType();
        if (listType == INBOX || listType == CALENDAR) {
            throw new ImmutableListException(listType.getType());
        }

        listMapper.updateEntityFromDTO(list, listDTO);
        listRepository.update(list);
    }

    public TodoList getListByIdEagerly(Long userId, Long listId) {
        TodoList list = listRepository.findByIdEagerly(listId).orElseThrow(() -> new TodoListNotFoundException(listId));
        validateUserAccess(list, userId);
        return list;
    }

    private void validateUserAccess(TodoList list, Long userId) {
        if (!userId.equals(list.getUser().getId())) {
            throw new UnauthorizedDataAccessException();
        }
    }

    @Transactional
    public void deleteList(Long userId, Long listId) {
        TodoList list = getListByIdEagerly(userId, listId);
        listRepository.delete(list);
    }

    public Set<Todo> getTodosFromList(Long userId, Long listId) {
        TodoList list = getListByIdEagerly(userId, listId);
        return list.getTodos();
    }

    public Todo getTodoFromList(Long userId, Long listId, Long todoId) {
        TodoList list = getListByIdEagerly(userId, listId);
        return getTodoFromList(todoId, list);
    }

    private Todo getTodoFromList(Long todoId, TodoList parent) {
        return parent.getTodos().stream()
            .filter(todo -> todo.getId().equals(todoId))
            .findFirst().orElseThrow(() -> new TodoNotFoundException(todoId));
    }

    @Transactional
    public Todo addNewTodoToList(Long userId, Long listId, TodoDTO todoDTO) {
        TodoList list = getListByIdEagerly(userId, listId);
        return todoService.createTodo(todoDTO, list);
    }

    @Transactional
    public void updateTodoFromList(Long userId, Long listId, Long todoId, TodoDTO todoDTO) {
        TodoList list = getListByIdEagerly(userId, listId);
        Todo todo = getTodoFromList(todoId, list);
        if (todoDTO.getPosition() > list.getTodos().size()) {
            throw new PositionExceedsMaxAllowedException(todoId);
        }

        todoService.updateTodo(todo, todoDTO);
    }

    @Transactional
    public void updateTodosFromList(Long userId, Long listId, Set<TodoDTO> todosDTO) {
        TodoList list = getListByIdEagerly(userId, listId);
        todoService.updateTodos(list.getTodos(), todosDTO);
    }

    @Transactional
    public void removeTodoFromList(Long userId, Long listId, Long todoId) {
        TodoList list = getListByIdEagerly(userId, listId);
        Todo todo = getTodoFromList(todoId, list);
        todoService.deleteTodo(todo, list);

        Set<Todo> todos = list.getTodos();
        adjustTodosPosition(todos);
    }

    private void adjustTodosPosition(Set<Todo> todos) {
        int newPosition = 1;
        for (Todo todo : todos) {
            todo.setPosition(newPosition++);
        }
    }

    @Transactional
    public void removeTodosFromList(Long userId, Long listId, Set<Long> todosId) {
        TodoList list = getListByIdEagerly(userId, listId);
        Set<Todo> todos = list.getTodos();

        for (Todo todo : new LinkedHashSet<>(todos)) {
            Long todoId = todo.getId();
            if (todosId.contains(todoId)) {
                todoService.deleteTodo(todo, list);
            }
        }

        adjustTodosPosition(todos);
    }

    @Transactional
    public void removeTodosFromList(Long userId, Long listId) {
        TodoList list = getListByIdEagerly(userId, listId);
        for (Todo todo : new LinkedHashSet<>(list.getTodos())) {
            todoService.deleteTodo(todo, list);
        }
    }
}
