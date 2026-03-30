package com.ecommerce.controller;

import com.ecommerce.dto.TaskDTO;
import com.ecommerce.exception.ErrorHandler;
import com.ecommerce.exception.SuccessHandler;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.TaskService;
import com.ecommerce.webapp.BusinessApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for task management (Admin only).
 */
@RestController
@RequestMapping("/api/v1/admin/tasks")
@Slf4j
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private SuccessHandler successHandler;

    @GetMapping("/statistics")
    public ResponseEntity<BusinessApiResponse> getStatistics() {
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> stats = taskService.getTaskStatistics();
            return ResponseEntity.ok(successHandler.handlerSuccess(stats, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<BusinessApiResponse> getMyTasks(Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            List<TaskDTO> tasks = taskService.getTasksByAssignedTo(principal.getId());
            return ResponseEntity.ok(successHandler.handlerSuccess(tasks, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    @GetMapping("/")
    public ResponseEntity<BusinessApiResponse> getAllTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long assignedTo,
            @RequestParam(required = false) String role) {
        long start = System.currentTimeMillis();
        try {
            List<TaskDTO> tasks;
            if (status != null && !status.isBlank()) {
                tasks = taskService.getTasksByStatus(status);
            } else if (assignedTo != null) {
                tasks = taskService.getTasksByAssignedTo(assignedTo);
            } else if (role != null && !role.isBlank()) {
                tasks = taskService.getTasksByRole(role);
            } else {
                tasks = taskService.getAllTasks();
            }
            return ResponseEntity.ok(successHandler.handlerSuccess(tasks, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessApiResponse> getTaskById(@PathVariable Long id) {
        long start = System.currentTimeMillis();
        try {
            TaskDTO task = taskService.getTaskById(id);
            return ResponseEntity.ok(successHandler.handlerSuccess(task, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    @PostMapping("/")
    public ResponseEntity<BusinessApiResponse> createTask(
            @RequestBody TaskDTO dto,
            Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            TaskDTO created = taskService.createTask(dto, principal.getId());
            return ResponseEntity.ok(successHandler.handlerSuccess(created, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusinessApiResponse> updateTask(
            @PathVariable Long id,
            @RequestBody TaskDTO dto) {
        long start = System.currentTimeMillis();
        try {
            TaskDTO updated = taskService.updateTask(id, dto);
            return ResponseEntity.ok(successHandler.handlerSuccess(updated, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BusinessApiResponse> deleteTask(@PathVariable Long id) {
        long start = System.currentTimeMillis();
        try {
            taskService.deleteTask(id);
            return ResponseEntity.ok(successHandler.handlerSuccess(
                    Map.of("message", "Task deleted successfully"), start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<BusinessApiResponse> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        long start = System.currentTimeMillis();
        try {
            String status = body.get("status");
            TaskDTO updated = taskService.updateTaskStatus(id, status);
            return ResponseEntity.ok(successHandler.handlerSuccess(updated, start));
        } catch (Exception e) {
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }
}
