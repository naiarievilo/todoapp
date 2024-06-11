package dev.naiarievilo.todoapp.users;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface UserRepository extends BaseJpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Transactional
    void deleteByEmail(String email);
}
