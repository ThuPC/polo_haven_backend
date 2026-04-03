package com.datn.backend.service.Hoa_don.hoadon_impl;

import com.datn.backend.entity.HTTT;
import com.datn.backend.repository.hoadon_repo.HtttRepo;
import com.datn.backend.service.Hoa_don.HtttService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HtttServiceImpl implements HtttService {
    @Autowired
    HtttRepo htttRepo;

    @Override
    public List<HTTT> getListHttt(){
        return htttRepo.findAll();
    }

}
