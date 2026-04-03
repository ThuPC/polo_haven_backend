package com.datn.backend.repository;

import com.datn.backend.dto.response.SanPhamApDungKhuyenMaiResponse;
import com.datn.backend.entity.ChiTietKhuyenMai;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
@Repository
public interface ChiTietKhuyenMaiRepository extends JpaRepository<ChiTietKhuyenMai, String> {
    // ===== LẤY DANH SÁCH SẢN PHẨM ÁP DỤNG KHUYẾN MÃI (DTO) =====
    @Query("""
        SELECT new com.datn.backend.dto.response.SanPhamApDungKhuyenMaiResponse(
            ctkm.id,
            ctkm.maSanPhamKhuyenMai,
            km.tenKhuyenMai,
            ctsp.tenCTSP,
            ctsp.donGia,
            ctkm.trangThai
        )
        FROM ChiTietKhuyenMai ctkm
        JOIN ctkm.khuyenMai km
        JOIN ctkm.chiTietSanPham ctsp
        WHERE km.id = :id
    """)
    List<SanPhamApDungKhuyenMaiResponse> getSanPhamKhuyenMaiById(@Param("id") String id);

    // ===== KIỂM TRA TRÙNG THỜI GIAN ÁP DỤNG =====
    @Query("SELECT c FROM ChiTietKhuyenMai c " +
            "WHERE c.chiTietSanPham.id = :idSanPham " +
            "AND c.khuyenMai.ngayBatDau <= :ngayKetThuc " +
            "AND c.khuyenMai.ngayKetThuc >= :ngayBatDau")
    List<ChiTietKhuyenMai> findOverlappingKhuyenMai(
            @Param("idSanPham") String idSanPham,
            @Param("ngayBatDau") LocalDate ngayBatDau,
            @Param("ngayKetThuc") LocalDate ngayKetThuc
    );



    // ===== XÓA CHI TIẾT THEO KHUYẾN MÃI =====
    @Transactional
    @Modifying
    @Query("DELETE FROM ChiTietKhuyenMai c WHERE c.khuyenMai.id = :khuyenMaiId")
    int deleteByKhuyenMaiId(@Param("khuyenMaiId") String khuyenMaiId);

    // ===== LẤY TOÀN BỘ CTKM THEO KHUYẾN MÃI =====
    List<ChiTietKhuyenMai> findAllByKhuyenMaiId(String khuyenMaiId);

    // ===== KIỂM TRA XUNG ĐỘT SẢN PHẨM VỚI KHUYẾN MÃI KHÁC =====
    @Query("""
        SELECT ctkm FROM ChiTietKhuyenMai ctkm
        WHERE ctkm.chiTietSanPham.id IN :ids
        AND ctkm.khuyenMai.trangThai IN (0, 1, 4)
        AND ctkm.khuyenMai.id <> :idKm
    """)
    List<ChiTietKhuyenMai> findConflicts(@Param("ids") List<String> ids, @Param("idKm") String idKm);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE ChiTietKhuyenMai c SET c.trangThai = :trangThai WHERE c.khuyenMai.id = :kmId")
    int updateTrangThaiByKhuyenMaiId(@Param("trangThai") Byte trangThai, @Param("kmId") String kmId);

    // ===== LẤY KHUYẾN MÃI ĐANG HOẠT ĐỘNG CHO CHI TIẾT SẢN PHẨM =====
    @Query("""
        SELECT ctkm FROM ChiTietKhuyenMai ctkm
        WHERE ctkm.chiTietSanPham = :chiTietSanPham
        AND ctkm.trangThai = :trangThai
        AND ctkm.khuyenMai.trangThai = 1
        AND ctkm.khuyenMai.ngayBatDau <= CURRENT_DATE
        AND ctkm.khuyenMai.ngayKetThuc >= CURRENT_DATE
    """)
    ChiTietKhuyenMai findByChiTietSanPhamAndTrangThai(
        @Param("chiTietSanPham") com.datn.backend.entity.ChiTietSanPham chiTietSanPham,
        @Param("trangThai") Byte trangThai
    );

}
