package dev.naiarievilo.todoapp.todolists.todos.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.naiarievilo.todoapp.todolists.dtos.groups.Creation;
import dev.naiarievilo.todoapp.todolists.dtos.groups.Deletion;
import dev.naiarievilo.todoapp.todolists.dtos.groups.Update;
import dev.naiarievilo.todoapp.validation.NotBlank;
import dev.naiarievilo.todoapp.validation.NotNull;
import dev.naiarievilo.todoapp.validation.OneNotNull;
import dev.naiarievilo.todoapp.validation.Positive;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

@OneNotNull(fields = {"listId", "groupId"}, groups = {Creation.class, Update.class})
public record TodoDTO(

    @Positive(groups = {Update.class, Deletion.class})
    Long id,

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    Long listId,

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    Long groupId,

    @NotBlank(groups = {Creation.class, Update.class})
    String task,

    @NotNull(groups = {Creation.class, Update.class})
    Boolean completed,

    @Positive(groups = {Creation.class, Update.class})
    Integer position,

    LocalDateTime createdAt,

    @Nullable
    LocalDateTime dueDate

) { }
