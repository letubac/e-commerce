package com.ecommerce.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.entity.CronJobConfig;
import com.ecommerce.repository.base.DbRepository;

import vn.com.unit.springframework.data.mirage.repository.query.Modifying;

@Repository
/**
 * author: LeTuBac
 */
public interface CronJobConfigRepository extends DbRepository<CronJobConfig, Long> {

    // Maps to: CronJobConfigRepository_findAll.sql
    List<CronJobConfig> findAll();

    // Maps to: CronJobConfigRepository_findByJobName.sql
    Optional<CronJobConfig> findByJobName(@Param("jobName") String jobName);

    // Maps to: CronJobConfigRepository_upsert.sql
    @Modifying
    void upsert(
            @Param("jobName") String jobName,
            @Param("enabled") boolean enabled,
            @Param("updatedAt") Date updatedAt);
}
