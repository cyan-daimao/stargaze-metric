package com.cyan.stargaze.metric.infra.persistence.metric.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.stargaze.metric.domain.metric.MetricBinding;
import com.cyan.stargaze.metric.domain.metric.repository.MetricBindingRepository;
import com.cyan.stargaze.metric.infra.persistence.MetricInfraConvert;
import com.cyan.stargaze.metric.infra.persistence.metric.dos.MetricBindingDO;
import com.cyan.stargaze.metric.infra.persistence.metric.mappers.MetricBindingMapper;
import com.cyan.stargaze.metric.infra.util.IdUtil;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MetricBindingRepositoryImpl implements MetricBindingRepository {

    private final MetricBindingMapper mapper;
    private final MetricInfraConvert convert;

    public MetricBindingRepositoryImpl(MetricBindingMapper mapper, MetricInfraConvert convert) {
        this.mapper = mapper;
        this.convert = convert;
    }

    @Override
    public MetricBinding findByMetricAndDataset(String metricId, String datasetId) {
        MetricBindingDO doObj = mapper.selectOne(new LambdaQueryWrapper<MetricBindingDO>()
                .eq(MetricBindingDO::getMetricId, IdUtil.toLong(metricId))
                .eq(MetricBindingDO::getDatasetId, IdUtil.toLong(datasetId)));
        return doObj == null ? null : convert.toMetricBinding(doObj);
    }

    @Override
    public List<MetricBinding> listByMetric(String metricId) {
        return mapper.selectList(new LambdaQueryWrapper<MetricBindingDO>()
                .eq(MetricBindingDO::getMetricId, IdUtil.toLong(metricId)))
                .stream().map(convert::toMetricBinding).toList();
    }

    @Override
    public MetricBinding save(MetricBinding binding) {
        MetricBindingDO doObj = convert.toMetricBindingDO(binding);
        mapper.insert(doObj);
        return convert.toMetricBinding(mapper.selectById(doObj.getId()));
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(IdUtil.toLong(id));
    }

    @Override
    public void deleteByMetric(String metricId) {
        mapper.delete(new LambdaQueryWrapper<MetricBindingDO>()
                .eq(MetricBindingDO::getMetricId, IdUtil.toLong(metricId)));
    }
}
