package com.datn.backend.mapper;

import com.datn.backend.dto.request.KichThuocRequestDTO;
import com.datn.backend.dto.response.KichThuocResponseDTO;
import com.datn.backend.entity.KichThuoc;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface KichThuocMapper {

    KichThuoc toEntity(KichThuocRequestDTO dto);

    KichThuocResponseDTO toResponseDTO(KichThuoc entity);

    List<KichThuocResponseDTO> toResponseDTOs(List<KichThuoc> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(KichThuocRequestDTO dto, @MappingTarget KichThuoc entity);
}
