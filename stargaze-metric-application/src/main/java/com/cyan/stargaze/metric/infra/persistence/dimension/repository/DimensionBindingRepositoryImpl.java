package com.cyan.stargaze.metric.infra.persistence.dimension.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.stargaze.metric.domain.dimension.DimensionBinding;
import com.cyan.stargaze.metric.domain.dimension.repository.DimensionBindingRepository;
import com.cyan.stargaze.metric.infra.persistence.MetricInfraConvert;
import com.cyan.stargaze.metric.infra.persistence.dimension.dos.DimensionBindingDO;
import com.cyan.stargaze.metric.infra.persistence.dimension.mappers.DimensionBindingMapper;
import com.cyan.stargaze.metric.infra.util.IdUtil;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DimensionBindingRepositoryImpl implements DimensionBindingRepository {

    private final DimensionBindingMapper mapper;
    private final MetricInfraConvert convert;

    public DimensionBindingRepositoryImpl(DimensionBindingMapper mapper, MetricInfraConvert convert) {
        this.mapper = mapper;
        this.convert = convert;
    }

    @Override
    public DimensionBinding findByDimensionAndDataset(String dimensionId, String datasetId) {
        DimensionBindingDO doObj = mapper.selectOne(new LambdaQueryWrapper<DimensionBindingDO>()
                .eq(DimensionBindingDO::getDimensionId, IdUtil.toLong(dimensionId))
                .eq(DimensionBindingDO::getDatasetId, IdUtil.toLong(datasetId)));
        return doObj == null ? null : convert.toDimensionBinding(doObj);
    }

    @Override
    public List<DimensionBinding> listByDimension(String dimensionId) {
        return mapper.selectList(new LambdaQueryWrapper<DimensionBindingDO>()
                .eq(DimensionBindingDO::getDimensionId, IdUtil.toLong(dimensionId)))
                .stream().map(convert::toDimensionBinding).toList();
    }

    @Override
    public DimensionBinding save(DimensionBinding binding) {
        DimensionBindingDO doObj = convert.toDimensionBindingDO(binding);
        mapper.insert(doObj);
        return convert.toDimensionBinding(mapper.selectById(doObj.getId()));
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(IdUtil.toLong(id));
    }

    @Override
    public void deleteByDimension(String dimensionId) {
        mapper.delete(new LambdaQueryWrapper<DimensionBindingDO>()
                .eq(DimensionBindingDO::getDimensionId, IdUtil.toLong(dimensionId)));
    }
}
