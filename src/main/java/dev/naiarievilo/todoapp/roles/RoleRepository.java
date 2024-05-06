package dev.naiarievilo.todoapp.roles;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends BaseJpaRepository<Role, Long> {

    Optional<Role> findByName(String role);

    @Query("SELECT r FROM Role AS r")
    List<Role> findAll();

    void deleteByName(String role);
}
