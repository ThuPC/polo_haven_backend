package com.datn.backend.repository;

import com.datn.backend.entity.KhachHang;
import com.datn.backend.entity.NhanVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, String> {

    // Phân trang tất cả khách hàng
    Page<KhachHang> findAll(Pageable pageable);
    List<KhachHang> findByIdIn(List<String> ids);
    @Query(value = "SELECT * FROM khach_hang WHERE cap_do = :capDo", nativeQuery = true)
    List<KhachHang> findByCapDo(@Param("capDo") int capDo);

    @Query("SELECT kh FROM KhachHang kh LEFT JOIN kh.diaChis dc " +
            "WHERE (:keyword IS NULL OR LOWER(kh.tenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(kh.sdt) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR kh.trangThai = :status) " +
            "AND (:gioiTinh IS NULL OR kh.gioiTinh = :gioiTinh)")
    Page<KhachHang> findWithFilters(
            @Param("keyword") String keyword,
            @Param("status") Byte status,
            @Param("gioiTinh") Byte gioiTinh,
            Pageable pageable);

    boolean existsBySdt(String sdt);

    KhachHang findBySdt(String sdt);

    boolean existsByEmail(String email);

    Optional<KhachHang> findByEmail(String email);
}
