package com.datn.backend.mapper;

import com.datn.backend.dto.response.SanPhamTenResponseDTO;
import com.datn.backend.entity.SanPham;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SanPhamTenMapper {

    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "tenSanPham", target = "tenSanPham"),
            @Mapping(source = "moTa", target = "moTa")
    })
    SanPhamTenResponseDTO toSanPhamTenResponseDTO(SanPham sanPham);

    List<SanPhamTenResponseDTO> toSanPhamTenResponseDTOs(List<SanPham> sanPhamList);
}


