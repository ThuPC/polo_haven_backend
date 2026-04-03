<template>
  <br><br>
  <header class="header">
    <nav class="navbar navbar-expand-lg navbar-light bg-light">
      <div class="container">
        <!-- Logo -->
        <router-link to="/" class="navbar-brand d-flex align-items-center">
          <span class="logo-text">Polo Haven</span>
        </router-link>

        <!-- Nút Toggler cho mobile -->
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav"
          aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
          <span class="navbar-toggler-icon"></span>
        </button>

        <!-- Menu chính -->
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav mx-auto">
            <li class="nav-item">
              <router-link to="/" class="nav-link">Trang chủ</router-link>
            </li>
            <li class="nav-item">
              <router-link to="/products" class="nav-link">Sản phẩm</router-link>
            </li>
            <li class="nav-item">
              <router-link to="/news" class="nav-link">Tin tức</router-link>
            </li>
            <li class="nav-item">
              <router-link to="/about" class="nav-link">Giới thiệu</router-link>
            </li>
            <li class="nav-item">
              <router-link to="/contact" class="nav-link">Liên hệ</router-link>
            </li>
            <li class="nav-item">
              <router-link to="/tracking" class="nav-link">Theo dõi hóa đơn</router-link>
            </li>
          </ul>

          <!-- Các icon chức năng bên phải -->
          <div class="d-flex align-items-center">
            <!-- Thanh tìm kiếm -->
            <div class="search-input-group me-3">
              <input 
                type="text" 
                class="form-control search-input" 
                placeholder="Tìm kiếm sản phẩm..."
                v-model="searchKeyword"
                @input="handleSearchInput"
                @keyup.enter="viewAllResults"
              >
              <button class="btn search-btn" @click="viewAllResults"><i class="bi bi-search"></i></button>
            </div>

            <!-- Icon giỏ hàng -->
            <div class="position-relative me-3">
              <router-link to="/cart" class="nav-link position-relative">
                <i class="bi bi-cart-fill"></i>
                <span v-if="cartCount > 0" class="cart-count badge bg-success">{{ cartCount }}</span>
              </router-link>
            </div>

            <!-- Menu người dùng -->
            <div class="user-dropdown-wrapper">
              <div class="nav-link user-dropdown" @click="showUserMenu = !showUserMenu">
                <template v-if="isAuthenticated && user">
                  <img :src="user.image" alt="User Avatar" class="user-avatar" />
                </template>
                <template v-else>
                  <i class="bi bi-person-circle"></i>
                </template>
              </div>
              <div v-if="showUserMenu" class="dropdown-menu user-menu">
                <template v-if="isAuthenticated && user">
                  <div class="dropdown-header">
                    Tài khoản:<br><strong>{{ user.name }}</strong>
                  </div>
                  <div class="dropdown-divider"></div>
                  <router-link to="/profile" class="dropdown-item" @click="showUserMenu = false">
                    <i class="bi bi-person-badge me-2"></i>Hồ sơ của tôi
                  </router-link>
                  <router-link to="/tracking" class="dropdown-item" @click="showUserMenu = false">
                    <i class="bi bi-box-seam me-2"></i>Đơn hàng
                  </router-link>
                  <div class="dropdown-divider"></div>
                  <a href="#" class="dropdown-item text-danger" @click.prevent="logout">
                    <i class="bi bi-box-arrow-right me-2"></i>Đăng xuất
                  </a>
                </template>
                <template v-else>
                  <router-link to="/register" class="dropdown-item" @click="showUserMenu = false">
                    <i class="bi bi-person-plus-fill me-2"></i>Đăng ký
                  </router-link>
                  <router-link to="/login" class="dropdown-item" @click="showUserMenu = false">
                    <i class="bi bi-box-arrow-in-right me-2"></i>Đăng nhập
                  </router-link>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>
    </nav>
  </header>

  <!-- Component Modal tìm kiếm -->
  <TimKiem 
    v-if="isModalVisible" 
    :results="searchResults" 
    :keyword="searchKeyword" 
    @close="closeSearchModal"
    @view-all="viewAllResults" 
  />
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { useCartStore } from '@/stores/cart';
import TimKiem from '@/views/TimKiem.vue'; // Giả sử component Modal của bạn là TimKiem.vue
import { getListSanPhamFill } from '@/services/sanPham';

// --- KHỞI TẠO CÁC STORE VÀ ROUTER ---
const cartStore = useCartStore();
const router = useRouter();

// --- PROPS ---
const props = defineProps({
  user: {
    type: Object,
    default: null
  }
});

// --- STATE CHO UI ---
const showUserMenu = ref(false);

// --- COMPUTED PROPERTIES ---
const cartCount = computed(() => cartStore.totalQuantity);
const isAuthenticated = computed(() => !!props.user);

// --- LOGIC XÁC THỰC VÀ MENU NGƯỜI DÙNG ---
const logout = () => {
  localStorage.removeItem("authToken");
  localStorage.removeItem("cart"); // Hoặc gọi cartStore.clearCart()
  window.dispatchEvent(new CustomEvent('auth-change'));
  showUserMenu.value = false;
  router.push('/login');
};

const handleClickOutside = (event) => {
  const dropdown = document.querySelector('.user-dropdown-wrapper');
  if (dropdown && !dropdown.contains(event.target)) {
    showUserMenu.value = false;
  }
};

// --- LOGIC TÌM KIẾM VÀ MODAL ---
const searchKeyword = ref('');
const searchResults = ref([]);
const isModalVisible = ref(false);
let debounceTimer = null;

// Hàm gọi API để lấy kết quả tìm kiếm nhanh
const fetchSearchResults = async () => {
  const keyword = searchKeyword.value.trim();
  if (keyword.length < 2) { // Chỉ tìm kiếm khi người dùng gõ ít nhất 2 ký tự
    searchResults.value = [];
    isModalVisible.value = false; // Ẩn modal nếu xóa hết chữ
    return;
  }
  try {
    const response = await getListSanPhamFill({
      keyword: keyword,
      limit: 5 // Chỉ lấy 5 kết quả top đầu
    });
    searchResults.value = response.data;
    isModalVisible.value = searchResults.value.length > 0; // Chỉ hiện modal nếu có kết quả
  } catch (error) {
    console.error("Lỗi khi tìm kiếm sản phẩm:", error);
    isModalVisible.value = false;
  }
};

// Sử dụng kỹ thuật debounce để tối ưu việc gọi API
const handleSearchInput = () => {
  clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    fetchSearchResults();
  }, 300); // Đợi 300ms sau khi người dùng ngừng gõ mới bắt đầu tìm kiếm
};

// Đóng Modal
const closeSearchModal = () => {
  isModalVisible.value = false;
};

// Chuyển đến trang sản phẩm với từ khóa tìm kiếm
const viewAllResults = () => {
  closeSearchModal(); // Đóng modal trước khi điều hướng
  const keyword = searchKeyword.value.trim();
  if (keyword) {
    router.push({
      path: '/products',
      query: { keyword: keyword }
    });
  }
};

// Đóng modal khi nhấn phím Escape
const handleEscapeKey = (event) => {
  if (event.key === 'Escape' && isModalVisible.value) {
    closeSearchModal();
  }
};

// --- LIFECYCLE HOOKS ---
onMounted(() => {
  document.addEventListener('click', handleClickOutside);
  document.addEventListener('keydown', handleEscapeKey);
});

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside);
  document.removeEventListener('keydown', handleEscapeKey);
  clearTimeout(debounceTimer); // Dọn dẹp timer khi component bị hủy
});
</script>

<style scoped>
/* Thêm một vài style cơ bản để giao diện đẹp hơn */
.navbar-brand .logo-text {
  font-weight: bold;
  font-size: 1.5rem;
}

.search-input-group {
  display: flex;
}
.search-input {
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
}
.search-btn {
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
  border-left: 0;
}

.cart-count {
  position: absolute;
  top: -5px;
  right: -10px;
  font-size: 0.75rem;
  line-height: 1;
  padding: .25em .5em;
  border-radius: 50%;
}

.user-dropdown-wrapper {
  position: relative;
}
.user-dropdown {
  cursor: pointer;
}
.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}
.user-menu {
  display: block; /* Ghi đè display: none của Bootstrap */
  position: absolute;
  right: 0;
  top: 100%;
  margin-top: 0.5rem;
  min-width: 220px;
}
</style>

<style scoped>
.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #ddd;
  cursor: pointer;
}

.user-dropdown-wrapper {
  position: relative;
  display: inline-block;
  margin-left: 1rem;
}

.user-dropdown {
  cursor: pointer;
}

.user-dropdown i {
  font-size: 1.8rem;
}

.dropdown-menu {
  display: block;
  /* v-if sẽ xử lý việc hiển thị/ẩn */
  position: absolute;
  right: 0;
  top: 130%;
  /* Điều chỉnh khoảng cách với avatar */
  background-color: white;
  border: 1px solid rgba(0, 0, 0, .15);
  border-radius: .5rem;
  box-shadow: 0 .5rem 1rem rgba(0, 0, 0, .175);
  z-index: 1000;
  min-width: 200px;
  padding: .5rem 0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  padding: .75rem 1rem;
  text-decoration: none;
  color: #333;
}

.dropdown-item:hover {
  background-color: #f8f9fa;
}

.dropdown-item i {
  font-size: 1rem;
}

.text-danger:hover {
  color: #fff !important;
  background-color: #dc3545;
}

.dropdown-header {
  padding: .75rem 1rem;
  color: #6c757d;
  font-size: 0.9rem;
}

.dropdown-divider {
  margin: .5rem 0;
}
</style>

<style scoped>
.user-dropdown-wrapper {
  position: relative;
}

.user-dropdown {
  cursor: pointer;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 1000;
}

.navbar {
  padding: 10px 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  background-color: #f5f7fa;
}

.navbar-brand .logo-text {
  font-size: 24px;
  font-weight: 700;
  color: #4ba27b;
}

.nav-link {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-left: 15px;
}

.nav-link:hover,
.nav-link.router-link-active {
  color: #4ba27b;
}

.dropdown-menu {
  border: none;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.dropdown-item {
  font-size: 15px;
}

.dropdown-item:hover {
  background-color: #f1f1f1;
}

.cart-count {
  position: absolute;
  top: -5px;
  right: -10px;
  font-size: 12px;
  padding: 2px 6px;
}

/* Search Input Group */
.search-input-group {
  display: flex;
  align-items: center;
  border: 1px solid #ced4da;
  border-radius: 25px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.search-input-group:focus-within {
  border-color: #32CD32;
  box-shadow: 0 0 0 0.25rem rgba(50, 205, 50, 0.25);
}

.search-input {
  border: none;
  padding: 8px 15px;
  font-size: 14px;
  outline: none;
  flex-grow: 1;
  min-width: 150px;
  /* Adjust as needed */
}

.search-btn {
  background-color: transparent;
  border: none;
  padding: 8px 12px;
  color: #333;
  cursor: pointer;
  transition: color 0.3s ease;
}

.search-btn:hover {
  color: #32CD32;
}

@media (max-width: 768px) {
  .navbar-nav {
    margin-top: 10px;
  }

  .nav-link {
    margin-left: 0;
    margin-bottom: 10px;
  }

  .d-flex {
    margin-top: 10px;
    flex-direction: column;
    align-items: flex-start !important;
  }

  .search-input-group {
    width: 100%;
    margin-bottom: 10px;
  }
}

.user-dropdown {
  position: relative;
}

.user-dropdown .bi-person-circle {
  font-size: 22px;
}

.user-menu {
  position: absolute;
  top: 120%;
  right: 0;
  min-width: 140px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  z-index: 100;
  padding: 8px 0;
  display: flex;
  flex-direction: column;
  animation: fadeIn 0.2s;
}

.user-menu .dropdown-item {
  padding: 8px 18px;
  color: #333;
  font-size: 15px;
  text-decoration: none;
  transition: background 0.2s, color 0.2s;
}

.user-menu .dropdown-item:hover {
  background: #f1f1f1;
  color: #4ba27b;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>