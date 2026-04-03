package com.datn.backend.repository;

import com.datn.backend.entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, String> {
    @Query(value = "SELECT * FROM phieu_giam_gia WHERE trang_thai IN (0, 1) ORDER BY ngay_sua DESC", nativeQuery = true)
    List<PhieuGiamGia> findAllWhereTrangThaiNot2Native();


    @Query(value = """
                SELECT COUNT(khgg.ma_phieu)
                FROM phieu_giam_gia pgg
                INNER JOIN khach_hang_giam_gia khgg ON pgg.id = khgg.id_phieu_giam_gia
                GROUP BY khgg.trang_thai_su_dung, khgg.id_phieu_giam_gia
                HAVING khgg.trang_thai_su_dung AND khgg.id_phieu_giam_gia = :idPhieuGiamGia
            """, nativeQuery = true)
    Long demSoPhieuDaSuDungTheoId(@Param("idPhieuGiamGia") String idPhieuGiamGia);

    @Query(value = """
                SELECT COUNT(khgg.ma_phieu)
                FROM phieu_giam_gia pgg
                INNER JOIN khach_hang_giam_gia khgg ON pgg.id = khgg.id_phieu_giam_gia
                GROUP BY khgg.id_phieu_giam_gia
                HAVING khgg.id_phieu_giam_gia = :idPhieuGiamGia
            """, nativeQuery = true)
    Long demSoMaPhieuTheoId(@Param("idPhieuGiamGia") String idPhieuGiamGia);

    List<PhieuGiamGia> findAllByTrangThaiAndNgayKetThucLessThan(Byte trangThai, long ngayKetThuc);

    @Modifying
    @Query("UPDATE PhieuGiamGia p SET p.trangThai = :expiredStatus " +
            "WHERE p.ngayKetThuc < :currentTimeMillis AND p.trangThai = :activeStatus")
    int updateStatusForExpiredVouchers(
            @Param("currentTimeMillis") Long currentTimeMillis,
            @Param("expiredStatus") Byte expiredStatus,
            @Param("activeStatus") Byte activeStatus
    );
}
