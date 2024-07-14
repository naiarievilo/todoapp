package dev.naiarievilo.todoapp.todolists.todos;

import dev.naiarievilo.todoapp.todolists.TodoList;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoMapper;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.PositionExceedsMaxAllowedException;
import dev.naiarievilo.todoapp.todolists.todos.exceptions.PositionNotUniqueException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import static dev.naiarievilo.todoapp.todolists.todos.TodoServiceTestCases.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    TodoRepository todoRepository;

    @Mock
    TodoMapper todoMapper;

    @InjectMocks
    TodoService todoService;

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
    @DisplayName("updateTodos(): " + REMOVES_TODO_FROM_PARENT_WHEN_TODO_NOT_IN_DTO_SET)
    void updateTodos_TodoNotInDTOSet_RemovesTodoFromParent() {
        parentList.setTodos(todoSet);
        todoDTOSet.remove(todoDTO_1);

        int newPosition = 1;
        Set<TodoDTO> updatedDTOSet = new LinkedHashSet<>();
        for (TodoDTO todoDTO : todoDTOSet) {
            TodoDTO updatedTodoDTO = new TodoDTO(
                todoDTO.id(),
                todoDTO.task(),
                todoDTO.completed(),
                newPosition++,
                todoDTO.createdAt(),
                todoDTO.dueDate()
            );

            updatedDTOSet.add(updatedTodoDTO);
        }

        todoService.updateTodos(parentList.getTodos(), updatedDTOSet, parentList);
        assertEquals(2, parentList.getTodos().size());
        assertFalse(parentList.getTodos().contains(todo_1));

        verify(todoMapper, times(2)).updateEntityFromDTO(any(Todo.class), any(TodoDTO.class));
        verify(todoRepository).delete(todoCaptor.capture());
        Todo deletedTodo = todoCaptor.getValue();
        assertNull(deletedTodo.getList());
        assertEquals(todo_1.getId(), deletedTodo.getId());
    }

    @Test
    @DisplayName("updateTodos(): " + THROWS_POSITION_NOT_UNIQUE_WHEN_TODOS_HAVE_SAME_POSITION)
    void updateTodos_PositionNotUniqueInList_ThrowsPositionNotUniqueException() {
        parentList.setTodos(todoSet);

        Integer samePosition = 3;
        Set<TodoDTO> updatedDTOSet = new LinkedHashSet<>();
        for (TodoDTO todoDTO : todoDTOSet) {
            TodoDTO updatedTodoDTO = new TodoDTO(
                todoDTO.id(),
                todoDTO.task(),
                todoDTO.completed(),
                samePosition,
                todoDTO.createdAt(),
                todoDTO.dueDate()
            );
            updatedDTOSet.add(updatedTodoDTO);
        }

        Set<Todo> parentTodos = parentList.getTodos();
        assertThrows(PositionNotUniqueException.class,
            () -> todoService.updateTodos(parentTodos, updatedDTOSet, parentList));

        Todo parentTodo_1 = parentTodos.iterator().next();
        verify(todoMapper).updateEntityFromDTO(parentTodo_1, updatedDTOSet.iterator().next());
        verify(todoRepository).update(parentTodo_1);
    }

    @Test
    @DisplayName("updateTodos(): " + THROWS_POSITION_EXCEEDS_MAX_ALLOWED_WHEN_POSITION_IS_GREATER_THAN_LIST_SIZE)
    void updateTodos_PositionExceedsMaxAllowedInList_ThrowsPositionExceedsMaxAllowedException() {
        parentList.setTodos(todoSet);

        Iterator<TodoDTO> dtoSetIterator = todoDTOSet.iterator();
        Set<TodoDTO> updatedTodoDTOSet = new LinkedHashSet<>();
        int newPosition = 1;
        while (dtoSetIterator.hasNext()) {
            TodoDTO todoDTO = dtoSetIterator.next();
            TodoDTO updatedTodoDTO = new TodoDTO(
                todoDTO.id(),
                todoDTO.task(),
                todoDTO.completed(),
                (dtoSetIterator.hasNext() ? newPosition++ : (todoDTOSet.size() + 1)),
                todoDTO.createdAt(),
                todoDTO.dueDate()
            );
            updatedTodoDTOSet.add(updatedTodoDTO);
        }

        Set<Todo> parentTodos = parentList.getTodos();
        assertThrows(PositionExceedsMaxAllowedException.class,
            () -> todoService.updateTodos(parentTodos, updatedTodoDTOSet, parentList));

        Iterator<Todo> parentTodosIterator = parentTodos.iterator();
        Iterator<TodoDTO> updatedDTOSetIterator = updatedTodoDTOSet.iterator();
        Todo parentTodo_1 = parentTodosIterator.next();
        Todo parentTodo_2 = parentTodosIterator.next();
        TodoDTO updatedDTO_1 = updatedDTOSetIterator.next();
        TodoDTO updatedDTO_2 = updatedDTOSetIterator.next();

        verify(todoMapper).updateEntityFromDTO(parentTodo_1, updatedDTO_1);
        verify(todoMapper).updateEntityFromDTO(parentTodo_2, updatedDTO_2);
        verify(todoRepository).update(parentTodo_1);
        verify(todoRepository).update(parentTodo_2);
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
