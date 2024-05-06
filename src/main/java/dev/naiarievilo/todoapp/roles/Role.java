package dev.naiarievilo.todoapp.roles;

import dev.naiarievilo.todoapp.permissions.Permission;
import dev.naiarievilo.todoapp.users.User;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.NaturalId;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NaturalId
    @Column(name = "role", unique = true, nullable = false, updatable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(name = "roles_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new LinkedHashSet<>();

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        addPermissions(permissions);
    }

    public void addPermissions(Set<Permission> permissions) {
        for (Permission permission : permissions) {
            this.permissions.add(permission);
            permission.getRoles().add(this);
        }
    }

    public void addPermission(Permission permission) {
        permissions.add(permission);
        permission.getRoles().add(this);
    }

    public void removePermission(Permission permission) {
        permissions.remove(permission);
        permission.getRoles().remove(this);
    }

    public void removePermissions(Set<Permission> permissions) {
        for (Permission permission : permissions) {
            this.permissions.remove(permission);
            permission.getRoles().remove(this);
        }
    }

    public void removeAllPermissions() {
        for (Permission permission : new LinkedHashSet<>(permissions)) {
            permissions.remove(permission);
            permission.getRoles().remove(this);
        }
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        hcb.append(name);
        return hcb.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Role other)) {
            return false;
        }

        EqualsBuilder eb = new EqualsBuilder();
        eb.append(name, other.name);
        return eb.isEquals();
    }
}