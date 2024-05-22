package dev.naiarievilo.todoapp.roles;

import dev.naiarievilo.todoapp.users.User;
import jakarta.persistence.*;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.NaturalId;

import java.util.LinkedHashSet;
import java.util.Set;

import static dev.naiarievilo.todoapp.validation.ValidationErrorMessages.*;

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

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new LinkedHashSet<>();

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        Validate.notEmpty(users, NOT_EMPTY);
        Validate.noNullElements(users, NO_NULL_ELEMENTS);
        this.users = users;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        Validate.notNull(id, NOT_NULL);
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Validate.notBlank(name, NOT_BLANK);
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        Validate.notBlank(description, NOT_BLANK);
        this.description = description;
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