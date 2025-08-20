package com.schedkiwi.centraltelemetry.application.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

/**
 * DTO para registro de aplicação
 */
data class ApplicationRegistrationDto(
    @field:NotBlank(message = "Nome da aplicação é obrigatório")
    @field:Size(max = 255, message = "Nome da aplicação deve ter no máximo 255 caracteres")
    val appName: String,
    
    @field:NotBlank(message = "Host é obrigatório")
    @field:Size(max = 255, message = "Host deve ter no máximo 255 caracteres")
    val host: String,
    
    @field:NotNull(message = "Porta é obrigatória")
    @field:Positive(message = "Porta deve ser um número positivo")
    val port: Int,
    
    @field:Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    val description: String? = null,
    
    @field:Size(max = 50, message = "Versão deve ter no máximo 50 caracteres")
    val version: String? = null,
    
    @field:Size(max = 50, message = "Ambiente deve ter no máximo 50 caracteres")
    val environment: String? = null,
    
    @field:NotNull(message = "Jobs agendados são obrigatórios")
    @field:Size(min = 1, message = "Deve ter pelo menos um job agendado")
    val scheduledJobs: List<ScheduledJobDto>
)

/**
 * DTO para job agendado
 */
data class ScheduledJobDto(
    @field:NotBlank(message = "ID do job é obrigatório")
    @field:Size(max = 255, message = "ID do job deve ter no máximo 255 caracteres")
    val jobId: String,
    
    @field:NotBlank(message = "Nome do método é obrigatório")
    @field:Size(max = 255, message = "Nome do método deve ter no máximo 255 caracteres")
    val methodName: String,
    
    @field:NotBlank(message = "Nome da classe é obrigatório")
    @field:Size(max = 255, message = "Nome da classe deve ter no máximo 255 caracteres")
    val className: String,
    
    @field:Size(max = 100, message = "Expressão cron deve ter no máximo 100 caracteres")
    val cronExpression: String? = null,
    
    @field:Positive(message = "Fixed rate deve ser um número positivo")
    val fixedRate: Long? = null,
    
    @field:Positive(message = "Fixed delay deve ser um número positivo")
    val fixedDelay: Long? = null,
    
    @field:Size(max = 20, message = "Unidade de tempo deve ter no máximo 20 caracteres")
    val timeUnit: String? = null,
    
    @field:Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    val description: String? = null
)
