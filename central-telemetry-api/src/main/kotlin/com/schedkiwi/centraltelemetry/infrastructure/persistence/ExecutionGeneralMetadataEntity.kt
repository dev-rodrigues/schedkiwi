package com.schedkiwi.centraltelemetry.infrastructure.persistence

import jakarta.persistence.*

/**
 * Entidade JPA para persistência de metadados gerais de execução
 */
@Entity
@Table(name = "execution_general_metadata")
class ExecutionGeneralMetadataEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String = "",

    @Column(name = "metadata_key", nullable = false)
    val metadataKey: String,

    @Column(name = "metadata_value", columnDefinition = "TEXT")
    val metadataValue: String? = null
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", nullable = false)
    var execution: ExecutionEntity? = null
}
