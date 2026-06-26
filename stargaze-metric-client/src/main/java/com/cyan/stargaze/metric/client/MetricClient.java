package com.cyan.stargaze.metric.client;

import com.cyan.arch.common.api.Response;
import com.cyan.stargaze.metric.client.dto.MetricDTO;
import com.cyan.stargaze.metric.client.dto.MetricResolveDTO;
import com.cyan.stargaze.metric.client.dto.ValidationResultDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 指标平台 RPC 契约(服务间调用)。
 * <p>
 * 路径统一 /rpc/metric,供 query/dashboard 依赖。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@FeignClient(name = "stargaze-metric", contextId = "metricClient", path = "/rpc/metric", url = "${feign.stargaze-metric.url:}")
public interface MetricClient {

    /**
     * 解析指标到指定数据集(纯函数,query 编译期调用)
     */
    @GetMapping("/{id}/resolve")
    Response<MetricResolveDTO> resolve(@PathVariable("id") String metricId,
                                       @RequestParam("dataset_id") String datasetId);

    /**
     * 列出已发布指标(dashboard 选指标用)
     */
    @GetMapping("/list")
    Response<List<MetricDTO>> listPublished();

    /**
     * 校验指标×维度组合合法性
     */
    @GetMapping("/validate")
    Response<ValidationResultDTO> validate(@RequestParam("metric_ids") List<String> metricIds,
                                           @RequestParam("dimension_ids") List<String> dimensionIds);

    /**
     * 指标是否存在且已发布
     */
    @GetMapping("/{id}/exists")
    Response<Boolean> exists(@PathVariable("id") String metricId);
}
