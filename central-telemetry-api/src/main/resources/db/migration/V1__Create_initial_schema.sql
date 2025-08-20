-- =====================================================
-- Script de Migração V1: Esquema Inicial
-- API Central de Telemetria - PostgreSQL
-- =====================================================

-- Habilitar extensão UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- 1. TABELA: applications (aplicações integradas)
-- =====================================================
CREATE TABLE applications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    app_name VARCHAR(255) NOT NULL UNIQUE,
    host VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL,
    environment VARCHAR(50) NOT NULL DEFAULT 'production',
    version VARCHAR(50) NOT NULL DEFAULT '1.0.0',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_heartbeat TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    -- Índices para performance
    CONSTRAINT uk_applications_host_port UNIQUE (host, port),
    CONSTRAINT uk_applications_app_name UNIQUE (app_name)
);

CREATE INDEX idx_applications_environment ON applications(environment);
CREATE INDEX idx_applications_is_active ON applications(is_active);
CREATE INDEX idx_applications_last_heartbeat ON applications(last_heartbeat);

-- =====================================================
-- 2. TABELA: scheduled_jobs (jobs agendados)
-- =====================================================
CREATE TABLE scheduled_jobs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_id VARCHAR(255) NOT NULL,
    method_name VARCHAR(255) NOT NULL,
    class_name VARCHAR(255) NOT NULL,
    cron_expression VARCHAR(100),
    fixed_rate BIGINT,
    fixed_delay BIGINT,
    time_unit VARCHAR(20) NOT NULL DEFAULT 'MILLISECONDS',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    application_id UUID NOT NULL,
    
    -- Relacionamentos
    CONSTRAINT fk_scheduled_jobs_application 
        FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE,
    
    -- Índices para performance
    CONSTRAINT uk_scheduled_jobs_app_job UNIQUE (application_id, job_id)
);

CREATE INDEX idx_scheduled_jobs_application_id ON scheduled_jobs(application_id);
CREATE INDEX idx_scheduled_jobs_job_id ON scheduled_jobs(job_id);

-- =====================================================
-- 3. TABELA: executions (execuções de schedulers)
-- =====================================================
CREATE TABLE executions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    run_id VARCHAR(255) NOT NULL UNIQUE,
    job_id VARCHAR(255) NOT NULL,
    app_name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('RUNNING', 'COMPLETED', 'FAILED', 'PAUSED')),
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    planned_total BIGINT NOT NULL DEFAULT 0,
    processed_items BIGINT NOT NULL DEFAULT 0,
    failed_items BIGINT NOT NULL DEFAULT 0,
    skipped_items BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    application_id UUID NOT NULL,
    scheduled_job_id UUID NOT NULL,
    
    -- Relacionamentos
    CONSTRAINT fk_executions_application 
        FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE,
    CONSTRAINT fk_executions_scheduled_job 
        FOREIGN KEY (scheduled_job_id) REFERENCES scheduled_jobs(id) ON DELETE CASCADE,
    
    -- Índices para performance
    CONSTRAINT uk_executions_run_id UNIQUE (run_id)
);

CREATE INDEX idx_executions_run_id ON executions(run_id);
CREATE INDEX idx_executions_job_id ON executions(job_id);
CREATE INDEX idx_executions_application_id ON executions(application_id);
CREATE INDEX idx_executions_scheduled_job_id ON executions(scheduled_job_id);
CREATE INDEX idx_executions_status ON executions(status);
CREATE INDEX idx_executions_start_time ON executions(start_time);
CREATE INDEX idx_executions_processed_items ON executions(processed_items);

-- =====================================================
-- 4. TABELA: execution_general_metadata (metadados gerais)
-- =====================================================
CREATE TABLE execution_general_metadata (
    execution_id UUID NOT NULL,
    metadata_key VARCHAR(255) NOT NULL,
    metadata_value TEXT,
    
    -- Relacionamentos
    CONSTRAINT fk_execution_metadata_execution 
        FOREIGN KEY (execution_id) REFERENCES executions(id) ON DELETE CASCADE,
    
    -- Chave primária composta
    CONSTRAINT pk_execution_metadata PRIMARY KEY (execution_id, metadata_key)
);

CREATE INDEX idx_execution_metadata_execution_id ON execution_general_metadata(execution_id);

-- =====================================================
-- 5. TABELA: item_metadata (metadados de itens processados)
-- =====================================================
CREATE TABLE item_metadata (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    item_key VARCHAR(255),
    metadata TEXT NOT NULL DEFAULT '{}',
    outcome VARCHAR(20) NOT NULL DEFAULT 'OK' CHECK (outcome IN ('OK', 'ERROR', 'SKIPPED')),
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    processing_time_ms BIGINT NOT NULL DEFAULT 0,
    error_message TEXT,
    stack_trace TEXT,
    execution_id UUID NOT NULL,
    
    -- Relacionamentos
    CONSTRAINT fk_item_metadata_execution 
        FOREIGN KEY (execution_id) REFERENCES executions(id) ON DELETE CASCADE
);

CREATE INDEX idx_item_metadata_execution_id ON item_metadata(execution_id);
CREATE INDEX idx_item_metadata_outcome ON item_metadata(outcome);
CREATE INDEX idx_item_metadata_processed_at ON item_metadata(processed_at);

-- =====================================================
-- 6. TABELA: execution_exceptions (exceções capturadas)
-- =====================================================
CREATE TABLE execution_exceptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    message TEXT NOT NULL,
    type VARCHAR(255) NOT NULL,
    stack_trace TEXT,
    captured_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    severity VARCHAR(20) NOT NULL DEFAULT 'HIGH' CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL', 'ERROR')),
    execution_id UUID NOT NULL,
    
    -- Relacionamentos
    CONSTRAINT fk_execution_exceptions_execution 
        FOREIGN KEY (execution_id) REFERENCES executions(id) ON DELETE CASCADE
);

CREATE INDEX idx_execution_exceptions_execution_id ON execution_exceptions(execution_id);
CREATE INDEX idx_execution_exceptions_severity ON execution_exceptions(severity);
CREATE INDEX idx_execution_exceptions_captured_at ON execution_exceptions(captured_at);

-- =====================================================
-- 7. TABELA: application_tokens (tokens de autenticação)
-- =====================================================
CREATE TABLE application_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    app_name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_used_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,
    
    -- Relacionamentos
    CONSTRAINT fk_application_tokens_app_name 
        FOREIGN KEY (app_name) REFERENCES applications(app_name) ON DELETE CASCADE
);

CREATE INDEX idx_application_tokens_app_name ON application_tokens(app_name);
CREATE INDEX idx_application_tokens_is_active ON application_tokens(is_active);
CREATE INDEX idx_application_tokens_expires_at ON application_tokens(expires_at);

-- =====================================================
-- 8. TABELA: execution_progress (progresso em tempo real)
-- =====================================================
CREATE TABLE execution_progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    execution_id UUID NOT NULL,
    sequence_number BIGINT NOT NULL,
    current_items BIGINT NOT NULL DEFAULT 0,
    total_items BIGINT NOT NULL DEFAULT 0,
    progress_percentage DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    status_message TEXT,
    captured_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    -- Relacionamentos
    CONSTRAINT fk_execution_progress_execution 
        FOREIGN KEY (execution_id) REFERENCES executions(id) ON DELETE CASCADE,
    
    -- Garantir ordem sequencial
    CONSTRAINT uk_execution_progress_sequence UNIQUE (execution_id, sequence_number)
);

CREATE INDEX idx_execution_progress_execution_id ON execution_progress(execution_id);
CREATE INDEX idx_execution_progress_sequence ON execution_progress(sequence_number);
CREATE INDEX idx_execution_progress_captured_at ON execution_progress(captured_at);

-- =====================================================
-- ÍNDICES ADICIONAIS PARA PERFORMANCE
-- =====================================================

-- Índices para consultas de métricas
CREATE INDEX idx_executions_status_start_time ON executions(status, start_time);
CREATE INDEX idx_executions_app_status ON executions(application_id, status);
CREATE INDEX idx_executions_job_status ON executions(scheduled_job_id, status);

-- Índices para consultas de relacionamento
CREATE INDEX idx_scheduled_jobs_app_created ON scheduled_jobs(application_id, created_at);
CREATE INDEX idx_item_metadata_exec_outcome ON item_metadata(execution_id, outcome);
CREATE INDEX idx_exceptions_exec_severity ON execution_exceptions(execution_id, severity);

-- =====================================================
-- COMENTÁRIOS DAS TABELAS
-- =====================================================
COMMENT ON TABLE applications IS 'Aplicações integradas que enviam telemetria';
COMMENT ON TABLE scheduled_jobs IS 'Jobs agendados de cada aplicação';
COMMENT ON TABLE executions IS 'Execuções de schedulers com métricas';
COMMENT ON TABLE execution_general_metadata IS 'Metadados gerais de cada execução';
COMMENT ON TABLE item_metadata IS 'Metadados de itens processados em cada execução';
COMMENT ON TABLE execution_exceptions IS 'Exceções capturadas durante execuções';
COMMENT ON TABLE application_tokens IS 'Tokens de autenticação para aplicações';
COMMENT ON TABLE execution_progress IS 'Progresso em tempo real das execuções';

-- =====================================================
-- FIM DO SCRIPT DE MIGRAÇÃO
-- =====================================================
