package com.datn.backend.dto.response.BanHangOnline;

import com.datn.backend.entity.KichThuoc;
import com.datn.backend.entity.MauSac;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


@Data
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class KichThuocResponse {
    private String id;
    private String size;
}
