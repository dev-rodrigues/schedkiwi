package com.schedkiwi.centraltelemetry.application.usecases

import com.schedkiwi.centraltelemetry.application.dto.ApplicationRegistrationDto
import com.schedkiwi.centraltelemetry.domain.entities.Application
import com.schedkiwi.centraltelemetry.domain.entities.ScheduledJob
import com.schedkiwi.centraltelemetry.domain.ports.ApplicationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Use Case para registro de aplicações
 */
@Service
@Transactional
class RegisterApplicationUseCase(
    private val applicationRepository: ApplicationRepository
) {

    /**
     * Registra uma nova aplicação
     */
    fun execute(dto: ApplicationRegistrationDto): Application {
        // Verificar se já existe aplicação com este nome
        val existingApplication = applicationRepository.findByAppName(dto.appName)
        if (existingApplication != null) {
            throw IllegalArgumentException("Aplicação '${dto.appName}' já está registrada")
        }

        // Verificar se já existe aplicação com este host e porta
        val existingHostPort = applicationRepository.findByHostAndPort(dto.host, dto.port)
        if (existingHostPort != null) {
            throw IllegalArgumentException("Já existe uma aplicação rodando em ${dto.host}:${dto.port}")
        }

        // Criar entidade de domínio
        val application = Application(
            appName = dto.appName,
            host = dto.host,
            port = dto.port,
            environment = dto.environment ?: "production",
            version = dto.version ?: "1.0.0"
        )

        // Adicionar jobs agendados
        dto.scheduledJobs.forEach { jobDto ->
            val scheduledJob = ScheduledJob(
                jobId = jobDto.jobId,
                methodName = jobDto.methodName,
                className = jobDto.className,
                cronExpression = jobDto.cronExpression,
                fixedRate = jobDto.fixedRate,
                fixedDelay = jobDto.fixedDelay,
                timeUnit = jobDto.timeUnit ?: "MILLISECONDS",
                applicationId = application.id
            )
            application.addScheduledJob(scheduledJob)
        }

        // Salvar aplicação
        return applicationRepository.save(application)
    }
}
