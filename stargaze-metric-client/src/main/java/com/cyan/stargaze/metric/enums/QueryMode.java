package com.cyan.stargaze.metric.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 来源查询能力模式。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum QueryMode {
    /** 可进入 OLAP 主链路 */
    OLAP("OLAP"),
    /** 仅点查/小批量补值 */
    POINT_LOOKUP("POINT_LOOKUP"),
    /** 仅作为过滤范围 */
    FILTER_ONLY("FILTER_ONLY");

    @EnumValue
    private final String code;

    @JsonValue
    public String toJsonValue() {
        String[] parts = code.split("_");
        StringBuilder sb = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            sb.append(parts[i].substring(0, 1));
            sb.append(parts[i].substring(1).toLowerCase());
        }
        return sb.toString();
    }

    @JsonCreator
    public static QueryMode fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (QueryMode value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }
}
