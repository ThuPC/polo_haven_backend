package com.datn.backend.repository;

import com.datn.backend.dto.response.SanPhamThinhHanhResponseDTO;
import com.datn.backend.entity.HoaDon;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public interface ThongKeRepo extends JpaRepository<HoaDon, UUID> {

    @Query(value = """
                SELECT COALESCE(SUM(hd.tong_tien_sau_khi_giam), 0)
                FROM hoa_don hd
                WHERE CAST(hd.ngay_thanh_toan AS DATE) = CAST(GETDATE() AS DATE)
                  AND hd.trang_thai = 5
            """, nativeQuery = true)
    BigDecimal getDoanhSoHomNay();

    @Query(value = """
                SELECT COALESCE(SUM(hd.tong_tien_sau_khi_giam), 0)
                FROM hoa_don hd
                WHERE CAST(hd.ngay_thanh_toan AS DATE) = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE)
                  AND hd.trang_thai = 5
            """, nativeQuery = true)
    BigDecimal getDoanhSoHomQua();

    @Query(value = """
                SELECT COALESCE(SUM(hd.tong_tien_sau_khi_giam), 0)
                FROM hoa_don hd
                WHERE DATEPART(ISO_WEEK, hd.ngay_thanh_toan) = DATEPART(ISO_WEEK, GETDATE())
                  AND YEAR(hd.ngay_thanh_toan) = YEAR(GETDATE())
                  AND hd.trang_thai = 5
            """, nativeQuery = true)
    BigDecimal getDoanhSoTuanNay();

    @Query(value = """
                SELECT COALESCE(SUM(hd.tong_tien_sau_khi_giam), 0)
                FROM hoa_don hd
                WHERE DATEPART(ISO_WEEK, hd.ngay_thanh_toan) = DATEPART(ISO_WEEK, DATEADD(WEEK, -1, GETDATE()))
                  AND YEAR(hd.ngay_thanh_toan) = YEAR(DATEADD(WEEK, -1, GETDATE()))
                  AND hd.trang_thai = 5
            """, nativeQuery = true)
    BigDecimal getDoanhSoTuanTruoc();

    @Query(value = """
                SELECT COALESCE(SUM(hd.tong_tien_sau_khi_giam), 0)
                FROM hoa_don hd
                WHERE MONTH(hd.ngay_thanh_toan) = MONTH(GETDATE())
                  AND YEAR(hd.ngay_thanh_toan) = YEAR(GETDATE())
                  AND hd.trang_thai = 5
            """, nativeQuery = true)
    BigDecimal getDoanhSoThangNay();

    @Query(value = """
                SELECT COALESCE(SUM(hd.tong_tien_sau_khi_giam), 0)
                FROM hoa_don hd
                WHERE MONTH(hd.ngay_thanh_toan) = MONTH(DATEADD(MONTH, -1, GETDATE()))
                  AND YEAR(hd.ngay_thanh_toan) = YEAR(DATEADD(MONTH, -1, GETDATE()))
                  AND hd.trang_thai = 5
            """, nativeQuery = true)
    BigDecimal getDoanhSoThangTruoc();


    @Query(value = "SELECT CAST(ngay_tao AS DATE) AS label, " +
            "COUNT(*) AS quantity, " +
            "SUM(tong_tien_sau_khi_giam) AS revenue " +
            "FROM hoa_don " +
            "WHERE ngay_tao BETWEEN :startDate AND :endDate " +
            "GROUP BY CAST(ngay_tao AS DATE) " +
            "ORDER BY CAST(ngay_tao AS DATE)", nativeQuery = true)
    List<Map<String, Object>> findBarChartDataByDay(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    // Biểu đồ cột - Theo tuần
    @Query(value = "SELECT DATEPART(WEEK, ngay_tao) AS label, " +
            "COUNT(*) AS quantity, " +
            "SUM(tong_tien_sau_khi_giam) AS revenue " +
            "FROM hoa_don " +
            "WHERE ngay_tao BETWEEN :startDate AND :endDate " +
            "GROUP BY DATEPART(WEEK, ngay_tao) " +
            "ORDER BY DATEPART(WEEK, ngay_tao)", nativeQuery = true)
    List<Map<String, Object>> findBarChartDataByWeek(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    // Biểu đồ cột - Theo tháng
    @Query(value = "SELECT FORMAT(ngay_tao, 'yyyy-MM') AS label, " +
            "COUNT(*) AS quantity, " +
            "SUM(tong_tien_sau_khi_giam) AS revenue " +
            "FROM hoa_don " +
            "WHERE ngay_tao BETWEEN :startDate AND :endDate " +
            "GROUP BY FORMAT(ngay_tao, 'yyyy-MM') " +
            "ORDER BY FORMAT(ngay_tao, 'yyyy-MM')", nativeQuery = true)
    List<Map<String, Object>> findBarChartDataByMonth(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    // Biểu đồ tròn - Theo trạng thái
    @Query(value = "SELECT trang_thai AS status, COUNT(*) AS count " +
            "FROM hoa_don " +
            "WHERE ngay_tao BETWEEN :startDate AND :endDate " +
            "GROUP BY trang_thai", nativeQuery = true)
    List<Map<String, Object>> findPieChartData(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
}