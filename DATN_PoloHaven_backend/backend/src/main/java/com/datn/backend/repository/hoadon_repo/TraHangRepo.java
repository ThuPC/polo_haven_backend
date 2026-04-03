package com.datn.backend.repository.hoadon_repo;

import com.datn.backend.entity.TraHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraHangRepo extends JpaRepository<TraHang, String> {
    List<TraHang> findByStatus(Byte status);
    List<TraHang> findByKhachHangId(String khachHangId);
}
