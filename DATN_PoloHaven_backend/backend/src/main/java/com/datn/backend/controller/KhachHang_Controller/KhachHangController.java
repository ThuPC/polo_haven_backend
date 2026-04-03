package com.datn.backend.controller.KhachHang_Controller;

import com.datn.backend.dto.request.KhachHangRequestDTO;
import com.datn.backend.dto.response.KhachHangResponseDTO;
import com.datn.backend.entity.KhachHang;
import com.datn.backend.repository.KhachHangRepository;
import com.datn.backend.service.khach_hang.KhachHangService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;


@RestController
@RequestMapping("/api/khach-hang")
@RequiredArgsConstructor
public class KhachHangController {

    private final KhachHangService khachHangService;
    private final KhachHangRepository khachHangRepository;
    private final com.datn.backend.service.auth.CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    
    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    @GetMapping("/getAll")
    public ResponseEntity<?> getAllNKhachHang(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "ngayTao") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String gioiTinh) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Byte statusByte = status != null && !status.isEmpty() ? Byte.parseByte(status) : null;
            Byte gioiTinhByte = gioiTinh != null && !gioiTinh.isEmpty() ? Byte.parseByte(gioiTinh) : null;

            Page<KhachHangResponseDTO> result;
            if (hasFilters(keyword, statusByte, gioiTinhByte)) {
                result = khachHangService.findAllWithFilters(pageable,keyword,statusByte,gioiTinhByte);
            } else {
                result = khachHangService.findAll(pageable);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi khi lấy danh sách khách hàng: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<KhachHangResponseDTO> getKhachHangById(@PathVariable String id) {
        try {
            KhachHangResponseDTO khachHang = khachHangService.findById(id);
            return ResponseEntity.ok(khachHang);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createKhachHang(
            @RequestPart("data") String data,
            @RequestPart(value = "hinhAnh", required = false) MultipartFile hinhAnh) throws IOException {
        try {
            // Chuyển đổi JSON string thành KhachHangRequestDTO
            KhachHangRequestDTO dto = objectMapper.readValue(data, KhachHangRequestDTO.class);

            if (hinhAnh != null && !hinhAnh.isEmpty()) {
                dto.setHinhAnh(hinhAnh); // Cần thêm getter/setter cho hinhAnh trong DTO
            }

            KhachHangResponseDTO result = khachHangService.create(dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Tạo khách hàng thành công",
                    "data", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Tạo khách hàng thất bại: " + e.getMessage()
            ));
        }
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateKhachHang(
            @PathVariable String id,
            @RequestPart("data") String data,
            @RequestPart(value = "hinhAnh", required = false) MultipartFile hinhAnh) throws IOException {
        try {
            System.out.println("DEBUG CONTROLLER UPDATE: Raw data received: " + data);
            KhachHangRequestDTO dto = objectMapper.readValue(data, KhachHangRequestDTO.class);
            System.out.println("DEBUG CONTROLLER UPDATE: DTO after parsing: " + dto.toString());
            System.out.println("DEBUG CONTROLLER UPDATE: ngaySinh in DTO: " + dto.getNgaySinh());

            if (hinhAnh != null && !hinhAnh.isEmpty()) {
                dto.setHinhAnh(hinhAnh);
            }

            KhachHangResponseDTO result = khachHangService.update(id, dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật khách hàng thành công",
                    "data", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Cập nhật khách hàng thất bại: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteKhachHang(@PathVariable String id) {
        try {
            khachHangService.delete(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xóa khách hàng thành công"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Xóa khách hàng thất bại: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/reset-password/{id}")
    public ResponseEntity<?> resetPassword(@PathVariable String id) {
        try {
            khachHangService.resetPassword(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đặt lại mật khẩu thành công"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Đặt lại mật khẩu thất bại: " + e.getMessage()
            ));
        }
    }

    /**
     * Gửi mã OTP để xác minh thay đổi mật khẩu
     */
    @PostMapping("/send-otp-change-password")
    public ResponseEntity<?> sendOtpForChangePassword() {
        try {
            String userId = getCurrentUserId();
            String otp = khachHangService.sendOtpForChangePassword(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Mã xác minh đã được gửi đến email của bạn"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Không thể gửi mã xác minh: " + e.getMessage()
            ));
        }
    }

    /**
     * Thay đổi mật khẩu cho user hiện tại (với xác minh OTP)
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        try {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            String otp = request.get("otp");
            
            if (currentPassword == null || newPassword == null || otp == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Vui lòng cung cấp đầy đủ thông tin: mật khẩu hiện tại, mật khẩu mới và mã xác minh"
                ));
            }
            
            String userId = getCurrentUserId();
            khachHangService.changePasswordWithOtp(userId, currentPassword, newPassword, otp);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Thay đổi mật khẩu thành công"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Thay đổi mật khẩu thất bại: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/export-excel")
    public ResponseEntity<Resource> exportExcel() {
        Resource resource = khachHangService.exportExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=khach_hang.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping("/import-excel")
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            khachHangService.importExcel(file);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Import Excel thành công!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Import Excel thất bại: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/download-template")
    public ResponseEntity<Resource> downloadTemplate() {
        Resource resource = khachHangService.downloadTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=template_khach_hang.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * Lấy thông tin profile của user hiện tại (từ JWT token)
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            // Lấy userId từ JWT token (đã được cấu hình trong SecurityConfig)
            String userId = getCurrentUserId();
            
            KhachHangResponseDTO profile = khachHangService.findById(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", profile
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Không thể lấy thông tin profile: " + e.getMessage()
            ));
        }
    }

    /**
     * Helper method để lấy userId từ JWT token
     */
    private String getCurrentUserId() {
        // Sử dụng SecurityContextHolder để lấy thông tin user hiện tại
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User chưa đăng nhập");
        }
        
        // Lấy JWT token từ request để decode và lấy userId
        try {
            String token = getJwtTokenFromRequest();
            if (token != null) {
                com.nimbusds.jwt.JWT jwt = com.nimbusds.jwt.JWTParser.parse(token);
                com.nimbusds.jwt.JWTClaimsSet claims = jwt.getJWTClaimsSet();
                String userId = (String) claims.getClaim("userId");
                if (userId != null) {
                    return userId;
                }
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Error extracting userId from JWT: " + e.getMessage());
        }
        
        // Fallback: sử dụng authentication.getName()
        return authentication.getName();
    }
    
    /**
     * Helper method để lấy JWT token từ request
     */
    private String getJwtTokenFromRequest() {
        try {
            HttpServletRequest request =
                ((org.springframework.web.context.request.ServletRequestAttributes) 
                 org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest();
            
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Error getting JWT token from request: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy thông tin đầy đủ của user hiện tại từ database
     */
    @GetMapping("/current-user-info")
    public ResponseEntity<?> getCurrentUserInfo() {
        try {
            System.out.println("DEBUG: getCurrentUserInfo called");
            String userId = getCurrentUserId();
            System.out.println("DEBUG: userId from token: " + userId);
            
            KhachHang khachHang = khachHangRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại với ID: " + userId));
            
            System.out.println("DEBUG: Found khachHang: " + khachHang.getTenKhachHang());
            System.out.println("DEBUG: sdt: " + khachHang.getSdt());
            System.out.println("DEBUG: gioiTinh: " + khachHang.getGioiTinh());
            System.out.println("DEBUG: ngaySinh: " + khachHang.getNgaySinh());
            
            // Tạo response với thông tin đầy đủ (xử lý null values)
            Map<String, Object> userInfo = Map.of(
                "id", khachHang.getId(),
                "tenKhachHang", khachHang.getTenKhachHang(),
                "email", khachHang.getEmail(),
                "sdt", khachHang.getSdt() != null ? khachHang.getSdt() : "",
                "gioiTinh", khachHang.getGioiTinh() != null ? khachHang.getGioiTinh() : 1,
                "ngaySinh", khachHang.getNgaySinh() != null ? khachHang.getNgaySinh().toString() : "",
                "hinhAnh", khachHang.getHinhAnh() != null ? khachHang.getHinhAnh() : "",
                "loginProvider", khachHang.getLoginProvider() != null ? khachHang.getLoginProvider() : "LOCAL",
                "trangThai", khachHang.getTrangThai() != null ? khachHang.getTrangThai() : 1
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", userInfo
            ));
        } catch (Exception e) {
            System.out.println("DEBUG: Error in getCurrentUserInfo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Không thể lấy thông tin user: " + e.getMessage()
            ));
        }
    }



    /**
     * Endpoint test đơn giản để kiểm tra authentication
     */
    @GetMapping("/test-auth")
    public ResponseEntity<?> testAuth() {
        try {
            System.out.println("DEBUG: testAuth called");
            
            // Lấy JWT token từ request
            String token = getJwtTokenFromRequest();
            System.out.println("DEBUG: JWT token from request: " + (token != null ? "exists" : "null"));
            
            if (token != null) {
                try {
                    com.nimbusds.jwt.JWT jwt = com.nimbusds.jwt.JWTParser.parse(token);
                    com.nimbusds.jwt.JWTClaimsSet claims = jwt.getJWTClaimsSet();
                    System.out.println("DEBUG: JWT claims: " + claims.toJSONObject());
                    
                    String userId = (String) claims.getClaim("userId");
                    String sub = claims.getSubject();
                    System.out.println("DEBUG: userId from JWT: " + userId);
                    System.out.println("DEBUG: sub from JWT: " + sub);
                    
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Authentication successful",
                        "userId", userId,
                        "sub", sub,
                        "allClaims", claims.toJSONObject()
                    ));
                } catch (Exception jwtError) {
                    System.out.println("DEBUG: Error parsing JWT: " + jwtError.getMessage());
                }
            }
            
            // Fallback
            String userId = getCurrentUserId();
            System.out.println("DEBUG: userId from getCurrentUserId: " + userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Authentication successful (fallback)",
                "userId", userId
            ));
        } catch (Exception e) {
            System.out.println("DEBUG: Error in testAuth: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Authentication failed: " + e.getMessage()
            ));
        }
    }

    private boolean hasFilters(String keyword, Byte status, Byte gioiTinh) {
        return (keyword != null && !keyword.trim().isEmpty()) ||
                status != null ||
                gioiTinh != null;
    }

}
