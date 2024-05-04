package dev.naiarievilo.todoapp.users;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface RoleRepository extends BaseJpaRepository<Role, Long> {

    Optional<Role> findByRole(String role);

    @Query("SELECT DISTINCT r.role FROM Role AS r")
    List<Role> findDistinctRoles();

    @Transactional
    void deleteByRole(String role);
}
