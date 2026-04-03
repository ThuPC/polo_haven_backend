package com.datn.backend.mapper;

import com.datn.backend.dto.request.XuatXuRequestDTO;
import com.datn.backend.dto.response.XuatXuResponseDTO;
import com.datn.backend.entity.XuatXu;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface XuatXuMapper {

    XuatXu toEntity(XuatXuRequestDTO dto);

    XuatXuResponseDTO toResponseDTO(XuatXu entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(XuatXuRequestDTO dto, @MappingTarget XuatXu entity);
}
