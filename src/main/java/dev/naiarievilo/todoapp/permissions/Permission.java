package dev.naiarievilo.todoapp.permissions;

import dev.naiarievilo.todoapp.roles.Role;
import jakarta.persistence.*;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.NaturalId;

import java.util.LinkedHashSet;
import java.util.Set;

import static dev.naiarievilo.todoapp.validation.ValidationMessages.*;

@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NaturalId
    @Column(name = "permission", unique = true, nullable = false, updatable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles = new LinkedHashSet<>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        Validate.notBlank(description, NOT_BLANK.message());
        this.description = description;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        Validate.noNullElements(roles, NO_NULL_ELEMENTS.message());
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        Validate.notNull(id, NOT_NULL.message());
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Validate.notBlank(name, NOT_BLANK.message());
        this.name = name;
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

        if (!(obj instanceof Permission other)) {
            return false;
        }

        EqualsBuilder eb = new EqualsBuilder();
        eb.append(name, other.name);
        return eb.isEquals();
    }

}