<template>
  <main class="main-content">
    <section class="page-header min-vh-100">
      <div class="container">
        <div class="row align-items-center min-vh-100">
          <!-- Cột trái: Form đăng nhập -->
          <div class="col-12 col-md-6">
            <div class="card shadow-sm">
              <div class="card-header text-start pb-3 border-0">
                <h2 class="font-weight-bold text-dark text-center mb-0">
                  Đăng nhập
                </h2>
              </div>
              <div class="card-body p-4">
                <!-- Form xử lý đăng nhập thường -->
                <form
                  role="form"
                  @submit.prevent="handleLogin"
                  class="needs-validation"
                  novalidate
                >
                  <div class="mb-3">
                    <input
                      id="sdt"
                      type="text"
                      class="form-control form-control-lg"
                      placeholder="Số điện thoại"
                      name="sdt"
                      v-model="authenticationRequest.sdt"
                      required
                    />
                  </div>
                  <div class="mb-3">
                    <input
                      id="password"
                      type="password"
                      class="form-control form-control-lg"
                      placeholder="Mật khẩu"
                      name="password"
                      v-model="authenticationRequest.matKhau"
                      required
                    />
                  </div>
                  <div class="mb-4 form-check">
                    <input
                      id="rememberMe"
                      name="remember-me"
                      type="checkbox"
                      class="form-check-input"
                    />
                    <label
                      class="form-check-label text-muted ms-2"
                      for="rememberMe"
                      >Lưu thông tin</label
                    >
                  </div>
                  <div class="text-center">
                    <button
                      type="submit"
                      class="btn btn-lg w-100 text-white mb-3"
                      style="
                        background: linear-gradient(
                          310deg,
                          #2dce89 0%,
                          #2dcecc 100%
                        );
                      "
                    >
                      Đăng nhập
                    </button>
                    <!-- Nút đăng nhập với Google -->
                    <a
                      class="btn btn-outline-primary btn-lg w-100"
                      @click="loginWithGoogle"
                      href="javascript:;"
                    >
                      <img
                        src="https://img.icons8.com/color/16/000000/google-logo.png"
                        class="me-2"
                      />
                      Đăng nhập với Google
                    </a>
                  </div>
                </form>
                <div class="text-center mt-3">
                  <p class="text-muted mb-0">
                    Chưa có tài khoản?
                    <a href="javascript:;" class="fw-bold gradient-text"
                      >Đăng ký</a
                    >
                  </p>
                </div>
              </div>
            </div>
          </div>
          <!-- Cột phải: Hình ảnh với gradient phủ -->
          <div class="col-12 col-md-6 d-none d-md-block">
            <div
              class="position-relative h-100 d-flex flex-column justify-content-center text-center p-4"
              style="
                background-image: url('https://raw.githubusercontent.com/creativetimofficial/public-assets/master/argon-dashboard-pro/assets/img/signin-ill.jpg');
                background-size: cover;
                background-position: center;
                border-radius: 1rem;
                min-height: 80vh;
              "
            >
              <div
                style="
                  position: absolute;
                  top: 0;
                  left: 0;
                  width: 100%;
                  height: 100%;
                  background: linear-gradient(
                    310deg,
                    rgba(45, 206, 137, 0.7) 0%,
                    rgba(45, 206, 204, 0.7) 100%
                  );
                  z-index: 1;
                  border-radius: 1rem;
                "
              ></div>
              <div style="position: relative; z-index: 2">
                <h4 class="text-white font-weight-bold">"Bill Gates"</h4>
                <p class="text-white">
                  Những khách hàng không hài lòng sẽ là bài học tuyệt vời cho
                  bạn
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { onBeforeUnmount, onBeforeMount, ref } from "vue";
import { useStore } from "vuex";
import { useRouter } from "vue-router";
import { jwtDecode } from "jwt-decode";
import { loginWithGoogleByRole, LoginFromCustomer } from "@/services/login";

const body = document.getElementsByTagName("body")[0];
const authenticationRequest = ref({
  sdt: "",
  matKhau: "",
});
const store = useStore();
const router = useRouter();

onBeforeMount(() => {
  localStorage.setItem("selectedRole", "KHACHHANG");
});

onBeforeUnmount(() => {});

const loginWithGoogle = () => {
  const roleReuqest = localStorage.getItem("selectedRole");
  loginWithGoogleByRole(roleReuqest);
};

const handleLogin = async () => {
  try {
    const response = await LoginFromCustomer(authenticationRequest.value);
    const token = response.data.token;

    if (token) {
      localStorage.setItem('authToken', token);
      const decodedToken = jwtDecode(token);
      const userRole = decodedToken.scope;
      const expiryTime = decodedToken.exp;
      const currentTime = Date.now() / 1000;

      if (expiryTime < currentTime) {
        localStorage.clear();
        router.push('/401unauthenticated'); // Chuyển hướng nếu token hết hạn
      } else {
        // Phân luồng dựa trên vai trò trong token
        if (userRole.includes('ROLE_KHACHHANG') || userRole.includes('ROLE_KHACHHANG')) {
          // phát ra sự kiện để thay đổi header
           window.dispatchEvent(new CustomEvent('auth-change'));
          router.push('/');
        } else {
           // Có thể thêm các điều kiện khác cho các vai trò khác ở đây
          router.push('/login'); // Hoặc trang dashboard mặc định
        }
      }
    } else {
      console.error('Không tìm thấy token trong phản hồi.');
      router.push('/403unauthorize');
    }
  } catch (error) {
    console.error("Đăng nhập thất bại:", error);
    alert("Đăng nhập thất bại! Vui lòng kiểm tra lại Số điện thoại và Mật khẩu.");
  }
}
</script>

<style scoped>
/* FontAwesome CDN for icons */
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css");

.main-content {
  background-color: #f8f9fa;
}
.card {
  background: #ffffff;
  border: none;
  border-radius: 1rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.15);
}

.card-header {
  background: transparent;
}

.btn {
  border-radius: 0.5rem;
  transition: all 0.3s ease;
  font-weight: 600;
  text-transform: none;
}
.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.btn-outline-primary {
  border: 1px solid #dee2e6;
  color: #495057;
  background: #fff;
}
.btn-outline-primary:hover {
  background-color: #f8f9fa;
  border-color: #ced4da;
  color: #212529;
}

.form-check-label {
  cursor: pointer;
}

.card-body {
  padding: 2rem;
}

.gradient-text {
  background: linear-gradient(310deg, #2dce89 0%, #2dcecc 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-fill-color: transparent;
}

/* Responsive adjustments */
@media (max-width: 767.98px) {
  .row.align-items-center {
    flex-direction: column;
    justify-content: center;
  }
}

@media (max-width: 900px) {
  .login-card {
    flex-direction: column;
    min-height: 0;
    max-width: 420px;
  }

  .login-side-image {
    min-height: 160px;
    border-radius: 24px 24px 0 0;
    align-items: flex-end;
    justify-content: flex-start;
  }

  .quote-box {
    border-radius: 0 0 24px 24px;
    padding: 18px 18px 12px 18px;
  }

  .login-form-box {
    padding: 32px 18px 24px 18px;
  }
}
</style>
