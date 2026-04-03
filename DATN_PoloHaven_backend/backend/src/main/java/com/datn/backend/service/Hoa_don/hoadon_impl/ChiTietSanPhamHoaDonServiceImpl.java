package com.datn.backend.service.Hoa_don.hoadon_impl;



import com.datn.backend.dto.response.ChiTietSanPhamHoaDonResponseDto;
import com.datn.backend.entity.ChiTietSanPham;
import com.datn.backend.repository.hoadon_repo.ChiTietSanPhamHoaDonRepo;
import com.datn.backend.service.Hoa_don.ChiTietSanPhamHoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChiTietSanPhamHoaDonServiceImpl implements ChiTietSanPhamHoaDonService {
    @Autowired
    ChiTietSanPhamHoaDonRepo chiTietSanPhamRepo;

    @Autowired
    ChiTietSanPhamHoaDonRepo chiTietSanPhamDtoRepo;

    @Override
    public List<ChiTietSanPham> getListChiTietSanPham() {
        return chiTietSanPhamRepo.findAll();
    }

    @Override
    public List<ChiTietSanPhamHoaDonResponseDto> getDanhSachSanPham(String id){
        List<ChiTietSanPhamHoaDonResponseDto> list = chiTietSanPhamDtoRepo.getDanhSachSanPham(id);
        for (ChiTietSanPhamHoaDonResponseDto ct : list) {
            ct.setTenAnh("http://localhost:8080/uploads/san_pham/"+ ct.getTenAnh());
        }
        return list;
    }
}
