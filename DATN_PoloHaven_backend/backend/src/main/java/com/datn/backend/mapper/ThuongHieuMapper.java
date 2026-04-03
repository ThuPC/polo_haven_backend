package com.datn.backend.mapper;

import com.datn.backend.dto.request.ThuongHieuRequestDTO;
import com.datn.backend.dto.response.ThuongHieuResponseDTO;
import com.datn.backend.entity.ThuongHieu;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ThuongHieuMapper {

    ThuongHieu toEntity(ThuongHieuRequestDTO dto);

    ThuongHieuResponseDTO toResponseDTO(ThuongHieu entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(ThuongHieuRequestDTO dto, @MappingTarget ThuongHieu entity);
}
