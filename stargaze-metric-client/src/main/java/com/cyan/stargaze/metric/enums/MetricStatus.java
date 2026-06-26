package com.cyan.stargaze.metric.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 指标/维度生命周期状态。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum MetricStatus {
    /** 草稿(可改 DSL) */
    DRAFT("draft"),
    /** 已发布(口径冻结,不可改 DSL) */
    PUBLISHED("published"),
    /** 已下线 */
    OFFLINE("offline");

    @EnumValue
    private final String code;

    public static MetricStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (MetricStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }
}
