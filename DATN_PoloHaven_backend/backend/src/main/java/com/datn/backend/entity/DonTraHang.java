package com.datn.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "don_tra_hang")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DonTraHang {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    
    @ManyToOne
    @JoinColumn(name = "id_hoa_don")
    HoaDon hoaDon;
    
    @ManyToOne
    @JoinColumn(name = "id_khach_hang")
    KhachHang khachHang;
    
    String maDonTraHang;
    
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm", timezone = "Asia/Ho_Chi_Minh")
    LocalDateTime ngayTao;
    @Nationalized
    String lyDo;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    String ghiChu;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    String ghiChuAdmin;
    
    // 0: Chờ xác nhận, 1: Đã xác nhận, 2: Đã từ chối, 3: Hoàn thành
    Byte trangThai;
    
    @OneToMany(mappedBy = "donTraHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ChiTietTraHang> chiTietTraHangList;
    
    @OneToMany(mappedBy = "donTraHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<MediaTraHang> mediaTraHangList;
}