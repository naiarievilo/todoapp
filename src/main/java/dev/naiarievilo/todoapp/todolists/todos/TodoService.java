package dev.naiarievilo.todoapp.todolists.todos;

import dev.naiarievilo.todoapp.todolists.TodoParent;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;

import java.util.Set;

public interface TodoService {

    Todo getTodoById(Long id);

    TodoDTO createTodo(TodoDTO todoDTO, TodoParent parent);

    TodoDTO updateTodo(TodoDTO todoDTO);

    void updateTodos(Set<Todo> todos, Set<TodoDTO> todosDTO, TodoParent parent);

    void deleteTodo(TodoDTO todoDTO);
}
