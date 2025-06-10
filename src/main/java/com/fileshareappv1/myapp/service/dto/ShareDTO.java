package com.fileshareappv1.myapp.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.fileshareappv1.myapp.domain.Share} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ShareDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant createdAt;

    private PostDTO post;

    private UserDTO user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public PostDTO getPost() {
        return post;
    }

    public void setPost(PostDTO post) {
        this.post = post;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShareDTO)) {
            return false;
        }

        ShareDTO shareDTO = (ShareDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, shareDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ShareDTO{" +
            "id=" + getId() +
            ", createdAt='" + getCreatedAt() + "'" +
            ", post=" + getPost() +
            ", user=" + getUser() +
            "}";
    }
}
