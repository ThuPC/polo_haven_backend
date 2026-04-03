package com.datn.backend.service.khach_hang;

import com.datn.backend.dto.request.KhachHangRequestDTO;
import com.datn.backend.dto.response.KhachHangResponseDTO;
import com.datn.backend.dto.response.KhachHang_BanHang_ResponseDTO;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KhachHangService {

    Page<KhachHangResponseDTO> findAll(Pageable pageable);
    Page<KhachHangResponseDTO> findAllWithFilters(
            Pageable pageable, String keyword, Byte status, Byte gioiTinh);
    KhachHangResponseDTO findById(String id);
    KhachHangResponseDTO create(KhachHangRequestDTO dto);
    KhachHangResponseDTO update(String id, KhachHangRequestDTO dto);
    void delete(String id);
    void resetPassword(String id);
    Resource exportExcel();
    void importExcel(MultipartFile file);
    Resource downloadTemplate();
    List<KhachHang_BanHang_ResponseDTO> getListKhachHangBanHang();
    
    // Profile methods - chỉ giữ lại method cần thiết
    void changePassword(String userId, String currentPassword, String newPassword);
    String sendOtpForChangePassword(String userId);
    void changePasswordWithOtp(String userId, String currentPassword, String newPassword, String otp);
    

}

