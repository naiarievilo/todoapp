package dev.naiarievilo.todoapp.todolists.todos.dtos;

import dev.naiarievilo.todoapp.todolists.TodoListController;
import dev.naiarievilo.todoapp.todolists.todos.Todo;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TodoMapper {

    public Set<TodoDTO> toDTOs(Set<Todo> todos) {
        if (todos == null || todos.isEmpty()) {
            return new LinkedHashSet<>();
        }

        Set<TodoDTO> todoDTOSet = new LinkedHashSet<>();
        for (Todo todo : todos) {
            todoDTOSet.add(toDTO(todo));
        }

        return todoDTOSet;
    }

    public TodoDTO toDTO(Todo todo) {
        return new TodoDTO(
            todo.getId(),
            todo.getTask(),
            todo.isCompleted(),
            todo.getPosition(),
            todo.getCreatedAt(),
            todo.getDueDate()
        );
    }

    public CollectionModel<TodoDTO> toModels(Set<Todo> todos, Long userId, Long listId) {
        Set<TodoDTO> todosDTO = new LinkedHashSet<>();
        for (Todo todo : todos) {
            TodoDTO todoDTO = toModel(todo, userId, listId);
            todosDTO.add(todoDTO);
        }

        return CollectionModel.of(todosDTO).withFallbackType(TodoDTO.class);
    }

    public TodoDTO toModel(Todo todo, Long userId, Long listId) {
        TodoDTO todoDTO = toDTO(todo);
        addSelfLink(todoDTO, userId, listId);
        return todoDTO;
    }

    public void addSelfLink(TodoDTO todoDTO, Long userId, Long listId) {
        Long todoId = todoDTO.getId();
        todoDTO.add(linkTo(methodOn(TodoListController.class).getTodoFromList(userId, listId, todoId)).withSelfRel());
    }

    public Todo toEntity(TodoDTO todoDTO) {
        Todo todo = new Todo();
        todo.setTask(todoDTO.getTask());
        todo.setCompleted(todoDTO.getCompleted());
        todo.setPosition(todoDTO.getPosition());
        todo.setDueDate(todoDTO.getDueDate());
        return todo;
    }

    public Todo toNewEntity(TodoDTO todoDTO) {
        Todo todo = new Todo();
        todo.setTask(todoDTO.getTask());
        todo.setCompleted(todoDTO.getCompleted());
        todo.setDueDate(todoDTO.getDueDate());
        return todo;
    }

    public void updateEntityFromDTO(Todo todo, TodoDTO todoDTO) {
        todo.setTask(todoDTO.getTask());
        todo.setCompleted(todoDTO.getCompleted());
        todo.setPosition(todoDTO.getPosition());
        todo.setDueDate(todoDTO.getDueDate());
    }

    public void addSelfLink(Set<TodoDTO> todosDTO, Long userId, Long listId) {
        for (TodoDTO todoDTO : todosDTO) {
            addSelfLink(todoDTO, userId, listId);
        }
    }
}
