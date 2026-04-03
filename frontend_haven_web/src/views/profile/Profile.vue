<template>
  <div class="profile-container">
    <!-- Header -->
    <div class="profile-header">
      <h1 class="profile-title">Hồ sơ của tôi</h1>
      <p class="profile-subtitle">Quản lý thông tin cá nhân và tài khoản</p>
    </div>

    <!-- Main Content -->
    <div class="profile-content">
      <!-- Sidebar Navigation -->
      <div class="profile-sidebar">
        <div class="user-info-card">
          <div class="user-avatar">
            <img v-if="userInfo?.hinhAnh" :src="getAvatarUrl(userInfo.hinhAnh)" alt="Avatar" />
            <div v-else class="avatar-placeholder">
              <i class="bi bi-person-circle"></i>
            </div>
          </div>
          <div class="user-details">
            <h3>{{ userInfo?.tenKhachHang || 'Khách hàng' }}</h3>
            <p>{{ userInfo?.email || 'email@example.com' }}</p>
          </div>
        </div>

        <nav class="profile-nav">
          <router-link 
            to="/profile/personal-info" 
            class="nav-item"
            active-class="active"
          >
            <i class="bi bi-person"></i>
            <span>Thông tin cá nhân</span>
          </router-link>
          
          <router-link 
            to="/profile/security" 
            class="nav-item"
            active-class="active"
          >
            <i class="bi bi-shield-lock"></i>
            <span>Bảo mật tài khoản</span>
          </router-link>
          
          <router-link 
            to="/profile/addresses" 
            class="nav-item"
            active-class="active"
          >
            <i class="bi bi-geo-alt"></i>
            <span>Quản lý địa chỉ</span>
          </router-link>
          
          <router-link 
            to="/profile/orders" 
            class="nav-item"
            active-class="active"
          >
            <i class="bi bi-box-seam"></i>
            <span>Lịch sử đơn hàng</span>
          </router-link>
          
          <router-link 
            v-if="userInfo?.loginProvider === 'GOOGLE'"
            to="/profile/google-info" 
            class="nav-item"
            active-class="active"
          >
            <i class="bi bi-google"></i>
            <span>Thông tin Google</span>
          </router-link>
        </nav>
      </div>

      <!-- Main Content Area -->
      <div class="profile-main">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getUserFromToken, getAvatarUrl, debugJwtToken } from '@/utils/jwtHelper';

const router = useRouter();
const userInfo = ref(null);

// Sử dụng utility function từ jwtHelper

const loadUserInfo = async () => {
  try {
    const token = localStorage.getItem('authToken');
    if (!token) {
      router.push('/login');
      return;
    }

    // Debug JWT token
    debugJwtToken();
    
    // Sử dụng utility function để lấy thông tin user
    userInfo.value = getUserFromToken();
    
    if (!userInfo.value) {
      router.push('/login');
      return;
    }
  } catch (error) {
    console.error('Lỗi khi tải thông tin người dùng:', error);
    router.push('/login');
  }
};

onMounted(() => {
  loadUserInfo();
});
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.profile-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 30px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.profile-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 10px 0;
}

.profile-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.profile-content {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 30px;
  min-height: 600px;
}

.profile-sidebar {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
  height: fit-content;
}

.user-info-card {
  text-align: center;
  padding-bottom: 30px;
  border-bottom: 1px solid #eee;
  margin-bottom: 30px;
}

.user-avatar {
  width: 100px;
  height: 100px;
  margin: 0 auto 20px;
  border-radius: 50%;
  overflow: hidden;
  border: 4px solid #f0f0f0;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: #6c757d;
}

.user-details h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.user-details p {
  margin: 0;
  color: #6c757d;
  font-size: 14px;
}

.profile-nav {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px 20px;
  text-decoration: none;
  color: #333;
  border-radius: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.nav-item:hover {
  background: #f8f9fa;
  color: #4ba27b;
}

.nav-item.active {
  background: #4ba27b;
  color: white;
}

.nav-item i {
  font-size: 18px;
  width: 20px;
}

.profile-main {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
}

@media (max-width: 768px) {
  .profile-content {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .profile-sidebar {
    order: 2;
  }
  
  .profile-main {
    order: 1;
  }
}
</style> 