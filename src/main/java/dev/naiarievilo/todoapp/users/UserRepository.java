package dev.naiarievilo.todoapp.users;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;

import java.util.Optional;

public interface UserRepository extends BaseJpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    void deleteByEmail(String email);
}
