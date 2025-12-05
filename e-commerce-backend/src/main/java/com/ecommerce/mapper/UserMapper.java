package com.ecommerce.mapper;

import org.springframework.stereotype.Component;

import com.ecommerce.dto.UserDTO;
import com.ecommerce.entity.User;

@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setActive(user.isActive());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setEmailVerifiedAt(user.getEmailVerifiedAt());
        dto.setEmailVerificationToken(user.getEmailVerificationToken());
        dto.setPasswordResetToken(user.getPasswordResetToken());
        dto.setPasswordResetExpiresAt(user.getPasswordResetExpiresAt());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setRole(user.getRole());

        return dto;
    }

    public User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setAddress(dto.getAddress());
        user.setActive(dto.isActive());
        user.setEmailVerified(dto.isEmailVerified());
        user.setEmailVerifiedAt(dto.getEmailVerifiedAt());
        user.setEmailVerificationToken(dto.getEmailVerificationToken());
        user.setPasswordResetToken(dto.getPasswordResetToken());
        user.setPasswordResetExpiresAt(dto.getPasswordResetExpiresAt());
        user.setLastLoginAt(dto.getLastLoginAt());
        user.setCreatedAt(dto.getCreatedAt());
        user.setUpdatedAt(dto.getUpdatedAt());
        user.setRole(dto.getRole());

        return user;
    }
}