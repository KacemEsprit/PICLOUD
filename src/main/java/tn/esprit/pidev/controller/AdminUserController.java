package tn.esprit.pidev.controller;

import tn.esprit.pidev.dto.UserResponse;
import tn.esprit.pidev.dto.UserUpdateRequest;
import tn.esprit.pidev.dto.UserCreateRequest;
import tn.esprit.pidev.dto.UserSearchCriteria;
import tn.esprit.pidev.entity.RoleEnum;
import tn.esprit.pidev.entity.User;
import tn.esprit.pidev.exception.InvalidFileException;
import tn.esprit.pidev.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private static final Logger logger = Logger.getLogger(AdminUserController.class.getName());

    @Autowired
    private UserService userService;

    /**
     * GET /api/admin/users - Get all users with pagination and sorting
     */
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RoleEnum role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        logger.info("GET /api/admin/users - Fetching all users");
        UserSearchCriteria criteria = new UserSearchCriteria(keyword, role, page, size, sortBy, sortDir);
        Page<UserResponse> users = userService.getAllUsers(criteria);
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/admin/users/search - Search users
     */
    @GetMapping("/search")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RoleEnum role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        logger.info("GET /api/admin/users/search - Searching users");
        UserSearchCriteria criteria = new UserSearchCriteria(keyword, role, page, size, sortBy, sortDir);
        Page<UserResponse> users = userService.searchUsers(criteria);
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/admin/users/{id} - Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        logger.info("GET /api/admin/users/" + id);
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * POST /api/admin/users - Create new user
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        logger.info("POST /api/admin/users - Creating new user");
        UserResponse createdUser = userService.createUser(request);
        return ResponseEntity.status(201).body(createdUser);
    }

    /**
     * PUT /api/admin/users/{id} - Update user information (without photo)
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, 
                                                    @Valid @RequestBody UserUpdateRequest request) {
        logger.info("PUT /api/admin/users/" + id);
        UserResponse updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * DELETE /api/admin/users/{id} - Soft delete user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("DELETE /api/admin/users/" + id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/admin/users/{id}/role - Change user role
     */
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse> changeUserRole(@PathVariable Long id, @RequestParam RoleEnum role) {
        logger.info("PATCH /api/admin/users/" + id + "/role");
        UserResponse updatedUser = userService.changeUserRole(id, role);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * PATCH /api/admin/users/{id}/status - Change user status (enabled/disabled)
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        logger.info("PATCH /api/admin/users/" + id + "/status");
        Boolean enabled = request.get("enabled");
        if (enabled == null) {
            throw new InvalidFileException("Enabled field is required");
        }
        UserResponse updatedUser = userService.updateUserStatus(id, enabled);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * POST /api/admin/users/{id}/photo - Upload user photo
     */
    @PostMapping("/{id}/photo")
    public ResponseEntity<UserResponse> uploadUserPhoto(@PathVariable Long id, 
                                                        @RequestParam("file") MultipartFile file) {
        logger.info("POST /api/admin/users/" + id + "/photo");
        UserResponse response = userService.uploadUserPhoto(id, file);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/users/{id}/photo - Download user photo
     */
    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> getUserPhoto(@PathVariable Long id) {
        logger.info("GET /api/admin/users/" + id + "/photo");
        UserResponse userResponse = userService.getUserById(id);
        byte[] photoData = userService.getUserPhoto(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(userResponse.getPhotoContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"user_" + id + ".jpg\"")
                .body(photoData);
    }

    /**
     * DELETE /api/admin/users/{id}/photo - Delete user photo
     */
    @DeleteMapping("/{id}/photo")
    public ResponseEntity<UserResponse> deleteUserPhoto(@PathVariable Long id) {
        logger.info("DELETE /api/admin/users/" + id + "/photo");
        UserResponse response = userService.deleteUserPhoto(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/users/me - Get current logged-in user profile
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'OPERATOR', 'PASSENGER')")
    public ResponseEntity<UserResponse> getCurrentUser() {
        logger.info("GET /api/admin/users/me - Fetching current user");
        User user = userService.getCurrentUser();
        UserResponse response = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getCin(),
                user.getRole().toString(),
                user.getPhotoContentType(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.isEnabled()
        );
        return ResponseEntity.ok(response);
    }
}
