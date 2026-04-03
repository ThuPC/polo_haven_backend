package com.datn.backend.repository;

import com.datn.backend.entity.ChiTietTraHang;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietTraHangRepository extends JpaRepository<ChiTietTraHang, String> {
    @EntityGraph(attributePaths = {
            "chiTietSanPham",
            "chiTietSanPham.sanPham",
            "chiTietSanPham.mauSac",
            "chiTietSanPham.kichThuoc",
            "chiTietSanPham.hinhAnh"
    })
    List<ChiTietTraHang> findByDonTraHangId(String donTraHangId);

    @Query("SELECT c FROM ChiTietTraHang c WHERE c.hoaDonChiTiet.id = :hoaDonChiTietId")
    List<ChiTietTraHang> findByHoaDonChiTietId(String hoaDonChiTietId);
}