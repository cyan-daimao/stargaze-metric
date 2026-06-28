package com.cyan.stargaze.metric.client;

import com.cyan.arch.common.api.Response;
import com.cyan.stargaze.metric.client.dto.DimensionDTO;
import com.cyan.stargaze.metric.client.dto.ValidationResultDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 维度平台 RPC 契约(服务间调用)。
 * <p>
 * 路径统一 /rpc/v1/dimension,供 dashboard 依赖,列出已发布维度与校验维度。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@FeignClient(name = "stargaze-metric", contextId = "dimensionClient", path = "/rpc/v1/dimension", url = "${feign.stargaze-metric.url:}")
public interface DimensionClient {

    /**
     * 列出已发布维度。
     */
    @GetMapping("/list")
    Response<List<DimensionDTO>> list();

    /**
     * 校验维度编码列表(存在且已发布)。
     */
    @GetMapping("/validate")
    Response<ValidationResultDTO> validate(@RequestParam("dimCodes") List<String> dimCodes);
}
