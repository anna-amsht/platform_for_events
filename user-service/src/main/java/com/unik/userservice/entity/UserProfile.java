package com.unik.userservice.entity;

import com.unik.userservice.dto.request.UpdateMyProfileRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(schema = "users", name = "user_profiles")
public class UserProfile {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 160)
    private String company;

    @Column(name = "job_title", length = 160)
    private String jobTitle;

    @Column(length = 120)
    private String city;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(length = 1000)
    private String bio;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            schema = "users",
            name = "user_interests",
            joinColumns = @JoinColumn(name = "user_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "interest_id")
    )
    private Set<Interest> interests = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserProfile() {
    }

    public static UserProfile create(UUID id, String email) {
        UserProfile profile = new UserProfile();
        profile.id = id;
        profile.email = email;
        return profile;
    }

    public void syncEmail(String email) {
        this.email = email;
    }

    public void update(UpdateMyProfileRequest request, Set<Interest> interests) {
        this.firstName = normalize(request.firstName());
        this.lastName = normalize(request.lastName());
        this.company = normalize(request.company());
        this.jobTitle = normalize(request.jobTitle());
        this.city = normalize(request.city());
        this.avatarUrl = normalize(request.avatarUrl());
        this.bio = normalize(request.bio());
        this.interests.clear();
        this.interests.addAll(interests);
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCompany() {
        return company;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getCity() {
        return city;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public Set<Interest> getInterests() {
        return interests;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
