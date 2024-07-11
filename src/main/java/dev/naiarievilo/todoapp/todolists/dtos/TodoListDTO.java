package dev.naiarievilo.todoapp.todolists.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.naiarievilo.todoapp.todolists.ListTypes;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.validation.NotBlank;
import dev.naiarievilo.todoapp.validation.NotNull;
import dev.naiarievilo.todoapp.validation.Positive;
import dev.naiarievilo.todoapp.validation.groups.Creation;
import dev.naiarievilo.todoapp.validation.groups.Update;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record TodoListDTO(

    @Positive(groups = Update.class)
    Long id,

    @NotBlank(groups = {Creation.class, Update.class})
    String title,

    @NotNull
    ListTypes type,

    @JsonProperty("created_at")
    LocalDateTime createdAt,

    @JsonProperty("due_date")
    LocalDate dueDate,

    @Nullable
    Set<TodoDTO> todos

) { }
