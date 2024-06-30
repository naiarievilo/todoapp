package dev.naiarievilo.todoapp.todolists.todos;

import dev.naiarievilo.todoapp.todolists.TodoList;
import dev.naiarievilo.todoapp.todolists.TodoListService;
import dev.naiarievilo.todoapp.todolists.TodoParent;
import dev.naiarievilo.todoapp.todolists.todo_groups.TodoGroup;
import dev.naiarievilo.todoapp.todolists.todo_groups.TodoGroupService;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoMapper;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.TodoNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final TodoGroupService groupService;
    private final TodoListService listService;
    private final TodoMapper todoMapper;

    public TodoServiceImpl(TodoRepository todoRepository, TodoGroupService groupService,
        TodoListService listService, TodoMapper todoMapper) {
        this.todoRepository = todoRepository;
        this.groupService = groupService;
        this.listService = listService;
        this.todoMapper = todoMapper;
    }

    private Todo getTodoByIdWithGroup(Long id) {
        return todoRepository.findByIdWithGroup(id).orElseThrow(() -> new TodoNotFoundException(id));
    }

    private Todo getTodoByIdWithList(Long id) {
        return todoRepository.findByIdWithList(id).orElseThrow(() -> new TodoNotFoundException(id));
    }


    @Override
    public Todo getTodoById(Long id) {
        return todoRepository.findById(id).orElseThrow(() -> new TodoNotFoundException(id));
    }

    @Override
    @Transactional
    public TodoDTO createTodo(TodoDTO todoDTO) {
        Todo newTodo = todoMapper.toEntity(todoDTO);

        Long listId = todoDTO.listId();
        Long groupId = todoDTO.groupId();
        TodoParent parent;
        if (listId != null) {
            parent = listService.getListByIdWithTodos(listId);
        } else {
            parent = groupService.getGroupByIdWithTodos(groupId);
        }

        parent.addTodo(newTodo);
        todoRepository.persist(newTodo);
        return todoMapper.toDTO(newTodo);
    }

    @Override
    @Transactional
    public TodoDTO updateTodo(TodoDTO todoDTO) {
        Todo todo = getTodoById(todoDTO.id());
        todoMapper.updateTodoFromDTO(todo, todoDTO);
        todoRepository.update(todo);
        return todoMapper.toDTO(todo);
    }

    @Override
    @Transactional
    public void deleteTodo(TodoDTO todoDTO) {
        Long todoId = todoDTO.id();
        Long listId = todoDTO.listId();

        Todo todo;
        if (listId != null) {
            todo = getTodoByIdWithList(todoId);
            TodoList list = Objects.requireNonNull(todo.getList());
            list.removeTodo(todo);

        } else {
            todo = getTodoByIdWithGroup(todoId);
            TodoGroup group = Objects.requireNonNull(todo.getGroup());
            group.removeTodo(todo);
        }

        todoRepository.delete(todo);
    }
}
