package com.ecommerce.service.impl;

import com.ecommerce.dto.CreateUserRequest;
import com.ecommerce.dto.UserDTO;
import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.UserService;
import com.ecommerce.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional("transactionManagerSql")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDTO createUser(CreateUserRequest request) {
        // Validate username availability
        if (!isUsernameAvailable(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        // Validate email availability
        if (!isEmailAvailable(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        Date now = new Date();

        Long result = userRepository.insertUser(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFirstName() + " " + request.getLastName(), // fullName,
                request.getFirstName(),
                request.getLastName(),
                request.getPhoneNumber(),
                request.getAddress(),
                "ROLE_CUSTOMER", // role
                true, // isActive
                false, // emailVerified
                now, // createdAt
                now // updatedAt
        );

        if (result == null || result <= 0) {
            throw new RuntimeException("Failed to create user");
        }

        // Retrieve the created user
        Optional<User> createdUser = userRepository.findByUsername(request.getUsername());
        if (createdUser.isEmpty()) {
            throw new RuntimeException("Failed to retrieve created user");
        }

        return userMapper.toDTO(createdUser.get());
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isEmpty()) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }

        Date now = new Date();

        Integer result = userRepository.updateUser(
                id,
                existingUser.get().getUsername(), // Keep existing username
                existingUser.get().getEmail(), // Keep existing email
                userDTO.getFullName(),
                userDTO.getFirstName(),
                userDTO.getLastName(),
                userDTO.getPhoneNumber(),
                userDTO.getAddress(),
                existingUser.get().isActive(), // Keep existing active status
                existingUser.get().isEmailVerified(), // Keep existing verification status
                now);

        if (result == null || result <= 0) {
            throw new RuntimeException("Failed to update user");
        }

        // Retrieve the updated user
        Optional<User> updatedUser = userRepository.findById(id);
        return userMapper.toDTO(updatedUser.get());
    }

    @Override
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDTO);
    }

    @Override
    public UserDTO getUserByIdOrThrow(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return userMapper.toDTO(user);
    }

    @Override
    public Optional<UserDTO> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toDTO);
    }

    @Override
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toDTO);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAllData().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.hardDelete(id);
    }

    @Override
    public void deactivateUser(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }

        Date now = new Date();
        userRepository.softDelete(id, false, now);
    }

    @Override
    public void activateUser(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }

        Date now = new Date();
        userRepository.softDelete(id, true, now);
    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        Date now = new Date();
        userRepository.updatePassword(userId, passwordEncoder.encode(newPassword), now);
    }

    @Override
    public void resetPassword(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new EntityNotFoundException("User not found with email: " + email);
        }

        // TODO: Implement password reset logic (send email, generate reset token, etc.)
        // For now, this is just a placeholder
    }

    @Override
    public boolean validatePassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    @Override
    public void sendVerificationEmail(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }

        // TODO: Implement email verification logic
        // For now, this is just a placeholder
    }

    @Override
    public void verifyEmail(String token) {
        // TODO: Implement email verification logic
        // For now, this is just a placeholder
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void updateLastLogin(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }

        // For now, we'll just update the updatedAt timestamp
        // If you need a specific lastLogin field, add it to the User entity and
        // repository
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
    }
}