package com.datn.backend.dto.request;

import com.datn.backend.contants.LoaiGiamGia;
import com.datn.backend.entity.PhieuGiamGia;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KhachHangGiamGiaReuquestDTO {
    private String tenPhieuGiamGia;
    private String loaiGiamGia;
    private Double giaTriGiam;
    private Double giaTriToiDa;
    private Double giaTriToiThieu;
    private Integer trangThai;
    private Long ngayBatDau;
    private Long ngayKetThuc;
    private String doiTuongApDung;

    // ---- CÁC TRƯỜDNG CÓ ĐIỀU KIỆN ----

    // Sẽ có giá trị KHI doiTuongApDung = "ALL"
    // Sẽ là NULL KHI doiTuongApDung = "SPECIFIC"
    private Integer soLuong;

    // Sẽ là một danh sách ID KHI doiTuongApDung = "SPECIFIC"
    // Sẽ là NULL KHI doiTuongApDung = "ALL"
    private List<String> danhSachKhachHangId;
    PhieuGiamGia phieuGiamGia;
    List<String> idKhachHang;
}
