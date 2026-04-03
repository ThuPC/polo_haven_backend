package com.datn.backend.controller.GioHang_Controller;

import com.datn.backend.contants.RoutesConstant;
import com.datn.backend.dto.response.GioHangChiTietResponseDTO;
import com.datn.backend.service.gio_hang_chi_tiet.GioHangChiTietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(RoutesConstant.PREFIX_API_CUSTOMER_PUBLIC + "/gio-hang")
public class GioHangChiTietController {

    @Autowired
    private GioHangChiTietService service;

    @GetMapping
    public List<GioHangChiTietResponseDTO> getCart(Authentication authentication) {
        String userId = ((Jwt) authentication.getPrincipal()).getClaimAsString("userId");
        return service.getCartByUserId(userId);
    }



    @PostMapping
    public GioHangChiTietResponseDTO addToCart(Authentication authentication, @RequestBody Map<String, Object> req) {
        String userId = ((Jwt) authentication.getPrincipal()).getClaimAsString("userId");
        String chiTietSanPhamId = (String) req.get("chiTietSanPhamId");
        int soLuong = (int) req.get("soLuong");
        return service.addToCart(userId, chiTietSanPhamId, soLuong);
    }

    @PutMapping("/{id}")
    public GioHangChiTietResponseDTO updateCart(@PathVariable String id, @RequestBody Map<String, Object> req) {
        int soLuong = (int) req.get("soLuong");
        return service.updateCartItem(id, soLuong);
    }

    @PostMapping("/delete-batch")
    public String xoaKhiMuaThanhCong(Authentication authentication, @RequestBody List<String> listIdCart) {
        try {
            String userId = ((Jwt) authentication.getPrincipal()).getClaimAsString("userId");
            for(String id : listIdCart){
                service.deleteCartItem(id);
            }
        }catch (Exception e){
            return "Xóa giỏ hàng thất bại";
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public void deleteCart(@PathVariable String id) {
        service.deleteCartItem(id);
    }

    @DeleteMapping("/clear")
    public void clearCart(Authentication authentication) {
        String userId = ((Jwt) authentication.getPrincipal()).getClaimAsString("userId");
        service.clearCartByKhachHangId(userId);
    }
}
