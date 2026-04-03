package com.datn.backend.repository;

import com.datn.backend.entity.ChatLieu;
import com.datn.backend.entity.HinhAnh;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HinhAnhRepo extends JpaRepository<HinhAnh, String> {
    Optional<HinhAnh> findTopByOrderByMaAnhDesc();

    Optional<HinhAnh> findByTenAnh(String tenAnh);

    Optional<HinhAnh> findByHash(String hash);
}

