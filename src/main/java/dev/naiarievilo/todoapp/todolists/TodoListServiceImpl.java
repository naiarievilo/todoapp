package dev.naiarievilo.todoapp.todolists;

import dev.naiarievilo.todoapp.todolists.dtos.TodoListDTO;
import dev.naiarievilo.todoapp.todolists.dtos.TodoListMapper;
import dev.naiarievilo.todoapp.todolists.exceptions.TodoListNotFoundException;
import dev.naiarievilo.todoapp.todolists.todo_groups.TodoGroupService;
import dev.naiarievilo.todoapp.todolists.todo_groups.dtos.TodoGroupDTO;
import dev.naiarievilo.todoapp.todolists.todos.TodoService;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import dev.naiarievilo.todoapp.users.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional(readOnly = true)
public class TodoListServiceImpl implements TodoListService {

    private final TodoListRepository listRepository;
    private final TodoListMapper listMapper;
    private final TodoGroupService groupService;
    private final TodoService todoService;


    public TodoListServiceImpl(TodoListRepository listRepository, TodoListMapper listMapper,
        TodoGroupService groupService, TodoService todoService) {
        this.listRepository = listRepository;
        this.listMapper = listMapper;

        this.groupService = groupService;
        this.todoService = todoService;
    }

    @Override
    public TodoList getListById(Long id) {
        return listRepository.findById(id).orElseThrow(() -> new TodoListNotFoundException(id));
    }

    @Override
    public TodoList getListByIdEagerly(Long id) {
        return listRepository.findByIdEagerly(id).orElseThrow(() -> new TodoListNotFoundException(id));
    }

    @Override
    public TodoList getListByIdWithGroups(Long id) {
        return listRepository.findByIdWithGroups(id).orElseThrow(() -> new TodoListNotFoundException(id));
    }

    @Override
    public TodoList getListByIdWithTodos(Long id) {
        return listRepository.findByIdWithTodos(id).orElseThrow(() -> new TodoListNotFoundException(id));
    }

    @Override
    @Transactional
    public TodoListDTO createList(TodoListDTO listDTO, User user) {
        TodoList newList = listMapper.toEntity(listDTO);
        newList.setUser(user);
        listRepository.persist(newList);
        return listMapper.toDTO(newList);
    }

    @Override
    @Transactional
    public TodoListDTO updateList(TodoListDTO listDTO) {
        TodoList list = getListByIdEagerly(listDTO.id());
        listMapper.updateEntityFromDTO(list, listDTO);

        Set<TodoGroupDTO> groupsDTO = listDTO.groups();
        Set<TodoDTO> todosDTO = listDTO.todos();
        if (groupsDTO != null) {
            groupService.updateGroups(list.getGroups(), groupsDTO, list);

        } else if (todosDTO != null) {
            todoService.updateTodos(list.getTodos(), todosDTO, list);
        }

        listRepository.merge(list);
        return listMapper.toDTO(list);
    }

    @Override
    @Transactional
    public void deleteList(TodoListDTO listDTO) {
        TodoList list = getListById(listDTO.id());
        listRepository.delete(list);
    }
}
