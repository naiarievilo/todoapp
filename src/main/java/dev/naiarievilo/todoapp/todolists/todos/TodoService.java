package dev.naiarievilo.todoapp.todolists.todos;

import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;

public interface TodoService {

    Todo getTodoById(Long id);

    TodoDTO createTodo(TodoDTO todoDTO);

    TodoDTO updateTodo(TodoDTO todoDTO);

    void deleteTodo(TodoDTO todoDTO);
}
