package com.datn.backend.mapper;

import com.datn.backend.dto.request.NhanVienRequestDTO;
import com.datn.backend.dto.response.NhanVienResponseDTO;
import com.datn.backend.entity.NhanVien;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface NhanVienMapper {

    @Mapping(target = "hinhAnh", ignore = true)
    @Mapping(target = "maNhanVien", ignore = true)
    @Mapping(target = "chucVu", ignore = true)
    NhanVien toEntity(NhanVienRequestDTO dto);

    @Mapping(source = "chucVu.maChucVu", target = "chucVu")
    NhanVienResponseDTO toResponseDTO(NhanVien entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "hinhAnh", ignore = true)
    @Mapping(target = "maNhanVien", ignore = true)
    @Mapping(target = "chucVu", ignore = true)
    void updateEntityFromDTO(NhanVienRequestDTO dto, @MappingTarget NhanVien entity);
}