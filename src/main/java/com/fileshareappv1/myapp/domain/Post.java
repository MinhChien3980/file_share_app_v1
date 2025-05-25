package com.fileshareappv1.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fileshareappv1.myapp.domain.enumeration.Privacy;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * A Post.
 */
@Entity
@Table(name = "post")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "post")
@SuppressWarnings("common-java:DuplicatedBlocks")
@EntityListeners(AuditingEntityListener.class)
public class Post implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Lob
    @Column(name = "content", nullable = false)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String content;

    @CreatedDate
    @Column(name = "created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "location_name")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String locationName;

    @DecimalMax(value = "90")
    @Column(name = "location_lat", precision = 21, scale = 2)
    private BigDecimal locationLat;

    @DecimalMax(value = "180")
    @Column(name = "location_long", precision = 21, scale = 2)
    private BigDecimal locationLong;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "privacy", nullable = false)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Keyword)
    private Privacy privacy;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "view_count")
    private Long viewCount;

    @Column(name = "comment_count")
    private Long commentCount;

    @Column(name = "share_count")
    private Long shareCount;

    @Column(name = "reaction_count")
    private Long reactionCount;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "rel_post__tags", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "tags_id"))
    @JsonIgnoreProperties(value = { "posts" }, allowSetters = true)
    private Set<Tag> tags = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_files", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "file_name")
    private List<String> files = new ArrayList<>();

    private Integer numFiles = 0;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Post id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return this.content;
    }

    public Post content(String content) {
        this.setContent(content);
        return this;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Post createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public Post updatedAt(Instant updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLocationName() {
        return this.locationName;
    }

    public Post locationName(String locationName) {
        this.setLocationName(locationName);
        return this;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public BigDecimal getLocationLat() {
        return this.locationLat;
    }

    public Post locationLat(BigDecimal locationLat) {
        this.setLocationLat(locationLat);
        return this;
    }

    public void setLocationLat(BigDecimal locationLat) {
        this.locationLat = locationLat;
    }

    public BigDecimal getLocationLong() {
        return this.locationLong;
    }

    public Post locationLong(BigDecimal locationLong) {
        this.setLocationLong(locationLong);
        return this;
    }

    public void setLocationLong(BigDecimal locationLong) {
        this.locationLong = locationLong;
    }

    public Privacy getPrivacy() {
        return this.privacy;
    }

    public Post privacy(Privacy privacy) {
        this.setPrivacy(privacy);
        return this;
    }

    public void setPrivacy(Privacy privacy) {
        this.privacy = privacy;
    }

    public Instant getScheduledAt() {
        return this.scheduledAt;
    }

    public Post scheduledAt(Instant scheduledAt) {
        this.setScheduledAt(scheduledAt);
        return this;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Long getViewCount() {
        return this.viewCount;
    }

    public Post viewCount(Long viewCount) {
        this.setViewCount(viewCount);
        return this;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Long getCommentCount() {
        return this.commentCount;
    }

    public Post commentCount(Long commentCount) {
        this.setCommentCount(commentCount);
        return this;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public Long getShareCount() {
        return this.shareCount;
    }

    public Post shareCount(Long shareCount) {
        this.setShareCount(shareCount);
        return this;
    }

    public void setShareCount(Long shareCount) {
        this.shareCount = shareCount;
    }

    public Long getReactionCount() {
        return this.reactionCount;
    }

    public Post reactionCount(Long reactionCount) {
        this.setReactionCount(reactionCount);
        return this;
    }

    public void setReactionCount(Long reactionCount) {
        this.reactionCount = reactionCount;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Post user(User user) {
        this.setUser(user);
        return this;
    }

    public Set<Tag> getTags() {
        return this.tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public Post tags(Set<Tag> tags) {
        this.setTags(tags);
        return this;
    }

    public Post addTags(Tag tag) {
        this.tags.add(tag);
        return this;
    }

    public Post removeTags(Tag tag) {
        this.tags.remove(tag);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Post)) {
            return false;
        }
        return getId() != null && getId().equals(((Post) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Post{" +
            "id=" + getId() +
            ", content='" + getContent() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", locationName='" + getLocationName() + "'" +
            ", locationLat=" + getLocationLat() +
            ", locationLong=" + getLocationLong() +
            ", privacy='" + getPrivacy() + "'" +
            ", scheduledAt='" + getScheduledAt() + "'" +
            ", viewCount=" + getViewCount() +
            ", commentCount=" + getCommentCount() +
            ", shareCount=" + getShareCount() +
            ", reactionCount=" + getReactionCount() +
            "}";
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public Integer getNumFiles() {
        return numFiles;
    }

    public void setNumFiles(Integer numFiles) {
        this.numFiles = numFiles;
    }
}
