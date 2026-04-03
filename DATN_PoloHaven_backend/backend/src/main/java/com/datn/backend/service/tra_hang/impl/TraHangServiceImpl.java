package com.datn.backend.service.tra_hang.impl;

import com.datn.backend.dto.request.DonTraHangRequest;
import com.datn.backend.dto.response.DonTraHangResponse;
import com.datn.backend.entity.*;
import com.datn.backend.repository.*;
import com.datn.backend.repository.hoadon_repo.HoaDonChiTietRepo;
import com.datn.backend.repository.hoadon_repo.HoaDonRepo;
import com.datn.backend.service.tra_hang.TraHangService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TraHangServiceImpl implements TraHangService {

    private final DonTraHangRepository donTraHangRepository;
    private final ChiTietTraHangRepository chiTietTraHangRepository;
    private final MediaTraHangRepository mediaTraHangRepository;
    private final HoaDonRepo hoaDonRepository;
    private final KhachHangRepository khachHangRepository;
    private final HoaDonChiTietRepo hoaDonChiTietRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;

    private static final String UPLOAD_DIR = "uploads/tra-hang/";

    @Override
    @Transactional
    public DonTraHangResponse taoYeuCauTraHang(DonTraHangRequest request, String khachHangId, List<MultipartFile> images, List<MultipartFile> videos) {
        // Kiểm tra hóa đơn tồn tại
        HoaDon hoaDon = hoaDonRepository.findById(request.getHoaDonId())
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        // Kiểm tra khách hàng
        KhachHang khachHang = khachHangRepository.findById(khachHangId)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

        // Kiểm tra quyền sở hữu hóa đơn
        if (!hoaDon.getKhachHang().getId().equals(khachHangId)) {
            throw new RuntimeException("Bạn không có quyền trả hàng cho đơn hàng này");
        }

        // Kiểm tra trạng thái hóa đơn (chỉ cho phép trả hàng khi đã giao hàng hoặc hoàn thành)
        if (hoaDon.getTrangThai() != 4 && hoaDon.getTrangThai() != 5) {
            throw new RuntimeException("Đơn hàng không thể trả hàng ở trạng thái hiện tại");
        }

        // Tạo đơn trả hàng
        DonTraHang donTraHang = DonTraHang.builder()
                .hoaDon(hoaDon)
                .khachHang(khachHang)
                .maDonTraHang("TH" + System.currentTimeMillis())
                .ngayTao(LocalDateTime.now())
                .lyDo(request.getLyDo())
                .ghiChu(request.getGhiChu())
                .trangThai((byte) 0) // 0: Chờ xác nhận
                .build();

        DonTraHang savedDonTraHang = donTraHangRepository.save(donTraHang);

        // Tạo chi tiết trả hàng
        List<ChiTietTraHang> chiTietTraHangList = new ArrayList<>();
        for (DonTraHangRequest.ChiTietTraHangRequest chiTietRequest : request.getChiTietTraHangList()) {
            HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.findById(chiTietRequest.getHoaDonChiTietId())
                    .orElseThrow(() -> new RuntimeException("Chi tiết hóa đơn không tồn tại"));

            // Lấy chiTietSanPham từ hoaDonChiTiet thay vì từ request
            ChiTietSanPham chiTietSanPham = hoaDonChiTiet.getChiTietSanPham();
            if (chiTietSanPham == null) {
                throw new RuntimeException("Chi tiết sản phẩm không tồn tại");
            }

            // Kiểm tra số lượng trả không vượt quá số lượng đã mua
            if (chiTietRequest.getSoLuong() > hoaDonChiTiet.getSoLuong()) {
                throw new RuntimeException("Số lượng trả không được vượt quá số lượng đã mua");
            }

            ChiTietTraHang chiTietTraHang = ChiTietTraHang.builder()
                    .donTraHang(savedDonTraHang)
                    .chiTietSanPham(chiTietSanPham)
                    .hoaDonChiTiet(hoaDonChiTiet)
                    .soLuong(chiTietRequest.getSoLuong())
                    .giaTien(hoaDonChiTiet.getGiaBan())
                    .lyDo(chiTietRequest.getLyDo())
                    .build();

            chiTietTraHangList.add(chiTietTraHang);
        }
        chiTietTraHangRepository.saveAll(chiTietTraHangList);

        // Xử lý upload media
        List<MediaTraHang> mediaList = new ArrayList<>();
        
        // Xử lý hình ảnh
        if (images != null && !images.isEmpty()) {
            mediaList.addAll(saveMediaFiles(images, savedDonTraHang, (byte) 0));
        }
        
        // Xử lý video
        if (videos != null && !videos.isEmpty()) {
            mediaList.addAll(saveMediaFiles(videos, savedDonTraHang, (byte) 1));
        }
        
        // Cập nhật trạng thái hóa đơn
        hoaDon.setTrangThai((byte) 7); // 7: Trả hàng
        hoaDonRepository.save(hoaDon);

        return mapToDonTraHangResponse(savedDonTraHang);
    }

    @Override
    public List<DonTraHangResponse> getDanhSachDonTraHangByKhachHang(String khachHangId) {
        List<DonTraHang> donTraHangList = donTraHangRepository.findByKhachHangId(khachHangId);
        return donTraHangList.stream()
                .map(this::mapToDonTraHangResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DonTraHangResponse> getDanhSachDonTraHangByTrangThai(Byte trangThai) {
        List<DonTraHang> donTraHangList = donTraHangRepository.findByTrangThai(trangThai);
        return donTraHangList.stream()
                .map(this::mapToDonTraHangResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DonTraHangResponse> getAllDonTraHang() {
        List<DonTraHang> donTraHangList = donTraHangRepository.findAll();
        return donTraHangList.stream()
                .map(this::mapToDonTraHangResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DonTraHangResponse getDonTraHangById(String id) {
        DonTraHang donTraHang = donTraHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn trả hàng không tồn tại"));
        return mapToDonTraHangResponse(donTraHang);
    }

    @Override
    @Transactional
    public DonTraHangResponse chapNhanDonTraHang(String id, String ghiChuAdmin, Map<String, Byte> tinhTrangHangMap) {
        DonTraHang donTraHang = donTraHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn trả hàng không tồn tại"));
        
        // Chỉ chấp nhận đơn đang ở trạng thái chờ xác nhận
        if (donTraHang.getTrangThai() != 0) {
            throw new RuntimeException("Đơn trả hàng không thể chấp nhận ở trạng thái hiện tại");
        }
        
        // Cập nhật tình trạng hàng cho từng chi tiết trả hàng
        if (tinhTrangHangMap != null && !tinhTrangHangMap.isEmpty()) {
            for (ChiTietTraHang chiTiet : donTraHang.getChiTietTraHangList()) {
                String chiTietId = chiTiet.getId();
                if (tinhTrangHangMap.containsKey(chiTietId)) {
                    chiTiet.setTinhTrangHang(tinhTrangHangMap.get(chiTietId));
                    chiTietTraHangRepository.save(chiTiet);
                }
            }
        }
        
        donTraHang.setTrangThai((byte) 1); // 1: Đã xác nhận
        donTraHang.setGhiChuAdmin(ghiChuAdmin);
        
        // Cập nhật trạng thái hóa đơn
        HoaDon hoaDon = donTraHang.getHoaDon();
        hoaDon.setTrangThai((byte) 8); // 8: Đang trả hàng
        hoaDonRepository.save(hoaDon);
        
        return mapToDonTraHangResponse(donTraHangRepository.save(donTraHang));
    }

    @Override
    @Transactional
    public DonTraHangResponse hoanThanhDonTraHang(String id, String ghiChuAdmin) {
        DonTraHang donTraHang = donTraHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn trả hàng không tồn tại"));
        
        // Chỉ hoàn thành đơn đã được xác nhận
        if (donTraHang.getTrangThai() != 1) {
            throw new RuntimeException("Đơn trả hàng phải được xác nhận trước khi hoàn thành");
        }
        
        System.out.println("=== BẮT ĐẦU HOÀN THÀNH ĐỚN TRẢ HÀNG ===");
        System.out.println("Mã đơn trả hàng: " + donTraHang.getMaDonTraHang());
        
        // Cộng lại số lượng sản phẩm dựa trên tình trạng hàng
        int tongSanPhamDuocCong = 0;
        for (ChiTietTraHang chiTiet : donTraHang.getChiTietTraHangList()) {
            ChiTietSanPham chiTietSanPham = chiTiet.getChiTietSanPham();
            
            if (chiTietSanPham == null) {
                System.out.println("CẢNH BÁO: Chi tiết sản phẩm null cho chi tiết trả hàng ID: " + chiTiet.getId());
                continue;
            }
            
            System.out.println("--- Xử lý chi tiết trả hàng ---");
            System.out.println("Sản phẩm: " + (chiTietSanPham.getSanPham() != null ? chiTietSanPham.getSanPham().getTenSanPham() : "N/A"));
            System.out.println("Màu sắc: " + (chiTietSanPham.getMauSac() != null ? chiTietSanPham.getMauSac().getTenMau() : "N/A"));
            System.out.println("Kích thước: " + (chiTietSanPham.getKichThuoc() != null ? chiTietSanPham.getKichThuoc().getSize() : "N/A"));
            System.out.println("Tình trạng hàng: " + chiTiet.getTinhTrangHang());
            System.out.println("Có cộng lại số lượng: " + chiTiet.isCongLaiSoLuong());
            
            if (chiTiet.isCongLaiSoLuong()) {
                int soLuongHienTai = chiTietSanPham.getSoLuong();
                int soLuongTra = chiTiet.getSoLuong();
                int soLuongMoi = soLuongHienTai + soLuongTra;
                
                // Validation: Đảm bảo số lượng không âm
                if (soLuongMoi < 0) {
                    throw new RuntimeException("Lỗi: Số lượng sản phẩm sau khi cộng lại bị âm. " +
                            "Sản phẩm: " + (chiTietSanPham.getSanPham() != null ? chiTietSanPham.getSanPham().getTenSanPham() : "N/A") +
                            ", Số lượng hiện tại: " + soLuongHienTai + 
                            ", Số lượng trả: " + soLuongTra);
                }
                
                System.out.println("Số lượng trước khi cộng: " + soLuongHienTai);
                System.out.println("Số lượng trả: " + soLuongTra);
                System.out.println("Số lượng sau khi cộng: " + soLuongMoi);
                
                chiTietSanPham.setSoLuong(soLuongMoi);
                chiTietSanPhamRepository.save(chiTietSanPham);
                tongSanPhamDuocCong++;
                
                System.out.println("✓ Đã cộng lại số lượng thành công");
            } else {
                System.out.println("✗ Không cộng lại số lượng (hàng bị lỗi hoặc chưa xác định tình trạng)");
            }
        }
        
        System.out.println("=== KẾT QUẢ CỘNG LẠI SỐ LƯỢNG ===");
        System.out.println("Tổng số sản phẩm được cộng lại: " + tongSanPhamDuocCong + "/" + donTraHang.getChiTietTraHangList().size());
        
        donTraHang.setTrangThai((byte) 3); // 3: Hoàn thành
        donTraHang.setGhiChuAdmin(ghiChuAdmin);
        
        // Cập nhật trạng thái hóa đơn
        HoaDon hoaDon = donTraHang.getHoaDon();
        hoaDon.setTrangThai((byte) 9); // 9: Đã trả hàng hoàn thành
        hoaDonRepository.save(hoaDon);
        
        System.out.println("=== HOÀN THÀNH ĐỚN TRẢ HÀNG THÀNH CÔNG ===");
        
        return mapToDonTraHangResponse(donTraHangRepository.save(donTraHang));
    }

    @Override
    @Transactional
    public DonTraHangResponse tuChoiDonTraHang(String id, String ghiChuAdmin) {
        DonTraHang donTraHang = donTraHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn trả hàng không tồn tại"));
        
        // Chỉ từ chối đơn đang ở trạng thái chờ xác nhận
        if (donTraHang.getTrangThai() != 0) {
            throw new RuntimeException("Đơn trả hàng không thể từ chối ở trạng thái hiện tại");
        }
        
        donTraHang.setTrangThai((byte) 2); // 2: Đã từ chối
        donTraHang.setGhiChuAdmin(ghiChuAdmin);
        
        // Cập nhật trạng thái hóa đơn về trạng thái trước đó
        HoaDon hoaDon = donTraHang.getHoaDon();
        hoaDon.setTrangThai((byte) 5); // 5: Hoàn thành // 10: Trả hàng bị từ chối
        hoaDonRepository.save(hoaDon);
        
        return mapToDonTraHangResponse(donTraHangRepository.save(donTraHang));
    }

    private List<MediaTraHang> saveMediaFiles(List<MultipartFile> files, DonTraHang donTraHang, byte loaiMedia) {
        List<MediaTraHang> mediaList = new ArrayList<>();
        
        try {
            // Tạo thư mục nếu chưa tồn tại
            Path uploadPath = Paths.get(UPLOAD_DIR + donTraHang.getId());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                
                // Tạo tên file ngẫu nhiên để tránh trùng lặp
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                
                // Lưu file
                Files.copy(file.getInputStream(), filePath);
                
                // Lưu thông tin vào database
                MediaTraHang media = MediaTraHang.builder()
                        .donTraHang(donTraHang)
                        .duongDan(UPLOAD_DIR + donTraHang.getId() + "/" + fileName)
                        .loaiMedia(loaiMedia)
                        .build();
                
                mediaList.add(mediaTraHangRepository.save(media));
            }
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file: " + e.getMessage());
        }
        
        return mediaList;
    }

    private DonTraHangResponse mapToDonTraHangResponse(DonTraHang donTraHang) {
        // Lấy danh sách chi tiết trả hàng
        List<ChiTietTraHang> chiTietTraHangList = chiTietTraHangRepository.findByDonTraHangId(donTraHang.getId());
        
        // Lấy danh sách media
        List<MediaTraHang> mediaTraHangList = mediaTraHangRepository.findByDonTraHangId(donTraHang.getId());



        // Map chi tiết trả hàng sang response
        List<DonTraHangResponse.ChiTietTraHangResponse> chiTietResponses = chiTietTraHangList.stream()
                .map(chiTiet -> {
                    ChiTietSanPham ctsp = chiTiet.getChiTietSanPham();
                    
                    // Debug logs để kiểm tra dữ liệu
                    System.out.println("=== Debug ChiTietTraHang ===");
                    System.out.println("ChiTietSanPham ID: " + (ctsp != null ? ctsp.getId() : "NULL"));
                    System.out.println("SanPham: " + (ctsp != null && ctsp.getSanPham() != null ? ctsp.getSanPham().getTenSanPham() : "NULL"));
                    System.out.println("MauSac: " + (ctsp != null && ctsp.getMauSac() != null ? ctsp.getMauSac().getTenMau() : "NULL"));
                    System.out.println("KichThuoc: " + (ctsp != null && ctsp.getKichThuoc() != null ? ctsp.getKichThuoc().getSize() : "NULL"));
                    System.out.println("HinhAnh: " + (ctsp != null && ctsp.getHinhAnh() != null ? ctsp.getHinhAnh().getTenAnh() : "NULL"));
                    
                    String tenSanPham = (ctsp != null && ctsp.getSanPham() != null) ? ctsp.getSanPham().getTenSanPham() : "Không có tên";
                    String mauSac = (ctsp != null && ctsp.getMauSac() != null) ? ctsp.getMauSac().getTenMau() : "Không có màu";
                    String kichThuoc = (ctsp != null && ctsp.getKichThuoc() != null) ? ctsp.getKichThuoc().getSize() : "Không có size";

                    String anhSanPham = null;
                    if (ctsp != null && ctsp.getHinhAnh() != null && ctsp.getHinhAnh().getTenAnh() != null
                            && !ctsp.getHinhAnh().getTenAnh().isEmpty()) {
                        anhSanPham = ctsp.getHinhAnh().getTenAnh();
                    }

                    return DonTraHangResponse.ChiTietTraHangResponse.builder()
                            .id(chiTiet.getId())
                            .tenSanPham(tenSanPham)
                            .mauSac(mauSac)
                            .kichThuoc(kichThuoc)
                            .soLuong(chiTiet.getSoLuong())
                            .lyDo(chiTiet.getLyDo())
                            .anhSanPham(anhSanPham)
                            .donGia(chiTiet.getGiaTien())
                            .build();
                })
                .collect(Collectors.toList());


        // Map media sang response
        List<DonTraHangResponse.MediaTraHangResponse> mediaResponses = mediaTraHangList.stream()
                .map(media -> DonTraHangResponse.MediaTraHangResponse.builder()
                        .id(media.getId())
                        .duongDan(media.getDuongDan())
                        .loaiMedia(media.getLoaiMedia())
                        .build())
                .collect(Collectors.toList());
        
        // Tạo response
        return DonTraHangResponse.builder()
                .id(donTraHang.getId())
                .maDonTraHang(donTraHang.getMaDonTraHang())
                .maHoaDon(donTraHang.getHoaDon().getMaHoaDon())
                .tenKhachHang(donTraHang.getKhachHang().getTenKhachHang())
                .email(donTraHang.getKhachHang().getEmail())
                .sdt(donTraHang.getKhachHang().getSdt())
                .ngayTao(donTraHang.getNgayTao())
                .lyDo(donTraHang.getLyDo())
                .ghiChu(donTraHang.getGhiChu())
                .ghiChuAdmin(donTraHang.getGhiChuAdmin())
                .trangThai(donTraHang.getTrangThai())
                .chiTietTraHangList(chiTietResponses)
                .mediaTraHangList(mediaResponses)
                .build();
    }
}