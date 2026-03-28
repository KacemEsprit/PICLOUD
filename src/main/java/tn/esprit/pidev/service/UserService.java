package tn.esprit.pidev.service;

import tn.esprit.pidev.dto.UserResponse;
import tn.esprit.pidev.dto.UserUpdateRequest;
import tn.esprit.pidev.dto.UserCreateRequest;
import tn.esprit.pidev.dto.UserSearchCriteria;
import tn.esprit.pidev.entity.RoleEnum;
import tn.esprit.pidev.entity.User;
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

    // DELETE Operations
    void deleteUser(Long id);  // Soft delete

    // PHOTO Operations
    UserResponse uploadUserPhoto(Long userId, MultipartFile file);
    byte[] getUserPhoto(Long userId);
    UserResponse deleteUserPhoto(Long userId);

    // AUTHENTICATION
    User getCurrentUser();
}
