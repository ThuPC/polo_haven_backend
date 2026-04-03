package com.datn.backend.service.san_pham;

import com.datn.backend.dto.request.*;
import com.datn.backend.dto.response.BanHangOnline.*;
import com.datn.backend.dto.response.SanPhamResponseDTO;
import com.datn.backend.entity.*;
import com.datn.backend.mapper.ChatLieuMapper;
import com.datn.backend.mapper.SanPhamMapper;
import com.datn.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SanPhamServiceImpl implements SanPhamService {

    private final SanPhamRepo sanPhamRepository;
    private final ChatLieuRepo chatLieuRepository;
    private final XuatXuRepo xuatXuRepository;
    private final ThuongHieuRepo thuongHieuRepository;
    private final SanPhamMapper sanPhamMapper;
    private final MauSacRepository mauSacRepository;
    private final KichThuocRepository kichCoRepository;
    private final SanPhamVariantRepo sanPhamVariantRepository;
    private final HinhAnhRepo hinhAnhRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Override
    public Page<SanPhamResponseDTO> getAllSanPhamWithSoLuongCTSP(int page, int size, String keyword, Byte status) {
        Pageable pageable = PageRequest.of(page, size);

        String searchKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : "%" + keyword.trim() + "%";

        return sanPhamRepository.getAllSanPhamWithSoLuongCTSP(searchKeyword, status, pageable);
    }

    @Override
    public SanPhamResponseDTO create(SanPhamRequestDTO dto) {
        // Kiểm tra tên sản phẩm đã tồn tại chưa (ignore case, trim)
        boolean exists = sanPhamRepository.existsByTenSanPhamIgnoreCase(dto.getTenSanPham().trim());
        if (exists) {
            throw new RuntimeException("Tên sản phẩm đã tồn tại");
        }

        ChatLieu chatLieu = chatLieuRepository.findById(dto.getChatLieuId())
                .orElseThrow(() -> new RuntimeException("Chất liệu không tồn tại"));
        XuatXu xuatXu = xuatXuRepository.findById(dto.getXuatXuId())
                .orElseThrow(() -> new RuntimeException("Xuất xứ không tồn tại"));
        ThuongHieu thuongHieu = thuongHieuRepository.findById(dto.getThuongHieuId())
                .orElseThrow(() -> new RuntimeException("Thương hiệu không tồn tại"));

        SanPham sanPham = sanPhamMapper.toEntity(dto);

        if (sanPham.getMaSanPham() == null || sanPham.getMaSanPham().isBlank()) {
            String randomMa = "SP" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            sanPham.setMaSanPham(randomMa);
        }

        sanPham.setChatLieu(chatLieu);
        sanPham.setXuatXu(xuatXu);
        sanPham.setThuongHieu(thuongHieu);
        sanPham.setNgayTao(System.currentTimeMillis());

        return sanPhamMapper.toResponseDTO(sanPhamRepository.save(sanPham));
    }

    @Override
    public SanPhamResponseDTO update(String id, SanPhamRequestDTO dto) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        ChatLieu chatLieu = chatLieuRepository.findById(dto.getChatLieuId())
                .orElseThrow(() -> new RuntimeException("Chất liệu không tồn tại"));
        XuatXu xuatXu = xuatXuRepository.findById(dto.getXuatXuId())
                .orElseThrow(() -> new RuntimeException("Xuất xứ không tồn tại"));
        ThuongHieu thuongHieu = thuongHieuRepository.findById(dto.getThuongHieuId())
                .orElseThrow(() -> new RuntimeException("Thương hiệu không tồn tại"));

        sanPhamMapper.updateEntityFromRequestDTO(dto, sanPham);
        sanPham.setChatLieu(chatLieu);
        sanPham.setXuatXu(xuatXu);
        sanPham.setThuongHieu(thuongHieu);

        return sanPhamMapper.toResponseDTO(sanPhamRepository.save(sanPham));
    }

    @Override
    public void delete(String id) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        sanPhamRepository.delete(sanPham);
    }

    @Override
    public SanPhamResponseDTO findById(String id) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        return sanPhamMapper.toResponseDTO(sanPham);
    }

    @Override
    public Page<SanPhamResponseDTO> findAll(Pageable pageable) {
        return sanPhamRepository.findAll(pageable)
                .map(sanPhamMapper::toResponseDTO);
    }

    @Override
    public Page<SanPhamResponseDTO> getSanPhamByThuongHieu(String thuongHieuId, Pageable pageable) {
        return sanPhamRepository.findByThuongHieuId(thuongHieuId, pageable)
                .map(sanPhamMapper::toResponseDTO);
    }

    @Override
    public Page<SanPhamResponseDTO> searchByKeyword(String keyword, Pageable pageable) {
        return sanPhamRepository.searchByKeyword(keyword, pageable)
                .map(sanPhamMapper::toResponseDTO);
    }

    @Override
    public Page<SanPhamResponseDTO> findAllWithFilters(Pageable pageable, String keyword, Byte status) {
        return sanPhamRepository.findWithFilters(keyword, status, pageable)
                .map(sanPhamMapper::toResponseDTO);
    }

    @Override
    public List<String> getAllTenSanPham() {
        return sanPhamRepository.findAllTenSanPham();
    }

    @Override
    public SanPhamResponseDTO updateStatus(String id, Byte trangThai) {
        SanPham entity = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        entity.setTrangThai(trangThai);
        return sanPhamMapper.toResponseDTO(sanPhamRepository.save(entity));
    }

    @Override
    public void createMultipleVariants(SanPhamVariantsRequestDTO dto) {
        SanPham sp = sanPhamRepository.findById(dto.getSanPhamId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        List<SanPhamVariant> variants = new ArrayList<>();

        for (SanPhamVariantRequestDTO variantDto : dto.getVariants()) {
            MauSac mauSac = mauSacRepository.findById(variantDto.getMauSacId())
                    .orElseThrow(() -> new RuntimeException("Màu sắc không tồn tại: " + variantDto.getMauSacId()));

            KichThuoc kichThuoc = kichCoRepository.findById(variantDto.getKichCoId())
                    .orElseThrow(() -> new RuntimeException("Kích cỡ không tồn tại: " + variantDto.getKichCoId()));

            HinhAnh hinhAnh = null;
            if (variantDto.getHinhAnhId() != null) {
                hinhAnh = hinhAnhRepository.findById(variantDto.getHinhAnhId())
                        .orElseThrow(() -> new RuntimeException("Hình ảnh không tồn tại: " + variantDto.getHinhAnhId()));
            }

            SanPhamVariant variant = new SanPhamVariant();
            variant.setSanPham(sp);
            variant.setMauSac(mauSac);
            variant.setKichCo(kichThuoc);
            variant.setHinhAnh(hinhAnh);

            variant.setGiaBan(variantDto.getGiaBan() != null ? variantDto.getGiaBan() : 0.0);
            variant.setSoLuong(variantDto.getSoLuong() != null ? variantDto.getSoLuong() : 0);
            variant.setTrangThai(variantDto.getTrangThai() != null ? variantDto.getTrangThai() : 1);

            variants.add(variant);
        }

        sanPhamVariantRepository.saveAll(variants);
    }

    @Override
    public String getIdByTenSanPham(String tenSanPham) {
        SanPham sanPham = sanPhamRepository.findByTenSanPham(tenSanPham)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với tên: " + tenSanPham));
        return sanPham.getId();
    }

    @Override
    public List<SanPhamResponse> listSanPhamMoiVe() {
        List<SanPhamSummaryDTO> summaries = sanPhamRepository.getListSanPhamSummaries();
        if (summaries.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> sanPhamIds = summaries.stream()
                .map(SanPhamSummaryDTO::getId)
                .collect(Collectors.toList());
        List<ChiTietMauSacKichThuocDTO> details = chiTietSanPhamRepository.findAllDetailsBySanPhamIds(sanPhamIds);
        Map<String, List<ChiTietMauSacKichThuocDTO>> detailsMap = details.stream()
                .collect(Collectors.groupingBy(ChiTietMauSacKichThuocDTO::getIdSanPham));
        return summaries.stream().map(summary -> {

            SanPhamResponse response = new SanPhamResponse();
            BeanUtils.copyProperties(summary, response);

            List<ChiTietMauSacKichThuocDTO> currentDetails = detailsMap.getOrDefault(summary.getId(), Collections.emptyList());
            List<MauSacResponse> mauSacList = currentDetails.stream()
                    .map(d -> new MauSacResponse(d.getIdMauSac(),d.getTenMauSac(),d.getMaMauSac()))
                    .distinct()
                    .collect(Collectors.toList());
            List<KichThuocResponse> kichThuocList = currentDetails.stream()
                    .map(d -> new KichThuocResponse(d.getIdKichThuoc(), d.getTenKichThuoc()))
                    .distinct()
                    .collect(Collectors.toList());
            List<String> imageList = currentDetails.stream()
                    .map(ChiTietMauSacKichThuocDTO::getTenAnh)
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(tenAnh -> "http://localhost:8080" + "/uploads/san_pham/" + tenAnh)
                    .collect(Collectors.toList());
            response.setImageList(imageList);
            response.setMauSacList(mauSacList);
            response.setKichThuocList(kichThuocList);

            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SanPhamResponse> listSanPham() {
        List<SanPhamSummaryDTO> summaries = sanPhamRepository.getListSanPhamTatCa();
        if (summaries.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> sanPhamIds = summaries.stream()
                .map(SanPhamSummaryDTO::getId)
                .collect(Collectors.toList());
        List<ChiTietMauSacKichThuocDTO> details = chiTietSanPhamRepository.findAllDetailsBySanPhamIds(sanPhamIds);
        Map<String, List<ChiTietMauSacKichThuocDTO>> detailsMap = details.stream()
                .collect(Collectors.groupingBy(ChiTietMauSacKichThuocDTO::getIdSanPham));
        return summaries.stream().map(summary -> {

            SanPhamResponse response = new SanPhamResponse();
            BeanUtils.copyProperties(summary, response);

            List<ChiTietMauSacKichThuocDTO> currentDetails = detailsMap.getOrDefault(summary.getId(), Collections.emptyList());
            List<MauSacResponse> mauSacList = currentDetails.stream()
                    .map(d -> new MauSacResponse(d.getIdMauSac(),d.getTenMauSac(),d.getMaMauSac()))
                    .distinct()
                    .collect(Collectors.toList());
            List<KichThuocResponse> kichThuocList = currentDetails.stream()
                    .map(d -> new KichThuocResponse(d.getIdKichThuoc(), d.getTenKichThuoc()))
                    .distinct()
                    .collect(Collectors.toList());
            List<String> imageList = currentDetails.stream()
                    .map(ChiTietMauSacKichThuocDTO::getTenAnh)
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(tenAnh -> "http://localhost:8080" + "/uploads/san_pham/" + tenAnh)
                    .collect(Collectors.toList());
            response.setImageList(imageList);
            response.setMauSacList(mauSacList);
            response.setKichThuocList(kichThuocList);

            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SanPhamResponse> listSanPhamFill(SanPhamFilterRequest request) {
        // Lấy danh sách sản phẩm tóm tắt
        List<SanPhamSummaryDTO> summaries = sanPhamRepository.getListSanPhamTatCa();

        // Lọc theo thương hiệu
        if (request.getThuongHieuIds() != null && !request.getThuongHieuIds().isEmpty()) {
            summaries = summaries.stream()
                    .filter(s -> request.getThuongHieuIds().contains(s.getThuongHieu()))
                    .collect(Collectors.toList());
        }

        // Lấy danh sách ID sản phẩm
        List<String> sanPhamIds = summaries.stream()
                .map(SanPhamSummaryDTO::getId)
                .toList();

        if (sanPhamIds.isEmpty()) return Collections.emptyList();

        // Lấy tất cả chi tiết biến thể theo sản phẩm
        List<ChiTietMauSacKichThuocDTOFill> details =
                chiTietSanPhamRepository.findAllDetailsBySanPhamIdsFill(sanPhamIds);

        // Lọc theo màu sắc, kích thước, chất liệu, giá
        details = details.stream().filter(d -> {
            boolean matchMauSac = request.getMauSacIds() == null || request.getMauSacIds().isEmpty()
                    || request.getMauSacIds().contains(d.getIdMauSac());

            boolean matchKichThuoc = request.getKichThuocIds() == null || request.getKichThuocIds().isEmpty()
                    || request.getKichThuocIds().contains(d.getIdKichThuoc());

            // Lọc theo chất liệu
            boolean matchChatLieu = request.getChatLieuIds() == null || request.getChatLieuIds().isEmpty()
                    || request.getChatLieuIds().contains(d.getIdChatLieu());

            // Lọc theo giá
            BigDecimal gia = d.getGiaSauGiam() != null ? d.getGiaSauGiam() : d.getDonGia();
            boolean matchGia = true;

            if (request.getGiaMin() != null && gia.compareTo(request.getGiaMin()) < 0) matchGia = false;
            if (request.getGiaMax() != null && gia.compareTo(request.getGiaMax()) > 0) matchGia = false;

            return matchMauSac && matchKichThuoc && matchChatLieu && matchGia;
        }).toList();

        // Gom nhóm chi tiết theo sản phẩm
        Map<String, List<ChiTietMauSacKichThuocDTOFill>> detailsMap = details.stream()
                .collect(Collectors.groupingBy(ChiTietMauSacKichThuocDTOFill::getIdSanPham));

        // Map dữ liệu ra response
        return summaries.stream()
                .filter(s -> detailsMap.containsKey(s.getId())) // Chỉ lấy sản phẩm có biến thể hợp lệ
                .map(summary -> {
                    SanPhamResponse response = new SanPhamResponse();
                    BeanUtils.copyProperties(summary, response);

                    List<ChiTietMauSacKichThuocDTOFill> currentDetails = detailsMap.get(summary.getId());

                    // Danh sách màu sắc
                    List<MauSacResponse> mauSacList = currentDetails.stream()
                            .map(d -> new MauSacResponse(d.getIdMauSac(), d.getTenMauSac(),d.getMaMauSac()))
                            .distinct()
                            .collect(Collectors.toList());

                    // Danh sách kích thước
                    List<KichThuocResponse> kichThuocList = currentDetails.stream()
                            .map(d -> new KichThuocResponse(d.getIdKichThuoc(), d.getTenKichThuoc()))
                            .distinct()
                            .collect(Collectors.toList());

                    // Danh sách chất liệu
                    List<ChatLieuResponse> chatLieuList = currentDetails.stream()
                            .map(d -> new ChatLieuResponse(d.getIdChatLieu(), d.getTenChatLieu()))
                            .distinct()
                            .collect(Collectors.toList());

                    List<String> imageList = currentDetails.stream()
                            .map(ChiTietMauSacKichThuocDTOFill::getTenAnh)
                            .filter(Objects::nonNull)
                            .distinct()
                            .map(tenAnh -> "http://localhost:8080" + "/uploads/san_pham/" + tenAnh)
                            .collect(Collectors.toList());

                    response.setMauSacList(mauSacList);
                    response.setKichThuocList(kichThuocList);
                    response.setChatLieuList(chatLieuList);
                    response.setImageList(imageList);

                    return response;
                }).collect(Collectors.toList());
    }


    @Override
    public List<SanPhamResponse> listSanPhamBanChay() {
        List<SanPhamSummaryDTO> summaries = sanPhamRepository.getBestSellingProducts();
        if (summaries.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> sanPhamIds = summaries.stream()
                .map(SanPhamSummaryDTO::getId)
                .collect(Collectors.toList());
        List<ChiTietMauSacKichThuocDTO> details = chiTietSanPhamRepository.findAllDetailsBySanPhamIds(sanPhamIds);
        Map<String, List<ChiTietMauSacKichThuocDTO>> detailsMap = details.stream()
                .collect(Collectors.groupingBy(ChiTietMauSacKichThuocDTO::getIdSanPham));
        return summaries.stream().map(summary -> {

            SanPhamResponse response = new SanPhamResponse();
            BeanUtils.copyProperties(summary, response);

            List<ChiTietMauSacKichThuocDTO> currentDetails = detailsMap.getOrDefault(summary.getId(), Collections.emptyList());
            List<MauSacResponse> mauSacList = currentDetails.stream()
                    .map(d -> new MauSacResponse(d.getIdMauSac(),d.getTenMauSac(),d.getMaMauSac()))
                    .distinct()
                    .collect(Collectors.toList());
            List<KichThuocResponse> kichThuocList = currentDetails.stream()
                    .map(d -> new KichThuocResponse(d.getIdKichThuoc(), d.getTenKichThuoc()))
                    .distinct()
                    .collect(Collectors.toList());
            List<String> imageList = currentDetails.stream()
                    .map(ChiTietMauSacKichThuocDTO::getTenAnh)
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(tenAnh -> "http://localhost:8080" + "/uploads/san_pham/" + tenAnh)
                    .collect(Collectors.toList());
            response.setImageList(imageList);
            response.setMauSacList(mauSacList);
            response.setKichThuocList(kichThuocList);

            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public SanPhamDetailResponse getSanPhamDetail(String idSanPham) {
        SanPhamDetailSummaryProjection summary = sanPhamRepository.findSanPhamChiTiet(idSanPham)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + idSanPham)); // Hoặc một Exception tùy chỉnh
        List<SanPhamVariantDetailDTO> details = chiTietSanPhamRepository.findAllVariantDetailsBySanPhamId(idSanPham);
        SanPhamDetailResponse response = new SanPhamDetailResponse();
        BeanUtils.copyProperties(summary, response);

                // code lỏ chữa cháy
                String combinedString = summary.getMoTa();
                String maSanPham = "";
                String moTaThucTe = "";
                if (combinedString != null && combinedString.contains(", ")) {
                    String[] parts = combinedString.split(", ", 2);
                    maSanPham = parts[0];
                    if (parts.length > 1) {
                        moTaThucTe = parts[1];
                    }
                } else {
                    moTaThucTe = combinedString;
                }
                response.setMaSanPham(maSanPham);
                response.setMoTa(moTaThucTe);
                // kết thúc đoạn code  lỏ


        List<MauSacResponse> mauSacList = details.stream()
                .map(d -> new MauSacResponse(d.getIdMauSac(), d.getTenMauSac(),d.getMaMauSac()))
                .distinct() // Loại bỏ các màu trùng lặp
                .collect(Collectors.toList());
        List<KichThuocResponse> kichThuocList = details.stream()
                .map(d -> new KichThuocResponse(d.getIdKichThuoc(), d.getSize()))
                .distinct() // Loại bỏ các size trùng lặp
                .collect(Collectors.toList());
        List<String> imageList = details.stream()
                .map(SanPhamVariantDetailDTO::getImageUrl)
                .filter(Objects::nonNull)
                .distinct()
                .map(tenAnh -> "http://localhost:8080" + "/uploads/san_pham/" + tenAnh)
                .collect(Collectors.toList());
        // Gán các danh sách đã xử lý vào đối tượng response
        response.setMauSacList(mauSacList);
        response.setKichThuocList(kichThuocList);
        response.setImageList(imageList);

        return response;
    }
}