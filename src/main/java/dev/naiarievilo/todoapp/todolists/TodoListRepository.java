package dev.naiarievilo.todoapp.todolists;

import dev.naiarievilo.todoapp.users.User;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface TodoListRepository extends BaseJpaRepository<TodoList, Long> {

    @Query(value = """
                 SELECT tl
                   FROM TodoList AS tl
             JOIN FETCH tl.user
        LEFT JOIN FETCH tl.todos
                  WHERE tl.user = :user
                    AND tl.type = :type
        """)
    Optional<TodoList> findByType(ListTypes type, User user);

    @Query(value = """
                 SELECT tl
                   FROM TodoList AS tl
             JOIN FETCH tl.user
        LEFT JOIN FETCH tl.todos
                  WHERE tl.user = :user
                    AND tl.type = :type
        """)
    List<TodoList> findAllByType(ListTypes type, User user);

    @Query("""
                 SELECT tl
                   FROM TodoList AS tl
             JOIN FETCH tl.user
        LEFT JOIN FETCH tl.todos
                  WHERE tl.id = :id
        """)
    Optional<TodoList> findByIdEagerly(Long id);

    @Query("""
                 SELECT tl
                   FROM TodoList as tl
             JOIN FETCH tl.user
        LEFT JOIN FETCH tl.todos
                  WHERE tl.user = :user
                    AND tl.type = :type
                    AND tl.dueDate = :date
        """)
    Optional<TodoList> findByTypeAndDueDate(ListTypes type, LocalDate date, User user);

    @Transactional
    @Modifying
    @Query("""
                 DELETE
                   FROM TodoList AS tl
                  WHERE tl.type = :type
                    AND tl.dueDate < :date
        """)
    void deleteAllByTypeAndDueDate(ListTypes type, LocalDate date);
}
