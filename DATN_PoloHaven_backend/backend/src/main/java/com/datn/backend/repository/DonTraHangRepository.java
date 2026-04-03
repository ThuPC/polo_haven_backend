package com.datn.backend.repository;

import com.datn.backend.entity.DonTraHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonTraHangRepository extends JpaRepository<DonTraHang, String> {
    List<DonTraHang> findByKhachHangId(String khachHangId);
    
    List<DonTraHang> findByTrangThai(Byte trangThai);
    
    @Query("SELECT d FROM DonTraHang d WHERE d.hoaDon.id = :hoaDonId")
    Optional<DonTraHang> findByHoaDonId(String hoaDonId);
}