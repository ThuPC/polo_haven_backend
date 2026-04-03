package com.datn.backend.repository;

import com.datn.backend.entity.SanPham;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SanPhamTenRepo {
    @Query("SELECT sp FROM SanPham sp")
    List<SanPham> findAllSanPhamBasic();

}
