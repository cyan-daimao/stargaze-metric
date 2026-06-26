package com.cyan.stargaze.metric.adapter.dataset.http;

import com.cyan.arch.common.api.Response;
import com.cyan.stargaze.metric.application.metric.MetricService;
import com.cyan.stargaze.metric.client.dto.DatasetListItemDTO;
import com.cyan.stargaze.metric.client.dto.PageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据集只读控制器(/api/v1/datasets)。
 * <p>
 * 指标平台视角的数据集查询，数据来自数据集平台，绑定数量由指标平台补充。
 *
 * @author cy.Y
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/datasets")
@RequiredArgsConstructor
public class DatasetController {

    private final MetricService metricService;

    @GetMapping
    public Response<PageDTO<DatasetListItemDTO>> list(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "status", required = false) String status) {
        return Response.success(metricService.listSyncDatasets(page, size, keyword, type, status));
    }
}
