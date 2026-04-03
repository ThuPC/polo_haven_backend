package com.datn.backend.service.san_pham;

import com.datn.backend.dto.request.ChiTietSanPhamRequestDTO;
import com.datn.backend.dto.response.ChiTietSanPhamResponseDTO;
import com.datn.backend.dto.response.ChiTietSanPham_BanHang_ResponseDTO;

import java.util.List;

public interface ChiTietSanPhamService {
    ChiTietSanPhamResponseDTO create(ChiTietSanPhamRequestDTO dto);

    List<ChiTietSanPhamResponseDTO> createBatch(List<ChiTietSanPhamRequestDTO> dtos);

    List<ChiTietSanPhamResponseDTO> getAll();

    ChiTietSanPhamResponseDTO getById(String id);

    void delete(String id);

    List<ChiTietSanPhamResponseDTO> getBySanPhamId(String idSanPham);

    ChiTietSanPhamResponseDTO update(String id, ChiTietSanPhamRequestDTO dto);
    List<ChiTietSanPham_BanHang_ResponseDTO> getAllEntity();
    String generateMaCTSP();
}

