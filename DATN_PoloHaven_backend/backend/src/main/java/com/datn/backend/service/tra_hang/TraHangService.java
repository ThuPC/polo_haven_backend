package com.datn.backend.service.tra_hang;

import com.datn.backend.dto.request.DonTraHangRequest;
import com.datn.backend.dto.response.DonTraHangResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface TraHangService {
    DonTraHangResponse taoYeuCauTraHang(DonTraHangRequest request, String khachHangId, List<MultipartFile> images, List<MultipartFile> videos);
    
    List<DonTraHangResponse> getDanhSachDonTraHangByKhachHang(String khachHangId);
    
    List<DonTraHangResponse> getDanhSachDonTraHangByTrangThai(Byte trangThai);
    
    List<DonTraHangResponse> getAllDonTraHang();
    
    DonTraHangResponse getDonTraHangById(String id);
    
    DonTraHangResponse chapNhanDonTraHang(String id, String ghiChuAdmin, Map<String, Byte> tinhTrangHangMap);
    
    DonTraHangResponse tuChoiDonTraHang(String id, String ghiChuAdmin);
    
    DonTraHangResponse hoanThanhDonTraHang(String id, String ghiChuAdmin);
}