package com.cyan.stargaze.metric.infra.persistence.metric.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.stargaze.metric.domain.metric.Metric;
import com.cyan.stargaze.metric.domain.metric.repository.MetricRepository;
import com.cyan.stargaze.metric.enums.MetricStatus;
import com.cyan.stargaze.metric.infra.persistence.MetricInfraConvert;
import com.cyan.stargaze.metric.infra.persistence.metric.dos.MetricDO;
import com.cyan.stargaze.metric.infra.persistence.metric.mappers.MetricMapper;
import com.cyan.stargaze.metric.infra.util.IdUtil;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MetricRepositoryImpl implements MetricRepository {

    private final MetricMapper mapper;
    private final MetricInfraConvert convert;

    public MetricRepositoryImpl(MetricMapper mapper, MetricInfraConvert convert) {
        this.mapper = mapper;
        this.convert = convert;
    }

    @Override
    public Metric findById(String id) {
        MetricDO doObj = mapper.selectById(IdUtil.toLong(id));
        return doObj == null ? null : convert.toMetric(doObj);
    }

    @Override
    public Metric findByName(String name) {
        MetricDO doObj = mapper.selectOne(new LambdaQueryWrapper<MetricDO>()
                .eq(MetricDO::getName, name));
        return doObj == null ? null : convert.toMetric(doObj);
    }

    @Override
    public Metric findByCode(String code) {
        MetricDO doObj = mapper.selectOne(new LambdaQueryWrapper<MetricDO>()
                .eq(MetricDO::getCode, code));
        return doObj == null ? null : convert.toMetric(doObj);
    }

    @Override
    public List<Metric> list(MetricStatus status) {
        LambdaQueryWrapper<MetricDO> wrapper = new LambdaQueryWrapper<MetricDO>()
                .eq(status != null, MetricDO::getStatus, status)
                .orderByDesc(MetricDO::getCreatedAt);
        return mapper.selectList(wrapper).stream().map(convert::toMetric).toList();
    }

    @Override
    public IPage<Metric> page(IPage<Metric> page, String keyword, MetricStatus status, String folder) {
        LambdaQueryWrapper<MetricDO> wrapper = new LambdaQueryWrapper<MetricDO>()
                .and(keyword != null && !keyword.isBlank(), w -> w
                        .like(MetricDO::getName, keyword)
                        .or()
                        .like(MetricDO::getCode, keyword))
                .eq(status != null, MetricDO::getStatus, status)
                .eq(folder != null && !folder.isBlank(), MetricDO::getFolder, folder)
                .orderByDesc(MetricDO::getCreatedAt);
        Page<MetricDO> doPage = new Page<>(page.getCurrent(), page.getSize());
        IPage<MetricDO> result = mapper.selectPage(doPage, wrapper);
        return result.convert(convert::toMetric);
    }

    @Override
    public Metric save(Metric metric) {
        MetricDO doObj = convert.toMetricDO(metric);
        mapper.insert(doObj);
        return findById(IdUtil.toString(doObj.getId()));
    }

    @Override
    public Metric update(Metric metric) {
        MetricDO doObj = convert.toMetricDO(metric);
        mapper.updateById(doObj);
        return findById(IdUtil.toString(doObj.getId()));
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(IdUtil.toLong(id));
    }

    @Override
    public List<String> listDistinctFolders() {
        LambdaQueryWrapper<MetricDO> wrapper = new LambdaQueryWrapper<MetricDO>()
                .select(MetricDO::getFolder)
                .isNotNull(MetricDO::getFolder)
                .ne(MetricDO::getFolder, "")
                .groupBy(MetricDO::getFolder)
                .orderByAsc(MetricDO::getFolder);
        return mapper.selectList(wrapper).stream()
                .map(MetricDO::getFolder)
                .distinct()
                .toList();
    }
}
