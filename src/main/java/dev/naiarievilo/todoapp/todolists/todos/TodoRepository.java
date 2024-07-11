package dev.naiarievilo.todoapp.todolists.todos;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface TodoRepository extends BaseJpaRepository<Todo, Long> {

    @Query("""
            SELECT t
              FROM Todo AS t
        JOIN FETCH t.list AS l
        JOIN FETCH l.todos
             WHERE t.id = :id
        """)
    Optional<Todo> findByIdEagerly(Long id);

}
