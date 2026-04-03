<template>
  <footer class="footer">
    <!-- Newsletter Section -->
    <div class="newsletter-section">
      <div class="container">
        <div class="newsletter-content">
          <div class="newsletter-image">
            <img src="../assets/slide3.jpg" alt="Newsletter Promotion" class="img-fluid rounded shadow-lg">
          </div>
          <div class="newsletter-form-wrapper">
            <div class="newsletter-text">
              <h3>Nhận thông tin khuyến mãi mới nhất</h3>
              <p>Đăng ký để nhận ưu đãi và thông tin sản phẩm mới</p>
            </div>
            <form @submit.prevent="handleSubscribe" class="newsletter-form">
              <div class="input-group">
                <input 
                  type="email" 
                  v-model="email"
                  placeholder="Nhập email của bạn..."
                  class="newsletter-input"
                  required
                >
                <button type="submit" class="newsletter-btn">Đăng ký</button>
              </div>
              <div class="form-check mt-2">
                <input type="checkbox" class="form-check-input" id="consent">
                <label class="form-check-label text-white" for="consent">Tôi đồng ý nhận email từ Polo Haven</label>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Footer -->
    <div class="main-footer">
      <div class="container">
        <div class="footer-grid">
          <!-- Company Info -->
          <div class="footer-column">
            <div class="footer-logo">
              <span class="footer-logo-text">Polo Haven</span>
            </div>
            <p class="footer-description">
              Chuyên cung cấp áo polo chất lượng cao, thiết kế hiện đại, phù hợp với mọi phong cách.
            </p>
            <div class="footer-social">
              <a href="#" class="social-icon"><i class="bi bi-facebook"></i></a>
              <a href="#" class="social-icon"><i class="bi bi-instagram"></i></a>
              <a href="#" class="social-icon"><i class="bi bi-twitter"></i></a>
              <a href="#" class="social-icon"><i class="bi bi-tiktok"></i></a>
            </div>
          </div>

          <!-- Quick Links -->
          <div class="footer-column">
            <h4 class="footer-title">Liên kết nhanh</h4>
            <ul class="footer-links">
              <li><router-link to="/" class="footer-link">Trang chủ</router-link></li>
              <li><router-link to="/products" class="footer-link">Sản phẩm</router-link></li>
              <li><router-link to="/about" class="footer-link">Giới thiệu</router-link></li>
              <li><router-link to="/news" class="footer-link">Tin tức</router-link></li>
              <li><router-link to="/contact" class="footer-link">Liên hệ</router-link></li>
            </ul>
          </div>

          <!-- Product Categories -->
          <div class="footer-column">
            <h4 class="footer-title">Danh mục sản phẩm</h4>
            <ul class="footer-links">
              <li><router-link to="/products/polo" class="footer-link">Áo Polo</router-link></li>
              <li><router-link to="/products/pants" class="footer-link">Quần</router-link></li>
              <li><router-link to="/products/accessories" class="footer-link">Phụ kiện</router-link></li>
            </ul>
          </div>

          <!-- Customer Service -->
          <div class="footer-column">
            <h4 class="footer-title">Hỗ trợ khách hàng</h4>
            <ul class="footer-links">
              <li><a href="#" class="footer-link">Hướng dẫn mua hàng</a></li>
              <li><a href="#" class="footer-link">Chính sách đổi trả</a></li>
              <li><a href="#" class="footer-link">Phương thức thanh toán</a></li>
              <li><a href="#" class="footer-link">Giao hàng & vận chuyển</a></li>
            </ul>
          </div>

          <!-- Contact Info -->
          <div class="footer-column">
            <h4 class="footer-title">Thông tin liên hệ</h4>
            <div class="contact-info">
              <div class="contact-item">
                <i class="bi bi-geo-alt-fill"></i>
                <span>123 Đường ABC, Quận XYZ, TP.HCM</span>
              </div>
              <div class="contact-item">
                <i class="bi bi-telephone-fill"></i>
                <span>0123 456 789</span>
              </div>
              <div class="contact-item">
                <i class="bi bi-envelope-fill"></i>
                <span>info@polohaven.com</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom Footer -->
    <div class="bottom-footer">
      <div class="container">
        <div class="bottom-content">
          <div class="copyright">
            <p>&copy; 2025 Polo Haven. Tất cả quyền được bảo lưu.</p>
          </div>
          <div class="footer-policies">
            <a href="#" class="policy-link">Chính sách bảo mật</a>
            <a href="#" class="policy-link">Điều khoản sử dụng</a>
            <a href="#" class="policy-link">Sitemap</a>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { ref } from 'vue'

const email = ref('')

const handleSubscribe = () => {
  if (email.value) {
    console.log('Subscribing email:', email.value)
    alert('Đăng ký thành công!')
    email.value = ''
  }
}
</script>

<style scoped>
.footer {
  background: #fff;
  color: #333;
  margin-top: 80px;
}

.newsletter-section {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  padding: 60px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.newsletter-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  align-items: center;
}

.newsletter-image {
  text-align: center;
}

.newsletter-image img {
  max-width: 100%;
  height: auto;
  border-radius: 10px;
}

.newsletter-form-wrapper {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.newsletter-text h3 {
  font-size: 28px;
  margin-bottom: 10px;
  font-weight: 600;
  color: #fff;
}

.newsletter-text p {
  font-size: 16px;
  opacity: 0.9;
  color: #fff;
  margin: 0;
}

.newsletter-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.input-group {
  display: flex;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 25px;
  overflow: hidden;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.newsletter-input {
  flex: 1;
  padding: 15px 20px;
  background: transparent;
  border: none;
  color: #fff;
  font-size: 16px;
  outline: none;
}

.newsletter-input::placeholder {
  color: rgba(255, 255, 255, 0.8);
}

.newsletter-btn {
  background: #4ba27b;
  border: none;
  color: #fff;
  padding: 15px 25px;
  border-radius: 25px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
}

.newsletter-btn:hover {
  background: #3a7f5e;
  transform: translateY(-2px);
}

.form-check-label {
  font-size: 14px;
  color: #fff;
}

.main-footer {
  padding: 60px 0 40px;
  background: #f8f9fa;
}

.footer-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1.5fr;
  gap: 40px;
}

.footer-column {
  display: flex;
  flex-direction: column;
}

.footer-logo-text {
  font-size: 24px;
  font-weight: bold;
  color: #4ba27b;
}

.footer-description {
  color: #555;
  line-height: 1.6;
  margin-bottom: 25px;
}

.footer-social {
  display: flex;
  gap: 15px;
}

.social-icon {
  width: 40px;
  height: 40px;
  background: #e9ecef;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #333;
  text-decoration: none;
  opacity: 0.7;
  transition: all 0.3s ease;
}

.social-icon:hover {
  background: #4ba27b;
  color: #fff;
  opacity: 1;
  transform: scale(1.1);
}

.footer-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #333;
}

.footer-links {
  list-style: none;
  margin: 0;
  padding: 0;
}

.footer-links li {
  margin-bottom: 12px;
}

.footer-link {
  color: #555;
  text-decoration: none;
  transition: color 0.3s ease;
}

.footer-link:hover {
  color: #4ba27b;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #555;
}

.contact-item i {
  width: 20px;
  font-size: 16px;
  color: #4ba27b;
}

.bottom-footer {
  background: #fff;
  padding: 25px 0;
  border-top: 1px solid #eee;
}

.bottom-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.copyright {
  color: #666;
  font-size: 14px;
}

.footer-policies {
  display: flex;
  gap: 20px;
}

.policy-link {
  color: #666;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s ease;
}

.policy-link:hover {
  color: #4ba27b;
}

@media (max-width: 1024px) {
  .footer-grid {
    grid-template-columns: 1fr 1fr 1fr;
    gap: 30px;
  }
  .newsletter-content {
    grid-template-columns: 1fr;
    text-align: center;
    gap: 30px;
  }
  .newsletter-image {
    order: 2;
  }
  .newsletter-form-wrapper {
    order: 1;
  }
}

@media (max-width: 768px) {
  .footer-grid {
    grid-template-columns: 1fr;
    gap: 40px;
  }
  .newsletter-section {
    padding: 40px 0;
  }
  .newsletter-text h3 {
    font-size: 24px;
  }
  .newsletter-input {
    min-width: 200px;
  }
  .input-group {
    flex-direction: column;
    gap: 10px;
    padding: 10px;
  }
  .newsletter-btn {
    align-self: stretch;
  }
  .bottom-content {
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }
  .footer-policies {
    order: 1;
  }
}
</style>