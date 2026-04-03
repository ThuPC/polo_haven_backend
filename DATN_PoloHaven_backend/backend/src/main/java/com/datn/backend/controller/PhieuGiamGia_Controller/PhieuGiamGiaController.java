package com.datn.backend.controller.PhieuGiamGia_Controller;

import com.datn.backend.contants.RoutesConstant;
import com.datn.backend.dto.request.PhieuGiamGia_CreationRequest;
import com.datn.backend.dto.request.PhieuGiamGia_UpdateRequest;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.entity.PhieuGiamGia;
import com.datn.backend.repository.PhieuGiamGiaRepository;
import com.datn.backend.service.PhieuGiamGia_Service.PhieuGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(RoutesConstant.PREFIX_API_ADMIN +"/phieu-giam-gia")
public class PhieuGiamGiaController {

    @Autowired
    PhieuGiamGiaService phieuGiamGiaService;


    @PreAuthorize("hasRole('QUANLY')")
    @PostMapping()
    public ResponseEntity<?> TaoPhieuGiamGia(@RequestBody PhieuGiamGia_CreationRequest request){
        return ResponseEntity.ok(phieuGiamGiaService.taoPhieuGiamGia(request));
    }


    // tất cả phiếu kể cả đã xóa
    @PreAuthorize("hasRole('QUANLY')")
    @GetMapping("/full")
    public ResponseEntity<?> fullDanhSachPhieuGiamGia(){
        return ResponseEntity.ok(phieuGiamGiaService.fullDanhSachPhieuGiamGia());
    }

    @PreAuthorize("hasRole('QUANLY')")
    @GetMapping()
    public ResponseEntity<?> danhSachPhieuGiamGia(){
        return ResponseEntity.ok(phieuGiamGiaService.danhSachPhieuGiamGia());
    }

    @PreAuthorize("hasRole('QUANLY')")
    @DeleteMapping("/{id}")
    public ApiResponse<?> xoaPhieuGiamGia(@PathVariable String id){

        return ApiResponse.builder()

                .result(phieuGiamGiaService.xoaPhieuGiamGia(id))
                .build();
    }


    @PreAuthorize("hasRole('QUANLY')")
    @PutMapping()
    public ResponseEntity<?> capNhatPhieuGiamGia(@RequestBody PhieuGiamGia_UpdateRequest request){
        return ResponseEntity.ok(phieuGiamGiaService.capNhatPhieuGiamGia(request));
    }

    @PreAuthorize("hasRole('QUANLY')")
    @PutMapping("/thay-doi-trang-thai/{id}")
    public ResponseEntity<?> capNhatTrangThai(@PathVariable String id){
        return ResponseEntity.ok(phieuGiamGiaService.capNhatTrangThai(id));
    }

}
    