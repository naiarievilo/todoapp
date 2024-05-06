package dev.naiarievilo.todoapp.permissions;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends BaseJpaRepository<Permission, Long> {

    Optional<Permission> findByName(String permissionName);

    @Query("SELECT p FROM Permission AS p")
    List<Permission> findAll();

    void deleteByName(String name);
}
