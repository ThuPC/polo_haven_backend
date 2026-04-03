package com.datn.backend.controller.HoaDon_Controller;

import com.datn.backend.configuration.SecurityUtil;
import com.datn.backend.dto.request.HuyHoaDonRequest;
import com.datn.backend.dto.request.TraHangRequest;
import com.datn.backend.dto.response.HoaDonResponseDTO;
import com.datn.backend.entity.HoaDon;
import com.datn.backend.mapper.HoaDonMapper;
import com.datn.backend.service.Hoa_don.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/khach-hang/hoa-don")
//@PreAuthorize("hasAuthority('ROLE_KHACHHANG')")
//@PreAuthorize("hasAnyRole('KHACH_HANG', 'QUAN_LY', 'NHAN_VIEN')")
public class HoaDonKhachHangController {

    @Autowired
    private HoaDonMapper hoaDonMapper;
    @Autowired
    private HoaDonService hoaDonService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyHoaDons(
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        JwtAuthenticationToken jwtAuthToken = (JwtAuthenticationToken) authentication;
        Jwt jwt = jwtAuthToken.getToken();
        String userId = jwt.getClaimAsString("userId"); // ✅
        System.out.println("ID KH từ token: " + userId);
        Jwt jwt1 = (Jwt) authentication.getPrincipal();
        System.out.println("TOÀN BỘ CLAIM: " + jwt1.getClaims());


        Page<HoaDon> hoaDons = hoaDonService.findAllByKhachHang(userId, trangThai, PageRequest.of(page, size));
        return ResponseEntity.ok(hoaDons.map(hoaDonMapper::toResponseDTO));
    }

    @GetMapping("/theo-doi/{id}")
    public ResponseEntity<?> xemChiTietHoaDon(@PathVariable("id") String hoaDonId) {
        try {
            // Validate input
            if (hoaDonId == null || hoaDonId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("ID hóa đơn không hợp lệ");
            }

            // Lấy ID khách hàng
            String idKhachHang = SecurityUtil.getCurrentUserId();
            if (idKhachHang == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Không tìm thấy thông tin khách hàng");
            }

//            System.out.println(" Debug: hoaDonId = " + hoaDonId);
//            System.out.println("�� Debug: idKhachHang = " + idKhachHang);

            // Gọi service
            HoaDonResponseDTO result = hoaDonService.getHoaDonChiTietChoKhachHang(hoaDonId, idKhachHang);

//            System.out.println(" Debug: result = " + result);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
//            System.err.println(" Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/huy/{id}")
    public ResponseEntity<?> huyDonHang(
            @PathVariable String id,
            @RequestBody HuyHoaDonRequest request,
            Authentication authentication) {

        try {
            // Validate request
            if (request.getLyDo() == null || request.getLyDo().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Lý do hủy không được để trống"));
            }

            if (request.getLyDo().trim().length() < 10) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Lý do hủy phải có ít nhất 10 ký tự"));
            }

            if (request.getLyDo().trim().length() > 500) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Lý do hủy không được quá 500 ký tự"));
            }

            // Lấy thông tin khách hàng từ authentication
            String khachHangId = SecurityUtil.getCurrentUserId();

            // Gọi service để hủy đơn hàng
            HoaDon hoaDonDaHuy = hoaDonService.huyDonHangKhach(id, khachHangId, request.getLyDo().trim());

            if (hoaDonDaHuy != null) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Hủy đơn hàng thành công!",
                        "data", Map.of(
                                "hoaDonId", hoaDonDaHuy.getId(),
                                "trangThai", hoaDonDaHuy.getTrangThai(),
                                "lyDo", request.getLyDo().trim(),
                                "ngayHuy", LocalDateTime.now()
                        )
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Không thể hủy đơn hàng."));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi server: " + e.getMessage()));
        }
    }

    @PostMapping("/tra/{id}")
    public ResponseEntity<?> traHang(
            @PathVariable String id,
            @RequestBody TraHangRequest request,
            Authentication authentication) {

        try {
            // Validate request
            if (request.getLyDo() == null || request.getLyDo().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Lý do trả hàng không được để trống"));
            }

            if (request.getLyDo().trim().length() < 10) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Lý do trả hàng phải có ít nhất 10 ký tự"));
            }

            if (request.getLyDo().trim().length() > 500) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Lý do trả hàng không được quá 500 ký tự"));
            }

            // Lấy thông tin khách hàng từ authentication
            String khachHangId = SecurityUtil.getCurrentUserId();

            // Gọi service để trả hàng
            // If client provided media, call new service method
            if (request.getMediaUrls() != null && !request.getMediaUrls().isEmpty()) {
                com.datn.backend.entity.TraHang traHang = hoaDonService.taoYeuCauTraHang(id, khachHangId, request.getLyDo().trim(), request.getMediaUrls());
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Yêu cầu trả hàng đã được tạo",
                        "traHangId", traHang.getId(),
                        "ngayTra", LocalDateTime.now()
                ));
            } else {
                HoaDon hoaDonTraHang = hoaDonService.traHangKhach(id, khachHangId, request.getLyDo().trim());
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Yêu cầu trả hàng đã được gửi thành công!",
                        "data", Map.of(
                                "hoaDonId", hoaDonTraHang.getId(),
                                "trangThai", hoaDonTraHang.getTrangThai(),
                                "lyDo", request.getLyDo().trim(),
                                "ngayTra", LocalDateTime.now()
                        )
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi server: " + e.getMessage()));
        }
    }

}
