package com.datn.backend.service.gio_hang_chi_tiet;


import com.datn.backend.dto.response.GioHangChiTietResponseDTO;
import com.datn.backend.entity.ChiTietSanPham;
import com.datn.backend.entity.GioHangChiTiet;
import com.datn.backend.entity.SanPham;
import com.datn.backend.repository.ChiTietKhuyenMaiRepository;
import com.datn.backend.repository.ChiTietSanPhamRepository;
import com.datn.backend.repository.GioHangChiTietRepository;
import com.datn.backend.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GioHangChiTietService {

    @Autowired
    private GioHangChiTietRepository gioHangChiTietRepository;

    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private ChiTietKhuyenMaiRepository chiTietKhuyenMaiRepository;
    @Value("${app.image.base-url}")
    private String imageBaseUrl;

    // Mapping entity sang DTO
    public GioHangChiTietResponseDTO toDto(GioHangChiTiet entity) {
        ChiTietSanPham chiTietSanPham = entity.getChiTietSanPham();
        SanPham sanPham = chiTietSanPham.getSanPham();
        
        // Lấy giá gốc từ ChiTietSanPham
        BigDecimal giaGoc = chiTietSanPham.getDonGia();
        
        // Lấy phần trăm giảm giá từ khuyến mãi đang hoạt động
        Integer phanTramGiam = 0;
        try {
            var khuyenMai = chiTietKhuyenMaiRepository.findByChiTietSanPhamAndTrangThai(chiTietSanPham, (byte) 1);
            if (khuyenMai != null && khuyenMai.getKhuyenMai() != null) {
                phanTramGiam = khuyenMai.getKhuyenMai().getPhanTramGiam() != null ? 
                    khuyenMai.getKhuyenMai().getPhanTramGiam().intValue() : 0;
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Tính giá sau giảm giá
        BigDecimal giaSauGiam = giaGoc;
        if (phanTramGiam > 0) {
            // Tính giá sau giảm = giá gốc * (100 - phần trăm giảm) / 100
            giaSauGiam = giaGoc.multiply(BigDecimal.valueOf(100 - phanTramGiam))
                              .divide(BigDecimal.valueOf(100), 0, BigDecimal.ROUND_HALF_UP);
        }
        


        return new GioHangChiTietResponseDTO(
                entity.getId(),
                chiTietSanPham.getId(),
                sanPham.getTenSanPham(),
                chiTietSanPham.getHinhAnh() != null && chiTietSanPham.getHinhAnh().getTenAnh() != null
                        ? imageBaseUrl + chiTietSanPham.getHinhAnh().getTenAnh()
                        : null,
                giaSauGiam,
                giaGoc,
                phanTramGiam,
                chiTietSanPham.getMauSac().getTenMau(),
                chiTietSanPham.getKichThuoc().getSize(),
                entity.getSoLuong(),
                chiTietSanPham.getSoLuong()
        );
    }

    // Lấy danh sách giỏ hàng của khách hàng (trả về DTO)
    public List<GioHangChiTietResponseDTO> getCartByUserId(String khachHangId) {
        List<GioHangChiTiet> entities = gioHangChiTietRepository.findByKhachHang_Id(khachHangId);
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Thêm sản phẩm vào giỏ hàng (trả về DTO)
    public GioHangChiTietResponseDTO addToCart(String khachHangId, String chiTietSanPhamId, int soLuong) {
        GioHangChiTiet item = gioHangChiTietRepository
                .findByKhachHang_IdAndChiTietSanPham_Id(khachHangId, chiTietSanPhamId)
                .orElse(null);

        if (item == null) {
            item = new GioHangChiTiet();
            item.setKhachHang(khachHangRepository.findById(khachHangId).orElseThrow());
            item.setChiTietSanPham(chiTietSanPhamRepository.findById(chiTietSanPhamId).orElseThrow());
            item.setSoLuong(soLuong);
            item.setTrangThai((byte) 1);
        } else {
            item.setSoLuong(item.getSoLuong() + soLuong);
        }
        GioHangChiTiet saved = gioHangChiTietRepository.save(item);
        return toDto(saved);
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng (trả về DTO)
    public GioHangChiTietResponseDTO updateCartItem(String cartItemId, int soLuong) {
        GioHangChiTiet item = gioHangChiTietRepository.findById(cartItemId).orElseThrow();
        item.setSoLuong(soLuong);
        GioHangChiTiet saved = gioHangChiTietRepository.save(item);
        return toDto(saved);
    }

    // Xóa sản phẩm khỏi giỏ hàng (không trả về gì)
    public void deleteCartItem(String cartItemId) {
        try{
            gioHangChiTietRepository.deleteById(cartItemId);
            System.out.println(" đã xoa xong id: "+cartItemId);
        }catch (Exception e){
            System.out.println("ko xóa được");
        }

    }

    // Xóa toàn bộ giỏ hàng của khách hàng (không trả về gì)
    public void clearCartByKhachHangId(String khachHangId) {
        List<GioHangChiTiet> items = gioHangChiTietRepository.findByKhachHang_Id(khachHangId);
        gioHangChiTietRepository.deleteAll(items);
    }
}

