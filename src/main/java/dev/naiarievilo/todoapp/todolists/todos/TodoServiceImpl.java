package dev.naiarievilo.todoapp.todolists.todos;

import dev.naiarievilo.todoapp.todolists.TodoList;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final TodoMapper todoMapper;

    public TodoServiceImpl(TodoRepository todoRepository, TodoMapper todoMapper) {
        this.todoRepository = todoRepository;
        this.todoMapper = todoMapper;
    }

    @Override
    public Todo createTodo(TodoDTO todoDTO, TodoList parent) {
        Todo newTodo = todoMapper.toEntity(todoDTO);
        parent.addTodo(newTodo);
        todoRepository.persist(newTodo);
        return newTodo;
    }

    @Override
    public void updateTodo(Todo todo, TodoDTO todoDTO) {
        todoMapper.updateEntityFromDTO(todo, todoDTO);
        todoRepository.update(todo);
    }

    @Override
    public void updateTodos(Set<Todo> todos, Set<TodoDTO> todosDTO, TodoList parent) {
        Map<Long, Todo> todosMap = new HashMap<>();
        for (Todo todo : todos) {
            todosMap.put(todo.getId(), todo);
        }

        Set<Long> matchedTodoIds = new LinkedHashSet<>();
        for (TodoDTO todoDTO : todosDTO) {
            Todo todo = todosMap.get(todoDTO.id());
            if (todo != null) {
                matchedTodoIds.add(todo.getId());
                updateTodo(todo, todoDTO);
            }
        }

        if (matchedTodoIds.size() == todosMap.size()) {
            return;
        }

        Set<Long> unmatchedTodoIds = todosMap.keySet();
        unmatchedTodoIds.removeAll(matchedTodoIds);
        for (Long unmatchedTodoId : unmatchedTodoIds) {
            Todo todoToRemove = todosMap.get(unmatchedTodoId);
            deleteTodo(todoToRemove, parent);
        }
    }

    @Override
    public void deleteTodo(Todo todo, TodoList parent) {
        parent.removeTodo(todo);
        todoRepository.delete(todo);
    }
}
