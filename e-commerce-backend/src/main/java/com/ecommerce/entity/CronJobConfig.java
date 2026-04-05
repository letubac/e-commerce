package com.ecommerce.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import vn.com.unit.miragesql.miragesql.annotation.PrimaryKey;
import jakarta.persistence.*;
import java.util.Date;

import com.ecommerce.constant.TableConstant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = TableConstant.CRON_JOB_CONFIGS)
/**
 * author: LeTuBac
 */
public class CronJobConfig {

    @Id
    @PrimaryKey(generationType = PrimaryKey.GenerationType.SEQUENCE, generator = TableConstant.SEQ
            + TableConstant.CRON_JOB_CONFIGS)
    @Column(name = "id")
    private Long id;

    @Column(name = "job_name")
    private String jobName;

    @Column(name = "enabled")
    private boolean enabled = true;

    @Column(name = "updated_at")
    private Date updatedAt;
}
