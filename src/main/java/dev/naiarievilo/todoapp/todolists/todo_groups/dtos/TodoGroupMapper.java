package dev.naiarievilo.todoapp.todolists.todo_groups.dtos;

import dev.naiarievilo.todoapp.todolists.todo_groups.TodoGroup;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mapper(componentModel = "spring", uses = TodoMapper.class)
public interface TodoGroupMapper {

    @Mapping(target = "listId", ignore = true)
    TodoGroupDTO toDTO(TodoGroup group);

    @Mapping(target = "list", ignore = true)
    TodoGroup toEntity(TodoGroupDTO groupDTO);

    Set<TodoGroupDTO> toDTOList(Set<TodoGroup> groups);

    default void updateGroupFromDTO(@MappingTarget Set<TodoGroup> groups, Set<TodoGroupDTO> groupsDTO) {
        Map<Long, TodoGroup> groupsMap = new HashMap<>();
        for (TodoGroup group : groups) {
            groupsMap.put(group.getId(), group);
        }

        for (TodoGroupDTO groupDTO : groupsDTO) {
            TodoGroup group = groupsMap.get(groupDTO.id());
            updateGroupFromDTO(group, groupDTO);
        }
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "list", ignore = true)
    void updateGroupFromDTO(@MappingTarget TodoGroup group, TodoGroupDTO groupsDTO);
}
