package com.ecommerce.entity;

import lombok.Getter;
import lombok.Setter;
import vn.com.unit.miragesql.miragesql.annotation.PrimaryKey;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.util.Date;

import com.ecommerce.constant.TableConstant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = TableConstant.TASKS)
/**
 * author: LeTuBac
 */
public class Task {

    @Id
    @PrimaryKey(generationType = PrimaryKey.GenerationType.SEQUENCE, generator = TableConstant.SEQ
            + TableConstant.TASKS)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private String status; // TODO, IN_PROGRESS, DONE, CANCELLED

    @Column(name = "priority")
    private String priority; // LOW, MEDIUM, HIGH, URGENT

    @Column(name = "assigned_to")
    private Long assignedTo;

    @Column(name = "assigned_role")
    private String assignedRole;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "due_date")
    private Date dueDate;

    @Column(name = "completed_at")
    private Date completedAt;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    // Mapped from SQL JOIN result (not a DB column on tasks table)
    @Column(name = "assigned_username")
    private String assignedUsername;

    @Column(name = "created_by_username")
    private String createdByUsername;
}
