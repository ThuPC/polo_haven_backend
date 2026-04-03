package com.datn.backend.mapper;

import com.datn.backend.dto.request.SanPhamVariantRequestDTO;
import com.datn.backend.dto.response.SanPhamVariantResponseDTO;
import com.datn.backend.entity.KichThuoc;
import com.datn.backend.entity.MauSac;
import com.datn.backend.entity.SanPhamVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;



        @Mapper(componentModel = "spring")
        public interface SanPhamVariantMapper {

            @Mapping(source = "sanPham.id", target = "sanPhamId")
            @Mapping(source = "sanPham.tenSanPham", target = "tenSanPham")
            @Mapping(source = "mauSac.tenMau", target = "mauSac")
            @Mapping(source = "kichCo.size", target = "kichCo")  // nếu kiểu String
            SanPhamVariantResponseDTO toResponseDTO(SanPhamVariant entity);

            default String map(String size) {
                return size;
            }
        }





