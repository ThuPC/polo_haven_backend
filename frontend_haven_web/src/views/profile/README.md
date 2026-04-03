# Chức năng Hồ sơ của tôi (Profile)

## Tổng quan
Chức năng "Hồ sơ của tôi" cho phép khách hàng quản lý thông tin cá nhân và tài khoản của mình.

## Cấu trúc thư mục
```
src/views/profile/
├── Profile.vue          # Component chính với layout và navigation
├── PersonalInfo.vue     # Thông tin cá nhân và avatar
├── Security.vue         # Thay đổi mật khẩu
├── Addresses.vue        # Quản lý địa chỉ (placeholder)
├── Orders.vue           # Lịch sử đơn hàng (placeholder)
└── README.md           # Hướng dẫn này
```

## Các tính năng đã hoàn thành

### 1. Thông tin cá nhân (PersonalInfo.vue)
- **Xem và chỉnh sửa thông tin cá nhân:**
  - Họ tên, email, số điện thoại
  - Giới tính, ngày sinh
  - CCCD/CMND
- **Quản lý avatar:**
  - Upload ảnh đại diện
  - Preview ảnh trước khi lưu
  - Xóa ảnh đã chọn
- **Validation:**
  - Kiểm tra định dạng email
  - Kiểm tra số điện thoại (10 số, bắt đầu bằng 0)
  - Kiểm tra CCCD/CMND (9-12 số)
  - Validation real-time với thông báo lỗi

### 2. Bảo mật tài khoản (Security.vue)
- **Thay đổi mật khẩu:**
  - Nhập mật khẩu hiện tại
  - Nhập mật khẩu mới với xác nhận
  - Hiển thị/ẩn mật khẩu
- **Kiểm tra độ mạnh mật khẩu:**
  - Thanh progress hiển thị độ mạnh
  - Danh sách yêu cầu mật khẩu
  - Màu sắc theo mức độ (Rất yếu, Yếu, Trung bình, Mạnh)
- **Validation mật khẩu:**
  - Ít nhất 8 ký tự
  - Có chữ hoa, chữ thường
  - Có số và ký tự đặc biệt
  - Xác nhận mật khẩu khớp

### 3. Layout và Navigation (Profile.vue)
- **Sidebar navigation:**
  - Thông tin user với avatar
  - Menu điều hướng với icons
  - Active state cho trang hiện tại
- **Responsive design:**
  - Tối ưu cho mobile và desktop
  - Grid layout linh hoạt

## Các tính năng đang phát triển

### 1. Quản lý địa chỉ (Addresses.vue)
- Thêm địa chỉ mới
- Chỉnh sửa địa chỉ hiện có
- Đặt địa chỉ mặc định
- Xóa địa chỉ

### 2. Lịch sử đơn hàng (Orders.vue)
- Xem danh sách đơn hàng
- Chi tiết đơn hàng
- Trạng thái đơn hàng
- Tải lại đơn hàng

## Cách sử dụng

### 1. Truy cập Profile
- Đăng nhập vào hệ thống
- Click vào avatar/user icon trên header
- Chọn "Hồ sơ của tôi" từ dropdown menu

### 2. Cập nhật thông tin cá nhân
- Vào tab "Thông tin cá nhân"
- Chỉnh sửa các trường thông tin
- Upload avatar mới (nếu muốn)
- Click "Cập nhật thông tin"

### 3. Thay đổi mật khẩu
- Vào tab "Bảo mật tài khoản"
- Nhập mật khẩu hiện tại
- Nhập mật khẩu mới (tuân theo yêu cầu)
- Xác nhận mật khẩu mới
- Click "Thay đổi mật khẩu"

## API Endpoints cần thiết

### Backend cần implement các API sau:

1. **GET /api/khach-hang/profile**
   - Lấy thông tin profile của user hiện tại

2. **PUT /api/khach-hang/profile**
   - Cập nhật thông tin profile
   - Hỗ trợ multipart/form-data cho avatar

3. **POST /api/khach-hang/change-password**
   - Thay đổi mật khẩu
   - Body: { currentPassword, newPassword }

4. **POST /api/khach-hang/upload-avatar**
   - Upload avatar riêng biệt
   - Hỗ trợ multipart/form-data

## Dependencies

### Frontend
- Vue 3 (Composition API)
- Vue Router
- Bootstrap Icons (bi-*)
- JWT Decode (jwt-decode)

### CSS Features
- CSS Grid và Flexbox
- CSS Variables cho màu sắc
- Responsive design
- Smooth transitions và animations

## Cấu hình

### Router Configuration
```javascript
{
  path: '/profile',
  name: 'Profile',
  component: Profile,
  children: [
    { path: '', redirect: '/profile/personal-info' },
    { path: 'personal-info', component: PersonalInfo },
    { path: 'security', component: Security },
    { path: 'addresses', component: Addresses },
    { path: 'orders', component: Orders }
  ]
}
```

### Service Configuration
- API base URL: `http://localhost:8080/api`
- Authentication: JWT token từ localStorage
- Error handling: Console logging và user notifications

## Lưu ý

1. **Authentication**: Cần đăng nhập để truy cập profile
2. **Token Management**: Sử dụng JWT token từ localStorage
3. **Error Handling**: Hiển thị thông báo lỗi cho user
4. **Loading States**: Hiển thị loading spinner khi gọi API
5. **Validation**: Client-side validation với real-time feedback
6. **File Upload**: Hỗ trợ upload ảnh với preview
7. **Responsive**: Tối ưu cho mobile và desktop

## Tương lai

- [ ] Tích hợp với backend API
- [ ] Thêm chức năng quản lý địa chỉ
- [ ] Thêm chức năng lịch sử đơn hàng
- [ ] Thêm chức năng thông báo
- [ ] Thêm chức năng xác thực 2FA
- [ ] Thêm chức năng liên kết tài khoản mạng xã hội 