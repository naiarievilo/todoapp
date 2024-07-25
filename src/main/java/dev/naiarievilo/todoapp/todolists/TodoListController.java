package dev.naiarievilo.todoapp.todolists;

import dev.naiarievilo.todoapp.security.AuthenticatedUser;
import dev.naiarievilo.todoapp.todolists.dtos.TodoListDTO;
import dev.naiarievilo.todoapp.todolists.dtos.TodoListMapper;
import dev.naiarievilo.todoapp.todolists.todos.Todo;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoMapper;
import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.validation.groups.Creation;
import dev.naiarievilo.todoapp.validation.groups.Update;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Set;

import static dev.naiarievilo.todoapp.todolists.ListTypes.CUSTOM;
import static dev.naiarievilo.todoapp.todolists.TodoListOpenAPIExamples.*;

@Tag(name = "To-do list API")
@SecurityRequirement(name = "Access Token")
@RestController
@RequestMapping(path = "/users/{userId}/todolists", produces = MediaTypes.HAL_JSON_VALUE)
public class TodoListController {

    private final TodoListService listService;
    private final TodoListMapper listMapper;
    private final TodoMapper todoMapper;

    public TodoListController(TodoListService listService, TodoListMapper listMapper, TodoMapper todoMapper) {
        this.listService = listService;
        this.listMapper = listMapper;
        this.todoMapper = todoMapper;
    }

    @Operation(
        summary = "Get inbox list",
        description = "Returns the user's inbox list. Each user has exactly one inbox list to add general to-dos.",
        responses = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = TodoListDTO.class),
                examples = @ExampleObject(value = INBOX_LIST_EXAMPLE))
            )
        }
    )
    @GetMapping("/inbox")
    @ResponseStatus(HttpStatus.OK)
    public TodoListDTO getInboxList(@AuthenticatedUser User user, @PathVariable Long userId) {
        TodoList list = listService.getInboxList(user);
        return listMapper.toModel(list, userId);
    }

    @Operation(
        summary = "Get today's list",
        description = "Returns the user's calendar list for the day. The list is automatically created if absent.",
        responses = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = TodoListDTO.class),
                examples = @ExampleObject(value = TODAY_LIST_EXAMPLE))
            )
        }
    )
    @GetMapping("/today")
    @ResponseStatus(HttpStatus.OK)
    public TodoListDTO getTodayList(@AuthenticatedUser User user, @PathVariable Long userId) {
        TodoList list = listService.getTodayList(user);
        return listMapper.toModel(list, userId);
    }

    @Operation(
        summary = "Get this week's lists",
        description = "Returns the user's calendar lists for the week. The lists are automatically created if absent.",
        responses = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = TodoListDTO.class),
                examples = @ExampleObject(WEEK_LISTS_EXAMPLE))
            )
        }
    )
    @GetMapping("/week")
    @ResponseStatus(HttpStatus.OK)
    public CollectionModel<TodoListDTO> getWeekLists(@AuthenticatedUser User user, @PathVariable Long userId) {
        Set<TodoList> weeklyLists = listService.getWeeklyLists(user);
        return listMapper.toModels(weeklyLists, userId);
    }

    @Operation(
        summary = "Get user's custom lists",
        description = "Returns the user's custom lists. Unlike inbox and calendar lists, custom lists' are retrieved " +
            "without its to-dos to decrease the response's payload size.",
        responses = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = TodoListDTO.class),
                examples = @ExampleObject(value = CUSTOM_LISTS_EXAMPLE))
            )
        }
    )
    @GetMapping("/custom")
    @ResponseStatus(HttpStatus.OK)
    public CollectionModel<TodoListDTO> getCustomLists(@AuthenticatedUser User user, @PathVariable Long userId) {
        Set<TodoList> customLists = listService.getAllCustomLists(user);
        return listMapper.toModels(customLists, userId);
    }

    @Operation(
        summary = "Create list",
        description = "Creates a custom list for the user. Currently, only custom lists can be created by the user.",
        responses = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = TodoListDTO.class),
                examples = @ExampleObject(value = NEW_CUSTOM_LIST_EXAMPLE))),
        }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TodoListDTO createList(
        @AuthenticatedUser User user,
        @PathVariable Long userId,
        @RequestBody @Validated(Creation.class) TodoListDTO listDTO
    ) {
        TodoList newList = listService.createList(user, listDTO, CUSTOM);
        return listMapper.toModel(newList, userId);
    }

    @Operation(
        summary = "Get list",
        responses = {
            @ApiResponse(responseCode = "200",
                content = @Content(schema = @Schema(implementation = TodoListDTO.class), examples = @ExampleObject(
                    value = INBOX_LIST_EXAMPLE))
            )
        }
    )
    @GetMapping("/{listId}")
    @ResponseStatus(HttpStatus.OK)
    public TodoListDTO getList(@PathVariable Long userId, @PathVariable Long listId) {
        TodoList list = listService.getListByIdEagerly(userId, listId);
        return listMapper.toModel(list, userId);
    }

    @Operation(
        summary = "Update list",
        description = "Updates list. Currently, only custom lists can be updated by the user, and the update is " +
            "restricted to the list's title.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            schema = @Schema(implementation = TodoListDTO.class),
            examples = @ExampleObject(value = "{\"title\": \"New Title\"}")
        ))
    )
    @PutMapping("/{listId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> updateList(
        @PathVariable Long userId,
        @PathVariable Long listId,
        @RequestBody @Validated(Update.class) TodoListDTO listDTO
    ) {
        listService.updateList(userId, listId, listDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete list", description = "Deletes a list. Currently, only custom lists can be deleted by" +
        " the user.")
    @DeleteMapping("/{listId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteList(@PathVariable Long userId, @PathVariable Long listId) {
        listService.deleteList(userId, listId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Get to-dos from list",
        responses = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = TodoDTO.class),
                examples = @ExampleObject(value = TODOS_EXAMPLE)))
        }
    )
    @GetMapping("/{listId}/todos")
    @ResponseStatus(HttpStatus.OK)
    public CollectionModel<TodoDTO> getTodosFromList(@PathVariable Long userId, @PathVariable Long listId) {
        Set<Todo> todos = listService.getTodosFromList(userId, listId);
        return todoMapper.toModels(todos, userId, listId);
    }

    @Operation(
        summary = "Add to-do to list",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            schema = @Schema(implementation = TodoDTO.class),
            examples = @ExampleObject(value = NEW_TODO_EXAMPLE)
        ))
    )
    @PostMapping("/{listId}/todos")
    @ResponseStatus(HttpStatus.CREATED)
    public TodoDTO addTodoToList(
        @PathVariable Long userId,
        @PathVariable Long listId,
        @RequestBody @Validated(Creation.class) TodoDTO todoDTO
    ) {
        Todo newTodo = listService.addNewTodoToList(userId, listId, todoDTO);
        return todoMapper.toModel(newTodo, userId, listId);
    }

    @Operation(
        summary = "Update to-dos from list",
        description = "Updates to-dos from a list. The position of to-dos within a list must be unique and not exceed" +
            " the list's size.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            schema = @Schema(implementation = TodoDTO.class),
            examples = @ExampleObject(value = UPDATED_TODOS_EXAMPLE)
        ))
    )
    @PutMapping("/{listId}/todos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> updateTodosFromList(
        @PathVariable Long userId,
        @PathVariable Long listId,
        @RequestBody Set<@Valid TodoDTO> todosDTO
    ) {
        listService.updateTodosFromList(userId, listId, todosDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Remove to-dos from list",
        description = "Remove to-dos from a list. The id of the to-dos to be removed must be provided in the payload " +
            "as an array. The position of the remaining to-dos are adjusted accordingly.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            schema = @Schema(implementation = ArrayList.class),
            examples = @ExampleObject(value = "[4, 5, 6]")
        ))
    )
    @Nullable
    @DeleteMapping("/{listId}/todos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> removeTodosFromList(
        @PathVariable Long userId,
        @PathVariable Long listId,
        @RequestBody(required = false) Set<Long> todosId
    ) {
        if (todosId.isEmpty()) {
            listService.removeTodosFromList(userId, listId);
        } else {
            listService.removeTodosFromList(userId, listId, todosId);
        }

        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Get to-do from list",
        responses = {@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation =
            TodoDTO.class), examples = @ExampleObject(value = TODO_EXAMPLE)))
        }
    )
    @GetMapping("/{listId}/todos/{todoId}")
    @ResponseStatus(HttpStatus.OK)
    public TodoDTO getTodoFromList(@PathVariable Long userId, @PathVariable Long listId, @PathVariable Long todoId) {
        Todo todo = listService.getTodoFromList(userId, listId, todoId);
        return todoMapper.toModel(todo, userId, listId);
    }

    @Operation(
        summary = "Update to-do from list. The position of to-dos within a list must be unique and not exceed the " +
            "list's size.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            schema = @Schema(implementation = TodoDTO.class),
            examples = @ExampleObject(value = UPDATED_TODO_EXAMPLE)
        ))
    )
    @PutMapping("/{listId}/todos/{todoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> updateTodoFromList(
        @PathVariable Long userId,
        @PathVariable Long listId,
        @PathVariable Long todoId,
        @RequestBody @Valid TodoDTO todoDTO
    ) {
        listService.updateTodoFromList(userId, listId, todoId, todoDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Remove to-do from list",
        description = "Removes to-do from a list. The position of the remaining to-dos in the list are adjusted " +
            "accordingly."
    )
    @DeleteMapping("/{listId}/todos/{todoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> removeTodoFromList(
        @PathVariable Long userId,
        @PathVariable Long listId,
        @PathVariable Long todoId
    ) {
        listService.removeTodoFromList(userId, listId, todoId);
        return ResponseEntity.noContent().build();
    }
}
