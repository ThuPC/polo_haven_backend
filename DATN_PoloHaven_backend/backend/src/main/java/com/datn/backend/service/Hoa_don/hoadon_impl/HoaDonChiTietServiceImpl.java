package com.datn.backend.service.Hoa_don.hoadon_impl;



import com.datn.backend.entity.HoaDonChiTiet;
import com.datn.backend.repository.hoadon_repo.HoaDonChiTietRepo;
import com.datn.backend.service.Hoa_don.HoaDonChiTietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HoaDonChiTietServiceImpl implements HoaDonChiTietService {

    @Autowired
    HoaDonChiTietRepo hoaDonChiTietRepo;

    @Override
    public List<HoaDonChiTiet> getListHoaDonChiTiet() {
        return hoaDonChiTietRepo.findAll();
    }
}
