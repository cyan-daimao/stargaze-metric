package com.cyan.stargaze.metric.application.dimension;

import com.cyan.arch.common.api.Page;
import com.cyan.stargaze.metric.application.dimension.bo.DimensionDetailBO;
import com.cyan.stargaze.metric.application.dimension.cmd.DimensionBindingCmd;
import com.cyan.stargaze.metric.application.dimension.cmd.DimensionCmd;
import com.cyan.stargaze.metric.client.dto.DimensionPreviewResponseDTO;
import com.cyan.stargaze.metric.domain.dimension.Dimension;
import com.cyan.stargaze.metric.domain.dimension.DimensionBinding;

import java.util.List;

/**
 * 维度应用服务。
 *
 * @author cy.Y
 * @since 1.0.0
 */
public interface DimensionService {

    Dimension create(DimensionCmd cmd);

    Dimension update(DimensionCmd cmd);

    Dimension findById(String id);

    /**
     * 查询维度详情(含关联数据集、关联指标)。
     */
    DimensionDetailBO findDetail(String id);

    List<Dimension> list(boolean publishedOnly);

    /**
     * 查询已发布维度详情列表。
     */
    List<DimensionDetailBO> listDetail(boolean publishedOnly);

    Page<Dimension> page(Integer page, Integer size, String keyword, String folder, String status);

    /**
     * 分页查询维度详情(含关联数据集、关联指标)。
     */
    Page<DimensionDetailBO> pageDetail(Integer page, Integer size, String keyword, String folder, String status);

    void delete(String id);

    Dimension publish(String id);

    DimensionBinding addBinding(DimensionBindingCmd cmd);

    void removeBinding(String bindingId);

    List<DimensionBinding> listBindings(String dimensionId);

    /** 列出所有不重复的目录名 */
    List<String> listFolders();

    /**
     * 维度预览(分组统计维度值,返回 SQL + 列名 + 数据行)。
     *
     * @param id 维度 ID
     * @return 预览结果
     */
    DimensionPreviewResponseDTO preview(String id);
}
