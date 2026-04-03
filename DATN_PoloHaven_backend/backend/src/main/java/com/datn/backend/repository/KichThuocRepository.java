package com.datn.backend.repository;

import com.datn.backend.dto.response.KichThuocResponseDTO;
import com.datn.backend.entity.KichThuoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KichThuocRepository extends JpaRepository<KichThuoc, String> {

    Page<KichThuoc> findAll(Pageable pageable);

    @Query("SELECT new com.datn.backend.dto.response.KichThuocResponseDTO(k.id, k.maKichThuoc, k.size, k.trangThai) FROM KichThuoc k WHERE k.trangThai = 1")
    List<KichThuocResponseDTO> findAllActiveKichThuoc();

    @Query("SELECT k.size  FROM KichThuoc k WHERE k.trangThai = 1")
    List<String> findAllSizeActive();

    @Query("SELECT k FROM KichThuoc k WHERE k.size LIKE %:keyword%")
    Page<KichThuoc> searchBySize(@Param("keyword") String keyword, Pageable pageable);

}
