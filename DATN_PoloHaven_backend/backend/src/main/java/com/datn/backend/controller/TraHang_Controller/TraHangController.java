package com.datn.backend.controller.TraHang_Controller;

import com.datn.backend.dto.request.DonTraHangRequest;
import com.datn.backend.dto.response.DonTraHangResponse;
import com.datn.backend.service.tra_hang.TraHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tra-hang")
@RequiredArgsConstructor
public class TraHangController {

    private final TraHangService traHangService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> taoYeuCauTraHang(
            @RequestPart("donTraHang") DonTraHangRequest donTraHangRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos,
            Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String khachHangId = jwt.getClaimAsString("userId");
            
            DonTraHangResponse response = traHangService.taoYeuCauTraHang(donTraHangRequest, khachHangId, images, videos);
            return ResponseEntity.ok(Map.of("success", true, "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/khach-hang")
    public ResponseEntity<?> getDanhSachDonTraHangByKhachHang(Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String khachHangId = jwt.getClaimAsString("userId");
            
            List<DonTraHangResponse> responses = traHangService.getDanhSachDonTraHangByKhachHang(khachHangId);
            return ResponseEntity.ok(Map.of("success", true, "data", responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDonTraHangById(@PathVariable String id, Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String khachHangId = jwt.getClaimAsString("userId");
            
            DonTraHangResponse response = traHangService.getDonTraHangById(id);
            
            // Kiểm tra quyền sở hữu
            if (!response.getTenKhachHang().equals(khachHangId)) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Bạn không có quyền xem đơn trả hàng này"));
            }
            
            return ResponseEntity.ok(Map.of("success", true, "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}