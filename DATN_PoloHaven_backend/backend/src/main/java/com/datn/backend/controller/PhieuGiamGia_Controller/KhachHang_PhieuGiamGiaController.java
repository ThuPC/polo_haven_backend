package com.datn.backend.controller.PhieuGiamGia_Controller;
import com.datn.backend.contants.RoutesConstant;
import com.datn.backend.entity.PhieuGiamGia;
import com.datn.backend.service.PhieuGiamGia_Service.KhachHang_GiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(RoutesConstant.PREFIX_API_ADMIN +"/phieu-giam-gia")
public class KhachHang_PhieuGiamGiaController {

    @Autowired
    KhachHang_GiamGiaService khachHangGiamGiaService;


    @PreAuthorize("hasRole('QUANLY')")
    @PostMapping("/tao-ma")
    public ResponseEntity<?> creationVoucherDetail(@RequestBody PhieuGiamGia phieuGiamGia){
        return ResponseEntity.ok(khachHangGiamGiaService.creationDetailVoucher(phieuGiamGia)) ;
    }


    @PreAuthorize("hasRole('QUANLY')")
    @GetMapping("/danh-sach-ma/hieu-luc/{id}")
    public ResponseEntity<?> getVoucherDetailChuaSuDung(@PathVariable String id){
        return ResponseEntity.ok(khachHangGiamGiaService.getListDetailVoucherChuaSuDung(id)) ;
    }

    @PreAuthorize("hasRole('QUANLY')")
    @GetMapping("/danh-sach-ma/da-su-dung/{id}")
    public ResponseEntity<?> getVoucherDetailDaSuDung(@PathVariable String id){
        return ResponseEntity.ok(khachHangGiamGiaService.getListDetailVoucherDaSuDung(id)) ;
    }

    @PreAuthorize("hasRole('QUANLY')")
    @GetMapping("/danh-sach-ma/tat-ca/{id}")
    public ResponseEntity<?> getVoucherDetail(@PathVariable String id){
        return ResponseEntity.ok(khachHangGiamGiaService.getListDetailVoucher(id)) ;
    }


    @PreAuthorize("hasRole('QUANLY')")
    @GetMapping("/danh-sach-khach-hang")
    public ResponseEntity<?> getListKhachHang(){
        return ResponseEntity.ok(khachHangGiamGiaService.getListKhachHang());
    }

    @PreAuthorize("hasRole('QUANLY')")
    @DeleteMapping("/xoa-ma/{id}")
    public ResponseEntity<?> deleteMa(@PathVariable String id){
        return ResponseEntity.ok(khachHangGiamGiaService.deleteMa(id));
    }



}
    