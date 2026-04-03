package com.datn.backend.service.khuyen_mai;

import com.datn.backend.entity.KhuyenMai;
import com.datn.backend.repository.KhuyenMaiRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class KhuyenMaiAutoUpdater {
    private final KhuyenMaiRepository khuyenMaiRepository;

    public KhuyenMaiAutoUpdater(KhuyenMaiRepository khuyenMaiRepository) {
        this.khuyenMaiRepository = khuyenMaiRepository;
    }

    // ✅ Chạy khi khởi động project
    @PostConstruct
    public void init() {
        capNhatTrangThaiKhuyenMai();
        System.out.println("Đã cập nhật trạng thái khuyến mãi khi start ứng dụng.");
    }

    // ✅ Chạy mỗi ngày lúc 1h sáng
    @Scheduled(cron = "0 0 1 * * *")
    public void capNhatTrangThaiKhuyenMai() {
        LocalDate today = LocalDate.now();
        List<KhuyenMai> khuyenMais = khuyenMaiRepository.findAll();

        for (KhuyenMai km : khuyenMais) {
            if (km.getNgayBatDau() != null && km.getNgayKetThuc() != null) {
                byte current = km.getTrangThai() != null ? km.getTrangThai() : 0;

                // 👉 Chỉ xử lý nếu trạng thái là 0 hoặc 1
                if (current == 0 || current == 1) {
                    byte newStatus = today.isBefore(km.getNgayBatDau()) ? (byte) 0
                            : today.isAfter(km.getNgayKetThuc()) ? (byte) 2
                            : (byte) 1;

                    if (newStatus != current) {
                        km.setTrangThai(newStatus);
                        khuyenMaiRepository.save(km);
                        System.out.println("KM [" + km.getMaKMTCT() + "] cập nhật trạng thái → " + newStatus);
                    }
                }
            }
        }

        System.out.println("Cập nhật trạng thái khuyến mãi hoàn tất.");
    }
}
