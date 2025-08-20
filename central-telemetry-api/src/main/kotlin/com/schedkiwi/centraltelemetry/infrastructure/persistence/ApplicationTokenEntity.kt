package com.schedkiwi.centraltelemetry.infrastructure.persistence

import jakarta.persistence.*
import java.time.Instant
import java.util.*

/**
 * Entidade JPA para persistência de tokens de aplicação
 */
@Entity
@Table(name = "application_tokens")
data class ApplicationTokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "token_hash", nullable = false, unique = true)
    val tokenHash: String,

    @Column(name = "app_name", nullable = false)
    val appName: String,

    @Column(name = "application_id", nullable = false)
    val applicationId: UUID,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "last_used_at")
    val lastUsedAt: Instant? = null,

    @Column(name = "expires_at")
    val expiresAt: Instant? = null,

    @Column(name = "created_by", nullable = false)
    val createdBy: String = "system"
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_name", referencedColumnName = "app_name", insertable = false, updatable = false)
    var application: ApplicationEntity? = null
}
