package com.cyan.stargaze.metric.adapter.dimension.rpc;

import com.cyan.arch.common.api.Response;
import com.cyan.stargaze.metric.adapter.MetricAdapterConvert;
import com.cyan.stargaze.metric.application.dimension.DimensionService;
import com.cyan.stargaze.metric.client.DimensionClient;
import com.cyan.stargaze.metric.client.dto.DimensionDTO;
import com.cyan.stargaze.metric.client.dto.ValidationResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 维度 RPC 控制器:实现 {@link DimensionClient} 契约(/rpc/v1/dimension)。
 * <p>
 * 供 dashboard 依赖,列出已发布维度与校验维度。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/rpc/v1/dimension")
@RequiredArgsConstructor
public class DimensionRpcController implements DimensionClient {

    private final DimensionService dimensionService;
    private final MetricAdapterConvert adapterConvert;

    @Override
    public Response<List<DimensionDTO>> list() {
        return Response.success(adapterConvert.toClientDimensionDTOList(dimensionService.list(true)));
    }

    @Override
    public Response<ValidationResultDTO> validate(List<String> dimCodes) {
        return Response.success(dimensionService.validate(dimCodes));
    }
}
