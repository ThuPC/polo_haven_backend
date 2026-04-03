package com.datn.backend.service.auth;

import com.datn.backend.entity.KhachHang;
import com.datn.backend.entity.NhanVien;
import com.datn.backend.exception.AppException;
import com.datn.backend.exception.ErrorCode;
import com.datn.backend.repository.ChucVuRepository;
import com.datn.backend.repository.KhachHangRepository;
import com.datn.backend.repository.NhanVienRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final String signer_key = "dPwfR7cghCw7V6xFm47kJcbftwri5sX2qjpnfHOTC3lyp7D5TsEhRNs40RWXbgiZ";

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private ChucVuRepository chucVuRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // Lấy vai trò đã chọn từ session, được thiết lập trước khi chuyển hướng đến Google
        String selectedRole = (String) request.getSession().getAttribute("selectedRole");
        if (selectedRole == null) {
            log.error("Không tìm thấy selectedRole trong session.");
            throw new AppException(ErrorCode.UnAuthenticated);
        }

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        Map<String, Object> attributes = token.getPrincipal().getAttributes();
        String email = (String) attributes.get("email");
        String jwtToken;

        // Dựa vào vai trò để gọi hàm xử lý tương ứng
        if ("KHACHHANG".equals(selectedRole)) {
            jwtToken = handleKhachHangLogin(email, attributes);
            request.getSession().removeAttribute("selectedRole");
            String redirectUrl = "http://localhost:3000/?token=" + jwtToken;
            response.sendRedirect(redirectUrl);
        } else if ("NHANVIEN".equals(selectedRole)) {
            jwtToken = handleNhanVienLogin(email, attributes);
            request.getSession().removeAttribute("selectedRole");
            String redirectUrl = "http://localhost:5173/oauth2-redirect?token=" + jwtToken;
            response.sendRedirect(redirectUrl);
        } else {
            log.error("Vai trò không hợp lệ: {}", selectedRole);
            throw new AppException(ErrorCode.UnAuthenticated);
        }

    }

    private String handleNhanVienLogin(String email, Map<String, Object> attributes) {
        NhanVien nhanVienLogin = nhanVienRepository.findByEmail(email);
        if (nhanVienLogin == null) {
            throw new AppException(ErrorCode.UnAuthenticated);
        }
        if (!"google".equalsIgnoreCase(nhanVienLogin.getLoginProvider()) || nhanVienLogin.getTrangThai() != 1) {
            // Sai nhà cung cấp đăng nhập hoặc tài khoản bị vô hiệu hóa
            throw new AppException(ErrorCode.UnAuthenticated);
        }

        if (chucVuRepository.getMaChucVuByNhanVienId(nhanVienLogin.getId()).isEmpty()) {
            throw new AppException(ErrorCode.UnAuthorized);
        }
        nhanVienLogin.setTenNhanVien((String) attributes.get("name"));
        nhanVienLogin.setHinhAnh((String) attributes.get("picture"));
        nhanVienRepository.save(nhanVienLogin);
        return generateTokenForNhanVien(nhanVienLogin);
    }


    public String handleKhachHangLogin(String email, Map<String, Object> attributes) {
        KhachHang khachHang = khachHangRepository.findByEmail(email)
                .orElseGet(() -> {
                    KhachHang newKhachHang = new KhachHang();
                    newKhachHang.setEmail(email);
                    newKhachHang.setTenKhachHang((String) attributes.get("name"));
                    newKhachHang.setHinhAnh((String) attributes.get("picture"));
                    newKhachHang.setCapDo((byte) 1);
                    // The following line might cause issues if a new customer is created during a concurrent request.
                    // Consider using a more robust ID generation strategy if this is a concern.
                    String maKhachHang = String.format("KH%05d", khachHangRepository.count() + 1);
                    newKhachHang.setMaKhachHang(maKhachHang);
                    newKhachHang.setNgayTao(System.currentTimeMillis());
                    newKhachHang.setTrangThai((byte) 1);
                    newKhachHang.setGioiTinh((byte) 1); // default to male
                    newKhachHang.setLoginProvider("GOOGLE"); // Set login provider for Google login
                    return newKhachHang;
                });

        // Update existing user with latest info from Google
        khachHang.setTenKhachHang((String) attributes.get("name"));
        khachHang.setHinhAnh((String) attributes.get("picture"));

        // Log all fields of the KhachHang object for debugging
        System.out.println("---------------------------------------------");
        System.out.println("Thông tin Khách hàng vừa login:");
        System.out.println("ID: " + khachHang.getId());
        System.out.println("Mã Khách Hàng: " + khachHang.getMaKhachHang());
        System.out.println("Tên Khách Hàng: " + khachHang.getTenKhachHang());
        System.out.println("Giới Tính: " + (khachHang.getGioiTinh() != null ? (khachHang.getGioiTinh() == 1 ? "Nam" : "Nữ") : "N/A"));
        System.out.println("Cấp Độ: " + khachHang.getCapDo());
        System.out.println("SĐT: " + khachHang.getSdt());
        System.out.println("Email: " + khachHang.getEmail());
        System.out.println("Mật Khẩu: " + (khachHang.getMatKhau() != null ? "******" : "N/A"));
        System.out.println("Login Provider: " + khachHang.getLoginProvider());
        System.out.println("Hình Ảnh URL: " + khachHang.getHinhAnh());
        System.out.println("Trạng Thái: " + (khachHang.getTrangThai() != null ? (khachHang.getTrangThai() == 1 ? "Hoạt động" : "Không hoạt động") : "N/A"));
        System.out.println("Ngày Tạo: " + (khachHang.getNgayTao() != null ? new Date(khachHang.getNgayTao()) : "N/A"));
        System.out.println("Ngày Sinh: " + khachHang.getNgaySinh());
        System.out.println("---------------------------------------------");

        khachHangRepository.save(khachHang);
        return generateTokenForKhachHang(khachHang);
    }

    public String generateTokenForNhanVien(NhanVien user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .claim("email", user.getEmail())
                .subject(user.getTenNhanVien())
                .claim("userId", user.getId())
                .claim("image", user.getHinhAnh())
                .claim("userType", "NHANVIEN") // Thêm loại người dùng
                .issuer("Polo_haven")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(10, ChronoUnit.HOURS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScopeForNhanVien(user)) // scope dành riêng cho nhân viên
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        try {
            System.out.println(signer_key.getBytes());
            jwsObject.sign(new MACSigner(signer_key.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Không thể tạo token cho nhân viên: ", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Tạo JWT token cho đối tượng KhachHang
     */
    public String generateTokenForKhachHang(KhachHang user) {

        String ngaySinhString = null;
        if (user.getNgaySinh() != null) {
            ngaySinhString = user.getNgaySinh().format(DateTimeFormatter.ISO_LOCAL_DATE); // Ví dụ: "2005-12-01"
        }
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .claim("email", user.getEmail())
                .subject(user.getTenKhachHang())
                .claim("userId", user.getId())
                .claim("image", user.getHinhAnh())
                .claim("userType", "KHACHHANG") // Thêm loại người dùng
                .claim("tenKhachHang", user.getTenKhachHang())
                .claim("sdt", user.getSdt())
                .claim("gioiTinh", user.getGioiTinh())
                .claim("ngaySinh", ngaySinhString)
                .claim("loginProvider", user.getLoginProvider())
                .issuer("Polo_haven")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(24, ChronoUnit.HOURS).toEpochMilli() // Khách hàng có thể có thời gian token dài hơn
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", "ROLE_KHACHHANG") // Khách hàng thường có một role cố định
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        try {
            jwsObject.sign(new MACSigner(signer_key.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Không thể tạo token cho khách hàng: ", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Xây dựng chuỗi scope (các quyền) cho NhanVien
     */
    private String buildScopeForNhanVien(NhanVien user) {
        List<String> roles = chucVuRepository.getMaChucVuByNhanVienId(user.getId());
        return roles.stream()
                .map(role -> "ROLE_" + role.toUpperCase())
                .collect(Collectors.joining(" "));
    }
}