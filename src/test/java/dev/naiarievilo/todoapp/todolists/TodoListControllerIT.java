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
import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users.UserService;
import dev.naiarievilo.todoapp.users.dtos.UserCreationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.ACCESS_TOKEN;
import static dev.naiarievilo.todoapp.security.jwt.JwtTokens.BEARER_PREFIX;
import static dev.naiarievilo.todoapp.todolists.ListTypes.*;
import static dev.naiarievilo.todoapp.todolists.TodoListControllerTestCases.*;
import static dev.naiarievilo.todoapp.todolists.TodoListServiceImpl.CALENDAR_LIST_TITLE;
import static dev.naiarievilo.todoapp.todolists.TodoListsTestHelper.*;
import static dev.naiarievilo.todoapp.todolists.todos.TodosTestHelper.TODO_TASK_2;
import static dev.naiarievilo.todoapp.users.UsersTestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TodoListControllerIT extends ControllerIntegrationTests {

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

        listDTO = new TodoListDTO(null, LIST_TITLE_1, null, null, null, null);
        otherListDTO = new TodoListDTO(null, LIST_TITLE_2, null, null, null, null);
    }

    @Test
    @DisplayName("getInboxList(): " + STATUS_200_RETURNS_INBOX_LIST_WHEN_USER_AUTHENTICATED)
    void getInboxList_UserAuthenticated_ReturnsUserInboxList() throws Exception {
        String responseBody = mockMvc.perform(get("/users/current/todolists/inbox")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
            )
            .andExpectAll(
                status().isOk(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andReturn().getResponse().getContentAsString();

        TodoListDTO returnedListDTO = objectMapper.readValue(responseBody, TodoListDTO.class);
        assertEquals(INBOX.toString(), returnedListDTO.title());
        assertEquals(INBOX, returnedListDTO.type());
    }

    @Test
    @DisplayName("getTodayList(): " + STATUS_200_RETURNS_TODAY_LIST_WHEN_USER_AUTHENTICATED)
    void getTodayList_UserAuthenticated_ReturnsUserTodayList() throws Exception {
        String responseBody = mockMvc.perform(get("/users/current/todolists/today")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
            )
            .andExpectAll(
                status().isOk(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andReturn().getResponse().getContentAsString();

        TodoListDTO returnedListDTO = objectMapper.readValue(responseBody, TodoListDTO.class);
        LocalDate today = LocalDate.now();
        assertEquals(today.format(CALENDAR_LIST_TITLE), returnedListDTO.title());
        assertEquals(CALENDAR, returnedListDTO.type());
        assertEquals(today, returnedListDTO.dueDate());
    }

    @Test
    @DisplayName("getWeekLists(): " + STATUS_200_RETURNS_WEEK_LISTS_WHEN_USER_AUTHENTICATED)
    void getWeekLists_UserAuthenticated_ReturnsUserWeekLists() throws Exception {
        String responseBody = mockMvc.perform(get("/users/current/todolists/week")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
            )
            .andExpectAll(
                status().isOk(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andReturn().getResponse().getContentAsString();

        Set<TodoListDTO> listDTOSet =
            objectMapper.readValue(responseBody, new TypeReference<LinkedHashSet<TodoListDTO>>() { });
        LocalDate dayOfWeek = START_OF_WEEK;
        for (TodoListDTO currListDTO : listDTOSet) {
            assertEquals(dayOfWeek.format(CALENDAR_LIST_TITLE), currListDTO.title());
            assertEquals(CALENDAR, currListDTO.type());
            assertEquals(dayOfWeek, currListDTO.dueDate());
            dayOfWeek = dayOfWeek.plusDays(1);
        }
    }

    @Test
    @DisplayName("getAllCustomLists(): " + STATUS_200_RETURNS_ALL_CUSTOM_LISTS_WHEN_USER_AUTHENTICATED)
    void getAllCustomLists_UserAuthenticated_ReturnsUserCustomLists() throws Exception {
        listService.createList(user, listDTO, CUSTOM);
        listService.createList(user, otherListDTO, CUSTOM);

        String responseBody = mockMvc.perform(get("/users/current/todolists/custom")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
            )
            .andExpectAll(
                status().isOk(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andReturn().getResponse().getContentAsString();

        Set<TodoListDTO> listDTOSet =
            objectMapper.readValue(responseBody, new TypeReference<LinkedHashSet<TodoListDTO>>() { });
        assertEquals(2, listDTOSet.size());
        for (TodoListDTO list : listDTOSet) {
            assertEquals(CUSTOM, list.type());
        }
    }

    @Test
    @DisplayName("createList(): " + STATUS_200_CREATES_LIST_WHEN_USER_AUTHENTICATED)
    void createList_UserAuthenticated_CreatesList() throws Exception {
        String responseBody = mockMvc.perform(post("/users/current/todolists")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(listDTO))
            )
            .andExpectAll(
                status().isCreated(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andReturn().getResponse().getContentAsString();

        TodoListDTO returnedListDTO = objectMapper.readValue(responseBody, TodoListDTO.class);
        assertNotNull(returnedListDTO);
        assertNotNull(returnedListDTO.id());
        assertNotNull(returnedListDTO.createdAt());
        assertEquals(CUSTOM, returnedListDTO.type());
    }

    @Test
    @DisplayName("updateList(): " + STATUS_400_RETURNS_ERROR_MESSAGE_WHEN_LIST_NOT_FOUND)
    void updateList_ListDoesNotExist_ReturnsErrorDetails() throws Exception {
        Long fakeListId = 3423L;
        TodoListDTO fakeListDTO = new TodoListDTO(fakeListId, LIST_TITLE_1, CUSTOM, null, null, null);
        var exception = new TodoListNotFoundException(fakeListId);

        String responseBody = mockMvc.perform(put("/users/current/todolists/" + fakeListId)
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
        TodoListDTO updatedOtherList = new TodoListDTO(otherList.getId(), LIST_TITLE_3, null, null, null, null);

        var exception = new UnauthorizedDataAccessException();

        String responseBody = mockMvc.perform(put("/users/current/todolists/" + otherList.getId())
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
    @DisplayName("updateList(): " + STATUS_204_UPDATES_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void updateList_UserHasListAccess_UpdatesList() throws Exception {
        TodoList list = listService.createList(user, listDTO, CUSTOM);
        TodoListDTO updatedListDTO =
            new TodoListDTO(list.getId(), LIST_TITLE_2, list.getType(), list.getCreatedAt(), list.getDueDate(), null);

        mockMvc.perform(put("/users/current/todolists/" + list.getId())
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedListDTO))
            )
            .andExpect(status().isNoContent());

        assertEquals(list.getTitle(), updatedListDTO.title());
    }

    @Test
    @DisplayName("deleteList(): " + STATUS_204_DELETES_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void deleteList_UserHasListAccess_DeleteList() throws Exception {
        TodoList list = listService.createList(user, listDTO, CUSTOM);

        mockMvc.perform(delete("/users/current/todolists/" + list.getId())
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
            )
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("addTodoToList(): " + STATUS_201_ADDS_TODO_TO_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void addTodoToList_UserHasListAccess_AddsTodoToList() throws Exception {
        TodoList list = listService.createList(user, listDTO, CUSTOM);

        TodoDTO newTodo = TodosTestHelper.newTodoDTO_1();

        String responseBody = mockMvc.perform(post("/users/current/todolists/" + list.getId() + "/todos")
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTodo))
            )
            .andExpectAll(
                status().isCreated(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andReturn().getResponse().getContentAsString();

        TodoDTO createdTodo = objectMapper.readValue(responseBody, TodoDTO.class);
        assertNotNull(createdTodo.id());
        assertNotNull(createdTodo.createdAt());
        assertEquals(newTodo.task(), createdTodo.task());
        assertEquals(newTodo.completed(), createdTodo.completed());
    }

    @Test
    @DisplayName("updateTodoFromList(): " + STATUS_204_UPDATES_TODO_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void updateTodoFromList_UserHasListAccess_UpdatesTodoFromList() throws Exception {
        TodoList list = listService.createList(user, listDTO, CUSTOM);
        TodoDTO newTodo = TodosTestHelper.newTodoDTO_1();
        Todo todo = todoService.createTodo(newTodo, list);
        TodoDTO updatedTodo = new TodoDTO(todo.getId(), TODO_TASK_2, todo.isCompleted(), todo.getPosition(),
            todo.getCreatedAt(), todo.getDueDate());

        mockMvc.perform(put("/users/current/todolists/" + list.getId() + "/todos/" + todo.getId())
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedTodo))
            )
            .andExpect(status().isNoContent());

        assertEquals(TODO_TASK_2, todo.getTask());
    }

    @Test
    @DisplayName("removeTodoFromList(): " + STATUS_204_DELETES_TODO_FROM_LIST_WHEN_USER_HAS_LIST_ACCESS)
    void updateTodoFromList_UserHasListAccess_RemoveTodoFromList() throws Exception {
        TodoList list = listService.createList(user, listDTO, CUSTOM);
        TodoDTO newTodo = TodosTestHelper.newTodoDTO_1();
        Todo todo = todoService.createTodo(newTodo, list);

        mockMvc.perform(delete("/users/current/todolists/" + list.getId() + "/todos/" + todo.getId())
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
            )
            .andExpect(status().isNoContent());

        assertEquals(0, list.getTodos().size());
    }
}
