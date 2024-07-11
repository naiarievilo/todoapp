package dev.naiarievilo.todoapp.todolists.todos;

import dev.naiarievilo.todoapp.todolists.TodoList;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static dev.naiarievilo.todoapp.todolists.todos.TodoServiceTestCases.*;
import static dev.naiarievilo.todoapp.todolists.todos.TodosTestHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TodoServiceUnitTests {

    @Mock
    TodoRepository todoRepository;

    @Mock
    TodoMapper todoMapper;

    @InjectMocks
    TodoServiceImpl todoService;

    @Captor
    private ArgumentCaptor<Todo> todoCaptor;
    private TodoDTO newTodoDTO_1;
    private Todo newTodo_1;
    private TodoDTO todoDTO_1;
    private Todo todo_1;
    private TodoList parentList;
    private Set<Todo> todoSet;
    private Set<TodoDTO> todoDTOSet;

    @BeforeEach
    void setUp() {
        parentList = new TodoList();

        todoSet = TodosTestHelper.todoSet();
        todoDTOSet = TodosTestHelper.todoDTOSet();

        todo_1 = TodosTestHelper.todo_1();
        todoDTO_1 = TodosTestHelper.todoDTO_1();
        newTodoDTO_1 =
            new TodoDTO(null, todoDTO_1.task(), false, todoDTO_1.position(), null, todoDTO_1.dueDate());

        newTodo_1 = new Todo();
        newTodo_1.setTask(newTodoDTO_1.task());
        newTodo_1.setCompleted(newTodoDTO_1.completed());
        newTodo_1.setPosition(newTodoDTO_1.position());
        newTodo_1.setDueDate(newTodoDTO_1.dueDate());
    }

    @Test
    @DisplayName("createTodo(): " + CREATES_TODO_WHEN_INPUT_VALID)
    void createTodo_InputValidAndParentList_CreatesTodo() {
        parentList.addTodo(todo_1);

        given(todoMapper.toEntity(newTodoDTO_1)).willReturn(newTodo_1);
        given(todoRepository.persist(any(Todo.class))).willReturn(todo_1);

        todoService.createTodo(newTodoDTO_1, parentList);
        verify(todoMapper).toEntity(newTodoDTO_1);
        verify(todoRepository).persist(todoCaptor.capture());
        Todo capturedTodo = todoCaptor.getValue();
        TodoList parent = capturedTodo.getList();
        assertNotNull(parent);
        assertTrue(parent.getTodos().contains(todo_1));
    }

    @Test
    @DisplayName("updateTodos(): " + ADDS_NEW_TODO_TO_PARENT_WHEN_NEW_DTO_IN_DTO_SET)
    void updateTodos_NewTodoInDTOSet_AddsNewTodoToParent() {
        parentList.setTodos(todoSet);
        TodoDTO newTodoDTO = new TodoDTO(null, NEW_TODO_TASK, true, NEW_TODO_POSITION, null, NEW_TODO_DUE_DATE);
        todoDTOSet.add(newTodoDTO);

        Todo newTodo = new Todo();
        newTodo.setId(NEW_TODO_ID);
        newTodo.setTask(newTodoDTO.task());
        newTodo.setCompleted(newTodoDTO.completed());
        newTodo.setPosition(newTodoDTO.position());

        given(todoMapper.toEntity(newTodoDTO)).willReturn(newTodo);

        todoService.updateTodos(parentList.getTodos(), todoDTOSet, parentList);
        assertEquals(4, parentList.getTodos().size());
        assertTrue(parentList.getTodos().contains(newTodo));

        verify(todoMapper, times(3)).updateEntityFromDTO(any(Todo.class), any(TodoDTO.class));
        verify(todoRepository, times(3)).update(any(Todo.class));
        verify(todoRepository).persist(todoCaptor.capture());
        Todo capturedTodo = todoCaptor.getValue();
        TodoList capturedParentList = capturedTodo.getList();
        assertEquals(capturedParentList, parentList);
        assertTrue(capturedParentList.getTodos().contains(capturedTodo));
    }

    @Test
    @DisplayName("updateTodos(): " + REMOVES_TODO_FROM_PARENT_WHEN_TODO_NOT_IN_DTO_SET)
    void updateTodos_TodoNotInDTOSet_RemovesTodoFromParent() {
        parentList.setTodos(todoSet);
        todoDTOSet.remove(todoDTO_1);

        todoService.updateTodos(parentList.getTodos(), todoDTOSet, parentList);
        assertEquals(2, parentList.getTodos().size());
        assertFalse(parentList.getTodos().contains(todo_1));

        verify(todoMapper, times(2)).updateEntityFromDTO(any(Todo.class), any(TodoDTO.class));
        verify(todoRepository).delete(todoCaptor.capture());
        Todo deletedTodo = todoCaptor.getValue();
        assertNull(deletedTodo.getList());
        assertEquals(todo_1.getId(), deletedTodo.getId());
    }

    @Test
    @DisplayName("updateTodos(): " + ADDS_AND_REMOVES_TODOS_FROM_PARENT_WHEN_DTO_SET_UPDATED)
    void updateTodos_NewAndDeletedTodosInDTOSet_AddsAndRemovesTodosFromParent() {
        parentList.setTodos(todoSet);
        todoDTOSet.remove(todoDTO_1);

        TodoDTO newTodoDTO = new TodoDTO(null, NEW_TODO_TASK, true, NEW_TODO_POSITION, null, NEW_TODO_DUE_DATE);
        todoDTOSet.add(newTodoDTO);

        Todo newTodo = new Todo();
        newTodo.setId(NEW_TODO_ID);
        newTodo.setTask(newTodoDTO.task());
        newTodo.setCompleted(newTodoDTO.completed());
        newTodo.setPosition(newTodoDTO.position());

        given(todoMapper.toEntity(newTodoDTO)).willReturn(newTodo);

        todoService.updateTodos(parentList.getTodos(), todoDTOSet, parentList);
        assertEquals(3, parentList.getTodos().size());
        assertTrue(parentList.getTodos().contains(newTodo));
        assertFalse(parentList.getTodos().contains(todo_1));

        verify(todoMapper, times(2)).updateEntityFromDTO(any(Todo.class), any(TodoDTO.class));
        verify(todoRepository, times(2)).update(any(Todo.class));
        verify(todoRepository).persist(todoCaptor.capture());
        Todo capturedTodo = todoCaptor.getValue();
        assertEquals(newTodo.getId(), capturedTodo.getId());
        assertEquals(newTodo.getList(), parentList);
    }

    @Test
    @DisplayName("deleteTodo(): " + DELETES_TODO_WHEN_TODO_EXISTS)
    void deleteTodo_TodoExists_DeletesTodo() {
        parentList.setTodos(todoSet);
        todoService.deleteTodo(todo_1, parentList);
        assertFalse(parentList.getTodos().contains(todo_1));

        verify(todoRepository).delete(todoCaptor.capture());
        Todo deletedTodo = todoCaptor.getValue();
        assertNull(deletedTodo.getList());
        assertEquals(todo_1.getId(), deletedTodo.getId());
    }
}
