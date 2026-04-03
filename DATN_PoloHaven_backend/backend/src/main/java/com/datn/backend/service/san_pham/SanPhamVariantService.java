package com.datn.backend.service.san_pham;

import com.datn.backend.dto.request.SanPhamVariantRequestDTO;
import com.datn.backend.dto.response.SanPhamVariantResponseDTO;

import java.util.List;

public interface SanPhamVariantService {
    SanPhamVariantResponseDTO create(SanPhamVariantRequestDTO dto);
    SanPhamVariantResponseDTO update(String id, SanPhamVariantRequestDTO dto);
    void delete(String id);
    SanPhamVariantResponseDTO findById(String id);
    List<SanPhamVariantResponseDTO> getBySanPhamId(String sanPhamId);
}
