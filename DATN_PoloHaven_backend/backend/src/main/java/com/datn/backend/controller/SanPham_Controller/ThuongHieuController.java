package com.datn.backend.controller.SanPham_Controller;

import com.datn.backend.dto.request.ThuongHieuRequestDTO;
import com.datn.backend.dto.response.ThuongHieuResponseDTO;
import com.datn.backend.service.san_pham.ThuongHieuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/thuong-hieu")
@RequiredArgsConstructor
public class ThuongHieuController {

    private final ThuongHieuService thuongHieuService;

    @GetMapping("/getAll")
    public ResponseEntity<Page<ThuongHieuResponseDTO>> getAllThuongHieu(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "") String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(thuongHieuService.search(keyword, pageable));
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody ThuongHieuRequestDTO dto) {
        try {
            ThuongHieuResponseDTO created = thuongHieuService.create(dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Tạo thương hiệu thành công",
                    "data", created
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Tạo thương hiệu thất bại: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable String id,
                                    @Valid @RequestBody ThuongHieuRequestDTO dto) {
        try {
            ThuongHieuResponseDTO updated = thuongHieuService.update(id, dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật thương hiệu thành công",
                    "data", updated
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Cập nhật thất bại: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/change-status/{id}")
    public ResponseEntity<?> changeStatus(@PathVariable String id, @RequestBody Map<String, Byte> request) {
        try {
            Byte newStatus = request.get("trangThai");
            ThuongHieuResponseDTO result = thuongHieuService.updateStatus(id, newStatus);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật trạng thái thành công",
                    "data", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Cập nhật trạng thái thất bại: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThuongHieuResponseDTO> getById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(thuongHieuService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/get-all-ten")
    public ResponseEntity<List<ThuongHieuResponseDTO>> getAllTenThuongHieu() {
        return ResponseEntity.ok(thuongHieuService.getAllActiveThuongHieu());
    }

}
