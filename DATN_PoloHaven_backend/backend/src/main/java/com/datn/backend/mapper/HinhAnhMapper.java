package com.datn.backend.mapper;

import com.datn.backend.dto.request.HinhAnhRequestDTO;
import com.datn.backend.dto.response.HinhAnhResponseDTO;
import com.datn.backend.entity.HinhAnh;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface HinhAnhMapper {

    HinhAnh toEntity(HinhAnhRequestDTO dto);

    HinhAnhResponseDTO toResponseDTO(HinhAnh entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(HinhAnhRequestDTO dto, @MappingTarget HinhAnh entity);
}

