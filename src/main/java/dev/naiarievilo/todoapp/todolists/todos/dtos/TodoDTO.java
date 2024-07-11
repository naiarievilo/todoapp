package dev.naiarievilo.todoapp.todolists.todos.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.naiarievilo.todoapp.validation.NotBlank;
import dev.naiarievilo.todoapp.validation.NotNull;
import dev.naiarievilo.todoapp.validation.Positive;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TodoDTO(

    @Nullable
    @Positive
    Long id,

    @NotBlank
    String task,

    @NotNull
    Boolean completed,

    @Positive
    Integer position,

    @Nullable
    @JsonProperty("created_at")
    LocalDateTime createdAt,

    @Nullable
    @JsonProperty("due_date")
    LocalDate dueDate

) { }
