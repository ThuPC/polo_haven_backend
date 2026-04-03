package com.datn.backend.controller.TraHang_Controller;

import com.datn.backend.dto.response.DonTraHangResponse;
import com.datn.backend.service.tra_hang.TraHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/tra-hang")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminTraHangController {

    private final TraHangService traHangService;

    @PreAuthorize("hasAnyRole('QUANLY', 'NHANVIEN')")
    @GetMapping
    public ResponseEntity<?> getAllDonTraHang() {
        try {
            List<DonTraHangResponse> responses = traHangService.getAllDonTraHang();
            return ResponseEntity.ok(Map.of("success", true, "data", responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyRole('QUANLY', 'NHANVIEN')")
    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<?> getDonTraHangByTrangThai(@PathVariable Byte trangThai) {
        try {
            List<DonTraHangResponse> responses = traHangService.getDanhSachDonTraHangByTrangThai(trangThai);
            return ResponseEntity.ok(Map.of("success", true, "data", responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyRole('QUANLY', 'NHANVIEN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getDonTraHangById(@PathVariable String id) {
        try {
            DonTraHangResponse response = traHangService.getDonTraHangById(id);
            return ResponseEntity.ok(Map.of("success", true, "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyRole('QUANLY', 'NHANVIEN')")
    @PutMapping("/{id}/chap-nhan")
    public ResponseEntity<?> chapNhanDonTraHang(
            @PathVariable String id,
            @RequestParam String ghiChuAdmin,
            @RequestBody(required = false) Map<String, Byte> tinhTrangHangMap) {
        try {
            DonTraHangResponse response = traHangService.chapNhanDonTraHang(id, ghiChuAdmin, tinhTrangHangMap);
            return ResponseEntity.ok(Map.of("success", true, "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyRole('QUANLY', 'NHANVIEN')")
    @PutMapping("/{id}/tu-choi")
    public ResponseEntity<?> tuChoiDonTraHang(
            @PathVariable String id,
            @RequestParam String ghiChuAdmin) {
        try {
            DonTraHangResponse response = traHangService.tuChoiDonTraHang(id, ghiChuAdmin);
            return ResponseEntity.ok(Map.of("success", true, "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyRole('QUANLY', 'NHANVIEN')")
    @PutMapping("/{id}/hoan-thanh")
    public ResponseEntity<?> hoanThanhDonTraHang(
            @PathVariable String id,
            @RequestParam String ghiChuAdmin) {
        try {
            DonTraHangResponse response = traHangService.hoanThanhDonTraHang(id, ghiChuAdmin);
            return ResponseEntity.ok(Map.of("success", true, "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}