<template>
  <div class="register-container">
    <main class="main-content">
      <section class="page-header min-vh-100">
        <div class="register-img-wrapper">
          <div class="register-img-bg">
            <div class="register-img-gradient"></div>
            <div class="register-img-content">
              <h4 class="text-white font-weight-bold">"Steve Jobs"</h4>
              <p class="text-white">Sáng tạo là điểm khác biệt giữa người dẫn đầu và kẻ theo sau</p>
            </div>
          </div>
        </div>
        <div class="register-form-center">
          <div class="card shadow-sm">
            <div class="card-header text-start pb-3 border-0">
              <h3 class="font-weight-bold text-dark text-center mb-0">Đăng ký tài khoản</h3>
            </div>
            <div class="card-body p-4">
              <form role="form" @submit.prevent="handleSubmit" class="needs-validation" novalidate>
                <div class="mb-3">
                  <input type="text" id="name" class="form-control form-control-lg" v-model="name" required
                    placeholder="Nhập họ và tên" />
                  <div class="invalid-feedback">Vui lòng nhập họ và tên.</div>
                </div>
                <div class="mb-3">
                  <input type="email" id="email" class="form-control form-control-lg" v-model="email" required
                    placeholder="Nhập email" />
                  <div class="invalid-feedback">Vui lòng nhập email.</div>
                </div>
                <div class="mb-3">
                  <input type="password" id="password" class="form-control form-control-lg" v-model="password"
                    required placeholder="Nhập mật khẩu" />
                  <div class="invalid-feedback">Vui lòng nhập mật khẩu.</div>
                </div>
                <div class="mb-3">
                  <input type="password" id="confirmPassword" class="form-control form-control-lg"
                    v-model="confirmPassword" required placeholder="Nhập lại mật khẩu" />
                  <div class="invalid-feedback">Vui lòng xác nhận mật khẩu.</div>
                </div>
                <div v-if="error" class="error-message">{{ error }}</div>
                <div class="text-center">
                  <button type="submit" class="btn btn-lg w-100 text-white mb-3 register-btn"
                    style="background: linear-gradient(310deg, #2dce89 0%, #2dcecc 100%);">
                    Đăng ký
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script>
import { useRouter } from 'vue-router';
export default {
  data() {
    return {
      name: '',
      email: '',
      password: '',
      confirmPassword: '',
      error: ''
    };
  },
  methods: {
    handleSubmit() {
      if (this.password !== this.confirmPassword) {
        this.error = 'Mật khẩu xác nhận không khớp!';
        return;
      }
      // Xử lý đăng ký ở đây (gửi dữ liệu lên server, v.v.)
      this.error = '';
      alert('Đăng ký thành công!');
      this.$router.push('/login');
    }
  }
};
</script>

<style scoped>
/* Ảnh ngang trên, form căn giữa dưới */
.register-img-wrapper {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 32px;
}
.register-img-bg {
  position: relative;
  width: 100vw;
  height: 260px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: url('https://raw.githubusercontent.com/creativetimofficial/public-assets/master/argon-dashboard-pro/assets/img/signin-ill.jpg');
  background-size: cover;
  background-position: center;
  border-radius: 0;
  margin-left: calc(-50vw + 50%);
  margin-right: calc(-50vw + 50%);
  object-fit: cover;
}
.register-img-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(310deg,rgba(45,206,137,0.7) 0%,rgba(45,206,204,0.7) 100%);
  z-index: 1;
}
.register-img-content {
  position: relative;
  z-index: 2;
  width: 100%;
  text-align: center;
}
.register-form-center {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}
/* Nền trắng cho trang đăng ký */
.register-container {
  margin: 0 auto;
  max-width: 500px;
  min-height: 100vh;
  width: 100vw;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

/* Phong cách giống trang đăng nhập */
.card {
  background: #ffffff;
  border: none;
  border-radius: 1rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  width: 700px;
  margin: 0 auto;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.15);
}

.card-header {
  background: transparent;
  border-bottom: none;
}

.register-btn {
  background-image: linear-gradient(310deg, #2dce89 0%, #2dcecc 100%);
  border: none;
  color: #fff;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  transition: all 0.3s ease;
  font-size: 18px;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(76, 51, 133, 0.08);
}

.register-btn:hover {
  background-image: linear-gradient(310deg, #2dce89 0%, #2dcecc 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(76, 51, 133, 0.12);
}

.card-body {
  padding: 2rem;
}

.form-group input,
.form-control {
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.2s;
  width: 100%;
  box-sizing: border-box;
}

.form-group input:focus,
.form-control:focus {
  border-color: #2dcecc;
  outline: none;
}
.error-message {
  color: #e74c3c;
  text-align: center;
  font-size: 15px;
  margin-bottom: -8px;
}

@media (max-width: 767.98px) {
  .row.align-items-center {
    flex-direction: column;
  }

  .col-12.col-md-6 {
    width: 100%;
    max-width: 100%;
  }

  .col-md-6 .position-relative {
    min-height: 50vh;
    margin-top: 2rem;
  }
}
</style>
