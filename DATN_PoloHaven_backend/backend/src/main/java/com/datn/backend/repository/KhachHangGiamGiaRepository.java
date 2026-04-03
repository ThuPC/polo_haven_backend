package com.datn.backend.repository;

import com.datn.backend.dto.response.KhachHangGiamGia_CongKhaiResponseDTO;
import com.datn.backend.dto.response.LichSuSuDung_VoucherProjection;
import com.datn.backend.entity.KhachHang_GiamGia;
import com.datn.backend.entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KhachHangGiamGiaRepository extends JpaRepository<KhachHang_GiamGia, String> {
    List<KhachHang_GiamGia> findByPhieuGiamGia(PhieuGiamGia phieuGiamGia);

    @Query(value = "SELECT * FROM khach_hang_giam_gia WHERE trang_thai_su_dung = 0 AND id_phieu_giam_gia = :phieuGiamGiaId", nativeQuery = true)
    List<KhachHang_GiamGia> findChuaSuDungByPhieuGiamGia(@Param("phieuGiamGiaId") String phieuGiamGiaId);

    @Modifying
    @Query(value = "UPDATE khach_hang_giam_gia SET trang_thai = 0 WHERE id_phieu_giam_gia = :id", nativeQuery = true)
    void updateTrangThaiNgungHoatDong(@Param("id") String id);


    @Query(value = """

            SELECT
              CAST(khgg.id AS VARCHAR) AS id,
              khgg.ma_phieu AS maPhieu,
              pgg.ten_phieu_giam_gia AS tenPhieuGiamGia,
              CAST(pgg.loai_giam_gia AS VARCHAR) AS loaiGiamGia,
              pgg.gia_tri_giam AS giaTriGiam,
              pgg.gia_tri_toi_da AS giaTriToiDa,
              pgg.gia_tri_toi_thieu AS giaTriToiThieu
          FROM khach_hang_giam_gia khgg
          INNER JOIN phieu_giam_gia pgg
              ON khgg.id_phieu_giam_gia = pgg.id
          WHERE
              pgg.trang_thai = 1
              AND pgg.ngay_bat_dau <= DATEDIFF('MILLISECOND', TIMESTAMP '1970-01-01 00:00:00', CURRENT_TIMESTAMP)
              AND pgg.ngay_ket_thuc >= DATEDIFF('MILLISECOND', TIMESTAMP '1970-01-01 00:00:00', CURRENT_TIMESTAMP)
              AND khgg.trang_thai_su_dung = 0
              AND khgg.trang_thai = 1
              AND khgg.id_khach_hang IS NULL
        """, nativeQuery = true)
    List<KhachHangGiamGia_CongKhaiResponseDTO> findAvailablePublicVouchers();

    @Query(value = """
            SELECT
        CAST(khgg.id AS VARCHAR) AS id,
        khgg.ma_phieu AS maPhieu,
        pgg.ten_phieu_giam_gia AS tenPhieuGiamGia,
        CAST(pgg.loai_giam_gia AS VARCHAR) AS loaiGiamGia,
        pgg.gia_tri_giam AS giaTriGiam,
        pgg.gia_tri_toi_da AS giaTriToiDa,
        pgg.gia_tri_toi_thieu AS giaTriToiThieu
    FROM khach_hang_giam_gia khgg
    INNER JOIN phieu_giam_gia pgg
        ON khgg.id_phieu_giam_gia = pgg.id
    WHERE
        pgg.trang_thai = 1
        AND pgg.ngay_bat_dau <= DATEDIFF('MILLISECOND', TIMESTAMP '1970-01-01 00:00:00', CURRENT_TIMESTAMP)
        AND pgg.ngay_ket_thuc >= DATEDIFF('MILLISECOND', TIMESTAMP '1970-01-01 00:00:00', CURRENT_TIMESTAMP)   \s
        AND khgg.trang_thai_su_dung = 0
        AND khgg.trang_thai = 1
        AND khgg.id_khach_hang = :idKhachHang
    """, nativeQuery = true)
    List<KhachHangGiamGia_CongKhaiResponseDTO> findAvailableVouchersByKhachHang(@Param("idKhachHang") String idKhachHang);

    @Query(value = """
    SELECT
        khgg.id,
        khgg.ma_phieu AS maPhieu,
        kh.ten_khach_hang AS tenKhachHang,
        khgg.so_tien_giam AS soTienGiam,
        hd.ma_hoa_don AS maHoaDon,
        hd.ngay_tao
    FROM khach_hang_giam_gia khgg
    INNER JOIN phieu_giam_gia pgg ON khgg.id_phieu_giam_gia = pgg.id
    LEFT JOIN hoa_don hd ON hd.id_khach_hang_giam_gia = khgg.id 
    LEFT JOIN khach_hang kh ON hd.id_khach_hang = kh.id
    WHERE khgg.trang_thai_su_dung = 1
      AND pgg.id = :idPhieu
    ORDER BY maHoaDon DESC
""", nativeQuery = true)
    List<LichSuSuDung_VoucherProjection> findVoucherDaSuDung(@Param("idPhieu") String idPhieu);

}
