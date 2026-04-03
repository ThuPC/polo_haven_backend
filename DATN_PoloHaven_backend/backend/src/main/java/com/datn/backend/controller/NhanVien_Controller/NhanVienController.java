package com.datn.backend.controller.NhanVien_Controller;

import com.datn.backend.dto.request.NhanVienRequestDTO;
import com.datn.backend.dto.response.NhanVienResponseDTO;
import com.datn.backend.service.nhan_vien.NhanVienService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nhan-vien")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NhanVienController {

    private final NhanVienService nhanVienService;

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllNhanVien(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "ngayTao") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String gioiTinh) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Byte statusByte = status != null && !status.isEmpty() ? Byte.parseByte(status) : null;
            Byte gioiTinhByte = gioiTinh != null && !gioiTinh.isEmpty() ? Byte.parseByte(gioiTinh) : null;

            Page<NhanVienResponseDTO> result;
            if (hasFilters(keyword, statusByte, gioiTinhByte)) {
                result = nhanVienService.findAllWithFilters(pageable, keyword, statusByte, gioiTinhByte);
            } else {
                result = nhanVienService.findAll(pageable);
            }

            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .body(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi khi lấy danh sách nhân viên: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<NhanVienResponseDTO> getNhanVienById(@PathVariable String id) {
        try {
            NhanVienResponseDTO nhanVien = nhanVienService.findById(id);
            return ResponseEntity.ok(nhanVien);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('QUANLY')")
    @PostMapping("/create")
    public ResponseEntity<?> createNhanVien(@ModelAttribute NhanVienRequestDTO dto) {
        try {
            NhanVienResponseDTO result = nhanVienService.create(dto);
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .body(Map.of(
                    "success", true,
                    "message", "Tạo nhân viên thành công",
                    "data", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Tạo nhân viên thất bại: " + e.getMessage()
            ));
        }
    }

    @PreAuthorize("hasRole('QUANLY')")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateNhanVien(
            @PathVariable String id,
            @ModelAttribute NhanVienRequestDTO dto) {
        try {
            NhanVienResponseDTO result = nhanVienService.update(id, dto);
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .body(Map.of(
                    "success", true,
                    "message", "Cập nhật nhân viên thành công",
                    "data", result
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Cập nhật nhân viên thất bại: " + e.getMessage()
            ));
        }
    }

    @PreAuthorize("hasRole('QUANLY')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteNhanVien(@PathVariable String id) {
        try {
            nhanVienService.delete(id);
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .body(Map.of(
                    "success", true,
                    "message", "Xóa nhân viên thành công"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Xóa nhân viên thất bại: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/reset-password/{id}")
    public ResponseEntity<Void> resetPassword(@PathVariable String id) {
        nhanVienService.resetPassword(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('QUANLY')")
    @GetMapping("/export-excel")
    public ResponseEntity<Resource> exportExcel() {
        Resource resource = nhanVienService.exportExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=nhan_vien.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PreAuthorize("hasRole('QUANLY')")
    @PostMapping("/import-excel")
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "File Excel không hợp lệ hoặc trống"
                ));
            }
            List<NhanVienResponseDTO> results = nhanVienService.importExcel(file);
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .body(Map.of(
                    "success", true,
                    "message", "Nhập Excel thành công: " + results.size() + " bản ghi được thêm",
                    "data", results
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getReason()
            ));
        } catch (Exception e) {
            e.printStackTrace(); // Log lỗi chi tiết
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Nhập Excel thất bại: " + e.getMessage()
            ));
        }
    }
    @PreAuthorize("hasRole('QUANLY')")  // Nếu cần quyền
    @GetMapping("/download-template")
    public ResponseEntity<Resource> downloadTemplate() {
        Resource resource = nhanVienService.downloadTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=template_nhan_vien.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
    private boolean hasFilters(String keyword, Byte status, Byte gioiTinh) {
        return (keyword != null && !keyword.trim().isEmpty()) ||
                status != null ||
                gioiTinh != null;
    }

}