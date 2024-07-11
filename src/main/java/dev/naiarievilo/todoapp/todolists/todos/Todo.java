package dev.naiarievilo.todoapp.todolists.todos;

import dev.naiarievilo.todoapp.todolists.TodoList;
import jakarta.persistence.*;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(name = "Todo")
@Table(name = "todos")
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "task", nullable = false)
    private String task;

    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Nullable
    @Column(name = "due_date")
    private LocalDate dueDate;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_list_id")
    private TodoList list;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getTask() { return task; }

    public void setTask(String task) { this.task = task; }

    public boolean isCompleted() { return completed; }

    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getPosition() { return position; }

    public void setPosition(int position) { this.position = position; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Nullable
    public LocalDate getDueDate() { return dueDate; }

    public void setDueDate(@Nullable LocalDate dueDate) { this.dueDate = dueDate; }

    @Nullable
    public TodoList getList() { return list; }

    public void setList(@Nullable TodoList list) { this.list = list; }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Todo other)) {
            return false;
        }

        return id != null && id.equals(other.id);
    }
}
