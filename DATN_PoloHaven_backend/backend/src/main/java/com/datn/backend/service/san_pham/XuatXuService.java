package com.datn.backend.service.san_pham;

import com.datn.backend.dto.request.ThuongHieuRequestDTO;
import com.datn.backend.dto.request.XuatXuRequestDTO;
import com.datn.backend.dto.response.ThuongHieuResponseDTO;
import com.datn.backend.dto.response.XuatXuResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface XuatXuService {
    List<String> getAllTenXuatXu();
    Page<XuatXuResponseDTO> getAll(Pageable pageable);
    XuatXuResponseDTO create(XuatXuRequestDTO dto);
    XuatXuResponseDTO update(String id, XuatXuRequestDTO dto);
    void delete(String id);
    XuatXuResponseDTO findById(String id);
    XuatXuResponseDTO updateStatus(String id, Byte trangThai);
    List<XuatXuResponseDTO> getAllActiveXuatXu();
    Page<XuatXuResponseDTO> getAll(Pageable pageable, String keyword);

}
