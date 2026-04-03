<template>
  <div class="news-page">
    <!-- Hero Section (News Carousel) -->
    <section class="hero-section">
      <div id="newsCarousel" class="carousel slide">
        <div class="carousel-indicators">
          <button type="button" data-bs-target="#newsCarousel" data-bs-slide-to="0" class="active" aria-current="true" aria-label="Slide 1"></button>
          <button type="button" data-bs-target="#newsCarousel" data-bs-slide-to="1" aria-label="Slide 2"></button>
          <button type="button" data-bs-target="#newsCarousel" data-bs-slide-to="2" aria-label="Slide 3"></button>
        </div>
        <div class="carousel-inner">
          <div class="carousel-item active">
            <img src="/src/assets/slide2.png" class="d-block w-100" alt="News 1" style="height: 400px; object-fit: cover;">
            <div class="carousel-caption d-none d-md-block">
              <h5>Giảm giá 30% toàn bộ áo polo</h5>
              <p>Ưu đãi đặc biệt từ 15/07 đến 20/07/2025!</p>
            </div>
          </div>
          <div class="carousel-item">
            <img src="/src/assets/slide3.jpg" class="d-block w-100" alt="News 2" style="height: 400px; object-fit: cover;">
            <div class="carousel-caption d-none d-md-block">
              <h5>Bộ sưu tập mùa hè 2025</h5>
              <p>Khám phá các mẫu áo mới với giá ưu đãi.</p>
            </div>
          </div>
          <div class="carousel-item">
            <img src="/src/assets/slide2.png" class="d-block w-100" alt="News 3" style="height: 400px; object-fit: cover;">
            <div class="carousel-caption d-none d-md-block">
              <h5>Miễn phí vận chuyển</h5>
              <p>Đơn từ 500k, áp dụng đến hết tháng 7.</p>
            </div>
          </div>
        </div>
        <button class="carousel-control-prev" type="button" data-bs-target="#newsCarousel" data-bs-slide="prev">
          <span class="carousel-control-prev-icon" aria-hidden="true"></span>
          <span class="visually-hidden">Previous</span>
        </button>
        <button class="carousel-control-next" type="button" data-bs-target="#newsCarousel" data-bs-slide="next">
          <span class="carousel-control-next-icon" aria-hidden="true"></span>
          <span class="visually-hidden">Next</span>
        </button>
      </div>
    </section>

    <!-- Featured News Section -->
    <section class="featured-news-section py-5">
      <div class="container">
        <div class="section-header text-center mb-5 animate__animated animate__fadeIn">
          <h2 class="section-title display-5 fw-bold">Tin tức nổi bật</h2>
          <p class="section-subtitle text-muted fs-5">Cập nhật ưu đãi và xu hướng thời trang</p>
        </div>
        <div id="featuredNewsCarousel" class="carousel slide">
          <div class="carousel-inner">
            <div v-for="(chunk, chunkIndex) in chunkedFeaturedNews" :key="chunkIndex" class="carousel-item" :class="{ active: chunkIndex === 0 }">
              <div class="row g-4">
                <div v-for="news in chunk" :key="news.id" class="col-12 col-sm-6 col-md-3">
                  <div class="news-card card h-100 shadow-sm cursor-pointer animate__animated animate__zoomIn" @click="navigateToNews(news.slug)">
                    <div class="news-image position-relative overflow-hidden" style="height: 200px;">
                      <img :src="news.image" :alt="news.title" class="card-img-top object-fit-cover" />
                      <div class="news-category position-absolute top-0 start-0 bg-success text-white px-3 py-1 rounded-end">{{ news.category }}</div>
                    </div>
                    <div class="news-info card-body">
                      <h3 class="news-title card-title fs-6 fw-semibold">{{ news.title }}</h3>
                      <p class="news-excerpt text-muted">{{ news.excerpt }}</p>
                      <div class="news-meta text-muted small">
                        <span><i class="bi bi-calendar-event me-1"></i>{{ formatDate(news.date) }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <button class="carousel-control-prev custom-prev" type="button" data-bs-target="#featuredNewsCarousel" data-bs-slide="prev">
            <span class="carousel-control-prev-icon" aria-hidden="true"></span>
            <span class="visually-hidden">Previous</span>
          </button>
          <button class="carousel-control-next custom-next" type="button" data-bs-target="#featuredNewsCarousel" data-bs-slide="next">
            <span class="carousel-control-next-icon" aria-hidden="true"></span>
            <span class="visually-hidden">Next</span>
          </button>
        </div>
        <div class="section-actions text-center mt-4 animate__animated animate__fadeInUp">
          <router-link to="/news/all" class="btn btn-outline-success">Xem tất cả tin tức</router-link>
        </div>
      </div>
    </section>

    <!-- Sidebar Section -->
    <section class="sidebar-section py-5">
      <div class="container">
        <div class="row g-3">
          <div class="col-12 col-md-8">
            <!-- More News Articles -->
            <div v-for="news in moreNews" :key="news.id" class="news-article p-4 bg-white rounded-lg shadow-sm mb-3 animate__animated animate__fadeInUp">
              <img :src="news.image" :alt="news.title" class="img-fluid rounded-top mb-3" style="height: 200px; object-fit: cover;">
              <h3 class="fs-5 fw-bold mb-2">{{ news.title }}</h3>
              <p class="text-muted">{{ news.excerpt }}</p>
              <router-link :to="'/news/' + news.slug" class="text-decoration-none text-success fw-semibold">Đọc thêm</router-link>
            </div>
          </div>
          <div class="col-12 col-md-4">
            <div class="news-sidebar p-4 bg-white rounded-lg shadow-sm mb-3 animate__animated animate__fadeInUp">
              <h3 class="fs-5 fw-bold mb-3" style="color: #4ba27b;"><i class="bi bi-list"></i> Tin nổi bật</h3>
              <ul class="list-unstyled">
                <li v-for="highlight in highlights" :key="highlight.id" class="mb-2">
                  <router-link :to="'/news/' + highlight.slug" class="text-muted text-decoration-none">{{ highlight.title }}</router-link>
                </li>
              </ul>
            </div>
            <div class="news-sidebar p-4 bg-white rounded-lg shadow-sm animate__animated animate__fadeInUp">
              <h3 class="fs-5 fw-bold mb-3" style="color: #4ba27b;"><i class="bi bi-bullhorn"></i> Quảng cáo</h3>
              <img src="/src/assets/slide3.jpg" alt="Ad Banner" class="img-fluid rounded" style="height: 200px; object-fit: cover;">
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section py-5 bg-gradient animate__animated animate__fadeIn">
      <div class="container">
        <div class="cta-content text-center">
          <h2 class="cta-title display-4 fw-bold">Cập nhật ưu đãi mỗi ngày</h2>
          <p class="cta-subtitle fs-4 mb-4">Đừng bỏ lỡ những đợt giảm giá hấp dẫn từ chúng tôi</p>
          <div class="cta-actions d-flex justify-content-center gap-3">
            <router-link to="/news/subscribe" class="btn btn-light btn-lg rounded-pill">Đăng ký nhận tin</router-link>
            <router-link to="/news/all" class="btn btn-light btn-lg rounded-pill">Xem tất cả</router-link>
          </div>
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
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import * as bootstrap from 'bootstrap'
import 'animate.css'

const featuredNews = ref([
  { id: 1, title: 'Giảm giá 30% toàn bộ áo polo', excerpt: 'Ưu đãi đặc biệt từ 15/07 đến 20/07/2025.', image: '/src/assets/ao1.webp', category: 'Ưu đãi', date: '2025-07-15', slug: 'giam-gia-30-ao-polo' },
  { id: 2, title: 'Bộ sưu tập mùa hè 2025', excerpt: 'Khám phá các mẫu áo mới với giá ưu đãi đặc biệt.', image: '/src/assets/ao2.webp', category: 'Mùa hè', date: '2025-07-14', slug: 'bo-suu-tap-mua-he-2025' },
  { id: 3, title: 'Miễn phí vận chuyển toàn quốc', excerpt: 'Áp dụng cho đơn từ 500k đến hết tháng 7.', image: '/src/assets/ao3.webp', category: 'Ưu đãi', date: '2025-07-13', slug: 'mien-phi-van-chuyen' },
  { id: 4, title: 'Giảm 50% áo thun nam', excerpt: 'Chương trình giảm giá sốc cuối tuần này.', image: '/src/assets/ao4.webp', category: 'Ưu đãi', date: '2025-07-12', slug: 'giam-50-ao-thun-nam' },
  { id: 5, title: 'Xu hướng áo sơ mi 2025', excerpt: 'Những mẫu áo sơ mi hot nhất mùa này.', image: '/src/assets/ao5.webp', category: 'Thời trang', date: '2025-07-11', slug: 'xu-huong-ao-so-mi-2025' },
  { id: 6, title: 'Quà tặng khi mua sắm', excerpt: 'Nhận quà miễn phí với đơn từ 1 triệu.', image: '/src/assets/ao6.webp', category: 'Ưu đãi', date: '2025-07-10', slug: 'qua-tang-khi-mua-sam' },
  { id: 7, title: 'Áo khoác mùa đông sớm', excerpt: 'Chuẩn bị cho mùa đông với giá ưu đãi.', image: '/src/assets/ao7.webp', category: 'Mùa đông', date: '2025-07-09', slug: 'ao-khoac-mua-dong' },
  { id: 8, title: 'Sale cuối tháng 7', excerpt: 'Giảm giá lên đến 40% cho tất cả sản phẩm.', image: '/src/assets/ao8.webp', category: 'Ưu đãi', date: '2025-07-08', slug: 'sale-cuoi-thang-7' }
])


const moreNews = ref([
  { id: 1, title: 'Áo polo giảm sốc', excerpt: 'Giảm 35% cho toàn bộ áo polo nam nữ.', image: '/src/assets/slide 2 banner.webp', slug: 'ao-polo-giam-soc' },
  { id: 2, title: 'Xu hướng thời trang thu đông', excerpt: 'Các mẫu áo mới cho mùa thu đông 2025.', image: '/src/assets/slide3.jpg', slug: 'xu-huong-thoi-trang-thu-dong' },
  { id: 3, title: 'Mua 2 tặng 1 áo thun', excerpt: 'Chương trình khuyến mãi hấp dẫn cuối tháng.', image: '/src/assets/slide 1.webp', slug: 'mua-2-tang-1-ao-thun' }
])

const highlights = ref([
  { id: 1, title: 'Giảm giá 30% áo polo', slug: 'giam-gia-30-ao-polo' },
  { id: 2, title: 'Miễn phí vận chuyển', slug: 'mien-phi-van-chuyen' },
  { id: 3, title: 'Sale cuối tháng 7', slug: 'sale-cuoi-thang-7' }
])

const chunkedFeaturedNews = computed(() => {
  const chunkSize = 4;
  const chunks = [];
  for (let i = 0; i < featuredNews.value.length; i += chunkSize) {
    chunks.push(featuredNews.value.slice(i, i + chunkSize));
  }
  return chunks;
});

const showBackToTop = ref(false);

const handleScroll = () => {
  showBackToTop.value = window.scrollY > 300;
};

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

const navigateToNews = (slug) => {
  console.log('Navigate to news:', slug);
};

const formatDate = (date) => {
  return new Date(date).toLocaleDateString('vi-VN', { year: 'numeric', month: 'long', day: 'numeric' });
};

onMounted(() => {
  const newsCarouselElement = document.getElementById('newsCarousel');
  if (newsCarouselElement) {
    new bootstrap.Carousel(newsCarouselElement, { interval: 5000, wrap: true });
  }

  const featuredNewsCarouselElement = document.getElementById('featuredNewsCarousel');
  if (featuredNewsCarouselElement) {
    new bootstrap.Carousel(featuredNewsCarouselElement, { interval: false, wrap: false });
  }

  window.addEventListener('scroll', handleScroll);
});

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll);
});
</script>

<style scoped>
@import 'animate.css';

.news-page {
  padding-top: 20px;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.section-header {
  margin-bottom: 3rem;
}

.section-title {
  font-size: 2.8rem;
  font-weight: 700;
  color: #333;
}

.section-subtitle {
  font-size: 1.2rem;
  color: #666;
}

.bg-gradient {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
}

.text-success {
  color: #4ba27b !important;
}

/* Hero Section */
.hero-section {
  position: relative;
  height: 70vh;
  min-height: 500px;
  overflow: hidden;
}

#newsCarousel {
  height: 100%;
}

.carousel-item {
  height: 100%;
}

.carousel-item img {
  height: 100%;
  object-fit: cover;
}

.carousel-caption {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  padding-left: 15%;
  padding-right: 15%;
  color: #fff;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
  z-index: 2;
}

.carousel-caption h5 {
  font-size: 3.5rem;
  font-weight: 700;
  margin-bottom: 20px;
  line-height: 1.2;
}

.carousel-caption p {
  font-size: 1.3rem;
  margin-bottom: 30px;
  opacity: 0.9;
  line-height: 1.6;
}

/* Featured News Section */
#featuredNewsCarousel .carousel-control-prev.custom-prev,
#featuredNewsCarousel .carousel-control-next.custom-next {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  border-radius: 50%;
  top: 50%;
  transform: translateY(-50%);
  opacity: 1;
  transition: background-color 0.3s ease, transform 0.3s ease;
  z-index: 10;
}

#featuredNewsCarousel .carousel-control-prev.custom-prev:hover,
#featuredNewsCarousel .carousel-control-next.custom-next:hover {
  background-color: #66ea8b;
  transform: translateY(-50%) scale(1.1);
}

#featuredNewsCarousel .carousel-control-prev.custom-prev {
  left: 0;
  margin-left: -60px;
}

#featuredNewsCarousel .carousel-control-next.custom-next {
  right: 0;
  margin-right: -60px;
}

#featuredNewsCarousel .carousel-control-prev.custom-prev .carousel-control-prev-icon,
#featuredNewsCarousel .carousel-control-next.custom-next .carousel-control-next-icon {
  background-image: none;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: white;
}

#featuredNewsCarousel .carousel-control-prev.custom-prev .carousel-control-prev-icon::before {
  content: "\f0d9";
  font-family: "Font Awesome 5 Free";
  font-weight: 900;
}

#featuredNewsCarousel .carousel-control-next.custom-next .carousel-control-next-icon::before {
  content: "\f0da";
  font-family: "Font Awesome 5 Free";
  font-weight: 900;
}

.featured-news-section .news-card {
  border: none;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  min-height: 300px;
}

.news-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1) !important;
}

.news-image {
  height: 200px;
  overflow: hidden;
  position: relative;
}

.news-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.news-card:hover .news-image img {
  transform: scale(1.05);
}

.news-category {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  color: white;
  padding: 5px 15px;
  border-bottom-right-radius: 10px;
  font-size: 0.9rem;
  font-weight: 600;
}

.news-info {
  padding: 1.5rem;
}

.news-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #333;
}

.news-excerpt {
  font-size: 0.95rem;
  color: #666;
  line-height: 1.6;
}

.news-meta span i {
  color: #4ba27b;
  margin-right: 5px;
}

/* Sidebar Section */
.news-sidebar {
  border-radius: 10px;
}

.news-article img {
  object-fit: cover;
}

/* CTA Section */
.cta-section {
  color: black;
  padding: 5rem 0;
  text-align: center;
}

.cta-title {
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 1rem;
}

.cta-subtitle {
  font-size: 1.5rem;
  margin-bottom: 2.5rem;
  opacity: 0.9;
}

.cta-actions .btn {
  padding: 15px 40px;
  border-radius: 50px;
  font-weight: 600;
  font-size: 1.1rem;
}

.cta-actions .btn-light {
  color: #4ba27b;
}

.cta-actions .btn-light:hover {
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
@media (max-width: 992px) {
  .carousel-caption {
    padding-left: 10%;
    padding-right: 10%;
  }

  #featuredNewsCarousel .carousel-control-prev.custom-prev,
  #featuredNewsCarousel .carousel-control-next.custom-next {
    width: 40px;
    height: 40px;
  }

  #featuredNewsCarousel .carousel-control-prev.custom-prev {
    margin-left: -50px;
  }

  #featuredNewsCarousel .carousel-control-next.custom-next {
    margin-right: -50px;
  }
}

@media (max-width: 768px) {
  .news-page {
    padding-top: 100px;
  }

  .hero-section {
    height: 60vh;
    min-height: 400px;
  }

  .carousel-caption h5 {
    font-size: 2.2rem;
  }

  .carousel-caption p {
    font-size: 1rem;
  }

  .section-title {
    font-size: 2.2rem;
  }

  .section-subtitle {
    font-size: 1rem;
  }

  .cta-title {
    font-size: 2.5rem;
  }

  .cta-subtitle {
    font-size: 1.2rem;
  }

  .cta-actions {
    flex-direction: column;
    gap: 15px;
  }

  #featuredNewsCarousel .carousel-control-prev.custom-prev,
  #featuredNewsCarousel .carousel-control-next.custom-next {
    width: 30px;
    height: 30px;
    font-size: 1.2rem;
  }

  #featuredNewsCarousel .carousel-control-prev.custom-prev {
    margin-left: -40px;
  }

  #featuredNewsCarousel .carousel-control-next.custom-next {
    margin-right: -40px;
  }
}

@media (max-width: 576px) {
  .hero-section {
    min-height: 350px;
  }

  .carousel-caption h5 {
    font-size: 1.8rem;
  }

  .carousel-caption p {
    font-size: 0.9rem;
  }

  .news-image {
    height: 150px;
  }

  #featuredNewsCarousel .carousel-control-prev.custom-prev,
  #featuredNewsCarousel .carousel-control-next.custom-next {
    display: none;
  }
}
</style>