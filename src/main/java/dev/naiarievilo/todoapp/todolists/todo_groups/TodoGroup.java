package dev.naiarievilo.todoapp.todolists.todo_groups;

import dev.naiarievilo.todoapp.todolists.TodoList;
import dev.naiarievilo.todoapp.todolists.todos.Todo;
import jakarta.persistence.*;
import org.springframework.lang.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity(name = "TodoGroup")
@Table(name = "todo_groups")
public class TodoGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    @Column(name = "position", nullable = false)
    private int position;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todolist_id")
    private TodoList list;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position")
    private Set<Todo> todos = new LinkedHashSet<>();

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public boolean isCompleted() { return completed; }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        for (Todo todo : todos) {
            todo.setCompleted(completed);
        }
    }

    public int getPosition() { return position; }

    public void setPosition(int position) { this.position = position; }

    @Nullable
    public TodoList getList() { return list; }

    public void setList(@Nullable TodoList list) { this.list = list; }

    public Set<Todo> getTodos() { return todos; }

    public void setTodos(Set<Todo> todos) { this.todos = todos; }

    public void addTodo(Todo todo) {
        todos.add(todo);
        todo.setGroup(this);
    }

    public void removeTodo(Todo todo) {
        todos.remove(todo);
        todo.setGroup(null);
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

        if (!(obj instanceof TodoGroup other)) {
            return false;
        }

        return id != null && id.equals(other.id);
    }
}
