package com.datn.backend.repository;

import com.datn.backend.entity.MediaTraHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaTraHangRepository extends JpaRepository<MediaTraHang, String> {
    List<MediaTraHang> findByDonTraHangId(String donTraHangId);
    
    List<MediaTraHang> findByDonTraHangIdAndLoaiMedia(String donTraHangId, Byte loaiMedia);
}