<template>
  <main>
    <!-- Truyền trạng thái user xuống HeaderLayout qua props -->
    <HeaderLayout v-if="!isAuthPage" :user="user" />
    <RouterView/>
    <FooterLayout v-if="!isAuthPage"/>
    
    <!-- Chat Widget cho khách hàng -->
    <CustomerChat v-if="!isAuthPage && user" :customerId="user.id" :customerName="user.name" />
  </main>
</template>

<script setup>
import FooterLayout from './layout/FooterLayout.vue';
import HeaderLayout from './layout/HeaderLayout.vue';
import CustomerChat from './components/CustomerChat.vue';
import { computed, onMounted, ref, onBeforeUnmount } from 'vue';
import { useRoute } from 'vue-router';
import { jwtDecode } from 'jwt-decode';
import { useCartStore } from '@/stores/cart'

const route = useRoute();
const isAuthPage = computed(() => ['/login', '/register', '/oauth2-redirect'].includes(route.path));

const user = ref(null);
const cartStore = useCartStore();

const checkLoginStatus = async  () => {
  const token = localStorage.getItem("authToken");
  if (!token) {
    user.value = null;
    return;
  }
  try {
    const decodedToken = jwtDecode(token);
    if (decodedToken.exp * 1000 > Date.now()) {
      user.value = {
        id: decodedToken.userId || decodedToken.sub, // Sử dụng userId nếu có, không thì dùng sub
        name: decodedToken.name || decodedToken.sub,
        image: decodedToken.image,
      };
      console.log("Cập nhật thành công user:", user.value); // Thêm log để kiểm tra
       // await cartStore.fetchCart();
    } else {
      localStorage.removeItem("authToken");
      user.value = null;
    }
  } catch (error) {
    console.error("Lỗi khi giải mã token:", error); // Thêm log lỗi
    localStorage.removeItem("authToken");
    user.value = null;
  }
};
// Hàm đồng bộ localStorage lên backend
async function syncLocalCartToBackend() {
  const localCart = JSON.parse(localStorage.getItem('cartItems') || '[]')
  for (const item of localCart) {
    await cartStore.addToCart(item, item.quantity)
  }
  localStorage.removeItem('cartItems')
  await cartStore.fetchCart()
}

const handleAuthChange = () => {
  console.log("Sự kiện auth-change từ một component khác được phát hiện.");
  checkLoginStatus();
}

onMounted(async () => {
  // Lắng nghe sự kiện từ các component khác (như Login.vue)
  window.addEventListener('auth-change', handleAuthChange);
  const urlParams = new URLSearchParams(window.location.search);
  const tokenFromUrl = urlParams.get('token');
  if (tokenFromUrl) {
    console.log("Phát hiện token từ URL (Google Login)");
    localStorage.setItem('authToken', tokenFromUrl);
    window.history.replaceState({}, document.title, window.location.pathname);
    await syncLocalCartToBackend();
    checkLoginStatus();
  } else {
    // === LUỒNG TẢI TRANG BÌNH THƯỜNG ===
    console.log("Không có token trên URL, kiểm tra đăng nhập từ phiên trước.");
    checkLoginStatus();
  }
});

onBeforeUnmount(() => {
  window.removeEventListener('auth-change', handleAuthChange);
});
</script>