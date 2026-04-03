package com.datn.backend.repository;

import com.datn.backend.entity.ChucVu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChucVuRepository extends JpaRepository<ChucVu,String > {
    @Query(
            value = "SELECT cv.ma_chuc_vu " +
                    "FROM nhan_vien nv " +
                    "JOIN chuc_vu cv ON nv.id_chuc_vu = cv.id " +
                    "WHERE nv.id = ?1",
            nativeQuery = true
    )
    List<String> getMaChucVuByNhanVienId(String nhanVienId);


    @Query(value = "SELECT * FROM chuc_vu WHERE ma_chuc_vu = :maChucVu", nativeQuery = true)
    ChucVu findByMaChucVu(@Param("maChucVu") String maChucVu);

}
