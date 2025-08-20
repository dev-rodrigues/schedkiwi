package com.schedkiwi.centraltelemetry.application.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size

/**
 * DTO para atualização de progresso
 */
data class ProgressUpdateDto(
    @field:NotBlank(message = "RunId é obrigatório")
    val runId: String,
    
    @field:NotNull(message = "Número de sequência é obrigatório")
    @field:PositiveOrZero(message = "Número de sequência deve ser zero ou positivo")
    val sequenceNumber: Long,
    
    @field:NotNull(message = "Itens atuais é obrigatório")
    @field:PositiveOrZero(message = "Itens atuais deve ser zero ou positivo")
    val currentItems: Long,
    
    @field:NotNull(message = "Total de itens é obrigatório")
    @field:PositiveOrZero(message = "Total de itens deve ser zero ou positivo")
    val totalItems: Long,
    
    @field:Size(max = 1000, message = "Mensagem de status deve ter no máximo 1000 caracteres")
    val statusMessage: String? = null
)
