package com.cyan.stargaze.metric.infra.persistence.metric.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.stargaze.metric.domain.metric.MetricDimensionCompat;
import com.cyan.stargaze.metric.domain.metric.repository.MetricCompatRepository;
import com.cyan.stargaze.metric.infra.persistence.MetricInfraConvert;
import com.cyan.stargaze.metric.infra.persistence.metric.dos.MetricDimensionCompatDO;
import com.cyan.stargaze.metric.infra.persistence.metric.mappers.MetricDimensionCompatMapper;
import com.cyan.stargaze.metric.infra.util.IdUtil;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MetricCompatRepositoryImpl implements MetricCompatRepository {

    private final MetricDimensionCompatMapper mapper;
    private final MetricInfraConvert convert;

    public MetricCompatRepositoryImpl(MetricDimensionCompatMapper mapper, MetricInfraConvert convert) {
        this.mapper = mapper;
        this.convert = convert;
    }

    @Override
    public List<MetricDimensionCompat> listByMetric(String metricId) {
        return mapper.selectList(new LambdaQueryWrapper<MetricDimensionCompatDO>()
                .eq(MetricDimensionCompatDO::getMetricId, IdUtil.toLong(metricId)))
                .stream().map(convert::toCompat).toList();
    }

    @Override
    public boolean isAllowed(String metricId, String dimensionId) {
        MetricDimensionCompatDO doObj = mapper.selectOne(new LambdaQueryWrapper<MetricDimensionCompatDO>()
                .eq(MetricDimensionCompatDO::getMetricId, IdUtil.toLong(metricId))
                .eq(MetricDimensionCompatDO::getDimensionId, IdUtil.toLong(dimensionId)));
        // 无记录默认允许
        return doObj == null || Boolean.TRUE.equals(doObj.getAllowed());
    }

    @Override
    public MetricDimensionCompat save(MetricDimensionCompat compat) {
        MetricDimensionCompatDO doObj = convert.toCompatDO(compat);
        mapper.insert(doObj);
        return convert.toCompat(doObj);
    }
}
