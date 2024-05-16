package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.roles.Role;
import jakarta.persistence.*;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.NaturalId;

import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NaturalId
    @Column(name = "email", unique = true, nullable = false, length = 320)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Generated
    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked;

    @Generated
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts;

    @Column(name = "failed_login_time")
    private LocalTime failedLoginTime;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(name = "users_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        Validate.notNull(id, NOT_NULL.message());
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        Validate.notBlank(email, NOT_BLANK.message());
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        Validate.notBlank(password, NOT_BLANK.message());
        this.password = password;
    }

    public boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(Boolean isLocked) {
        Validate.notNull(isLocked, NOT_NULL.message());
        this.isLocked = isLocked;
    }

    public boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean isEnabled) {
        Validate.notNull(isEnabled, NOT_NULL.message());
        this.isEnabled = isEnabled;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        Validate.notNull(failedLoginAttempts, NOT_NULL.message());
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public void incrementFailedLoginAttempts() {
        failedLoginAttempts++;
    }

    public LocalTime getFailedLoginTime() {
        return failedLoginTime;
    }

    public void setFailedLoginTime(LocalTime failedLoginTime) {
        Validate.notNull(failedLoginTime, NOT_NULL.message());
        this.failedLoginTime = failedLoginTime;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        Validate.noNullElements(roles, NO_NULL_ELEMENTS.message());
        addRoles(roles);
    }

    public void addRoles(Set<Role> roles) {
        Validate.noNullElements(roles, NO_NULL_ELEMENTS.message());
        for (Role role : roles) {
            this.roles.add(role);
            role.getUsers().add(this);
        }
    }

    public void addRole(Role role) {
        Validate.notNull(role, NOT_NULL.message());
        roles.add(role);
        role.getUsers().add(this);
    }

    public void removeRole(Role role) {
        Validate.notNull(role, NOT_NULL.message());
        roles.remove(role);
        role.getUsers().remove(this);
    }

    public void removeRoles(Set<Role> roles) {
        Validate.noNullElements(roles, NO_NULL_ELEMENTS.message());
        for (Role role : roles) {
            this.roles.remove(role);
            role.getUsers().remove(this);
        }
    }

    public void removeAllRoles() {
        for (Role role : new LinkedHashSet<>(roles)) {
            role.getUsers().remove(this);
            roles.remove(role);
        }
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        hcb.append(email);
        return hcb.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof User other)) {
            return false;
        }

        EqualsBuilder eb = new EqualsBuilder();
        eb.append(email, other.email);
        return eb.isEquals();
    }

}