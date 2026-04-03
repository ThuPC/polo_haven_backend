package com.datn.backend.repository.hoadon_repo;


import com.datn.backend.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HoaDonRepo extends JpaRepository<HoaDon, String> {
    @Query("SELECT MAX(h.maHoaDon) FROM HoaDon h")
    String findMaxMaHoaDon();


    @Query("SELECT h FROM HoaDon h LEFT JOIN h.khachHang kh WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(h.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "(h.tenKhachHang IS NOT NULL AND LOWER(h.tenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(h.sdt IS NOT NULL AND LOWER(h.sdt) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(kh.tenKhachHang IS NOT NULL AND LOWER(kh.tenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(kh.sdt IS NOT NULL AND LOWER(kh.sdt) LIKE LOWER(CONCAT('%', :keyword, '%')))) AND " +
            "(:trangThai IS NULL OR h.trangThai = :trangThai) AND " +
            "(:fromDate IS NULL OR h.ngayTao >= :fromDate) AND " +
            "(:toDate IS NULL OR h.ngayTao <= :toDate) AND " +
            "(:loaiHoaDon IS NULL OR h.loaiHoaDon = :loaiHoaDon) " +
            "ORDER BY h.ngayTao DESC")
    Page<HoaDon> searchHoaDon(
            @Param("keyword") String keyword,
            @Param("trangThai") Integer trangThai,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("loaiHoaDon") Integer loaiHoaDon,
            Pageable pageable);


    List<HoaDon> findAllByIdIn(List<String> ids);

    long countByTrangThai(Byte trangThai);


    @Query("""
    SELECT hd FROM HoaDon hd
    WHERE hd.trangThai != 0
    ORDER BY hd.ngayTao DESC
""")
    Page<HoaDon> findDonHangGanDay(Pageable pageable);

    @Query("SELECT h FROM HoaDon h WHERE h.khachHang.id = :khachHangId " +
            "AND (:trangThai IS NULL OR h.trangThai = :trangThai) " +
            "ORDER BY h.ngayTao DESC")
    Page<HoaDon> findByKhachHangIdAndTrangThai(
            @Param("khachHangId") String khachHangId,
            @Param("trangThai") Integer trangThai,
            Pageable pageable);
    @Query("SELECT h FROM HoaDon h WHERE h.id = :id AND h.khachHang.id = :idKhachHang")
    @EntityGraph(attributePaths = {
            "chiTietHoaDonList",
            "chiTietHoaDonList.chiTietSanPham",
            "chiTietHoaDonList.chiTietSanPham.sanPham",
            "chiTietHoaDonList.chiTietSanPham.mauSac",
            "chiTietHoaDonList.chiTietSanPham.kichThuoc",
            "chiTietHoaDonList.chiTietSanPham.hinhAnh"
    })
    Optional<HoaDon> findByIdAndKhachHang_Id(@Param("id") String id, @Param("idKhachHang") String idKhachHang);

    Optional<HoaDon> findByConfirmationToken(String token);


}
