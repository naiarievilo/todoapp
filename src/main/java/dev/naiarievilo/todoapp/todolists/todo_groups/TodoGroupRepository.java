package dev.naiarievilo.todoapp.todolists.todo_groups;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface TodoGroupRepository extends BaseJpaRepository<TodoGroup, Long> {

    @Query("""
            SELECT tg
              FROM TodoGroup AS tg
        JOIN FETCH tg.todos
        JOIN FETCH tg.list
             WHERE tg.id = :id
        """)
    Optional<TodoGroup> findByIdEagerly(Long id);

    @Query("""
            SELECT tg
              FROM TodoGroup AS tg
        JOIN FETCH tg.todos AS t
        JOIN FETCH t.group.todos
             WHERE tg.id = :id
        """)
    Optional<TodoGroup> findByIdWithTodos(Long id);


    @Query("""
            SELECT tg
              FROM TodoGroup AS tg
        JOIN FETCH tg.list AS l
        JOIN FETCH l.groups
             WHERE tg.id = :id
        """)
    Optional<TodoGroup> findByIdWithList(Long id);
}
