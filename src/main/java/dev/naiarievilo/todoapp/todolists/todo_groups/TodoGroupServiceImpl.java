package dev.naiarievilo.todoapp.todolists.todo_groups;

import dev.naiarievilo.todoapp.todolists.TodoList;
import dev.naiarievilo.todoapp.todolists.TodoListService;
import dev.naiarievilo.todoapp.todolists.todo_groups.dtos.TodoGroupDTO;
import dev.naiarievilo.todoapp.todolists.todo_groups.dtos.TodoGroupMapper;
import dev.naiarievilo.todoapp.todolists.todo_groups.exceptions.TodoGroupNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class TodoGroupServiceImpl implements TodoGroupService {

    private final TodoGroupRepository groupRepository;
    private final TodoListService listService;
    private final TodoGroupMapper groupMapper;


    public TodoGroupServiceImpl(TodoGroupRepository groupRepository, TodoListService listService,
        TodoGroupMapper groupMapper) {
        this.groupRepository = groupRepository;
        this.listService = listService;
        this.groupMapper = groupMapper;
    }

    @Override
    public TodoGroup getGroupById(Long id) {
        return groupRepository.findById(id).orElseThrow(() -> new TodoGroupNotFoundException(id));
    }

    @Override
    public TodoGroup getGroupByIdWithTodos(Long id) {
        return groupRepository.findByIdWithTodos(id).orElseThrow(() -> new TodoGroupNotFoundException(id));
    }

    @Override
    public TodoGroup getGroupByIdWithList(Long id) {
        return groupRepository.findByIdWithList(id).orElseThrow(() -> new TodoGroupNotFoundException(id));
    }

    @Override
    @Transactional
    public TodoGroupDTO createGroup(TodoGroupDTO groupDTO) {
        TodoList list = listService.getListByIdWithGroups(groupDTO.listId());
        TodoGroup newGroup = groupMapper.toEntity(groupDTO);
        list.addGroup(newGroup);
        groupRepository.persist(newGroup);
        return groupMapper.toDTO(newGroup);
    }

    @Override
    @Transactional
    public TodoGroupDTO updateGroup(TodoGroupDTO groupDTO) {
        TodoGroup group = getGroupById(groupDTO.id());
        groupMapper.updateGroupFromDTO(group, groupDTO);
        groupRepository.update(group);
        return groupMapper.toDTO(group);
    }

    @Override
    @Transactional
    public void deleteGroup(TodoGroupDTO groupDTO) {
        TodoGroup group = getGroupByIdWithList(groupDTO.id());
        TodoList list = Objects.requireNonNull(group.getList());
        list.removeGroup(group);
        groupRepository.delete(group);
    }
}
