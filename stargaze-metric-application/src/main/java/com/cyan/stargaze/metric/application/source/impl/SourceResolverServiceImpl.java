package com.cyan.stargaze.metric.application.source.impl;

import com.cyan.stargaze.dataset.client.DatasetClient;
import com.cyan.stargaze.metric.application.source.SourceResolverService;
import com.cyan.stargaze.metric.client.dto.SourceResolveDTO;
import com.cyan.stargaze.metric.enums.MetricSourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 来源解析应用服务实现。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class SourceResolverServiceImpl implements SourceResolverService {

    private final DatasetClient datasetClient;

    @Override
    public SourceResolveDTO resolveDataset(String datasetCode) {
        var resp = datasetClient.exists(datasetCode);
        boolean exists = resp != null && resp.getCode() == 200 && Boolean.TRUE.equals(resp.getData());
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("datasetCode", datasetCode);
        details.put("tableName", "dwd_" + datasetCode.toLowerCase());
        details.put("engine", "starrocks");
        details.put("maxRows", 10000);
        details.put("status", exists ? "published" : "unavailable");
        details.put("supports", Map.of("sql", true, "groupBy", true, "filter", true, "orderBy", true, "join", true, "batchLookup", false));
        return new SourceResolveDTO()
                .setSourceType(MetricSourceType.DATASET)
                .setSourceCode(datasetCode)
                .setSourceName(datasetCode)
                .setDetails(details);
    }

    @Override
    public SourceResolveDTO resolvePortraitFeature(String featureCode) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("featureCode", featureCode);
        details.put("featureTableCode", "FT_PORTRAIT_FEATURE_VALUE_STORE");
        details.put("entityType", "user");
        details.put("servingMode", "offline");
        details.put("storage", "narrowTable");
        details.put("tableName", "portrait_feature_value_store");
        details.put("entityField", "entity_id");
        details.put("entityTypeField", "entity_type");
        details.put("featureCodeField", "feature_code");
        details.put("valueField", "feature_value_decimal");
        details.put("snapshotField", "dt");
        details.put("status", "online");
        details.put("supports", Map.of("sql", true, "groupBy", true, "filter", true, "orderBy", false, "join", true, "batchLookup", false));
        return new SourceResolveDTO()
                .setSourceType(MetricSourceType.PORTRAIT_FEATURE)
                .setSourceCode(featureCode)
                .setSourceName(featureCode)
                .setDetails(details);
    }

    @Override
    public SourceResolveDTO resolveRealtimeTable(String tableCode) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("tableCode", tableCode);
        details.put("freshness", "realtime");
        details.put("queryMode", "olap");
        details.put("engine", "starrocks");
        details.put("tableName", tableCode.toLowerCase());
        details.put("timeField", "window_start");
        details.put("watermarkField", "event_watermark");
        details.put("latencySeconds", 10);
        details.put("supports", Map.of("sql", true, "groupBy", true, "filter", true, "orderBy", true, "join", true, "batchLookup", false));
        return new SourceResolveDTO()
                .setSourceType(MetricSourceType.REALTIME_TABLE)
                .setSourceCode(tableCode)
                .setSourceName(tableCode)
                .setDetails(details);
    }

    @Override
    public SourceResolveDTO resolveHttpApi(String apiCode) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("apiCode", apiCode);
        details.put("queryMode", "pointLookup");
        details.put("endpoint", "/" + apiCode.toLowerCase().replace("_", "/") + "/batch");
        details.put("method", "POST");
        details.put("authMode", "serviceToken");
        details.put("supports", Map.of("sql", false, "groupBy", false, "filter", true, "orderBy", false, "join", false, "batchLookup", true));
        details.put("limits", Map.of("maxBatchSize", 500, "timeoutMs", 3000));
        details.put("usage", "pointLookupOnly");
        return new SourceResolveDTO()
                .setSourceType(MetricSourceType.HTTP_API)
                .setSourceCode(apiCode)
                .setSourceName(apiCode)
                .setDetails(details);
    }
}
