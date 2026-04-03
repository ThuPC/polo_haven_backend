package com.datn.backend.repository;

import com.datn.backend.dto.response.XuatXuResponseDTO;
import com.datn.backend.entity.XuatXu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface XuatXuRepo extends JpaRepository<XuatXu, String> {

    Page<XuatXu> findAll(Pageable pageable);

    @Query("SELECT s.noiXuatXu FROM XuatXu s WHERE s.trangThai = 1")
    List<String> findAllTenXuatXu();

    @Query("SELECT new com.datn.backend.dto.response.XuatXuResponseDTO(x.id, x.maXuatXu, x.noiXuatXu, x.trangThai) FROM XuatXu x WHERE x.trangThai = 1")
    List<XuatXuResponseDTO> findAllActiveXuatXu();

    Page<XuatXu> findByNoiXuatXuContainingIgnoreCase(String keyword, Pageable pageable);

}
