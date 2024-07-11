package dev.naiarievilo.todoapp.todolists.todos;

import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

public class TodosTestHelper {

    public static final LocalDate NEW_TODO_DUE_DATE = LocalDate.now().plusDays(4);
    public static final Long NEW_TODO_ID = 4L;
    public static final Integer NEW_TODO_POSITION = 4;
    public static final String NEW_TODO_TASK = "New todo task";
    public static final LocalDate TODO_DUE_DATE_1 = LocalDate.now().plusDays(1);
    public static final LocalDate TODO_DUE_DATE_2 = LocalDate.now().plusDays(2);
    public static final LocalDate TODO_DUE_DATE_3 = LocalDate.now().plusDays(3);
    public static final Long TODO_ID_1 = 1L;
    public static final Long TODO_ID_2 = 2L;
    public static final Long TODO_ID_3 = 3L;
    public static final Integer TODO_POSITION_1 = 1;
    public static final Integer TODO_POSITION_2 = 2;
    public static final Integer TODO_POSITION_3 = 3;
    public static final String TODO_TASK_1 = "Todo task 1";
    public static final String TODO_TASK_2 = "Todo task 2";
    public static final String TODO_TASK_3 = "Todo task 3";

    public static Set<TodoDTO> todoDTOSet() {
        Set<TodoDTO> todoDTOSet = new LinkedHashSet<>();
        todoDTOSet.add(todoDTO_1());
        todoDTOSet.add(todoDTO_2());
        todoDTOSet.add(todoDTO_3());

        return todoDTOSet;
    }

    public static TodoDTO todoDTO_1() {
        return new TodoDTO(TODO_ID_1, TODO_TASK_1, false, TODO_POSITION_1, null, TODO_DUE_DATE_1);
    }

    public static TodoDTO todoDTO_2() {
        return new TodoDTO(TODO_ID_2, TODO_TASK_2, false, TODO_POSITION_2, null, TODO_DUE_DATE_2);
    }

    public static TodoDTO todoDTO_3() {
        return new TodoDTO(TODO_ID_3, TODO_TASK_3, false, TODO_POSITION_3, null, TODO_DUE_DATE_3);
    }

    public static Set<Todo> todoSet() {
        Set<Todo> todoSet = new LinkedHashSet<>();
        todoSet.add(todo_1());
        todoSet.add(todo_2());
        todoSet.add(todo_3());

        return todoSet;
    }

    public static Todo todo_1() {
        TodoDTO todoDTO = todoDTO_1();
        Todo todo = new Todo();
        todo.setId(todoDTO.id());
        todo.setTask(todoDTO.task());
        todo.setCompleted(todoDTO.completed());
        todo.setPosition(todoDTO.position());
        todo.setDueDate(todoDTO.dueDate());

        return todo;
    }

    public static Todo todo_2() {
        TodoDTO todoDTO = todoDTO_2();
        Todo todo = new Todo();
        todo.setId(todoDTO.id());
        todo.setTask(todoDTO.task());
        todo.setCompleted(todoDTO.completed());
        todo.setPosition(todoDTO.position());
        todo.setDueDate(todoDTO.dueDate());

        return todo;
    }

    public static Todo todo_3() {
        TodoDTO todoDTO = todoDTO_3();
        Todo todo = new Todo();
        todo.setId(todoDTO.id());
        todo.setTask(todoDTO.task());
        todo.setCompleted(todoDTO.completed());
        todo.setPosition(todoDTO.position());
        todo.setDueDate(todoDTO.dueDate());

        return todo;
    }

    public static Set<TodoDTO> newTodoDTOSet() {
        Set<TodoDTO> newTodoDTOSet = new LinkedHashSet<>();
        newTodoDTOSet.add(newTodoDTO_1());
        newTodoDTOSet.add(newTodoDTO_2());
        newTodoDTOSet.add(newTodoDTO_3());
        return newTodoDTOSet;
    }

    public static TodoDTO newTodoDTO_1() {
        return new TodoDTO(null, TODO_TASK_1, false, TODO_POSITION_1, null, TODO_DUE_DATE_1);
    }

    public static TodoDTO newTodoDTO_2() {
        return new TodoDTO(null, TODO_TASK_2, true, TODO_POSITION_2, null, TODO_DUE_DATE_2);
    }

    public static TodoDTO newTodoDTO_3() {
        return new TodoDTO(null, TODO_TASK_3, false, TODO_POSITION_3, null, TODO_DUE_DATE_3);
    }

    public static Set<Todo> newTodoSet() {
        Set<Todo> newTodoSet = new LinkedHashSet<>();
        newTodoSet.add(newTodo_1());
        newTodoSet.add(newTodo_2());
        newTodoSet.add(newTodo_3());
        return newTodoSet;
    }

    public static Todo newTodo_1() {
        TodoDTO todoDTO = newTodoDTO_1();
        Todo todo = new Todo();
        todo.setTask(todoDTO.task());
        todo.setCompleted(todoDTO.completed());
        todo.setPosition(todoDTO.position());
        todo.setDueDate(todoDTO.dueDate());
        return todo;
    }

    public static Todo newTodo_2() {
        TodoDTO todoDTO = newTodoDTO_2();
        Todo todo = new Todo();
        todo.setTask(todoDTO.task());
        todo.setCompleted(todoDTO.completed());
        todo.setPosition(todoDTO.position());
        todo.setDueDate(todoDTO.dueDate());
        return todo;
    }

    public static Todo newTodo_3() {
        TodoDTO todoDTO = newTodoDTO_3();
        Todo todo = new Todo();
        todo.setTask(todoDTO.task());
        todo.setCompleted(todoDTO.completed());
        todo.setPosition(todoDTO.position());
        todo.setDueDate(todoDTO.dueDate());
        return todo;
    }
}
