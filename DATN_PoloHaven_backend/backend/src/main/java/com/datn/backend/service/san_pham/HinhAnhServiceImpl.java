package com.datn.backend.service.san_pham;

import com.datn.backend.dto.response.HinhAnhResponseDTO;
import com.datn.backend.entity.HinhAnh;
import com.datn.backend.repository.HinhAnhRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HinhAnhServiceImpl implements HinhAnhService {

    private final HinhAnhRepo hinhAnhRepository;

    @Value("${upload.path}")
    private String uploadPath; // Giá trị tương đối: uploads/san_pham

    @Override
    public HinhAnhResponseDTO uploadImage(MultipartFile file) {
        System.out.println(">>> Đã vào uploadImage()");

        try {
            if (file.isEmpty()) {
                System.out.println(">>> File rỗng!");
                throw new RuntimeException("File rỗng!");
            }

            String originalFilename = file.getOriginalFilename();
            System.out.println(">>> Tên file gốc: " + originalFilename);

            String hash = DigestUtils.md5DigestAsHex(file.getBytes());
            System.out.println(">>> Hash của file: " + hash);

            Optional<HinhAnh> existing = hinhAnhRepository.findByHash(hash);
            if (existing.isPresent()) {
                HinhAnh ha = existing.get();
                String url = "/uploads/san_pham/" + ha.getTenAnh();
                System.out.println(">>> Ảnh đã tồn tại. Trả về URL: " + url);
                return new HinhAnhResponseDTO(ha.getId(), ha.getTenAnh(), url);
            }

            String extension = Optional.ofNullable(originalFilename)
                    .filter(f -> f.contains("."))
                    .map(f -> f.substring(f.lastIndexOf(".")))
                    .orElse("");
            String uniqueFileName = UUID.randomUUID() + extension;
            System.out.println(">>> Tên file sau khi đổi: " + uniqueFileName);

            // Dùng user.dir để build path tuyệt đối
            String absoluteUploadPath = System.getProperty("user.dir") + File.separator + uploadPath;
            System.out.println(">>> Đường dẫn ảnh upload: " + absoluteUploadPath); // <-- LOG THÊM
            File uploadDir = new File(absoluteUploadPath);
            if (!uploadDir.exists()) {
                boolean dirsCreated = uploadDir.mkdirs();
                if (dirsCreated) {
                    System.out.println(">>> Thư mục đã được tạo: " + uploadDir.getAbsolutePath());
                } else {
                    System.err.println(">>> Không thể tạo thư mục: " + uploadDir.getAbsolutePath());
                }
            }
            File destinationFile = new File(uploadDir, uniqueFileName);
            file.transferTo(destinationFile);
            System.out.println(">>> Đã lưu file tại: " + destinationFile.getAbsolutePath());

            HinhAnh hinhAnh = new HinhAnh();
            hinhAnh.setTenAnh(uniqueFileName);
            hinhAnh.setTrangThai((byte) 1);
            hinhAnh.setHash(hash);
            hinhAnh = hinhAnhRepository.save(hinhAnh);

            String url = "/uploads/san_pham/" + uniqueFileName;
            System.out.println(">>> Upload ảnh thành công, URL trả về: " + url);

            return new HinhAnhResponseDTO(hinhAnh.getId(), hinhAnh.getTenAnh(), url);
        } catch (Exception e) {
            System.out.println(">>> Lỗi khi upload ảnh: " + e.getMessage());
            throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage());
        }
    }
}
