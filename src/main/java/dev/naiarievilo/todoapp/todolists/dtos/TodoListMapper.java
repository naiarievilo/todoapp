package dev.naiarievilo.todoapp.todolists.dtos;

import dev.naiarievilo.todoapp.todolists.TodoList;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Set;

@Mapper(componentModel = "spring", uses = TodoMapper.class)
public interface TodoListMapper {

    TodoListDTO toDTO(TodoList list);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "todos", ignore = true)
    TodoList toEntity(TodoListDTO listDTO);

    Set<TodoListDTO> toSetDTO(Set<TodoList> lists);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "todos", ignore = true)
    void updateEntityFromDTO(@MappingTarget TodoList list, TodoListDTO listDTO);
}
