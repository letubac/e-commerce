package com.ecommerce.entity;

import lombok.Getter;
import lombok.Setter;
import vn.com.unit.miragesql.miragesql.annotation.PrimaryKey;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Date;

import com.ecommerce.constant.TableConstant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = TableConstant.FLASH_SALES)
/**
 * author: LeTuBac
 */
public class FlashSale {
	@Id
    @PrimaryKey(generationType = PrimaryKey.GenerationType.SEQUENCE, generator = TableConstant.SEQ
    + TableConstant.FLASH_SALES)
	@Column(name = "id")
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "start_time")
	private Date startTime;

	@Column(name = "end_time")
	private Date endTime;

	@Column(name = "is_active")
	private boolean isActive;

	@Column(name = "banner_image_url")
	private String bannerImageUrl;

	@Column(name = "background_color")
	private String backgroundColor;

	@Column(name = "created_at")
	private Date createdAt;

	@Column(name = "updated_at")
	private Date updatedAt;

	// Business methods
	public boolean isCurrentlyActive() {
		if (!isActive)
			return false;
		Date now = new Date();
		return now.after(startTime) && now.before(endTime);
	}

	public boolean isUpcoming() {
		return isActive && new Date().before(startTime);
	}

	public boolean isExpired() {
		return new Date().after(endTime);
	}

	public long getRemainingTimeInMinutes() {
		if (isExpired())
			return 0;

		Date target = isUpcoming() ? startTime : endTime;

		return Duration.between(new Date().toInstant(), target.toInstant()).toMinutes();
	}
}
