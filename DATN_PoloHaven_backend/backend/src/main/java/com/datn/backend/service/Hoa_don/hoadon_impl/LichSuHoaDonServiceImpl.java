package com.datn.backend.service.Hoa_don.hoadon_impl;


import com.datn.backend.dto.response.LichSuHoaDonResponseDto;
import com.datn.backend.entity.LichSuHoaDon;
import com.datn.backend.repository.hoadon_repo.LichSuHoaDonRepo;
import com.datn.backend.service.Hoa_don.LichSuHoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LichSuHoaDonServiceImpl implements LichSuHoaDonService {

    @Autowired
    LichSuHoaDonRepo lichSuHoaDonRepo;

    @Autowired
    LichSuHoaDonRepo lichSuHoaDonDtoRepo;

    @Override
    public List<LichSuHoaDon> getListLichSuHoaDon() {
        return lichSuHoaDonRepo.findAll();
    }

//    @Override
//    public List<LichSuHoaDonDto> getListLichSuHoaDonDto(int id) {
//        return lichSuHoaDonDtoRepo.getListLichSuHoaDonDto(id);
//    }

    @Override
    public List<LichSuHoaDonResponseDto> getListLichSuHoaDonDto(String id) {
        List<LichSuHoaDonResponseDto> result = lichSuHoaDonDtoRepo.getLichSuHoaDon(id);
        System.out.println("Số lượng lịch sử thanh toán: " + result.size());
        if (result.isEmpty()) {
            System.out.println("Không có dữ liệu lịch sử thanh toán cho hóa đơn ID: " + id);
        }
        return result;
    }

    @Override
    public LichSuHoaDonResponseDto getLichSuHoaDonId(String hoaDonId) {
        return lichSuHoaDonRepo.getLichSuHoaDonId(hoaDonId);
    }
}
