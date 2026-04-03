package com.datn.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DonTraHangRequest {
    private String hoaDonId;
    private String lyDo;
    private String ghiChu;
    private List<ChiTietTraHangRequest> chiTietTraHangList;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChiTietTraHangRequest {
        private String hoaDonChiTietId;
        private String chiTietSanPhamId;
        private Integer soLuong;
        private String lyDo;
    }
}