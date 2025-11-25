package com.ecommerce.service;

import com.ecommerce.dto.CreateUserRequest;
import com.ecommerce.dto.UserDTO;
import com.ecommerce.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {

    // User management
    UserDTO createUser(CreateUserRequest request);

    UserDTO updateUser(Long id, UserDTO userDTO);

    Optional<UserDTO> getUserById(Long id);

    UserDTO getUserByIdOrThrow(Long id);

    Optional<UserDTO> getUserByUsername(String username);

    Optional<UserDTO> getUserByEmail(String email);

    List<UserDTO> getAllUsers();

    void deleteUser(Long id);

    void deactivateUser(Long id);

    void activateUser(Long id);

    // Password management
    void changePassword(Long userId, String currentPassword, String newPassword);

    void resetPassword(String email);

    boolean validatePassword(String rawPassword, String hashedPassword);

    // Email verification
    void sendVerificationEmail(Long userId);

    void verifyEmail(String token);

    // User validation
    boolean isUsernameAvailable(String username);

    boolean isEmailAvailable(String email);

    // Internal methods for authentication
    Optional<User> findUserByUsername(String username);

    void updateLastLogin(Long userId);
}