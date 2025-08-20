package com.schedkiwi.centraltelemetry.domain.entities

import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Testes unitários para a entidade Application
 */
class ApplicationTest {

    @Test
    fun `deve criar aplicação com valores obrigatórios`() {
        // Given
        val appName = "test-app"
        val host = "localhost"
        val port = 8080

        // When
        val application = Application(
            appName = appName,
            host = host,
            port = port
        )

        // Then
        assertEquals(appName, application.appName)
        assertEquals(host, application.host)
        assertEquals(port, application.port)
        assertTrue(application.scheduledJobs.isEmpty())
        assertTrue(application.id.toString().isNotEmpty())
        assertTrue(application.createdAt.isBefore(Instant.now().plusSeconds(1)))
    }

    @Test
    fun `deve criar aplicação com todos os campos`() {
        // Given
        val id = UUID.randomUUID()
        val appName = "test-app"
        val host = "localhost"
        val port = 8080
        val createdAt = Instant.now().minusSeconds(60)
        val scheduledJobs = mutableListOf<ScheduledJob>()

        // When
        val application = Application(
            id = id,
            appName = appName,
            host = host,
            port = port,
            createdAt = createdAt,
            scheduledJobs = scheduledJobs
        )

        // Then
        assertEquals(id, application.id)
        assertEquals(appName, application.appName)
        assertEquals(host, application.host)
        assertEquals(port, application.port)
        assertEquals(createdAt, application.createdAt)
        assertEquals(scheduledJobs, application.scheduledJobs)
    }

    @Test
    fun `deve adicionar job agendado à aplicação`() {
        // Given
        val application = Application(
            appName = "test-app",
            host = "localhost",
            port = 8080
        )
        val job = ScheduledJob(
            jobId = "job-1",
            methodName = "processData",
            className = "com.example.Service",
            cronExpression = "0 */5 * * * *",
            applicationId = application.id
        )

        // When
        application.addScheduledJob(job)

        // Then
        assertEquals(1, application.scheduledJobs.size)
        assertEquals(job, application.scheduledJobs.first())
    }

    @Test
    fun `deve adicionar múltiplos jobs agendados`() {
        // Given
        val application = Application(
            appName = "test-app",
            host = "localhost",
            port = 8080
        )
        val job1 = ScheduledJob(
            jobId = "job-1",
            methodName = "processData1",
            className = "com.example.Service1",
            cronExpression = "0 */5 * * * *",
            applicationId = application.id
        )
        val job2 = ScheduledJob(
            jobId = "job-2",
            methodName = "processData2",
            className = "com.example.Service2",
            fixedRate = 30000L,
            applicationId = application.id
        )

        // When
        application.addScheduledJob(job1)
        application.addScheduledJob(job2)

        // Then
        assertEquals(2, application.scheduledJobs.size)
        assertTrue(application.scheduledJobs.contains(job1))
        assertTrue(application.scheduledJobs.contains(job2))
    }

    @Test
    fun `deve validar porta válida`() {
        // Given & When & Then - não deve lançar exceção
        val application1 = Application(
            appName = "test-app",
            host = "localhost",
            port = 1
        )
        assertEquals(1, application1.port)

        val application2 = Application(
            appName = "test-app",
            host = "localhost",
            port = 65535
        )
        assertEquals(65535, application2.port)

        val application3 = Application(
            appName = "test-app",
            host = "localhost",
            port = 8080
        )
        assertEquals(8080, application3.port)
    }

    @Test
    fun `deve aceitar diferentes tipos de host`() {
        // Given & When & Then
        val app1 = Application(
            appName = "test-app",
            host = "localhost",
            port = 8080
        )
        assertEquals("localhost", app1.host)

        val app2 = Application(
            appName = "test-app",
            host = "192.168.1.100",
            port = 8080
        )
        assertEquals("192.168.1.100", app2.host)

        val app3 = Application(
            appName = "test-app",
            host = "api.example.com",
            port = 8080
        )
        assertEquals("api.example.com", app3.host)
    }

    @Test
    fun `deve manter lista mutável de jobs`() {
        // Given
        val application = Application(
            appName = "test-app",
            host = "localhost",
            port = 8080
        )

        // When & Then - lista deve ser mutável
        assertTrue(application.scheduledJobs is MutableList)
        
        val job = ScheduledJob(
            jobId = "job-1",
            methodName = "processData",
            className = "com.example.Service",
            cronExpression = "0 */5 * * * *",
            applicationId = application.id
        )
        
        application.scheduledJobs.add(job)
        assertEquals(1, application.scheduledJobs.size)
    }

    @Test
    fun `deve permitir modificação de jobs através do método addScheduledJob`() {
        // Given
        val application = Application(
            appName = "test-app",
            host = "localhost",
            port = 8080
        )
        val initialSize = application.scheduledJobs.size

        // When
        val job = ScheduledJob(
            jobId = "job-1",
            methodName = "processData",
            className = "com.example.Service",
            cronExpression = "0 */5 * * * *",
            applicationId = application.id
        )
        application.addScheduledJob(job)

        // Then
        assertEquals(initialSize + 1, application.scheduledJobs.size)
        assertEquals(job, application.scheduledJobs.last())
    }

    @Test
    fun `deve preservar ordem de adição dos jobs`() {
        // Given
        val application = Application(
            appName = "test-app",
            host = "localhost",
            port = 8080
        )

        val job1 = ScheduledJob(
            jobId = "job-1",
            methodName = "processData1",
            className = "com.example.Service1",
            cronExpression = "0 */5 * * * *",
            applicationId = application.id
        )
        val job2 = ScheduledJob(
            jobId = "job-2",
            methodName = "processData2",
            className = "com.example.Service2",
            fixedRate = 30000L,
            applicationId = application.id
        )
        val job3 = ScheduledJob(
            jobId = "job-3",
            methodName = "processData3",
            className = "com.example.Service3",
            fixedDelay = 60000L,
            applicationId = application.id
        )

        // When
        application.addScheduledJob(job1)
        application.addScheduledJob(job2)
        application.addScheduledJob(job3)

        // Then
        assertEquals(3, application.scheduledJobs.size)
        assertEquals(job1, application.scheduledJobs[0])
        assertEquals(job2, application.scheduledJobs[1])
        assertEquals(job3, application.scheduledJobs[2])
    }
}
