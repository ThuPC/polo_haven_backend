package com.datn.backend.repository.hoadon_repo;

import com.datn.backend.entity.HTTT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HtttRepo extends JpaRepository<HTTT,String> {
    List<HTTT> findByTrangThaiOrderByTenHinhThucAsc(Byte trangThai);
}
