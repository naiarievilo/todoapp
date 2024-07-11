package dev.naiarievilo.todoapp.todolists.todos.dtos;

import dev.naiarievilo.todoapp.todolists.todos.Todo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface TodoMapper {

    TodoDTO toDTO(Todo todo);

    Set<TodoDTO> toSetDTO(Set<Todo> todos);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "list", ignore = true)
    void updateEntityFromDTO(@MappingTarget Todo todo, TodoDTO todoDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "list", ignore = true)
    Todo toEntity(TodoDTO todoDTO);
}
