package dev.naiarievilo.todoapp.todolists.dtos;

import dev.naiarievilo.todoapp.todolists.TodoList;
import dev.naiarievilo.todoapp.todolists.todo_groups.dtos.TodoGroupMapper;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {TodoMapper.class, TodoGroupMapper.class})
public interface TodoListMapper {

    TodoGroupMapper groupMapper = Mappers.getMapper(TodoGroupMapper.class);
    TodoMapper todoMapper = Mappers.getMapper(TodoMapper.class);

    TodoListDTO toDTO(TodoList list);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "todos", ignore = true)
    @Mapping(target = "groups", ignore = true)
    TodoList toEntity(TodoListDTO listDTO);

    Set<TodoListDTO> toDTOList(Set<TodoList> lists);

    default void updateListFromDTO(@MappingTarget TodoList list, TodoListDTO listDTO) {
        list.setTitle(listDTO.title());
        list.setDueDate(listDTO.dueDate());
        todoMapper.updateTodoFromDTO(list.getTodos(), listDTO.todos(), list);
        groupMapper.updateGroupFromDTO(list.getGroups(), listDTO.groups(), list);
    }
}
