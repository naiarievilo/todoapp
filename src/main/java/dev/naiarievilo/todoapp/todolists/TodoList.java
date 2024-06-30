package dev.naiarievilo.todoapp.todolists;

import dev.naiarievilo.todoapp.todolists.todo_groups.TodoGroup;
import dev.naiarievilo.todoapp.todolists.todos.Todo;
import dev.naiarievilo.todoapp.users.User;
import jakarta.persistence.*;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity(name = "TodoList")
@Table(name = "todolists")
public class TodoList implements TodoParent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "type", length = 64, nullable = false, updatable = false)
    private String type;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Nullable
    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "list", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position")
    private Set<Todo> todos = new LinkedHashSet<>();

    @OneToMany(mappedBy = "list", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position")
    private Set<TodoGroup> groups = new LinkedHashSet<>();

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Nullable
    public LocalDateTime getDueDate() { return dueDate; }

    public void setDueDate(@Nullable LocalDateTime dueDate) { this.dueDate = dueDate; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    @Override
    public Set<Todo> getTodos() { return todos; }

    @Override
    public void setTodos(Set<Todo> todos) { this.todos = todos; }

    @Override
    public void addTodo(Todo todo) {
        todos.add(todo);
        todo.setList(this);
    }

    @Override
    public void removeTodo(Todo todo) {
        todos.remove(todo);
        todo.setList(this);
    }

    @Override
    public void removeTodos(Set<Todo> todos) {
        for (Todo todo : todos) {
            removeTodo(todo);
        }
    }

    public Set<TodoGroup> getGroups() { return groups; }

    public void setGroups(Set<TodoGroup> groups) { this.groups = groups; }

    public void addGroup(TodoGroup group) {
        groups.add(group);
        group.setList(this);
    }

    public void removeGroup(TodoGroup group) {
        groups.remove(group);
        group.setList(null);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof TodoList other)) {
            return false;
        }

        return id != null && id.equals(other.id);
    }
}
