package dev.naiarievilo.todoapp.todolists.todos.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.naiarievilo.todoapp.validation.NotBlank;
import dev.naiarievilo.todoapp.validation.NotNull;
import dev.naiarievilo.todoapp.validation.Positive;
import dev.naiarievilo.todoapp.validation.groups.Creation;
import dev.naiarievilo.todoapp.validation.groups.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TodoDTO(

    @Positive(groups = Update.class)
    Long id,

    @NotBlank(groups = {Creation.class, Update.class})
    String task,

    @NotNull(groups = {Creation.class, Update.class})
    Boolean completed,

    @Positive(groups = {Creation.class, Update.class})
    Integer position,

    @JsonProperty("created_at")
    LocalDateTime createdAt,

    @JsonProperty("due_date")
    LocalDate dueDate

) { }
