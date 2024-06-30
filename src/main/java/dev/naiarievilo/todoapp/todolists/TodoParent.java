package dev.naiarievilo.todoapp.todolists;

import dev.naiarievilo.todoapp.todolists.todos.Todo;

import java.util.Set;

public interface TodoParent {

    Set<Todo> getTodos();

    void setTodos(Set<Todo> todos);

    void addTodo(Todo todo);

    void removeTodo(Todo todo);

    void removeTodos(Set<Todo> todos);
}
