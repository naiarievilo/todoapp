package dev.naiarievilo.todoapp.todolists.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.naiarievilo.todoapp.todolists.ListTypes;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.validation.NotBlank;
import dev.naiarievilo.todoapp.validation.Positive;
import dev.naiarievilo.todoapp.validation.groups.Creation;
import dev.naiarievilo.todoapp.validation.groups.Update;
import jakarta.validation.Valid;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public class TodoListDTO extends RepresentationModel<TodoListDTO> {

    @Positive
    private final Long id;

    @NotBlank(groups = {Creation.class, Update.class})
    private final String title;

    private final ListTypes type;

    private final LocalDateTime createdAt;

    @Nullable
    private final LocalDate dueDate;

    private final Set<@Valid TodoDTO> todos;

    public TodoListDTO(Long id, String title, ListTypes type, LocalDateTime createdAt, LocalDate dueDate) {
        this(id, title, type, createdAt, dueDate, null);
    }

    @JsonCreator
    public TodoListDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("title") String title,
        @JsonProperty("type") ListTypes type,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @Nullable @JsonProperty("due_date") LocalDate dueDate,
        @JsonProperty(value = "todos") Set<TodoDTO> todos
    ) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.createdAt = createdAt;
        this.dueDate = dueDate;
        this.todos = todos;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public ListTypes getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Nullable
    public LocalDate getDueDate() {
        return dueDate;
    }

    public Set<TodoDTO> getTodos() {
        return todos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TodoListDTO that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
