package com.datn.backend.mapper;

import com.datn.backend.dto.response.ChiTietSanPhamHoaDonResponseDto;
import com.datn.backend.entity.ChiTietSanPham;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChiTietSanPhamHoaDonMapper {
    //       @Mapping(source = "nhanVien.tenNhanVien", target = "tenNhanVien")
    @Mapping(source = "hinhAnh.tenAnh", target = "tenAnh")
    ChiTietSanPhamHoaDonResponseDto toResponseDTO(ChiTietSanPham chiTietSanPham);
}
