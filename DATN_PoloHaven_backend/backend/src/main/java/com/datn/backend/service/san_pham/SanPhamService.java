package com.datn.backend.service.san_pham;

import com.datn.backend.dto.request.SanPhamFilterRequest;
import com.datn.backend.dto.request.SanPhamRequestDTO;
//import com.datn.backend.dto.request.SanPhamVariantsRequestDTO;
import com.datn.backend.dto.request.SanPhamVariantRequestDTO;
import com.datn.backend.dto.request.SanPhamVariantsRequestDTO;
import com.datn.backend.dto.response.BanHangOnline.SanPhamDetailResponse;
import com.datn.backend.dto.response.BanHangOnline.SanPhamResponse;
import com.datn.backend.dto.response.SanPhamResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SanPhamService {
    List<String> getAllTenSanPham();

    Page<SanPhamResponseDTO> getAllSanPhamWithSoLuongCTSP(int page, int size, String keyword, Byte status);

    SanPhamResponseDTO create(SanPhamRequestDTO dto);

    SanPhamResponseDTO update(String id, SanPhamRequestDTO dto);

    void delete(String id);

    SanPhamResponseDTO findById(String id);

    Page<SanPhamResponseDTO> findAll(Pageable pageable);

    Page<SanPhamResponseDTO> getSanPhamByThuongHieu(String thuongHieuId, Pageable pageable);

    Page<SanPhamResponseDTO> searchByKeyword(String keyword, Pageable pageable);

    Page<SanPhamResponseDTO> findAllWithFilters(Pageable pageable, String keyword, Byte status);

    SanPhamResponseDTO updateStatus(String id, Byte trangThai);

    void createMultipleVariants(SanPhamVariantsRequestDTO dto);

    String getIdByTenSanPham(String tenSanPham);

    List<SanPhamResponse> listSanPhamMoiVe();

    List<SanPhamResponse> listSanPham();

    List<SanPhamResponse> listSanPhamFill(SanPhamFilterRequest request);

    List<SanPhamResponse> listSanPhamBanChay();

    SanPhamDetailResponse getSanPhamDetail(String id);
}
