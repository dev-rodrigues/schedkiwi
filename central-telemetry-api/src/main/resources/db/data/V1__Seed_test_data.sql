-- =====================================================
-- Script de Dados de Seed: Dados de Teste
-- API Central de Telemetria - PostgreSQL
-- =====================================================

-- =====================================================
-- 1. APLICAÇÕES DE TESTE
-- =====================================================
INSERT INTO applications (id, app_name, host, port, environment, version, created_at, updated_at, is_active, last_heartbeat) VALUES
(
    '550e8400-e29b-41d4-a716-446655440001',
    'test-app-1',
    'localhost',
    8081,
    'development',
    '1.0.0',
    NOW() - INTERVAL '1 day',
    NOW(),
    true,
    NOW()
),
(
    '550e8400-e29b-41d4-a716-446655440002',
    'test-app-2',
    'localhost',
    8082,
    'staging',
    '2.1.0',
    NOW() - INTERVAL '2 days',
    NOW(),
    true,
    NOW() - INTERVAL '1 hour'
),
(
    '550e8400-e29b-41d4-a716-446655440003',
    'test-app-3',
    'localhost',
    8083,
    'production',
    '1.5.2',
    NOW() - INTERVAL '3 days',
    NOW(),
    false,
    NOW() - INTERVAL '1 day'
);

-- =====================================================
-- 2. JOBS AGENDADOS DE TESTE
-- =====================================================
INSERT INTO scheduled_jobs (id, job_id, method_name, class_name, cron_expression, fixed_rate, fixed_delay, time_unit, created_at, application_id) VALUES
(
    '660e8400-e29b-41d4-a716-446655440001',
    'data-processing-job',
    'processData',
    'DataProcessingScheduler',
    '0 */5 * * * ?',
    NULL,
    NULL,
    'MILLISECONDS',
    NOW() - INTERVAL '1 day',
    '550e8400-e29b-41d4-a716-446655440001'
),
(
    '660e8400-e29b-41d4-a716-446655440002',
    'cleanup-job',
    'cleanupOldData',
    'CleanupScheduler',
    '0 0 2 * * ?',
    NULL,
    NULL,
    'MILLISECONDS',
    NOW() - INTERVAL '1 day',
    '550e8400-e29b-41d4-a716-446655440001'
),
(
    '660e8400-e29b-41d4-a716-446655440003',
    'report-generation-job',
    'generateReport',
    'ReportScheduler',
    NULL,
    300000,
    NULL,
    'MILLISECONDS',
    NOW() - INTERVAL '2 days',
    '550e8400-e29b-41d4-a716-446655440002'
),
(
    '660e8400-e29b-41d4-a716-446655440004',
    'sync-job',
    'syncData',
    'SyncScheduler',
    NULL,
    NULL,
    60000,
    'MILLISECONDS',
    NOW() - INTERVAL '2 days',
    '550e8400-e29b-41d4-a716-446655440002'
);

-- =====================================================
-- 3. EXECUÇÕES DE TESTE
-- =====================================================
INSERT INTO executions (id, run_id, job_id, app_name, status, start_time, end_time, planned_total, processed_items, failed_items, skipped_items, created_at, application_id, scheduled_job_id) VALUES
(
    '770e8400-e29b-41d4-a716-446655440001',
    'run-001-2024-01-19',
    'data-processing-job',
    'test-app-1',
    'COMPLETED',
    NOW() - INTERVAL '2 hours',
    NOW() - INTERVAL '1 hour 45 minutes',
    1000,
    1000,
    0,
    0,
    NOW() - INTERVAL '2 hours',
    '550e8400-e29b-41d4-a716-446655440001',
    '660e8400-e29b-41d4-a716-446655440001'
),
(
    '770e8400-e29b-41d4-a716-446655440002',
    'run-002-2024-01-19',
    'cleanup-job',
    'test-app-1',
    'COMPLETED',
    NOW() - INTERVAL '1 hour',
    NOW() - INTERVAL '30 minutes',
    500,
    500,
    0,
    0,
    NOW() - INTERVAL '1 hour',
    '550e8400-e29b-41d4-a716-446655440001',
    '660e8400-e29b-41d4-a716-446655440002'
),
(
    '770e8400-e29b-41d4-a716-446655440003',
    'run-003-2024-01-19',
    'report-generation-job',
    'test-app-2',
    'RUNNING',
    NOW() - INTERVAL '30 minutes',
    NULL,
    200,
    150,
    0,
    0,
    NOW() - INTERVAL '30 minutes',
    '550e8400-e29b-41d4-a716-446655440002',
    '660e8400-e29b-41d4-a716-446655440003'
),
(
    '770e8400-e29b-41d4-a716-446655440004',
    'run-004-2024-01-19',
    'sync-job',
    'test-app-2',
    'FAILED',
    NOW() - INTERVAL '15 minutes',
    NOW() - INTERVAL '10 minutes',
    100,
    25,
    75,
    0,
    NOW() - INTERVAL '15 minutes',
    '550e8400-e29b-41d4-a716-446655440002',
    '660e8400-e29b-41d4-a716-446655440004'
);

-- =====================================================
-- 4. METADADOS GERAIS DE EXECUÇÃO
-- =====================================================
INSERT INTO execution_general_metadata (execution_id, metadata_key, metadata_value) VALUES
('770e8400-e29b-41d4-a716-446655440001', 'batch_size', '100'),
('770e8400-e29b-41d4-a716-446655440001', 'memory_usage_mb', '512'),
('770e8400-e29b-41d4-a716-446655440001', 'cpu_usage_percent', '45.2'),
('770e8400-e29b-41d4-a716-446655440002', 'files_deleted', '150'),
('770e8400-e29b-41d4-a716-446655440002', 'space_freed_mb', '2048'),
('770e8400-e29b-41d4-a716-446655440003', 'current_batch', '3'),
('770e8400-e29b-41d4-a716-446655440003', 'estimated_completion', '2024-01-19T12:00:00Z'),
('770e8400-e29b-41d4-a716-446655440004', 'error_context', 'Database connection timeout'),
('770e8400-e29b-41d4-a716-446655440004', 'retry_count', '3');

-- =====================================================
-- 5. METADADOS DE ITENS PROCESSADOS
-- =====================================================
INSERT INTO item_metadata (id, item_key, metadata, outcome, processed_at, processing_time_ms, error_message, stack_trace, execution_id) VALUES
(
    '880e8400-e29b-41d4-a716-446655440001',
    'user-001',
    '{"user_id": "001", "data_type": "profile", "size_bytes": 1024}',
    'OK',
    NOW() - INTERVAL '1 hour 50 minutes',
    150,
    NULL,
    NULL,
    '770e8400-e29b-41d4-a716-446655440001'
),
(
    '880e8400-e29b-41d4-a716-446655440002',
    'user-002',
    '{"user_id": "002", "data_type": "profile", "size_bytes": 2048}',
    'OK',
    NOW() - INTERVAL '1 hour 49 minutes',
    200,
    NULL,
    NULL,
    '770e8400-e29b-41d4-a716-446655440001'
),
(
    '880e8400-e29b-41d4-a716-446655440003',
    'file-001',
    '{"file_path": "/tmp/old-data-1.log", "size_bytes": 1048576}',
    'OK',
    NOW() - INTERVAL '45 minutes',
    50,
    NULL,
    NULL,
    '770e8400-e29b-41d4-a716-446655440002'
),
(
    '880e8400-e29b-41d4-a716-446655440004',
    'report-section-1',
    '{"section": "summary", "rows": 50, "columns": 10}',
    'OK',
    NOW() - INTERVAL '25 minutes',
    300,
    NULL,
    NULL,
    '770e8400-e29b-41d4-a716-446655440003'
),
(
    '880e8400-e29b-41d4-a716-446655440005',
    'sync-record-001',
    '{"table": "users", "record_id": "001", "operation": "update"}',
    'ERROR',
    NOW() - INTERVAL '12 minutes',
    5000,
    'Database connection timeout after 5 seconds',
    'java.sql.SQLTimeoutException: Connection timeout...',
    '770e8400-e29b-41d4-a716-446655440004'
);

-- =====================================================
-- 6. EXCEÇÕES CAPTURADAS
-- =====================================================
INSERT INTO execution_exceptions (id, message, type, stack_trace, captured_at, severity, execution_id) VALUES
(
    '990e8400-e29b-41d4-a716-446655440001',
    'Database connection timeout after 5 seconds',
    'java.sql.SQLTimeoutException',
    'java.sql.SQLTimeoutException: Connection timeout\n  at com.zaxxer.hikari.pool.HikariPool.getConnection(HikariPool.java:200)\n  at com.schedkiwi.sync.SyncService.syncData(SyncService.kt:45)',
    NOW() - INTERVAL '12 minutes',
    'HIGH',
    '770e8400-e29b-41d4-a716-446655440004'
),
(
    '990e8400-e29b-41d4-a716-446655440002',
    'Invalid data format for user record',
    'java.lang.IllegalArgumentException',
    'java.lang.IllegalArgumentException: Invalid user data format\n  at com.schedkiwi.sync.UserMapper.map(UserMapper.kt:23)',
    NOW() - INTERVAL '11 minutes',
    'MEDIUM',
    '770e8400-e29b-41d4-a716-446655440004'
),
(
    '990e8400-e29b-41d4-a716-446655440003',
    'Network connectivity issue',
    'java.net.ConnectException',
    'java.net.ConnectException: Connection refused\n  at java.net.PlainSocketImpl.socketConnect(Native Method)',
    NOW() - INTERVAL '10 minutes',
    'CRITICAL',
    '770e8400-e29b-41d4-a716-446655440004'
);

-- =====================================================
-- 7. TOKENS DE APLICAÇÃO DE TESTE
-- =====================================================
INSERT INTO application_tokens (id, token_hash, app_name, description, is_active, created_at, last_used_at, expires_at) VALUES
(
    'aa0e8400-e29b-41d4-a716-446655440001',
    'sha256:test-token-1-hash',
    'test-app-1',
    'Token de desenvolvimento para test-app-1',
    true,
    NOW() - INTERVAL '1 day',
    NOW() - INTERVAL '1 hour',
    NOW() + INTERVAL '30 days'
),
(
    'aa0e8400-e29b-41d4-a716-446655440002',
    'sha256:test-token-2-hash',
    'test-app-2',
    'Token de staging para test-app-2',
    true,
    NOW() - INTERVAL '2 days',
    NOW() - INTERVAL '30 minutes',
    NOW() + INTERVAL '60 days'
),
(
    'aa0e8400-e29b-41d4-a716-446655440003',
    'sha256:test-token-3-hash',
    'test-app-3',
    'Token de produção para test-app-3 (inativo)',
    false,
    NOW() - INTERVAL '3 days',
    NOW() - INTERVAL '2 days',
    NOW() - INTERVAL '1 day'
);

-- =====================================================
-- 8. PROGRESSO EM TEMPO REAL
-- =====================================================
INSERT INTO execution_progress (id, execution_id, sequence_number, current_items, total_items, progress_percentage, status_message, captured_at) VALUES
(
    'bb0e8400-e29b-41d4-a716-446655440001',
    '770e8400-e29b-41d4-a716-446655440003',
    1,
    50,
    200,
    25.00,
    'Processando primeira etapa do relatório',
    NOW() - INTERVAL '25 minutes'
),
(
    'bb0e8400-e29b-41d4-a716-446655440002',
    '770e8400-e29b-41d4-a716-446655440003',
    2,
    100,
    200,
    50.00,
    'Processando segunda etapa do relatório',
    NOW() - INTERVAL '20 minutes'
),
(
    'bb0e8400-e29b-41d4-a716-446655440003',
    '770e8400-e29b-41d4-a716-446655440003',
    3,
    150,
    200,
    75.00,
    'Processando terceira etapa do relatório',
    NOW() - INTERVAL '15 minutes'
);

-- =====================================================
-- FIM DO SCRIPT DE SEED
-- =====================================================
