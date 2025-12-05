package com.ecommerce.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.entity.User;
import com.ecommerce.repository.base.DbRepository;

import vn.com.unit.springframework.data.mirage.repository.query.Modifying;

@Repository
public interface UserRepository extends DbRepository<User, Long> {

        // Basic CRUD operations
        // Maps to: userRepository_findAll.sql
        List<User> findAllData();

        // Maps to: userRepository_findById.sql
        Optional<User> findById(@Param("id") Long id);

        // Maps to: userRepository_findByUsername.sql
        Optional<User> findByUsername(@Param("username") String username);

        // Maps to: userRepository_findByEmail.sql
        Optional<User> findByEmail(@Param("email") String email);

        // Maps to: userRepository_findByUsernameOrEmail.sql
        Optional<User> findByUsernameOrEmail(@Param("username") String username, @Param("email") String email);

        // Maps to: userRepository_findActive.sql
        List<User> findActive();

        // Maps to: userRepository_existsByEmail.sql
        Boolean existsByEmail(@Param("email") String email);

        // Maps to: userRepository_existsByUsername.sql
        Boolean existsByUsername(@Param("username") String username);

        // Maps to: userRepository_existsById.sql
        Boolean existsById(@Param("id") Long id);

        // Maps to: userRepository_countAll.sql
        Long countAll();

        // Additional methods for dashboard statistics
        // count() is already provided by JpaRepository

        // Maps to: userRepository_countActiveUsers.sql
        Long countActiveUsers();

        // Maps to: userRepository_countActiveUsersSince.sql
        Long countActiveUsersSince(@Param("date") java.util.Date date);

        // Maps to: userRepository_countByCreatedAtAfter.sql
        Long countByCreatedAtAfter(@Param("date") java.util.Date date);

        // Insert/Update operations
        // Maps to: userRepository_insertUser.sql
        @Modifying
        Long insertUser(@Param("username") String username,
                        @Param("email") String email,
                        @Param("password") String password,
                        @Param("fullName") String fullName,
                        @Param("firstName") String firstName,
                        @Param("lastName") String lastName,
                        @Param("phoneNumber") String phoneNumber,
                        @Param("address") String address,
                        @Param("role") String role,
                        @Param("isActive") Boolean isActive,
                        @Param("emailVerified") Boolean emailVerified,
                        @Param("createdAt") Date createdAt,
                        @Param("updatedAt") Date updatedAt);

        // Maps to: userRepository_updateUser.sql
        @Modifying
        Integer updateUser(@Param("id") Long id,
                        @Param("username") String username,
                        @Param("email") String email,
                        @Param("fullName") String fullName,
                        @Param("firstName") String firstName,
                        @Param("lastName") String lastName,
                        @Param("phoneNumber") String phoneNumber,
                        @Param("address") String address,
                        @Param("isActive") Boolean isActive,
                        @Param("emailVerified") Boolean emailVerified,
                        @Param("updatedAt") Date updatedAt);

        // Maps to: userRepository_updatePassword.sql
        @Modifying
        Integer updatePassword(@Param("id") Long id,
                        @Param("password") String password,
                        @Param("updatedAt") Date updatedAt);

        // Maps to: userRepository_updateEmailVerified.sql
        @Modifying
        Integer updateEmailVerified(@Param("id") Long id,
                        @Param("emailVerified") Boolean emailVerified,
                        @Param("updatedAt") Date updatedAt);

        // Maps to: userRepository_softDelete.sql
        @Modifying
        Integer softDelete(@Param("id") Long id,
                        @Param("isActive") Boolean isActive,
                        @Param("updatedAt") Date updatedAt);

        // Maps to: userRepository_hardDelete.sql
        @Modifying
        Integer hardDelete(@Param("id") Long id);
}