package com.datn.backend.controller.BanHang_Controller.TaiQuay;

import com.datn.backend.contants.RoutesConstant;
import com.datn.backend.service.PhieuGiamGia_Service.KhachHang_GiamGiaService;
import com.datn.backend.service.khach_hang.KhachHangService;
import com.datn.backend.service.san_pham.ChiTietSanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RoutesConstant.PREFIX_API_ADMIN +"/ban-hang-tai-quay")
public class Data_BanHang_TaiQuay_Controller {

    @Autowired
    ChiTietSanPhamService chiTietSanPhamService;
    @Autowired
    KhachHangService khachHangService;
    @Autowired
    KhachHang_GiamGiaService khachHangGiamGiaService;


    @GetMapping("/danh-sach-san-pham")
    public ResponseEntity<?> getListSanPhamChiTietInBanHang(){
        return  ResponseEntity.ok( chiTietSanPhamService.getAllEntity());
    }

    @GetMapping("/danh-sach-khach-hang")
    public ResponseEntity<?> getListKhachHang(){
        return ResponseEntity.ok(khachHangService.getListKhachHangBanHang());
    }

    @GetMapping("/danh-sach-ma-giam-gia/cong-khai")
    public ResponseEntity<?> getListPublicVoucher(){
        return ResponseEntity.ok(khachHangGiamGiaService.getPublicVoucher());
    }
    @GetMapping("/danh-sach-ma-giam-gia/{idKhachHang}")
    public ResponseEntity<?> getListPrivateVoucher(@PathVariable String idKhachHang){
        return ResponseEntity.ok(khachHangGiamGiaService.getPublicPrivateVoucher(idKhachHang));
    }





}
