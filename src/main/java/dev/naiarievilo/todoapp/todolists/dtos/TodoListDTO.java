package dev.naiarievilo.todoapp.todolists.dtos;

import dev.naiarievilo.todoapp.todolists.dtos.groups.Creation;
import dev.naiarievilo.todoapp.todolists.dtos.groups.Deletion;
import dev.naiarievilo.todoapp.todolists.dtos.groups.Update;
import dev.naiarievilo.todoapp.todolists.todo_groups.TodoGroup;
import dev.naiarievilo.todoapp.todolists.todos.Todo;
import dev.naiarievilo.todoapp.validation.NotBlank;
import dev.naiarievilo.todoapp.validation.NotNull;
import dev.naiarievilo.todoapp.validation.Positive;

import java.time.LocalDate;
import java.util.Set;

public record TodoListDTO(

    @Positive(groups = {Update.class, Deletion.class})
    Long id,

    @NotBlank(max = 64, groups = {Creation.class, Update.class})
    String type,

    @NotNull(groups = Creation.class)
    LocalDate date,

    @NotNull(groups = {Creation.class, Update.class})
    Set<TodoGroup> groups,

    @NotNull(groups = {Creation.class, Update.class})
    Set<Todo> todos

) { }
