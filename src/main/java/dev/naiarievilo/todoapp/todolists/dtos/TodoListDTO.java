package dev.naiarievilo.todoapp.todolists.dtos;

import dev.naiarievilo.todoapp.todolists.dtos.groups.Creation;
import dev.naiarievilo.todoapp.todolists.dtos.groups.Deletion;
import dev.naiarievilo.todoapp.todolists.dtos.groups.Update;
import dev.naiarievilo.todoapp.todolists.todo_groups.dtos.TodoGroupDTO;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.validation.NoNullElements;
import dev.naiarievilo.todoapp.validation.NotBlank;
import dev.naiarievilo.todoapp.validation.Positive;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.Set;

public record TodoListDTO(

    @Positive(groups = {Update.class, Deletion.class})
    Long id,

    @NotBlank(groups = {Creation.class, Update.class})
    String title,

    @NotBlank(max = 64, groups = {Creation.class, Update.class})
    String type,

    LocalDateTime createdAt,

    @Nullable
    LocalDateTime dueDate,

    @NoNullElements(groups = {Update.class})
    Set<TodoGroupDTO> groups,

    @NoNullElements(groups = {Update.class})
    Set<TodoDTO> todos

) { }
