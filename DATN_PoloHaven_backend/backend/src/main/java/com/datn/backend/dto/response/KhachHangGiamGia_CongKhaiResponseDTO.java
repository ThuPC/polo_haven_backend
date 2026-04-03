package com.datn.backend.dto.response;

import com.datn.backend.contants.LoaiGiamGia;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KhachHangGiamGia_CongKhaiResponseDTO {
    String id;
    String maPhieu;
    String tenPhieuGiamGia;
    String  loaiGiamGia;
    Double giaTriGiam;
    Double giaTriToiDa;
    Double giaTriToiThieu;

}
