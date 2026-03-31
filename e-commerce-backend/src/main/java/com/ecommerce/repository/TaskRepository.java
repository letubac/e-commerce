package com.ecommerce.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.entity.Task;
import com.ecommerce.repository.base.DbRepository;

import vn.com.unit.springframework.data.mirage.repository.query.Modifying;

@Repository
public interface TaskRepository extends DbRepository<Task, Long> {

        // Maps to: TaskRepository_findAllTasks.sql
        List<Task> findAllTasks();

        // Maps to: TaskRepository_findById.sql
        Optional<Task> findById(@Param("id") Long id);

        // Maps to: TaskRepository_saveData.sql
        @Modifying
        Long saveData(
                        @Param("title") String title,
                        @Param("description") String description,
                        @Param("status") String status,
                        @Param("priority") String priority,
                        @Param("assignedTo") Long assignedTo,
                        @Param("assignedRole") String assignedRole,
                        @Param("createdBy") Long createdBy,
                        @Param("dueDate") Date dueDate,
                        @Param("createdAt") Date createdAt,
                        @Param("updatedAt") Date updatedAt);

        // Maps to: TaskRepository_update.sql
        @Modifying
        void update(
                        @Param("id") Long id,
                        @Param("title") String title,
                        @Param("description") String description,
                        @Param("status") String status,
                        @Param("priority") String priority,
                        @Param("assignedTo") Long assignedTo,
                        @Param("assignedRole") String assignedRole,
                        @Param("dueDate") Date dueDate,
                        @Param("completedAt") Date completedAt,
                        @Param("updatedAt") Date updatedAt);

        // Maps to: TaskRepository_deleteById.sql
        @Modifying
        void deleteById(@Param("id") Long id);

        // Maps to: TaskRepository_findByAssignedTo.sql
        List<Task> findByAssignedTo(@Param("assignedTo") Long assignedTo);

        // Maps to: TaskRepository_findByStatus.sql
        List<Task> findByStatus(@Param("status") String status);

        // Maps to: TaskRepository_findByAssignedRole.sql
        List<Task> findByAssignedRole(@Param("assignedRole") String assignedRole);
}
