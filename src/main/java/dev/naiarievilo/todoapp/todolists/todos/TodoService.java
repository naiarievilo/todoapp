package dev.naiarievilo.todoapp.todolists.todos;

import dev.naiarievilo.todoapp.todolists.TodoList;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoMapper;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.PositionExceedsMaxAllowedException;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.PositionNotUniqueException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoMapper todoMapper;

    public TodoService(TodoRepository todoRepository, TodoMapper todoMapper) {
        this.todoRepository = todoRepository;
        this.todoMapper = todoMapper;
    }

    public Todo createTodo(TodoDTO todoDTO, TodoList list) {
        Todo newTodo = todoMapper.toNewEntity(todoDTO);
        newTodo.setPosition(list.getTodos().size() + 1);
        list.addTodo(newTodo);
        todoRepository.persist(newTodo);
        return newTodo;
    }

    public void updateTodos(Set<Todo> todos, Set<TodoDTO> todosDTO) {
        Map<Long, Todo> todosMap = new HashMap<>();
        for (Todo todo : todos) {
            todosMap.put(todo.getId(), todo);
        }

        Integer maxPositionAllowed = todos.size();
        Set<Integer> newPositionsRecorded = new HashSet<>();
        for (TodoDTO todoDTO : todosDTO) {
            Todo todo = todosMap.get(todoDTO.getId());
            if (todo == null) {
                continue;
            }

            if (todoDTO.getPosition() > maxPositionAllowed) {
                throw new PositionExceedsMaxAllowedException(todo.getId());
            } else if (newPositionsRecorded.contains(todoDTO.getPosition())) {
                throw new PositionNotUniqueException(todo.getId());
            }

            updateTodo(todo, todoDTO);
            newPositionsRecorded.add(todoDTO.getPosition());
        }
    }

    public void updateTodo(Todo todo, TodoDTO todoDTO) {
        todoMapper.updateEntityFromDTO(todo, todoDTO);
        todoRepository.update(todo);
    }

    public void synchronizeWithListDueDate(Set<Todo> todos, LocalDate listDueDate) {
        for (Todo todo : todos) {
            if (todo.getDueDate() != null) {
                todo.setDueDate(listDueDate);
                todoRepository.update(todo);
            }
        }
    }

    public void deleteTodo(Todo todo, TodoList list) {
        list.removeTodo(todo);
        todoRepository.delete(todo);
    }
}
