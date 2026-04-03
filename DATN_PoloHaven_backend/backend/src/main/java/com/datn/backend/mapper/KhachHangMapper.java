package com.datn.backend.mapper;

import com.datn.backend.dto.request.DiaChiRequestDTO;
import com.datn.backend.dto.request.KhachHangRequestDTO;
import com.datn.backend.dto.response.DiaChiResponseDTO;
import com.datn.backend.dto.response.KhachHangResponseDTO;
import com.datn.backend.dto.response.KhachHang_BanHang_ResponseDTO;
import com.datn.backend.entity.DiaChi;
import com.datn.backend.entity.KhachHang;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

@Mapper(componentModel = "spring", uses = {DiaChiMapper.class})
public interface KhachHangMapper {

    @Mapping(target = "maKhachHang", ignore = true)
    @Mapping(target = "hinhAnh", ignore = true)
    @Mapping(target = "diaChis", source = "diaChis")
    KhachHang toEntity(KhachHangRequestDTO dto);

    @Mapping(target = "diaChis", source = "diaChis")
    KhachHangResponseDTO toResponseDTO(KhachHang khachHang);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "hinhAnh", ignore = true)
    @Mapping(target = "maKhachHang", ignore = true)
    @Mapping(target = "diaChis", source = "diaChis")
    void updateEntityFromDTO(KhachHangRequestDTO dto, @MappingTarget KhachHang entity);

    @Named("mapDefaultDiaChi")
    default DiaChiResponseDTO mapDefaultDiaChi(List<DiaChi> diaChis) {
        if (diaChis == null || diaChis.isEmpty()) {
            return null;
        }
        return diaChis.stream()
                .filter(DiaChi::getIsDefault)
                .findFirst()
                .map(diaChi -> {
                    DiaChiMapper mapper = org.mapstruct.factory.Mappers.getMapper(DiaChiMapper.class);
                    return mapper.toResponse(diaChi); // Sử dụng toResponse thay vì mapDiaChiToDiaChiDTO
                })
                .orElse(null);
    }

    @Mapping(target = "diaChis", source = "diaChis")
    KhachHang_BanHang_ResponseDTO khachHangBanHang(KhachHang request);
}

