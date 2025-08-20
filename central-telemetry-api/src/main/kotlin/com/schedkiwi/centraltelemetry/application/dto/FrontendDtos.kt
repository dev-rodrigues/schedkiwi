package com.schedkiwi.centraltelemetry.application.dto

import java.time.Instant
import java.time.LocalDateTime
import java.util.*

// ============================================================================
// DTOs para Aplicações
// ============================================================================

/**
 * Resumo de uma aplicação para listagem
 */
data class ApplicationSummaryDto(
    val id: UUID,
    val appName: String,
    val host: String,
    val port: Int,
    val status: String,
    val lastSeen: Instant?,
    val totalJobs: Int,
    val totalExecutions: Int
)

/**
 * Detalhes completos de uma aplicação
 */
data class ApplicationDetailDto(
    val id: UUID,
    val appName: String,
    val host: String,
    val port: Int,
    val status: String,
    val lastSeen: Instant?,
    val createdAt: Instant,
    val scheduledJobs: List<JobSummaryDto>,
    val totalExecutions: Int,
    val successfulExecutions: Int,
    val failedExecutions: Int
)

/**
 * Status atual de uma aplicação
 */
data class ApplicationStatusDto(
    val appName: String,
    val status: String,
    val lastSeen: Instant?,
    val isActive: Boolean,
    val totalJobs: Int,
    val runningJobs: Int
)

// ============================================================================
// DTOs para Jobs
// ============================================================================

/**
 * Resumo de um job agendado
 */
data class JobSummaryDto(
    val jobId: String,
    val methodName: String,
    val className: String,
    val cronExpression: String,
    val description: String?
)

// ============================================================================
// DTOs para Execuções
// ============================================================================

/**
 * Resumo de uma execução para listagem
 */
data class ExecutionSummaryDto(
    val id: UUID,
    val runId: String,
    val jobId: String,
    val appName: String,
    val status: String,
    val startTime: Instant,
    val endTime: Instant?,
    val processedItems: Int,
    val failedItems: Int,
    val itemsPerSecond: Double?
)

/**
 * Detalhes completos de uma execução
 */
data class ExecutionDetailDto(
    val id: UUID,
    val runId: String,
    val jobId: String,
    val appName: String,
    val status: String,
    val startTime: Instant,
    val endTime: Instant?,
    val processedItems: Int,
    val failedItems: Int,
    val skippedItems: Int,
    val itemsPerSecond: Double?,
    val duration: Long?, // em milissegundos
    val progress: ExecutionProgressDto?
)

/**
 * Progresso detalhado de uma execução
 */
data class ExecutionProgressDto(
    val currentItem: Int,
    val totalItems: Int,
    val progressPercentage: Double,
    val processedItems: Int,
    val failedItems: Int,
    val itemsPerSecond: Double?
)

// ============================================================================
// DTOs para Itens e Exceções
// ============================================================================

/**
 * Detalhes de um item processado
 */
data class ItemDetailDto(
    val id: UUID,
    val itemId: String,
    val outcome: String,
    val processingTime: Long?, // em milissegundos
    val metadata: Map<String, String>?,
    val timestamp: Instant
)

/**
 * Detalhes de uma exceção capturada
 */
data class ExceptionDetailDto(
    val message: String,
    val stackTrace: String?,
    val timestamp: Instant,
    val itemId: String?
)

// ============================================================================
// DTOs para Métricas
// ============================================================================

/**
 * Métricas de uma aplicação específica
 */
data class ApplicationMetricsDto(
    val appName: String,
    val totalExecutions: Int,
    val successfulExecutions: Int,
    val failedExecutions: Int,
    val successRate: Double,
    val totalItemsProcessed: Int,
    val totalItemsFailed: Int,
    val averageItemsPerSecond: Double
)

/**
 * Métricas agregadas de todas as aplicações
 */
data class AggregatedMetricsDto(
    val totalApplications: Int,
    val totalExecutions: Int,
    val totalItemsProcessed: Int,
    val averageExecutionsPerApp: Double,
    val averageItemsPerExecution: Double
)

/**
 * Métricas de performance globais
 */
data class PerformanceMetricsDto(
    val averageItemsPerSecond: Double,
    val averageExecutionTime: Double, // em milissegundos
    val totalErrors: Int,
    val totalProcessed: Int,
    val errorRate: Double,
    val performanceScore: Double // 0.0 a 1.0
)
