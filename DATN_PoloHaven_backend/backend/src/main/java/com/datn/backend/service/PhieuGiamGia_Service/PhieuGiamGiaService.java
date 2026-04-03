package com.datn.backend.service.PhieuGiamGia_Service;

import com.datn.backend.dto.request.PhieuGiamGia_CreationRequest;
import com.datn.backend.dto.request.PhieuGiamGia_UpdateRequest;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.PhieuGiamGiaResponseDTO;
import com.datn.backend.entity.PhieuGiamGia;
import java.util.List;

public interface PhieuGiamGiaService {
    PhieuGiamGia taoPhieuGiamGia(PhieuGiamGia_CreationRequest request);
    List<PhieuGiamGia> fullDanhSachPhieuGiamGia();

    ApiResponse<PhieuGiamGia> xoaPhieuGiamGia(String id);
    PhieuGiamGia capNhatPhieuGiamGia(PhieuGiamGia_UpdateRequest request);

    List<PhieuGiamGiaResponseDTO> danhSachPhieuGiamGia();

    PhieuGiamGia capNhatTrangThai(String id);

    int deactivateExpiredVouchers();

}
