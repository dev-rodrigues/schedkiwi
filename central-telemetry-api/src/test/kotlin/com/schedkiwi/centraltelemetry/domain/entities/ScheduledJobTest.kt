package com.schedkiwi.centraltelemetry.domain.entities

import com.schedkiwi.centraltelemetry.domain.valueobjects.ExecutionStatus
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Testes unitários para a entidade ScheduledJob
 */
class ScheduledJobTest {

    @Test
    fun `deve criar job com valores obrigatórios`() {
        // Given
        val jobId = "test-job"
        val methodName = "processData"
        val className = "com.example.Service"
        val applicationId = UUID.randomUUID()

        // When
        val job = ScheduledJob(
            jobId = jobId,
            methodName = methodName,
            className = className,
            applicationId = applicationId
        )

        // Then
        assertEquals(jobId, job.jobId)
        assertEquals(methodName, job.methodName)
        assertEquals(className, job.className)
        assertEquals(applicationId, job.applicationId)
        assertNull(job.cronExpression)
        assertNull(job.fixedRate)
        assertNull(job.fixedDelay)
        assertEquals("MILLISECONDS", job.timeUnit)
        assertNull(job.description)
        assertTrue(job.id.toString().isNotEmpty())
        assertTrue(job.createdAt.isBefore(Instant.now().plusSeconds(1)))
        assertTrue(job.executions.isEmpty())
    }

    @Test
    fun `deve criar job com cron expression`() {
        // Given
        val jobId = "cron-job"
        val methodName = "scheduledTask"
        val className = "com.example.CronService"
        val cronExpression = "0 0 12 * * ?"
        val applicationId = UUID.randomUUID()

        // When
        val job = ScheduledJob(
            jobId = jobId,
            methodName = methodName,
            className = className,
            cronExpression = cronExpression,
            applicationId = applicationId
        )

        // Then
        assertEquals(cronExpression, job.cronExpression)
        assertNull(job.fixedRate)
        assertNull(job.fixedDelay)
    }

    @Test
    fun `deve criar job com fixed rate`() {
        // Given
        val jobId = "fixed-rate-job"
        val methodName = "periodicTask"
        val className = "com.example.PeriodicService"
        val fixedRate = 30000L
        val applicationId = UUID.randomUUID()

        // When
        val job = ScheduledJob(
            jobId = jobId,
            methodName = methodName,
            className = className,
            fixedRate = fixedRate,
            applicationId = applicationId
        )

        // Then
        assertEquals(fixedRate, job.fixedRate)
        assertNull(job.cronExpression)
        assertNull(job.fixedDelay)
    }

    @Test
    fun `deve criar job com fixed delay`() {
        // Given
        val jobId = "fixed-delay-job"
        val methodName = "delayedTask"
        val className = "com.example.DelayedService"
        val fixedDelay = 60000L
        val applicationId = UUID.randomUUID()

        // When
        val job = ScheduledJob(
            jobId = jobId,
            methodName = methodName,
            className = className,
            fixedDelay = fixedDelay,
            applicationId = applicationId
        )

        // Then
        assertEquals(fixedDelay, job.fixedDelay)
        assertNull(job.cronExpression)
        assertNull(job.fixedRate)
    }

    @Test
    fun `deve criar job com todos os campos opcionais`() {
        // Given
        val id = UUID.randomUUID()
        val jobId = "full-job"
        val methodName = "complexTask"
        val className = "com.example.ComplexService"
        val cronExpression = "0 */15 * * * *"
        val fixedRate = 45000L
        val fixedDelay = 90000L
        val timeUnit = "SECONDS"
        val description = "Job complexo de exemplo"
        val applicationId = UUID.randomUUID()
        val createdAt = Instant.now().minusSeconds(300)

        // When
        val job = ScheduledJob(
            id = id,
            jobId = jobId,
            methodName = methodName,
            className = className,
            cronExpression = cronExpression,
            fixedRate = fixedRate,
            fixedDelay = fixedDelay,
            timeUnit = timeUnit,
            description = description,
            applicationId = applicationId,
            createdAt = createdAt
        )

        // Then
        assertEquals(id, job.id)
        assertEquals(jobId, job.jobId)
        assertEquals(methodName, job.methodName)
        assertEquals(className, job.className)
        assertEquals(cronExpression, job.cronExpression)
        assertEquals(fixedRate, job.fixedRate)
        assertEquals(fixedDelay, job.fixedDelay)
        assertEquals(timeUnit, job.timeUnit)
        assertEquals(description, job.description)
        assertEquals(applicationId, job.applicationId)
        assertEquals(createdAt, job.createdAt)
    }

    @Test
    fun `deve aceitar diferentes time units`() {
        // Given
        val applicationId = UUID.randomUUID()

        // When & Then
        val job1 = ScheduledJob(
            jobId = "job-1",
            methodName = "task1",
            className = "Service1",
            timeUnit = "SECONDS",
            applicationId = applicationId
        )
        assertEquals("SECONDS", job1.timeUnit)

        val job2 = ScheduledJob(
            jobId = "job-2",
            methodName = "task2",
            className = "Service2",
            timeUnit = "MINUTES",
            applicationId = applicationId
        )
        assertEquals("MINUTES", job2.timeUnit)

        val job3 = ScheduledJob(
            jobId = "job-3",
            methodName = "task3",
            className = "Service3",
            timeUnit = "HOURS",
            applicationId = applicationId
        )
        assertEquals("HOURS", job3.timeUnit)
    }

    @Test
    fun `deve permitir description nula`() {
        // Given
        val applicationId = UUID.randomUUID()

        // When
        val job = ScheduledJob(
            jobId = "test-job",
            methodName = "processData",
            className = "com.example.Service",
            description = null,
            applicationId = applicationId
        )

        // Then
        assertNull(job.description)
    }

    @Test
    fun `deve permitir description com texto`() {
        // Given
        val applicationId = UUID.randomUUID()
        val description = "Job que processa dados importantes"

        // When
        val job = ScheduledJob(
            jobId = "test-job",
            methodName = "processData",
            className = "com.example.Service",
            description = description,
            applicationId = applicationId
        )

        // Then
        assertEquals(description, job.description)
    }

    @Test
    fun `deve manter lista mutável de execuções`() {
        // Given
        val applicationId = UUID.randomUUID()
        val job = ScheduledJob(
            jobId = "test-job",
            methodName = "processData",
            className = "com.example.Service",
            applicationId = applicationId
        )

        // When & Then - lista deve ser mutável
        assertTrue(job.executions is MutableList)
        assertTrue(job.executions.isEmpty())
    }

    @Test
    fun `deve adicionar execution através do método addExecution`() {
        // Given
        val applicationId = UUID.randomUUID()
        val job = ScheduledJob(
            jobId = "test-job",
            methodName = "processData",
            className = "com.example.Service",
            applicationId = applicationId
        )
        
        val execution = Execution(
            runId = "run-123",
            jobId = "test-job",
            appName = "test-app",
            status = ExecutionStatus.RUNNING,
            startTime = Instant.now(),
            applicationId = applicationId,
            scheduledJobId = job.id
        )

        // When
        job.addExecution(execution)

        // Then
        assertEquals(1, job.executions.size)
        assertEquals(execution, job.executions[0])
        assertEquals(job.id, job.executions[0].scheduledJobId)
    }

    @Test
    fun `deve adicionar múltiplas executions`() {
        // Given
        val applicationId = UUID.randomUUID()
        val job = ScheduledJob(
            jobId = "test-job",
            methodName = "processData",
            className = "com.example.Service",
            applicationId = applicationId
        )
        
        val execution1 = Execution(
            runId = "run-1",
            jobId = "test-job",
            appName = "test-app",
            status = ExecutionStatus.RUNNING,
            startTime = Instant.now().minusSeconds(60),
            applicationId = applicationId,
            scheduledJobId = job.id
        )
        
        val execution2 = Execution(
            runId = "run-2",
            jobId = "test-job",
            appName = "test-app",
            status = ExecutionStatus.COMPLETED,
            startTime = Instant.now().minusSeconds(30),
            endTime = Instant.now().minusSeconds(20),
            applicationId = applicationId,
            scheduledJobId = job.id
        )

        // When
        job.addExecution(execution1)
        job.addExecution(execution2)

        // Then
        assertEquals(2, job.executions.size)
        assertEquals("run-1", job.executions[0].runId)
        assertEquals("run-2", job.executions[1].runId)
        assertEquals(ExecutionStatus.RUNNING, job.executions[0].status)
        assertEquals(ExecutionStatus.COMPLETED, job.executions[1].status)
    }
}
