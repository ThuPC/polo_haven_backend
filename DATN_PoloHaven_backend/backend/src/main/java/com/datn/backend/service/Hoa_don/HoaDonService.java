package com.datn.backend.service.Hoa_don;

import com.datn.backend.dto.response.HoaDonGanDayResponseDTO;
import com.datn.backend.dto.response.HoaDonResponseDTO;
import com.datn.backend.dto.response.LichSuHoaDonResponseDto;
import com.datn.backend.entity.HoaDon;
import com.datn.backend.entity.TraHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface HoaDonService {

    Page<HoaDonResponseDTO> searchHoaDon(String keyword, String trangThai, LocalDateTime fromDate,
                                         LocalDateTime toDate, Integer loaiHoaDon, Pageable pageable);

    HoaDon detailHoaDon(String id);


    List<HoaDon> findAllByIds(List<String> ids);

    HoaDon findHoaDonById(String id);

    Map<Integer, Long> thongKeSoLuongHoaDonTheoTrangThai();

    byte[] generateInvoicePdf(HoaDon hoaDon) throws Exception;

    HoaDon capNhatTrangThai(String id, Byte trangThai, String ghiChu, String tenNhanVien);

    HoaDon xuLyThanhToan(String id, BigDecimal tongTien, BigDecimal daThanhToan,
                         String hinhThucId, String maGiaoDich, String ghiChu, String tenNhanVien);

    HoaDon xuLyHoanTien(String id, BigDecimal soTienHoan, String ghiChu, String tenNhanVien);

    List<LichSuHoaDonResponseDto> getLichSuHoaDon(String hoaDonId);

    List<HoaDonGanDayResponseDTO> getDonHangGanDay(int page, int size);

    Page<HoaDon> findAllByKhachHang(String khachHangId, Integer trangThai, Pageable pageable);

    HoaDonResponseDTO getHoaDonChiTietChoKhachHang(String hoaDonId, String idKhachHang);

    HoaDon huyDonHangKhach(String idHoaDon, String khachHangId, String lyDo);

    HoaDon traHangKhach(String idHoaDon, String khachHangId, String lyDo);

    // New: create return request with media URLs (images/videos)
    TraHang taoYeuCauTraHang(String idHoaDon, String khachHangId, String lyDo, java.util.List<String> mediaUrls);

    // Admin: list pending return requests
    java.util.List<com.datn.backend.entity.TraHang> getTraHangRequests(Byte status);

    // Admin: review a return request (mark as pristine or defective)
    com.datn.backend.entity.TraHang reviewTraHangRequest(String traHangId, Byte newStatus, String adminNote, String adminName);
}
