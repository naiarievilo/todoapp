package dev.naiarievilo.todoapp.todolists.dtos;

import dev.naiarievilo.todoapp.todolists.TodoList;
import dev.naiarievilo.todoapp.todolists.TodoListController;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class TodoListMapper {

    private final TodoMapper todoMapper;

    public TodoListMapper(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    public Set<TodoListDTO> toDTOs(Set<TodoList> lists) {
        Set<TodoListDTO> listsDTO = new LinkedHashSet<>();
        for (TodoList list : lists) {
            listsDTO.add(toDTO(list));
        }

        return listsDTO;
    }

    public TodoListDTO toDTO(TodoList list) {
        return new TodoListDTO(
            list.getId(),
            list.getTitle(),
            list.getType(),
            list.getCreatedAt(),
            list.getDueDate(),
            todoMapper.toDTOs(list.getTodos())
        );
    }

    public Set<TodoListDTO> toModels(Set<TodoList> lists, Long userId) {
        Set<TodoListDTO> listsDTO = new LinkedHashSet<>();
        for (TodoList list : lists) {
            listsDTO.add(toModel(list, userId));
        }

        return listsDTO;
    }

    public TodoListDTO toModel(TodoList list, Long userId) {
        TodoListDTO listDTO = toDTO(list);
        Long listId = list.getId();
        addLinks(listDTO, userId);
        todoMapper.addSelfLink(listDTO.getTodos(), userId, listId);
        return listDTO;
    }

    public void addLinks(TodoListDTO listDTO, Long userId) {
        Long listId = listDTO.getId();
        listDTO.add(
            linkTo(methodOn(TodoListController.class).getList(userId, listId)).withSelfRel()
                .andAffordance(
                    afford(methodOn(TodoListController.class).updateList(userId, listId, listDTO))
                )
                .andAffordance(
                    afford(methodOn(TodoListController.class).deleteList(userId, listId))
                ),

            linkTo(methodOn(TodoListController.class).getTodosFromList(userId, listId)).withRel("todos")
        );
    }

    public TodoList toEntity(TodoListDTO listDTO) {
        TodoList list = new TodoList();
        list.setTitle(listDTO.getTitle());
        list.setType(listDTO.getType());
        list.setDueDate(listDTO.getDueDate());
        return list;
    }

    public void updateEntityFromDTO(TodoList list, TodoListDTO listDTO) {
        list.setTitle(listDTO.getTitle());
    }
}
