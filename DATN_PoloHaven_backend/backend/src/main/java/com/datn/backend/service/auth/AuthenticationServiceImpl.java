package com.datn.backend.service.auth;

import com.datn.backend.dto.request.AuthenticationRequest;
import com.datn.backend.dto.response.AuthenticationResponse;
import com.datn.backend.entity.KhachHang;
import com.datn.backend.entity.NhanVien;
import com.datn.backend.exception.AppException;
import com.datn.backend.exception.ErrorCode;
import com.datn.backend.repository.KhachHangRepository;
import com.datn.backend.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.Serial;

@Service
public class AuthenticationServiceImpl implements AuthenticationService{

    @Autowired
    NhanVienRepository nhanVienRepository;
    @Autowired
    KhachHangRepository khachHangRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    @Override
    public AuthenticationResponse authenticate_Staff(AuthenticationRequest request) {

        NhanVien nhanVien = nhanVienRepository.findBySdt(request.getSdt());
        if(nhanVien == null){
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        if (!"local".equalsIgnoreCase(nhanVien.getLoginProvider())||nhanVien.getTrangThai()!=1) {
            throw new AppException(ErrorCode.UnAuthenticated);
        }
        boolean authentication = passwordEncoder.matches(request.getMatKhau(), nhanVien.getMatKhau());
        if(!authentication){
            throw  new AppException(ErrorCode.UnAuthenticated);
        }
        String token = customOAuth2SuccessHandler.generateTokenForNhanVien(nhanVien);
        return AuthenticationResponse.builder()
                .authenticated(true)
                .token(token)
                .build();
    }


    @Override
    public AuthenticationResponse authenticate_Customers(AuthenticationRequest request) {
        KhachHang khachHang = khachHangRepository.findBySdt(request.getSdt());
        if(khachHang == null){
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        if (!"local".equalsIgnoreCase(khachHang.getLoginProvider())||khachHang.getTrangThai()!=1) {
            System.out.println("kiểu đăng nhập: "+khachHang.getLoginProvider());
            throw new AppException(ErrorCode.UnAuthenticated);
        }
        boolean authentication = passwordEncoder.matches(request.getMatKhau(), khachHang.getMatKhau());
        if(!authentication){
            System.out.println("Mật khẩu truyền vào"+request.getMatKhau());
            throw  new AppException(ErrorCode.UnAuthenticated);
        }
        String token = customOAuth2SuccessHandler.generateTokenForKhachHang(khachHang);
        return AuthenticationResponse.builder()
                .authenticated(true)
                .token(token)
                .build();
    }
}
