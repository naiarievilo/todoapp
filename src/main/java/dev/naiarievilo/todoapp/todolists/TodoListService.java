package dev.naiarievilo.todoapp.todolists;

import dev.naiarievilo.todoapp.todolists.dtos.TodoListDTO;
import dev.naiarievilo.todoapp.users.User;

public interface TodoListService {

    TodoList getListById(Long id);

    TodoList getListByIdEagerly(Long id);

    TodoList getListByIdWithGroups(Long id);

    TodoList getListByIdWithTodos(Long id);

    TodoListDTO createList(TodoListDTO listDTO, User user);

    TodoListDTO updateList(TodoListDTO listDTO);

    void deleteList(TodoListDTO listDTO);
}
