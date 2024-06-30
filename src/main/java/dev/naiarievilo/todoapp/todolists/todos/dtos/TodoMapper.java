package dev.naiarievilo.todoapp.todolists.todos.dtos;

import dev.naiarievilo.todoapp.todolists.TodoParent;
import dev.naiarievilo.todoapp.todolists.todos.Todo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface TodoMapper {

    @Mapping(target = "listId", ignore = true)
    @Mapping(target = "groupId", ignore = true)
    TodoDTO toDTO(Todo todo);

    Set<TodoDTO> toDTOList(Set<Todo> todos);

    default void updateTodoFromDTO(@MappingTarget Set<Todo> todos, Set<TodoDTO> todosDTO, TodoParent parent) {
        Map<Long, Todo> todosMap = new HashMap<>();
        for (Todo todo : todos) {
            todosMap.put(todo.getId(), todo);
        }

        Set<Long> matchedTodoIds = new LinkedHashSet<>();
        for (TodoDTO todoDTO : todosDTO) {
            Todo todo = todosMap.get(todoDTO.id());
            if (todo != null) {
                matchedTodoIds.add(todo.getId());
                updateTodoFromDTO(todo, todoDTO);
                continue;
            }

            Todo newTodo = toEntity(todoDTO);
            parent.addTodo(newTodo);
        }

        if (matchedTodoIds.size() == todosMap.size()) {
            return;
        }

        Set<Long> unmatchedTodoIds = todosMap.keySet();
        unmatchedTodoIds.removeAll(matchedTodoIds);
        for (Long unmatchedTodoId : unmatchedTodoIds) {
            Todo todoToRemove = todosMap.get(unmatchedTodoId);
            parent.removeTodo(todoToRemove);
        }
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "list", ignore = true)
    @Mapping(target = "group", ignore = true)
    void updateTodoFromDTO(@MappingTarget Todo todo, TodoDTO todoDTO);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "list", ignore = true)
    @Mapping(target = "group", ignore = true)
    Todo toEntity(TodoDTO todoDTO);
}
