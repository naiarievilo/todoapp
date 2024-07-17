package dev.naiarievilo.todoapp.todolists;

import dev.naiarievilo.todoapp.ServiceIntegrationTests;
import dev.naiarievilo.todoapp.security.exceptions.UnauthorizedDataAccessException;
import dev.naiarievilo.todoapp.todolists.dtos.TodoListDTO;
import dev.naiarievilo.todoapp.todolists.exceptions.TodoListNotFoundException;
import dev.naiarievilo.todoapp.todolists.todos.Todo;
import dev.naiarievilo.todoapp.todolists.todos.TodoRepository;
import dev.naiarievilo.todoapp.todolists.todos.TodosTestHelper;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.naiarievilo.todoapp.todolists.ListTypes.*;
import static dev.naiarievilo.todoapp.todolists.TodoListServiceTestCases.*;
import static dev.naiarievilo.todoapp.todolists.TodoListsTestHelper.*;
import static dev.naiarievilo.todoapp.todolists.todos.TodosTestHelper.TODO_TASK_2;
import static dev.naiarievilo.todoapp.users.UsersTestConstants.EMAIL_1;
import static dev.naiarievilo.todoapp.users.UsersTestConstants.PASSWORD_1;
import static org.junit.jupiter.api.Assertions.*;

class TodoListServiceIT extends ServiceIntegrationTests {

    @Autowired
    TodoListRepository listRepository;

    @Autowired
    TodoRepository todoRepository;

    @Autowired
    TodoListService listService;

    @Autowired
    UserRepository userRepository;

    private User user;
    private TodoList list;
    private TodoListDTO listDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail(EMAIL_1);
        user.setPassword(PASSWORD_1);
        userRepository.persist(user);

        list = new TodoList();
        list.setTitle(LIST_TITLE_1);
        list.setType(CUSTOM);
        list.setUser(user);

        listDTO = new TodoListDTO(null, list.getTitle(), null, null, null, null);
    }

    @Test
    @Transactional
    @DisplayName("getInboxList(): " + RETURNS_INBOX_LIST_WHEN_LIST_EXISTS)
    void getInboxList_InboxListExists_ReturnsInboxList() {
        TodoList inboxList = listService.getInboxList(user);
        assertNotNull(inboxList);
        assertEquals(INBOX, inboxList.getType());
    }

    @Test
    @Transactional
    @DisplayName("getTodayList(): " + RETURNS_TODAY_LIST_WHEN_LIST_EXISTS)
    void getTodayList_TodayListExists_ReturnsTodayList() {
        userRepository.persist(user);

        TodoList todayList = listService.getTodayList(user);
        assertNotNull(todayList);
        assertEquals(CALENDAR, todayList.getType());
        assertEquals(LocalDate.now(), todayList.getDueDate());
    }

    @Test
    @Transactional
    @DisplayName("getWeekLists(): " + RETURNS_WEEKLY_LISTS_WHEN_LISTS_EXIST)
    void getWeekLists_WeeklyListsExist_ReturnsWeeklyLists() {
        Set<TodoList> weeklyLists = listService.getWeeklyLists(user);
        assertNotNull(weeklyLists);
        assertEquals(7, weeklyLists.size());

        LocalDate dayOfWeek = START_OF_WEEK;
        for (TodoList dayList : weeklyLists) {
            assertEquals(dayOfWeek, dayList.getDueDate());
            assertEquals(CALENDAR, dayList.getType());
            dayOfWeek = dayOfWeek.plusDays(1);
        }
    }

    @Test
    @Transactional
    @DisplayName("getAllCustomLists(): " + RETURNS_ALL_USER_CUSTOM_LISTS)
    void getAllCustomLists_ReturnsAllPersonalizedLists() {
        TodoList otherList = new TodoList();
        list.setType(CUSTOM);
        otherList.setType(CUSTOM);
        otherList.setUser(user);
        listRepository.persistAll(Set.of(list, otherList));

        Set<TodoList> customLists = listService.getAllCustomLists(user);
        assertNotNull(customLists);
        assertEquals(2, customLists.size());
    }

    @Test
    @Transactional
    @DisplayName("getListByIdEagerly(): " + RETURNS_LIST_WHEN_LIST_EXISTS)
    void getListByIdEagerly_ListExists_ReturnsList() {
        listRepository.persist(list);
        Long userId = user.getId();
        Long listId = list.getId();

        TodoList returnedList = listService.getListByIdEagerly(userId, listId);
        assertNotNull(returnedList);
        assertEquals(list.getId(), returnedList.getId());
        assertEquals(user, list.getUser());
    }

    @Test
    @Transactional
    @DisplayName("getListByIdEagerly(): " + THROWS_UNAUTHORIZED_DATA_ACCESS_WHEN_USER_DOES_NOT_HAVE_LIST_ACCESS)
    void getListByIdEagerly_ListDoesNotExist_ThrowsUnauthorizedDataAccessException() {
        userRepository.persist(user);
        listRepository.persist(list);
        Long userId = 3242L;
        Long listId = list.getId();

        assertThrows(UnauthorizedDataAccessException.class, () -> listService.getListByIdEagerly(userId, listId));
    }

    @Test
    @Transactional
    @DisplayName("createList(): " + CREATES_LIST_WHEN_INPUT_VALID)
    void createList_InputValid_CreatesList() {

        TodoList createdList = listService.createList(user, listDTO, CUSTOM);
        assertNotNull(createdList);
        assertEquals(listDTO.title(), createdList.getTitle());
        assertEquals(CUSTOM, createdList.getType());
        assertNotNull(createdList.getCreatedAt());
        assertTrue(listRepository.existsById(createdList.getId()));
    }

    @Test
    @Transactional
    @DisplayName("updateList(): " + UPDATES_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void updateList_UserHasListAccess_UpdatesList() {
        listRepository.persist(list);
        Long userId = user.getId();
        Long listId = list.getId();
        TodoListDTO updatedListDTO = new TodoListDTO(list.getId(), LIST_TITLE_2, null, null, LocalDate.now(), null);

        listService.updateList(userId, listId, updatedListDTO);

        TodoList updatedList = listRepository.findById(listId).orElseThrow(TodoListNotFoundException::new);
        assertEquals(updatedListDTO.title(), updatedList.getTitle());
        assertEquals(updatedListDTO.dueDate(), updatedList.getDueDate());
    }

    @Test
    @Transactional
    @DisplayName("updateList(): " + UPDATES_LIST_AND_ITS_TODOS_WHEN_USER_HAS_ACCESS)
    void updateList_HasTodosToUpdate_UpdatesListAndTodos() {
        Todo persistedTodo = TodosTestHelper.newTodo_1();
        list.addTodo(persistedTodo);
        listRepository.persist(list);

        TodoDTO updatedTodoDTO = new TodoDTO(
            persistedTodo.getId(), TODO_TASK_2, true, persistedTodo.getPosition(), null, null
        );
        TodoListDTO updatedListDTO = new TodoListDTO(
            list.getId(), LIST_TITLE_2, null, null, LocalDate.now(), Set.of(updatedTodoDTO)
        );

        Long userId = user.getId();
        Long listId = list.getId();

        listService.updateList(userId, listId, updatedListDTO);

        TodoList updatedList = listRepository.findById(listId).orElseThrow(TodoListNotFoundException::new);
        assertEquals(updatedListDTO.title(), updatedList.getTitle());
        assertEquals(updatedListDTO.dueDate(), updatedList.getDueDate());
        assertEquals(1, updatedList.getTodos().size());

        Todo updatedTodo = updatedList.getTodos().iterator().next();
        assertEquals(updatedTodoDTO.task(), updatedTodo.getTask());
        assertEquals(updatedTodoDTO.position(), updatedTodo.getPosition());
        assertEquals(updatedTodoDTO.dueDate(), updatedTodo.getDueDate());
    }

    @Test
    @Transactional
    @DisplayName("deleteList(): " + DELETES_LIST_WHEN_USER_HAS_ACCESS)
    void deleteList_UserHasListAccess_DeletesList() {
        listRepository.persist(list);
        Long userId = user.getId();
        Long listId = list.getId();

        listService.deleteList(userId, listId);
        assertFalse(listRepository.existsById(listId));
    }

    @Test
    @Transactional
    @DisplayName("addNewTodoToList(): " + ADDS_TODO_TO_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void addNewTodoToList_UserHasListAccess_AddsTodoToList() {
        listRepository.persist(list);
        Long userId = user.getId();
        Long listId = list.getId();

        TodoDTO newTodoDTO = TodosTestHelper.newTodoDTO_1();

        listService.addNewTodoToList(userId, listId, newTodoDTO);
        assertEquals(1, list.getTodos().size());

        Todo addedTodo = list.getTodos().iterator().next();
        assertTrue(todoRepository.existsById(addedTodo.getId()));
        assertEquals(newTodoDTO.task(), addedTodo.getTask());
        assertEquals(newTodoDTO.position(), addedTodo.getPosition());
        assertEquals(newTodoDTO.dueDate(), addedTodo.getDueDate());
    }

    @Test
    @Transactional
    @DisplayName("updateTodoFromList(): " + UPDATES_TODO_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void updateTodoFromList_UserHasListAccess_UpdatesTodoFromList() {
        Todo newTodo = TodosTestHelper.newTodo_1();
        list.addTodo(newTodo);
        listRepository.persist(list);

        TodoDTO updatedTodoDTO = new TodoDTO(newTodo.getId(), "updated task", true, newTodo.getPosition(), null, null);
        Long userId = user.getId();
        Long listId = list.getId();
        Long todoId = updatedTodoDTO.id();
        assert todoId != null;

        listService.updateTodoFromList(userId, listId, todoId, updatedTodoDTO);
        Todo updatedTodo = list.getTodos().iterator().next();
        assertEquals(updatedTodoDTO.task(), updatedTodo.getTask());
        assertEquals(updatedTodoDTO.completed(), updatedTodo.isCompleted());
    }

    @Test
    @DisplayName("updateTodosFromList(): " + UPDATES_TODOS_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void updateTodosFromList_UserHasListAccess_UpdatesTodosFromList() {
        Set<Todo> newTodoSet = TodosTestHelper.newTodoSet();
        for (Todo newTodoInSet : newTodoSet) {
            list.addTodo(newTodoInSet);
        }
        listRepository.persist(list);

        Set<TodoDTO> updatedTodoDTOSet = new LinkedHashSet<>();
        int newPosition = newTodoSet.size();
        for (Todo newTodoInSet : newTodoSet) {
            updatedTodoDTOSet.add(new TodoDTO(
                newTodoInSet.getId(),
                newTodoInSet.getTask(),
                newTodoInSet.isCompleted(),
                newPosition--,
                newTodoInSet.getCreatedAt(),
                newTodoInSet.getDueDate()
            ));
        }

        Long userId = user.getId();
        Long listId = list.getId();
        listService.updateTodosFromList(userId, listId, updatedTodoDTOSet);

        newPosition = newTodoSet.size();
        for (Todo updatedTodo : newTodoSet) {
            assertEquals(newPosition--, updatedTodo.getPosition());
        }
    }

    @Test
    @Transactional
    @DisplayName("removeTodoFromList(): " + DELETES_TODO_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void removeTodoFromList_UserHasListAccess_DeletesTodoFromList() {
        Todo newTodo = TodosTestHelper.newTodo_1();
        list.addTodo(newTodo);
        listRepository.persist(list);

        Long userId = user.getId();
        Long listId = list.getId();
        Long todoId = newTodo.getId();

        listService.removeTodoFromList(userId, listId, todoId);
        assertEquals(0, list.getTodos().size());
        assertFalse(todoRepository.existsById(todoId));
    }

    @Test
    @DisplayName("removeTodosFromList(): " + DELETES_TODO_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void removeTodosFromList_UserHasListAccess_DeletesTodosFromList() {
        Set<Todo> newTodos = TodosTestHelper.newTodoSet();
        list.setTodos(newTodos);
        listRepository.persist(list);

        Set<Long> todosId = list.getTodos().stream()
            .limit(2)
            .map(Todo::getId)
            .collect(Collectors.toSet());

        Long userId = user.getId();
        Long listId = list.getId();

        listService.removeTodosFromList(userId, listId, todosId);
        assertEquals(1, list.getTodos().size());
    }

    @Test
    @DisplayName("removeTodosFromList(): " + DELETES_TODO_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void removeTodosFromList_UserHasListAccess_DeletesAllTodosFromList() {
        Set<Todo> newTodos = TodosTestHelper.newTodoSet();
        list.setTodos(newTodos);
        listRepository.persist(list);

        Long userId = user.getId();
        Long listId = list.getId();

        listService.removeTodosFromList(userId, listId);
        assertEquals(0, list.getTodos().size());
    }
}
