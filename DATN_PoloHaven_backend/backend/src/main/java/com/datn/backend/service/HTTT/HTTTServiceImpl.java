package com.datn.backend.service.HTTT;

import com.datn.backend.entity.HTTT;
import com.datn.backend.repository.hoadon_repo.HtttRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HTTTServiceImpl implements HTTTService {

    @Autowired
    private HtttRepo htttRepo;

    @Override
    public List<HTTT> getAllActivePaymentMethods() {
        return htttRepo.findByTrangThaiOrderByTenHinhThucAsc((byte) 1);
    }

    @Override
    public HTTT getPaymentMethodById(String id) {
        return htttRepo.findById(id).orElse(null);
    }
}