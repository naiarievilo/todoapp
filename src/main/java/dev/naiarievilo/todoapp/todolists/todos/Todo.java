package dev.naiarievilo.todoapp.todolists.todos;

import dev.naiarievilo.todoapp.todolists.TodoList;
import dev.naiarievilo.todoapp.todolists.todo_groups.TodoGroup;
import jakarta.persistence.*;
import org.springframework.lang.Nullable;

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

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todolist_id")
    private TodoList list;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_group_id")
    private TodoGroup group;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getTask() { return task; }

    public void setTask(String task) { this.task = task; }

    public boolean isCompleted() { return completed; }

    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getPosition() { return position; }

    public void setPosition(int position) { this.position = position; }

    @Nullable
    public TodoList getList() { return list; }

    public void setList(@Nullable TodoList list) { this.list = list; }

    @Nullable
    public TodoGroup getGroup() { return group; }

    public void setGroup(@Nullable TodoGroup group) { this.group = group; }

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
