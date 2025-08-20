package com.schedkiwi.centraltelemetry.application.mappers

import com.schedkiwi.centraltelemetry.domain.valueobjects.ItemMetadata
import com.schedkiwi.centraltelemetry.infrastructure.persistence.ItemMetadataEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.NullValuePropertyMappingStrategy
import org.mapstruct.ReportingPolicy

/**
 * Mapper para conversão entre entidades de domínio e entidades JPA de ItemMetadata
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface ItemMetadataMapper {
    
    /**
     * Converte entidade de domínio para entidade JPA
     */
    @Mapping(target = "execution", ignore = true)
    fun toEntity(domain: ItemMetadata): ItemMetadataEntity
    
    /**
     * Converte entidade JPA para entidade de domínio
     */
    fun toDomain(entity: ItemMetadataEntity): ItemMetadata
    
    /**
     * Atualiza entidade JPA com dados da entidade de domínio
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "execution", ignore = true)
    fun updateEntity(@MappingTarget entity: ItemMetadataEntity, domain: ItemMetadata): ItemMetadataEntity
    
    /**
     * Lista de entidades de domínio para lista de entidades JPA
     */
    fun toEntityList(domainList: List<ItemMetadata>): List<ItemMetadataEntity>
    
    /**
     * Lista de entidades JPA para lista de entidades de domínio
     */
    fun toDomainList(entityList: List<ItemMetadataEntity>): List<ItemMetadata>
}
