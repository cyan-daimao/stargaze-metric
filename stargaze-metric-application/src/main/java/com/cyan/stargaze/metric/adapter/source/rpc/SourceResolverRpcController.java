package com.cyan.stargaze.metric.adapter.source.rpc;

import com.alibaba.fastjson2.JSON;
import com.cyan.arch.common.api.Response;
import com.cyan.stargaze.dataset.client.DatasetClient;
import com.cyan.stargaze.metric.client.dto.SourceResolveDTO;
import com.cyan.stargaze.metric.enums.MetricSourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 来源解析 RPC 控制器(/rpc/v1/source)。
 * <p>
 * 仅供服务间调用,将 dataset/画像/实时表/API 来源解析为物理执行信息。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/rpc/v1")
@RequiredArgsConstructor
public class SourceResolverRpcController {

    private final DatasetClient datasetClient;

    @GetMapping("/dataset/{datasetCode}/resolve")
    public Response<SourceResolveDTO> resolveDataset(@PathVariable("datasetCode") String datasetCode) {
        var resp = datasetClient.exists(datasetCode);
        boolean exists = resp != null && resp.getCode() == 200 && Boolean.TRUE.equals(resp.getData());
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("datasetCode", datasetCode);
        details.put("tableName", "dwd_" + datasetCode.toLowerCase());
        details.put("engine", "starrocks");
        details.put("maxRows", 10000);
        details.put("status", exists ? "published" : "unavailable");
        details.put("supports", Map.of("sql", true, "groupBy", true, "filter", true, "orderBy", true, "join", true, "batchLookup", false));
        return Response.success(new SourceResolveDTO()
                .setSourceType(MetricSourceType.DATASET)
                .setSourceCode(datasetCode)
                .setSourceName(datasetCode)
                .setDetails(details));
    }

    @GetMapping("/portrait-feature/{featureCode}/resolve")
    public Response<SourceResolveDTO> resolvePortraitFeature(@PathVariable("featureCode") String featureCode) {
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
        return Response.success(new SourceResolveDTO()
                .setSourceType(MetricSourceType.PORTRAIT_FEATURE)
                .setSourceCode(featureCode)
                .setSourceName(featureCode)
                .setDetails(details));
    }

    @GetMapping("/realtime-table/{tableCode}/resolve")
    public Response<SourceResolveDTO> resolveRealtimeTable(@PathVariable("tableCode") String tableCode) {
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
        return Response.success(new SourceResolveDTO()
                .setSourceType(MetricSourceType.REALTIME_TABLE)
                .setSourceCode(tableCode)
                .setSourceName(tableCode)
                .setDetails(details));
    }

    @GetMapping("/http-api/{apiCode}/resolve")
    public Response<SourceResolveDTO> resolveHttpApi(@PathVariable("apiCode") String apiCode) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("apiCode", apiCode);
        details.put("queryMode", "pointLookup");
        details.put("endpoint", "/" + apiCode.toLowerCase().replace("_", "/") + "/batch");
        details.put("method", "POST");
        details.put("authMode", "serviceToken");
        details.put("supports", Map.of("sql", false, "groupBy", false, "filter", true, "orderBy", false, "join", false, "batchLookup", true));
        details.put("limits", Map.of("maxBatchSize", 500, "timeoutMs", 3000));
        details.put("usage", "pointLookupOnly");
        return Response.success(new SourceResolveDTO()
                .setSourceType(MetricSourceType.HTTP_API)
                .setSourceCode(apiCode)
                .setSourceName(apiCode)
                .setDetails(details));
    }
}
