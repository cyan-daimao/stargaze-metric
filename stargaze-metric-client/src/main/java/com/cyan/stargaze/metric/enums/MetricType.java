package com.cyan.stargaze.metric.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 指标类型。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum MetricType {
    /** 原子指标 */
    ATOMIC("atomic"),
    /** 派生指标 */
    DERIVED("derived"),
    /** 窗口指标 */
    WINDOW("window");

    @EnumValue
    private final String code;
}
