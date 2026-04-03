<script setup>
import { computed, ref, onMounted, onBeforeUnmount } from "vue";
import { useStore } from "vuex";
import { useRoute, useRouter } from "vue-router";
import { jwtDecode } from "jwt-decode";
import Breadcrumbs from "../Breadcrumbs.vue";


const isAuthenticated = ref(false);
const userName = ref(null);
const userImage = ref(null);
const showUserMenu = ref(false);
const router = useRouter();

const checkAuthStatus = () => {
  const token = localStorage.getItem('authToken');
  if (!token) {
    isAuthenticated.value = false;
    return;
  }
  try {
    const decoded = jwtDecode(token);
    const currentTime = Date.now() / 1000;
    if (decoded.exp < currentTime) {
      localStorage.removeItem('authToken');
      isAuthenticated.value = false;
    } else {
      isAuthenticated.value = true;
      userName.value = decoded.sub;
      userImage.value = decoded.image;
    }
  } catch (error) {
    console.error("Lỗi giải mã token:", error);
    localStorage.removeItem('authToken');
    isAuthenticated.value = false;
  }
};

checkAuthStatus();

const handleLogout = () => {
  console.log("đăng xuất")
  localStorage.removeItem('authToken');
  isAuthenticated.value = false;
  userName.value = null;
  userImage.value = null;
  router.push({ name: 'Signin' });
};

// --- Logic có sẵn của bạn ---
const store = useStore();
const isRTL = computed(() => store.state.isRTL);
const route = useRoute();

const showMenu = ref(false);
const currentRouteName = computed(() => route.name);
const currentDirectory = computed(() => {
  let dir = route.path.split("/")[1];
  return dir.charAt(0).toUpperCase() + dir.slice(1);
});

const minimizeSidebar = () => store.commit("sidebarMinimize");
const toggleConfigurator = () => store.commit("toggleConfigurator");

// Hàm này bây giờ sẽ được sử dụng trong template
const closeMenu = () => {
  setTimeout(() => {
    showMenu.value = false;
  }, 100);
};

onMounted(() => {
  window.addEventListener('auth-change', checkAuthStatus);
  // Đồng thời cũng chạy hàm này một lần khi tải trang
  checkAuthStatus();
});
onBeforeUnmount(() => {
  window.removeEventListener('auth-change', checkAuthStatus);
});


</script>
<template>
  <nav class="navbar navbar-main navbar-expand-lg px-0 mx-4 shadow-none border-radius-xl"
    :class="isRTL ? 'top-0 position-sticky z-index-sticky' : ''" v-bind="$attrs" id="navbarBlur" data-scroll="true">
    <div class="px-3 py-1 container-fluid">
      <breadcrumbs :current-page="currentRouteName" :current-directory="currentDirectory" />

      <div class="mt-2 collapse navbar-collapse mt-sm-0 me-md-0 me-sm-4" :class="isRTL ? 'px-0' : 'me-sm-4'"
        id="navbar">
        <div class="pe-md-3 d-flex align-items-center" :class="isRTL ? 'me-md-auto' : 'ms-md-auto'">
        </div>
        <ul class="navbar-nav justify-content-end">

          <!-- HIỂN THỊ KHI CHƯA ĐĂNG NHẬP -->
          <li v-if="!isAuthenticated" class="nav-item d-flex align-items-center">
            <router-link :to="{ name: 'Signin' }" class="px-0 nav-link font-weight-bold text-white">
              <i class="fa fa-user" :class="isRTL ? 'ms-sm-2' : 'me-sm-2'"></i>
              <span class="d-sm-inline d-none">Đăng nhập</span>
            </router-link>
          </li>
          <li v-else class="nav-item dropdown d-flex align-items-center pe-2">
            <!-- Ảnh đại diện và tên -->
            <a href="#" class="nav-link text-white font-weight-bold p-0 dropdown-toggle" id="userProfileDropdown"
              aria-expanded="false" @click.prevent="showUserMenu = !showUserMenu"
              >

              <img v-if="userImage" :src="userImage" class="avatar avatar-sm rounded-circle me-1" alt="user-avatar" />
              <i v-else class="fa fa-user-circle text-white me-1 fs-5"></i>
              <span class="d-none d-sm-inline-block">{{ userName }}</span>
            </a>

            <ul class="dropdown-menu dropdown-menu-end px-2 py-3 me-sm-n4" :class="{ 'show': showUserMenu }"
              aria-labelledby="userProfileDropdown">
              <li>
                <a class="dropdown-item border-radius-md" href="#" @click="handleLogout">
                  <div class="d-flex py-1">
                    <div class="d-flex flex-column justify-content-center">
                      <h6 class="text-sm font-weight-normal mb-0">
                        <i class="fa fa-sign-out-alt me-1"></i>
                        Đăng xuất
                      </h6>
                    </div>
                  </div>
                </a>
              </li>
            </ul>
          </li>



          <li class="nav-item d-xl-none ps-3 d-flex align-items-center">
            <a href="#" @click="minimizeSidebar" class="p-0 nav-link text-white" id="iconNavbarSidenav">
              <div class="sidenav-toggler-inner">
                <i class="sidenav-toggler-line bg-white"></i>
                <i class="sidenav-toggler-line bg-white"></i>
                <i class="sidenav-toggler-line bg-white"></i>
              </div>
            </a>
          </li>
          <li class="px-3 nav-item d-flex align-items-center">
            <a class="p-0 nav-link text-white" @click="toggleConfigurator">
              <i class="cursor-pointer fa fa-cog fixed-plugin-button-nav"></i>
            </a>
          </li>
          <li class="nav-item dropdown d-flex align-items-center" :class="isRTL ? 'ps-2' : 'pe-2'">
            <a href="#" class="p-0 nav-link text-white" :class="[showMenu ? 'show' : '']" id="dropdownMenuButton"
              data-bs-toggle="dropdown" aria-expanded="false" @click="showMenu = !showMenu" @blur="closeMenu">
              <i class="cursor-pointer fa fa-bell"></i>
            </a>
            <ul class="px-2 py-3 dropdown-menu dropdown-menu-end me-sm-n4" :class="showMenu ? 'show' : ''"
              aria-labelledby="dropdownMenuButton">
              <li class="mb-2">
                <a class="dropdown-item border-radius-md" href="javascript:;">
                  <div class="py-1 d-flex">
                    <h6 class="mb-1 text-sm font-weight-normal">Chưa có thông báo mới</h6>
                  </div>
                </a>
              </li>
            </ul>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>