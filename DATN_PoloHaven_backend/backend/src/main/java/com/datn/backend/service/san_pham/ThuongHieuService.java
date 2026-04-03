package com.datn.backend.service.san_pham;

import com.datn.backend.dto.request.ThuongHieuRequestDTO;
import com.datn.backend.dto.response.ThuongHieuResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ThuongHieuService {
    Page<ThuongHieuResponseDTO> getAll(Pageable pageable);
    ThuongHieuResponseDTO create(ThuongHieuRequestDTO dto);
    ThuongHieuResponseDTO update(String id, ThuongHieuRequestDTO dto);
    void delete(String id);
    ThuongHieuResponseDTO findById(String id);
    List<String> getAllTenThuongHieu();
    ThuongHieuResponseDTO updateStatus(String id, Byte trangThai);
    List<ThuongHieuResponseDTO> getAllActiveThuongHieu();
    Page<ThuongHieuResponseDTO> search(String keyword, Pageable pageable);

}
