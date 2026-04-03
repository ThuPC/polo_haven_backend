package com.datn.backend.controller.SanPham_Controller;

import com.datn.backend.dto.request.KichThuocRequestDTO;
import com.datn.backend.dto.response.KichThuocResponseDTO;
import com.datn.backend.service.san_pham.KichThuocService;
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
@RequestMapping("/api/kich-thuoc")
@RequiredArgsConstructor
public class KichThuocController {

    private final KichThuocService kichThuocService;

    @GetMapping("/getAll")
    public ResponseEntity<Page<KichThuocResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(kichThuocService.getAll(pageable));
    }

    @GetMapping("/get-all-active")
    public ResponseEntity<List<KichThuocResponseDTO>> getAllActive() {
        return ResponseEntity.ok(kichThuocService.getAllActiveKichThuoc());
    }

    @GetMapping("/get-size")
    public ResponseEntity<List<KichThuocResponseDTO>> getAllSize() {
        return ResponseEntity.ok(kichThuocService.getAllSize());
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody KichThuocRequestDTO dto) {
        try {
            KichThuocResponseDTO created = kichThuocService.create(dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Tạo kích thước thành công",
                    "data", created
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Tạo kích thước thất bại: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable String id,
                                    @Valid @RequestBody KichThuocRequestDTO dto) {
        try {
            KichThuocResponseDTO updated = kichThuocService.update(id, dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật kích thước thành công",
                    "data", updated
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Cập nhật kích thước thất bại: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/change-status/{id}")
    public ResponseEntity<?> changeStatus(@PathVariable String id,
                                          @RequestBody Map<String, Byte> request) {
        try {
            Byte newStatus = request.get("trangThai");
            KichThuocResponseDTO result = kichThuocService.updateStatus(id, newStatus);
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

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            kichThuocService.delete(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xoá kích thước thành công"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Xoá kích thước thất bại: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<KichThuocResponseDTO> getById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(kichThuocService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<KichThuocResponseDTO>> search(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(kichThuocService.search(keyword, pageable));
    }

    @GetMapping("/get-all-id-size")
    public ResponseEntity<List<KichThuocResponseDTO>> getAllIdSize() {
        return ResponseEntity.ok(kichThuocService.getAllIdSize());
    }

}
