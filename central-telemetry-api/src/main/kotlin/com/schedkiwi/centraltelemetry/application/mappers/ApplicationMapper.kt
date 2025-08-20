package com.schedkiwi.centraltelemetry.application.mappers

import com.schedkiwi.centraltelemetry.domain.entities.Application
import com.schedkiwi.centraltelemetry.infrastructure.persistence.ApplicationEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.NullValuePropertyMappingStrategy
import org.mapstruct.ReportingPolicy

/**
 * Mapper para conversão entre entidades de domínio e entidades JPA de Application
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface ApplicationMapper {
    
    /**
     * Converte entidade de domínio para entidade JPA
     */
    @Mapping(target = "scheduledJobs", ignore = true)
    fun toEntity(domain: Application): ApplicationEntity
    
    /**
     * Converte entidade JPA para entidade de domínio
     */
    @Mapping(target = "scheduledJobs", ignore = true)
    fun toDomain(entity: ApplicationEntity): Application
    
    /**
     * Atualiza entidade JPA com dados da entidade de domínio
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "scheduledJobs", ignore = true)
    fun updateEntity(@MappingTarget entity: ApplicationEntity, domain: Application): ApplicationEntity
    
    /**
     * Lista de entidades de domínio para lista de entidades JPA
     */
    fun toEntityList(domainList: List<Application>): List<ApplicationEntity>
    
    /**
     * Lista de entidades JPA para lista de entidades de domínio
     */
    fun toDomainList(entityList: List<ApplicationEntity>): List<Application>
}
