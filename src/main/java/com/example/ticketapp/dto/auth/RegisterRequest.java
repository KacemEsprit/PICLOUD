package com.example.ticketapp.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.ticketapp.entity.RoleEnum;

public class RegisterRequest {

    private String username;
    private String password;
    private String email;
    private String name;
    private RoleEnum role;
    @JsonProperty("CIN")
    private Long CIN;
    private String photo;
    private String phone;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
    }

    public Long getCIN() {
        return CIN;
    }

    public void setCIN(Long CIN) {
        this.CIN = CIN;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

