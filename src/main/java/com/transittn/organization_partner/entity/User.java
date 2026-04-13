package com.transittn.organization_partner.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleEnum role;

    @Column(unique = true)
    private Long cin;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_token_expiry")
    private LocalDateTime passwordResetTokenExpiry;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] photo;

    @Column(name = "photo_content_type")
    private String photoContentType;

    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public User(String username, String password, String email, String name, RoleEnum role) {
        this();
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; this.updatedAt = LocalDateTime.now(); }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; this.updatedAt = LocalDateTime.now(); }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; this.updatedAt = LocalDateTime.now(); }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; this.updatedAt = LocalDateTime.now(); }
    public RoleEnum getRole() { return role; }
    public void setRole(RoleEnum role) { this.role = role; this.updatedAt = LocalDateTime.now(); }
    public Long getCin() { return cin; }
    public void setCin(Long cin) { this.cin = cin; this.updatedAt = LocalDateTime.now(); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; this.updatedAt = LocalDateTime.now(); }
    public String getPasswordResetToken() { return passwordResetToken; }
    public void setPasswordResetToken(String t) { this.passwordResetToken = t; this.updatedAt = LocalDateTime.now(); }
    public LocalDateTime getPasswordResetTokenExpiry() { return passwordResetTokenExpiry; }
    public void setPasswordResetTokenExpiry(LocalDateTime t) { this.passwordResetTokenExpiry = t; this.updatedAt = LocalDateTime.now(); }
    public byte[] getPhoto() { return photo; }
    public void setPhoto(byte[] photo) { this.photo = photo; this.updatedAt = LocalDateTime.now(); }
    public String getPhotoContentType() { return photoContentType; }
    public void setPhotoContentType(String t) { this.photoContentType = t; this.updatedAt = LocalDateTime.now(); }
}
