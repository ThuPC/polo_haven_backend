
package com.datn.backend.service.nhan_vien;

import com.datn.backend.dto.request.NhanVienRequestDTO;
import com.datn.backend.dto.response.NhanVienResponseDTO;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NhanVienService {

    Page<NhanVienResponseDTO> findAll(Pageable pageable);
    Page<NhanVienResponseDTO> findAllWithFilters(
            Pageable pageable, String keyword, Byte status, Byte gioiTinh);
    NhanVienResponseDTO findById(String id);
    NhanVienResponseDTO create(NhanVienRequestDTO dto);
    NhanVienResponseDTO update(String id, NhanVienRequestDTO dto);
    void delete(String id);
    void resetPassword(String id);
    Resource exportExcel();
    List<NhanVienResponseDTO> importExcel(MultipartFile file);
    Resource downloadTemplate();
}
