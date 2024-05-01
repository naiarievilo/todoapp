package dev.naiarievilo.todoapp.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByPermission(String permissionName);
}
