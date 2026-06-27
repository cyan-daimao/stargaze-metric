-- ============================================================================
-- stargaze-metric 元数据库 Schema (PostgreSQL 15+)
-- ----------------------------------------------------------------------------
-- 服务: stargaze-metric
-- 说明:
--   1. 对应指标平台语义资产重构后的数据模型,以 metric/dimension 为核心,
--      统一描述来源绑定(sourceBinding)与 DSL。
--   2. 主键 id 为 BIGINT,由应用层 MyBatis-Plus IdType.ASSIGN_ID(雪花)赋值。
--   3. 业务键 metric_code / dim_code 全局唯一,作为路由与消费端引用标识。
--   4. JSON/DSL 字段在 DO 中使用 String 序列化,数据库使用 TEXT 类型。
--   5. 逻辑删除: deleted_at 为 NULL 表示存活,@TableLogic(value="null", delval="now()")。
--   6. 已移除 workspace / workspace_id 等多租户字段;created_by/updated_by 存储工号。
-- ============================================================================

CREATE SCHEMA IF NOT EXISTS stargaze_metric;
SET search_path TO stargaze_metric;

-- 清理旧表(子表先删)
DROP TABLE IF EXISTS metric_dimension_compat  CASCADE;
DROP TABLE IF EXISTS metric_dimension_binding CASCADE;
DROP TABLE IF EXISTS metric_binding           CASCADE;
DROP TABLE IF EXISTS metric_version           CASCADE;
DROP TABLE IF EXISTS semantic_asset_binding   CASCADE;
DROP TABLE IF EXISTS dimension_binding        CASCADE;
DROP TABLE IF EXISTS dimension                CASCADE;
DROP TABLE IF EXISTS metric                   CASCADE;

-- ============================================================================
-- 业务指标
-- ============================================================================
CREATE TABLE metric (
    id              BIGINT          PRIMARY KEY,
    metric_code     VARCHAR(128)    NOT NULL,              -- 指标业务编码,全局唯一
    name            VARCHAR(128)    NOT NULL,              -- 指标名称,全局唯一
    code            VARCHAR(128)    NOT NULL,              -- 指标标识(英文代码,全局唯一)
    description     TEXT,                                  -- 业务定义/口径说明
    folder          VARCHAR(128),                          -- 所属目录
    format          VARCHAR(32),                           -- 数据格式:number/percent/currency/int
    status          VARCHAR(16)     NOT NULL DEFAULT 'draft', -- draft/published/offline/sourceError
    source_type     VARCHAR(32)     NOT NULL,              -- dataset/portraitFeature/portraitTag/portraitCrowd/realtimeTable/httpApi
    source_code     VARCHAR(128)    NOT NULL,              -- 来源编码(数据集code/特征code/表code/API code)
    source_name     VARCHAR(128),                          -- 来源名称(冗余展示)
    query_mode      VARCHAR(16)     NOT NULL DEFAULT 'olap', -- olap/pointLookup/filterOnly
    freshness       VARCHAR(16),                           -- offline/nearRealtime/realtime
    dsl_kind        VARCHAR(16)     NOT NULL DEFAULT 'atomic', -- atomic/derived/window/apiMetric
    dsl             TEXT,                                  -- 指标 DSL JSON(metric.dsl.v1)
    source_snapshot TEXT,                                  -- 来源解析快照 JSON
    supports        TEXT,                                  -- 能力声明 JSON(sql/groupBy/filter/orderBy/join/batchLookup)
    precision       INT,                                   -- 小数位精度
    owner_id        BIGINT,                                -- 负责人 ID
    created_by      VARCHAR(64),                           -- 创建人工号
    updated_by      VARCHAR(64),                           -- 更新人工号
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE metric IS '业务指标(语义资产,统一来源绑定与 DSL)';
COMMENT ON COLUMN metric.metric_code IS '指标业务编码,全局唯一';
COMMENT ON COLUMN metric.name IS '指标名称,全局唯一';
COMMENT ON COLUMN metric.code IS '指标标识(英文代码),全局唯一';
COMMENT ON COLUMN metric.status IS '状态:draft/published/offline/sourceError';
COMMENT ON COLUMN metric.source_type IS '来源类型:dataset/portraitFeature/...';
COMMENT ON COLUMN metric.source_code IS '来源编码';
COMMENT ON COLUMN metric.query_mode IS '查询能力:olap/pointLookup/filterOnly';
COMMENT ON COLUMN metric.dsl IS '指标 DSL JSON';
COMMENT ON COLUMN metric.source_snapshot IS '来源解析快照 JSON';
COMMENT ON COLUMN metric.supports IS '来源能力声明 JSON';

CREATE UNIQUE INDEX uk_metric_code ON metric (metric_code) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uk_metric_name ON metric (name) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uk_metric_code_uk ON metric (code) WHERE deleted_at IS NULL;
CREATE INDEX idx_metric_status ON metric (status) WHERE deleted_at IS NULL;
CREATE INDEX idx_metric_folder ON metric (folder) WHERE deleted_at IS NULL;
CREATE INDEX idx_metric_source ON metric (source_type, source_code) WHERE deleted_at IS NULL;

-- ============================================================================
-- 业务维度
-- ============================================================================
CREATE TABLE dimension (
    id              BIGINT          PRIMARY KEY,
    dim_code        VARCHAR(128)    NOT NULL,              -- 维度业务编码,全局唯一
    name            VARCHAR(128)    NOT NULL,              -- 维度名称,全局唯一
    description     TEXT,                                  -- 维度说明
    folder          VARCHAR(128),                          -- 所属目录
    semantic_type   VARCHAR(16)     NOT NULL DEFAULT 'category', -- geo/time/category
    format          TEXT,                                  -- 格式配置 JSON
    status          VARCHAR(16)     NOT NULL DEFAULT 'draft', -- draft/published/offline
    source_type     VARCHAR(32)     NOT NULL DEFAULT 'dataset',
    source_code     VARCHAR(128)    NOT NULL,              -- 来源编码
    source_name     VARCHAR(128),                          -- 来源名称
    query_mode      VARCHAR(16)     NOT NULL DEFAULT 'olap',
    freshness       VARCHAR(16),                           -- offline/nearRealtime/realtime
    dsl_kind        VARCHAR(16)     NOT NULL DEFAULT 'field', -- field/time/mapping/portraitTag/apiLookup
    dsl             TEXT,                                  -- 维度 DSL JSON(dimension.dsl.v1)
    source_snapshot TEXT,                                  -- 来源快照 JSON
    supports        TEXT,                                  -- 能力声明 JSON
    owner_id        BIGINT,
    created_by      VARCHAR(64),
    updated_by      VARCHAR(64),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE dimension IS '业务维度(语义资产)';
COMMENT ON COLUMN dimension.dim_code IS '维度业务编码,全局唯一';
COMMENT ON COLUMN dimension.semantic_type IS '语义类型:geo/time/category';
COMMENT ON COLUMN dimension.dsl IS '维度 DSL JSON';

CREATE UNIQUE INDEX uk_dimension_code ON dimension (dim_code) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uk_dimension_name ON dimension (name) WHERE deleted_at IS NULL;
CREATE INDEX idx_dimension_status ON dimension (status) WHERE deleted_at IS NULL;
CREATE INDEX idx_dimension_folder ON dimension (folder) WHERE deleted_at IS NULL;
CREATE INDEX idx_dimension_source ON dimension (source_type, source_code) WHERE deleted_at IS NULL;

-- ============================================================================
-- 语义资产来源绑定(支持一个资产绑定多来源;主来源在 metric/dimension 中冗余)
-- ============================================================================
CREATE TABLE semantic_asset_binding (
    id              BIGINT          PRIMARY KEY,
    asset_type      VARCHAR(16)     NOT NULL,              -- metric / dimension
    asset_id        BIGINT          NOT NULL,              -- metric.id / dimension.id
    asset_code      VARCHAR(128)    NOT NULL,              -- metric_code / dim_code
    source_type     VARCHAR(32)     NOT NULL,
    source_code     VARCHAR(128)    NOT NULL,
    source_name     VARCHAR(128),
    query_mode      VARCHAR(16)     NOT NULL DEFAULT 'olap',
    freshness       VARCHAR(16),
    source_snapshot TEXT,                                  -- 来源快照 JSON
    field_mapping   TEXT,                                  -- 字段映射 JSON
    dsl_override    TEXT,                                  -- 当前来源下 DSL 覆盖
    is_primary      BOOLEAN         NOT NULL DEFAULT FALSE,
    status          VARCHAR(16)     NOT NULL DEFAULT 'draft',
    created_by      VARCHAR(64),
    updated_by      VARCHAR(64),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE semantic_asset_binding IS '语义资产与可绑定来源的多对多关系';
COMMENT ON COLUMN semantic_asset_binding.asset_type IS '资产类型:metric/dimension';
COMMENT ON COLUMN semantic_asset_binding.is_primary IS '是否主来源';

CREATE INDEX idx_sem_asset_binding_asset ON semantic_asset_binding (asset_type, asset_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_sem_asset_binding_source ON semantic_asset_binding (source_type, source_code) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uk_sem_asset_binding_unique ON semantic_asset_binding (asset_type, asset_id, source_type, source_code) WHERE deleted_at IS NULL;

-- ============================================================================
-- 指标-维度关联(描述指标可组合哪些维度)
-- ============================================================================
CREATE TABLE metric_dimension_binding (
    id              BIGINT          PRIMARY KEY,
    metric_id       BIGINT          NOT NULL,
    metric_code     VARCHAR(128)    NOT NULL,
    dimension_id    BIGINT,
    dimension_code  VARCHAR(128)    NOT NULL,
    dimension_name  VARCHAR(128)    NOT NULL,              -- 冗余,便于展示
    source_type     VARCHAR(32),                           -- 维度来源类型
    source_code     VARCHAR(128),                          -- 维度来源编码
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE metric_dimension_binding IS '指标可关联维度';
COMMENT ON COLUMN metric_dimension_binding.dimension_name IS '维度名称冗余';

CREATE INDEX idx_metric_dim_binding_metric ON metric_dimension_binding (metric_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_metric_dim_binding_dim ON metric_dimension_binding (dimension_id) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uk_metric_dim_binding_unique ON metric_dimension_binding (metric_id, dimension_code) WHERE deleted_at IS NULL;

-- ============================================================================
-- 指标×维度组合合法性
-- ============================================================================
CREATE TABLE metric_dimension_compat (
    metric_id       BIGINT      NOT NULL,
    dimension_id    BIGINT      NOT NULL,
    allowed         BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ,
    PRIMARY KEY (metric_id, dimension_id)
);
COMMENT ON TABLE metric_dimension_compat IS '指标×维度组合合法性';
COMMENT ON COLUMN metric_dimension_compat.allowed IS '是否允许组合';

CREATE INDEX idx_metric_dim_compat_dimension ON metric_dimension_compat (dimension_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_metric_dim_compat_allowed ON metric_dimension_compat (metric_id, dimension_id, allowed) WHERE deleted_at IS NULL;

-- ============================================================================
-- 指标版本(口径变更审计)
-- ============================================================================
CREATE TABLE metric_version (
    id         BIGINT       PRIMARY KEY,
    metric_id  BIGINT       NOT NULL,
    metric_code VARCHAR(128) NOT NULL,
    version    INT          NOT NULL,
    dsl        TEXT,
    source_snapshot TEXT,
    change_log TEXT,
    created_by VARCHAR(64),
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
-- 维度-数据集字段绑定(维度可跨数据集绑定字段)
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
COMMENT ON TABLE dimension_binding IS '维度-数据集字段绑定';
COMMENT ON COLUMN dimension_binding.expr IS '维度计算表达式(可空)';

CREATE INDEX idx_dimension_binding_dimension ON dimension_binding (dimension_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_dimension_binding_dataset ON dimension_binding (dataset_id) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uk_dimension_binding_dimension_dataset ON dimension_binding (dimension_id, dataset_id) WHERE deleted_at IS NULL;
