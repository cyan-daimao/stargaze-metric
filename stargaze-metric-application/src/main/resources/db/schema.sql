-- ============================================================================
-- stargaze-metric 元数据库 Schema (PostgreSQL 15+)
-- ----------------------------------------------------------------------------
-- 服务: stargaze-metric
-- 说明:
--   1. 对应 DO: MetricDO / MetricBindingDO / MetricDimensionBindingDO
--      / MetricDimensionCompatDO / MetricVersionDO / DimensionDO / DimensionBindingDO。
--   2. 主键 id 为 BIGINT,由应用层 MyBatis-Plus IdType.ASSIGN_ID(雪花)赋值,数据库不自增。
--   3. 时间列使用 TIMESTAMPTZ 对齐 OffsetDateTime。
--   4. JSON/DSL 字段在 DO 中均为 String 序列化,故使用 TEXT 类型。
--   5. 逻辑删除: deleted_at 为 NULL 表示存活,@TableLogic(value="null", delval="now()")。
--   6. 已移除 workspace / workspace_id 等多租户字段。
-- ============================================================================

CREATE SCHEMA IF NOT EXISTS stargaze_metric;
SET search_path TO stargaze_metric;

-- 清理旧表(子表先删)
DROP TABLE IF EXISTS metric_dimension_compat  CASCADE;
DROP TABLE IF EXISTS metric_dimension_binding CASCADE;
DROP TABLE IF EXISTS metric_binding           CASCADE;
DROP TABLE IF EXISTS metric_version           CASCADE;
DROP TABLE IF EXISTS dimension_binding        CASCADE;
DROP TABLE IF EXISTS dimension                CASCADE;
DROP TABLE IF EXISTS metric                   CASCADE;

-- ============================================================================
-- 业务指标
-- ============================================================================
CREATE TABLE metric (
    id              BIGINT       PRIMARY KEY,
    name            VARCHAR(128) NOT NULL,              -- 指标名称,全局唯一
    code            VARCHAR(128),                       -- 指标标识(英文代码)
    business_name   VARCHAR(128),                       -- 业务名称
    description     TEXT,                               -- 描述
    folder          VARCHAR(128),                       -- 所属文件夹
    format          VARCHAR(32),                        -- 格式
    type            VARCHAR(16)  NOT NULL,              -- 类型:atomic/derived/window
    measure_kind    VARCHAR(16)  NOT NULL,              -- 度量方式:sum/avg/count/distinct_count/max/min/expr
    dsl             TEXT,                               -- 指标 DSL
    filter_condition TEXT,                              -- 过滤条件
    precision       INT,                                -- 精度
    primary_dataset_id BIGINT,                         -- 主数据集 ID
    owner_id        BIGINT,                             -- 负责人 ID
    status          VARCHAR(16)  NOT NULL DEFAULT 'draft',
    version         INT          NOT NULL DEFAULT 1,
    created_by      BIGINT,
    updated_by      BIGINT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE metric IS '业务指标(口径统一,可绑定多数据集)';
COMMENT ON COLUMN metric.name IS '指标名称,全局唯一';
COMMENT ON COLUMN metric.code IS '指标标识(英文代码)';
COMMENT ON COLUMN metric.type IS '类型:atomic/derived/window';
COMMENT ON COLUMN metric.measure_kind IS '度量方式:sum/avg/count/distinct_count/max/min/expr';
COMMENT ON COLUMN metric.dsl IS '指标 DSL';
COMMENT ON COLUMN metric.filter_condition IS '过滤条件';
COMMENT ON COLUMN metric.precision IS '精度';
COMMENT ON COLUMN metric.primary_dataset_id IS '主数据集 ID';
COMMENT ON COLUMN metric.status IS '状态:draft/published/offline';

CREATE UNIQUE INDEX uk_metric_name ON metric (name) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uk_metric_code ON metric (code) WHERE deleted_at IS NULL AND code IS NOT NULL;
CREATE INDEX idx_metric_status ON metric (status) WHERE deleted_at IS NULL;
CREATE INDEX idx_metric_folder ON metric (folder) WHERE deleted_at IS NULL;
CREATE INDEX idx_metric_owner ON metric (owner_id) WHERE deleted_at IS NULL;

-- ============================================================================
-- 指标版本(口径变更审计)
-- ============================================================================
CREATE TABLE metric_version (
    id         BIGINT       PRIMARY KEY,
    metric_id  BIGINT       NOT NULL,
    version    INT          NOT NULL,
    dsl        TEXT,
    change_log TEXT,
    created_by BIGINT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ
);
COMMENT ON TABLE metric_version IS '指标版本(口径变更审计)';
COMMENT ON COLUMN metric_version.version IS '版本号';
COMMENT ON COLUMN metric_version.change_log IS '变更说明';

CREATE UNIQUE INDEX uk_metric_version_metric_version ON metric_version (metric_id, version) WHERE deleted_at IS NULL;
CREATE INDEX idx_metric_version_metric ON metric_version (metric_id) WHERE deleted_at IS NULL;

-- ============================================================================
-- 指标-数据集字段绑定
-- ============================================================================
CREATE TABLE metric_binding (
    id           BIGINT   PRIMARY KEY,
    metric_id    BIGINT   NOT NULL,
    dataset_id   BIGINT   NOT NULL,
    field_id     BIGINT,
    is_primary   BOOLEAN  NOT NULL DEFAULT FALSE,       -- 是否主数据集
    dsl_override TEXT,                                  -- 该数据集下 DSL 覆盖
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at   TIMESTAMPTZ
);
COMMENT ON TABLE metric_binding IS '指标-数据集字段绑定(同一指标可绑定多个数据集)';
COMMENT ON COLUMN metric_binding.is_primary IS '是否主数据集';
COMMENT ON COLUMN metric_binding.dsl_override IS '该数据集下 DSL 覆盖';

CREATE INDEX idx_metric_binding_metric ON metric_binding (metric_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_metric_binding_dataset ON metric_binding (dataset_id) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uk_metric_binding_metric_dataset ON metric_binding (metric_id, dataset_id) WHERE deleted_at IS NULL;

-- ============================================================================
-- 指标-维度绑定
-- ============================================================================
CREATE TABLE metric_dimension_binding (
    id             BIGINT   PRIMARY KEY,
    metric_id      BIGINT   NOT NULL,
    dimension_id   BIGINT,
    dimension_name VARCHAR(128) NOT NULL,              -- 维度名称(冗余,便于展示)
    dataset_id     BIGINT,
    field_id       BIGINT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at     TIMESTAMPTZ
);
COMMENT ON TABLE metric_dimension_binding IS '指标绑定的维度字段';
COMMENT ON COLUMN metric_dimension_binding.dimension_name IS '维度名称(冗余,便于展示)';

CREATE INDEX idx_metric_dim_binding_metric ON metric_dimension_binding (metric_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_metric_dim_binding_dimension ON metric_dimension_binding (dimension_id) WHERE deleted_at IS NULL;

-- ============================================================================
-- 指标×维度组合合法性
-- ============================================================================
CREATE TABLE metric_dimension_compat (
    metric_id    BIGINT    NOT NULL,
    dimension_id BIGINT    NOT NULL,
    allowed      BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at   TIMESTAMPTZ,
    PRIMARY KEY (metric_id, dimension_id)
);
COMMENT ON TABLE metric_dimension_compat IS '指标×维度组合合法性';
COMMENT ON COLUMN metric_dimension_compat.allowed IS '是否允许组合';

CREATE INDEX idx_metric_dim_compat_dimension ON metric_dimension_compat (dimension_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_metric_dim_compat_allowed ON metric_dimension_compat (metric_id, dimension_id, allowed) WHERE deleted_at IS NULL;

-- ============================================================================
-- 业务维度(可跨数据集)
-- ============================================================================
CREATE TABLE dimension (
    id            BIGINT       PRIMARY KEY,
    name          VARCHAR(128) NOT NULL,              -- 维度名称,全局唯一
    code          VARCHAR(128),                       -- 维度标识(英文代码)
    business_name VARCHAR(128),                       -- 业务名称
    folder        VARCHAR(128),                       -- 所属文件夹
    semantic_type VARCHAR(16)  NOT NULL DEFAULT 'category', -- 语义类型:geo/time/category
    dictionary_id BIGINT,
    format        TEXT,                               -- 格式(JSON 序列化字符串)
    owner_id      BIGINT,                             -- 负责人 ID
    status        VARCHAR(16)  NOT NULL DEFAULT 'draft',
    created_by    BIGINT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at    TIMESTAMPTZ
);
COMMENT ON TABLE dimension IS '业务维度(可跨数据集绑定)';
COMMENT ON COLUMN dimension.name IS '维度名称,全局唯一';
COMMENT ON COLUMN dimension.semantic_type IS '语义类型:geo/time/category';
COMMENT ON COLUMN dimension.status IS '状态:draft/published/offline';

CREATE UNIQUE INDEX uk_dimension_name ON dimension (name) WHERE deleted_at IS NULL;
CREATE INDEX idx_dimension_status ON dimension (status) WHERE deleted_at IS NULL;
CREATE INDEX idx_dimension_owner ON dimension (owner_id) WHERE deleted_at IS NULL;

-- ============================================================================
-- 维度-数据集字段绑定
-- ============================================================================
CREATE TABLE dimension_binding (
    id            BIGINT   PRIMARY KEY,
    dimension_id  BIGINT   NOT NULL,
    dataset_id    BIGINT   NOT NULL,
    field_id      BIGINT   NOT NULL,
    expr          TEXT,                               -- 维度计算表达式(可空)
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at    TIMESTAMPTZ
);
COMMENT ON TABLE dimension_binding IS '维度-数据集字段绑定(一个维度可绑定多个数据集)';
COMMENT ON COLUMN dimension_binding.expr IS '维度计算表达式(可空)';

CREATE INDEX idx_dimension_binding_dimension ON dimension_binding (dimension_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_dimension_binding_dataset ON dimension_binding (dataset_id) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uk_dimension_binding_dimension_dataset ON dimension_binding (dimension_id, dataset_id) WHERE deleted_at IS NULL;
