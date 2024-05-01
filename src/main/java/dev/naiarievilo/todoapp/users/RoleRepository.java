package dev.naiarievilo.todoapp.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRole(String role);

    @Query("SELECT DISTINCT r.role FROM Role AS r")
    List<Role> findDistinctRoles();

    @Transactional
    void deleteByRole(String role);
}
