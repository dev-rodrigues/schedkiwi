package com.schedkiwi.centraltelemetry.application.mappers

import com.schedkiwi.centraltelemetry.domain.entities.Execution
import com.schedkiwi.centraltelemetry.infrastructure.persistence.ExecutionEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.NullValuePropertyMappingStrategy
import org.mapstruct.ReportingPolicy

/**
 * Mapper para conversão entre entidades de domínio e entidades JPA de Execution
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = [ItemMetadataMapper::class, ExceptionInfoMapper::class]
)
interface ExecutionMapper {
    
    /**
     * Converte entidade de domínio para entidade JPA
     */
    @Mapping(target = "application", ignore = true)
    @Mapping(target = "scheduledJob", ignore = true)
    @Mapping(target = "itemMetadata", ignore = true)
    @Mapping(target = "exceptions", ignore = true)
    @Mapping(target = "generalMetadata", expression = "java(domain.getGeneralMetadata().entrySet().stream().collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, e -> e.getValue() != null ? e.getValue().toString() : null)))")
    fun toEntity(domain: Execution): ExecutionEntity
    
    /**
     * Converte entidade JPA para entidade de domínio
     */
    @Mapping(target = "applicationId", expression = "java(entity.getApplication() != null ? entity.getApplication().getId() : null)")
    @Mapping(target = "scheduledJobId", expression = "java(entity.getScheduledJob() != null ? entity.getScheduledJob().getId() : null)")
    @Mapping(target = "itemMetadata", source = "itemMetadata")
    @Mapping(target = "exceptions", source = "exceptions")
    @Mapping(target = "generalMetadata", expression = "java(entity.getGeneralMetadata().entrySet().stream().collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))")
    fun toDomain(entity: ExecutionEntity): Execution
    
    /**
     * Atualiza entidade JPA com dados da entidade de domínio
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "application", ignore = true)
    @Mapping(target = "scheduledJob", ignore = true)
    @Mapping(target = "itemMetadata", ignore = true)
    @Mapping(target = "exceptions", ignore = true)
    @Mapping(target = "generalMetadata", expression = "java(domain.getGeneralMetadata().entrySet().stream().collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, e -> e.getValue() != null ? e.getValue().toString() : null)))")
    fun updateEntity(@MappingTarget entity: ExecutionEntity, domain: Execution): ExecutionEntity
    
    /**
     * Lista de entidades de domínio para lista de entidades JPA
     */
    fun toEntityList(domainList: List<Execution>): List<ExecutionEntity>
    
    /**
     * Lista de entidades JPA para lista de entidades de domínio
     */
    fun toDomainList(entityList: List<ExecutionEntity>): List<Execution>
}
