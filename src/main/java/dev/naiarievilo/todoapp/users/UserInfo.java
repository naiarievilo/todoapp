package dev.naiarievilo.todoapp.users;

import jakarta.persistence.*;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.NaturalId;

import static dev.naiarievilo.todoapp.validation.ValidationErrorMessages.NOT_BLANK;
import static dev.naiarievilo.todoapp.validation.ValidationErrorMessages.NOT_NULL;

@Entity
@Table(name = "users_info")
public class UserInfo {

    @Id
    @NaturalId
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private User user;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        Validate.notNull(id, NOT_NULL);
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        Validate.notNull(user, NOT_NULL);
        this.user = user;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        Validate.notBlank(firstName, NOT_BLANK);
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        Validate.notBlank(lastName, NOT_BLANK);
        this.lastName = lastName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hb = new HashCodeBuilder();
        hb.append(id);
        return hb.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof UserInfo other)) {
            return false;
        }

        EqualsBuilder eb = new EqualsBuilder();
        eb.append(id, other.id);
        return eb.isEquals();
    }

}