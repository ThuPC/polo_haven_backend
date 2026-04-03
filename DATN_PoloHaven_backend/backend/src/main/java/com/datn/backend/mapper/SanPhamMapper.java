package com.datn.backend.mapper;

import com.datn.backend.dto.request.SanPhamRequestDTO;
import com.datn.backend.dto.response.SanPhamResponseDTO;
import com.datn.backend.entity.SanPham;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SanPhamMapper {

    @Mapping(target = "chatLieu", ignore = true)
    @Mapping(target = "xuatXu", ignore = true)
    @Mapping(target = "thuongHieu", ignore = true)
    @Mapping(target = "maSanPham", ignore = true)
    SanPham toEntity(SanPhamRequestDTO dto);

    @Mapping(source = "chatLieu.tenChatLieu", target = "tenChatLieu")
    @Mapping(source = "xuatXu.noiXuatXu", target = "tenXuatXu")
    @Mapping(source = "thuongHieu.tenThuongHieu", target = "tenThuongHieu")
    @Mapping(source = "moTa", target = "moTa")
    SanPhamResponseDTO toResponseDTO(SanPham entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "chatLieu", ignore = true)
    @Mapping(target = "xuatXu", ignore = true)
    @Mapping(target = "thuongHieu", ignore = true)
    @Mapping(target = "maSanPham", ignore = true)

    void updateEntityFromRequestDTO(SanPhamRequestDTO dto, @MappingTarget SanPham entity);
}

