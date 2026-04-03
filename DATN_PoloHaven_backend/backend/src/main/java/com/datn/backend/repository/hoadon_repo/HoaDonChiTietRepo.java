package com.datn.backend.repository.hoadon_repo;


import com.datn.backend.dto.response.SanPhamThinhHanhResponseDTO;
import com.datn.backend.entity.HoaDon;
import com.datn.backend.entity.HoaDonChiTiet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoaDonChiTietRepo extends JpaRepository<HoaDonChiTiet, String> {

    List<HoaDonChiTiet> findByHoaDon(HoaDon hoaDon);

    @Query("""
    SELECT new com.datn.backend.dto.response.SanPhamThinhHanhResponseDTO(
        sp.id,
        sp.tenSanPham,
        MIN(ha.tenAnh),
        SUM(hdct.soLuong)
    )
    FROM HoaDonChiTiet hdct
    JOIN hdct.chiTietSanPham ctsp
    JOIN ctsp.sanPham sp
    LEFT JOIN ctsp.hinhAnh ha
    JOIN hdct.hoaDon hd
    WHERE hd.trangThai = 5
    GROUP BY sp.id, sp.tenSanPham
    ORDER BY SUM(hdct.soLuong) DESC
""")
    List<SanPhamThinhHanhResponseDTO> getTopSanPhamBanChay(Pageable pageable);

}
