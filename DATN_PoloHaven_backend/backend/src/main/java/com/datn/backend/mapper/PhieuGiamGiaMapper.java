package com.datn.backend.mapper;

import com.datn.backend.dto.request.PhieuGiamGia_CreationRequest;
import com.datn.backend.dto.request.PhieuGiamGia_UpdateRequest;
import com.datn.backend.dto.response.PhieuGiamGiaResponseDTO;
import com.datn.backend.entity.PhieuGiamGia;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PhieuGiamGiaMapper {
    PhieuGiamGia toPhieuGiamGia(PhieuGiamGia_CreationRequest request);
    PhieuGiamGiaResponseDTO toPhieuGiamGiaResponse(PhieuGiamGia phieuGiamGia);
    void updatePhieuGiamGia(@MappingTarget PhieuGiamGia phieuGiamGia, PhieuGiamGia_UpdateRequest phieuGiamGiaUpdateRequest);
}
