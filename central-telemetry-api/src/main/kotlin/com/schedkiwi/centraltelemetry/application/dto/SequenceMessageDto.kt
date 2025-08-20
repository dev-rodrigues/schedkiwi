package com.schedkiwi.centraltelemetry.application.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size

/**
 * DTO para mensagem de sequência
 */
data class SequenceMessageDto(
    @field:NotBlank(message = "RunId é obrigatório")
    val runId: String,
    
    @field:NotNull(message = "Número de sequência é obrigatório")
    @field:PositiveOrZero(message = "Número de sequência deve ser zero ou positivo")
    val sequenceNumber: Long,
    
    @field:Size(max = 1000, message = "Mensagem deve ter no máximo 1000 caracteres")
    val message: String? = null
)
