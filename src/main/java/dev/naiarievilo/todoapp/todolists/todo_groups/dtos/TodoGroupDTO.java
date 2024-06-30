package dev.naiarievilo.todoapp.todolists.todo_groups.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.naiarievilo.todoapp.todolists.dtos.groups.Creation;
import dev.naiarievilo.todoapp.todolists.dtos.groups.Deletion;
import dev.naiarievilo.todoapp.todolists.dtos.groups.Update;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.validation.NoNullElements;
import dev.naiarievilo.todoapp.validation.NotBlank;
import dev.naiarievilo.todoapp.validation.NotNull;
import dev.naiarievilo.todoapp.validation.Positive;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.Set;

public record TodoGroupDTO(

    @Positive(groups = {Update.class, Deletion.class})
    Long id,

    @NotNull(groups = {Creation.class,})
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    Long listId,

    @NotBlank(groups = Creation.class)
    String name,

    @NotNull(groups = Creation.class)
    Boolean completed,

    @Positive(groups = {Creation.class, Update.class})
    Integer position,

    LocalDateTime createdAt,

    @Nullable
    LocalDateTime dueDate,

    @NoNullElements(groups = Update.class)
    Set<TodoDTO> todos

) { }
