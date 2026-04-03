package com.datn.backend.entity;

import com.datn.backend.contants.ThoiGian_For_CapDo;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "quy_doi_cap_do")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CapDo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String maCapDo;
    @Nationalized
    String tenCapDo;
    BigDecimal soTienMua;
    LocalDate ThoiGian;
}
