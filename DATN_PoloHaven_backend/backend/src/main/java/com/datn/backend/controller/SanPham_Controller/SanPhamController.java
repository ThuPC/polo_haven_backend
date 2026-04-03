package com.datn.backend.controller.SanPham_Controller;

import com.datn.backend.dto.request.SanPhamRequestDTO;
import com.datn.backend.dto.response.SanPhamResponseDTO;
import com.datn.backend.dto.response.SanPhamVariantResponseDTO;
import com.datn.backend.exception.DuplicateEntityException;
import com.datn.backend.service.san_pham.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
public class SanPhamController {

    private final SanPhamTenService sanPhamTenService;
    private final SanPhamService sanPhamService;
    private final ChatLieuService chatLieuService;
    private final XuatXuService xuatXuService;
    private final ThuongHieuService thuongHieuService;
    private final MauSacService mauSacService;
    private final KichThuocService kichThuocService;
    private final SanPhamVariantService sanPhamVariantService;

    @GetMapping("/getAll")
    public Page<SanPhamResponseDTO> getAllSanPhamWithSoLuong(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Byte status
    ) {
        return sanPhamService.getAllSanPhamWithSoLuongCTSP(page, size, keyword, status);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<SanPhamResponseDTO>> searchSanPham(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
            return ResponseEntity.ok(sanPhamService.searchByKeyword(keyword, pageable));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/combobox-data")
    public ResponseEntity<Map<String, List<?>>> getComboBoxData() {
        Map<String, List<?>> data = new HashMap<>();

        data.put("tenSanPham", sanPhamTenService.getAllTenSanPham()); // List<SanPhamTenDTO>
        data.put("tenMauSac", mauSacService.getAllDistinctTenMauSac()); // List<MauSacResponseDTO>
        data.put("tenThuongHieu", thuongHieuService.getAllActiveThuongHieu()); // List<ThuongHieuResponseDTO>
        data.put("tenChatLieu", chatLieuService.getAllActiveChatLieu()); // List<ChatLieuResponseDTO>
        data.put("tenXuatXu", xuatXuService.getAllActiveXuatXu()); // List<XuatXuResponseDTO>
        data.put("tenSize", kichThuocService.getAllDistinctTenKichThuoc()); // List<KichThuocResponseDTO>

        return ResponseEntity.ok(data);
    }

    @GetMapping("/by-thuong-hieu")
    public ResponseEntity<Page<SanPhamResponseDTO>> getByThuongHieu(
            @RequestParam String thuongHieuId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
            return ResponseEntity.ok(sanPhamService.getSanPhamByThuongHieu(thuongHieuId, pageable));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SanPhamResponseDTO> getById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(sanPhamService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @ModelAttribute SanPhamRequestDTO dto) {
        try {
            SanPhamResponseDTO created = sanPhamService.create(dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Tạo sản phẩm thành công",
                    "data", created
            ));
        } catch (DuplicateEntityException e) {
            // Tên sản phẩm đã tồn tại
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (RuntimeException e) {
            // Lỗi khác
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Tạo sản phẩm thất bại: " + e.getMessage()
            ));
        }
    }


    @GetMapping("/variants/by-san-pham/{sanPhamId}")
    public ResponseEntity<List<SanPhamVariantResponseDTO>> getVariantsBySanPhamId(@PathVariable String sanPhamId) {
        List<SanPhamVariantResponseDTO> variants = sanPhamVariantService.getBySanPhamId(sanPhamId);
        return ResponseEntity.ok(variants);
    }

    @GetMapping("/san-pham/id-by-name")
    public ResponseEntity<String> getIdByTenSanPham(@RequestParam String tenSanPham) {
        String id = sanPhamService.getIdByTenSanPham(tenSanPham);
        return ResponseEntity.ok(id);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(
            @PathVariable String id,
            @Valid @RequestBody SanPhamRequestDTO dto) {
        try {
            SanPhamResponseDTO updated = sanPhamService.update(id, dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật sản phẩm thành công",
                    "data", updated
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Cập nhật sản phẩm thất bại: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/change-status/{id}")
    public ResponseEntity<?> changeStatus(@PathVariable String id, @RequestBody Map<String, Byte> body) {
        try {
            Byte newStatus = body.get("trangThai");
            SanPhamResponseDTO updated = sanPhamService.updateStatus(id, newStatus);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Thay đổi trạng thái thành công",
                    "data", updated
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Thay đổi trạng thái thất bại: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            sanPhamService.delete(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xóa sản phẩm thành công"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Xóa sản phẩm thất bại: " + e.getMessage()
            ));
        }
    }
}

