package dev.naiarievilo.todoapp.todolists.todo_groups.dtos;

import dev.naiarievilo.todoapp.todolists.todo_groups.TodoGroup;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {TodoMapper.class})
public interface TodoGroupMapper {

    @Mapping(target = "listId", ignore = true)
    TodoGroupDTO toDTO(TodoGroup group);

    Set<TodoGroupDTO> toSetDTO(Set<TodoGroup> groups);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "list", ignore = true)
    @Mapping(target = "todos", ignore = true)
    void updateEntityFromDTO(@MappingTarget TodoGroup group, TodoGroupDTO groupDTO);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "list", ignore = true)
    @Mapping(target = "todos", ignore = true)
    TodoGroup toEntity(TodoGroupDTO groupDTO);
}
