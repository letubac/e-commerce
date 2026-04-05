package com.ecommerce.service.impl;

import com.ecommerce.dto.TaskDTO;
import com.ecommerce.entity.Task;
import com.ecommerce.exception.DetailException;
import com.ecommerce.repository.TaskRepository;
import com.ecommerce.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
/**
 * author: LeTuBac
 */
public class TaskServiceImpl implements TaskService {

    private static final String TASK_NOT_FOUND = "Task not found with id: ";
    private static final String STATUS_DONE = "DONE";

    private final TaskRepository taskRepository;

    @Override
    public List<TaskDTO> getAllTasks() throws DetailException {
        try {
            return taskRepository.findAllTasks().stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching all tasks", e);
            throw new DetailException("E_TASK_FETCH_FAILED");
        }
    }

    @Override
    public TaskDTO getTaskById(Long id) throws DetailException {
        try {
            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> new DetailException(TASK_NOT_FOUND + id));
            return mapToDTO(task);
        } catch (DetailException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching task {}", id, e);
            throw new DetailException("E_TASK_FETCH_FAILED");
        }
    }

    @Override
    public TaskDTO createTask(TaskDTO dto, Long createdBy) throws DetailException {
        try {
            Date now = new Date();
            Long taskId = taskRepository.saveData(
                    dto.getTitle(),
                    dto.getDescription(),
                    dto.getStatus() != null ? dto.getStatus() : "TODO",
                    dto.getPriority() != null ? dto.getPriority() : "MEDIUM",
                    dto.getAssignedTo(),
                    dto.getAssignedRole(),
                    createdBy,
                    dto.getDueDate(),
                    now,
                    now);

            log.info("Created task with id: {}", taskId);
            return getTaskById(taskId);
        } catch (DetailException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating task", e);
            throw new DetailException("E_TASK_CREATE_FAILED");
        }
    }

    @Override
    public TaskDTO updateTask(Long id, TaskDTO dto) throws DetailException {
        try {
            taskRepository.findById(id)
                    .orElseThrow(() -> new DetailException(TASK_NOT_FOUND + id));

            Date completedAt = STATUS_DONE.equals(dto.getStatus()) ? new Date() : dto.getCompletedAt();

            taskRepository.update(
                    id,
                    dto.getTitle(),
                    dto.getDescription(),
                    dto.getStatus(),
                    dto.getPriority(),
                    dto.getAssignedTo(),
                    dto.getAssignedRole(),
                    dto.getDueDate(),
                    completedAt,
                    new Date());

            log.info("Updated task {}", id);
            return getTaskById(id);
        } catch (DetailException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating task {}", id, e);
            throw new DetailException("E_TASK_UPDATE_FAILED");
        }
    }

    @Override
    public void deleteTask(Long id) throws DetailException {
        try {
            taskRepository.findById(id)
                    .orElseThrow(() -> new DetailException(TASK_NOT_FOUND + id));
            taskRepository.deleteById(id);
            log.info("Deleted task {}", id);
        } catch (DetailException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting task {}", id, e);
            throw new DetailException("E_TASK_DELETE_FAILED");
        }
    }

    @Override
    public List<TaskDTO> getTasksByAssignedTo(Long userId) throws DetailException {
        try {
            return taskRepository.findByAssignedTo(userId).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching tasks by assignedTo {}", userId, e);
            throw new DetailException("E_TASK_FETCH_FAILED");
        }
    }

    @Override
    public List<TaskDTO> getTasksByStatus(String status) throws DetailException {
        try {
            return taskRepository.findByStatus(status).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching tasks by status {}", status, e);
            throw new DetailException("E_TASK_FETCH_FAILED");
        }
    }

    @Override
    public List<TaskDTO> getTasksByRole(String role) throws DetailException {
        try {
            return taskRepository.findByAssignedRole(role).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching tasks by role {}", role, e);
            throw new DetailException("E_TASK_FETCH_FAILED");
        }
    }

    @Override
    public TaskDTO updateTaskStatus(Long id, String status) throws DetailException {
        try {
            Task existing = taskRepository.findById(id)
                    .orElseThrow(() -> new DetailException(TASK_NOT_FOUND + id));

            Date completedAt = STATUS_DONE.equals(status) ? new Date() : existing.getCompletedAt();

            taskRepository.update(
                    id,
                    existing.getTitle(),
                    existing.getDescription(),
                    status,
                    existing.getPriority(),
                    existing.getAssignedTo(),
                    existing.getAssignedRole(),
                    existing.getDueDate(),
                    completedAt,
                    new Date());

            log.info("Updated status of task {} to {}", id, status);
            return getTaskById(id);
        } catch (DetailException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating status for task {}", id, e);
            throw new DetailException("E_TASK_UPDATE_FAILED");
        }
    }

    @Override
    public Map<String, Object> getTaskStatistics() throws DetailException {
        try {
            List<Task> all = taskRepository.findAllTasks();
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", all.size());
            stats.put("todo", all.stream().filter(t -> "TODO".equals(t.getStatus())).count());
            stats.put("inProgress", all.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count());
            stats.put("done", all.stream().filter(t -> STATUS_DONE.equals(t.getStatus())).count());
            stats.put("cancelled", all.stream().filter(t -> "CANCELLED".equals(t.getStatus())).count());
            stats.put("highPriority", all.stream()
                    .filter(t -> "HIGH".equals(t.getPriority()) || "URGENT".equals(t.getPriority())).count());
            return stats;
        } catch (Exception e) {
            log.error("Error getting task statistics", e);
            throw new DetailException("E_TASK_FETCH_FAILED");
        }
    }

    private TaskDTO mapToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setAssignedTo(task.getAssignedTo());
        dto.setAssignedRole(task.getAssignedRole());
        dto.setCreatedBy(task.getCreatedBy());
        dto.setDueDate(task.getDueDate());
        dto.setCompletedAt(task.getCompletedAt());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        // Usernames are populated via SQL JOIN in the repository queries
        dto.setAssignedUsername(task.getAssignedUsername());
        dto.setCreatedByUsername(task.getCreatedByUsername());
        return dto;
    }
}
