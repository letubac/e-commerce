package com.ecommerce.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.constant.UserConstant;
import com.ecommerce.dto.CreateUserRequest;
import com.ecommerce.dto.UserDTO;
import com.ecommerce.entity.User;
import com.ecommerce.exception.DetailException;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional("transactionManagerSql")
@Slf4j
/**
 * author: LeTuBac
 */
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDTO createUser(CreateUserRequest request) throws DetailException {
        try {
            // Validate username availability
            if (!isUsernameAvailable(request.getUsername())) {
                throw new DetailException(UserConstant.USERNAME_ALREADY_EXISTS);
            }

            // Validate email availability
            if (!isEmailAvailable(request.getEmail())) {
                throw new DetailException(UserConstant.EMAIL_ALREADY_EXISTS);
            }

            Date now = new Date();

            Long result = userRepository.insertUser(
                    request.getUsername(),
                    request.getEmail(),
                    passwordEncoder.encode(request.getPassword()),
                    request.getFirstName() + " " + request.getLastName(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPhoneNumber(),
                    request.getAddress(),
                    "CUSTOMER",
                    true,
                    true,
                    now,
                    now);

            if (result == null || result <= 0) {
                throw new DetailException(UserConstant.USER_CREATE_FAILED);
            }

            // Retrieve the created user
            Optional<User> createdUser = userRepository.findByUsername(request.getUsername());
            if (createdUser.isEmpty()) {
                throw new DetailException(UserConstant.USER_CREATE_FAILED);
            }

            return userMapper.toDTO(createdUser.get());
        } catch (DetailException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            throw new DetailException(UserConstant.USER_CREATE_FAILED);
        }
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) throws DetailException {
        try {
            Optional<User> existingUser = userRepository.findById(id);
            if (existingUser.isEmpty()) {
                throw new DetailException(UserConstant.USER_NOT_FOUND);
            }

            Date now = new Date();

            Integer result = userRepository.updateUser(
                    id,
                    existingUser.get().getUsername(),
                    existingUser.get().getEmail(),
                    userDTO.getFullName(),
                    userDTO.getFirstName(),
                    userDTO.getLastName(),
                    userDTO.getPhoneNumber(),
                    userDTO.getAddress(),
                    existingUser.get().isActive(),
                    existingUser.get().isEmailVerified(),
                    now);

            if (result == null || result <= 0) {
                throw new DetailException(UserConstant.USER_UPDATE_FAILED);
            }

            // Retrieve the updated user
            Optional<User> updatedUser = userRepository.findById(id);
            return userMapper.toDTO(updatedUser.get());
        } catch (DetailException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating user {}: {}", id, e.getMessage(), e);
            throw new DetailException(UserConstant.USER_UPDATE_FAILED);
        }
    }

    @Override
    public Optional<UserDTO> getUserById(Long id) throws DetailException {
        try {
            return userRepository.findById(id)
                    .map(userMapper::toDTO);
        } catch (Exception e) {
            log.error("Error fetching user {}: {}", id, e.getMessage(), e);
            throw new DetailException(UserConstant.USER_FETCH_FAILED);
        }
    }

    @Override
    public UserDTO getUserByIdOrThrow(Long id) throws DetailException {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new DetailException(UserConstant.USER_NOT_FOUND));
            return userMapper.toDTO(user);
        } catch (DetailException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching user {}: {}", id, e.getMessage(), e);
            throw new DetailException(UserConstant.USER_FETCH_FAILED);
        }
    }

    @Override
    public Optional<UserDTO> getUserByUsername(String username) throws DetailException {
        try {
            return userRepository.findByUsername(username)
                    .map(userMapper::toDTO);
        } catch (Exception e) {
            log.error("Error fetching user by username {}: {}", username, e.getMessage(), e);
            throw new DetailException(UserConstant.USER_FETCH_FAILED);
        }
    }

    @Override
    public Optional<UserDTO> getUserByEmail(String email) throws DetailException {
        try {
            return userRepository.findByEmail(email)
                    .map(userMapper::toDTO);
        } catch (Exception e) {
            log.error("Error fetching user by email {}: {}", email, e.getMessage(), e);
            throw new DetailException(UserConstant.USER_FETCH_FAILED);
        }
    }

    @Override
    public List<UserDTO> getAllUsers() throws DetailException {
        try {
            return userRepository.findAllData().stream()
                    .map(userMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching all users: {}", e.getMessage(), e);
            throw new DetailException(UserConstant.USER_FETCH_FAILED);
        }
    }

    @Override
    public void deleteUser(Long id) throws DetailException {
        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isEmpty()) {
                throw new DetailException(UserConstant.USER_NOT_FOUND);
            }
            userRepository.hardDelete(id);
        } catch (DetailException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting user {}: {}", id, e.getMessage(), e);
            throw new DetailException(UserConstant.USER_DELETE_FAILED);
        }
    }

    @Override
    public void deactivateUser(Long id) throws DetailException {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                throw new DetailException(UserConstant.USER_NOT_FOUND);
            }

            Date now = new Date();
            userRepository.softDelete(id, false, now);
        } catch (DetailException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deactivating user {}: {}", id, e.getMessage(), e);
            throw new DetailException(UserConstant.ACCOUNT_STATUS_UPDATE_FAILED);
        }
    }

    @Override
    public void activateUser(Long id) throws DetailException {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                throw new DetailException(UserConstant.USER_NOT_FOUND);
            }

            Date now = new Date();
            userRepository.softDelete(id, true, now);
        } catch (DetailException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error activating user {}: {}", id, e.getMessage(), e);
            throw new DetailException(UserConstant.ACCOUNT_STATUS_UPDATE_FAILED);
        }
    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) throws DetailException {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                throw new DetailException(UserConstant.USER_NOT_FOUND);
            }

            User user = userOpt.get();
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new DetailException(UserConstant.PASSWORD_MISMATCH);
            }

            Date now = new Date();
            userRepository.updatePassword(userId, passwordEncoder.encode(newPassword), now);
        } catch (DetailException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error changing password for user {}: {}", userId, e.getMessage(), e);
            throw new DetailException(UserConstant.PASSWORD_CHANGE_FAILED);
        }
    }

    @Override
    public void resetPassword(String email) throws DetailException {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                throw new DetailException(UserConstant.USER_NOT_FOUND);
            }

            // TODO: Implement password reset logic (send email, generate reset token, etc.)
            // For now, this is just a placeholder
        } catch (DetailException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error resetting password for email {}: {}", email, e.getMessage(), e);
            throw new DetailException(UserConstant.PASSWORD_RESET_FAILED);
        }
    }

    @Override
    public boolean validatePassword(String rawPassword, String hashedPassword) throws DetailException {
        try {
            return passwordEncoder.matches(rawPassword, hashedPassword);
        } catch (Exception e) {
            log.error("Error validating password: {}", e.getMessage(), e);
            throw new DetailException(UserConstant.PASSWORD_INVALID);
        }
    }

    @Override
    public void sendVerificationEmail(Long userId) throws DetailException {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                throw new DetailException(UserConstant.USER_NOT_FOUND);
            }

            // TODO: Implement email verification logic
            // For now, this is just a placeholder
        } catch (DetailException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error sending verification email for user {}: {}", userId, e.getMessage(), e);
            throw new DetailException(UserConstant.EMAIL_SEND_FAILED);
        }
    }

    @Override
    public void verifyEmail(String token) throws DetailException {
        try {
            // TODO: Implement email verification logic
            // For now, this is just a placeholder
        } catch (Exception e) {
            log.error("Error verifying email with token: {}", e.getMessage(), e);
            throw new DetailException(UserConstant.EMAIL_VERIFICATION_FAILED);
        }
    }

    @Override
    public boolean isUsernameAvailable(String username) throws DetailException {
        try {
            return !userRepository.existsByUsername(username);
        } catch (Exception e) {
            log.error("Error checking username availability: {}", e.getMessage(), e);
            throw new DetailException(UserConstant.USER_FETCH_FAILED);
        }
    }

    @Override
    public boolean isEmailAvailable(String email) throws DetailException {
        try {
            return !userRepository.existsByEmail(email);
        } catch (Exception e) {
            log.error("Error checking email availability: {}", e.getMessage(), e);
            throw new DetailException(UserConstant.USER_FETCH_FAILED);
        }
    }

    @Override
    public Optional<User> findUserByUsername(String username) throws DetailException {
        try {
            return userRepository.findByUsername(username);
        } catch (Exception e) {
            log.error("Error finding user by username {}: {}", username, e.getMessage(), e);
            throw new DetailException(UserConstant.USER_FETCH_FAILED);
        }
    }

    @Override
    public void updateLastLogin(Long userId) throws DetailException {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                throw new DetailException(UserConstant.USER_NOT_FOUND);
            }

            User user = userOpt.get();
            Date now = new Date();

            userRepository.updateUser(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getPhoneNumber(),
                    user.getAddress(),
                    user.isActive(),
                    user.isEmailVerified(),
                    now);
        } catch (DetailException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating last login for user {}: {}", userId, e.getMessage(), e);
            throw new DetailException(UserConstant.USER_UPDATE_FAILED);
        }
    }

    // Admin operations implementation
    @Override
    public Page<UserDTO> getAllUsersWithPagination(Pageable pageable) throws DetailException {
        try {
            List<User> allUsers = userRepository.findAllData();

            List<UserDTO> userDTOs = allUsers.stream()
                    .map(userMapper::toDTO)
                    .collect(Collectors.toList());

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), userDTOs.size());

            List<UserDTO> pageContent = userDTOs.subList(start, end);

            return new org.springframework.data.domain.PageImpl<>(
                    pageContent,
                    pageable,
                    userDTOs.size());
        } catch (Exception e) {
            log.error("Error getting paginated users: {}", e.getMessage(), e);
            throw new DetailException(UserConstant.USER_PAGINATION_FAILED);
        }
    }

    @Override
    public Page<UserDTO> searchUsers(String keyword, Pageable pageable) throws DetailException {
        try {
            List<User> allUsers = userRepository.findAllData();

            List<UserDTO> filteredUsers = allUsers.stream()
                    .filter(user -> {
                        if (keyword == null || keyword.trim().isEmpty()) {
                            return true;
                        }
                        String lowerKeyword = keyword.toLowerCase();
                        return (user.getUsername() != null && user.getUsername().toLowerCase().contains(lowerKeyword))
                                ||
                                (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerKeyword)) ||
                                (user.getFullName() != null && user.getFullName().toLowerCase().contains(lowerKeyword))
                                ||
                                (user.getPhoneNumber() != null && user.getPhoneNumber().contains(lowerKeyword));
                    })
                    .map(userMapper::toDTO)
                    .collect(Collectors.toList());

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredUsers.size());

            List<UserDTO> pageContent = filteredUsers.subList(start, end);

            return new org.springframework.data.domain.PageImpl<>(
                    pageContent,
                    pageable,
                    filteredUsers.size());
        } catch (Exception e) {
            log.error("Error searching users: {}", e.getMessage(), e);
            throw new DetailException(UserConstant.USER_SEARCH_FAILED);
        }
    }

    @Override
    @Transactional
    public void lockUser(Long id) throws DetailException {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                throw new DetailException(UserConstant.USER_NOT_FOUND);
            }

            Date now = new Date();
            userRepository.softDelete(id, false, now);
        } catch (DetailException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error locking user {}: {}", id, e.getMessage(), e);
            throw new DetailException(UserConstant.ACCOUNT_LOCK_FAILED);
        }
    }

    @Override
    @Transactional
    public void unlockUser(Long id) throws DetailException {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                throw new DetailException(UserConstant.USER_NOT_FOUND);
            }

            Date now = new Date();
            userRepository.softDelete(id, true, now);
        } catch (DetailException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error unlocking user {}: {}", id, e.getMessage(), e);
            throw new DetailException(UserConstant.ACCOUNT_UNLOCK_FAILED);
        }
    }

    @Override
    public Map<String, Object> getUserStatistics() throws DetailException {
        try {
            Map<String, Object> statistics = new HashMap<>();

            Long totalUsers = userRepository.countAll();
            statistics.put("totalUsers", totalUsers);

            Long activeUsers = userRepository.countActiveUsers();
            statistics.put("activeUsers", activeUsers);

            statistics.put("inactiveUsers", totalUsers - activeUsers);

            Date thirtyDaysAgo = new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000);
            Long newUsersThisMonth = userRepository.countActiveUsersSince(thirtyDaysAgo);
            statistics.put("newUsersThisMonth", newUsersThisMonth);

            return statistics;
        } catch (Exception e) {
            log.error("Error getting user statistics: {}", e.getMessage(), e);
            throw new DetailException(UserConstant.USER_STATISTICS_FAILED);
        }
    }

    @Override
    public void updateUserRole(Long id, String role) throws DetailException {
        try {
            if (!userRepository.existsById(id)) {
                throw new DetailException(UserConstant.USER_NOT_FOUND);
            }
            userRepository.updateRole(id, role, new Date());
            log.info("Updated role for user {} to {}", id, role);
        } catch (DetailException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating role for user {}: {}", id, e.getMessage(), e);
            throw new DetailException(UserConstant.USER_UPDATE_FAILED);
        }
    }
}