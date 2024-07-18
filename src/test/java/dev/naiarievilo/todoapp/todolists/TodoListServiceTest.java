package dev.naiarievilo.todoapp.todolists;

import dev.naiarievilo.todoapp.security.exceptions.UnauthorizedDataAccessException;
import dev.naiarievilo.todoapp.todolists.dtos.TodoListDTO;
import dev.naiarievilo.todoapp.todolists.dtos.TodoListMapper;
import dev.naiarievilo.todoapp.todolists.exceptions.TodoListNotFoundException;
import dev.naiarievilo.todoapp.todolists.todos.Todo;
import dev.naiarievilo.todoapp.todolists.todos.TodoService;
import dev.naiarievilo.todoapp.todolists.todos.TodosTestHelper;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.TodoNotFoundException;
import dev.naiarievilo.todoapp.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static dev.naiarievilo.todoapp.todolists.ListTypes.*;
import static dev.naiarievilo.todoapp.todolists.TodoListServiceTestCases.*;
import static dev.naiarievilo.todoapp.todolists.TodoListsTestHelper.*;
import static dev.naiarievilo.todoapp.todolists.todos.TodosTestHelper.*;
import static dev.naiarievilo.todoapp.users.UsersTestConstants.EMAIL_1;
import static dev.naiarievilo.todoapp.users.UsersTestConstants.USER_ID_1;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoListServiceTest {

    @Mock
    TodoListRepository listRepository;

    @Mock
    TodoListMapper listMapper;

    @Mock
    TodoService todoService;

    @InjectMocks
    TodoListService listService;

    @Captor
    ArgumentCaptor<TodoList> listCaptor;
    private User user;
    private TodoList inboxList;
    private TodoList todayList;
    private TodoList persistedList;
    private Set<TodoList> weeklyLists;
    private Long listId;
    private Long userId;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID_1);
        user.setEmail(EMAIL_1);

        inboxList = TodoListsTestHelper.inboxList();
        todayList = TodoListsTestHelper.todayList();
        weeklyLists = TodoListsTestHelper.weeklyLists();
        persistedList = TodoListsTestHelper.list_1();
        persistedList.setUser(user);

        userId = user.getId();
        listId = persistedList.getId();
    }

    @Test
    @DisplayName("getInboxList(): " + RETURNS_INBOX_LIST_WHEN_LIST_EXISTS)
    void getInboxList_InboxListExists_ReturnsInboxList() {
        given(listRepository.findByType(INBOX, user)).willReturn(Optional.of(inboxList));

        TodoList returnedInboxList = listService.getInboxList(user);
        assertEquals(INBOX, returnedInboxList.getType());

        verify(listRepository).findByType(INBOX, user);
        verify(listRepository, never()).persist(any(TodoList.class));
    }

    @Test
    @DisplayName("getInboxList(): " + CREATES_AND_RETURNS_INBOX_LIST_WHEN_LIST_DOES_NOT_EXIST)
    void getInboxList_InboxListDoesNotExist_CreatesAndReturnsInboxList() {
        given(listRepository.findByType(INBOX, user)).willReturn(Optional.empty());

        TodoList returnedInboxList = listService.getInboxList(user);
        assertEquals(INBOX, returnedInboxList.getType());
        assertEquals(user, returnedInboxList.getUser());

        verify(listRepository).findByType(INBOX, user);
        verify(listRepository).persist(listCaptor.capture());
        assertEquals(INBOX, listCaptor.getValue().getType());
    }

    @Test
    @DisplayName("getTodayList(): " + RETURNS_TODAY_LIST_WHEN_LIST_EXISTS)
    void getTodayList_TodayListExists_ReturnsTodayList() {
        given(listRepository.findByTypeAndDueDate(CALENDAR, TODAY, user)).willReturn(Optional.of(todayList));

        TodoList returnedTodayList = listService.getTodayList(user);
        assertEquals(CALENDAR, returnedTodayList.getType());
        assertEquals(TODAY, returnedTodayList.getDueDate());

        verify(listRepository).findByTypeAndDueDate(CALENDAR, TODAY, user);
        verify(listRepository, never()).persist(any(TodoList.class));
    }

    @Test
    @DisplayName("getTodayList(): " + CREATES_AND_RETURNS_TODAY_LIST_WHEN_LIST_DOES_NOT_EXIST)
    void getTodayList_TodayListDoesNotExist_CreatesAndReturnsTodayList() {
        given(listRepository.findByTypeAndDueDate(CALENDAR, TODAY, user)).willReturn(Optional.empty());

        TodoList returnedTodayList = listService.getTodayList(user);
        assertEquals(CALENDAR, returnedTodayList.getType());
        assertEquals(TODAY, returnedTodayList.getDueDate());
        assertEquals(user, returnedTodayList.getUser());

        verify(listRepository).findByTypeAndDueDate(CALENDAR, TODAY, user);
        verify(listRepository).persist(listCaptor.capture());
        TodoList capturedList = listCaptor.getValue();
        assertEquals(CALENDAR, capturedList.getType());
        assertEquals(TODAY, capturedList.getDueDate());
        assertEquals(user, capturedList.getUser());
    }

    @Test
    @DisplayName("getWeekLists(): " + RETURNS_WEEKLY_LISTS_WHEN_LISTS_EXIST)
    void getWeeklyLists_WeeklyListsExist_ReturnsWeeklyLists() {
        LocalDate dayOfWeek = START_OF_WEEK;
        for (TodoList listDay : weeklyLists) {
            given(listRepository.findByTypeAndDueDate(CALENDAR, dayOfWeek, user)).willReturn(Optional.of(listDay));
            dayOfWeek = dayOfWeek.plusDays(1);
        }

        Set<TodoList> returnedWeeklyLists = listService.getWeeklyLists(user);
        assertEquals(7, returnedWeeklyLists.size());
        assertTrue(returnedWeeklyLists.containsAll(weeklyLists));

        verify(listRepository, never()).persist(any(TodoList.class));

        dayOfWeek = START_OF_WEEK;
        while (dayOfWeek.isBefore(NEXT_WEEK)) {
            verify(listRepository).findByTypeAndDueDate(CALENDAR, dayOfWeek, user);
            dayOfWeek = dayOfWeek.plusDays(1);
        }
    }

    @Test
    @DisplayName("getWeekLists(): " + CREATES_AND_RETURNS_WEEKLY_LISTS_WHEN_LISTS_DO_NOT_EXIST)
    void getWeeklyLists_WeeklyListsDoNotExist_CreatesAndReturnsWeeklyLists() {
        LocalDate dayOfWeek = START_OF_WEEK;
        while (dayOfWeek.isBefore(NEXT_WEEK)) {
            given(listRepository.findByTypeAndDueDate(CALENDAR, dayOfWeek, user)).willReturn(Optional.empty());
            dayOfWeek = dayOfWeek.plusDays(1);
        }

        Set<TodoList> returnedWeeklyLists = listService.getWeeklyLists(user);
        assertEquals(7, returnedWeeklyLists.size());

        verify(listRepository, times(7)).persist(any(TodoList.class));

        dayOfWeek = START_OF_WEEK;
        for (TodoList returnedListDay : returnedWeeklyLists) {
            assertEquals(CALENDAR, returnedListDay.getType());
            assertEquals(dayOfWeek, returnedListDay.getDueDate());
            verify(listRepository).persist(returnedListDay);
            dayOfWeek = dayOfWeek.plusDays(1);
        }
    }

    @Test
    @DisplayName("getListByIdEagerly(): " + RETURNS_LIST_WHEN_LIST_EXISTS)
    void getListByIdEagerly_ListExists_ReturnsList() {
        todayList.setId(LIST_ID_1);
        todayList.setUser(user);

        given(listRepository.findByIdEagerly(listId)).willReturn(Optional.of(todayList));

        TodoList returnedList = listService.getListByIdEagerly(userId, listId);
        assertEquals(todayList, returnedList);
        verify(listRepository).findByIdEagerly(listId);
    }

    @Test
    @DisplayName("getListByIdEagerly(): " + THROWS_LIST_NOT_FOUND_WHEN_LIST_DOES_NOT_EXIST)
    void getListByIdEagerly_ListDoesNotExist_ThrowsTodoListNotFoundException() {
        given(listRepository.findByIdEagerly(LIST_ID_1)).willReturn(Optional.empty());
        assertThrows(TodoListNotFoundException.class, () -> listService.getListByIdEagerly(userId, LIST_ID_1));
    }

    @Test
    @DisplayName("getListByIdEagerly(): " + THROWS_UNAUTHORIZED_DATA_ACCESS_WHEN_USER_DOES_NOT_HAVE_LIST_ACCESS)
    void getListByIdEagerly_ListDoesNotExist_ThrowsUnauthorizedDataAccessException() {
        userId = 243L;
        todayList.setId(LIST_ID_1);
        todayList.setUser(user);

        given(listRepository.findByIdEagerly(listId)).willReturn(Optional.of(todayList));
        assertThrows(UnauthorizedDataAccessException.class, () -> listService.getListByIdEagerly(userId, listId));
    }

    @Test
    @DisplayName("createList(): " + CREATES_LIST_WHEN_INPUT_VALID)
    void createList_InputValid_CreatesList() {
        TodoListDTO newListDTO = new TodoListDTO(null, LIST_TITLE_1, null, null, LocalDate.now().plusDays(3), null);
        TodoList newList = new TodoList();
        newList.setTitle(newListDTO.title());
        newList.setDueDate(newListDTO.dueDate());

        given(listMapper.toEntity(newListDTO)).willReturn(newList);

        TodoList createdList = listService.createList(user, newListDTO, CUSTOM);
        assertEquals(newList.getTitle(), createdList.getTitle());
        assertEquals(newList.getDueDate(), createdList.getDueDate());

        verify(listMapper).toEntity(newListDTO);
        verify(listRepository).persist(listCaptor.capture());
        TodoList capturedList = listCaptor.getValue();
        assertEquals(newList.getTitle(), capturedList.getTitle());
        assertEquals(newList.getDueDate(), capturedList.getDueDate());
    }

    @Test
    @DisplayName("updateList(): " + DOES_NOT_UPDATE_LIST_WHEN_USER_DOES_NOT_HAVE_LIST_ACCESS)
    void updateList_UserDoesNotHaveListAccess_DoesNotUpdateList() {
        userId = 875L;

        TodoListDTO updatedListDTO = new TodoListDTO(
            persistedList.getId(), LIST_TITLE_2, null, null, LocalDate.now().plusDays(3), null
        );

        given(listRepository.findByIdEagerly(listId)).willReturn(Optional.of(persistedList));

        assertThrows(UnauthorizedDataAccessException.class,
            () -> listService.updateList(userId, listId, updatedListDTO));
        verify(listRepository).findByIdEagerly(listId);
        verifyNoInteractions(listMapper);
        verifyNoInteractions(todoService);
    }

    @Test
    @DisplayName("updateList(): " + UPDATES_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void updateList_UserHasListAccess_UpdatesList() {
        TodoListDTO updatedListDTO = new TodoListDTO(
            persistedList.getId(), LIST_TITLE_2, null, null, LocalDate.now().plusDays(3), null
        );

        given(listRepository.findByIdEagerly(listId)).willReturn(Optional.of(persistedList));

        listService.updateList(userId, listId, updatedListDTO);
        verify(listRepository).findByIdEagerly(listId);
        verify(listMapper).updateEntityFromDTO(persistedList, updatedListDTO);
        verifyNoInteractions(todoService);
    }

    @Test
    @DisplayName("updateList(): " + UPDATES_LIST_AND_ITS_TODOS_WHEN_USER_HAS_LIST_ACCESS)
    void updateList_HasTodosToUpdate_UpdatesListAndTodos() {
        TodoListDTO updatedListDTO = new TodoListDTO(
            persistedList.getId(), LIST_TITLE_2, null, null, LocalDate.now().plusDays(3),
            Set.of(new TodoDTO(null, TODO_TASK_1, false, TODO_POSITION_1, null, null))
        );

        given(listRepository.findByIdEagerly(listId)).willReturn(Optional.of(persistedList));

        listService.updateList(userId, listId, updatedListDTO);
        verify(listRepository).findByIdEagerly(listId);
        verify(listMapper).updateEntityFromDTO(persistedList, updatedListDTO);
        assert updatedListDTO.todos() != null;
        verify(todoService).updateTodos(persistedList.getTodos(), updatedListDTO.todos(), persistedList);
    }

    @Test
    @DisplayName("deleteList(): " + DOES_NOT_DELETE_LIST_WHEN_USER_DOES_NOT_HAVE_LIST_ACCESS)
    void deleteList_UserDoesNotHaveListAccess_DoesNotDeleteList() {
        userId = 987L;
        given(listRepository.findByIdEagerly(listId)).willReturn(Optional.of(persistedList));

        assertThrows(UnauthorizedDataAccessException.class, () -> listService.deleteList(userId, listId));
        verify(listRepository, never()).delete(persistedList);
    }

    @Test
    @DisplayName("deleteList(): " + DELETES_LIST_WHEN_USER_HAS_ACCESS)
    void deleteList_UserHasListAccess_DeletesList() {
        given(listRepository.findByIdEagerly(listId)).willReturn(Optional.of(persistedList));
        listService.deleteList(userId, listId);
        verify(listRepository).delete(persistedList);
    }

    @Test
    @DisplayName("getTodosFromList(): " + DOES_NOT_GET_TODOS_FROM_LIST_WHEN_USER_DOES_NOT_HAVE_LIST_ACCESS)
    void getTodosFromList_UserDoesNotHaveListAccess_DoesNotGetTodosFromList() {
        userId = 3432L;
        Set<Todo> persistedTodos = TodosTestHelper.todoSet();
        persistedList.setTodos(persistedTodos);

        given(listRepository.findByIdEagerly(listId)).willReturn(Optional.of(persistedList));
        assertThrows(UnauthorizedDataAccessException.class, () -> listService.getTodosFromList(userId, listId));
    }

    @Test
    @DisplayName("getTodosFromList(): " + GETS_ALL_TODOS_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void getTodosFromList_UserHasListAccess_GetsTodosFromList() {
        Set<Todo> persistedTodos = TodosTestHelper.todoSet();
        persistedList.setTodos(persistedTodos);

        given(listRepository.findByIdEagerly(listId)).willReturn(Optional.of(persistedList));

        Set<Todo> returnedTodos = listService.getTodosFromList(userId, listId);
        assertTrue(persistedTodos.containsAll(returnedTodos) && returnedTodos.containsAll(persistedTodos));
    }

    @Test
    @DisplayName("addNewTodoToList(): " + DOES_NOT_ADD_TODO_TO_LIST_WHEN_USER_DOES_NOT_HAVE_LIST_ACCESS)
    void addNewTodoToList_UserDoesNotHaveListAccess_DoesNotAddTodoToList() {
        userId = 43L;
        TodoDTO newTodo = TodosTestHelper.newTodoDTO_1();

        given(listRepository.findByIdEagerly(listId)).willReturn(Optional.of(persistedList));
        assertThrows(UnauthorizedDataAccessException.class,
            () -> listService.addNewTodoToList(userId, listId, newTodo));
        verify(listRepository).findByIdEagerly(listId);
        verifyNoInteractions(todoService);
    }

    @Test
    @DisplayName("addNewTodoToList(): " + ADDS_TODO_TO_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void addNewTodoToList_UserHasListAccess_AddsTodoToList() {
        TodoDTO newTodo = TodosTestHelper.newTodoDTO_1();

        given(listRepository.findByIdEagerly(listId)).willReturn(Optional.of(persistedList));
        given(todoService.createTodo(newTodo, persistedList)).willReturn(TodosTestHelper.todo_1());

        Todo createdTodo = listService.addNewTodoToList(userId, listId, newTodo);
        assertNotNull(createdTodo);

        verify(listRepository).findByIdEagerly(listId);
        verify(todoService).createTodo(newTodo, persistedList);
    }

    @Test
    @DisplayName("updateTodoFromList(): " + THROWS_TODO_NOT_FOUND_WHEN_TODO_NOT_IN_LIST)
    void updateTodoFromList_TodoDoesNotExist_ThrowsTodoNotFoundException() {
        Todo persistedTodo = TodosTestHelper.todo_1();
        TodoDTO updatedTodo = TodosTestHelper.todoDTO_2();
        persistedList.addTodo(persistedTodo);

        Long todoId = updatedTodo.id();
        assert todoId != null;

        given(listRepository.findByIdEagerly(listId)).willReturn(Optional.of(persistedList));

        assertThrows(TodoNotFoundException.class,
            () -> listService.updateTodoFromList(userId, listId, todoId, updatedTodo));
        verify(listRepository).findByIdEagerly(listId);
        verifyNoInteractions(todoService);
    }

    @Test
    @DisplayName("updateTodoFromList(): " + DOES_NOT_UPDATE_TODO_FROM_LIST_WHEN_USER_DOES_NOT_HAVE_LIST_ACCESS)
    void updateTodoFromList_UserDoesNotHaveListAccess_DoesNotUpdateTodoFromList() {
        userId = 432L;
        Todo persistedTodo = TodosTestHelper.todo_1();
        TodoDTO updatedTodo = new TodoDTO(persistedTodo.getId(), TODO_TASK_2, true, TODO_POSITION_3, null, null);
        persistedList.addTodo(persistedTodo);
        Long todoId = persistedTodo.getId();

        given(listRepository.findByIdEagerly(listId)).willReturn(Optional.of(persistedList));
        assertThrows(UnauthorizedDataAccessException.class,
            () -> listService.updateTodoFromList(userId, listId, todoId, updatedTodo));
        verify(listRepository).findByIdEagerly(listId);
        verifyNoInteractions(todoService);
    }

    @Test
    @DisplayName("updateTodoFromList(): " + UPDATES_TODO_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void updateTodoFromList_UserHasListAccess_UpdatesTodoFromList() {
        Todo persistedTodo = TodosTestHelper.todo_1();
        TodoDTO updatedTodo = new TodoDTO(persistedTodo.getId(), TODO_TASK_2, true, TODO_POSITION_3, null, null);
        persistedList.addTodo(persistedTodo);
        Long todoId = persistedTodo.getId();

        given(listRepository.findByIdEagerly(listId)).willReturn(Optional.of(persistedList));

        listService.updateTodoFromList(userId, listId, todoId, updatedTodo);
        verify(listRepository).findByIdEagerly(listId);
        verify(todoService).updateTodo(persistedTodo, updatedTodo);
    }

    @Test
    @DisplayName("updateTodosFromList(): " + UPDATES_TODOS_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void updateTodosFromList_UserHasListAccess_UpdatesTodosFromList() {
        Set<Todo> persistedTodos = TodosTestHelper.todoSet();
        Set<TodoDTO> updatedTodosDTO = TodosTestHelper.todoDTOSet();
        persistedList.setTodos(persistedTodos);

        given(listRepository.findByIdEagerly(listId)).willReturn(Optional.of(persistedList));

        listService.updateTodosFromList(userId, listId, updatedTodosDTO);

        verify(listRepository).findByIdEagerly(listId);
        verify(todoService).updateTodos(persistedTodos, updatedTodosDTO, persistedList);
    }

    @Test
    @DisplayName("removeTodoFromList(): " + THROWS_TODO_NOT_FOUND_WHEN_TODO_NOT_IN_LIST)
    void removeTodoFromList_TodoDoesNotExist_ThrowsTodoNotFoundException() {
        Todo persistedTodo = TodosTestHelper.todo_1();
        persistedList.addTodo(persistedTodo);
        Long todoId = 433L;

        given(listRepository.findByIdEagerly(listId)).willReturn(Optional.of(persistedList));

        assertThrows(TodoNotFoundException.class, () -> listService.removeTodoFromList(userId, listId, todoId));
        verify(listRepository).findByIdEagerly(listId);
        verifyNoInteractions(todoService);
    }

    @Test
    @DisplayName("removeTodoFromList(): " + DOES_NOT_DELETE_TODO_FROM_LIST_WHEN_USER_DOES_NOT_HAVE_LIST_ACCESS)
    void removeTodoFromList_UserDoesNotHaveListAccess_DoesNotDeleteTodoFromList() {
        userId = 750L;
        Todo persistedTodo = TodosTestHelper.todo_1();
        Long todoId = persistedTodo.getId();
        persistedList.addTodo(persistedTodo);

        given(listRepository.findByIdEagerly(listId)).willReturn(Optional.of(persistedList));

        assertThrows(UnauthorizedDataAccessException.class,
            () -> listService.removeTodoFromList(userId, listId, todoId));
        verify(listRepository).findByIdEagerly(listId);
        verifyNoInteractions(todoService);
    }

    @Test
    @DisplayName("removeTodoFromList(): " + DELETES_TODO_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void removeTodoFromList_UserHasListAccess_DeletesTodoFromList() {
        Todo persistedTodo = TodosTestHelper.todo_1();
        Long todoId = persistedTodo.getId();
        persistedList.addTodo(persistedTodo);

        given(listRepository.findByIdEagerly(listId)).willReturn(Optional.of(persistedList));

        listService.removeTodoFromList(userId, listId, todoId);
        verify(listRepository).findByIdEagerly(listId);
        verify(todoService).deleteTodo(persistedTodo, persistedList);
    }

    @Test
    @DisplayName("removeTodosFromList(): " + DELETES_TODO_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void removeTodosFromList_UserHasListAccess_DeletesTodosFromList() {
        Set<Todo> persistedTodos = TodosTestHelper.todoSet();
        persistedList.setTodos(persistedTodos);

        Set<Long> todosId = new HashSet<>();
        for (Todo persistedTodo : persistedTodos) {
            todosId.add(persistedTodo.getId());
        }

        given(listRepository.findByIdEagerly(listId)).willReturn(Optional.of(persistedList));

        listService.removeTodosFromList(userId, listId, todosId);
        verify(listRepository).findByIdEagerly(listId);
        for (Todo persistedTodo : persistedTodos) {
            verify(todoService).deleteTodo(persistedTodo, persistedList);
        }
    }

    @Test
    @DisplayName("removeTodosFromList(): " + DELETES_TODO_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void removeTodosFromList_UserHasListAccess_DeletesAllTodosFromList() {
        Set<Todo> persistedTodos = TodosTestHelper.todoSet();
        persistedList.setTodos(persistedTodos);

        given(listRepository.findByIdEagerly(listId)).willReturn(Optional.of(persistedList));

        listService.removeTodosFromList(userId, listId);
        verify(listRepository).findByIdEagerly(listId);
        for (Todo persistedTodo : persistedTodos) {
            verify(todoService).deleteTodo(persistedTodo, persistedList);
        }
    }
}
