package com.datn.backend.controller.Authentication;

import com.datn.backend.dto.request.AuthenticationRequest;
import com.datn.backend.service.auth.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class AuthenticationController {
    @Autowired
    AuthenticationService authenticationService;


    @GetMapping("/api/auth/login")
    public void prepareOAuth2Login(@RequestParam("role") String role,
                                   HttpServletRequest request,
                                   HttpServletResponse response) throws IOException {
        request.getSession().setAttribute("selectedRole", role.toUpperCase());
        String oauth2RedirectUrl = "/oauth2/authorization/google";
        response.sendRedirect(oauth2RedirectUrl);
    }
    // nhân viên
    @PostMapping("/login-form")
    public ResponseEntity<?> loginNhanVien(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate_Staff(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login-form-for-customer")
    public ResponseEntity<?> loginKhachHang(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate_Customers(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/login-google")
    public void customLoginRedirect(HttpServletResponse response) throws Exception {
        response.sendRedirect("/oauth2/authorization/google");
    }
}
