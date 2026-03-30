package com.ecommerce.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ecommerce.dto.CreateUserRequest;
import com.ecommerce.dto.UserDTO;
import com.ecommerce.entity.User;
import com.ecommerce.exception.DetailException;

public interface UserService {

    // User management
    UserDTO createUser(CreateUserRequest request) throws DetailException;

    UserDTO updateUser(Long id, UserDTO userDTO) throws DetailException;

    Optional<UserDTO> getUserById(Long id) throws DetailException;

    UserDTO getUserByIdOrThrow(Long id) throws DetailException;

    Optional<UserDTO> getUserByUsername(String username) throws DetailException;

    Optional<UserDTO> getUserByEmail(String email) throws DetailException;

    List<UserDTO> getAllUsers() throws DetailException;

    void deleteUser(Long id) throws DetailException;

    void deactivateUser(Long id) throws DetailException;

    void activateUser(Long id) throws DetailException;

    // Admin operations
    Page<UserDTO> getAllUsersWithPagination(Pageable pageable) throws DetailException;

    Page<UserDTO> searchUsers(String keyword, Pageable pageable) throws DetailException;

    void lockUser(Long id) throws DetailException;

    void unlockUser(Long id) throws DetailException;

    Map<String, Object> getUserStatistics() throws DetailException;

    // Password management
    void changePassword(Long userId, String currentPassword, String newPassword) throws DetailException;

    void resetPassword(String email) throws DetailException;

    boolean validatePassword(String rawPassword, String hashedPassword) throws DetailException;

    // Email verification
    void sendVerificationEmail(Long userId) throws DetailException;

    void verifyEmail(String token) throws DetailException;

    // User validation
    boolean isUsernameAvailable(String username) throws DetailException;

    boolean isEmailAvailable(String email) throws DetailException;

    // Internal methods for authentication
    Optional<User> findUserByUsername(String username) throws DetailException;

    void updateLastLogin(Long userId) throws DetailException;

    void updateUserRole(Long id, String role) throws DetailException;
}