package dev.naiarievilo.todoapp.todolists.todos;

import dev.naiarievilo.todoapp.todolists.TodoList;
import dev.naiarievilo.todoapp.todolists.TodoParent;
import dev.naiarievilo.todoapp.todolists.todo_groups.TodoGroup;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoMapper;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.TodoNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final TodoMapper todoMapper;

    public TodoServiceImpl(TodoRepository todoRepository, TodoMapper todoMapper) {
        this.todoRepository = todoRepository;
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
    public TodoDTO createTodo(TodoDTO todoDTO, TodoParent parent) {
        Todo newTodo = todoMapper.toEntity(todoDTO);
        parent.addTodo(newTodo);
        todoRepository.persist(newTodo);
        return todoMapper.toDTO(newTodo);
    }

    @Override
    @Transactional
    public TodoDTO updateTodo(TodoDTO todoDTO) {
        Todo todo = getTodoById(todoDTO.id());
        todoMapper.updateEntityFromDTO(todo, todoDTO);
        todoRepository.update(todo);
        return todoMapper.toDTO(todo);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public void updateTodos(Set<Todo> todos, Set<TodoDTO> todosDTO, TodoParent parent) {
        Map<Long, Todo> todosMap = new HashMap<>();
        for (Todo todo : todos) {
            todosMap.put(todo.getId(), todo);
        }

        Set<Long> matchedTodoIds = new LinkedHashSet<>();
        for (TodoDTO todoDTO : todosDTO) {
            Todo todo = todosMap.get(todoDTO.id());
            if (todo != null) {
                matchedTodoIds.add(todo.getId());
                todoMapper.updateEntityFromDTO(todo, todoDTO);
            } else {
                Todo newTodo = todoMapper.toEntity(todoDTO);
                parent.addTodo(newTodo);
            }
        }

        if (matchedTodoIds.size() == todosMap.size()) {
            return;
        }

        Set<Long> unmatchedTodoIds = todosMap.keySet();
        unmatchedTodoIds.removeAll(matchedTodoIds);
        for (Long unmatchedTodoId : unmatchedTodoIds) {
            Todo todoToRemove = todosMap.get(unmatchedTodoId);
            parent.removeTodo(todoToRemove);
        }
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
