package com.datn.backend.mapper;

import com.datn.backend.dto.request.ChiTietSanPhamRequestDTO;
import com.datn.backend.dto.response.ChiTietSanPhamResponseDTO;
import com.datn.backend.entity.ChiTietSanPham;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ChiTietSanPhamMapper {

    @Mapping(source = "sanPham.id", target = "idSanPham")
    @Mapping(source = "mauSac.id", target = "idMauSac")
    @Mapping(source = "kichThuoc.id", target = "idKichThuoc")
    @Mapping(source = "hinhAnh.id", target = "idHinhAnh")
    ChiTietSanPhamRequestDTO toRequestDTO(ChiTietSanPham entity);

    @Mapping(source = "sanPham.tenSanPham", target = "tenSanPham")
    @Mapping(source = "mauSac.tenMau", target = "mauSac")
    @Mapping(source = "kichThuoc.size", target = "kichThuoc")
    @Mapping(source = "hinhAnh.tenAnh", target = "tenAnh")
    @Mapping(source = "sanPham.chatLieu.tenChatLieu", target = "chatLieu")
    @Mapping(source = "sanPham.thuongHieu.tenThuongHieu", target = "thuongHieu")
    @Mapping(source = "sanPham.xuatXu.noiXuatXu", target = "xuatXu")
    ChiTietSanPhamResponseDTO toResponseDTO(ChiTietSanPham entity);

    @Mapping(source = "idSanPham", target = "sanPham.id")
    @Mapping(source = "idMauSac", target = "mauSac.id")
    @Mapping(source = "idKichThuoc", target = "kichThuoc.id")
    ChiTietSanPham toEntity(ChiTietSanPhamRequestDTO dto);

    @AfterMapping
    default void setImageUrl(@MappingTarget ChiTietSanPhamResponseDTO dto, ChiTietSanPham entity) {
        if (entity.getHinhAnh() != null && entity.getHinhAnh().getTenAnh() != null) {
            String baseUrl = "http://localhost:8080/uploads/san_pham/";
            dto.setUrl(baseUrl + entity.getHinhAnh().getTenAnh());
        }
    }
}

