package dev.naiarievilo.todoapp.todolists.todo_groups.dtos;

import dev.naiarievilo.todoapp.todolists.TodoList;
import dev.naiarievilo.todoapp.todolists.todo_groups.TodoGroup;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Mapper(componentModel = "spring", uses = TodoMapper.class)
public interface TodoGroupMapper {

    TodoMapper todoMapper = Mappers.getMapper(TodoMapper.class);

    @Mapping(target = "listId", ignore = true)
    TodoGroupDTO toDTO(TodoGroup group);

    Set<TodoGroupDTO> toDTOList(Set<TodoGroup> groups);

    default void updateGroupFromDTO(@MappingTarget Set<TodoGroup> groups, Set<TodoGroupDTO> groupsDTO,
        TodoList parent) {

        Map<Long, TodoGroup> groupsMap = new HashMap<>();
        for (TodoGroup group : groups) {
            groupsMap.put(group.getId(), group);
        }

        Set<Long> matchedGroupIds = new LinkedHashSet<>();
        for (TodoGroupDTO groupDTO : groupsDTO) {
            TodoGroup group = groupsMap.get(groupDTO.id());
            if (group != null) {
                matchedGroupIds.add(group.getId());
                updateGroupFromDTO(group, groupDTO);
                continue;
            }

            TodoGroup newGroup = toEntity(groupDTO);
            parent.addGroup(newGroup);
        }

        if (matchedGroupIds.size() == groupsMap.size()) {
            return;
        }

        Set<Long> unmatchedGroupIds = groupsMap.keySet();
        unmatchedGroupIds.removeAll(matchedGroupIds);
        for (Long unmatchedGroupId : unmatchedGroupIds) {
            TodoGroup groupToRemove = groupsMap.get(unmatchedGroupId);
            parent.removeGroup(groupToRemove);
        }
    }

    default void updateGroupFromDTO(@MappingTarget TodoGroup group, TodoGroupDTO groupDTO) {
        group.setName(groupDTO.name());
        group.setCompleted(groupDTO.completed());
        group.setPosition(groupDTO.position());
        group.setDueDate(groupDTO.dueDate());
        todoMapper.updateTodoFromDTO(group.getTodos(), groupDTO.todos(), group);
    }

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "list", ignore = true)
    @Mapping(target = "todos", ignore = true)
    TodoGroup toEntity(TodoGroupDTO groupDTO);
}
