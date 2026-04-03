package com.datn.backend.repository;

import com.datn.backend.entity.SanPhamVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanPhamVariantRepo extends JpaRepository<SanPhamVariant, String> {
    List<SanPhamVariant> findBySanPhamId(String sanPhamId);

}
