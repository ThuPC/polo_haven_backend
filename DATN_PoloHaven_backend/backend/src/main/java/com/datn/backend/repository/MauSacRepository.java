package com.datn.backend.repository;

import com.datn.backend.dto.response.MauSacResponseDTO;
import com.datn.backend.entity.MauSac;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MauSacRepository extends JpaRepository<MauSac, String> {
    Page<MauSac> findAll(Pageable pageable);

    Page<MauSac> findByTenMauContainingIgnoreCase(String keyword, Pageable pageable);

    @Query("SELECT s.tenMau FROM MauSac s WHERE s.trangThai = 1")
    List<String> findAllTenMau();

    @Query("SELECT new com.datn.backend.dto.response.MauSacResponseDTO(s.id, s.maMau,s.tenMau, s.maMauSac, s.trangThai) FROM MauSac s WHERE s.trangThai = 1")
    List<MauSacResponseDTO> findAllActiveMauSac();
}
