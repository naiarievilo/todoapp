package dev.naiarievilo.todoapp.users;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface PermissionRepository extends BaseJpaRepository<Permission, Long> {

    Optional<Permission> findByName(String permissionName);

    @Query("SELECT p FROM Permission AS p")
    List<Permission> findAll();
}
