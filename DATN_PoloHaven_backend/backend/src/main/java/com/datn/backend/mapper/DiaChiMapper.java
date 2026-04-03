
package com.datn.backend.mapper;

import com.datn.backend.dto.request.DiaChiRequestDTO;
import com.datn.backend.dto.request.DiaChi_BanHangRequestDTO;
import com.datn.backend.dto.response.DiaChiResponseDTO;
import com.datn.backend.entity.DiaChi;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DiaChiMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "khachHang.id", source = "khachHangId")
    DiaChi toEntity(DiaChiRequestDTO request, String khachHangId);

    @Mapping(source = "khachHang.id", target = "khachHangId")
    default DiaChiResponseDTO toResponse(DiaChi entity) {
        DiaChiResponseDTO dto = new DiaChiResponseDTO();
        dto.setId(entity.getId());
        dto.setMaDiaChi(entity.getMaDiaChi());
        dto.setThanhPho(entity.getThanhPho());
        dto.setQuanHuyen(entity.getQuanHuyen());
        dto.setXaPhuong(entity.getXaPhuong());
        dto.setSoNha(entity.getSoNha());
        dto.setDiaChiChiTiet(entity.getDiaChiChiTiet());
        dto.setTenNguoiNhan(entity.getTenNguoiNhan());
        dto.setSdt(entity.getSdt());
        dto.setTrangThai(entity.getTrangThai());
        dto.setIsDefault(entity.getIsDefault());
        dto.setKhachHangId(entity.getKhachHang() != null ? entity.getKhachHang().getId() : null);
        dto.setProvinceId(entity.getProvinceId());
        dto.setDistrictId(entity.getDistrictId());
        dto.setWardCode(entity.getWardCode());
        
        return dto;
    }

    @Mapping(target = "khachHang", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "provinceId", source = "provinceId")
    @Mapping(target = "districtId", source = "districtId")
    @Mapping(target = "wardCode", source = "wardCode")
    void updateEntityFromDTO(DiaChiRequestDTO dto, @MappingTarget DiaChi entity);

    @Mapping(source = "khachHang.id", target = "khachHangId")
    DiaChiRequestDTO mapDiaChiToDiaChiDTO(DiaChi diaChi);

    @Mapping(target = "id",source = "id")
    @Mapping(target = "tenNguoiNhan", source = "tenNguoiNhan")
    @Mapping(target = "sdt", source = "sdt")
    @Mapping(target = "thanhPho", source = "thanhPho")
    @Mapping(target = "quanHuyen", source = "quanHuyen")
    @Mapping(target = "xaPhuong", source = "xaPhuong")
    @Mapping(target = "soNha", source = "soNha")
    DiaChi_BanHangRequestDTO toBanHangDto(DiaChi diaChi);
    List<DiaChiResponseDTO> toResponseList(List<DiaChi> diaChiList);

}