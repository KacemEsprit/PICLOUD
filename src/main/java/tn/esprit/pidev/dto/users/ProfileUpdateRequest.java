package tn.esprit.pidev.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for user profile updates
 * Non-admin users can only update: email, name, and cin
 * They cannot change: username, role, or enabled status
 */
public class ProfileUpdateRequest {
    @JsonProperty("email")
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @JsonProperty("name")
    @NotBlank(message = "Name is required")
    private String name;

    @JsonProperty("cin")
    private Long cin;  // Optional field

    // Constructors
    public ProfileUpdateRequest() {
    }

    public ProfileUpdateRequest(String email, String name, Long cin) {
        this.email = email;
        this.name = name;
        this.cin = cin;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCin() {
        return cin;
    }

    public void setCin(Long cin) {
        this.cin = cin;
    }
}


