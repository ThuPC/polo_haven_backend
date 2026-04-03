package com.datn.backend.controller.BanHang_Controller.Online;

import com.datn.backend.contants.RoutesConstant;
import com.datn.backend.dto.request.SanPhamFilterRequest;
import com.datn.backend.dto.response.ChiTietSanPhamResponseDTO;
import com.datn.backend.service.PhieuGiamGia_Service.KhachHang_GiamGiaService;
import com.datn.backend.service.dia_chi.DiaChiService;
import com.datn.backend.service.khach_hang.KhachHangService;
import com.datn.backend.service.san_pham.ChiTietSanPhamService;
import com.datn.backend.service.san_pham.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(RoutesConstant.PREFIX_API_CUSTOMER_PUBLIC )
public class Data_BanHang_Online_Controller {

    @Autowired
    ChiTietSanPhamService chiTietSanPhamService;
    @Autowired
    KhachHangService khachHangService;
    @Autowired
    KhachHang_GiamGiaService khachHangGiamGiaService;
    @Autowired
    SanPhamService sanPhamService;
    @Autowired
    DiaChiService diaChiService;

    @GetMapping("/danh-sach-san-pham")
    public ResponseEntity<?> getListSanPham(){
        return  ResponseEntity.ok( sanPhamService.listSanPham());
    }

    @PostMapping("/danh-sach-san-pham-fill")
    public ResponseEntity<?> getListSanPhamFill(@RequestBody SanPhamFilterRequest request){
        return ResponseEntity.ok(sanPhamService.listSanPhamFill(request));
    }

    @GetMapping("/danh-sach-san-pham/moi-ve")
    public ResponseEntity<?> getListSanPhamMoiVe(){
        return  ResponseEntity.ok( sanPhamService.listSanPhamMoiVe());
    }

    @GetMapping("/danh-sach-chi-tiet-san-pham")
    public ResponseEntity<List<ChiTietSanPhamResponseDTO>> getAll() {
        return ResponseEntity.ok(chiTietSanPhamService.getAll());
    }


    @GetMapping("/danh-sach-san-pham/noi-bat")
    public ResponseEntity<?> getListSanPhamBanChay(){
        return  ResponseEntity.ok( sanPhamService.listSanPhamBanChay());
    }

    @GetMapping("/danh-sach-san-pham/chi-tiet/{id}")
    public ResponseEntity<?> getSanPhamDetail(@PathVariable String id){
        return  ResponseEntity.ok( sanPhamService.getSanPhamDetail(id));
    }


    @GetMapping("/danh-sach-ma-giam-gia/cong-khai")
    public ResponseEntity<?> getListPublicVoucher(){
        return ResponseEntity.ok(khachHangGiamGiaService.getPublicVoucher());
    }
    @GetMapping("/danh-sach-ma-giam-gia/{idKhachHang}")
    public ResponseEntity<?> getListPrivateVoucher(@PathVariable String idKhachHang){
        return ResponseEntity.ok(khachHangGiamGiaService.getPublicPrivateVoucher(idKhachHang));
    }

    @GetMapping("/danh-sach-dia-chi/{idKhachHang}")
    public ResponseEntity<?> getListAddress(@PathVariable String idKhachHang){
        return ResponseEntity.ok(diaChiService.getListAddressByCustomer(idKhachHang));
    }

}
