package dev.naiarievilo.todoapp.todolists.todos;

import dev.naiarievilo.todoapp.todolists.TodoList;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;

import java.util.Set;

public interface TodoService {

    Todo createTodo(TodoDTO todoDTO, TodoList parent);

    void updateTodo(Todo todo, TodoDTO todoDTO);

    void updateTodos(Set<Todo> todos, Set<TodoDTO> todosDTO, TodoList parent);

    void deleteTodo(Todo todo, TodoList parent);
}
