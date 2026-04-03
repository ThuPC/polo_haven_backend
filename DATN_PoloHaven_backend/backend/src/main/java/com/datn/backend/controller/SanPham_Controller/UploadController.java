package com.datn.backend.controller.SanPham_Controller;

import com.datn.backend.dto.response.HinhAnhResponseDTO;
import com.datn.backend.service.san_pham.HinhAnhService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final HinhAnhService hinhAnhService;

    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        System.out.println(">>> Đã nhận file: " + file.getOriginalFilename());
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("Không có file hoặc file rỗng.");
            }

            HinhAnhResponseDTO response = hinhAnhService.uploadImage(file);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi upload ảnh: " + e.getMessage());
        }
    }
}

