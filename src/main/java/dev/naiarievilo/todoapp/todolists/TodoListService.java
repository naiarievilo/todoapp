package dev.naiarievilo.todoapp.todolists;

import dev.naiarievilo.todoapp.todolists.dtos.TodoListDTO;

public interface TodoListService {

    TodoList getListById(Long id);

    TodoList getListByIdEagerly(Long id);

    TodoList getListByIdWithGroups(Long id);

    TodoList getListByIdWithTodos(Long id);

    TodoListDTO createList(TodoListDTO listDTO);

    TodoListDTO updateList(TodoListDTO listDTO);

    void deleteList(TodoListDTO listDTO);
}
