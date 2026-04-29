package com.mstock.api.Mappers;

import com.mstock.api.DTO.BatchDTO;
import com.mstock.api.entities.Batch;
import com.mstock.api.payload.Request.BatchRequest;
import org.mapstruct.*;

import java.util.List;

/**
 * BatchMapper - MapStruct mapper for Batch entity and DTOs
 * Handles Entity ↔ DTO conversions with null-safety
 */
@Mapper(componentModel = "spring")
public interface BatchMapper {

    /**
     * Normalize empty strings to null before mapping
     */
    @BeforeMapping
    default void normalizeRequest(BatchRequest request) {
        if (request.getLotNumber() != null && request.getLotNumber().trim().isEmpty()) {
            request.setLotNumber(null);
        }
        if (request.getLocation() != null && request.getLocation().trim().isEmpty()) {
            request.setLocation(null);
        }
    }

    /**
     * Update batch from request - ignore null values and relationships
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateBatchFromRequest(BatchRequest request, @MappingTarget Batch batch);

    /**
     * Convert Batch entity to BatchDTO
     */
    @BeanMapping(resultType = BatchDTO.class, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    BatchDTO toBatchDTO(Batch batch);

    /**
     * Convert list of Batch entities to list of BatchDTOs
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    List<BatchDTO> toBatchDTOList(List<Batch> batches);
}
