package com.datn.backend.repository;


import com.datn.backend.dto.response.ThuongHieuResponseDTO;
import com.datn.backend.entity.ThuongHieu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThuongHieuRepo extends JpaRepository<ThuongHieu, String> {
    Page<ThuongHieu> findAll(Pageable pageable);

    @Query("SELECT s.tenThuongHieu FROM ThuongHieu s WHERE s.trangThai = 1")
    List<String> findAllTenThuongHieu();

    @Query("SELECT new com.datn.backend.dto.response.ThuongHieuResponseDTO(t.id, t.maThuongHieu, t.tenThuongHieu, t.trangThai) FROM ThuongHieu t WHERE t.trangThai = 1")
    List<ThuongHieuResponseDTO> findAllActiveThuongHieu();

    Page<ThuongHieu> findByTenThuongHieuContainingIgnoreCase(String keyword, Pageable pageable);

}
