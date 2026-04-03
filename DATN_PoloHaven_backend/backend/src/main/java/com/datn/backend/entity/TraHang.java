package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "tra_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TraHang {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne
    @JoinColumn(name = "hoa_don_id")
    HoaDon hoaDon;

    @Column(name = "khach_hang_id")
    String khachHangId;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    String lyDo;

    // store paths or urls separated by comma OR in a separate table; simple approach: JSON string
    @Column(name = "media_urls", columnDefinition = "NVARCHAR(MAX)")
    String mediaUrlsJson;

    // 0 = mới yêu cầu, 1 = nguyên vẹn, 2 = lỗi, 3 = đã hoàn tiền, 4 = từ chối
    Byte status;

    String adminNote;

    LocalDateTime requestedAt;

    LocalDateTime reviewedAt;
}
