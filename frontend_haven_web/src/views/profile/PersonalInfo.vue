<template>
  <div class="personal-info">
    <!-- Header -->
    <div class="section-header">
      <h2>Thông tin cá nhân</h2>
      <p>Cập nhật thông tin cá nhân của bạn</p>
    </div>

    <!-- Notification -->
    <div v-if="notification.show" :class="['notification', notification.type]">
      <i :class="notification.type === 'success' ? 'bi bi-check-circle' : 'bi bi-exclamation-triangle'"></i>
      {{ notification.message }}
      <button @click="hideNotification" class="close-btn">&times;</button>
    </div>

    <!-- Form -->
    <form @submit.prevent="handleUpdateProfile" class="profile-form">
      <div class="form-grid">
        <!-- Avatar Section -->
        <div class="avatar-section">
          <div class="avatar-upload">
            <img v-if="avatarPreview" :src="avatarPreview" alt="Avatar Preview" class="avatar-preview" />
            <div v-else-if="userInfo?.hinhAnh" class="avatar-preview">
              <img :src="getAvatarUrl(userInfo.hinhAnh)" alt="Current Avatar" />
            </div>
            <div v-else class="avatar-placeholder">
              <i class="bi bi-person-circle"></i>
              <span>Chưa có ảnh</span>
            </div>
            <div class="avatar-overlay">
              <label for="avatarInput" class="upload-btn">
                <i class="bi bi-camera"></i>
                <span>Thay đổi ảnh</span>
              </label>
              <input 
                id="avatarInput" 
                type="file" 
                accept="image/*" 
                @change="handleAvatarChange" 
                style="display: none"
              />
            </div>
          </div>
          <button v-if="avatarPreview" @click="removeAvatar" type="button" class="remove-avatar-btn">
            <i class="bi bi-trash"></i>
            Xóa ảnh
          </button>
        </div>

        <!-- Personal Information -->
        <div class="info-section">
          <div class="form-row">
            <div class="form-group">
              <label for="tenKhachHang">Họ và tên *</label>
              <input 
                id="tenKhachHang"
                v-model="form.tenKhachHang" 
                type="text" 
                placeholder="Nhập họ và tên"
                :class="{ 'error': errors.tenKhachHang }"
                @blur="validateField('tenKhachHang')"
              />
              <span v-if="errors.tenKhachHang" class="error-message">{{ errors.tenKhachHang }}</span>
            </div>
            
            <div class="form-group">
              <label for="email">Email *</label>
              <input 
                id="email"
                v-model="form.email" 
                type="email" 
                placeholder="example@email.com"
                :class="{ 'error': errors.email }"
                @blur="validateField('email')"
              />
              <span v-if="errors.email" class="error-message">{{ errors.email }}</span>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="sdt">Số điện thoại *</label>
              <input 
                id="sdt"
                v-model="form.sdt" 
                type="tel" 
                placeholder="0123456789"
                :class="{ 'error': errors.sdt }"
                @blur="validateField('sdt')"
                maxlength="10"
              />
              <span v-if="errors.sdt" class="error-message">{{ errors.sdt }}</span>
            </div>
            
            <div class="form-group">
              <label>Giới tính *</label>
              <div class="radio-group" :class="{ 'error': errors.gioiTinh }">
                <label class="radio-label">
                  <input v-model="form.gioiTinh" type="radio" name="gender" value="1" />
                  <span class="radio-custom"></span>
                  Nam
                </label>
                <label class="radio-label">
                  <input v-model="form.gioiTinh" type="radio" name="gender" value="0" />
                  <span class="radio-custom"></span>
                  Nữ
                </label>
              </div>
              <span v-if="errors.gioiTinh" class="error-message">{{ errors.gioiTinh }}</span>
            </div>
          </div>

                     <div class="form-row">
             <div class="form-group">
               <label for="ngaySinh">Ngày sinh</label>
               <input 
                 id="ngaySinh"
                 v-model="form.ngaySinh" 
                 type="date"
                 :class="{ 'error': errors.ngaySinh }"
               />
               <span v-if="errors.ngaySinh" class="error-message">{{ errors.ngaySinh }}</span>
             </div>
           </div>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="form-actions">
        <button type="button" @click="resetForm" class="btn btn-secondary">
          <i class="bi bi-arrow-clockwise"></i>
          Khôi phục
        </button>
        <button type="submit" class="btn btn-primary" :disabled="loading">
          <i v-if="loading" class="bi bi-arrow-clockwise spin"></i>
          <i v-else class="bi bi-check-lg"></i>
          {{ loading ? 'Đang cập nhật...' : 'Cập nhật thông tin' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { jwtDecode } from 'jwt-decode';
import { getProfile, updateProfile as updateProfileService, getCurrentUserInfo, testAuth } from '@/services/profile.js';

// Reactive data
const userInfo = ref(null);
const avatarFile = ref(null);
const avatarPreview = ref(null);
const loading = ref(false);
const notification = ref({ show: false, type: 'success', message: '' });

const form = reactive({
  tenKhachHang: '',
  email: '',
  sdt: '',
  gioiTinh: '',
  ngaySinh: ''
});

const errors = reactive({});

// Validation rules
const validationRules = {
  tenKhachHang: [
    { required: true, message: 'Vui lòng nhập họ và tên' },
    { minLength: 2, message: 'Họ và tên phải có ít nhất 2 ký tự' },
    { maxLength: 50, message: 'Họ và tên không được vượt quá 50 ký tự' }
  ],
  email: [
    { required: true, message: 'Vui lòng nhập email' },
    { type: 'email', message: 'Email không đúng định dạng' }
  ],
  sdt: [
    { required: true, message: 'Vui lòng nhập số điện thoại' },
    { pattern: /^0\d{9}$/, message: 'Số điện thoại phải bắt đầu bằng 0 và có đúng 10 chữ số' }
  ],
  gioiTinh: [
    { required: true, message: 'Vui lòng chọn giới tính' }
  ]
};

// Methods
const getAvatarUrl = (imageName) => {
  if (!imageName) return null;
  
  // Nếu là URL từ Google (bắt đầu bằng http/https)
  if (imageName.startsWith('http://') || imageName.startsWith('https://')) {
    return imageName;
  }
  
  // Nếu là tên file local
  return `http://localhost:8080/uploads/nhan_vien/${imageName}`;
};

const loadUserInfo = async () => {
  try {
    // Thử test authentication trước
    try {
      console.log('PersonalInfo - Testing authentication...');
      const authResponse = await testAuth();
      console.log('PersonalInfo - Auth test response:', authResponse);
    } catch (authError) {
      console.log('PersonalInfo - Auth test failed:', authError);
    }
    
    // Thử lấy thông tin đầy đủ từ backend
    try {
      console.log('PersonalInfo - Attempting to get current user info from backend...');
      const response = await getCurrentUserInfo();
      console.log('PersonalInfo - Backend response:', response);
      if (response.success && response.data) {
        userInfo.value = response.data;
        console.log('PersonalInfo - Loaded user info from backend:', userInfo.value);
        
        // Populate form với dữ liệu từ backend
        console.log('PersonalInfo - Populating form with backend data:', userInfo.value);
        Object.assign(form, {
          tenKhachHang: userInfo.value.tenKhachHang || '',
          email: userInfo.value.email || '',
          sdt: userInfo.value.sdt || '',
          gioiTinh: userInfo.value.gioiTinh?.toString() || '',
          ngaySinh: userInfo.value.ngaySinh || ''
        });
        console.log('PersonalInfo - Form after populate from backend:', form);
        return; // Thoát sớm nếu lấy được thông tin từ backend
      }
    } catch (backendError) {
      console.log('PersonalInfo - Backend API not available, trying profile endpoint:', backendError);
      
      // Thử endpoint profile cũ
      try {
        const profileResponse = await getProfile();
        if (profileResponse.success && profileResponse.data) {
          userInfo.value = profileResponse.data;
          console.log('PersonalInfo - Loaded user info from profile endpoint:', userInfo.value);
          
          // Populate form với dữ liệu từ profile endpoint
          Object.assign(form, {
            tenKhachHang: userInfo.value.tenKhachHang || '',
            email: userInfo.value.email || '',
            sdt: userInfo.value.sdt || '',
            gioiTinh: userInfo.value.gioiTinh?.toString() || '',
            ngaySinh: userInfo.value.ngaySinh || ''
          });
          return;
        }
      } catch (profileError) {
        console.log('PersonalInfo - Profile endpoint also failed, using JWT token:', profileError);
      }
    }
    
    // Fallback: lấy từ JWT token (chỉ khi cả 2 endpoint đều fail)
    const token = localStorage.getItem('authToken');
    console.log('PersonalInfo - Token from localStorage:', token ? 'exists' : 'not found');
    if (!token) return;

    const decoded = jwtDecode(token);
    console.log('PersonalInfo - Decoded JWT token:', decoded);
    
    userInfo.value = {
      id: decoded.userId || decoded.sub || decoded.id,
      tenKhachHang: decoded.tenKhachHang || decoded.name || decoded.sub,
      email: decoded.email,
      hinhAnh: decoded.hinhAnh || decoded.picture || decoded.image,
      sdt: decoded.sdt || decoded.phone,
      gioiTinh: decoded.gioiTinh,
      ngaySinh: decoded.ngaySinh,
      loginProvider: decoded.loginProvider || 'LOCAL'
    };
    console.log('PersonalInfo - User info from JWT (fallback):', userInfo.value);

    // Populate form với dữ liệu từ JWT (fallback)
    Object.assign(form, {
      tenKhachHang: userInfo.value.tenKhachHang || '',
      email: userInfo.value.email || '',
      sdt: userInfo.value.sdt || '',
      gioiTinh: userInfo.value.gioiTinh?.toString() || '',
      ngaySinh: userInfo.value.ngaySinh || ''
    });
    console.log('PersonalInfo - Form after populate from JWT (fallback):', form);
  } catch (error) {
    console.error('Lỗi khi tải thông tin người dùng:', error);
  }
};

const handleAvatarChange = (event) => {
  const file = event.target.files[0];
  if (!file) return;

  if (!file.type.startsWith('image/')) {
    showNotification('error', 'Vui lòng chọn file ảnh');
    return;
  }

  if (file.size > 5 * 1024 * 1024) {
    showNotification('error', 'Kích thước file không được vượt quá 5MB');
    return;
  }

  avatarFile.value = file;
  avatarPreview.value = URL.createObjectURL(file);
};

const removeAvatar = () => {
  if (avatarPreview.value) {
    URL.revokeObjectURL(avatarPreview.value);
  }
  avatarFile.value = null;
  avatarPreview.value = null;
  const fileInput = document.getElementById('avatarInput');
  if (fileInput) fileInput.value = '';
};

const validateField = (fieldName) => {
  const rules = validationRules[fieldName];
  if (!rules) return true;

  const value = form[fieldName];
  
  for (const rule of rules) {
    if (rule.required && (!value || value.toString().trim() === '')) {
      errors[fieldName] = rule.message;
      return false;
    }
    if (rule.minLength && value && value.length < rule.minLength) {
      errors[fieldName] = rule.message;
      return false;
    }
    if (rule.maxLength && value && value.length > rule.maxLength) {
      errors[fieldName] = rule.message;
      return false;
    }
    if (rule.pattern && value && !rule.pattern.test(value)) {
      errors[fieldName] = rule.message;
      return false;
    }
    if (rule.type === 'email' && value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
      errors[fieldName] = rule.message;
      return false;
    }
  }
  
  delete errors[fieldName];
  return true;
};

const validateForm = () => {
  const fields = Object.keys(validationRules);
  let isValid = true;
  
  fields.forEach(field => {
    if (!validateField(field)) {
      isValid = false;
    }
  });
  
  return isValid;
};

const handleUpdateProfile = async () => {
  if (!validateForm()) {
    showNotification('error', 'Vui lòng kiểm tra lại thông tin đã nhập');
    return;
  }

  try {
    loading.value = true;
    
    // Tạo FormData để gửi lên server
    const formData = new FormData();
    
         // Chuẩn bị dữ liệu để gửi
     const updateData = {
       tenKhachHang: form.tenKhachHang,
       email: form.email,
       sdt: form.sdt,
       gioiTinh: parseInt(form.gioiTinh),
       ngaySinh: form.ngaySinh || null,
       loginProvider: userInfo.value.loginProvider || 'LOCAL',
       trangThai: userInfo.value.trangThai || 1
     };
    
    formData.append('data', JSON.stringify(updateData));
    
    // Thêm file ảnh nếu có
    if (avatarFile.value) {
      formData.append('hinhAnh', avatarFile.value);
    }
    
    // Gọi API cập nhật profile
    const response = await updateProfileService(userInfo.value.id, formData);
    
    if (response.success) {
      showNotification('success', 'Cập nhật thông tin thành công!');
      
      // Cập nhật lại thông tin user
      await loadUserInfo();
      
      // Xóa preview avatar
      removeAvatar();
    } else {
      showNotification('error', response.message || 'Có lỗi xảy ra khi cập nhật thông tin');
    }
    
  } catch (error) {
    console.error('Lỗi khi cập nhật thông tin:', error);
    showNotification('error', 'Có lỗi xảy ra khi cập nhật thông tin');
  } finally {
    loading.value = false;
  }
};

const resetForm = () => {
  if (userInfo.value) {
    Object.assign(form, {
      tenKhachHang: userInfo.value.tenKhachHang || '',
      email: userInfo.value.email || '',
      sdt: userInfo.value.sdt || '',
      gioiTinh: userInfo.value.gioiTinh?.toString() || '',
      ngaySinh: userInfo.value.ngaySinh || ''
    });
  }
  
  removeAvatar();
  Object.keys(errors).forEach(key => delete errors[key]);
  
  showNotification('success', 'Đã khôi phục thông tin ban đầu');
};

const showNotification = (type, message) => {
  notification.value = { show: true, type, message };
  setTimeout(() => hideNotification(), 5000);
};

const hideNotification = () => {
  notification.value.show = false;
};

onMounted(() => {
  console.log('PersonalInfo - Component mounted');
  console.log('PersonalInfo - Checking localStorage for authToken...');
  const token = localStorage.getItem('authToken');
  console.log('PersonalInfo - Token exists:', !!token);
  if (token) {
    try {
      const decoded = jwtDecode(token);
      console.log('PersonalInfo - Token can be decoded:', !!decoded);
      console.log('PersonalInfo - Token payload:', decoded);
    } catch (error) {
      console.error('PersonalInfo - Error decoding token:', error);
    }
  }
  loadUserInfo();
});
</script>

<style scoped>
.personal-info {
  max-width: 100%;
}

.section-header {
  margin-bottom: 30px;
}

.section-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.section-header p {
  color: #6c757d;
  margin: 0;
}

.notification {
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.notification.success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.notification.error {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.notification .close-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  margin-left: auto;
  color: inherit;
}

.profile-form {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.form-grid {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 40px;
  margin-bottom: 30px;
}

.avatar-section {
  text-align: center;
}

.avatar-upload {
  position: relative;
  width: 150px;
  height: 150px;
  margin: 0 auto 20px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.avatar-upload:hover {
  border-color: #4ba27b;
}

.avatar-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: #f8f9fa;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #6c757d;
  font-size: 14px;
}

.avatar-placeholder i {
  font-size: 40px;
  margin-bottom: 8px;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-upload:hover .avatar-overlay {
  opacity: 1;
}

.upload-btn {
  color: white;
  text-decoration: none;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  font-size: 12px;
}

.upload-btn i {
  font-size: 20px;
}

.remove-avatar-btn {
  background: #dc3545;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 5px;
  margin: 0 auto;
  transition: background 0.3s ease;
}

.remove-avatar-btn:hover {
  background: #c82333;
}

.info-section {
  flex: 1;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.form-group input {
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.form-group input:focus {
  outline: none;
  border-color: #4ba27b;
}

.form-group input.error {
  border-color: #dc3545;
}

.error-message {
  color: #dc3545;
  font-size: 12px;
  margin-top: 4px;
}

.radio-group {
  display: flex;
  gap: 20px;
  margin-top: 5px;
}

.radio-group.error .radio-custom {
  border-color: #dc3545;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
}

.radio-label input[type='radio'] {
  display: none;
}

.radio-custom {
  width: 18px;
  height: 18px;
  border: 2px solid #ddd;
  border-radius: 50%;
  position: relative;
  transition: border-color 0.3s ease;
}

.radio-label input[type='radio']:checked + .radio-custom {
  border-color: #4ba27b;
}

.radio-label input[type='radio']:checked + .radio-custom::after {
  content: '';
  width: 10px;
  height: 10px;
  background: #4ba27b;
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.form-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: #4ba27b;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #3d8b6a;
  transform: translateY(-1px);
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #5a6268;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
    gap: 30px;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .btn {
    width: 100%;
    justify-content: center;
  }
}
</style> 