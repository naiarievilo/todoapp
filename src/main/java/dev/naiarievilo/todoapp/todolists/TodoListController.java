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
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static dev.naiarievilo.todoapp.todolists.ListTypes.CUSTOM;

@RestController
@RequestMapping("/users/current/todolists")
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
    public TodoListDTO getInboxList(@AuthenticatedUser User user) {
        TodoList list = listService.getInboxList(user);
        return listMapper.toDTO(list);
    }

    @GetMapping("/today")
    @ResponseStatus(HttpStatus.OK)
    public TodoListDTO getTodayList(@AuthenticatedUser User user) {
        TodoList list = listService.getTodayList(user);
        return listMapper.toDTO(list);
    }

    @GetMapping("/week")
    @ResponseStatus(HttpStatus.OK)
    public Set<TodoListDTO> getWeekLists(@AuthenticatedUser User user) {
        Set<TodoList> weeklyLists = listService.getWeeklyLists(user);
        return listMapper.toSetDTO(weeklyLists);
    }

    @GetMapping("/custom")
    @ResponseStatus(HttpStatus.OK)
    public Set<TodoListDTO> getCustomLists(@AuthenticatedUser User user) {
        Set<TodoList> customLists = listService.getAllCustomLists(user);
        return listMapper.toSetDTO(customLists);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TodoListDTO createList(@AuthenticatedUser User user,
        @RequestBody @Validated(Creation.class) TodoListDTO listDTO) {
        TodoList newList = listService.createList(user, listDTO, CUSTOM);
        return listMapper.toDTO(newList);
    }

    @PutMapping("/{listId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateList(@AuthenticatedUser User user, @PathVariable Long listId,
        @RequestBody @Validated(Update.class) TodoListDTO listDTO) {
        listService.updateList(user.getId(), listId, listDTO);
    }

    @DeleteMapping("/{listId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteList(@AuthenticatedUser User user, @PathVariable Long listId) {
        listService.deleteList(user.getId(), listId);
    }

    @PostMapping("{listId}/todos")
    @ResponseStatus(HttpStatus.CREATED)
    public TodoDTO addTodoToList(@AuthenticatedUser User user, @PathVariable Long listId,
        @RequestBody @Validated(Creation.class) TodoDTO todoDTO) {
        Todo newTodo = listService.addNewTodoToList(user.getId(), listId, todoDTO);
        return todoMapper.toDTO(newTodo);
    }

    @PutMapping("{listId}/todos/{todoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTodoFromList(@AuthenticatedUser User user, @PathVariable Long listId, @PathVariable Long todoId,
        @RequestBody @Validated(Update.class) TodoDTO todoDTO) {
        listService.updateTodoFromList(user.getId(), listId, todoId, todoDTO);
    }

    @DeleteMapping("{listId}/todos/{todoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTodoFromList(@AuthenticatedUser User user, @PathVariable Long listId, @PathVariable Long todoId) {
        listService.removeTodoFromList(user.getId(), listId, todoId);
    }
}
