
package com.datn.backend.repository;

import com.datn.backend.entity.NhanVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, String> {

    NhanVien findByEmail(String email);

    NhanVien findBySdt(String sdt);

    @Query("SELECT nv FROM NhanVien nv WHERE " +
            "(:keyword IS NULL OR LOWER(nv.tenNhanVien) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(nv.sdt) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR nv.trangThai = :status) AND " +
            "(:gioiTinh IS NULL OR nv.gioiTinh = :gioiTinh)")
    Page<NhanVien> findWithFilters(
            @Param("keyword") String keyword,
            @Param("status") Byte status,
            @Param("gioiTinh") Byte gioiTinh,
            Pageable pageable);

    boolean existsByEmail(String email);

    boolean existsBySdt(String sdt);

    boolean existsByCccd(String cccd);
}
