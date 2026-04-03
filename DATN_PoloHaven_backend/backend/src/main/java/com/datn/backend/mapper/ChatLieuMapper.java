package com.datn.backend.mapper;

import com.datn.backend.dto.request.ChatLieuRequestDTO;
import com.datn.backend.dto.response.ChatLieuResponseDTO;
import com.datn.backend.entity.ChatLieu;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ChatLieuMapper {

    ChatLieu toEntity(ChatLieuRequestDTO dto);

    ChatLieuResponseDTO toResponseDTO(ChatLieu entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(ChatLieuRequestDTO dto, @MappingTarget ChatLieu entity);
}
