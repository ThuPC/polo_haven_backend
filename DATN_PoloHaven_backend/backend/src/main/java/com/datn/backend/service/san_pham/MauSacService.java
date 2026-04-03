package com.datn.backend.service.san_pham;

import com.datn.backend.dto.request.MauSacRequestDTO;
import com.datn.backend.dto.response.MauSacResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MauSacService {
    Page<MauSacResponseDTO> getAll(Pageable pageable);
    MauSacResponseDTO create(MauSacRequestDTO dto);
    MauSacResponseDTO update(String id, MauSacRequestDTO dto);
    void delete(String id);
    MauSacResponseDTO findById(String id);
    //    List<String> getAllTenMauSac();
    MauSacResponseDTO updateStatus(String id, Byte trangThai);
    List<MauSacResponseDTO> getAllIdTenMauSac();
    List<MauSacResponseDTO> getAllDistinctTenMauSac();
    Page<MauSacResponseDTO> search(String keyword, Pageable pageable);

}
