package com.datn.backend.repository;

import com.datn.backend.dto.request.ChiTietMauSacKichThuocDTOFill;
import com.datn.backend.dto.response.BanHangOnline.ChiTietMauSacKichThuocDTO;
import com.datn.backend.dto.response.BanHangOnline.SanPhamVariantDetailDTO;
import com.datn.backend.dto.response.ChiTietSanPhamHoaDonResponseDto;
import com.datn.backend.dto.response.ChiTietSanPhamProjection;
import com.datn.backend.dto.response.ChiTietSanPham_BanHang_ResponseDTO;
import com.datn.backend.entity.ChiTietSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, String> {
    List<ChiTietSanPham> findBySanPhamId(String idSanPham);

    @Query(value = "SELECT  ctsp.id AS id, ha.ma_anh, ctsp.don_gia, ctsp.mactsp, ctsp.tenctsp, httthd.ma_giao_dich, ctsp.so_luong, hd.ma_hoa_don " +
            "FROM hoa_don hd " +
            "JOIN hoa_don_chi_tiet hdct on hd.id = hdct.id_hoa_don " +
            "JOIN httt_hoa_don httthd on httthd.id_hoa_don = hd.id " +
            "JOIN hinh_thuc_thanh_toan httt on httt.id = httthd.id_httt " +
            "JOIN chi_tiet_san_pham ctsp on hdct.id_chi_tiet_san_pham = ctsp.id " +
            "JOIN san_pham sp on ctsp.id_san_pham = sp.id " +
            "JOIN hinh_anh ha on ha.id = ctsp.id_hinh_anh " +
            "WHERE hd.id = :id", nativeQuery = true)
    List<ChiTietSanPhamHoaDonResponseDto> getDanhSachSanPham(@Param("id") String id);


    @Query(value = """
    SELECT 
        ct.id as id, 
        ct.mactsp as maCTSP, 
        ct.tenctsp as tenCTSP, 
        ms.ma_mau_sac as maMauSac,
        ms.ten_mau as mauSac, 
        kt.size as kichThuoc,
        ct.so_luong as soLuong, 
        ct.don_gia as donGia,
        
        -- Thay đổi quan trọng 1: Tính toán giá sau giảm và lấy phần trăm giảm
        CASE
            WHEN km.id IS NOT NULL THEN ct.don_gia - (ct.don_gia * km.phan_tram_giam / 100)
            ELSE ct.don_gia
        END as giaSauGiam,
        km.phan_tram_giam as phanTramGiam,

        ct.ghi_chu as ghiChu, 
        cl.ten_chat_lieu as chatLieu,
        th.ten_thuong_hieu as thuongHieu,
        xx.noi_xuat_xu as xuatXu,
        ha.ten_anh as url
    FROM 
        chi_tiet_san_pham ct
        INNER JOIN san_pham sp ON ct.id_san_pham = sp.id
        INNER JOIN chat_lieu cl ON cl.id = sp.id_chat_lieu 
        INNER JOIN thuong_hieu th ON th.id = sp.id_thuong_hieu
        INNER JOIN xuat_xu xx ON xx.id = sp.id_xuat_xu 
        LEFT JOIN hinh_anh ha ON ct.id_hinh_anh = ha.id
        INNER JOIN mau_sac ms ON ct.id_mau_sac = ms.id
        INNER JOIN kich_thuoc kt ON ct.id_kich_thuoc = kt.id 
        
        -- Thay đổi quan trọng 2: Sử dụng LEFT JOIN cho khuyến mãi
        -- và đặt điều kiện trạng thái trong mệnh đề ON
        LEFT JOIN chi_tiet_khuyen_mai ctkm ON ct.id = ctkm.id_chi_tiet_san_pham AND ctkm.trang_thai = 1
        LEFT JOIN khuyen_mai km ON km.id = ctkm.id_khuyen_mai AND km.trang_thai = 1
    WHERE 
        ct.trang_thai = 1 AND sp.trang_thai = 1
    """,
            nativeQuery = true)
    List<ChiTietSanPham_BanHang_ResponseDTO> getAllChiTietSanPhamForBanHang();

    @Query(value = """
            SELECT 
                 ct.id as id, 
                 ct.maCTSP as maCTSP, 
                 ct.tenCTSP as tenCTSP, 
                 ms.ten_mau as mauSac, 
                 kt.size as kichThuoc,
                 ct.so_luong as soLuong, 
                 ct.don_gia as donGia,
            
                 -- Thay đổi quan trọng 1: Tính toán giá sau giảm và lấy phần trăm giảm
                 CASE
                     WHEN km.id IS NOT NULL THEN ct.don_gia - (ct.don_gia * km.phan_tram_giam / 100)
                     ELSE ct.don_gia
                 END as giaSauGiam,
                 km.phan_tram_giam as phanTramGiam,
            
                 ct.ghi_chu as ghiChu, 
                 cl.ten_chat_lieu as chatLieu,
                 th.ten_thuong_hieu as thuongHieu,
                 xx.noi_xuat_xu as xuatXu,
                 COALESCE(ha.hash, 'https://default-image-url.com/placeholder.jpg') as url
             FROM 
                 chi_tiet_san_pham ct
                 INNER JOIN san_pham sp ON ct.id_san_pham = sp.id
                 INNER JOIN chat_lieu cl ON cl.id = sp.id_chat_lieu 
                 INNER JOIN thuong_hieu th ON th.id = sp.id_thuong_hieu
                 INNER JOIN xuat_xu xx ON xx.id = sp.id_xuat_xu 
                 LEFT JOIN hinh_anh ha ON ct.id_hinh_anh = ha.id
                 INNER JOIN mau_sac ms ON ct.id_mau_sac = ms.id
                 INNER JOIN kich_thuoc kt ON ct.id_kich_thuoc = kt.id 
            
                 -- Thay đổi quan trọng 2: Sử dụng LEFT JOIN cho khuyến mãi
                 -- và đặt điều kiện trạng thái trong mệnh đề ON
                 LEFT JOIN chi_tiet_khuyen_mai ctkm ON ct.id = ctkm.id_chi_tiet_san_pham AND ctkm.trang_thai = 1
                 LEFT JOIN khuyen_mai km ON km.id = ctkm.id_khuyen_mai AND km.trang_thai = 1
             WHERE 
                 ct.trang_thai = 1 
                 AND sp.trang_thai = 1
                 AND (:kichThuoc IS NULL OR kt.size = :kichThuoc)
                 AND (:mauSac IS NULL OR ms.ten_mau = :mauSac)
                 AND (:chatLieu IS NULL OR cl.ten_chat_lieu = :chatLieu)
                 AND (:thuongHieu IS NULL OR th.ten_thuong_hieu = :thuongHieu)
                 AND (:xuatXu IS NULL OR xx.noi_xuat_xu = :xuatXu)
                 AND (:searchQuery IS NULL OR ct.tenctsp LIKE %:searchQuery% OR ct.mactsp LIKE %:searchQuery%)
            """,
            nativeQuery = true)
    List<ChiTietSanPham_BanHang_ResponseDTO> findFilteredProducts(
            @Param("kichThuoc") String kichThuoc,
            @Param("mauSac") String mauSac,
            @Param("chatLieu") String chatLieu,
            @Param("thuongHieu") String thuongHieu,
            @Param("xuatXu") String xuatXu,
            @Param("searchQuery") String searchQuery
    );

    @Query("SELECT DISTINCT c.kichThuoc.size FROM ChiTietSanPham c WHERE c.kichThuoc.size IS NOT NULL AND c.trangThai = 1")
    List<String> findDistinctSizes();

    @Query("SELECT DISTINCT c.mauSac.tenMau FROM ChiTietSanPham c WHERE c.mauSac.tenMau IS NOT NULL AND c.trangThai = 1")
    List<String> findDistinctColors();

    @Query("SELECT DISTINCT c.sanPham.chatLieu.tenChatLieu FROM ChiTietSanPham c WHERE c.sanPham.chatLieu.tenChatLieu IS NOT NULL AND c.sanPham.trangThai = 1")
    List<String> findDistinctMaterials();

    @Query("SELECT DISTINCT c.sanPham.thuongHieu.tenThuongHieu FROM ChiTietSanPham c WHERE c.sanPham.thuongHieu.tenThuongHieu IS NOT NULL AND c.sanPham.trangThai = 1")
    List<String> findDistinctBrands();

    @Query("SELECT DISTINCT c.sanPham.xuatXu.noiXuatXu FROM ChiTietSanPham c WHERE c.sanPham.xuatXu.noiXuatXu IS NOT NULL AND c.sanPham.trangThai = 1")
    List<String> findDistinctOrigins();

    @Query(value = """
    SELECT
        ct.id_san_pham as idSanPham,
        ms.id as idMauSac,
        ms.ten_mau as tenMauSac,
        ms.ma_mau_sac as maMauSac,
        kt.id as idKichThuoc,
        kt.size as tenKichThuoc,
        ha.ten_anh as imageUrl
        
    FROM 
        chi_tiet_san_pham ct
        INNER JOIN mau_sac ms ON ct.id_mau_sac = ms.id
        INNER JOIN kich_thuoc kt ON ct.id_kich_thuoc = kt.id
        LEFT JOIN hinh_anh ha ON ct.id_hinh_anh = ha.id
    WHERE 
        ct.trang_thai = 1 
        AND ct.id_san_pham IN (:sanPhamIds)
    """, nativeQuery = true)
    List<ChiTietMauSacKichThuocDTO> findAllDetailsBySanPhamIds(@Param("sanPhamIds") List<String> sanPhamIds);


    @Query(value = """
    SELECT
        ms.id as idMauSac,
        ms.ten_mau as tenMauSac,
        ms.ma_mau_sac as maMauSac,
        kt.id as idKichThuoc,
        kt.size as tenKichThuoc,
        ha.ten_anh as imageUrl
    FROM 
        chi_tiet_san_pham ct
        INNER JOIN mau_sac ms ON ct.id_mau_sac = ms.id
        INNER JOIN kich_thuoc kt ON ct.id_kich_thuoc = kt.id
        LEFT JOIN hinh_anh ha ON ct.id_hinh_anh = ha.id
    WHERE 
        ct.trang_thai = 1 
        AND ct.id_san_pham = :idSanPham
    """, nativeQuery = true)
    List<SanPhamVariantDetailDTO> findAllVariantDetailsBySanPhamId(@Param("idSanPham") String idSanPham);

//    @Query(value = """
//    SELECT
//        ct.id_san_pham as idSanPham,
//        ms.id as idMauSac,
//        ms.ten_mau as tenMauSac,
//        kt.id as idKichThuoc,
//        kt.size as tenKichThuoc,
//        ct.don_gia as donGia,
//        CASE
//            WHEN km.phan_tram_giam IS NOT NULL THEN ct.don_gia - (ct.don_gia * km.phan_tram_giam / 100)
//            ELSE ct.don_gia
//        END as giaSauGiam
//    FROM
//        chi_tiet_san_pham ct
//        INNER JOIN mau_sac ms ON ct.id_mau_sac = ms.id
//        INNER JOIN kich_thuoc kt ON ct.id_kich_thuoc = kt.id
//        LEFT JOIN chi_tiet_khuyen_mai ctkm ON ct.id = ctkm.id_chi_tiet_san_pham AND ctkm.trang_thai = 1
//        LEFT JOIN khuyen_mai km ON km.id = ctkm.id_khuyen_mai AND km.trang_thai = 1
//    WHERE
//        ct.trang_thai = 1
//        AND ct.id_san_pham IN (:sanPhamIds)
//""", nativeQuery = true)
//    List<ChiTietMauSacKichThuocDTOFill> findAllDetailsBySanPhamIdsFill(@Param("sanPhamIds") List<String> sanPhamIds);

@Query(value = """
    SELECT
        ct.id_san_pham AS idSanPham,
        ms.id AS idMauSac,
        ms.ten_mau AS tenMauSac,
        ms.ma_mau_sac as maMauSac,
        kt.id AS idKichThuoc,
        kt.size AS tenKichThuoc,
        cl.id AS idChatLieu,
        cl.ten_chat_lieu AS tenChatLieu,
        ct.don_gia AS donGia,
        -- compute giaSauGiam before the image so the column order matches DTO constructor
        CASE 
            WHEN km.phan_tram_giam IS NOT NULL 
                THEN ct.don_gia - (ct.don_gia * km.phan_tram_giam / 100)
            ELSE ct.don_gia
        END AS giaSauGiam,
        -- alias the image column to tenAnh to match the DTO field name
        ha.ten_anh AS tenAnh
    FROM chi_tiet_san_pham ct
    INNER JOIN mau_sac ms ON ct.id_mau_sac = ms.id
    INNER JOIN kich_thuoc kt ON ct.id_kich_thuoc = kt.id
    INNER JOIN san_pham sp ON ct.id_san_pham = sp.id
    INNER JOIN chat_lieu cl ON sp.id_chat_lieu = cl.id
    LEFT JOIN hinh_anh ha ON ct.id_hinh_anh = ha.id
    LEFT JOIN chi_tiet_khuyen_mai ctkm 
           ON ct.id = ctkm.id_chi_tiet_san_pham AND ctkm.trang_thai = 1
    LEFT JOIN khuyen_mai km 
           ON km.id = ctkm.id_khuyen_mai AND km.trang_thai = 1
    WHERE ct.trang_thai = 1 
      AND ct.id_san_pham IN (:sanPhamIds)
""", nativeQuery = true)
List<ChiTietMauSacKichThuocDTOFill> findAllDetailsBySanPhamIdsFill(
        @Param("sanPhamIds") List<String> sanPhamIds
);

    @Query("SELECT c.maCTSP FROM ChiTietSanPham c ORDER BY c.maCTSP DESC LIMIT 1")
    String findMaxMaCTSP();

    @Query(value = "SELECT mactsp, tenctsp FROM chi_tiet_san_pham WHERE id = :id", nativeQuery = true)
    ChiTietSanPhamProjection findMaTenById(@Param("id") String id);


}
