package dev.naiarievilo.todoapp.todolists.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.naiarievilo.todoapp.todolists.ListTypes;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.validation.NoNullElements;
import dev.naiarievilo.todoapp.validation.NotBlank;
import dev.naiarievilo.todoapp.validation.NotNull;
import dev.naiarievilo.todoapp.validation.Positive;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record TodoListDTO(

    @Nullable
    @Positive
    Long id,

    @NotBlank
    String title,

    @NotNull
    ListTypes type,

    @Nullable
    @JsonProperty("created_at")
    LocalDateTime createdAt,

    @Nullable
    @JsonProperty("due_date")
    LocalDate dueDate,

    @Nullable
    @NoNullElements
    Set<TodoDTO> todos

) { }