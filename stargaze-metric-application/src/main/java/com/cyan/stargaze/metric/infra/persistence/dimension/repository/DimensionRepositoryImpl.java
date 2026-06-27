package com.cyan.stargaze.metric.infra.persistence.dimension.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.arch.common.api.Page;
import com.cyan.stargaze.metric.domain.dimension.Dimension;
import com.cyan.stargaze.metric.domain.dimension.repository.DimensionRepository;
import com.cyan.stargaze.metric.enums.MetricStatus;
import com.cyan.stargaze.metric.infra.persistence.MetricInfraConvert;
import com.cyan.stargaze.metric.infra.persistence.dimension.dos.DimensionDO;
import com.cyan.stargaze.metric.infra.persistence.dimension.mappers.DimensionMapper;
import com.cyan.stargaze.metric.infra.util.IdUtil;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
    public Dimension findByName(String name) {
        DimensionDO doObj = mapper.selectOne(new LambdaQueryWrapper<DimensionDO>()
                .eq(DimensionDO::getName, name));
        return doObj == null ? null : convert.toDimension(doObj);
    }

    @Override
    public Dimension findByCode(String code) {
        DimensionDO doObj = mapper.selectOne(new LambdaQueryWrapper<DimensionDO>()
                .eq(DimensionDO::getCode, code));
        return doObj == null ? null : convert.toDimension(doObj);
    }

    @Override
    public List<Dimension> list(MetricStatus status) {
        LambdaQueryWrapper<DimensionDO> wrapper = new LambdaQueryWrapper<DimensionDO>()
                .eq(status != null, DimensionDO::getStatus, status)
                .orderByDesc(DimensionDO::getCreatedAt);
        return mapper.selectList(wrapper).stream().map(convert::toDimension).toList();
    }

    @Override
    public Page<Dimension> page(int current, int size, String keyword, MetricStatus status, String folder) {
        LambdaQueryWrapper<DimensionDO> wrapper = new LambdaQueryWrapper<DimensionDO>()
                .and(keyword != null && !keyword.isBlank(), w -> w
                        .like(DimensionDO::getName, keyword)
                        .or()
                        .like(DimensionDO::getCode, keyword))
                .eq(status != null, DimensionDO::getStatus, status)
                .eq(folder != null && !folder.isBlank(), DimensionDO::getFolder, folder)
                .orderByDesc(DimensionDO::getCreatedAt);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<DimensionDO> doPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);
        var result = mapper.selectPage(doPage, wrapper);
        List<Dimension> data = Optional.ofNullable(result.getRecords()).orElse(List.of()).stream()
                .map(convert::toDimension)
                .toList();
        return new Page<>(data, result.getCurrent(), result.getSize(), result.getTotal());
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

    @Override
    public List<String> listDistinctFolders() {
        LambdaQueryWrapper<DimensionDO> wrapper = new LambdaQueryWrapper<DimensionDO>()
                .select(DimensionDO::getFolder)
                .isNotNull(DimensionDO::getFolder)
                .ne(DimensionDO::getFolder, "")
                .groupBy(DimensionDO::getFolder)
                .orderByAsc(DimensionDO::getFolder);
        return mapper.selectList(wrapper).stream()
                .map(DimensionDO::getFolder)
                .distinct()
                .toList();
    }
}
