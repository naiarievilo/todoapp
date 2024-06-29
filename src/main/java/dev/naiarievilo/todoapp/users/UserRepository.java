package dev.naiarievilo.todoapp.users;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface UserRepository extends BaseJpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User AS u JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdEagerly(Long id);

    @Query("SELECT u FROM User AS u JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailEagerly(String email);

    @Transactional
    void deleteByEmail(String email);
}
