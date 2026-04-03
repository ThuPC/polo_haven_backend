<template>
  <div class="about-page">
    <!-- Hero Section -->
    <section class="hero-sections gradient text-white text-center py-5">
      <div class="container">
        <h1 class="display-3 fw-bold animate__animated animate__fadeIn">Về Chúng Tôi</h1>
        <p class="lead fs-4 mt-3 animate__animated animate__fadeInUp">Khám phá câu chuyện và sứ mệnh của thương hiệu thời trang hàng đầu</p>
      </div>
    </section>

    <!-- About Story Section -->
    <section class="about-story-section py-5">
      <div class="container">
        <div class="row align-items-center g-4">
          <div class="col-12 col-md-6">
            <img src="/src/assets/img/havenlogo.png" alt="About Story" class="img-fluid rounded shadow-sm animate__animated animate__zoomIn" style="height: 400px; object-fit: cover;" @error="onImageError">
          </div>
          <div class="col-12 col-md-6">
            <div class="about-content">
              <h2 class="section-title fs-2 fw-bold mb-3">Câu Chuyện Của Chúng Tôi</h2>
              <p class="text-muted fs-5">Thành lập vào năm 2015, chúng tôi bắt đầu với niềm đam mê mang đến những bộ trang phục chất lượng cao và phong cách độc đáo. Từ một cửa hàng nhỏ tại Hà Nội, chúng tôi đã phát triển thành thương hiệu thời trang được yêu thích trên toàn quốc, cam kết mang lại sự tự tin và phong cách cho mọi khách hàng.</p>
              <p class="text-muted fs-5">Với sự sáng tạo không ngừng và tập trung vào xu hướng mới, chúng tôi tự hào là người bạn đồng hành trong hành trình thời trang của bạn.</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Mission & Values Section -->
    <section class="mission-values-section py-5 bg-light">
      <div class="container">
        <div class="section-header text-center mb-5 animate__animated animate__fadeIn">
          <h2 class="section-title display-5 fw-bold">Sứ Mệnh & Giá Trị</h2>
          <p class="section-subtitle text-muted fs-5">Những gì định hình chúng tôi</p>
        </div>
        <div class="row g-4">
          <div class="col-12 col-md-4">
            <div class="mission-card p-4 text-center bg-white rounded shadow-sm h-100 animate__animated animate__fadeInUp">
              <div class="mission-icon mb-3 rounded-circle bg-success text-white" style="width: 70px; height: 70px; font-size: 30px; display: flex; align-items: center; justify-content: center;">
                <i class="bi bi-heart"></i>
              </div>
              <h3 class="fs-5 fw-semibold mb-2">Chất Lượng</h3>
              <p class="text-muted">Cam kết sử dụng chất liệu cao cấp và quy trình sản xuất bền vững.</p>
            </div>
          </div>
          <div class="col-12 col-md-4">
            <div class="mission-card p-4 text-center bg-white rounded shadow-sm h-100 animate__animated animate__fadeInUp" style="animation-delay: 0.2s;">
              <div class="mission-icon mb-3 rounded-circle bg-success text-white" style="width: 70px; height: 70px; font-size: 30px; display: flex; align-items: center; justify-content: center;">
                <i class="bi bi-people"></i>
              </div>
              <h3 class="fs-5 fw-semibold mb-2">Khách Hàng</h3>
              <p class="text-muted">Luôn đặt trải nghiệm khách hàng lên hàng đầu.</p>
            </div>
          </div>
          <div class="col-12 col-md-4">
            <div class="mission-card p-4 text-center bg-white rounded shadow-sm h-100 animate__animated animate__fadeInUp" style="animation-delay: 0.4s;">
              <div class="mission-icon mb-3 rounded-circle bg-success text-white" style="width: 70px; height: 70px; font-size: 30px; display: flex; align-items: center; justify-content: center;">
                <i class="bi bi-gear"></i>
              </div>
              <h3 class="fs-5 fw-semibold mb-2">Đổi Mới</h3>
              <p class="text-muted">Luôn tiên phong trong các xu hướng thời trang mới.</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Team Section -->
    <section class="team-section py-5">
      <div class="container">
        <div class="section-header text-center mb-5 animate__animated animate__fadeIn">
          <h2 class="section-title display-5 fw-bold">Đội Ngũ Của Chúng Tôi</h2>
          <p class="section-subtitle text-muted fs-5">Những con người đằng sau thành công</p>
        </div>
        <div class="row g-4">
          <div v-for="member in teamMembers" :key="member.id" class="col-12 col-md-3">
            <div class="team-card p-3 text-center bg-white rounded shadow-sm animate__animated animate__fadeInUp">
              <img :src="member.image" :alt="member.name" class="img-fluid rounded-circle mb-3" style="width: 150px; height: 150px; object-fit: cover;" @error="onImageError">
              <h3 class="fs-6 fw-semibold">{{ member.name }}</h3>
              <p class="text-muted mb-1">{{ member.role }}</p>
              <p class="text-muted small">{{ member.bio }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section py-5 gradient text-white text-center">
      <div class="container">
        <h2 class="cta-title display-4 fw-bold">Khám Phá Bộ Sưu Tập Của Chúng Tôi</h2>
        <p class="cta-subtitle fs-4 mb-4">Tham gia ngay để nhận ưu đãi đặc biệt!</p>
        <router-link to="/shop" class="btn btn-light btn-lg rounded-pill">Mua sắm ngay</router-link>
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

const teamMembers = ref([
  { id: 1, name: 'Nguyễn Văn A', role: 'Giám đốc', bio: 'Người sáng lập với hơn 10 năm kinh nghiệm trong ngành thời trang.', image: '/src/assets/ao8.webp' },
  { id: 2, name: 'Trần Thị B', role: 'Thiết kế trưởng', bio: 'Chuyên gia tạo ra các xu hướng thời trang mới mẻ.', image: '/src/assets/ao6.webp' },
  { id: 3, name: 'Lê Văn C', role: 'Quản lý sản xuất', bio: 'Đảm bảo chất lượng cho mọi sản phẩm.', image: '/src/assets/ao7.webp' },
  { id: 4, name: 'Phạm Thị D', role: 'Marketing', bio: 'Xây dựng thương hiệu trên toàn quốc.', image: '/src/assets/ao9.webp' }
])

const showBackToTop = ref(false)

const handleScroll = () => {
  showBackToTop.value = window.scrollY > 300;
};

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

const onImageError = (event) => {
  event.target.src = '/src/assets/placeholder.jpg'; // Hình ảnh dự phòng nếu không tải được
};

onMounted(() => {
  window.addEventListener('scroll', handleScroll);
});

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll);
});
</script>

<style scoped>
@import 'animate.css';

.about-page {
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
  color: black;
  font-size: 3.5rem;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.hero-section .lead {
  color: black;
  font-size: 1.5rem;
  opacity: 0.9;
}

/* About Story Section */
.about-story-section .about-content {
  padding: 2rem;
}

.about-story-section h2 {
  color: #333;
  font-size: 2.2rem;
}

.about-story-section p {
  line-height: 1.8;
}

/* Mission & Values Section */
.mission-values-section .section-header {
  margin-bottom: 3rem;
}

.mission-values-section .mission-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.mission-values-section .mission-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.15) !important;
}

.mission-values-section .mission-icon {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
}

/* Team Section */
.team-section .section-header {
  margin-bottom: 3rem;
}

.team-section .team-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.team-section .team-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.15) !important;
}

/* CTA Section */
.cta-section {
  padding: 5rem 0;
}

.cta-section .cta-title {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.cta-section .cta-subtitle {
  font-size: 1.5rem;
  margin-bottom: 2.5rem;
  opacity: 0.9;
}

.cta-section .btn {
  padding: 15px 40px;
  font-weight: 600;
  font-size: 1.1rem;
}

.cta-section .btn-light {
  color: #4ba27b;
}

.cta-section .btn-light:hover {
  background-color: #e2e6ea;
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
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
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

  .about-story-section .row {
    flex-direction: column;
  }

  .about-story-section .about-content {
    padding: 1rem;
  }

  .mission-values-section .section-title {
    font-size: 2rem;
  }

  .mission-values-section .section-subtitle {
    font-size: 1rem;
  }

  .team-section .section-title {
    font-size: 2rem;
  }

  .team-section .section-subtitle {
    font-size: 1rem;
  }

  .cta-section .cta-title {
    font-size: 2rem;
  }

  .cta-section .cta-subtitle {
    font-size: 1.2rem;
  }
}

@media (max-width: 576px) {
  .hero-section {
    padding: 3rem 0;
  }

  .about-story-section img {
    height: 300px;
  }

  .mission-values-section .col-md-4 {
    flex: 0 0 100%;
    max-width: 100%;
  }
}
</style>