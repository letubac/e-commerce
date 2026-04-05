package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * author: LeTuBac
 */
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private String status;   // TODO, IN_PROGRESS, DONE, CANCELLED
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    private Long assignedTo;
    private String assignedRole;
    private Long createdBy;
    private Date dueDate;
    private Date completedAt;
    private Date createdAt;
    private Date updatedAt;
    private String assignedUsername;
    private String createdByUsername;
}
