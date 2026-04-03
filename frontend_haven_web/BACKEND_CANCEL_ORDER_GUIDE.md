# Hướng dẫn implement API hủy đơn hàng - Backend

## 🎯 Mục tiêu
Thêm chức năng cho phép khách hàng yêu cầu hủy đơn hàng thông qua API.

## 📋 API Endpoint cần implement

### POST `/khach-hang/hoa-don/huy/{id}`
- **Method**: POST
- **Path**: `/khach-hang/hoa-don/huy/{id}`
- **Description**: Gửi yêu cầu hủy đơn hàng
- **Authentication**: Required (JWT Token)

### Request Body
```json
{
  "lyDo": "Lý do hủy đơn hàng (bắt buộc, 10-500 ký tự)"
}
```

### Response Success (200)
```json
{
  "success": true,
  "message": "Yêu cầu hủy đơn hàng đã được gửi thành công",
  "data": {
    "hoaDonId": "uuid",
    "trangThai": 4,
    "lyDo": "Lý do hủy",
    "ngayHuy": "2024-01-15T10:30:00"
  }
}
```

### Response Errors
- **400**: Dữ liệu không hợp lệ
- **401**: Chưa đăng nhập
- **403**: Không có quyền hủy đơn hàng này
- **404**: Không tìm thấy đơn hàng
- **409**: Đơn hàng không thể hủy ở trạng thái hiện tại
- **500**: Lỗi server

## 🔧 Implementation Guide

### 1. Controller
```java
@RestController
@RequestMapping("/khach-hang/hoa-don")
public class KhachHangHoaDonController {
    
    @PostMapping("/huy/{id}")
    public ResponseEntity<?> yeuCauHuyHoaDon(
            @PathVariable("id") String hoaDonId,
            @RequestBody HuyHoaDonRequest request,
            Authentication authentication) {
        
        try {
            String khachHangId = SecurityUtil.getCurrentUserId();
            
            // Validate request
            if (request.getLyDo() == null || request.getLyDo().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Lý do hủy không được để trống"));
            }
            
            if (request.getLyDo().trim().length() < 10) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Lý do hủy phải có ít nhất 10 ký tự"));
            }
            
            if (request.getLyDo().trim().length() > 500) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Lý do hủy không được quá 500 ký tự"));
            }
            
            // Gọi service
            HuyHoaDonResponse response = hoaDonService.yeuCauHuyHoaDon(
                hoaDonId, 
                khachHangId, 
                request.getLyDo().trim()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (HoaDonNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (HoaDonKhongTheHuyException e) {
            return ResponseEntity.status(409)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }
}
```

### 2. Request/Response DTOs
```java
// HuyHoaDonRequest.java
public class HuyHoaDonRequest {
    private String lyDo;
    
    // Constructors, getters, setters
}

// HuyHoaDonResponse.java
public class HuyHoaDonResponse {
    private boolean success;
    private String message;
    private HuyHoaDonData data;
    
    // Constructors, getters, setters
}

// HuyHoaDonData.java
public class HuyHoaDonData {
    private String hoaDonId;
    private Integer trangThai;
    private String lyDo;
    private LocalDateTime ngayHuy;
    
    // Constructors, getters, setters
}
```

### 3. Service
```java
@Service
public class HoaDonService {
    
    public HuyHoaDonResponse yeuCauHuyHoaDon(String hoaDonId, String khachHangId, String lyDo) {
        // 1. Tìm hóa đơn và kiểm tra quyền sở hữu
        HoaDon hoaDon = hoaDonRepo.findByIdAndKhachHang_Id(hoaDonId, khachHangId)
            .orElseThrow(() -> new HoaDonNotFoundException("Không tìm thấy hóa đơn"));
        
        // 2. Kiểm tra trạng thái có thể hủy không
        if (!coTheHuyHoaDon(hoaDon.getTrangThai())) {
            throw new HoaDonKhongTheHuyException(
                "Đơn hàng không thể hủy ở trạng thái: " + getStatusText(hoaDon.getTrangThai())
            );
        }
        
        // 3. Cập nhật trạng thái thành "Đã hủy"
        hoaDon.setTrangThai(4); // Đã hủy
        hoaDon.setNgayCapNhat(LocalDateTime.now());
        
        // 4. Lưu lý do hủy (có thể tạo bảng riêng hoặc thêm field)
        // Ví dụ: tạo bảng HoaDonHuyLichSu
        HoaDonHuyLichSu huyLichSu = new HoaDonHuyLichSu();
        huyLichSu.setHoaDon(hoaDon);
        huyLichSu.setLyDo(lyDo);
        huyLichSu.setNgayHuy(LocalDateTime.now());
        huyLichSu.setKhachHangId(khachHangId);
        hoaDonHuyLichSuRepo.save(huyLichSu);
        
        // 5. Lưu hóa đơn
        hoaDonRepo.save(hoaDon);
        
        // 6. Trả về response
        HuyHoaDonData data = new HuyHoaDonData();
        data.setHoaDonId(hoaDon.getId());
        data.setTrangThai(hoaDon.getTrangThai());
        data.setLyDo(lyDo);
        data.setNgayHuy(LocalDateTime.now());
        
        HuyHoaDonResponse response = new HuyHoaDonResponse();
        response.setSuccess(true);
        response.setMessage("Yêu cầu hủy đơn hàng đã được gửi thành công");
        response.setData(data);
        
        return response;
    }
    
    private boolean coTheHuyHoaDon(Integer trangThai) {
        // Chỉ cho phép hủy đơn hàng ở trạng thái: Chờ xác nhận (0), Đã xác nhận (1)
        return trangThai == 0 || trangThai == 1;
    }
    
    private String getStatusText(Integer trangThai) {
        Map<Integer, String> statusMap = Map.of(
            0, "Chờ xác nhận",
            1, "Đã xác nhận", 
            2, "Đang giao hàng",
            3, "Đã giao hàng",
            4, "Đã hủy",
            5, "Hoàn trả"
        );
        return statusMap.getOrDefault(trangThai, "Không xác định");
    }
}
```

### 4. Repository (nếu cần)
```java
@Repository
public interface HoaDonHuyLichSuRepository extends JpaRepository<HoaDonHuyLichSu, String> {
    List<HoaDonHuyLichSu> findByHoaDonIdOrderByNgayHuyDesc(String hoaDonId);
}
```

### 5. Entity (nếu cần)
```java
@Entity
@Table(name = "hoa_don_huy_lich_su")
public class HoaDonHuyLichSu {
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "hoa_don_id")
    private HoaDon hoaDon;
    
    @Column(name = "ly_do", nullable = false)
    private String lyDo;
    
    @Column(name = "ngay_huy", nullable = false)
    private LocalDateTime ngayHuy;
    
    @Column(name = "khach_hang_id", nullable = false)
    private String khachHangId;
    
    // Constructors, getters, setters
}
```

### 6. Custom Exceptions
```java
public class HoaDonKhongTheHuyException extends RuntimeException {
    public HoaDonKhongTheHuyException(String message) {
        super(message);
    }
}

public class HoaDonNotFoundException extends RuntimeException {
    public HoaDonNotFoundException(String message) {
        super(message);
    }
}
```

## 🧪 Testing

### Test với Postman
1. **Setup**: Đăng nhập và lấy JWT token
2. **Request**:
   ```
   POST http://localhost:8080/khach-hang/hoa-don/huy/{hoaDonId}
   Headers: 
     Authorization: Bearer {token}
     Content-Type: application/json
   Body:
   {
     "lyDo": "Tôi muốn hủy đơn hàng vì đã mua ở nơi khác rẻ hơn"
   }
   ```

### Test với curl
```bash
curl -X POST http://localhost:8080/khach-hang/hoa-don/huy/{hoaDonId} \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "lyDo": "Tôi muốn hủy đơn hàng vì đã mua ở nơi khác rẻ hơn"
  }'
```

## 📝 Notes
- Chỉ cho phép hủy đơn hàng ở trạng thái "Chờ xác nhận" và "Đã xác nhận"
- Lưu lịch sử hủy để admin có thể xem xét
- Có thể thêm notification cho admin khi có yêu cầu hủy
- Có thể thêm email thông báo cho khách hàng 