package dev.naiarievilo.todoapp.todolists.todos;

import dev.naiarievilo.todoapp.todolists.TodoList;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoMapper;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.PositionExceedsMaxAllowedException;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.PositionNotUniqueException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoMapper todoMapper;

    public TodoService(TodoRepository todoRepository, TodoMapper todoMapper) {
        this.todoRepository = todoRepository;
        this.todoMapper = todoMapper;
    }

    public Todo createTodo(TodoDTO todoDTO, TodoList parent) {
        Todo newTodo = todoMapper.toEntity(todoDTO);
        parent.addTodo(newTodo);
        todoRepository.persist(newTodo);
        return newTodo;
    }

    public void updateTodos(Set<Todo> todos, Set<TodoDTO> todosDTO, TodoList parent) {
        Map<Long, Todo> todosMap = new HashMap<>();
        for (Todo todo : todos) {
            todosMap.put(todo.getId(), todo);
        }

        Integer maxPositionAllowed = todosDTO.size();
        Set<Integer> newPositionsRecorded = new HashSet<>();
        Set<Long> matchedTodoIds = new LinkedHashSet<>();
        for (TodoDTO todoDTO : todosDTO) {
            Todo todo = todosMap.get(todoDTO.id());
            if (todo == null) {
                continue;
            }

            if (todoDTO.position() > maxPositionAllowed) {
                throw new PositionExceedsMaxAllowedException();
            } else if (newPositionsRecorded.contains(todoDTO.position())) {
                throw new PositionNotUniqueException();
            }

            matchedTodoIds.add(todo.getId());
            updateTodo(todo, todoDTO);
            newPositionsRecorded.add(todoDTO.position());
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

    public void updateTodo(Todo todo, TodoDTO todoDTO) {
        todoMapper.updateEntityFromDTO(todo, todoDTO);
        todoRepository.update(todo);
    }

    public void deleteTodo(Todo todo, TodoList parent) {
        parent.removeTodo(todo);
        todoRepository.delete(todo);
    }
}
