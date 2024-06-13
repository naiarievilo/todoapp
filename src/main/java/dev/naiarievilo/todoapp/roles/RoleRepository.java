package dev.naiarievilo.todoapp.roles;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface RoleRepository extends BaseJpaRepository<Role, Long> {

    Optional<Role> findByName(String role);

    @Query("SELECT r FROM Role r")
    List<Role> findAll();

    @Transactional
    void deleteByName(String role);

    @Transactional
    @Modifying
    @Query("DELETE FROM Role")
    void deleteAll();
}
