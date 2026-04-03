package com.datn.backend.service.san_pham;

import com.datn.backend.dto.request.KichThuocRequestDTO;
import com.datn.backend.dto.response.KichThuocResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface KichThuocService {
    Page<KichThuocResponseDTO> getAll(Pageable pageable);

    List<KichThuocResponseDTO> getAllSize();

    List<KichThuocResponseDTO> getAllActiveKichThuoc();

    List<KichThuocResponseDTO> getAllDistinctTenKichThuoc();

    KichThuocResponseDTO create(KichThuocRequestDTO dto);

    KichThuocResponseDTO update(String id, KichThuocRequestDTO dto);

    KichThuocResponseDTO updateStatus(String id, Byte trangThai);

    void delete(String id);

    KichThuocResponseDTO findById(String id);

    Page<KichThuocResponseDTO> search(String keyword, Pageable pageable);

    List<KichThuocResponseDTO> getAllIdSize();
}

