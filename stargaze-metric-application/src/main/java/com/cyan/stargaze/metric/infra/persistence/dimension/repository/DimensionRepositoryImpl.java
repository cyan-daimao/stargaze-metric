package com.cyan.stargaze.metric.infra.persistence.dimension.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.stargaze.metric.domain.dimension.Dimension;
import com.cyan.stargaze.metric.domain.dimension.repository.DimensionRepository;
import com.cyan.stargaze.metric.enums.MetricStatus;
import com.cyan.stargaze.metric.infra.persistence.MetricInfraConvert;
import com.cyan.stargaze.metric.infra.persistence.dimension.dos.DimensionDO;
import com.cyan.stargaze.metric.infra.persistence.dimension.mappers.DimensionMapper;
import com.cyan.stargaze.metric.infra.util.IdUtil;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DimensionRepositoryImpl implements DimensionRepository {

    private final DimensionMapper mapper;
    private final MetricInfraConvert convert;

    public DimensionRepositoryImpl(DimensionMapper mapper, MetricInfraConvert convert) {
        this.mapper = mapper;
        this.convert = convert;
    }

    @Override
    public Dimension findById(String id) {
        DimensionDO doObj = mapper.selectById(IdUtil.toLong(id));
        return doObj == null ? null : convert.toDimension(doObj);
    }

    @Override
    public Dimension findByName(String workspaceId, String name) {
        DimensionDO doObj = mapper.selectOne(new LambdaQueryWrapper<DimensionDO>()
                .eq(DimensionDO::getWorkspaceId, IdUtil.toLong(workspaceId))
                .eq(DimensionDO::getName, name));
        return doObj == null ? null : convert.toDimension(doObj);
    }

    @Override
    public List<Dimension> listByWorkspace(String workspaceId, MetricStatus status) {
        LambdaQueryWrapper<DimensionDO> wrapper = new LambdaQueryWrapper<DimensionDO>()
                .eq(DimensionDO::getWorkspaceId, IdUtil.toLong(workspaceId))
                .eq(status != null, DimensionDO::getStatus, status)
                .orderByDesc(DimensionDO::getCreatedAt);
        return mapper.selectList(wrapper).stream().map(convert::toDimension).toList();
    }

    @Override
    public Dimension save(Dimension dimension) {
        DimensionDO doObj = convert.toDimensionDO(dimension);
        mapper.insert(doObj);
        return findById(IdUtil.toString(doObj.getId()));
    }

    @Override
    public Dimension update(Dimension dimension) {
        DimensionDO doObj = convert.toDimensionDO(dimension);
        mapper.updateById(doObj);
        return findById(IdUtil.toString(doObj.getId()));
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(IdUtil.toLong(id));
    }
}
