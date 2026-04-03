package com.datn.backend.service.khuyen_mai;

import com.datn.backend.dto.request.KhuyenMaiRequest;
import com.datn.backend.dto.response.KhuyenMaiResponse;
import com.datn.backend.entity.KhuyenMai;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface KhuyenMaiService {
    // Add khuyen mai
    KhuyenMaiResponse createKhuyenMai(KhuyenMaiRequest requestDTO);

    // Cap nhat khuyen mai
    KhuyenMaiResponse updateKhuyenMai(String id, KhuyenMaiRequest requestDTO);

    // Xoa khuyen mai
    void deleteKhuyenMai(String id);

    // Detail theo id
    KhuyenMaiResponse getKhuyenMaiById(String id);

    // Lay danh sach da phan trang
    Page<KhuyenMaiResponse> getAllKhuyenMai(Pageable pageable);

    String toggleKhuyenMai(KhuyenMai request);

    void validateBeforeToggle(String id,Byte newStatus);
}
