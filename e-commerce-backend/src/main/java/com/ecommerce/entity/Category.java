package com.ecommerce.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "slug")
	private String slug;

	@Column(name = "description")
	private String description;

	@Column(name = "image_url")
	private String imageUrl;

	@Column(name = "parent_id")
	private Long parentId;

	@Column(name = "sort_order")
	private Integer sortOrder;

	@Column(name = "is_active")
	private boolean isActive;

	@Column(name = "meta_title")
	private String metaTitle;

	@Column(name = "meta_description")
	private String metaDescription;

	@Column(name = "created_at")
	private Date createdAt;

	@Column(name = "updated_at")
	private Date updatedAt;

	// Business methods
	public boolean isRootCategory() {
		return parentId == null;
	}

	public String getFullPath() {
		return name; // In real scenario, would build full path like "Electronics > Smartphones"
	}
}

