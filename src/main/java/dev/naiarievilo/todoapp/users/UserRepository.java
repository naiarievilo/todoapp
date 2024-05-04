package dev.naiarievilo.todoapp.users;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface UserRepository extends BaseJpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Modifying
    @Transactional
    void deleteByUsername(String username);
}
