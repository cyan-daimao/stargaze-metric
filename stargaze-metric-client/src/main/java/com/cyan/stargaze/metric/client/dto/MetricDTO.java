package com.cyan.stargaze.metric.client.dto;

import com.cyan.stargaze.metric.enums.MeasureKind;
import com.cyan.stargaze.metric.enums.MetricStatus;
import com.cyan.stargaze.metric.enums.MetricType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 指标 DTO(对外契约 + 前端)。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MetricDTO {

    /** 主键 */
    private String id;

    /** 空间 ID */
    private String workspaceId;

    /** 指标名 */
    private String name;

    /** 业务名 */
    private String businessName;

    /** 指标类型 */
    private MetricType type;

    /** 聚合方式 */
    private MeasureKind measureKind;

    /** 指标 DSL */
    private String dsl;

    /** 口径说明 */
    private String caliber;

    /** 状态 */
    private MetricStatus status;

    /** 版本号 */
    private Integer version;

    /** 负责人 ID */
    private String ownerId;
}
