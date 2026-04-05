package com.ecommerce.service;

import com.ecommerce.dto.TaskDTO;
import com.ecommerce.exception.DetailException;

import java.util.List;
import java.util.Map;

/**
 * author: LeTuBac
 */
public interface TaskService {

    List<TaskDTO> getAllTasks() throws DetailException;

    TaskDTO getTaskById(Long id) throws DetailException;

    TaskDTO createTask(TaskDTO dto, Long createdBy) throws DetailException;

    TaskDTO updateTask(Long id, TaskDTO dto) throws DetailException;

    void deleteTask(Long id) throws DetailException;

    List<TaskDTO> getTasksByAssignedTo(Long userId) throws DetailException;

    List<TaskDTO> getTasksByStatus(String status) throws DetailException;

    List<TaskDTO> getTasksByRole(String role) throws DetailException;

    TaskDTO updateTaskStatus(Long id, String status) throws DetailException;

    Map<String, Object> getTaskStatistics() throws DetailException;
}
