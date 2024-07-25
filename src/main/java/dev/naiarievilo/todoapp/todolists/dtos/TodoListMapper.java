package dev.naiarievilo.todoapp.todolists.dtos;

import dev.naiarievilo.todoapp.todolists.TodoList;
import dev.naiarievilo.todoapp.todolists.TodoListController;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoMapper;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

import static dev.naiarievilo.todoapp.todolists.ListTypes.CALENDAR;
import static dev.naiarievilo.todoapp.todolists.ListTypes.INBOX;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TodoListMapper {

    private final TodoMapper todoMapper;

    public TodoListMapper(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    public CollectionModel<TodoListDTO> toModels(Set<TodoList> lists, Long userId) {
        Set<TodoListDTO> listsDTO = new LinkedHashSet<>();
        for (TodoList list : lists) {
            listsDTO.add(toModel(list, userId));
        }

        return CollectionModel.of(listsDTO).withFallbackType(TodoListDTO.class);
    }

    public TodoListDTO toModel(TodoList list, Long userId) {
        TodoListDTO listDTO = toDTO(list);
        Long listId = list.getId();
        addLinks(listDTO, userId);
        todoMapper.addSelfLink(listDTO.getTodos(), userId, listId);
        return listDTO;
    }

    public TodoListDTO toDTO(TodoList list) {
        Set<TodoDTO> todosDTO = new LinkedHashSet<>();
        if (list.getType() == CALENDAR || list.getType() == INBOX) {
            todosDTO = todoMapper.toDTOs(list.getTodos());
        }

        return new TodoListDTO(
            list.getId(),
            list.getTitle(),
            list.getType(),
            list.getCreatedAt(),
            list.getDueDate(),
            todosDTO
        );
    }

    public void addLinks(TodoListDTO listDTO, Long userId) {
        Long listId = listDTO.getId();
        listDTO.add(
            linkTo(methodOn(TodoListController.class).getList(userId, listId)).withSelfRel(),
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
