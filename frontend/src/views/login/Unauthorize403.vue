<script setup>
// import { useRouter } from 'vue-router';
// const router = useRouter();
// const goBack = () => router.go(-1); 
</script>

<template>
  <div class="unauthorized-container">
    <div class="unauthorized-box">
      <div class="icon-wrapper">
        <i class="fas fa-lock"></i>
      </div>
      <h1>403</h1>
      <h2>Truy cập bị từ chối</h2>
      <p>
        Xin lỗi, bạn không có quyền truy cập trang này. <br>
      </p>
      <div class="actions">
        <router-link to="/signin" class="btn btn-primary">
          <i class="fas fa-sign-in-alt"></i> Quay lại trang đăng nhập
        </router-link>
        <router-link to="/signup" class="btn btn-secondary">
          <i class="fas fa-home"></i> Đăng ký
        </router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Roboto:wght@400;700;900&display=swap');

.unauthorized-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f4f6f9;
  font-family: 'Roboto', sans-serif;
  color: #333;
}

.unauthorized-box {
  background: #ffffff;
  padding: 40px 50px;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  text-align: center;
  max-width: 500px;
  width: 90%;
}

.icon-wrapper {
  font-size: 5rem;
  color: #dc3545; /* Màu đỏ cảnh báo */
  margin-bottom: 20px;
}

h1 {
  font-size: 7rem;
  font-weight: 900;
  margin: 0;
  color: #2c3e50;
  line-height: 1;
}

h2 {
  font-size: 1.75rem;
  margin: 10px 0 15px 0;
  color: #34495e;
}

p {
  font-size: 1rem;
  color: #6c757d;
  margin-bottom: 30px;
  line-height: 1.6;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 15px;
  align-items: center;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 12px 25px;
  border-radius: 8px;
  text-decoration: none;
  font-weight: bold;
  font-size: 1rem;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 100%;
  max-width: 300px;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover {
  background-color: #0056b3;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 123, 255, 0.3);
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #5a6268;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(108, 117, 125, 0.3);
}
</style>