package dev.naiarievilo.todoapp.todolists.todos.dtos;

import dev.naiarievilo.todoapp.todolists.todos.Todo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface TodoMapper {

    @Mapping(target = "listId", ignore = true)
    @Mapping(target = "groupId", ignore = true)
    TodoDTO toDTO(Todo todo);

    @Mapping(target = "list", ignore = true)
    @Mapping(target = "group", ignore = true)
    Todo toEntity(TodoDTO todoDTO);

    Set<TodoDTO> toDTOList(Set<Todo> todos);

    default void updateTodoFromDTO(@MappingTarget Set<Todo> todos, Set<TodoDTO> todosDTO) {
        Map<Long, Todo> todosMap = new HashMap<>();
        for (Todo todo : todos) {
            todosMap.put(todo.getId(), todo);
        }

        for (TodoDTO todoDTO : todosDTO) {
            Todo todo = todosMap.get(todoDTO.id());
            updateTodoFromDTO(todo, todoDTO);
        }
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "list", ignore = true)
    @Mapping(target = "group", ignore = true)
    void updateTodoFromDTO(@MappingTarget Todo todo, TodoDTO todoDTO);
}
