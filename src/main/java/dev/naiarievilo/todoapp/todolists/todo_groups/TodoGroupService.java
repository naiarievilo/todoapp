package dev.naiarievilo.todoapp.todolists.todo_groups;

import dev.naiarievilo.todoapp.todolists.TodoList;
import dev.naiarievilo.todoapp.todolists.todo_groups.dtos.TodoGroupDTO;

import java.util.Set;

public interface TodoGroupService {

    TodoGroup getGroupById(Long id);

    TodoGroup getGroupByIdWithTodos(Long id);

    TodoGroup getGroupByIdWithList(Long id);

    TodoGroupDTO createGroup(TodoGroupDTO groupDTO, TodoList parent);

    TodoGroupDTO updateGroup(TodoGroupDTO groupDTO);

    void updateGroups(Set<TodoGroup> groups, Set<TodoGroupDTO> groupsDTO, TodoList list);

    void deleteGroup(TodoGroupDTO groupDTO);
}
