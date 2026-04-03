<template>
  <div class="google-info">
    <div class="section-header">
      <h2>Thông tin từ Google</h2>
      <p>Thông tin được đồng bộ từ tài khoản Google của bạn</p>
    </div>

    <div class="google-info-card">
      <div class="google-header">
        <i class="bi bi-google"></i>
        <h3>Tài khoản Google</h3>
      </div>

      <div class="info-grid">
        <div class="info-item">
          <label>Email Google:</label>
          <span>{{ userInfo?.email || 'Chưa có thông tin' }}</span>
        </div>
        
        <div class="info-item">
          <label>Tên hiển thị:</label>
          <span>{{ userInfo?.tenKhachHang || 'Chưa có thông tin' }}</span>
        </div>
        
        <div class="info-item">
          <label>Ảnh đại diện:</label>
          <div class="avatar-display">
            <img v-if="userInfo?.hinhAnh" :src="getAvatarUrl(userInfo.hinhAnh)" alt="Google Avatar" />
            <div v-else class="no-avatar">
              <i class="bi bi-person-circle"></i>
              <span>Chưa có ảnh</span>
            </div>
          </div>
        </div>
        
        <div class="info-item">
          <label>Loại đăng nhập:</label>
          <span class="login-type">
            <i class="bi bi-google"></i>
            {{ userInfo?.loginProvider === 'GOOGLE' ? 'Google OAuth' : 'Local' }}
          </span>
        </div>
      </div>

      <div class="google-note">
        <i class="bi bi-info-circle"></i>
        <p>
          Thông tin từ Google sẽ được tự động đồng bộ khi bạn đăng nhập. 
          Bạn có thể cập nhật thông tin bổ sung trong tab "Thông tin cá nhân".
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { jwtDecode } from 'jwt-decode';

const userInfo = ref(null);

const getAvatarUrl = (imageName) => {
  if (!imageName) return null;
  
  // Nếu là URL từ Google (bắt đầu bằng http/https)
  if (imageName.startsWith('http://') || imageName.startsWith('https://')) {
    return imageName;
  }
  
  // Nếu là tên file local
  return `http://localhost:8080/uploads/khach_hang/${imageName}`;
};

const loadUserInfo = async () => {
  try {
    const token = localStorage.getItem('authToken');
    if (!token) return;

    const decoded = jwtDecode(token);
    console.log('GoogleInfo - Decoded JWT token:', decoded);
    
    userInfo.value = {
      id: decoded.sub || decoded.id,
      tenKhachHang: decoded.tenKhachHang || decoded.name || decoded.sub,
      email: decoded.email,
      hinhAnh: decoded.hinhAnh || decoded.picture || decoded.image,
      sdt: decoded.sdt || decoded.phone,
      gioiTinh: decoded.gioiTinh,
      ngaySinh: decoded.ngaySinh,
      cccd: decoded.cccd,
      loginProvider: decoded.loginProvider || 'LOCAL'
    };
  } catch (error) {
    console.error('Lỗi khi tải thông tin Google:', error);
  }
};

onMounted(() => {
  loadUserInfo();
});
</script>

<style scoped>
.google-info {
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

.google-info-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.google-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.google-header i {
  font-size: 32px;
  color: #4285f4;
}

.google-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item label {
  font-size: 14px;
  font-weight: 500;
  color: #6c757d;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-item span {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.avatar-display {
  display: flex;
  align-items: center;
  gap: 15px;
}

.avatar-display img {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #f0f0f0;
}

.no-avatar {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #6c757d;
  font-size: 14px;
}

.no-avatar i {
  font-size: 24px;
}

.login-type {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #4285f4;
  font-weight: 600;
}

.login-type i {
  font-size: 16px;
}

.google-note {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: flex-start;
  gap: 15px;
  border-left: 4px solid #4285f4;
}

.google-note i {
  font-size: 20px;
  color: #4285f4;
  margin-top: 2px;
}

.google-note p {
  margin: 0;
  color: #6c757d;
  line-height: 1.6;
  font-size: 14px;
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .google-header {
    flex-direction: column;
    text-align: center;
    gap: 10px;
  }
}
</style> 