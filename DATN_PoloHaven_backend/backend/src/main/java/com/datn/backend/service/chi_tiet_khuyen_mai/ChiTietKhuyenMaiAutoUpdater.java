package com.datn.backend.service.chi_tiet_khuyen_mai;

import com.datn.backend.entity.ChiTietKhuyenMai;
import com.datn.backend.repository.ChiTietKhuyenMaiRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class ChiTietKhuyenMaiAutoUpdater {
    private final ChiTietKhuyenMaiRepository chiTietKhuyenMaiRepo;

    @PostConstruct
    public void runOnStartup() {
        capNhatTrangThaiChiTiet();
        System.out.println("Đã đồng bộ trạng thái CTKM khi start ứng dụng.");
    }

    @Scheduled(cron = "0 0 2 * * *") // Mỗi ngày lúc 2h sáng
    public void capNhatTrangThaiChiTiet() {
        var list = chiTietKhuyenMaiRepo.findAll();
        for (ChiTietKhuyenMai ct : list) {
            var km = ct.getKhuyenMai();
            if (km != null) {
                // Nếu khuyến mãi đã hết hạn (trạng thái = 2), xóa CTKM luôn
                if (km.getTrangThai() == 2) {
                    chiTietKhuyenMaiRepo.delete(ct);
                    System.out.println("Xóa CTKM vì KM hết hạn: " + ct.getMaSanPhamKhuyenMai());
                    continue;
                }
                if (km.getNgayKetThuc().isBefore(LocalDate.now())) {
                    chiTietKhuyenMaiRepo.delete(ct);
                    System.out.println("Xóa CTKM vì KM hết hạn: " + ct.getMaSanPhamKhuyenMai());
                    continue;
                }
                // Nếu khác trạng thái thì đồng bộ lại
                if (!ct.getTrangThai().equals(km.getTrangThai())) {
                    ct.setTrangThai(km.getTrangThai());
                    chiTietKhuyenMaiRepo.save(ct);
                    System.out.println("Đồng bộ CTKM: " + ct.getMaSanPhamKhuyenMai() + " → " + km.getTrangThai());
                }
            }
        }
    }
}
