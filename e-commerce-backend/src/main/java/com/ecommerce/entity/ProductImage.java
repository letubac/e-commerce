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
@Table(name = TableConstant.PRODUCT_IMAGES)
/**
 * author: LeTuBac
 */
public class ProductImage {
    @Id
    @PrimaryKey(generationType = PrimaryKey.GenerationType.SEQUENCE, generator = TableConstant.SEQ
    + TableConstant.PRODUCT_IMAGES)
    @Column(name = "id")
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "alt_text")
    private String altText;

    @Column(name = "is_primary")
    private boolean isPrimary;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "created_at")
    private Date createdAt;

    // Business methods
    public boolean isPrimaryImage() {
        return isPrimary;
    }
}
