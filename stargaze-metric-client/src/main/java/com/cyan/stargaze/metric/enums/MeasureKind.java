package com.cyan.stargaze.metric.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 度量聚合方式。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum MeasureKind {
    SUM("sum"),
    AVG("avg"),
    COUNT("count"),
    DISTINCT_COUNT("distinct_count"),
    MAX("max"),
    MIN("min"),
    /** 表达式(派生指标,DSL 自定义) */
    EXPR("expr");

    @EnumValue
    private final String code;
}
