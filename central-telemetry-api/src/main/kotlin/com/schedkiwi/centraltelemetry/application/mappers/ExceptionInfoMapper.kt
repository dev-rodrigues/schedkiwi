package com.schedkiwi.centraltelemetry.application.mappers

import com.schedkiwi.centraltelemetry.domain.valueobjects.ExceptionInfo
import com.schedkiwi.centraltelemetry.infrastructure.persistence.ExceptionInfoEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.NullValuePropertyMappingStrategy
import org.mapstruct.ReportingPolicy

/**
 * Mapper para conversão entre entidades de domínio e entidades JPA de ExceptionInfo
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface ExceptionInfoMapper {
    
    /**
     * Converte entidade de domínio para entidade JPA
     */
    @Mapping(target = "execution", ignore = true)
    fun toEntity(domain: ExceptionInfo): ExceptionInfoEntity
    
    /**
     * Converte entidade JPA para entidade de domínio
     */
    fun toDomain(entity: ExceptionInfoEntity): ExceptionInfo
    
    /**
     * Atualiza entidade JPA com dados da entidade de domínio
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "execution", ignore = true)
    fun updateEntity(@MappingTarget entity: ExceptionInfoEntity, domain: ExceptionInfo): ExceptionInfoEntity
    
    /**
     * Lista de entidades de domínio para lista de entidades JPA
     */
    fun toEntityList(domainList: List<ExceptionInfo>): List<ExceptionInfoEntity>
    
    /**
     * Lista de entidades JPA para lista de entidades de domínio
     */
    fun toDomainList(entityList: List<ExceptionInfoEntity>): List<ExceptionInfo>
}
