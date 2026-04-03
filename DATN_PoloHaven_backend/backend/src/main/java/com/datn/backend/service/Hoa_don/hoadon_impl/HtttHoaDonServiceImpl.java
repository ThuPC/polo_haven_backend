package com.datn.backend.service.Hoa_don.hoadon_impl;


import com.datn.backend.dto.response.LichSuThanhToanDto;
import com.datn.backend.entity.HTTT;
import com.datn.backend.entity.HTTT_Hoa_Don;
import com.datn.backend.repository.hoadon_repo.HtttHoaDonRepo;
import com.datn.backend.service.Hoa_don.HtttHoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HtttHoaDonServiceImpl implements HtttHoaDonService {
    @Autowired
    HtttHoaDonRepo htttHoaDonRepo;

    @Override
    public List<HTTT_Hoa_Don> getListHtttHoaDon() {
        return htttHoaDonRepo.findAll();
    }

    @Override
    public List<LichSuThanhToanDto> getLichSuThanhToan(String id) {
        return htttHoaDonRepo.getLichSuThanhToan(id);
    }
}
