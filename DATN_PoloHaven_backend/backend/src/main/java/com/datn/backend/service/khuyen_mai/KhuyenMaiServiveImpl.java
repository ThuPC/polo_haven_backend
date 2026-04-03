package com.datn.backend.service.khuyen_mai;

import com.datn.backend.dto.request.KhuyenMaiRequest;
import com.datn.backend.dto.response.KhuyenMaiResponse;
import com.datn.backend.entity.ChiTietKhuyenMai;
import com.datn.backend.entity.KhuyenMai;
import com.datn.backend.repository.ChiTietKhuyenMaiRepository;
import com.datn.backend.repository.KhuyenMaiRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
//code 1/7
@Service
@RequiredArgsConstructor
public class KhuyenMaiServiveImpl implements KhuyenMaiService {
    private final KhuyenMaiRepository repository;
    private final KhuyenMaiRepository khuyenMaiRepository;
    private final ChiTietKhuyenMaiRepository chiTietKhuyenMaiRepository;

    private KhuyenMaiResponse mapToResponse(KhuyenMai entity) {
        return new KhuyenMaiResponse(
                entity.getId(),
                entity.getMaKMTCT(),
                entity.getTenKhuyenMai(),
                entity.getMoTa(),
                entity.getPhanTramGiam(),
                entity.getNgayBatDau(),
                entity.getNgayKetThuc(),
                entity.getNgayTao(),
                entity.getTrangThai()
        );
    }

    @Override
    public KhuyenMaiResponse createKhuyenMai(KhuyenMaiRequest requestDTO) {
        KhuyenMai entity = KhuyenMai.builder()
                .maKMTCT(generateMaKhuyenMai())
                .tenKhuyenMai(requestDTO.getTenKhuyenMai())
                .moTa(requestDTO.getMoTa())
                .phanTramGiam(requestDTO.getPhanTramGiam())
                .ngayBatDau(requestDTO.getNgayBatDau())
                .ngayKetThuc(requestDTO.getNgayKetThuc())
                .ngayTao(LocalDateTime.now())
                .trangThai(requestDTO.getTrangThai())
                .build();
        return mapToResponse(repository.save(entity));
    }

    private String generateMaKhuyenMai() {
        String letters = getRandomLetters(3);
        int numbers = 100 + new Random().nextInt(900);
        return "KM-" + letters + numbers;
    }

    private String getRandomLetters(int length) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    @Override
    public KhuyenMaiResponse updateKhuyenMai(String id, KhuyenMaiRequest requestDTO) {
        KhuyenMai entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi với ID: " + id));

        entity.setMaKMTCT(requestDTO.getMaKMTCT());
        entity.setTenKhuyenMai(requestDTO.getTenKhuyenMai());
        entity.setMoTa(requestDTO.getMoTa());
        entity.setPhanTramGiam(requestDTO.getPhanTramGiam());
        entity.setNgayBatDau(requestDTO.getNgayBatDau());
        entity.setNgayKetThuc(requestDTO.getNgayKetThuc());
        entity.setTrangThai(requestDTO.getTrangThai());

        // CHỈ ghi đè trạng thái nếu người dùng gửi lên (ví dụ set trạng thái kết thúc sớm)
        if (requestDTO.getTrangThai() != null) {
            entity.setTrangThai(requestDTO.getTrangThai());
            khuyenMaiRepository.save(entity);

            // Đồng bộ trạng thái chi tiết CTKM nếu có thay đổi trạng thái từ người dùng
            chiTietKhuyenMaiRepository.updateTrangThaiByKhuyenMaiId(requestDTO.getTrangThai(), entity.getId());
        }

        return mapToResponse(repository.save(entity));
    }

    @Override
    public void deleteKhuyenMai(String id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy khuyến mãi để xoá với ID: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public KhuyenMaiResponse getKhuyenMaiById(String id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi với ID: " + id));
    }

    @Override
    public Page<KhuyenMaiResponse> getAllKhuyenMai(Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "ngayTao")
        );
        return repository.findAll(sortedPageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public String toggleKhuyenMai(KhuyenMai req) {
        KhuyenMai km = khuyenMaiRepository.findById(req.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi"));

        validateBeforeToggleInternal(km, req.getTrangThai());

        km.setTrangThai(req.getTrangThai());
        khuyenMaiRepository.save(km);
        return "Cập nhật trạng thái thành công!";
    }

    private void validateBeforeToggleInternal(KhuyenMai km, Byte newStatus) {
        if (newStatus == 1 || newStatus == 4) {
            List<ChiTietKhuyenMai> ctkmList = chiTietKhuyenMaiRepository.findAllByKhuyenMaiId(km.getId());
            for (ChiTietKhuyenMai ctkm : ctkmList) {
                String idSp = ctkm.getChiTietSanPham().getId();
                List<ChiTietKhuyenMai> conflicts = chiTietKhuyenMaiRepository.findOverlappingKhuyenMai(
                        idSp,
                        km.getNgayBatDau(),
                        km.getNgayKetThuc()
                );
                boolean conflict = conflicts.stream()
                        .anyMatch(o -> !o.getKhuyenMai().getId().equals(km.getId())
                                && o.getTrangThai() != 2 && o.getTrangThai() != 3);
                if (conflict) {
                    throw new RuntimeException("Sản phẩm " + ctkm.getChiTietSanPham().getTenCTSP() +
                            " đã nằm trong khuyến mãi khác đang hoặc sắp áp dụng. Không thể bật lại.");
                }
            }
        }
    }

    @Override
    public void validateBeforeToggle(String id, Byte newStatus) {
        KhuyenMai km = khuyenMaiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi"));
        validateBeforeToggleInternal(km, newStatus);
    }
}
