package com.cyan.stargaze.metric.domain.metric;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 指标版本(口径变更审计)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricVersion {

    /** 主键 */
    private String id;

    /** 指标 ID */
    private String metricId;

    /** 版本号 */
    private Integer version;

    /** DSL 快照 */
    private String dsl;

    /** 口径说明快照 */
    private String caliber;

    /** 变更日志 */
    private String changeLog;

    /** 创建人 */
    private String createdBy;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 逻辑删除时间 */
    private LocalDateTime deletedAt;
}
