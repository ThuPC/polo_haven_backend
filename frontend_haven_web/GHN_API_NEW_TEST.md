# GHN API Mới - Test Guide (Sau ngày 1/7)

## 🔄 Thay đổi API địa chỉ mới:

### **1. API Tỉnh/Thành phố:**
```bash
# Cũ: /master-data/province
# Mới: /v2/master-data/province

curl -X GET "http://localhost:8080/api/v1/ghn/provinces" \
  -H "Content-Type: application/json"
```

### **2. API Quận/Huyện:**
```bash
# Cũ: /master-data/district?province_id=201
# Mới: /v2/master-data/district?province_id=201

curl -X GET "http://localhost:8080/api/v1/ghn/districts/201" \
  -H "Content-Type: application/json"
```

### **3. API Phường/Xã:**
```bash
# Cũ: /master-data/ward?district_id=3440
# Mới: /v2/master-data/ward?district_id=3440

curl -X GET "http://localhost:8080/api/v1/ghn/wards/3440" \
  -H "Content-Type: application/json"
```

### **4. API Tính phí vận chuyển (Đơn giản hóa):**
```bash
# Loại bỏ service_id, chỉ cần các thông tin cơ bản
curl -X POST "http://localhost:8080/api/v1/ghn/shipping-fee" \
  -H "Content-Type: application/json" \
  -d '{
    "from_district_id": 202,
    "to_district_id": 3440,
    "to_ward_code": "13010",
    "weight": 200,
    "height": 10,
    "length": 20,
    "width": 20
  }'
```

### **5. API Phí theo vùng (Fallback):**
```bash
curl -X POST "http://localhost:8080/api/v1/ghn/regional-shipping-fee" \
  -H "Content-Type: application/json" \
  -d '{
    "to_district_id": 3440
  }'
```

## 🗑️ Loại bỏ:

### **❌ Không còn sử dụng:**
- `available-services` endpoint
- `service_id` trong request tính phí
- Các shop ID phức tạp (chỉ giữ Hà Nội và TP.HCM)

### **✅ Giữ lại:**
- API địa chỉ (provinces, districts, wards)
- API tính phí vận chuyển
- Fallback phí theo vùng

## 🧪 Test Cases:

### **Test 1: Lấy địa chỉ Hà Nội**
```bash
# Lấy quận/huyện Hà Nội
curl -X GET "http://localhost:8080/api/v1/ghn/districts/201"

# Lấy phường/xã Cầu Giấy
curl -X GET "http://localhost:8080/api/v1/ghn/wards/202"
```

### **Test 2: Tính phí Hà Nội → TP.HCM**
```bash
curl -X POST "http://localhost:8080/api/v1/ghn/shipping-fee" \
  -H "Content-Type: application/json" \
  -d '{
    "from_district_id": 202,
    "to_district_id": 2270,
    "to_ward_code": "231003",
    "weight": 500
  }'
```

### **Test 3: Test fallback phí theo vùng**
```bash
curl -X POST "http://localhost:8080/api/v1/ghn/regional-shipping-fee" \
  -H "Content-Type: application/json" \
  -d '{
    "to_district_id": 2270
  }'
```

## 📊 Kết quả mong đợi:

### **✅ Thành công:**
- API địa chỉ trả về danh sách đầy đủ
- Tính phí chính xác cho các tuyến có sẵn
- Fallback phí theo vùng khi không có tuyến

### **⚠️ Lưu ý:**
- API mới có thể có cấu trúc response khác
- Cần test kỹ các trường hợp lỗi
- Đảm bảo token GHN vẫn hợp lệ

## 🔧 Debug:

### **Kiểm tra log backend:**
```bash
# Xem log khi gọi API
tail -f backend/logs/application.log
```

### **Kiểm tra response:**
```bash
# Test trực tiếp với GHN
curl -X GET "https://online-gateway.ghn.vn/shiip/public-api/v2/master-data/province" \
  -H "Token: a7719d89-583c-11f0-99f8-027692bb547c"
``` 