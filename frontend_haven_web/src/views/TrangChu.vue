```vue
<template>
  <div class="homepage">
    <!-- Hero Section (Bootstrap Carousel) -->
    <section class="hero-section">
      <div id="carouselExampleCaptions" class="carousel slide">
        <div class="carousel-indicators">
          <button type="button" data-bs-target="#carouselExampleCaptions" data-bs-slide-to="0" class="active"
                  aria-current="true" aria-label="Slide 1"></button>
          <button type="button" data-bs-target="#carouselExampleCaptions" data-bs-slide-to="1"
                  aria-label="Slide 2"></button>
          <button type="button" data-bs-target="#carouselExampleCaptions" data-bs-slide-to="2"
                  aria-label="Slide 3"></button>
        </div>
        <div class="carousel-inner">
          <div class="carousel-item active">
            <img src="../assets/slide3.jpg" class="d-block w-100" alt="...">
          </div>
          <div class="carousel-item">
            <img src="../assets/slide2.png" class="d-block w-100" alt="...">
          </div>
          <div class="carousel-item">
            <img src="../assets/slide3.jpg" class="d-block w-100" alt="...">
          </div>
        </div>
        <button class="carousel-control-prev" type="button" data-bs-target="#carouselExampleCaptions"
                data-bs-slide="prev">
          <span class="carousel-control-prev-icon" aria-hidden="true"></span>
          <span class="visually-hidden">Previous</span>
        </button>
        <button class="carousel-control-next" type="button" data-bs-target="#carouselExampleCaptions"
                data-bs-slide="next">
          <span class="carousel-control-next-icon" aria-hidden="true"></span>
          <span class="visually-hidden">Next</span>
        </button>
      </div>
    </section>

    <!-- Features Section -->
    <section class="features-section py-5 bg-light">
      <div class="container">
        <div class="row g-4">
          <div v-for="feature in features" :key="feature.id" class="col-12 col-md-6 col-lg-3">
            <div
                class="feature-card p-4 text-center bg-white rounded shadow-sm h-100 d-flex flex-column align-items-center justify-content-center animate__animated animate__fadeInUp">
              <div
                  class="feature-icon mb-3 d-flex align-items-center justify-content-center rounded-circle bg-success text-white"
                  style="width:70px; height:70px; font-size:30px;">
                <i :class="feature.icon"></i>
              </div>
              <h3 class="feature-title fs-5 fw-semibold mb-2">{{ feature.title }}</h3>
              <p class="feature-description text-muted">{{ feature.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- New Arrivals Section -->
    <section class="new-arrivals-section py-5">
      <div class="container">
        <div class="section-header text-center mb-5 animate__animated animate__fadeIn">
          <h2 class="section-title display-5 fw-bold">Sản phẩm mới về</h2>
          <p class="section-subtitle text-muted fs-5">Những mẫu áo polo vừa cập bến tại Polo Haven</p>
        </div>

        <div id="newArrivalsCarousel" class="carousel slide">
          <div class="carousel-inner">
            <div v-for="(chunk, chunkIndex) in chunkedNewArrivalProducts" :key="chunkIndex" class="carousel-item"
                 :class="{ active: chunkIndex === 0 }">
              <div class="row g-4">
                <div v-for="product in chunk" :key="product.id" class="col-12 col-sm-6 col-md-3">
                  <div class="product-card card h-100 shadow-sm cursor-pointer animate__animated animate__zoomIn"
                       @click="navigateToProduct(product.id)">
                    <div class="product-image position-relative overflow-hidden" style="height: 250px;">
                      <img v-if="product.imageList && product.imageList.length > 0" :src="product.imageList[0]"
                           :alt="product.tenSanPham" class="card-img-top object-fit-cover h-100 w-100" />
                      <img v-else src="" :alt="product.tenSanPham" class="card-img-top object-fit-cover h-100 w-100" />

                      <div
                          class="product-badge position-absolute top-0 start-0 bg-info text-white px-3 py-1 rounded-pill fs-6">
                        Mới
                      </div>
                    </div>
                    <div class="product-info card-body">
                      <h3 class="product-name card-title fs-6 fw-semibold">{{ product.tenSanPham }}
                        <span v-if="product.phanTramGiam > 0" class="text-danger fw-bold ms-2">
                          -{{ product.phanTramGiam }}%
                        </span>
                      </h3>
                      <div class="product-details mb-2">
                        <p class="mb-1"><small class="text-muted">Màu sắc:</small>
                          <span v-for="(color, cIdx) in product.mauSacList" :key="cIdx" class="color-dot"
                                :style="{ backgroundColor: color.maMauSac }"></span>
                        </p>
                        <!-- <p class="mb-0"><small class="text-muted">Size:</small> {{ product.kichThuocList.size.join(', ') }}</p> -->
                        <p class="mb-0"><small class="text-muted">Size:</small> {{product.kichThuocList.map(item =>
                            item.size).join(', ') }}</p>
                      </div>
                      <div class="product-price fs-5 fw-semibold">
                        <span class="current-price text-success">{{ formatPriceRange(product.khoangGia) }}</span>
                      </div>
                    </div>
                    <div
                        class="product-overlay d-flex align-items-center justify-content-center position-absolute top-0 start-0 w-100 h-100 bg-dark bg-opacity-50 transition-opacity">
                      <button class="quick-add-btn btn btn-success rounded-pill shadow"
                              @click.stop="addToCart(product)"
                              :disabled="cartStore.isAdding">
                        <span v-if="cartStore.isAdding" class="spinner-border spinner-border-sm me-2"></span>
                        <i class="bi bi-cart-plus"></i>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <button class="carousel-control-prev custom-prev" type="button" data-bs-target="#newArrivalsCarousel"
                  data-bs-slide="prev">
            <span class="carousel-control-prev-icon" aria-hidden="true"></span>
            <span class="visually-hidden">Previous</span>
          </button>
          <button class="carousel-control-next custom-next" type="button" data-bs-target="#newArrivalsCarousel"
                  data-bs-slide="next">
            <span class="carousel-control-next-icon" aria-hidden="true"></span>
            <span class="visually-hidden">Next</span>
          </button>
        </div>

        <div class="section-actions text-center mt-4 animate__animated animate__fadeInUp">
          <router-link to="/products" class="btn btn-outline-success">Xem tất cả sản phẩm</router-link>
        </div>
      </div>
    </section>

    <!-- Banner Slideshow Section -->
    <section class="banner-section py-5">
      <div class="container">
        <div id="bannerCarousel" class="carousel slide">
          <div class="carousel-indicators">
            <button type="button" data-bs-target="#bannerCarousel" data-bs-slide-to="0" class="active"
                    aria-current="true" aria-label="Slide 1"></button>
            <button type="button" data-bs-target="#bannerCarousel" data-bs-slide-to="1" aria-label="Slide 2"></button>
            <button type="button" data-bs-target="#bannerCarousel" data-bs-slide-to="2" aria-label="Slide 3"></button>
          </div>
          <div class="carousel-inner">
            <div class="carousel-item active">
              <img src="../assets/slide3.jpg" class="d-block w-100" alt="Banner 1"
                   style=" object-fit: cover;">
            </div>
            <div class="carousel-item">
              <img src="../assets/slide3.jpg" class="d-block w-100" alt="Banner 2"
                   style=" object-fit: cover;">
            <div class="carousel-item">
              <img src="../assets/slide2.png" class="d-block w-100" alt="Banner 3"
                   style=" object-fit: cover;">
            </div>
          </div>
          <button class="carousel-control-prev" type="button" data-bs-target="#bannerCarousel" data-bs-slide="prev">
            <span class="carousel-control-prev-icon" aria-hidden="true"></span>
            <span class="visually-hidden">Previous</span>
          </button>
          <button class="carousel-control-next" type="button" data-bs-target="#bannerCarousel" data-bs-slide="next">
            <span class="carousel-control-next-icon" aria-hidden="true"></span>
            <span class="visually-hidden">Next</span>
          </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Featured Products Section -->
    <section class="featured-products-section py-5">
      <div class="container">
        <div class="section-header text-center mb-5 animate__animated animate__fadeIn">
          <h2 class="section-title display-5 fw-bold">Sản phẩm nổi bật</h2>
          <p class="section-subtitle text-muted fs-5">Những mẫu áo polo được yêu thích nhất</p>
        </div>

        <div id="featuredProductsCarousel" class="carousel slide">
          <div class="carousel-inner">
            <div v-for="(chunk, chunkIndex) in chunkedFeaturedProducts" :key="chunkIndex" class="carousel-item"
                 :class="{ active: chunkIndex === 0 }">
              <div class="row g-4">
                <div v-for="product in chunk" :key="product.id" class="col-12 col-sm-6 col-md-3">
                  <div class="product-card card h-100 shadow-sm cursor-pointer animate__animated animate__zoomIn"
                       @click="navigateToProduct(product.id)">
                    <div class="product-image position-relative overflow-hidden" style="height: 250px;">
                      <img v-if="product.imageList && product.imageList.length > 0" :src="product.imageList[0]"
                           :alt="product.tenSanPham" class="card-img-top object-fit-cover h-100 w-100" />
                      <img v-else src="" :alt="product.tenSanPham" class="card-img-top object-fit-cover h-100 w-100" />

                      <!-- <div
                        class="product-badge position-absolute top-0 start-0 bg-info text-white px-3 py-1 rounded-pill fs-6">
                        Mới
                      </div> -->
                    </div>
                    <div class="product-info card-body">
                      <h3 class="product-name card-title fs-6 fw-semibold">{{ product.tenSanPham }}
                        <span v-if="product.phanTramGiam > 0" class="text-danger fw-bold ms-2">
                          -{{ product.phanTramGiam }}%
                        </span>
                      </h3>
                      <div class="product-details mb-2">
                        <p class="mb-1"><small class="text-muted">Màu sắc:</small>
                          <span v-for="(color, cIdx) in product.mauSacList" :key="cIdx" class="color-dot"
                                :style="{ backgroundColor: color.maMauSac }"></span>
                        </p>
                        <p class="mb-0"><small class="text-muted">Size:</small> {{product.kichThuocList.map(item =>
                            item.size).join(', ')}}</p>
                      </div>
                      <div class="product-price fs-5 fw-semibold">
                        <span class="current-price text-success">{{ formatPriceRange(product.khoangGia) }}</span>
                        <span v-if="product.originalPrice"
                              class="original-price text-muted text-decoration-line-through ms-2">{{
                            formatPrice(product.originalPrice) }}</span>
                      </div>
                    </div>
                    <div
                        class="product-overlay d-flex align-items-center justify-content-center position-absolute top-0 start-0 w-100 h-100 bg-dark bg-opacity-50 transition-opacity">
                      <div class="product-actions d-flex gap-3">
                        <button class="quick-add-btn btn btn-success rounded-pill shadow"
                                @click.stop="addToCart(product)">
                          <i class="bi bi-cart-plus me-2"></i>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <button class="carousel-control-prev custom-prev" type="button" data-bs-target="#featuredProductsCarousel"
                  data-bs-slide="prev">
            <span class="carousel-control-prev-icon" aria-hidden="true"></span>
            <span class="visually-hidden">Previous</span>
          </button>
          <button class="carousel-control-next custom-next" type="button" data-bs-target="#featuredProductsCarousel"
                  data-bs-slide="next">
            <span class="carousel-control-next-icon" aria-hidden="true"></span>
            <span class="visually-hidden">Next</span>
          </button>
        </div>

        <div class="section-actions text-center mt-4 animate__animated animate__fadeInUp">
          <router-link to="/products" class="btn btn-outline-success">Xem tất cả sản phẩm</router-link>
        </div>
      </div>
    </section>

    <!-- About Section -->
    <section class="about-section py-5">
      <div class="container">
        <div
            class="about-content d-flex flex-column flex-lg-row align-items-center gap-4 animate__animated animate__fadeInUp">
          <div class="about-text flex-fill">
            <h2 class="about-title display-5 fw-bold">Về Polo Haven</h2>
            <p class="about-description text-muted mb-4">
              Với hơn 10 năm kinh nghiệm trong ngành thời trang, Polo Haven tự hào là
              địa chỉ tin cậy cung cấp những mẫu áo polo chất lượng cao, thiết kế
              hiện đại và phù hợp với mọi phong cách.
            </p>
            <div class="about-stats d-flex gap-4 mb-4">
              <div class="stat-item text-center">
                <div class="stat-number display-6 fw-bold">10+</div>
                <div class="stat-label text-muted">Năm kinh nghiệm</div>
              </div>
              <div class="stat-item text-center">
                <div class="stat-number display-6 fw-bold">50K+</div>
                <div class="stat-label text-muted">Khách hàng tin tưởng</div>
              </div>
              <div class="stat-item text-center">
                <div class="stat-number display-6 fw-bold">100+</div>
                <div class="stat-label text-muted">Mẫu thiết kế</div>
              </div>
            </div>
            <router-link to="/about" class="btn btn-success btn-lg rounded-pill">Tìm hiểu thêm</router-link>
          </div>
          <div class="about-image flex-fill">
            <img src="../assets/img/havenlogo.png" alt="Về Polo Haven" class="img-fluid rounded shadow-lg" />
          </div>
        </div>
      </div>
    </section>

    <!-- Blog Section -->
    <section class="blog-section py-5">
      <div class="container">
        <div class="section-header text-center mb-5 animate__animated animate__fadeIn">
          <h2 class="section-title display-5 fw-bold">Tin tức & Blog</h2>
          <p class="section-subtitle text-muted fs-5">Cập nhật xu hướng thời trang mới nhất</p>
        </div>
        <div class="row g-4">
          <article v-for="post in blogPosts" :key="post.id"
                   class="blog-card col-12 col-md-4 cursor-pointer animate__animated animate__fadeInUp"
                   @click="navigateToPost(post.slug)">
            <div class="blog-image position-relative overflow-hidden" style="height: 250px;">
              <img :src="post.image" :alt="post.title" class="img-fluid rounded" />
              <div class="blog-category position-absolute top-0 start-0 bg-success text-white px-3 py-1 rounded-end">{{
                  post.category }}</div>
            </div>
            <div class="blog-content p-3 bg-white rounded shadow-sm">
              <div class="blog-meta d-flex justify-content-between text-muted mb-2 small">
                <span class="blog-date"><i class="bi bi-calendar-event me-1"></i>{{ formatDate(post.date) }}</span>
                <span class="blog-author"><i class="bi bi-person me-1"></i>{{ post.author }}</span>
              </div>
              <h3 class="blog-title fs-5 fw-semibold">{{ post.title }}</h3>
              <p class="blog-excerpt">{{ post.excerpt }}</p>
              <div class="blog-footer d-flex justify-content-between align-items-center">
                <span class="read-more text-success fw-semibold cursor-pointer">Đọc thêm <i
                    class="bi bi-arrow-right"></i></span>
                <div class="blog-stats text-muted small d-flex gap-3">
                  <span><i class="bi bi-eye"></i> {{ post.views }}</span>
                  <span><i class="bi bi-chat-left"></i> {{ post.comments }}</span>
                </div>
              </div>
            </div>
          </article>
        </div>
        <div class="section-actions text-center mt-4 animate__animated animate__fadeInUp">
          <router-link to="/news" class="btn btn-outline-success">Xem tất cả bài viết</router-link>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section py-5 bg-gradient animate__animated animate__fadeIn">
      <div class="container">
        <div class="cta-content text-center">
          <h2 class="cta-title display-4 fw-bold">Sẵn sàng tìm chiếc áo polo hoàn hảo?</h2>
          <p class="cta-subtitle fs-4 mb-4">
            Khám phá bộ sưu tập đa dạng với hàng trăm mẫu thiết kế độc đáo
          </p>
          <div class="cta-actions d-flex justify-content-center gap-3">
            <router-link to="/products" class="btn btn-light btn-lg rounded-pill">
              Mua sắm ngay <i class="bi bi-arrow-right"></i>
            </router-link>
            <router-link to="/contact" class="btn btn-light btn-lg rounded-pill">
              Liên hệ tư vấn <i class="bi bi-chat-dots"></i>
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <!-- Back to Top Button -->
    <button v-if="showBackToTop" @click="scrollToTop"
            class="btn btn-success back-to-top animate__animated animate__fadeIn">
      <i class="bi bi-arrow-up"></i>
    </button>

    <!-- Quick Add Modal -->
    <div v-if="selectedProduct" class="modal fade" id="quickAddModal" tabindex="-1" aria-labelledby="quickAddModalLabel"
         aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="quickAddModalLabel">Thêm vào giỏ hàng - {{ selectedProduct.tenSanPham }}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="row">
              <div class="col-12 col-md-6">
                <img :src="selectedProduct.image || '/src/assets/ao1.webp'" class="img-fluid rounded"
                     alt="Product Image" />
              </div>
              <div class="col-12 col-md-6">
                <div class="mb-2 d-flex align-items-center gap-2">
                  <span v-if="selectedProduct?.phanTramGiam > 0" class="badge bg-danger">
                    -{{ selectedProduct.phanTramGiam }}%
                  </span>
                </div>
                <div class="mb-3 d-flex align-items-end gap-2">
                  <span class="fw-bold text-danger" style="font-size: 1.1rem;">{{ formatPrice(selectedPrice) }}</span>
                  <span v-if="selectedProduct?.phanTramGiam > 0 && selectedOriginalPrice > selectedPrice"
                        class="text-muted text-decoration-line-through" style="font-size: 0.95rem;">{{
                      formatPrice(selectedOriginalPrice) }}</span>
                </div>
                <div class="mb-3">
                  <label class="form-label">Màu sắc:</label>
                  <div>
                    <button v-for="color in selectedProduct.mauSacList" :key="color.id" type="button"
                            class="color-dot btn btn-sm me-2"
                            :style="{ backgroundColor: color.maMauSac, border: selectedProduct.selectedColor === color.id ? '2px solid #4ba27b' : '1px solid #eee' }"
                            @click="selectColor(color.id)"></button>
                  </div>
                </div>
                <div class="mb-3">
                  <label class="form-label">Size:</label>
                  <div>
                    <button v-for="size in selectedProduct.kichThuocList" :key="size.size" type="button"
                            class="btn btn-sm me-2"
                            :class="{ 'btn-secondary': selectedProduct.selectedSize === size.size, 'btn-outline-secondary': selectedProduct.selectedSize !== size.size }"
                            @click="selectSize(size.size)">{{ size.size }}</button>
                  </div>
                </div>
                <div class="mb-3">
                  <label for="quantity" class="form-label">Số lượng:</label>
                  <input type="number" v-model="quantity" id="quantity" class="form-control" min="1" />
                </div>
                <button @click="confirmAddToCart" class="btn btn-success w-100"
                        :disabled="!selectedProduct.selectedColor || !selectedProduct.selectedSize || isLoadingVariants">
                  <i class="bi bi-cart-plus me-2"></i> Thêm nhanh vào giỏ hàng
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick View Modal -->
    <div v-if="quickViewProduct" class="modal fade" id="quickViewModal" tabindex="-1"
         aria-labelledby="quickViewModalLabel" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="quickViewModalLabel">{{ quickViewProduct.tenSanPham }}</h5>
            <button type="button" class="btn-close" @click="closeQuickViewModal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="row">
              <div class="col-12 col-md-6">
                <img :src="quickViewProduct.image || '/src/assets/ao1.webp'" class="img-fluid rounded"
                     alt="Product Image" />
              </div>
              <div class="col-12 col-md-6">
                <h6>Giá: {{ formatPriceRange(quickViewProduct.khoangGia) }}</h6>
                <p>Màu sắc:
                  <span v-for="(color, cIdx) in quickViewProduct.mauSacList" :key="cIdx" class="color-dot"
                        :style="{ backgroundColor: color.ten }"></span>
                </p>
                <p>Size: {{ quickViewProduct.kichThuocList.map(item => item.size).join(', ') }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import 'animate.css'
import { getListArrivalProduct, getListBestSelling } from '@/services/trangChu'
import { getListSanPhamChiTiet } from '@/services/spct'
import { useCartStore } from '@/stores/cart'
import { jwtDecode } from 'jwt-decode'
import { useToast } from 'vue-toastification'

const toast = useToast()

const router = useRouter()
const cartStore = useCartStore()


const features = ref([
  { id: 1, icon: 'bi bi-award-fill', title: 'Chất lượng cao', description: 'Chất liệu cotton 100% cao cấp, bền đẹp theo thời gian' },
  { id: 2, icon: 'bi bi-truck', title: 'Giao hàng nhanh', description: 'Giao hàng toàn quốc trong 24-48h, miễn phí với đơn từ 500k' },
  { id: 3, icon: 'bi bi-arrow-repeat', title: 'Đổi trả dễ dàng', description: 'Chính sách đổi trả trong 7 ngày, không cần lý do' },
  { id: 4, icon: 'bi bi-headset', title: 'Hỗ trợ 24/7', description: 'Đội ngũ tư vấn chuyên nghiệp, hỗ trợ khách hàng mọi lúc' }
])
const blogPosts = ref([
  { id: 1, title: 'Xu hướng thời trang áo polo 2025', excerpt: 'Khám phá những xu hướng mới nhất trong thế giới thời trang áo polo...', image: '/src/assets/ao1.webp', category: 'Xu hướng', date: '2024-01-15', author: 'Admin', views: 1250, comments: 8, slug: 'xu-huong-thoi-trang-ao-polo-2024' },
  { id: 2, title: 'Cách chọn áo polo phù hợp với dáng người', excerpt: 'Hướng dẫn chi tiết cách chọn áo polo phù hợp với từng dáng người...', image: '/src/assets/ao2.webp', category: 'Hướng dẫn', date: '2024-01-10', author: 'Stylist', views: 980, comments: 12, slug: 'cach-chon-ao-polo-phu-hop-voi-dang-nguoi' },
  { id: 3, title: 'Bảo quản áo polo đúng cách', excerpt: 'Những mẹo hay giúp bạn bảo quản áo polo luôn mới và bền đẹp...', image: '/src/assets/ao3.webp', category: 'Mẹo hay', date: '2024-01-05', author: 'Expert', views: 765, comments: 6, slug: 'bao-quan-ao-polo-dung-cach' }
])

const newArrivalProducts = ref([])
const featuredProducts = ref([])
const productVariants = ref([])
const showBackToTop = ref(false)
const selectedProduct = ref(null)
const quantity = ref(1)
const quickViewProduct = ref(null)
const isLoadingVariants = ref(false)

const fetchNewArrivalProduct = async () => {
  try {
    const response = await getListArrivalProduct()
    newArrivalProducts.value = response.data.map(product => ({
      ...product,
      id: product.id || null,
      image: product.image || '/src/assets/ao1.webp',
      mauSacList: product.mauSacList || [],
      kichThuocList: product.kichThuocList || []
    }))
  } catch (error) {
    console.error('Lỗi khi tải sản phẩm mới:', error)
    toast.error('Không thể tải danh sách sản phẩm mới!')
  }
}

const fetchFeaturedProducts = async () => {
  try {
    const response = await getListBestSelling()
    featuredProducts.value = response.data.map(product => ({
      ...product,
      id: product.id || null,
      image: product.image || '/src/assets/ao1.webp',
      mauSacList: product.mauSacList || [],
      kichThuocList: product.kichThuocList || []
    }))
  } catch (error) {
    console.error('Lỗi khi tải sản phẩm nổi bật:', error)
    toast.error('Không thể tải danh sách sản phẩm nổi bật!')
  }
}

const fetchProductVariants = async () => {
  try {
    isLoadingVariants.value = true
    const response = await getListSanPhamChiTiet()
    productVariants.value = response.data.map(variant => ({
      ...variant,
      id: variant.id,
      sanPhamId: variant.sanPhamId || null,
      mauSac: variant.mauSac || 'Unknown',
      kichThuoc: String(variant.kichThuoc) || 'Unknown',
      // Tạm thời coi donGia là giá gốc; sẽ tính lại sau khi biết sản phẩm tương ứng
      giaGoc: parseFloat(variant.donGia) || 0,
      donGia: parseFloat(variant.donGia) || 0
    }))
    // Ánh xạ sanPhamId dựa trên tenSanPham, chatLieu, thuongHieu
    productVariants.value = productVariants.value.map(variant => {
      const product = [...newArrivalProducts.value, ...featuredProducts.value].find(
          p => p.tenSanPham === variant.tenSanPham && p.chatLieu === variant.chatLieu && p.thuongHieu === variant.thuongHieu
      )
      const discount = product?.phanTramGiam || 0
      const giaGoc = variant.giaGoc || parseFloat(variant.donGia) || 0
      const giaSauGiam = discount > 0 ? Math.round(giaGoc * (1 - discount / 100)) : giaGoc
      return {
        ...variant,
        sanPhamId: product ? product.id : null,
        giaGoc,
        donGia: giaSauGiam
      }
    })
  } catch (error) {
    console.error('Lỗi khi tải biến thể sản phẩm:', error)
    alert('Không thể tải dữ liệu biến thể sản phẩm!')
  } finally {
    isLoadingVariants.value = false
  }
}

const chunkedNewArrivalProducts = computed(() => {
  const chunkSize = 4
  const chunks = []
  for (let i = 0; i < newArrivalProducts.value.length; i += chunkSize) {
    chunks.push(newArrivalProducts.value.slice(i, i + chunkSize))
  }
  return chunks
})

const chunkedFeaturedProducts = computed(() => {
  const chunkSize = 4
  const chunks = []
  for (let i = 0; i < featuredProducts.value.length; i += chunkSize) {
    chunks.push(featuredProducts.value.slice(i, i + chunkSize))
  }
  return chunks
})

const handleScroll = () => {
  showBackToTop.value = window.scrollY > 300
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const navigateToProduct = (id) => {
  if (!id) {
    console.error('Lỗi: ID sản phẩm không được định nghĩa')
    toast.error('Không thể điều hướng: Sản phẩm không hợp lệ!')
    return
  }
  router.push(`/product/${id}`)
}

const navigateToPost = (slug) => {
  if (!slug) {
    console.error('Lỗi: Slug bài viết không được định nghĩa')
    return
  }
  router.push(`/news/${slug}`)
}

const addToCart = async (product) => {
  try {
    if (!product.id) {
      throw new Error('ID sản phẩm không hợp lệ')
    }
    if (isLoadingVariants.value) {
      toast.info('Đang tải dữ liệu biến thể, vui lòng thử lại sau!')
      return
    }
    // Lấy danh sách biến thể của sản phẩm
    const variants = productVariants.value.filter(v => v.sanPhamId === product.id)
    if (variants.length === 0) {
      toast.error(`Không tìm thấy biến thể cho sản phẩm "${product.tenSanPham}". Vui lòng kiểm tra dữ liệu sản phẩm hoặc liên hệ hỗ trợ.`)
      return
    }
    // Loại bỏ trùng lặp màu sắc và kích thước dựa trên giá trị và ánh xạ thông tin màu sắc đầy đủ
    const uniqueColors = Array.from(
        new Map(variants.map(v => {
          const colorInfo = product.mauSacList.find(c => c.ten.toLowerCase() === v.mauSac.toLowerCase())
          return [v.mauSac, colorInfo || { id: v.mauSac, ten: v.mauSac, maMauSac: '#eee' }]
        })).values()
    )
    const uniqueSizes = Array.from(
        new Map(variants.map(v => [v.kichThuoc, { size: v.kichThuoc }])).values()
    )
    if (uniqueColors.length === 0 || uniqueSizes.length === 0) {
      toast.error('Sản phẩm này không có màu sắc hoặc kích thước khả dụng!')
      return
    }

    // Chọn biến thể mặc định
    const defaultVariant = variants[0]
    const defaultColorObj = uniqueColors.find(c => c.ten.toLowerCase() === defaultVariant.mauSac.toLowerCase())
    
    selectedProduct.value = {
      ...product,
      tenSanPham: product.tenSanPham,
      image: product.image || '/src/assets/ao1.webp',
      mauSacList: uniqueColors,
      kichThuocList: uniqueSizes,
      variants,
      selectedColor: defaultColorObj ? defaultColorObj.id : defaultVariant.mauSac,
      selectedSize: defaultVariant.kichThuoc,
      donGia: defaultVariant.donGia
    }
    quantity.value = 1

    // Cập nhật giá ngay khi mở modal
    updateSelectedPrice()

    await nextTick()
    if (window.bootstrap && window.bootstrap.Modal) {
      const modal = new window.bootstrap.Modal(document.getElementById('quickAddModal'))
      modal.show()
    }



    let token;
    token = localStorage.getItem('authToken')
    if(token){
      // khách hàng có đăng nhập
      const decodeToken = jwtDecode(token);
      const userId = decodeToken.userId;
    }





  } catch (error) {
    console.error('Lỗi khi thêm vào giỏ hàng:', error)
    toast.error('Có lỗi xảy ra khi thêm sản phẩm vào giỏ hàng!')
  }
}

const selectColor = (colorId) => {
  if (selectedProduct.value) {
    selectedProduct.value.selectedColor = colorId
    updateSelectedPrice()
  }
}

const selectSize = (sizeValue) => {
  if (selectedProduct.value) {
    selectedProduct.value.selectedSize = sizeValue
    updateSelectedPrice()
  }
}

const updateSelectedPrice = () => {
  if (!selectedProduct.value || !selectedProduct.value.variants || !selectedProduct.value.selectedColor || !selectedProduct.value.selectedSize) {
    selectedProduct.value.donGia = selectedProduct.value.variants?.[0]?.donGia || parseFloat(selectedProduct.value.khoangGia?.split(' - ')[0]) || 0
    return
  }
  
  const colorObj = selectedProduct.value.mauSacList.find(c => c.id === selectedProduct.value.selectedColor);
  const colorName = colorObj ? colorObj.ten : selectedProduct.value.selectedColor;

  const variant = selectedProduct.value.variants.find(
      v => v.mauSac === colorName && v.kichThuoc === selectedProduct.value.selectedSize
  )
  selectedProduct.value.donGia = variant ? variant.donGia : selectedProduct.value.variants[0]?.donGia || parseFloat(selectedProduct.value.khoangGia?.split(' - ')[0]) || 0
}

const selectedPrice = computed(() => {
  return selectedProduct.value?.donGia || 0
})

const selectedOriginalPrice = computed(() => {
  if (!selectedProduct.value?.variants) return selectedProduct.value?.donGia || 0
  
  const colorObj = selectedProduct.value.mauSacList.find(c => c.id === selectedProduct.value.selectedColor);
  const colorName = colorObj ? colorObj.ten : selectedProduct.value.selectedColor;

  const v = selectedProduct.value.variants.find(
      vv => vv.mauSac === colorName && vv.kichThuoc === selectedProduct.value.selectedSize
  )
  return v?.giaGoc || selectedProduct.value?.donGia || 0
})

const confirmAddToCart = () => {
  if (
      selectedProduct.value &&
      quantity.value > 0 &&
      selectedProduct.value.selectedColor &&
      selectedProduct.value.selectedSize
  ) {
    const colorObj = selectedProduct.value.mauSacList.find(c => c.id === selectedProduct.value.selectedColor);
    const colorName = colorObj ? colorObj.ten : selectedProduct.value.selectedColor;

    const variant = selectedProduct.value.variants.find(
        v => v.mauSac === colorName && v.kichThuoc === selectedProduct.value.selectedSize
    )
    if (!variant) {
      toast.error('Không tìm thấy biến thể phù hợp với màu sắc và kích thước đã chọn! Vui lòng chọn lại.')
      return
    }
    cartStore.addToCart({
      id: variant.id,
      tenSanPham: selectedProduct.value.tenSanPham,
      image: selectedProduct.value.image,
      color: colorName,
      size: selectedProduct.value.selectedSize,
      price: variant.donGia,
      originalPrice: variant.giaGoc || variant.donGia,
      phanTramGiam: selectedProduct.value.phanTramGiam || 0,
      soLuongTon: typeof variant.soLuong === 'number' ? variant.soLuong : undefined,
      maSanPham: selectedProduct.value.maSanPham || null,
      productViewId: selectedProduct.value.id || null
    }, quantity.value)
    closeModal()
  } else {
    toast.info('Vui lòng chọn đầy đủ màu sắc và kích thước!')
  }
}


const quickView = async (product) => {
  quickViewProduct.value = product
  await nextTick()
  const modal = new window.bootstrap.Modal(document.getElementById('quickViewModal'))
  modal.show()
}

const closeQuickViewModal = () => {
  quickViewProduct.value = null
  const modal = window.bootstrap.Modal.getInstance(document.getElementById('quickViewModal'))
  if (modal) modal.hide()
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', minimumFractionDigits: 0, maximumFractionDigits: 0 }).format(price)
}

const formatPriceRange = (priceString, locale = 'vi-VN', currency = 'VND') => {
  if (!priceString || typeof priceString !== 'string' || !priceString.includes(' - ')) {
    const singlePrice = parseFloat(priceString)
    if (!isNaN(singlePrice)) {
      return new Intl.NumberFormat(locale, {
        style: 'currency',
        currency: currency,
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
      }).format(singlePrice)
    }
    return 'N/A'
  }
  const [minPriceStr, maxPriceStr] = priceString.split(' - ')
  const minPrice = parseFloat(minPriceStr)
  const maxPrice = parseFloat(maxPriceStr)
  if (isNaN(minPrice) || isNaN(maxPrice)) {
    return 'Giá không hợp lệ'
  }
  const formatter = new Intl.NumberFormat(locale, {
    style: 'currency',
    currency: currency,
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  })
  if (minPrice === maxPrice) {
    return formatter.format(minPrice)
  }
  return `${formatter.format(minPrice)} - ${formatter.format(maxPrice)}`
}

const formatDate = (date) => {
  return new Date(date).toLocaleDateString('vi-VN', { year: 'numeric', month: 'long', day: 'numeric' })
}

const closeModal = () => {
  if (window.bootstrap && window.bootstrap.Modal) {
    const modal = document.getElementById('quickAddModal')
    const bootstrapModal = window.bootstrap.Modal.getInstance(modal)
    if (bootstrapModal) {
      bootstrapModal.hide()
    }
  }
}

onMounted(async () => {
  await Promise.all([
    fetchNewArrivalProduct(),
    fetchFeaturedProducts(),
    fetchProductVariants()
  ])

  // Initialize carousels
  const heroCarouselElement = document.getElementById('carouselExampleCaptions')
  if (heroCarouselElement && window.bootstrap) {
    new window.bootstrap.Carousel(heroCarouselElement, { interval: 5000, wrap: true })
  }

  const newArrivalsCarouselElement = document.getElementById('newArrivalsCarousel')
  if (newArrivalsCarouselElement && window.bootstrap) {
    new window.bootstrap.Carousel(newArrivalsCarouselElement, { interval: false, wrap: false })
  }

  const featuredProductsCarouselElement = document.getElementById('featuredProductsCarousel')
  if (featuredProductsCarouselElement && window.bootstrap) {
    new window.bootstrap.Carousel(featuredProductsCarouselElement, { interval: false, wrap: false })
  }

  const bannerCarouselElement = document.getElementById('bannerCarousel')
  if (bannerCarouselElement && window.bootstrap) {
    new window.bootstrap.Carousel(bannerCarouselElement, { interval: 5000, wrap: true })
  }

  window.addEventListener('scroll', handleScroll)
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style>
@import 'animate.css';

.homepage {
  padding-top: 130px;
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

#carouselExampleCaptions {
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

/* Features Section */
.feature-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.15) !important;
}

.feature-icon {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  width: 70px;
  height: 70px;
  font-size: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: white;
}

/* Product Carousel Styles */
#newArrivalsCarousel .carousel-control-prev.custom-prev,
#newArrivalsCarousel .carousel-control-next.custom-next,
#featuredProductsCarousel .carousel-control-prev.custom-prev,
#featuredProductsCarousel .carousel-control-next.custom-next {
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

#newArrivalsCarousel .carousel-control-prev.custom-prev:hover,
#newArrivalsCarousel .carousel-control-next.custom-next:hover,
#featuredProductsCarousel .carousel-control-prev.custom-prev:hover,
#featuredProductsCarousel .carousel-control-next.custom-next:hover {
  background-color: #66ea8b;
  transform: translateY(-50%) scale(1.1);
}

#newArrivalsCarousel .carousel-control-prev.custom-prev,
#featuredProductsCarousel .carousel-control-prev.custom-prev {
  left: 0;
  margin-left: -60px;
}

#newArrivalsCarousel .carousel-control-next.custom-next,
#featuredProductsCarousel .carousel-control-next.custom-next {
  right: 0;
  margin-right: -60px;
}

#newArrivalsCarousel .carousel-control-prev.custom-prev .carousel-control-prev-icon,
#newArrivalsCarousel .carousel-control-next.custom-next .carousel-control-next-icon,
#featuredProductsCarousel .carousel-control-prev.custom-prev .carousel-control-prev-icon,
#featuredProductsCarousel .carousel-control-next.custom-next .carousel-control-next-icon {
  background-image: none;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: white;
}

#newArrivalsCarousel .carousel-control-prev.custom-prev .carousel-control-prev-icon::before,
#featuredProductsCarousel .carousel-control-prev.custom-prev .carousel-control-prev-icon::before {
  content: "\f0d9";
  font-family: "Font Awesome 5 Free";
  font-weight: 900;
}

#newArrivalsCarousel .carousel-control-next.custom-next .carousel-control-next-icon::before,
#featuredProductsCarousel .carousel-control-next.custom-next .carousel-control-next-icon::before {
  content: "\f0da";
  font-family: "Font Awesome 5 Free";
  font-weight: 900;
}

/* Product Card Styles */
.new-arrivals-section .product-card,
.featured-products-section .product-card {
  border: none;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
  transition: transform 0.3s ease, box-shadow 0.3s ease, opacity 0.3s ease;
  min-height: 300px;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1) !important;
  opacity: 0.9;
}

.product-image {
  height: 250px;
  overflow: hidden;
  position: relative;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.product-overlay {
  background: rgba(0, 0, 0, 0.5);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 2;
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.product-card:hover .product-overlay {
  opacity: 1;
  pointer-events: auto;
}

.product-actions {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  transition: opacity 0.3s ease;
  z-index: 3;
}

.product-actions .action-btn {
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  width: 100px;
  height: 45px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 1.2rem;
  transition: all 0.3s ease;
}

.product-actions .action-btn:hover {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  color: white;
  transform: scale(1.1);
}

.quick-add-btn {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  padding: 8px;
  font-size: 1.5rem;
  font-weight: 600;
  opacity: 0;
  transition: opacity 0.3s ease, background-color 0.3s ease, box-shadow 0.3s ease;
  z-index: 4;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 45px;
  height: 45px;
  background: linear-gradient(135deg, #4ba27b 0%, #66ea8b 100%);
  color: white;
  border: none;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(75, 162, 123, 0.6);
  cursor: pointer;
}

.quick-add-btn:hover {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  box-shadow: 0 6px 16px rgba(102, 234, 139, 0.8);
}

.product-card:hover .quick-add-btn {
  opacity: 1;
}

.product-badge {
  z-index: 5;
}

.product-info {
  padding: 1.5rem;
}

.product-name {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #333;
}

.product-details .color-dot {
  display: inline-block;
  width: 15px;
  height: 15px;
  border-radius: 50%;
  border: 1px solid #eee;
  margin-right: 5px;
  vertical-align: middle;
}

.product-price .current-price {
  color: #32CD32;
  font-size: 1.4rem;
  font-weight: 700;
}

.product-price .original-price {
  font-size: 1rem;
  color: #999;
}

/* Banner Section */
.banner-section {
  position: relative;
  height: 300px;
  overflow: hidden;
}

#bannerCarousel {
  height: 100%;
}

#bannerCarousel .carousel-item {
  height: 100%;
}

#bannerCarousel .carousel-item img {
  height: 100%;
  object-fit: cover;
}

#bannerCarousel .carousel-caption {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #fff;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
  text-align: center;
}

#bannerCarousel .carousel-caption h5 {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 10px;
}

#bannerCarousel .carousel-caption p {
  font-size: 1.2rem;
  opacity: 0.9;
}

/* About Section */
.about-content {
  background-color: #f8f9fa;
  padding: 3rem;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.about-title {
  color: #333;
}

.about-description {
  color: #555;
}

.stat-item .stat-number {
  color: #4ba27b;
}

.about-image img {
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

/* Blog Section */
.blog-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.blog-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1) !important;
}

.blog-image {
  border-radius: 10px;
  overflow: hidden;
  position: relative;
}

.blog-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.blog-category {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  color: white;
  padding: 5px 15px;
  border-bottom-right-radius: 10px;
  font-size: 0.9rem;
  font-weight: 600;
}

.blog-content {
  margin-top: -20px;
  position: relative;
  z-index: 1;
  padding-top: 25px !important;
}

.blog-meta span i {
  color: #4ba27b;
  margin-right: 5px;
}

.blog-title {
  color: #333;
  margin-top: 10px;
  margin-bottom: 10px;
}

.blog-excerpt {
  color: #666;
  font-size: 0.95rem;
  line-height: 1.6;
}

.read-more {
  color: #4ba27b;
  font-weight: 600;
  cursor: pointer;
  transition: color 0.3s ease;
}

.read-more:hover {
  color: #28a745;
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

.cta-actions .btn-outline-light {
  color: #fff;
  border-color: #fff;
}

.cta-actions .btn-outline-light:hover {
  background-color: #fff;
  color: #4ba27b;
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

/* Modal Styles */
#quickAddModal .modal-content {
  border-radius: 10px;
}

#quickAddModal .modal-header {
  border-bottom: none;
}

#quickAddModal .modal-body {
  padding: 2rem;
}

#quickAddModal .modal-body h6 {
  font-weight: 600;
  margin-bottom: 1rem;
}

#quickAddModal .modal-body .color-dot {
  display: inline-block;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 1px solid #eee;
  margin-right: 5px;
  vertical-align: middle;
}

#quickAddModal .modal-body .form-control {
  max-width: 100px;
}

/* Responsive Adjustments */
@media (max-width: 992px) {
  .hero-title {
    font-size: 2.8rem;
  }

  .hero-subtitle {
    font-size: 1.1rem;
  }

  .carousel-caption {
    padding-left: 10%;
    padding-right: 10%;
  }

  .about-content {
    flex-direction: column;
  }

  .about-image {
    margin-top: 2rem;
  }

  #newArrivalsCarousel .carousel-control-prev.custom-prev,
  #newArrivalsCarousel .carousel-control-next.custom-next,
  #featuredProductsCarousel .carousel-control-prev.custom-prev,
  #featuredProductsCarousel .carousel-control-next.custom-next {
    width: 40px;
    height: 40px;
  }

  #newArrivalsCarousel .carousel-control-prev.custom-prev,
  #featuredProductsCarousel .carousel-control-prev.custom-prev {
    margin-left: -50px;
  }

  #newArrivalsCarousel .carousel-control-next.custom-next,
  #featuredProductsCarousel .carousel-control-next.custom-next {
    margin-right: -50px;
  }
}

@media (max-width: 768px) {
  .homepage {
    padding-top: 100px;
  }

  .hero-section {
    height: 60vh;
    min-height: 400px;
  }

  .hero-title {
    font-size: 2.2rem;
  }

  .hero-subtitle {
    font-size: 1rem;
  }

  .carousel-caption {
    text-align: center;
    align-items: center;
    padding-left: 5%;
    padding-right: 5%;
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

  #newArrivalsCarousel .carousel-control-prev.custom-prev,
  #newArrivalsCarousel .carousel-control-next.custom-next,
  #featuredProductsCarousel .carousel-control-prev.custom-prev,
  #featuredProductsCarousel .carousel-control-next.custom-next {
    width: 30px;
    height: 30px;
    font-size: 1.2rem;
  }

  #newArrivalsCarousel .carousel-control-prev.custom-prev,
  #featuredProductsCarousel .carousel-control-prev.custom-prev {
    margin-left: -40px;
  }

  #newArrivalsCarousel .carousel-control-next.custom-next,
  #featuredProductsCarousel .carousel-control-next.custom-next {
    margin-right: -40px;
  }

  #bannerCarousel .carousel-caption h5 {
    font-size: 2rem;
  }

  #bannerCarousel .carousel-caption p {
    font-size: 1rem;
  }
}

@media (max-width: 576px) {
  .hero-section {
    min-height: 350px;
  }

  .hero-title {
    font-size: 1.8rem;
  }

  .hero-subtitle {
    font-size: 0.9rem;
  }

  .product-image {
    height: 200px;
  }

  .blog-content {
    padding: 15px !important;
  }

  #newArrivalsCarousel .carousel-control-prev.custom-prev,
  #newArrivalsCarousel .carousel-control-next.custom-next,
  #featuredProductsCarousel .carousel-control-prev.custom-prev,
  #featuredProductsCarousel .carousel-control-next.custom-next {
    display: none;
  }

  #quickAddModal .modal-body {
    padding: 1rem;
  }

  #quickAddModal .modal-body .row {
    flex-direction: column;
    text-align: center;
  }

  #quickAddModal .modal-body img {
    margin-bottom: 1rem;
  }

  #bannerCarousel {
    height: 200px;
  }

  #bannerCarousel .carousel-item img {
    height: 200px;
  }
}

/* Quick View Modal Styles */
#quickViewModal .modal-content {
  border-radius: 10px;
}

#quickViewModal .modal-header {
  border-bottom: none;
}

#quickViewModal .modal-body {
  padding: 2rem;
}

#quickViewModal .modal-body h6 {
  font-weight: 600;
  margin-bottom: 1rem;
}

#quickViewModal .modal-body .color-dot {
  display: inline-block;
  width: 15px;
  height: 15px;
  border-radius: 50%;
  border: 1px solid #eee;
  margin-right: 5px;
  vertical-align: middle;
}

#quickViewModal .modal-body .form-control {
  max-width: 100px;
}
</style>