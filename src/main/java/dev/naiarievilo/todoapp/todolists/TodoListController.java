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
import jakarta.validation.Valid;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static dev.naiarievilo.todoapp.todolists.ListTypes.CUSTOM;

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

    @GetMapping("/inbox")
    @ResponseStatus(HttpStatus.OK)
    public TodoListDTO getInboxList(@AuthenticatedUser User user, @PathVariable Long userId) {
        TodoList list = listService.getInboxList(user);
        return listMapper.toModel(list, userId);
    }

    @GetMapping("/today")
    @ResponseStatus(HttpStatus.OK)
    public TodoListDTO getTodayList(@AuthenticatedUser User user, @PathVariable Long userId) {
        TodoList list = listService.getTodayList(user);
        return listMapper.toModel(list, userId);
    }

    @GetMapping("/week")
    @ResponseStatus(HttpStatus.OK)
    public Set<TodoListDTO> getWeekLists(@AuthenticatedUser User user, @PathVariable Long userId) {
        Set<TodoList> weeklyLists = listService.getWeeklyLists(user);
        return listMapper.toModels(weeklyLists, userId);
    }

    @GetMapping("/custom")
    @ResponseStatus(HttpStatus.OK)
    public Set<TodoListDTO> getCustomLists(@AuthenticatedUser User user, @PathVariable Long userId) {
        Set<TodoList> customLists = listService.getAllCustomLists(user);
        return listMapper.toModels(customLists, userId);
    }

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

    @GetMapping("/{listId}")
    @ResponseStatus(HttpStatus.OK)
    public TodoListDTO getList(@PathVariable Long userId, @PathVariable Long listId) {
        TodoList list = listService.getListByIdEagerly(userId, listId);
        return listMapper.toModel(list, userId);
    }

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

    @DeleteMapping("/{listId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteList(@PathVariable Long userId, @PathVariable Long listId) {
        listService.deleteList(userId, listId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{listId}/todos")
    @ResponseStatus(HttpStatus.OK)
    public Set<TodoDTO> getTodosFromList(@PathVariable Long userId, @PathVariable Long listId) {
        Set<Todo> todos = listService.getTodosFromList(userId, listId);
        return todoMapper.toModels(todos, userId, listId);
    }

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

    @GetMapping("/{listId}/todos/{todoId}")
    @ResponseStatus(HttpStatus.OK)
    public TodoDTO getTodoFromList(@PathVariable Long userId, @PathVariable Long listId, @PathVariable Long todoId) {
        Todo todo = listService.getTodoFromList(userId, listId, todoId);
        return todoMapper.toModel(todo, userId, listId);
    }

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
