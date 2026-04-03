package com.datn.backend.service.san_pham;

import com.datn.backend.dto.response.SanPhamTenResponseDTO;

import java.util.List;

public interface SanPhamTenService {
    List<SanPhamTenResponseDTO> getAllTenSanPham();
}

