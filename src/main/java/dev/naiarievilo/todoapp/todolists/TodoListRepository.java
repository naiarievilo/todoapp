package dev.naiarievilo.todoapp.todolists;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface TodoListRepository extends BaseJpaRepository<TodoList, Long> {

    @Query("""
            SELECT tl
              FROM TodoList AS tl
        JOIN FETCH tl.todos
        JOIN FETCH tl.groups AS tg
        JOIN FETCH tg.todos
             WHERE tl.id = :id
        """)
    Optional<TodoList> findByIdEagerly(Long id);


    @Query("""
            SELECT tl
              FROM TodoList AS tl
        JOIN FETCH tl.todos
             WHERE tl.id = :id
        """)
    Optional<TodoList> findByIdWithTodos(Long id);


    @Query("""
            SELECT tl
              FROM TodoList AS tl
        JOIN FETCH tl.groups
             WHERE tl.id = :id
        """)
    Optional<TodoList> findByIdWithGroups(Long id);
}
