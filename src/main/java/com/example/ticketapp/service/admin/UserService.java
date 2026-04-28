package com.example.ticketapp.service.admin;

import com.example.ticketapp.dto.users.UserResponse;
import com.example.ticketapp.dto.admin.UserUpdateRequest;
import com.example.ticketapp.dto.admin.UserCreateRequest;
import com.example.ticketapp.dto.admin.UserSearchCriteria;
import com.example.ticketapp.entity.RoleEnum;
import com.example.ticketapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    // READ Operations
    Page<UserResponse> getAllUsers(UserSearchCriteria criteria);
    UserResponse getUserById(Long id);
    Page<UserResponse> searchUsers(UserSearchCriteria criteria);

    // CREATE Operations
    UserResponse createUser(UserCreateRequest request);

    // UPDATE Operations
    UserResponse updateUser(Long id, UserUpdateRequest request);
    UserResponse changeUserRole(Long id, RoleEnum role);
    UserResponse updateUserStatus(Long id, boolean enabled);

    // DELETE Operations
    void deleteUser(Long id);  // Soft delete

    // PHOTO Operations
    UserResponse uploadUserPhoto(Long userId, MultipartFile file);
    byte[] getUserPhoto(Long userId);
    UserResponse deleteUserPhoto(Long userId);

    // AUTHENTICATION
    User getCurrentUser();
}

