<template>
  <div class="orders">
    <div class="section-header">
      <h2>Lịch sử đơn hàng</h2>
      <p>Xem và theo dõi các đơn hàng đã đặt</p>
    </div>

    <div class="coming-soon">
      <div class="coming-soon-content">
        <i class="bi bi-box-seam"></i>
        <h3>Tính năng đang phát triển</h3>
        <p>Chức năng lịch sử đơn hàng sẽ sớm được cập nhật. Vui lòng quay lại sau!</p>
        <router-link to="/profile/personal-info" class="btn btn-primary">
          <i class="bi bi-arrow-left"></i>
          Quay lại thông tin cá nhân
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
// Component placeholder cho lịch sử đơn hàng
</script>

<style scoped>
.orders {
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

.coming-soon {
  background: white;
  border-radius: 12px;
  padding: 60px 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.coming-soon-content i {
  font-size: 64px;
  color: #4ba27b;
  margin-bottom: 20px;
}

.coming-soon-content h3 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 15px 0;
}

.coming-soon-content p {
  font-size: 16px;
  color: #6c757d;
  margin: 0 0 30px 0;
  line-height: 1.6;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.btn-primary {
  background: #4ba27b;
  color: white;
}

.btn-primary:hover {
  background: #3d8b6a;
  transform: translateY(-1px);
}
</style> 