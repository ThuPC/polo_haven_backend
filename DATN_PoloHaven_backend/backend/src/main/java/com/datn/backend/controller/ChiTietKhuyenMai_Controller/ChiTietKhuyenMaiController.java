package com.datn.backend.controller.ChiTietKhuyenMai_Controller;
import com.datn.backend.dto.request.ChiTietKhuyenMaiRequest;
import com.datn.backend.dto.response.ChiTietSanPhamResponseDTO;
import com.datn.backend.dto.response.SanPhamApDungKhuyenMaiResponse;
import com.datn.backend.entity.ChiTietKhuyenMai;
import com.datn.backend.entity.ChiTietSanPham;
import com.datn.backend.service.chi_tiet_khuyen_mai.ChiTietKhuyenMaiService;
import com.datn.backend.service.san_pham.ChiTietSanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/giam-gia/chi-tiet-khuyen-mai")
public class ChiTietKhuyenMaiController {

    @Autowired
    private ChiTietKhuyenMaiService service;

    @Autowired
    private ChiTietSanPhamService chiTietSanPhamService;
    @Autowired
    private ChiTietKhuyenMaiService chiTietKhuyenMaiService;

    @PostMapping("/add")
    public ResponseEntity<?> addMultiple(@RequestBody List<ChiTietKhuyenMaiRequest> requests) {
        try {
            List<ChiTietKhuyenMai> added = service.addMany(requests);
            return ResponseEntity.ok(added);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // Lấy tất cả chi tiết sản phẩm
    @GetMapping("/all-chi-tiet-san-pham")
    public ResponseEntity<List<ChiTietSanPhamResponseDTO>> getAllChiTietSanPham() {
        return ResponseEntity.ok(chiTietSanPhamService.getAll());
    }
    @GetMapping("/san-pham-ap-dung/{id}")
    public ResponseEntity<List<SanPhamApDungKhuyenMaiResponse>> getSanPhamKhuyenMaiById(@PathVariable String id) {
        List<SanPhamApDungKhuyenMaiResponse> result = service.getSanPhamKhuyenMaiById(id);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChiTietKhuyenMai(@PathVariable String id) {
        try {
            chiTietKhuyenMaiService.deleteById(id);
            return ResponseEntity.ok("Xoá chi tiết khuyến mãi thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}