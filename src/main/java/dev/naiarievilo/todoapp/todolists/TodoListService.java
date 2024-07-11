package dev.naiarievilo.todoapp.todolists;

import dev.naiarievilo.todoapp.todolists.dtos.TodoListDTO;
import dev.naiarievilo.todoapp.todolists.todos.Todo;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.users.User;

import java.util.Set;

public interface TodoListService {

    TodoList getInboxList(User user);

    TodoList getTodayList(User user);

    Set<TodoList> getWeeklyLists(User user);

    TodoList getListByIdEagerly(Long userId, Long listId);

    TodoList createList(User user, TodoListDTO listDTO, ListTypes listType);

    void updateList(Long userId, Long listId, TodoListDTO listDTO);

    void deleteList(Long userId, Long listId);

    Todo addNewTodoToList(Long userId, Long listId, TodoDTO todoDTO);

    void updateTodoFromList(Long userId, Long listId, Long todoId, TodoDTO todoDTO);

    void removeTodoFromList(Long userId, Long listId, Long todoId);
}
