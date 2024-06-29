package dev.naiarievilo.todoapp.todolists;

import dev.naiarievilo.todoapp.todolists.dtos.TodoListDTO;
import dev.naiarievilo.todoapp.todolists.dtos.TodoListMapper;
import dev.naiarievilo.todoapp.todolists.exceptions.TodoListNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TodoListServiceImpl implements TodoListService {

    private final TodoListRepository listRepository;
    private final TodoListMapper listMapper;

    public TodoListServiceImpl(TodoListRepository listRepository, TodoListMapper listMapper) {
        this.listRepository = listRepository;
        this.listMapper = listMapper;
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
    public TodoListDTO createList(TodoListDTO listDTO) {
        TodoList newList = listMapper.toEntity(listDTO);
        listRepository.persist(newList);
        return listMapper.toDTO(newList);
    }

    @Override
    @Transactional
    public TodoListDTO updateList(TodoListDTO listDTO) {
        TodoList list = getListByIdEagerly(listDTO.id());
        listMapper.updateListFromDTO(list, listDTO);
        listRepository.update(list);
        return listMapper.toDTO(list);
    }

    @Override
    @Transactional
    public void deleteList(TodoListDTO listDTO) {
        TodoList list = getListById(listDTO.id());
        listRepository.delete(list);
    }
}
