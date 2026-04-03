package com.datn.backend.mapper;

import com.datn.backend.dto.response.ThongKeResponseDTO;
import com.datn.backend.entity.ThongKe;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ThongKeMapper {
    ThongKeResponseDTO toResponseDTO(ThongKe thongKe);
}

