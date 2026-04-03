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
            <img src="../assets/slide3.jpg" class="d-block w-100" alt="..." />
            <div class="carousel-caption d-none d-md-block">
              <h5>Sản phẩm mới</h5>
              <p>Khám phá những mẫu áo polo mới nhất tại Polo Haven.</p>
            </div>
          </div>
          <div class="carousel-item">
            <img src="../assets/slide2.png" class="d-block w-100" alt="..." />
            <div class="carousel-caption d-none d-md-block">
              <h5>Ưu đãi đặc biệt</h5>
              <p>Giảm giá lên đến 30% cho tất cả sản phẩm!</p>
            </div>
          </div>
          <div class="carousel-item">
            <img src="../assets/slide3.jpg" class="d-block w-100" alt="..." />
            <div class="carousel-caption d-none d-md-block">
              <h5>Bộ sưu tập đa dạng</h5>
              <p>Chọn ngay chiếc áo polo phù hợp với bạn.</p>
            </div>
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

    <!-- Main Content -->
    <section class="main-content py-5">
      <div class="container">
        <div class="row g-4">
          <!-- Filters (Left Sidebar) -->
          <div class="col-12 col-md-3">
            <div class="filters-section bg-light p-4 rounded">
              <h3 class="fs-5 fw-semibold mb-3">Bộ lọc</h3>

              <!-- Color Filter -->
              <div class="mb-4">
                <h4 class="fs-6 fw-semibold mb-2">Màu sắc</h4>
                
                <!-- Danh sách tất cả màu để chọn -->
                <div class="d-flex flex-wrap gap-2 align-items-center">
                  <div
                    v-for="(color, idx) in colorOptions"
                    :key="color.id"
                    @click="toggleMauSac(color.id)"
                    @keydown.enter.prevent="toggleMauSac(color.id)"
                    @keydown.space.prevent="toggleMauSac(color.id)"
                    role="button"
                    :aria-pressed="filters.mauSacIds.includes(color.id)"
                    :aria-label="`Màu: ${color.ten || color.hex || color.maMauSac}`"
                    tabindex="0"
                    class="color-filter-rect d-inline-flex align-items-center justify-content-center"
                    :class="[colorPatternClass(color, idx), { 'active': filters.mauSacIds.includes(color.id), 'selected': filters.mauSacIds.includes(color.id) }]"
                    :style="{ borderColor: color.hex || color.ten, backgroundColor: color.hex || color.ten }"
                  >
                    <span class="rect-label" :style="{ color: getContrastColor(color.hex || color.ten) }">{{ color.name || color.hex || color.ten }}</span>
                    <span v-if="filters.mauSacIds.includes(color.id)" class="selected-checkmark">✓</span>
                  </div>
                </div>
              </div>

              <!-- Size Filter -->
              <div class="mb-4">
                <h4 class="fs-6 fw-semibold mb-2">Kích thước</h4>
                <div class="d-flex flex-wrap gap-2">
                  <button
                    v-for="size in sizeOptions"
                    :key="size.id"
                    @click="toggleKichThuoc(size.id)"
                    :class="['btn btn-outline-secondary btn-sm', { 'active': filters.kichThuocIds.includes(size.id), 'btn-size-active': filters.kichThuocIds.includes(size.id) }]"
                    :aria-pressed="filters.kichThuocIds.includes(size.id)"
                    :title="filters.kichThuocIds.includes(size.id) ? 'Đang lọc: ' + (size.label || size.size) : 'Chọn để lọc ' + (size.label || size.size)"
                  >
                    <span class="size-label">{{ size.label || size.size }}</span>
                  </button>
                </div>
              </div>

              <!-- Price Filter -->
              <div class="mb-4">
                <h4 class="fs-6 fw-semibold mb-2">Giá tiền</h4>
                <div class="d-flex flex-column gap-2">
                  <div class="form-check">
                    <input class="form-check-input" type="radio" name="priceRange" id="price0-100k" @change="setPriceRange(0, 100000)" />
                    <label class="form-check-label" for="price0-100k">Dưới 100.000đ</label>
                  </div>
                  <div class="form-check">
                    <input class="form-check-input" type="radio" name="priceRange" id="price100-500k" @change="setPriceRange(100000, 500000)" />
                    <label class="form-check-label" for="price100-500k">100.000đ - 500.000đ</label>
                  </div>
                  <div class="form-check">
                    <input class="form-check-input" type="radio" name="priceRange" id="price500-1000k" @change="setPriceRange(500000, 1000000)" />
                    <label class="form-check-label" for="price500-1000k">500.000đ - 1.000.000đ</label>
                  </div>
                  <div class="form-check">
                    <input class="form-check-input" type="radio" name="priceRange" id="price1000k-10000k" @change="setPriceRange(1000000, 10000000)" />
                    <label class="form-check-label" for="price500-1000k">Trên 1.000.000đ</label>
                  </div>
                </div>
              </div>

              <!-- Material Filter -->
              <div class="mb-4">
                <h4 class="fs-6 fw-semibold mb-2">Chất liệu</h4>
                <div class="d-flex flex-wrap gap-2">
                  <button
                    v-for="material in materialOptions"
                    :key="material.id"
                    @click="toggleChatLieu(material.id)"
                    class="btn btn-outline-secondary btn-sm material-filter-btn"
                    :class="{ 'active': filters.chatLieuIds.includes(material.id) }"
                  >
                    {{ material.ten }}
                  </button>
                </div>
              </div>

              <!-- Clear Filters Button -->
              <div class="mb-4">
                <button @click="clearAllFilters" class="btn btn-outline-danger w-100">
                  Xóa tất cả bộ lọc
                </button>
              </div>
            </div>
          </div>

          <!-- Product List (Right Content) -->
          <div class="col-12 col-md-9">
            <div class="section-header text-center mb-5 animate__animated animate__fadeIn">
              <h2 class="section-title display-5 fw-bold">Danh sách áo polo</h2>
              <p class="section-subtitle text-muted fs-5">Khám phá các mẫu áo polo đa dạng</p>
            </div>
            <div class="row g-4">
              <div v-for="product in paginatedProducts" :key="product.id" class="col-12 col-sm-6 col-md-4">
                <div class="product-card card h-100 shadow-sm cursor-pointer animate__animated animate__zoomIn">
                  <div class="product-image position-relative overflow-hidden" style="height: 250px;">
                    <img v-if="product.imageList && product.imageList.length > 0" :src="product.imageList[0] || '/src/assets/ao1.webp'"
                      :alt="product.tenSanPham" class="card-img-top object-fit-cover h-100 w-100" />
                    <img v-else src="" :alt="product.tenSanPham" class="card-img-top object-fit-cover h-100 w-100" />

                    <div v-if="product.phanTramGiam > 0"
                      class="product-badge position-absolute top-0 start-0 bg-danger text-white px-3 py-1 rounded-pill fs-6">
                      Sale
                    </div>
                  </div>
                  <div class="product-info card-body">
                    <h3 class="product-name card-title fs-6 fw-semibold">
                      {{ product.tenSanPham }}
                      <span v-if="product.phanTramGiam > 0" class="text-danger fw-bold ms-2">
                        -{{ product.phanTramGiam }}%
                      </span>
                    </h3>
                    <div class="product-details mb-2">
                      <p class="mb-1">
                        <small class="text-muted">Màu sắc:</small>
                        <span v-for="(color, cIdx) in product.mauSacList" :key="cIdx" class="color-dot ms-1"
                          :style="{ backgroundColor: color.maMauSac }">
                        </span>
                      </p>
                    </div>
                    <div class="product-details mb-2">
                      <p class="mb-1">
                        <small class="text-muted">Size:</small>
                        {{product.kichThuocList.map(item =>
                          item.size).join(', ')}}
                      </p>
                    </div>
                    <div class="product-price fs-5 fw-semibold">
                      <span class="current-price text-success">{{ formatPriceRange(product.khoangGia) }}</span>
                    </div>
                  </div>
                  <div
                    class="product-overlay d-flex align-items-center justify-content-center position-absolute top-0 start-0 w-100 h-100 bg-dark bg-opacity-50 transition-opacity">
                    <router-link :to="`/product/${product.id}`" class="btn btn-success rounded-pill shadow">
                      Xem chi tiết
                    </router-link>
                  </div>
                </div>
              </div>
            </div>

            <!-- Pagination -->
            <nav aria-label="Page navigation" class="mt-4">
              <ul class="pagination justify-content-center custom-pagination">
                <li class="page-item" :class="{ 'disabled': currentPage === 1 }">
                  <a class="page-link" href="#" @click.prevent="currentPage--" aria-label="Previous">
                    <span aria-hidden="true">&laquo;</span>
                  </a>
                </li>
                <li v-for="page in totalPages" :key="page" class="page-item"
                  :class="{ 'active': currentPage === page }">
                  <a class="page-link" href="#" @click.prevent="currentPage = page">{{ page }}</a>
                </li>
                <li class="page-item" :class="{ 'disabled': currentPage === totalPages }">
                  <a class="page-link" href="#" @click.prevent="currentPage++" aria-label="Next">
                    <span aria-hidden="true">&raquo;</span>
                  </a>
                </li>
              </ul>
            </nav>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import 'bootstrap/dist/css/bootstrap.min.css'
import { getListSanPhamFill, getAllMauSac, getAllKichThuoc, getAllChatLieu } from '@/services/sanPham'

const products = ref([])
const currentPage = ref(1)
const itemsPerPage = 6

const filters = ref({
  mauSacIds: [],
  kichThuocIds: [],
  thuongHieuIds: [],
  giaMin: null,
  giaMax: null,
  chatLieuIds: [],
  hinhAnhIds: [],
  tenSanPham: '',
  tenThuongHieu: '',
  tenChatLieu: '',
  tenMauSac: '',
  tenKichThuoc: '',
  tenHinhAnh: '',
});

const paginatedProducts = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return products.value.slice(start, end)
})

const totalPages = computed(() => Math.ceil(products.value.length / itemsPerPage))

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

// Hàm chuyển đổi tên màu thành mã màu hex
const getColorHex = (colorName) => {
  const colorMap = {
    'Yellow': '#FFFF00',
    'Red': '#FF0000',
    'Blue': '#0000FF',
    'Green': '#008000',
    'Black': '#000000',
    'White': '#FFFFFF',
    'Orange': '#FFA500',
    'Purple': '#800080',
    'Pink': '#FFC0CB',
    'Brown': '#A52A2A',
    'Gray': '#808080',
    'Navy': '#000080',
    'Maroon': '#800000',
    'Olive': '#808000',
    'Lime': '#00FF00',
    'Aqua': '#00FFFF',
    'Teal': '#008080',
    'Silver': '#C0C0C0',
    'Gold': '#FFD700',
    'Coral': '#FF7F50',
    'Indigo': '#4B0082',
    'Violet': '#EE82EE',
    'Cyan': '#00FFFF',
    'Magenta': '#FF00FF'
  }
  return colorMap[colorName] || '#CCCCCC'
}

// Khai báo các options cho màu sắc, kích thước và chất liệu
const colorOptions = ref([]);
const sizeOptions = ref([]);
const materialOptions = ref([]);

// Patterns to help color-blind users distinguish colors
const patternLegend = ref([
  { label: 'Một gạch ngang', cls: 'pattern-stripe', color: '#000000' },
  { label: 'Chấm nhỏ', cls: 'pattern-dots', color: '#FFFFFF' },
  { label: 'Chéo', cls: 'pattern-diag', color: '#777777' }
]);

// Assign pattern class based on index to distribute patterns
function colorPatternClass(color, idx) {
  const arr = ['pattern-stripe', 'pattern-dots', 'pattern-diag'];
  return arr[idx % arr.length];
}

// Hàm lấy dữ liệu màu sắc từ API
const fetchMauSac = async () => {
  try {
    const response = await getAllMauSac();
    if (response.data) {
      colorOptions.value = response.data.map(item => ({
        id: item.id,
        ten: item.tenMau || item.ten || '#CCCCCC',
        hex: item.maMauSac || getColorHex(item.tenMau || item.ten),
        name: item.tenMau || item.ten
      }));
    }
  } catch (error) {
    console.error("Lỗi khi lấy danh sách màu sắc:", error);
  }
};

// Hàm lấy dữ liệu kích thước từ API
const fetchKichThuoc = async () => {
  try {
    const response = await getAllKichThuoc();
    if (response.data) {
      sizeOptions.value = response.data.map(item => ({
        id: item.id,
        label: item.size || item.tenSize
      }));
    }
  } catch (error) {
    console.error("Lỗi khi lấy danh sách kích thước:", error);
  }
};

// Hàm lấy dữ liệu chất liệu từ API
const fetchChatLieu = async () => {
  try {
    const response = await getAllChatLieu();
    if (response.data) {
      materialOptions.value = response.data.map(item => ({
        id: item.id,
        ten: item.tenChatLieu
      }));
    }
  } catch (error) {
    console.error("Lỗi khi lấy danh sách chất liệu:", error);
  }
};

function toggleMauSac(id) {
  const idx = filters.value.mauSacIds.indexOf(id);
  if (idx === -1) {
    filters.value.mauSacIds.push(id);
  } else {
    filters.value.mauSacIds.splice(idx, 1);
  }
  fetchProduct();
}

const toggleKichThuoc = (id) => {
  const idx = filters.value.kichThuocIds.indexOf(id);
  if (idx === -1) {
    filters.value.kichThuocIds.push(id);
  } else {
    filters.value.kichThuocIds.splice(idx, 1);
  }
  fetchProduct();
}

function setPriceRange(min, max) {
  filters.value.giaMin = min;
  filters.value.giaMax = max;
  fetchProduct();
}

function toggleChatLieu(id) {
  if (filters.value.chatLieuIds.includes(id)) {
    filters.value.chatLieuIds = [];
  } else {
    filters.value.chatLieuIds = [id];
  }
  fetchProduct();
}

function clearAllFilters() {
  filters.value = {
    mauSacIds: [],
    kichThuocIds: [],
    thuongHieuIds: [],
    giaMin: null,
    giaMax: null,
    chatLieuIds: [],
  };
  
  const priceRadios = document.querySelectorAll('input[name="priceRange"]');
  priceRadios.forEach(radio => {
    radio.checked = false;
  });
  
  fetchProduct();
}

// Hàm xóa tất cả màu đã chọn
function clearSelectedColors() {
  filters.value.mauSacIds = [];
  fetchProduct();
}

// Hàm lấy tên màu từ ID
function getSelectedColorName(colorId) {
  const color = colorOptions.value.find(c => c.id === colorId);
  return color ? (color.name || color.ten || color.hex) : 'Unknown';
}

// Hàm lấy mã màu hex từ ID
function getSelectedColorHex(colorId) {
  const color = colorOptions.value.find(c => c.id === colorId);
  return color ? (color.hex || getColorHex(color.ten)) : '#CCCCCC';
}

// Hàm tính màu tương phản cho text
function getContrastColor(hexColor) {
  if (!hexColor || !hexColor.startsWith('#')) {
    return '#000000'; // Default to black for invalid colors
  }
  
  // Remove # if present
  const hex = hexColor.replace('#', '');
  
  // Convert to RGB
  const r = parseInt(hex.substr(0, 2), 16);
  const g = parseInt(hex.substr(2, 2), 16);
  const b = parseInt(hex.substr(4, 2), 16);
  
  // Calculate luminance
  const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
  
  // Return black for light colors, white for dark colors
  return luminance > 0.5 ? '#000000' : '#FFFFFF';
}

const fetchProduct = async () => {
  try {
    console.log("=== FETCH PRODUCT START ===");
    console.log("Raw filters (ref):", filters); 
    console.log("filters.value (reactive):", filters.value);
    console.log("Filters as JSON:", JSON.stringify(filters.value));

    const response = await getListSanPhamFill(filters.value);

    console.log(">>> Response status:", response.status);
    console.log(">>> Response data:", response.data);

    products.value = response.data;
    currentPage.value = 1;

    console.log("Products state updated:", products.value);
    console.log("=== FETCH PRODUCT END ===");
  } catch (error) {
    console.error("=== FETCH PRODUCT ERROR ===");
    if (error.response) {
      console.error("Status:", error.response.status);
      console.error("Response data:", error.response.data);
      console.error("Headers:", error.response.headers);
    } else {
      console.error("Error message:", error.message);
    }
  }
};

// Hàm khởi tạo dữ liệu ban đầu
const initializeData = async () => {
  try {
    await Promise.all([
      fetchMauSac(),
      fetchKichThuoc(),
      fetchChatLieu()
    ]);
    
    await fetchProduct();
  } catch (error) {
    console.error('Error initializing data:', error);
  }
}

onMounted(() => {
  initializeData()
})
</script>

<style scoped>
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

.bg-light {
  background-color: #f8f9fa;
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

/* Filters Section */
.filters-section {
  border-radius: 10px;
  box-shadow: 0 6px 24px rgba(27,31,35,0.04);
  border: 1px solid rgba(0,0,0,0.04);
}

.filters-section h3 {
  color: #333;
  display: flex;
  align-items: center;
  gap: 10px;
}

.filters-section h4 {
  color: #555;
}

.filters-section .form-check-label {
  cursor: pointer;
  color: #555;
}

.filters-section .btn-outline-secondary {
  border-color: #ddd;
  color: #333;
}

.filters-section .btn-outline-secondary:hover {
  background-color: #e9ecef;
}

/* Color-blind patterns and accessibility */
.color-filter-rect {
  min-width: 84px;
  height: 36px;
  padding: 0 12px;
  border-radius: 8px;
  border: 2px solid rgba(0,0,0,0.06);
  background: rgba(0,0,0,0.06);
  position: relative;
  cursor: pointer;
  user-select: none;
  gap: 8px;
  font-weight: 600;
  font-size: 0.95rem;
  transition: transform 0.12s ease, box-shadow 0.12s ease, border-color 0.12s ease;
}

.color-filter-rect:focus {
  outline: 3px solid #ffd54f;
  outline-offset: 3px;
}

/* Enhanced active state for better visibility */
.color-filter-rect.active {
  border: 3px solid #4ba27b !important;
  box-shadow: 0 8px 20px rgba(75, 162, 123, 0.3), 0 0 0 2px rgba(75, 162, 123, 0.2);
  transform: translateY(-3px) scale(1.05);
  background: rgba(75, 162, 123, 0.1);
}

/* Add checkmark for selected colors */
.color-filter-rect.active::before {
  content: '✓';
  position: absolute;
  top: -8px;
  right: -8px;
  width: 20px;
  height: 20px;
  background: #4ba27b;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  box-shadow: 0 2px 6px rgba(75, 162, 123, 0.4);
  z-index: 10;
}

/* Hover effect */
.color-filter-rect:hover:not(.active) {
  transform: translateY(-1px);
  border-color: #4ba27b;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.color-legend {
  font-size: 0.9rem;
}

.legend-item .legend-dot {
  width: 40px;
  height: 22px;
  display: inline-block;
  border-radius: 6px;
  border: 1px solid rgba(0,0,0,0.08);
}

.rect-label {
  display: inline-block;
  color: inherit;
}

/* Modernize filters box */
.filters-section {
  box-shadow: 0 6px 24px rgba(27,31,35,0.04);
  border: 1px solid rgba(0,0,0,0.04);
}

.filters-section h3 {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* Product List Section */
.product-card {
  border: none;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
  transition: transform 0.3s ease, box-shadow 0.3s ease, opacity 0.3s ease;
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
}

.product-card:hover .product-overlay {
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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.product-overlay .btn {
  padding: 8px 20px;
  font-weight: 600;
  background: linear-gradient(135deg, #4ba27b 0%, #66ea8b 100%);
  color: white;
  border: none;
}

.product-overlay .btn:hover {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  box-shadow: 0 6px 16px rgba(102, 234, 139, 0.8);
}

/* Pagination */
.custom-pagination .page-item .page-link {
  color: #4ba27b;
  border: none;
  background: none;
  font-weight: 600;
  font-size: 1.1rem;
  margin: 0 4px;
  border-radius: 50% !important;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s, color 0.2s, box-shadow 0.2s;
}

.custom-pagination .page-item.active .page-link {
  background: linear-gradient(135deg, #4ba27b 0%, #66ea8b 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(75, 162, 123, 0.15);
}

.custom-pagination .page-item .page-link:hover,
.custom-pagination .page-item:not(.active):not(.disabled) .page-link:focus {
  background: #e6f9ef;
  color: #4ba27b;
}

.custom-pagination .page-item.disabled .page-link {
  color: #ccc;
  background: none;
  cursor: not-allowed;
  pointer-events: none;
}

/* Material Filter Styles */
.material-filter-btn {
  transition: all 0.3s ease;
  border-radius: 20px;
  font-weight: 500;
  padding: 6px 12px;
  font-size: 0.875rem;
}

.material-filter-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.material-filter-btn.active {
  background: linear-gradient(135deg, #4ba27b 0%, #66ea8b 100%);
  border-color: #4ba27b;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(75, 162, 123, 0.3);
}

/* Size button active state */
.btn-size-active {
  background: linear-gradient(135deg, #4ba27b 0%, #66ea8b 100%) !important;
  color: #fff !important;
  border-color: #4ba27b !important;
  box-shadow: 0 6px 18px rgba(75,162,123,0.18) !important;
  transform: translateY(-2px);
}

.btn-size-active::after {
  content: '✓';
  display: inline-block;
  margin-left: 8px;
  font-weight: 700;
}

.btn-size-active:focus {
  outline: 3px solid rgba(255,213,79,0.18);
  outline-offset: 3px;
}

/* Custom radio button styles */
.form-check-input[type="radio"] {
  border-color: #4ba27b;
}

.form-check-input[type="radio"]:checked {
  background-color: #4ba27b;
  border-color: #4ba27b;
}

.form-check-input[type="radio"]:focus {
  border-color: #66ea8b;
  box-shadow: 0 0 0 0.25rem rgba(75, 162, 123, 0.25);
}

.form-check-input[type="radio"]:checked:focus {
  border-color: #4ba27b;
  box-shadow: 0 0 0 0.25rem rgba(75, 162, 123, 0.25);
}

/* Responsive Adjustments */
@media (max-width: 992px) {
  .hero-section {
    height: 60vh;
    min-height: 400px;
  }

  .carousel-caption {
    padding-left: 10%;
    padding-right: 10%;
  }

  .row {
    flex-direction: column;
  }

  .col-md-3,
  .col-md-9 {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .homepage {
    padding-top: 100px;
  }

  .hero-section {
    height: 50vh;
    min-height: 350px;
  }

  .carousel-caption h5 {
    font-size: 2.2rem;
  }

  .carousel-caption p {
    font-size: 1rem;
  }

  .product-image {
    height: 200px;
  }

  .product-name {
    font-size: 1rem;
  }
}

@media (max-width: 576px) {
  .hero-section {
    min-height: 300px;
  }

  .carousel-caption h5 {
    font-size: 1.8rem;
  }

  .carousel-caption p {
    font-size: 0.9rem;
  }

  .product-image {
    height: 180px;
  }

  .product-info {
    padding: 1rem;
  }
}
</style>