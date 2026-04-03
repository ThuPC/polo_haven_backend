package com.datn.backend.mapper;

import com.datn.backend.dto.request.MauSacRequestDTO;
import com.datn.backend.dto.response.MauSacResponseDTO;
import com.datn.backend.entity.MauSac;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MauSacMapper {

    MauSac toEntity(MauSacRequestDTO dto);

    MauSacResponseDTO toResponseDTO(MauSac entity);

    List<MauSacResponseDTO> toResponseDTOList(List<MauSac> list);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "tenMau", source = "tenMau")
    @Mapping(target = "maMauSac", source = "maMauSac")
    @Mapping(target = "trangThai", source = "trangThai")
    void updateEntityFromDTO(MauSacRequestDTO dto, @MappingTarget MauSac entity);
}

