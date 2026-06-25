-- 指标平台完整数据库 schema（PostgreSQL）
-- 默认 schema: stargaze_metric

-- 指标主表
CREATE TABLE IF NOT EXISTS metric (
    id              BIGSERIAL PRIMARY KEY,
    workspace_id    BIGINT NOT NULL,
    name            VARCHAR(256) NOT NULL,
    code            VARCHAR(128),
    business_name   VARCHAR(256),
    description     TEXT,
    folder          VARCHAR(128),
    format          VARCHAR(32),
    type            VARCHAR(32) NOT NULL,
    measure_kind    VARCHAR(32) NOT NULL,
    expression      TEXT,
    dsl             TEXT,
    caliber         TEXT,
    primary_dataset_id BIGINT,
    owner_id        BIGINT,
    status          VARCHAR(32) NOT NULL DEFAULT 'draft',
    version         INTEGER NOT NULL DEFAULT 1,
    created_by      BIGINT,
    updated_by      BIGINT,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT now(),
    deleted_at      TIMESTAMP WITH TIME ZONE
);

COMMENT ON TABLE metric IS '业务指标主表';
COMMENT ON COLUMN metric.code IS '指标标识（英文代码）';
COMMENT ON COLUMN metric.expression IS '计算表达式（对应原 DSL）';
COMMENT ON COLUMN metric.dsl IS '兼容旧字段的指标 DSL';
COMMENT ON COLUMN metric.caliber IS '兼容旧字段的口径说明';
COMMENT ON COLUMN metric.primary_dataset_id IS '主数据集 ID';
COMMENT ON COLUMN metric.status IS '状态: draft/published/offline/deprecated';

CREATE UNIQUE INDEX IF NOT EXISTS uk_metric_workspace_name ON metric(workspace_id, name) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_metric_workspace_code ON metric(workspace_id, code) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_metric_workspace_status ON metric(workspace_id, status) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_metric_folder ON metric(folder) WHERE deleted_at IS NULL;

-- 指标版本快照表
CREATE TABLE IF NOT EXISTS metric_version (
    id              BIGSERIAL PRIMARY KEY,
    metric_id       BIGINT NOT NULL,
    version         INTEGER NOT NULL,
    dsl             TEXT,
    caliber         TEXT,
    change_log      VARCHAR(512),
    created_by      BIGINT,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT now(),
    deleted_at      TIMESTAMP WITH TIME ZONE
);

COMMENT ON TABLE metric_version IS '指标版本快照';

CREATE INDEX IF NOT EXISTS idx_metric_version_metric ON metric_version(metric_id);

-- 指标-数据集字段绑定表
CREATE TABLE IF NOT EXISTS metric_binding (
    id              BIGSERIAL PRIMARY KEY,
    metric_id       BIGINT NOT NULL,
    dataset_id      BIGINT NOT NULL,
    field_id        BIGINT,
    is_primary      BOOLEAN DEFAULT FALSE,
    dsl_override    TEXT,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT now(),
    deleted_at      TIMESTAMP WITH TIME ZONE
);

COMMENT ON TABLE metric_binding IS '指标与数据集字段绑定关系';
COMMENT ON COLUMN metric_binding.is_primary IS '是否主数据集';
COMMENT ON COLUMN metric_binding.dsl_override IS '当前数据集下的 DSL 覆盖';

CREATE INDEX IF NOT EXISTS idx_metric_binding_metric ON metric_binding(metric_id);
CREATE INDEX IF NOT EXISTS idx_metric_binding_dataset ON metric_binding(dataset_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_metric_binding_metric_dataset ON metric_binding(metric_id, dataset_id) WHERE deleted_at IS NULL;

-- 指标-维度绑定表
CREATE TABLE IF NOT EXISTS metric_dimension_binding (
    id              BIGSERIAL PRIMARY KEY,
    metric_id       BIGINT NOT NULL,
    dimension_name  VARCHAR(256) NOT NULL,
    dataset_id      BIGINT,
    field_id        BIGINT,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT now(),
    deleted_at      TIMESTAMP WITH TIME ZONE
);

COMMENT ON TABLE metric_dimension_binding IS '指标绑定的维度字段';

CREATE INDEX IF NOT EXISTS idx_metric_dim_binding_metric ON metric_dimension_binding(metric_id);

-- 指标×维度组合合法性表
CREATE TABLE IF NOT EXISTS metric_dimension_compat (
    metric_id       BIGINT NOT NULL,
    dimension_id    BIGINT NOT NULL,
    allowed         BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT now(),
    deleted_at      TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (metric_id, dimension_id)
);

COMMENT ON TABLE metric_dimension_compat IS '指标与维度组合是否允许';

CREATE INDEX IF NOT EXISTS idx_metric_dim_compat_dimension ON metric_dimension_compat(dimension_id);

-- 维度主表
CREATE TABLE IF NOT EXISTS dimension (
    id              BIGSERIAL PRIMARY KEY,
    workspace_id    BIGINT NOT NULL,
    name            VARCHAR(256) NOT NULL,
    business_name   VARCHAR(256),
    semantic_type   VARCHAR(32),
    dictionary_id   BIGINT,
    format          VARCHAR(32),
    owner_id        BIGINT,
    status          VARCHAR(32) NOT NULL DEFAULT 'draft',
    created_by      BIGINT,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT now(),
    deleted_at      TIMESTAMP WITH TIME ZONE
);

COMMENT ON TABLE dimension IS '维度主表';

CREATE UNIQUE INDEX IF NOT EXISTS uk_dimension_workspace_name ON dimension(workspace_id, name) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_dimension_workspace_status ON dimension(workspace_id, status) WHERE deleted_at IS NULL;

-- 维度-数据集字段绑定表
CREATE TABLE IF NOT EXISTS dimension_binding (
    id              BIGSERIAL PRIMARY KEY,
    dimension_id    BIGINT NOT NULL,
    dataset_id      BIGINT NOT NULL,
    field_id        BIGINT NOT NULL,
    expr            TEXT,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT now(),
    deleted_at      TIMESTAMP WITH TIME ZONE
);

COMMENT ON TABLE dimension_binding IS '维度与数据集字段绑定关系';

CREATE INDEX IF NOT EXISTS idx_dimension_binding_dimension ON dimension_binding(dimension_id);
CREATE INDEX IF NOT EXISTS idx_dimension_binding_dataset ON dimension_binding(dataset_id);
