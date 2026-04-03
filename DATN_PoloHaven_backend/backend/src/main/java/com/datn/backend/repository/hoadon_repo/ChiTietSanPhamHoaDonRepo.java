package com.datn.backend.repository.hoadon_repo;


import com.datn.backend.dto.response.ChiTietSanPhamHoaDonResponseDto;
import com.datn.backend.entity.ChiTietSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietSanPhamHoaDonRepo extends JpaRepository<ChiTietSanPham, String> {

    @Query("SELECT new com.datn.backend.dto.response.ChiTietSanPhamHoaDonResponseDto(" +
            "ctsp.id, ha.tenAnh, ms.tenMau, kt.size, ctsp.tenCTSP, ctsp.maCTSP, hdct.soLuong, hdct.giaBan) " +
            "FROM HoaDon hd " +
            "JOIN hd.chiTietHoaDonList hdct " +
            "JOIN hdct.chiTietSanPham ctsp " +
            "LEFT JOIN ctsp.hinhAnh ha " +
            "LEFT JOIN ctsp.mauSac ms " +
            "LEFT JOIN ctsp.kichThuoc kt " +
            "WHERE hd.id = :id")
    List<ChiTietSanPhamHoaDonResponseDto> getDanhSachSanPham(@Param("id") String id);
}
