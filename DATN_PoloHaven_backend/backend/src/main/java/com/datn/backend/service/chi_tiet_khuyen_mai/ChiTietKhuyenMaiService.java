package com.datn.backend.service.chi_tiet_khuyen_mai;

import com.datn.backend.dto.request.ChiTietKhuyenMaiRequest;
import com.datn.backend.dto.response.SanPhamApDungKhuyenMaiResponse;
import com.datn.backend.entity.ChiTietKhuyenMai;

import java.util.List;

public interface ChiTietKhuyenMaiService {
    List<ChiTietKhuyenMai> addMany(List<ChiTietKhuyenMaiRequest> requests);

    List<SanPhamApDungKhuyenMaiResponse> getSanPhamKhuyenMaiById(String id);

    void deleteById(String id);
}
