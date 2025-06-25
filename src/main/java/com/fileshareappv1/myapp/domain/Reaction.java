package com.fileshareappv1.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fileshareappv1.myapp.domain.enumeration.ReactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * A Reaction.
 */
@Entity
@Table(name = "reaction")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "reaction")
@SuppressWarnings("common-java:DuplicatedBlocks")
@EntityListeners(AuditingEntityListener.class)
public class Reaction implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Keyword)
    private ReactionType type;

    @LastModifiedDate
    @Column(name = "reacted_at", nullable = false)
    private Instant reactedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "user", "tags" }, allowSetters = true)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Reaction id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReactionType getType() {
        return this.type;
    }

    public Reaction type(ReactionType type) {
        this.setType(type);
        return this;
    }

    public void setType(ReactionType type) {
        this.type = type;
    }

    public Instant getReactedAt() {
        return this.reactedAt;
    }

    public Reaction reactedAt(Instant reactedAt) {
        this.setReactedAt(reactedAt);
        return this;
    }

    public void setReactedAt(Instant reactedAt) {
        this.reactedAt = reactedAt;
    }

    public Post getPost() {
        return this.post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Reaction post(Post post) {
        this.setPost(post);
        return this;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Reaction user(User user) {
        this.setUser(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reaction)) {
            return false;
        }
        return getId() != null && getId().equals(((Reaction) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Reaction{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", reactedAt='" + getReactedAt() + "'" +
            "}";
    }
}
