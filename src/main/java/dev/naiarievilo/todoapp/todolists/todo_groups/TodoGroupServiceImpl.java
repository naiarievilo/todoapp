package dev.naiarievilo.todoapp.todolists.todo_groups;

import dev.naiarievilo.todoapp.todolists.TodoList;
import dev.naiarievilo.todoapp.todolists.todo_groups.dtos.TodoGroupDTO;
import dev.naiarievilo.todoapp.todolists.todo_groups.dtos.TodoGroupMapper;
import dev.naiarievilo.todoapp.todolists.todo_groups.exceptions.TodoGroupNotFoundException;
import dev.naiarievilo.todoapp.todolists.todos.TodoService;
import dev.naiarievilo.todoapp.todolists.todos.dtos.TodoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class TodoGroupServiceImpl implements TodoGroupService {

    private final TodoGroupRepository groupRepository;
    private final TodoGroupMapper groupMapper;
    private final TodoService todoService;


    public TodoGroupServiceImpl(TodoGroupRepository groupRepository, TodoGroupMapper groupMapper,
        TodoService todoService) {
        this.groupRepository = groupRepository;
        this.groupMapper = groupMapper;
        this.todoService = todoService;
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
    public TodoGroupDTO createGroup(TodoGroupDTO groupDTO, TodoList parent) {
        TodoGroup newGroup = groupMapper.toEntity(groupDTO);
        parent.addGroup(newGroup);
        groupRepository.persist(newGroup);
        return groupMapper.toDTO(newGroup);
    }

    @Override
    @Transactional
    public TodoGroupDTO updateGroup(TodoGroupDTO groupDTO) {
        TodoGroup group = getGroupByIdWithTodos(groupDTO.id());
        groupMapper.updateEntityFromDTO(group, groupDTO);

        Set<TodoDTO> todosDTO = groupDTO.todos();
        if (todosDTO != null) {
            todoService.updateTodos(group.getTodos(), todosDTO, group);
        }

        groupRepository.merge(group);
        return groupMapper.toDTO(group);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public void updateGroups(Set<TodoGroup> groups, Set<TodoGroupDTO> groupsDTO, TodoList parent) {
        Map<Long, TodoGroup> groupsMap = new HashMap<>();
        for (TodoGroup group : groups) {
            groupsMap.put(group.getId(), group);
        }

        Set<Long> matchedGroupIds = new LinkedHashSet<>();
        for (TodoGroupDTO groupDTO : groupsDTO) {
            TodoGroup group = groupsMap.get(groupDTO.id());
            if (group != null) {
                matchedGroupIds.add(group.getId());
                groupMapper.updateEntityFromDTO(group, groupDTO);
            } else {
                TodoGroup newGroup = groupMapper.toEntity(groupDTO);
                parent.addGroup(newGroup);
            }
        }

        if (matchedGroupIds.size() == groupsMap.size()) {
            return;
        }

        Set<Long> unmatchedGroupIds = groupsMap.keySet();
        unmatchedGroupIds.removeAll(matchedGroupIds);
        for (Long unmatchedGroupId : unmatchedGroupIds) {
            TodoGroup groupToRemove = groupsMap.get(unmatchedGroupId);
            parent.removeGroup(groupToRemove);
        }
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
