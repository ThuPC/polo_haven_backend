package com.datn.backend.repository.hoadon_repo;

import com.datn.backend.dto.response.LichSuThanhToanDto;
import com.datn.backend.entity.HTTT;
import com.datn.backend.entity.HTTT_Hoa_Don;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface HtttHoaDonRepo extends JpaRepository<HTTT_Hoa_Don, String> {
    @Query("SELECT new com.datn.backend.dto.response.LichSuThanhToanDto(" +
            "ht.maGiaoDich, hd.tongTien, hd.ngayThanhToan, httt.tenHinhThuc) " +
            "FROM HTTT_Hoa_Don ht " +
            "JOIN ht.hoaDon hd " +
            "JOIN ht.httt httt " +
            "WHERE hd.id = :hoaDonId")
    List<LichSuThanhToanDto> getLichSuThanhToan(@Param("hoaDonId") String hoaDonId);

    Optional<HTTT_Hoa_Don> findByHoaDonId(String hoaDonId);
}
