package com.datn.backend.repository;

import com.datn.backend.dto.response.BanHangOnline.SanPhamDetailSummaryProjection;
import com.datn.backend.dto.response.BanHangOnline.SanPhamSummaryDTO;
import com.datn.backend.dto.response.SanPhamResponseDTO;
import com.datn.backend.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamRepo extends JpaRepository<SanPham, String> {

    @Query("""
SELECT new com.datn.backend.dto.response.SanPhamResponseDTO(
    sp.id,
    sp.maSanPham,
    sp.tenSanPham,
    cl.tenChatLieu,
    xx.noiXuatXu,
    th.tenThuongHieu,
    sp.moTa,
    sp.trangThai,
    SUM(CASE WHEN ctsp.trangThai = 1 THEN ctsp.soLuong ELSE 0 END),
    MIN(ctsp.donGia),
    MAX(ctsp.donGia)
)
FROM SanPham sp
LEFT JOIN sp.chatLieu cl
LEFT JOIN sp.xuatXu xx
LEFT JOIN sp.thuongHieu th
LEFT JOIN ChiTietSanPham ctsp ON ctsp.sanPham.id = sp.id
WHERE (:keyword IS NULL OR sp.tenSanPham LIKE :keyword)
  AND (:status IS NULL OR sp.trangThai = :status)
GROUP BY sp.id, sp.maSanPham, sp.tenSanPham, cl.tenChatLieu, xx.noiXuatXu, th.tenThuongHieu, sp.moTa, sp.trangThai
""")
    Page<SanPhamResponseDTO> getAllSanPhamWithSoLuongCTSP(
            @Param("keyword") String keyword,
            @Param("status") Byte status,
            Pageable pageable);

    Page<SanPham> findAll(Pageable pageable);

    Page<SanPham> findByThuongHieuId(String thuongHieuId, Pageable pageable);

    @Query("SELECT sp FROM SanPham sp WHERE " +
            "(:keyword IS NULL OR sp.tenSanPham LIKE %:keyword%) AND " +
            "(:status IS NULL OR sp.trangThai = :status)")
    Page<SanPham> findWithFilters(@Param("keyword") String keyword,
                                  @Param("status") Byte status,
                                  Pageable pageable);

    @Query("SELECT sp FROM SanPham sp WHERE " +
            "sp.tenSanPham LIKE %:keyword%")
    Page<SanPham> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT s.tenSanPham FROM SanPham s")
    List<String> findAllTenSanPham();

    Optional<SanPham> findByTenSanPham(String tenSanPham);

    boolean existsByTenSanPhamIgnoreCase(String tenSanPham);




    @Query(value = """
    SELECT
    TOP 8
    sp.id AS id,
    sp.ten_san_pham AS tenSanPham,
    th.ten_thuong_hieu AS thuongHieu,
    cl.ten_chat_lieu AS chatLieu,
    xx.noi_xuat_xu AS xuatXu,
    CAST(MIN(CASE
            WHEN km.id IS NOT NULL THEN ct.don_gia - (ct.don_gia * km.phan_tram_giam / 100)
    ELSE ct.don_gia
    END) AS VARCHAR(50))
            + ' - ' +
    CAST(MAX(CASE
            WHEN km.id IS NOT NULL THEN ct.don_gia - (ct.don_gia * km.phan_tram_giam / 100)
    ELSE ct.don_gia
    END) AS VARCHAR(50)) AS khoangGia,
    --MAX(ha.hash) AS url,
    SUM(ct.so_luong) AS soLuongTon,
    ISNULL(MAX(km.phan_tram_giam), 0) AS phanTramGiam
    FROM san_pham sp
    INNER JOIN chi_tiet_san_pham ct ON sp.id = ct.id_san_pham
    INNER JOIN chat_lieu cl ON cl.id = sp.id_chat_lieu
    INNER JOIN thuong_hieu th ON th.id = sp.id_thuong_hieu
    INNER JOIN xuat_xu xx ON xx.id = sp.id_xuat_xu
    LEFT JOIN hinh_anh ha ON ct.id_hinh_anh = ha.id
    LEFT JOIN chi_tiet_khuyen_mai ctkm ON ct.id = ctkm.id_chi_tiet_san_pham AND ctkm.trang_thai = 1
    LEFT JOIN khuyen_mai km ON km.id = ctkm.id_khuyen_mai AND km.trang_thai = 1
    WHERE ct.trang_thai = 1 AND sp.trang_thai = 1
    GROUP BY
    sp.id,
    sp.ten_san_pham,
    th.ten_thuong_hieu,
    cl.ten_chat_lieu,
    xx.noi_xuat_xu,
    sp.ngay_tao -- Thêm cột ngày tạo vào GROUP BY để có thể sắp xếp
    
    -- ---- DÒNG MỚI ĐƯỢC THÊM VÀO ----
    ORDER BY
    sp.ngay_tao DESC -- Sắp xếp theo ngày tạo của sản phẩm, mới nhất lên đầu

    """, nativeQuery = true)
    List<SanPhamSummaryDTO> getListSanPhamSummaries();
    // 8 sản phẩm mới nhất


    @Query(value = """
SELECT
        sp.id AS id,
        sp.ten_san_pham AS tenSanPham,
        th.ten_thuong_hieu AS thuongHieu,
        cl.ten_chat_lieu AS chatLieu,
        xx.noi_xuat_xu AS xuatXu,
        CAST(MIN(CASE
                WHEN km.id IS NOT NULL THEN ct.don_gia - (ct.don_gia * km.phan_tram_giam / 100)
                ELSE ct.don_gia
            END) AS VARCHAR(50))
            + ' - ' +
        CAST(MAX(CASE
                WHEN km.id IS NOT NULL THEN ct.don_gia - (ct.don_gia * km.phan_tram_giam / 100)
                ELSE ct.don_gia
            END) AS VARCHAR(50)) AS khoangGia,
        --MAX(ha.hash) AS url,
        SUM(ct.so_luong) AS soLuongTon,
        ISNULL(MAX(km.phan_tram_giam), 0) AS phanTramGiam
    FROM san_pham sp
    INNER JOIN chi_tiet_san_pham ct ON sp.id = ct.id_san_pham
    INNER JOIN chat_lieu cl ON cl.id = sp.id_chat_lieu
    INNER JOIN thuong_hieu th ON th.id = sp.id_thuong_hieu
    INNER JOIN xuat_xu xx ON xx.id = sp.id_xuat_xu
    LEFT JOIN hinh_anh ha ON ct.id_hinh_anh = ha.id
    LEFT JOIN chi_tiet_khuyen_mai ctkm ON ct.id = ctkm.id_chi_tiet_san_pham AND ctkm.trang_thai = 1
    LEFT JOIN khuyen_mai km ON km.id = ctkm.id_khuyen_mai AND km.trang_thai = 1
    WHERE ct.trang_thai = 1 AND sp.trang_thai = 1
    GROUP BY
        sp.id,
        sp.ten_san_pham,
        th.ten_thuong_hieu,
        cl.ten_chat_lieu,
        xx.noi_xuat_xu
    """, nativeQuery = true)
    List<SanPhamSummaryDTO> getListSanPhamTatCa();


    @Query(value = """
    -- Sử dụng Common Table Expression (CTE) để tính tổng số lượng đã bán cho mỗi sản phẩm
    WITH SalesSummary AS (
        SELECT
            ct.id_san_pham,
            SUM(hdct.so_luong) AS totalSold
        FROM
            hoa_don_chi_tiet hdct
        INNER JOIN chi_tiet_san_pham ct ON hdct.id_chi_tiet_san_pham = ct.id
        -- Tùy chọn: Thêm điều kiện để chỉ tính các hóa đơn đã hoàn thành
        -- INNER JOIN hoa_don hd ON hdct.id_hoa_don = hd.id WHERE hd.trang_thai = 5
        GROUP BY
            ct.id_san_pham
    )

    -- Câu truy vấn chính để lấy thông tin sản phẩm
    SELECT
        sp.id AS id,
        sp.ten_san_pham AS tenSanPham,
        th.ten_thuong_hieu AS thuongHieu,
        cl.ten_chat_lieu AS chatLieu,
        xx.noi_xuat_xu AS xuatXu,
        CAST(MIN(CASE
                WHEN km.id IS NOT NULL THEN ct.don_gia - (ct.don_gia * km.phan_tram_giam / 100)
                ELSE ct.don_gia
            END) AS VARCHAR(50))
            + ' - ' +
        CAST(MAX(CASE
                WHEN km.id IS NOT NULL THEN ct.don_gia - (ct.don_gia * km.phan_tram_giam / 100)
                ELSE ct.don_gia
            END) AS VARCHAR(50)) AS khoangGia,
        --MAX(ha.hash) AS url,
        SUM(ct.so_luong) AS soLuongTon,
        ISNULL(MAX(km.phan_tram_giam), 0) AS phanTramGiam

    FROM san_pham sp
    INNER JOIN chi_tiet_san_pham ct ON sp.id = ct.id_san_pham
    LEFT JOIN SalesSummary sales ON sp.id = sales.id_san_pham
    
    INNER JOIN chat_lieu cl ON cl.id = sp.id_chat_lieu
    INNER JOIN thuong_hieu th ON th.id = sp.id_thuong_hieu
    INNER JOIN xuat_xu xx ON xx.id = sp.id_xuat_xu
    LEFT JOIN hinh_anh ha ON ct.id_hinh_anh = ha.id
    LEFT JOIN chi_tiet_khuyen_mai ctkm ON ct.id = ctkm.id_chi_tiet_san_pham AND ctkm.trang_thai = 1
    LEFT JOIN khuyen_mai km ON km.id = ctkm.id_khuyen_mai AND km.trang_thai = 1
    WHERE ct.trang_thai = 1 AND sp.trang_thai = 1
    GROUP BY
        sp.id,
        sp.ten_san_pham,
        th.ten_thuong_hieu,
        cl.ten_chat_lieu,
        xx.noi_xuat_xu

    ORDER BY
        -- Sử dụng MAX() để thỏa mãn quy tắc của GROUP BY và ISNULL() để xử lý sản phẩm chưa bán được
        ISNULL(MAX(sales.totalSold), 0) DESC

    """, nativeQuery = true)
    List<SanPhamSummaryDTO> getBestSellingProducts();


    @Query(value = """
    -- Sử dụng CTE để tính tổng số lượng đã bán cho sản phẩm này
    WITH SalesSummary AS (
        SELECT
            ct.id_san_pham,
            SUM(hdct.so_luong) AS totalSold
        FROM
            hoa_don_chi_tiet hdct
        INNER JOIN chi_tiet_san_pham ct ON hdct.id_chi_tiet_san_pham = ct.id
        WHERE ct.id_san_pham = :idSanPham -- Chỉ tính cho sản phẩm đang xem
        GROUP BY
            ct.id_san_pham
    )
    
    -- Câu truy vấn chính để lấy thông tin tóm tắt
    SELECT
        sp.id AS id,
        sp.ten_san_pham AS tenSanPham,
        sp.ma_san_pham + ', ' + sp.mo_ta AS moTa,
        th.ten_thuong_hieu AS thuongHieu,
        cl.ten_chat_lieu AS chatLieu,
        xx.noi_xuat_xu AS xuatXu,
        CAST(MIN(CASE WHEN km.id IS NOT NULL THEN ct.don_gia - (ct.don_gia * km.phan_tram_giam / 100) ELSE ct.don_gia END) AS VARCHAR(50))
        + ' - ' +
        CAST(MAX(CASE WHEN km.id IS NOT NULL THEN ct.don_gia - (ct.don_gia * km.phan_tram_giam / 100) ELSE ct.don_gia END) AS VARCHAR(50)) AS khoangGia,

        SUM(ct.so_luong) AS soLuongTon,
        ISNULL(MAX(km.phan_tram_giam), 0) AS phanTramGiam,
        
        -- --- DÒNG MỚI ĐƯỢC THÊM VÀO ---
        -- Lấy tổng số lượng đã bán từ bảng tạm, nếu chưa bán được thì trả về 0
        ISNULL(sales.totalSold, 0) AS soLuongDaBan

    FROM san_pham sp
    INNER JOIN chi_tiet_san_pham ct ON sp.id = ct.id_san_pham
    
    -- --- JOIN MỚI ĐƯỢC THÊM VÀO ---
    -- LEFT JOIN với bảng tạm đã tính toán ở trên
    LEFT JOIN SalesSummary sales ON sp.id = sales.id_san_pham

    INNER JOIN chat_lieu cl ON cl.id = sp.id_chat_lieu
    INNER JOIN thuong_hieu th ON th.id = sp.id_thuong_hieu
    INNER JOIN xuat_xu xx ON xx.id = sp.id_xuat_xu
    LEFT JOIN hinh_anh ha ON ct.id_hinh_anh = ha.id
    LEFT JOIN chi_tiet_khuyen_mai ctkm ON ct.id = ctkm.id_chi_tiet_san_pham AND ctkm.trang_thai = 1
    LEFT JOIN khuyen_mai km ON km.id = ctkm.id_khuyen_mai AND km.trang_thai = 1
    WHERE ct.trang_thai = 1 AND sp.trang_thai = 1 AND sp.id = :idSanPham
    GROUP BY
        sp.id,
        sp.ten_san_pham,
        sp.ma_san_pham,
        sp.mo_ta,
        th.ten_thuong_hieu,
        cl.ten_chat_lieu,
        xx.noi_xuat_xu,
        sales.totalSold -- Thêm cột đã join vào GROUP BY
    """,
            nativeQuery = true)
    Optional<SanPhamDetailSummaryProjection> findSanPhamChiTiet(@Param("idSanPham") String idSanPham);

}
