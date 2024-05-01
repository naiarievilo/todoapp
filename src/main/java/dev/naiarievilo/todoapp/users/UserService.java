package dev.naiarievilo.todoapp.users;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.GroupManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
public class UserService implements UserDetailsManager, GroupManager {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public UserService(PermissionRepository permissionRepository, RoleRepository roleRepository,
        UserRepository userRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<String> findAllGroups() {
        List<Role> distinctRoles = roleRepository.findDistinctRoles();

        return distinctRoles.stream()
            .map(Role::getRole)
            .toList();
    }

    @Override
    public List<String> findUsersInGroup(String groupName) {
        Role role = roleRepository.findByRole(groupName).orElseThrow(RoleNotFoundException::new);

        return role.getUsers().stream()
            .map(User::getUsername)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createGroup(String groupName, List<GrantedAuthority> authorities) {
        Set<Permission> permissions = authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .map(permissionRepository::findByPermission)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());

        Role newRole = new Role();
        newRole.setRole(groupName);
        newRole.setPermissions(permissions);

        roleRepository.save(newRole);
    }

    @Override
    @Transactional
    public void deleteGroup(String groupName) {
        Role role = roleRepository.findByRole(groupName).orElseThrow(RoleNotFoundException::new);

        role.removeAllPermissions();
        for (User user : role.getUsers()) {
            user.removeRole(role);
        }

        roleRepository.deleteByRole(groupName);
    }

    @Override
    public void renameGroup(String oldName, String newName) {
        throw new UnsupportedOperationException("Method not supported.");
    }

    @Override
    @Transactional
    public void addUserToGroup(String username, String group) {
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        Role role = roleRepository.findByRole(group).orElseThrow(RoleNotFoundException::new);
        user.addRole(role);
    }

    @Override
    @Transactional
    public void removeUserFromGroup(String username, String groupName) {
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        user.getRoles().removeIf(role -> role.getRole().equals(groupName));
    }

    @Override
    public List<GrantedAuthority> findGroupAuthorities(String groupName) {
        Role role = roleRepository.findByRole(groupName).orElseThrow(RoleNotFoundException::new);

        return role.getPermissions().stream()
            .map(Permission::getPermission)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addGroupAuthority(String groupName, GrantedAuthority authority) {
        Role role = roleRepository.findByRole(groupName).orElseThrow(RoleNotFoundException::new);
        Permission permission = permissionRepository.findByPermission(authority.getAuthority())
            .orElseThrow(PermissionNotFoundException::new);

        role.addPermission(permission);
    }

    @Override
    @Transactional
    public void removeGroupAuthority(String groupName, GrantedAuthority authority) {
        Role role = roleRepository.findByRole(groupName).orElseThrow(RoleNotFoundException::new);
        role.getPermissions().removeIf(permission ->
            permission.getPermission().equals(authority.getAuthority()));
    }

    @Override
    @Transactional
    public void createUser(UserDetails user) {

    }

    @Override
    @Transactional
    public void updateUser(UserDetails user) {

    }

    @Override
    @Transactional
    public void deleteUser(String username) {

    }

    @Override
    @Transactional
    public void changePassword(String oldPassword, String newPassword) {

    }

    @Override
    public boolean userExists(String username) {
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
