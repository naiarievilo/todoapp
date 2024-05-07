package dev.naiarievilo.todoapp.users;

import dev.naiarievilo.todoapp.roles.Role;
import jakarta.persistence.*;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.NaturalId;

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
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", unique = true, nullable = false, length = 320)
    private String email;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @ColumnDefault("false")
    @Column(name = "is_expired", nullable = false)
    private boolean isExpired = false;

    @ColumnDefault("false")
    @Column(name = "is_locked", nullable = false)
    private boolean isLocked = false;

    @ColumnDefault("false")
    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = false;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        Validate.notBlank(username, NOT_BLANK.message());
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        Validate.notBlank(password, NOT_BLANK.message());
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        Validate.notBlank(email, NOT_BLANK.message());
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        Validate.notBlank(firstName, NOT_BLANK.message());
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        Validate.notBlank(lastName, NOT_BLANK.message());
        this.lastName = lastName;
    }

    public boolean getIsExpired() {
        return isExpired;
    }

    public void setIsExpired(boolean isExpired) {
        this.isExpired = isExpired;
    }

    public boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
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
        hcb.append(username);
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
        eb.append(username, other.username);
        return eb.isEquals();
    }

}