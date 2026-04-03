package com.datn.backend.repository;

import com.datn.backend.entity.GioHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface GioHangChiTietRepository extends JpaRepository<GioHangChiTiet, String> {
    List<GioHangChiTiet> findByKhachHang_Id(String khachHangId);
    Optional<GioHangChiTiet> findByKhachHang_IdAndChiTietSanPham_Id(String khachHangId, String chiTietSanPhamId);
}
