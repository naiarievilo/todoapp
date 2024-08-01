package dev.naiarievilo.todoapp.todolists;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.naiarievilo.todoapp.ControllerIntegrationTests;
import dev.naiarievilo.todoapp.security.ErrorDetails;
import dev.naiarievilo.todoapp.security.exceptions.UnauthorizedDataAccessException;
import dev.naiarievilo.todoapp.security.jwt.JwtService;
import dev.naiarievilo.todoapp.todolists.dtos.TodoListDTO;
import dev.naiarievilo.todoapp.todolists.dtos.TodoListMapper;
import dev.naiarievilo.todoapp.todolists.exceptions.TodoListNotFoundException;
import dev.naiarievilo.todoapp.todolists.todos.Todo;
import dev.naiarievilo.todoapp.todolists.todos.TodoService;
import dev.naiarievilo.todoapp.todolists.todos.TodosTestHelper;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoMapper;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.ImmutableListException;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.TodoNotFoundException;
import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users.UserService;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.ACCESS_TOKEN;
import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.BEARER_PREFIX;
import static dev.naiarievilo.todoapp.todolists.ListTypes.*;
import static dev.naiarievilo.todoapp.todolists.TodoListControllerTestCases.*;
import static dev.naiarievilo.todoapp.todolists.TodoListService.CALENDAR_LIST_TITLE;
import static dev.naiarievilo.todoapp.todolists.TodoListsTestHelper.*;
import static dev.naiarievilo.todoapp.todolists.todos.TodosTestHelper.TODO_TASK_2;
import static dev.naiarievilo.todoapp.users.UsersTestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TodoListControllerIT extends ControllerIntegrationTests {

    @Autowired
    TodoListRepository listRepository;
    @Autowired
    TodoListService listService;
    @Autowired
    TodoService todoService;
    @Autowired
    UserService userService;
    @Autowired
    JwtService jwtService;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    TodoListMapper listMapper;
    @Autowired
    TodoMapper todoMapper;
    @Autowired
    MockMvc mockMvc;

    private TodoListDTO listDTO;
    private TodoListDTO otherListDTO;
    private String accessToken;
    private User user;

    @BeforeEach
    void setUp() {
        var userCreationDTO = new UserCreationDTO(EMAIL_1, PASSWORD_1, CONFIRM_PASSWORD_1, FIRST_NAME_1, LAST_NAME_1);
        user = userService.createUser(userCreationDTO);
        accessToken = jwtService.createToken(user, ACCESS_TOKEN);

        listDTO = new TodoListDTO(null, LIST_TITLE_1, null, null, null);
        otherListDTO = new TodoListDTO(null, LIST_TITLE_2, null, null, null);
    }

    @Test
    @DisplayName("getInboxList(): " + STATUS_200_RETURNS_INBOX_LIST_WHEN_USER_AUTHENTICATED)
    void getInboxList_UserAuthenticated_ReturnsUserInboxList() throws Exception {
        String responseBody = mockMvc.perform(get("/users/" + user.getId() + "/todolists/inbox")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
            )
            .andExpectAll(
                status().isOk(),
                content().contentTypeCompatibleWith(MediaType.parseMediaType(MediaTypes.HAL_JSON_VALUE))
            )
            .andReturn().getResponse().getContentAsString();

        TodoListDTO returnedListDTO = objectMapper.readValue(responseBody, TodoListDTO.class);
        assertEquals(INBOX.toString(), returnedListDTO.getTitle());
        assertEquals(INBOX, returnedListDTO.getType());
    }

    @Test
    @DisplayName("getTodayList(): " + STATUS_200_RETURNS_TODAY_LIST_WHEN_USER_AUTHENTICATED)
    void getTodayList_UserAuthenticated_ReturnsUserTodayList() throws Exception {
        String responseBody = mockMvc.perform(get("/users/" + user.getId() + "/todolists/today")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
            )
            .andExpectAll(
                status().isOk(),
                content().contentTypeCompatibleWith(MediaType.parseMediaType(MediaTypes.HAL_JSON_VALUE))
            )
            .andReturn().getResponse().getContentAsString();

        TodoListDTO returnedListDTO = objectMapper.readValue(responseBody, TodoListDTO.class);
        LocalDate today = LocalDate.now();
        assertEquals(today.format(CALENDAR_LIST_TITLE), returnedListDTO.getTitle());
        assertEquals(CALENDAR, returnedListDTO.getType());
        assertEquals(today, returnedListDTO.getDueDate());
    }

    @Test
    @DisplayName("getWeekLists(): " + STATUS_200_RETURNS_WEEK_LISTS_WHEN_USER_AUTHENTICATED)
    void getWeekLists_UserAuthenticated_ReturnsUserWeekLists() throws Exception {
        String responseBody = mockMvc.perform(get("/users/" + user.getId() + "/todolists/week")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
            )
            .andExpectAll(
                status().isOk(),
                content().contentType(MediaType.parseMediaType(MediaTypes.HAL_JSON_VALUE))
            )
            .andReturn().getResponse().getContentAsString();

        CollectionModel<TodoListDTO> listDTOSetModel =
            objectMapper.readValue(responseBody, new TypeReference<>() { });

        Collection<TodoListDTO> listDTOSet = listDTOSetModel.getContent();
        LocalDate dayOfWeek = START_OF_WEEK;
        for (TodoListDTO currListDTO : listDTOSet) {
            assertEquals(dayOfWeek.format(CALENDAR_LIST_TITLE), currListDTO.getTitle());
            assertEquals(CALENDAR, currListDTO.getType());
            assertEquals(dayOfWeek, currListDTO.getDueDate());
            dayOfWeek = dayOfWeek.plusDays(1);
        }
    }

    @Test
    @DisplayName("getAllCustomLists(): " + STATUS_200_RETURNS_ALL_CUSTOM_LISTS_WHEN_USER_AUTHENTICATED)
    void getAllCustomLists_UserAuthenticated_ReturnsUserCustomLists() throws Exception {
        listService.createList(user, listDTO, CUSTOM);
        listService.createList(user, otherListDTO, CUSTOM);

        String responseBody = mockMvc.perform(get("/users/" + user.getId() + "/todolists/custom")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
            )
            .andExpectAll(
                status().isOk(),
                content().contentType(MediaType.parseMediaType(MediaTypes.HAL_JSON_VALUE))
            )
            .andReturn().getResponse().getContentAsString();

        // Currently, the listDTOSetModel returns an empty content, even though the response contains both lists
        // created for the test. Need fix as soon as possible to not get false negative.
        CollectionModel<TodoListDTO> listDTOSetModel =
            objectMapper.readValue(responseBody, new TypeReference<>() { });

        Collection<TodoListDTO> listDTOSet = listDTOSetModel.getContent();
        for (TodoListDTO list : listDTOSet) {
            assertEquals(CUSTOM, list.getType());
        }
    }

    @Test
    @DisplayName("createList(): " + STATUS_200_CREATES_LIST_WHEN_USER_AUTHENTICATED)
    void createList_UserAuthenticated_CreatesList() throws Exception {
        String responseBody = mockMvc.perform(post("/users/" + user.getId() + "/todolists")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.parseMediaType(MediaTypes.HAL_JSON_VALUE))
                .content(objectMapper.writeValueAsString(listDTO))
            )
            .andExpectAll(
                status().isCreated(),
                content().contentType(MediaType.parseMediaType(MediaTypes.HAL_JSON_VALUE))
            )
            .andReturn().getResponse().getContentAsString();

        TodoListDTO returnedListDTO = objectMapper.readValue(responseBody, TodoListDTO.class);
        assertNotNull(returnedListDTO);
        assertNotNull(returnedListDTO.getId());
        assertNotNull(returnedListDTO.getCreatedAt());
        assertEquals(CUSTOM, returnedListDTO.getType());
    }

    @Test
    @DisplayName("updateList(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_LIST_NOT_FOUND)
    void updateList_ListDoesNotExist_ReturnsErrorDetails() throws Exception {
        Long fakeListId = 3423L;
        TodoListDTO fakeListDTO = new TodoListDTO(fakeListId, LIST_TITLE_1, CUSTOM, null, null);
        var exception = new TodoListNotFoundException(fakeListId);

        String responseBody = mockMvc.perform(put("/users/" + user.getId() + "/todolists/" + fakeListId)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fakeListDTO))
            )
            .andExpectAll(
                status().isNotFound(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andReturn().getResponse().getContentAsString();

        ErrorDetails errorDetails = objectMapper.readValue(responseBody, ErrorDetails.class);
        assertNotNull(errorDetails.getTimestamp());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorDetails.getStatus());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), errorDetails.getReason());
        assertTrue(errorDetails.getMessages().contains(exception.getMessage()));
    }

    @Test
    @DisplayName("updateList(): " + STATUS_401_RETURNS_ERROR_MESSAGE_WHEN_USER_DOES_NOT_HAVE_LIST_ACCESS)
    void updateList_UserDoesNotHaveListAccess_ReturnsErrorDetails() throws Exception {
        var otherUserCreationDTO =
            new UserCreationDTO(EMAIL_2, PASSWORD_2, CONFIRM_PASSWORD_2, FIRST_NAME_2, LAST_NAME_2);
        User otherUser = userService.createUser(otherUserCreationDTO);
        TodoList otherList = listService.createList(otherUser, otherListDTO, CUSTOM);
        TodoListDTO updatedOtherList = new TodoListDTO(otherList.getId(), LIST_TITLE_3, null, null, null);

        var exception = new UnauthorizedDataAccessException();

        String responseBody = mockMvc.perform(put("/users/" + user.getId() + "/todolists/" + otherList.getId())
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedOtherList))
            )
            .andExpectAll(
                status().isUnauthorized(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andReturn().getResponse().getContentAsString();

        ErrorDetails errorDetails = objectMapper.readValue(responseBody, ErrorDetails.class);
        assertNotNull(errorDetails.getTimestamp());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), errorDetails.getStatus());
        assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), errorDetails.getReason());
        assertTrue(errorDetails.getMessages().contains(exception.getMessage()));
    }

    @Test
    @DisplayName("updateList(): +" + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_USER_TRIES_TO_UPDATE_CALENDAR_OR_INBOX)
    void updateList_UpdateCalendarOrInbox_ReturnsErrorDetails() throws Exception {
        TodoList list = listService.createList(user, listDTO, INBOX);
        ListTypes listType = list.getType();
        TodoListDTO updatedListDTO = new TodoListDTO(
            list.getId(), LIST_TITLE_2, list.getType(), list.getCreatedAt(), list.getDueDate()
        );

        var exception = new ImmutableListException(listType.getType());

        String responseBody = mockMvc.perform(put("/users/" + user.getId() + "/todolists/" + list.getId())
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedListDTO))
            )
            .andExpectAll(
                status().isBadRequest(),
                content().contentType(MediaType.APPLICATION_JSON)
            )
            .andReturn().getResponse().getContentAsString();

        ErrorDetails errorDetails = objectMapper.readValue(responseBody, ErrorDetails.class);
        assertNotNull(errorDetails.getTimestamp());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorDetails.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), errorDetails.getReason());
        assertTrue(errorDetails.getMessages().contains(exception.getMessage()));
    }

    @Test
    @DisplayName("updateList(): " + STATUS_204_UPDATES_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void updateList_UserHasListAccess_UpdatesList() throws Exception {
        TodoList list = listService.createList(user, listDTO, CUSTOM);
        TodoListDTO updatedListDTO =
            new TodoListDTO(list.getId(), LIST_TITLE_2, list.getType(), list.getCreatedAt(), list.getDueDate());

        mockMvc.perform(put("/users/" + user.getId() + "/todolists/" + list.getId())
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedListDTO))
            )
            .andExpect(status().isNoContent());

        assertEquals(list.getTitle(), updatedListDTO.getTitle());
    }

    @Test
    @DisplayName("deleteList(): " + STATUS_204_DELETES_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void deleteList_UserHasListAccess_DeleteList() throws Exception {
        TodoList list = listService.createList(user, listDTO, CUSTOM);

        mockMvc.perform(delete("/users/" + user.getId() + "/todolists/" + list.getId())
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
            )
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("addTodoToList(): " + STATUS_201_ADDS_TODO_TO_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void addTodoToList_UserHasListAccess_AddsTodoToList() throws Exception {
        TodoList list = listService.createList(user, listDTO, CUSTOM);

        TodoDTO newTodo = TodosTestHelper.newTodoDTO_1();

        String responseBody = mockMvc.perform(post("/users/" + user.getId() + "/todolists/" + list.getId() + "/todos")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTodo))
            )
            .andExpectAll(
                status().isCreated(),
                content().contentTypeCompatibleWith(MediaType.parseMediaType(MediaTypes.HAL_JSON_VALUE))
            )
            .andReturn().getResponse().getContentAsString();

        TodoDTO createdTodo = objectMapper.readValue(responseBody, TodoDTO.class);
        assertNotNull(createdTodo.getId());
        assertNotNull(createdTodo.getCreatedAt());
        assertEquals(newTodo.getTask(), createdTodo.getTask());
        assertEquals(newTodo.getCompleted(), createdTodo.getCompleted());
    }

    @Test
    @DisplayName("updateTodoFromList(): " + STATUS_404_RETURNS_ERROR_MESSAGE_WHEN_TODO_NOT_FOUND)
    void updateTodoFromList_TodoDoesNotExist_ReturnsErrorDetails() throws Exception {
        TodoList list = listService.createList(user, listDTO, CUSTOM);
        TodoDTO newTodo = TodosTestHelper.newTodoDTO_1();
        Todo todo = todoService.createTodo(newTodo, list);
        TodoDTO updatedTodo = new TodoDTO(todo.getId(), TODO_TASK_2, todo.isCompleted(), todo.getPosition(),
            todo.getCreatedAt(), todo.getDueDate());

        todoService.deleteTodo(todo, list);
        var exception = new TodoNotFoundException(todo.getId());

        String responseBody = mockMvc.perform(
                put("/users/" + user.getId() + "/todolists/" + list.getId() + "/todos/" + todo.getId())
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedTodo))
            )
            .andExpectAll(
                status().isNotFound(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andReturn().getResponse().getContentAsString();

        ErrorDetails errorDetails = objectMapper.readValue(responseBody, ErrorDetails.class);
        assertNotNull(errorDetails.getTimestamp());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorDetails.getStatus());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), errorDetails.getReason());
        assertTrue(errorDetails.getMessages().contains(exception.getMessage()));
    }

    @Test
    @DisplayName("updateTodoFromList(): " + STATUS_204_UPDATES_TODO_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void updateTodoFromList_UserHasListAccess_UpdatesTodoFromList() throws Exception {
        TodoList list = listService.createList(user, listDTO, CUSTOM);
        TodoDTO newTodo = TodosTestHelper.newTodoDTO_1();
        Todo todo = todoService.createTodo(newTodo, list);
        TodoDTO updatedTodo = new TodoDTO(todo.getId(), TODO_TASK_2, todo.isCompleted(), todo.getPosition(),
            todo.getCreatedAt(), todo.getDueDate());

        mockMvc.perform(put("/users/" + user.getId() + "/todolists/" + list.getId() + "/todos/" + todo.getId())
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedTodo))
            )
            .andExpect(status().isNoContent());

        assertEquals(TODO_TASK_2, todo.getTask());
    }

    @Test
    @DisplayName("updateTodosFromList(): " + STATUS_204_UPDATES_TODOS_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void updateTodosFromList_UserHasListAccess_UpdatesTodosFromList() throws Exception {
        TodoList list = listService.createList(user, listDTO, CUSTOM);
        Set<TodoDTO> newTodoDTOSet = TodosTestHelper.newTodoDTOSet();
        Set<Todo> persistedTodoSet = new LinkedHashSet<>();
        for (TodoDTO newTodoDTO : newTodoDTOSet) {
            Todo todo = todoService.createTodo(newTodoDTO, list);
            persistedTodoSet.add(todo);
        }

        Set<TodoDTO> updatedTodoDTOSet = new LinkedHashSet<>();
        int newPosition = persistedTodoSet.size();
        for (Todo persistedTodo : persistedTodoSet) {
            TodoDTO updatedTodoDTO = new TodoDTO(
                persistedTodo.getId(),
                persistedTodo.getTask(),
                persistedTodo.isCompleted(),
                newPosition--,
                null,
                persistedTodo.getDueDate()
            );
            updatedTodoDTOSet.add(updatedTodoDTO);
        }

        mockMvc.perform(put("/users/" + user.getId() + "/todolists/" + list.getId() + "/todos")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedTodoDTOSet))
            )
            .andExpect(status().isNoContent());

        newPosition = persistedTodoSet.size();
        for (Todo persistedTodo : persistedTodoSet) {
            assertEquals(newPosition--, persistedTodo.getPosition());
        }
    }

    @Test
    @DisplayName("removeTodoFromList(): " + STATUS_204_DELETES_TODO_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void removeTodoFromList_UserHasListAccess_RemoveTodoFromList() throws Exception {
        TodoList list = listService.createList(user, listDTO, CUSTOM);
        TodoDTO newTodo = TodosTestHelper.newTodoDTO_1();
        Todo todo = todoService.createTodo(newTodo, list);

        mockMvc.perform(delete("/users/" + user.getId() + "/todolists/" + list.getId() + "/todos/" + todo.getId())
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
            )
            .andExpect(status().isNoContent());

        assertEquals(0, list.getTodos().size());
    }

    @Test
    @DisplayName("removeTodosFromList(): " + STATUS_204_DELETES_TODOS_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void removeTodosFromList_UserHasListAccess_RemovesAllTodosFromList() throws Exception {
        TodoList list = listService.createList(user, listDTO, CUSTOM);
        Set<TodoDTO> newTodoDTOSet = TodosTestHelper.newTodoDTOSet();
        Set<Long> todosId = new HashSet<>();
        for (TodoDTO newTodoDTO : newTodoDTOSet) {
            Todo todo = todoService.createTodo(newTodoDTO, list);
            todosId.add(todo.getId());
        }

        mockMvc.perform(delete("/users/" + user.getId() + "/todolists/" + list.getId() + "/todos")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(todosId))
            )
            .andExpect(status().isNoContent());

        assertTrue(list.getTodos().isEmpty());
    }

    @Test
    @DisplayName("removeTodosFromList(): " + STATUS_204_DELETES_ALL_TODOS_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void removeAllTodosFromList_UserHasListAccess_RemovesAllTodosFromList() throws Exception {
        TodoList list = listService.createList(user, listDTO, CUSTOM);
        Set<TodoDTO> newTodoDTOSet = TodosTestHelper.newTodoDTOSet();
        for (TodoDTO newTodoDTO : newTodoDTOSet) {
            todoService.createTodo(newTodoDTO, list);
        }

        mockMvc.perform(delete("/users/" + user.getId() + "/todolists/" + list.getId() + "/todos")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LinkedHashSet<>()))
            )
            .andExpect(status().isNoContent());

        assertTrue(list.getTodos().isEmpty());
    }
}