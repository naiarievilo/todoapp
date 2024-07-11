package dev.naiarievilo.todoapp.todolists.todos;

import dev.naiarievilo.todoapp.ServiceIntegrationTests;
import dev.naiarievilo.todoapp.todolists.ListTypes;
import dev.naiarievilo.todoapp.todolists.TodoList;
import dev.naiarievilo.todoapp.todolists.TodoListRepository;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoMapper;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.TodoNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static dev.naiarievilo.todoapp.todolists.todos.TodoServiceTestCases.*;
import static dev.naiarievilo.todoapp.todolists.todos.TodosTestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

class TodoServiceIntegrationTests extends ServiceIntegrationTests {

    @Autowired
    TodoRepository todoRepository;

    @Autowired
    TodoMapper todoMapper;

    @Autowired
    TodoListRepository listRepository;

    @Autowired
    TodoService todoService;

    private TodoDTO newTodoDTO_1;
    private Todo newTodo_1;
    private TodoList parentList;
    private Set<Todo> newTodoSet;
    private Set<TodoDTO> todoDTOSet;
    private TodoDTO newTodoDTO;

    @BeforeEach
    void setUp() {
        parentList = new TodoList();
        parentList.setType(ListTypes.PERSONALIZED);

        newTodoSet = TodosTestHelper.newTodoSet();
        todoDTOSet = TodosTestHelper.todoDTOSet();

        newTodoDTO_1 = TodosTestHelper.newTodoDTO_1();
        newTodo_1 = TodosTestHelper.newTodo_1();
        newTodoDTO = new TodoDTO(null, NEW_TODO_TASK, false, NEW_TODO_POSITION, null, NEW_TODO_DUE_DATE);
    }

    @Test
    @Transactional
    @DisplayName("createTodo(): " + CREATES_TODO_WHEN_INPUT_VALID)
    void createTodo_InputValidAndParentList_CreatesTodo() {
        listRepository.persist(parentList);

        Todo returnedTodo = todoService.createTodo(newTodoDTO_1, parentList);
        assertTrue(parentList.getTodos().contains(returnedTodo));
        assertTrue(todoRepository.findById(returnedTodo.getId()).isPresent());
    }

    @Test
    @Transactional
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
    @Transactional
    @DisplayName("updateTodos(): " + ADDS_NEW_TODO_TO_PARENT_WHEN_NEW_DTO_IN_DTO_SET)
    void updateTodos_NewTodoInDTOSet_AddsNewTodoToParent() {
        parentList.setTodos(newTodoSet);
        listRepository.persist(parentList);

        todoDTOSet.add(newTodoDTO);
        todoService.updateTodos(parentList.getTodos(), todoDTOSet, parentList);
        assertEquals(4, parentList.getTodos().size());

        Todo addedTodo = parentList.getTodos().stream().reduce((a, b) -> b).orElse(null);
        assertNotNull(addedTodo);
        assertEquals(newTodoDTO.task(), addedTodo.getTask());
        assertEquals(newTodoDTO.position(), addedTodo.getPosition());
        assertEquals(newTodoDTO.dueDate(), addedTodo.getDueDate());
    }

    @Test
    @Transactional
    @DisplayName("updateTodos(): " + REMOVES_TODO_FROM_PARENT_WHEN_TODO_NOT_IN_DTO_SET)
    void updateTodos_TodoNotInDTOSet_RemovesTodoFromParent() {
        parentList.setTodos(newTodoSet);
        listRepository.persist(parentList);

        todoDTOSet.remove(TodosTestHelper.todoDTO_1());

        todoService.updateTodos(parentList.getTodos(), todoDTOSet, parentList);
        assertEquals(2, parentList.getTodos().size());
        assertFalse(parentList.getTodos().contains(TodosTestHelper.todo_1()));
    }

    @Test
    @Transactional
    @DisplayName("updateTodos(): " + ADDS_AND_REMOVES_TODOS_FROM_PARENT_WHEN_DTO_SET_UPDATED)
    void updateTodos_NewAndDeletedTodosInDTOSet_AddsAndRemovesTodosFromParent() {
        parentList.setTodos(newTodoSet);
        listRepository.persist(parentList);

        todoDTOSet.remove(TodosTestHelper.todoDTO_1());
        todoDTOSet.add(newTodoDTO);

        todoService.updateTodos(parentList.getTodos(), todoDTOSet, parentList);
        assertEquals(3, parentList.getTodos().size());
    }

    @Test
    @Transactional
    @DisplayName("deleteTodo(): " + DELETES_TODO_WHEN_TODO_EXISTS)
    void deleteTodo_TodoExists_DeletesTodo() {
        parentList.addTodo(newTodo_1);
        listRepository.persist(parentList);

        todoService.deleteTodo(newTodo_1, parentList);
        assertFalse(todoRepository.findById(newTodo_1.getId()).isPresent());
        assertFalse(parentList.getTodos().contains(newTodo_1));
    }

}
