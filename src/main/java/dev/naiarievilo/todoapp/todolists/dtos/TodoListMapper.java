package dev.naiarievilo.todoapp.todolists.dtos;

import dev.naiarievilo.todoapp.todolists.TodoList;
import dev.naiarievilo.todoapp.todolists.todo_groups.dtos.TodoGroupMapper;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {TodoGroupMapper.class, TodoMapper.class})
public interface TodoListMapper {

    TodoListDTO toDTO(TodoList list);

    @Mapping(target = "user", ignore = true)
    TodoList toEntity(TodoListDTO listDTO);

    Set<TodoListDTO> toDTOList(Set<TodoList> lists);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateListFromDTO(@MappingTarget TodoList list, TodoListDTO listDTO);
}
