CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'DEVELOPER',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    repo_url VARCHAR(500),
    language VARCHAR(50) NOT NULL DEFAULT 'Java',
    owner_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE analysis_runs (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    triggered_by BIGINT REFERENCES users(id),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    source_path TEXT,
    commit_hash VARCHAR(40),
    branch VARCHAR(255),
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE static_issues (
    id BIGSERIAL PRIMARY KEY,
    analysis_run_id BIGINT NOT NULL REFERENCES analysis_runs(id),
    rule_id VARCHAR(10) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    confidence VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    file_name VARCHAR(500),
    line_number INTEGER,
    class_name VARCHAR(255),
    method_name VARCHAR(255),
    call_path TEXT,
    explanation TEXT,
    recommendation TEXT,
    trade_offs TEXT
);

CREATE TABLE runtime_metrics (
    id BIGSERIAL PRIMARY KEY,
    analysis_run_id BIGINT NOT NULL REFERENCES analysis_runs(id),
    metric_name VARCHAR(100) NOT NULL,
    metric_value DOUBLE PRECISION NOT NULL,
    unit VARCHAR(50),
    recorded_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE eco_scores (
    id BIGSERIAL PRIMARY KEY,
    analysis_run_id BIGINT NOT NULL UNIQUE REFERENCES analysis_runs(id),
    total_score INTEGER NOT NULL,
    cpu_score INTEGER,
    memory_score INTEGER,
    database_score INTEGER,
    network_score INTEGER,
    static_score INTEGER,
    computed_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE reports (
    id BIGSERIAL PRIMARY KEY,
    analysis_run_id BIGINT NOT NULL REFERENCES analysis_runs(id),
    format VARCHAR(20) NOT NULL DEFAULT 'JSON',
    content TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
