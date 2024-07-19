package dev.naiarievilo.todoapp.todolists.todos.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.naiarievilo.todoapp.validation.NotBlank;
import dev.naiarievilo.todoapp.validation.NotNull;
import dev.naiarievilo.todoapp.validation.Positive;
import dev.naiarievilo.todoapp.validation.groups.Creation;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TodoDTO extends RepresentationModel<TodoDTO> {

    @Positive
    private final Long id;

    @NotBlank(groups = Creation.class)
    private final String task;

    @NotNull(groups = Creation.class)
    private final Boolean completed;

    @Positive
    private final Integer position;

    private final LocalDateTime createdAt;

    private final LocalDate dueDate;

    public TodoDTO(String task, Boolean completed, Integer position, LocalDate dueDate) {
        this(null, task, completed, position, null, dueDate);
    }

    @JsonCreator
    public TodoDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("task") String task,
        @JsonProperty("completed") Boolean completed,
        @JsonProperty("position") Integer position,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("due_date") LocalDate dueDate
    ) {
        this.id = id;
        this.task = task;
        this.completed = completed;
        this.position = position;
        this.createdAt = createdAt;
        this.dueDate = dueDate;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public Integer getPosition() {
        return position;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Long getId() {
        return id;
    }

    public String getTask() {
        return task;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TodoDTO that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
