package com.datn.backend.controller.SanPham_Controller;

import com.datn.backend.dto.request.MauSacRequestDTO;
import com.datn.backend.dto.response.MauSacResponseDTO;
import com.datn.backend.service.san_pham.MauSacService;
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
@RequestMapping("/api/mau-sac")
@RequiredArgsConstructor
public class MauSacController {

    private final MauSacService mauSacService;

    @GetMapping("/getAll")
    public ResponseEntity<Page<MauSacResponseDTO>> getAllThuongHieu(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "") String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(mauSacService.search(keyword, pageable));
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody MauSacRequestDTO dto) {
        try {
            MauSacResponseDTO created = mauSacService.create(dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Tạo màu sắc thành công",
                    "data", created
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Tạo màu sắc thất bại: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable String id,
                                    @Valid @RequestBody MauSacRequestDTO dto) {
        try {
            MauSacResponseDTO updated = mauSacService.update(id, dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật màu sắc thành công",
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
            MauSacResponseDTO result = mauSacService.updateStatus(id, newStatus);
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
    public ResponseEntity<MauSacResponseDTO> getById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(mauSacService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/get-all-ten")
    public ResponseEntity<List<MauSacResponseDTO>> getAllIdTenMau() {
        return ResponseEntity.ok(mauSacService.getAllIdTenMauSac());
    }

}
