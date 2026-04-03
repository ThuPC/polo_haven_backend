package com.datn.backend.repository.hoadon_repo;


import com.datn.backend.dto.response.LichSuHoaDonResponseDto;
import com.datn.backend.entity.LichSuHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuHoaDonRepo extends JpaRepository<LichSuHoaDon, String> {
    @Query("SELECT new com.datn.backend.dto.response.LichSuHoaDonResponseDto(" +
            "lshd.id, httthd.maGiaoDich, hd.tongTien, hd.ngayThanhToan, httt.tenHinhThuc, " +
            "lshd.trangThai, lshd.tenNhanVien, lshd.hanhDongNguoiThaoTac, lshd.ghiChu, lshd.ngayCapNhat) " +
            "FROM LichSuHoaDon lshd " +
            "JOIN lshd.hoaDon hd " +
            "LEFT JOIN HTTT_Hoa_Don httthd ON httthd.hoaDon.id = hd.id " +
            "LEFT JOIN httthd.httt httt " +
            "WHERE hd.id = :hoaDonId " +
            "AND (lshd.trangThai != 1 OR lshd.trangThai = 1) " +
            "ORDER BY lshd.ngayCapNhat DESC")
    List<LichSuHoaDonResponseDto> getLichSuHoaDon(@Param("hoaDonId") String hoaDonId);


    @Query("SELECT new com.datn.backend.dto.response.LichSuHoaDonResponseDto(" +
            "lshd.id, httthd.maGiaoDich, hd.tongTien, hd.ngayThanhToan, httt.tenHinhThuc, " +
            "lshd.trangThai, lshd.tenNhanVien, lshd.hanhDongNguoiThaoTac, lshd.ghiChu, lshd.ngayCapNhat) " +
            "FROM LichSuHoaDon lshd " +
            "JOIN lshd.hoaDon hd " +
            "LEFT JOIN HTTT_Hoa_Don httthd ON httthd.hoaDon.id = hd.id " +
            "LEFT JOIN httthd.httt httt " +
            "WHERE hd.id = :hoaDonId " +
            "ORDER BY lshd.ngayCapNhat DESC")
    LichSuHoaDonResponseDto getLichSuHoaDonId(@Param("hoaDonId") String hoaDonId);

}
