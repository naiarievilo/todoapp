package dev.naiarievilo.todoapp.todolists.todo_groups;

import dev.naiarievilo.todoapp.todolists.todo_groups.dtos.TodoGroupDTO;

public interface TodoGroupService {

    TodoGroup getGroupById(Long id);

    TodoGroup getGroupByIdWithTodos(Long id);

    TodoGroup getGroupByIdWithList(Long id);

    TodoGroupDTO createGroup(TodoGroupDTO groupDTO);

    TodoGroupDTO updateGroup(TodoGroupDTO groupDTO);

    void deleteGroup(TodoGroupDTO groupDTO);
}
