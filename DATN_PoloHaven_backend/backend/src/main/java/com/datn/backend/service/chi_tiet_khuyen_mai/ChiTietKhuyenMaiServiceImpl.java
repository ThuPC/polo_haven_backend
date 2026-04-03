package com.datn.backend.service.chi_tiet_khuyen_mai;

import com.datn.backend.dto.request.ChiTietKhuyenMaiRequest;
import com.datn.backend.dto.response.SanPhamApDungKhuyenMaiResponse;
import com.datn.backend.entity.ChiTietKhuyenMai;
import com.datn.backend.entity.ChiTietSanPham;
import com.datn.backend.entity.KhuyenMai;
import com.datn.backend.repository.ChiTietKhuyenMaiRepository;
import com.datn.backend.repository.ChiTietSanPhamRepository;
import com.datn.backend.repository.KhuyenMaiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChiTietKhuyenMaiServiceImpl implements ChiTietKhuyenMaiService {
    @Autowired
    private ChiTietKhuyenMaiRepository repository;

    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Autowired
    private KhuyenMaiRepository khuyenMaiRepository;
    @Autowired
    private ChiTietKhuyenMaiRepository chiTietKhuyenMaiRepository;

    @Override
    public List<ChiTietKhuyenMai> addMany(List<ChiTietKhuyenMaiRequest> requests) {
        List<ChiTietKhuyenMai> result = new ArrayList<>();
        for (ChiTietKhuyenMaiRequest req : requests) {
            //Lấy chi tiết sản phẩm
            ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(req.getIdChiTietSanPham())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết sản phẩm: " + req.getIdChiTietSanPham()));
            //Lấy khuyến mãi
            KhuyenMai khuyenMai = khuyenMaiRepository.findById(req.getIdKhuyenMai())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi: " + req.getIdKhuyenMai()));
            //Trạng thái khuyến mãi (0: sắp tới, 1: đang diễn ra, 2: hết hạn, 3: kết thúc sớm, 4: áp dụng sớm)
            byte trangThaiKM = khuyenMai.getTrangThai() != null ? khuyenMai.getTrangThai() : 0;
            //Lấy các CTKM bị trùng thời gian
            List<ChiTietKhuyenMai> overlaps = repository.findOverlappingKhuyenMai(
                    chiTietSanPham.getId(),
                    khuyenMai.getNgayBatDau(),
                    khuyenMai.getNgayKetThuc()
            );
            //Lọc các CTKM đang còn hiệu lực gây xung đột
            List<ChiTietKhuyenMai> conflictOverlaps = overlaps.stream()
                    .filter(o -> {
                        byte trangThaiKMKhac = o.getKhuyenMai().getTrangThai() != null ? o.getKhuyenMai().getTrangThai() : 0;
                        // Bỏ qua các khuyến mãi đã hết hoặc kết thúc sớm
                        if (trangThaiKMKhac == 2 || trangThaiKMKhac == 3) return false;
                        // Nếu khuyến mãi mới là SẮP TỚI (trangThaiKM = 0)
                        if (trangThaiKM == 0) {
                            // Chỉ chặn nếu khuyến mãi đang tồn tại là đang diễn ra (1) hoặc áp dụng sớm (4)
                            return trangThaiKMKhac == 1 || trangThaiKMKhac == 4;
                        }
                        // Nếu khuyến mãi mới là đang diễn ra hoặc áp dụng sớm thì chặn nếu bất kỳ cái nào khác cũng đang áp dụng
                        return true;
                    })
                    .toList();
            //Nếu có xung đột thì thông báo lỗi
            if (!conflictOverlaps.isEmpty()) {
                String tenKms = conflictOverlaps.stream()
                        .map(o -> o.getKhuyenMai().getTenKhuyenMai())
                        .collect(Collectors.joining(", "));
                throw new RuntimeException("Sản phẩm '" + chiTietSanPham.getTenCTSP()
                        + "' đã có khuyến mãi '" + tenKms
                        + "' đang hoặc sắp diễn ra trong thời gian bạn chọn.");
            }
            //Tính tiền giảm giá: đơn giá * % giảm / 100
            BigDecimal donGia = chiTietSanPham.getDonGia();
            BigDecimal phanTramGiam = khuyenMai.getPhanTramGiam();
            BigDecimal soTienGiam = donGia.multiply(phanTramGiam).divide(BigDecimal.valueOf(100));
            //Tạo mới CTKM
            ChiTietKhuyenMai entity = ChiTietKhuyenMai.builder()
                    .chiTietSanPham(chiTietSanPham)
                    .khuyenMai(khuyenMai)
                    .maSanPhamKhuyenMai("KM-" + chiTietSanPham.getMaCTSP())
                    .trangThai(trangThaiKM)
                    .build();
            result.add(entity);
        }
        return repository.saveAll(result);
    }

    @Override
    public List<SanPhamApDungKhuyenMaiResponse> getSanPhamKhuyenMaiById(String id) {
        return repository.getSanPhamKhuyenMaiById(id);
    }

    @Override
    public void deleteById(String id) {
        ChiTietKhuyenMai chiTiet = chiTietKhuyenMaiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết khuyến mãi"));
        chiTietKhuyenMaiRepository.deleteById(id);
    }

}
