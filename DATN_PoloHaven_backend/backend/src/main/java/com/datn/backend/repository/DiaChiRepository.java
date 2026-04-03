package com.datn.backend.repository;

import com.datn.backend.entity.DiaChi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaChiRepository extends JpaRepository<DiaChi,String> {
    List<DiaChi> findByKhachHangId(String khachHangId);

    @Modifying
    @Query("UPDATE DiaChi d SET d.isDefault = FALSE WHERE d.khachHang.id = :khachHangId")
    void resetDefaultAddress(String khachHangId);
}
