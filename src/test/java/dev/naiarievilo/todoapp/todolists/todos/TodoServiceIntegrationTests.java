package dev.naiarievilo.todoapp.todolists.todos;

import dev.naiarievilo.todoapp.ServiceIntegrationTests;
import dev.naiarievilo.todoapp.todolists.ListTypes;
import dev.naiarievilo.todoapp.todolists.TodoList;
import dev.naiarievilo.todoapp.todolists.TodoListRepository;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoMapper;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.TodoNotFoundException;
import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashSet;
import java.util.Set;

import static dev.naiarievilo.todoapp.todolists.todos.TodoServiceTestCases.*;
import static dev.naiarievilo.todoapp.todolists.todos.TodosTestHelper.*;
import static dev.naiarievilo.todoapp.users.UsersTestConstants.EMAIL_1;
import static dev.naiarievilo.todoapp.users.UsersTestConstants.PASSWORD_2;
import static org.junit.jupiter.api.Assertions.*;

class TodoServiceIntegrationTests extends ServiceIntegrationTests {

    @Autowired
    TodoRepository todoRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TodoMapper todoMapper;

    @Autowired
    TodoListRepository listRepository;

    @Autowired
    TodoService todoService;

    private TodoDTO newTodoDTO_1;
    private Todo newTodo_1;
    private TodoList parentList;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail(EMAIL_1);
        user.setPassword(PASSWORD_2);
        userRepository.persist(user);

        parentList = new TodoList();
        parentList.setUser(user);
        parentList.setType(ListTypes.CUSTOM);

        newTodoDTO_1 = TodosTestHelper.newTodoDTO_1();
        newTodo_1 = TodosTestHelper.newTodo_1();
    }

    @Test
    @DisplayName("createTodo(): " + CREATES_TODO_WHEN_INPUT_VALID)
    void createTodo_InputValidAndParentList_CreatesTodo() {
        listRepository.persist(parentList);

        Todo returnedTodo = todoService.createTodo(newTodoDTO_1, parentList);
        assertTrue(parentList.getTodos().contains(returnedTodo));
        assertTrue(todoRepository.findById(returnedTodo.getId()).isPresent());
    }

    @Test
    @DisplayName("updateTodo(): " + UPDATES_TODO_WHEN_INPUT_VALID)
    void updateTodo_InputValid_UpdatesTodo() {
        parentList.addTodo(newTodo_1);
        listRepository.persist(parentList);

        TodoDTO updatedTodoDTO =
            new TodoDTO(newTodo_1.getId(), NEW_TODO_TASK, true, NEW_TODO_POSITION, null, NEW_TODO_DUE_DATE);

        todoService.updateTodo(newTodo_1, updatedTodoDTO);
        assertEquals(newTodo_1.getTask(), updatedTodoDTO.task());
        assertTrue(newTodo_1.isCompleted());
        assertEquals(newTodo_1.getPosition(), updatedTodoDTO.position());
        assertEquals(newTodo_1.getDueDate(), updatedTodoDTO.dueDate());

        Todo updatedTodo = todoRepository.findById(newTodo_1.getId()).orElseThrow(TodoNotFoundException::new);
        assertEquals(updatedTodo.getTask(), updatedTodoDTO.task());
        assertTrue(updatedTodo.isCompleted());
        assertEquals(updatedTodo.getPosition(), updatedTodoDTO.position());
        assertEquals(updatedTodo.getDueDate(), updatedTodoDTO.dueDate());
    }

    @Test
    @DisplayName("updateTodos(): " + REMOVES_TODO_FROM_PARENT_WHEN_TODO_NOT_IN_DTO_SET)
    void updateTodos_TodoNotInDTOSet_RemovesTodoFromParent() {
        parentList.setTodos(TodosTestHelper.newTodoSet());
        listRepository.persist(parentList);

        Set<TodoDTO> updatedTodoDTOSet = new LinkedHashSet<>();
        Todo todoToRemove = null;
        TodoDTO lastTodoDTO = null;
        for (Todo todo : parentList.getTodos()) {
            TodoDTO newTodoDTO = new TodoDTO(
                todo.getId(), todo.getTask(), todo.isCompleted(), todo.getPosition(), null, todo.getDueDate()
            );
            updatedTodoDTOSet.add(newTodoDTO);
            todoToRemove = todo;
            lastTodoDTO = newTodoDTO;
        }

        updatedTodoDTOSet.remove(lastTodoDTO);

        todoService.updateTodos(parentList.getTodos(), updatedTodoDTOSet, parentList);
        assertEquals(2, parentList.getTodos().size());
        assertFalse(parentList.getTodos().contains(todoToRemove));
    }

    @Test
    @DisplayName("deleteTodo(): " + DELETES_TODO_WHEN_TODO_EXISTS)
    void deleteTodo_TodoExists_DeletesTodo() {
        parentList.addTodo(newTodo_1);
        listRepository.persist(parentList);

        todoService.deleteTodo(newTodo_1, parentList);
        assertFalse(todoRepository.findById(newTodo_1.getId()).isPresent());
        assertFalse(parentList.getTodos().contains(newTodo_1));
    }

}
