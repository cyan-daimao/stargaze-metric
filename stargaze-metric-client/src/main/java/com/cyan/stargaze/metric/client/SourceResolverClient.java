package com.cyan.stargaze.metric.client;

import com.cyan.arch.common.api.Response;
import com.cyan.stargaze.metric.client.dto.SourceResolveDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 来源解析 RPC 契约(服务间调用)。
 * <p>
 * 路径统一 /rpc/v1,供查询/看板等服务获取数据集、画像、实时表、HTTP API 的物理执行信息。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@FeignClient(name = "stargaze-metric", contextId = "sourceResolverClient", path = "/rpc/v1", url = "${feign.stargaze-metric.url:}")
public interface SourceResolverClient {

    /**
     * 解析数据集来源。
     */
    @GetMapping("/dataset/{datasetCode}/resolve")
    Response<SourceResolveDTO> resolveDataset(@PathVariable("datasetCode") String datasetCode);

    /**
     * 解析画像特征来源。
     */
    @GetMapping("/portrait-feature/{featureCode}/resolve")
    Response<SourceResolveDTO> resolvePortraitFeature(@PathVariable("featureCode") String featureCode);

    /**
     * 解析实时表来源。
     */
    @GetMapping("/realtime-table/{tableCode}/resolve")
    Response<SourceResolveDTO> resolveRealtimeTable(@PathVariable("tableCode") String tableCode);

    /**
     * 解析 HTTP API 来源。
     */
    @GetMapping("/http-api/{apiCode}/resolve")
    Response<SourceResolveDTO> resolveHttpApi(@PathVariable("apiCode") String apiCode);
}
