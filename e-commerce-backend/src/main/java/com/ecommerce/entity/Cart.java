package com.ecommerce.entity;

import java.util.Date;

import com.ecommerce.constant.TableConstant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.unit.miragesql.miragesql.annotation.PrimaryKey;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = TableConstant.CARTS)
/**
 * author: LeTuBac
 */
public class Cart {
    @Id
    @PrimaryKey(generationType = PrimaryKey.GenerationType.SEQUENCE, generator = TableConstant.SEQ
    + TableConstant.CARTS)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "session_id")
    private String sessionId; // For guest users

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    // Business methods
    public boolean isGuestCart() {
        return userId == null && sessionId != null;
    }

    public boolean isUserCart() {
        return userId != null;
    }
}
