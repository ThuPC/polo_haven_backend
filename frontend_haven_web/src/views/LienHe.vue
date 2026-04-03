<template>
  <div class="contact-page">
    <!-- Hero Section -->
    <section class="hero-sections gradient text-white text-center py-5">
      <div class="container">
        <h1 class="display-3 fw-bold animate__animated animate__fadeIn">Liên Hệ Với Chúng Tôi</h1>
        <p class="lead fs-4 mt-3 animate__animated animate__fadeInUp">Chúng tôi luôn sẵn sàng hỗ trợ bạn</p>
      </div>
    </section>

    <!-- Contact Info Section -->
    <section class="contact-info-section py-5">
      <div class="container">
        <div class="row g-4">
          <div class="col-12 col-md-4">
            <div class="contact-card p-4 text-center bg-white rounded shadow-sm h-100 animate__animated animate__fadeInUp">
              <div class="contact-icon mb-3 rounded-circle bg-success text-white" style="width: 70px; height: 70px; font-size: 30px; display: flex; align-items: center; justify-content: center;">
                <i class="bi bi-geo-alt"></i>
              </div>
              <h3 class="fs-5 fw-semibold mb-2">Địa Chỉ</h3>
              <p class="text-muted">123 Đường Thời Trang, Quận Hoàn Kiếm, Hà Nội, Việt Nam</p>
            </div>
          </div>
          <div class="col-12 col-md-4">
            <div class="contact-card p-4 text-center bg-white rounded shadow-sm h-100 animate__animated animate__fadeInUp" style="animation-delay: 0.2s;">
              <div class="contact-icon mb-3 rounded-circle bg-success text-white" style="width: 70px; height: 70px; font-size: 30px; display: flex; align-items: center; justify-content: center;">
                <i class="bi bi-envelope"></i>
              </div>
              <h3 class="fs-5 fw-semibold mb-2">Email</h3>
              <p class="text-muted">support@fashionbrand.com</p>
            </div>
          </div>
          <div class="col-12 col-md-4">
            <div class="contact-card p-4 text-center bg-white rounded shadow-sm h-100 animate__animated animate__fadeInUp" style="animation-delay: 0.4s;">
              <div class="contact-icon mb-3 rounded-circle bg-success text-white" style="width: 70px; height: 70px; font-size: 30px; display: flex; align-items: center; justify-content: center;">
                <i class="bi bi-telephone"></i>
              </div>
              <h3 class="fs-5 fw-semibold mb-2">Điện Thoại</h3>
              <p class="text-muted">+84 123 456 789</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Contact Form Section -->
    <section class="contact-form-section py-5 bg-light">
      <div class="container">
        <div class="section-header text-center mb-5 animate__animated animate__fadeIn">
          <h2 class="section-title display-5 fw-bold">Gửi Thông Tin Cho Chúng Tôi</h2>
          <p class="section-subtitle text-muted fs-5">Hãy để lại tin nhắn, chúng tôi sẽ liên hệ ngay!</p>
        </div>
        <div class="row justify-content-center">
          <div class="col-12 col-md-8">
            <form @submit.prevent="submitForm" class="p-4 bg-white rounded shadow-sm animate__animated animate__fadeInUp">
              <div class="mb-3">
                <label for="name" class="form-label">Họ và Tên</label>
                <input type="text" v-model="formData.name" id="name" class="form-control" required>
              </div>
              <div class="mb-3">
                <label for="email" class="form-label">Email</label>
                <input type="email" v-model="formData.email" id="email" class="form-control" required>
              </div>
              <div class="mb-3">
                <label for="subject" class="form-label">Chủ Đề</label>
                <input type="text" v-model="formData.subject" id="subject" class="form-control" required>
              </div>
              <div class="mb-3">
                <label for="message" class="form-label">Tin Nhắn</label>
                <textarea v-model="formData.message" id="message" class="form-control" rows="5" required></textarea>
              </div>
              <button type="submit" class="btn btn-success w-100">Gửi Tin Nhắn</button>
            </form>
          </div>
        </div>
      </div>
    </section>

    <!-- Map Section -->
    <section class="map-section py-5">
      <div class="container">
        <div class="section-header text-center mb-5 animate__animated animate__fadeIn">
          <h2 class="section-title display-5 fw-bold">Vị Trí Của Chúng Tôi</h2>
          <p class="section-subtitle text-muted fs-5">Tìm chúng tôi trên bản đồ</p>
        </div>
        <div class="map-container">
          <iframe src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3724.123456!2d105.834159!3d21.027764!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x0%3A0x0!2zMjHCsDAxJzQzLjkiTiAxMDXCsDUwJzAzLjAiRQ!5e0!3m2!1svi!2s!4v1626345678!5m2!1svi!2s" width="100%" height="450" style="border:0;" allowfullscreen="" loading="lazy" class="rounded shadow-sm animate__animated animate__fadeIn"></iframe>
        </div>
      </div>
    </section>

    <!-- Back to Top Button -->
    <button v-if="showBackToTop" @click="scrollToTop" class="btn btn-success back-to-top animate__animated animate__fadeIn">
      <i class="bi bi-arrow-up"></i>
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as bootstrap from 'bootstrap'
import 'animate.css'

const formData = ref({
  name: '',
  email: '',
  subject: '',
  message: ''
})

const showBackToTop = ref(false)

const handleScroll = () => {
  showBackToTop.value = window.scrollY > 300;
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

const submitForm = () => {
  console.log('Form submitted:', formData.value);
  // Thêm logic gửi form (ví dụ: API call) tại đây
  alert('Tin nhắn của bạn đã được gửi! Chúng tôi sẽ liên hệ sớm.');
  formData.value = { name: '', email: '', subject: '', message: '' };
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll);
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll);
})
</script>

<style scoped>
@import 'animate.css';

.contact-page {
  padding-top: 20px;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.gradient {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
}

.text-success {
  color: #4ba27b !important;
}

/* Hero Section */
.hero-section {
  padding: 5rem 0;
}

.hero-section h1 {
  font-size: 3.5rem;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.hero-section .lead {
  font-size: 1.5rem;
  opacity: 0.9;
}

/* Contact Info Section */
.contact-info-section .contact-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.contact-info-section .contact-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.15) !important;
}

.contact-info-section .contact-icon {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
}

/* Contact Form Section */
.contact-form-section .section-header {
  margin-bottom: 3rem;
}

.contact-form-section form {
  border-radius: 10px;
}

.contact-form-section .form-label {
  font-weight: 500;
}

.contact-form-section .btn-success {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  border: none;
}

.contact-form-section .btn-success:hover {
  background: linear-gradient(135deg, #4ba27b 0%, #66ea8b 100%);
}

/* Map Section */
.map-section .section-header {
  margin-bottom: 3rem;
}

.map-container {
  border-radius: 10px;
  overflow: hidden;
}

/* Back to Top Button */
.back-to-top {
  position: fixed;
  bottom: 30px;
  right: 30px;
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  color: white;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
  z-index: 999;
}

.back-to-top:hover {
  background: linear-gradient(135deg, #4ba27b 0%, #66ea8b 100%);
  transform: translateY(-3px);
}

/* Responsive Adjustments */
@media (max-width: 768px) {
  .hero-section h1 {
    font-size: 2.5rem;
  }

  .hero-section .lead {
    font-size: 1.2rem;
  }

  .contact-info-section .col-md-4 {
    flex: 0 0 100%;
    max-width: 100%;
  }

  .contact-form-section .section-title {
    font-size: 2rem;
  }

  .contact-form-section .section-subtitle {
    font-size: 1rem;
  }

  .map-section .section-title {
    font-size: 2rem;
  }

  .map-section .section-subtitle {
    font-size: 1rem;
  }
}

@media (max-width: 576px) {
  .hero-section {
    padding: 3rem 0;
  }

  .contact-form-section form {
    padding: 2rem;
  }

  .map-container iframe {
    height: 300px;
  }
}
</style>