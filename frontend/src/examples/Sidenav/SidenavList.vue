<script setup>
import { ref, computed } from "vue";
import { useRoute } from "vue-router";
import { useStore } from "vuex";

import SidenavItem from "./SidenavItem.vue";
import SidenavCard from "./SidenavCard.vue";
// import PaymentCard from "./components/PaymentCard.vue";
// import InvoiceCard from "./components/InvoiceCard.vue";
// import BillingCard from "./components/BillingCard.vue";
// import TransactionCard from "./components/TransactionCard.vue";

const store = useStore();
const isRTL = computed(() => store.state.isRTL);
const route = useRoute();

// Trạng thái mở/đóng menu con
const openMenus = ref({});

// Kiểm tra route hiện tại để mở menu con tương ứng
const getRoute = () => {
  const routeArr = route.path.split("/");
  return routeArr[1];
};

// Hàm toggle menu con
const toggleMenu = (menuName) => {
  openMenus.value = {
    ...openMenus.value,
    [menuName]: !openMenus.value[menuName],
  };
};
</script>

<template>
  <div
    class="collapse navbar-collapse w-auto h-auto h-100"
    id="sidenav-collapse-main"
  >
    <ul class="navbar-nav">
      <!-- Dashboard -->
       <li class="nav-item">
        <sidenav-item to="/banHangTaiQuay" :class="getRoute() === 'trang-chu' ? 'active' : ''"
          :navText="isRTL ? 'Bán hàng' : 'Bán hàng'">
          <template v-slot:icon>
            <i class="ni ni-shop text-primary text-sm opacity-10"></i>
          </template>
        </sidenav-item>
      </li>

      <li class="nav-item">
        <sidenav-item
          to="/thong-ke"
          :class="getRoute() === 'thong-ke' ? 'active' : ''"
          :navText="isRTL ? 'Dashboard' : 'Thống kê'"
        >
          <template v-slot:icon>
            <!-- <i class="ni ni-tv-2 text-primary text-sm opacity-10"></i> -->
             <i class="ni ni-shop text-success text-sm opacity-10"></i> <!-- Cửa hàng -->
          </template>
        </sidenav-item>
      </li>

      <!-- Product -->
      <li class="nav-item">
        <div
          class="d-flex justify-content-between align-items-center nav-link ms-3"
          @click="toggleMenu('product')"
          style="cursor: pointer"
        >
          <span>
            <!-- <i class="ni ni-bag-17 text-warning text-sm opacity-10"></i> -->
             <i class="ni ni-archive-2 text-success text-sm opacity-10"></i> <!-- Kho -->
            Quản Lý Sản Phẩm
          </span>
          <i
            class="ni text-sm"
            :class="openMenus['product'] ? 'ni-bold-down' : 'ni-bold-right'"
          ></i>
        </div>
        <ul v-if="openMenus['product']" class="navbar-nav">
          <li class="nav-item">
            <sidenav-item to="/product/san-pham/hien-thi" navText="Sản Phẩm" />
          </li>
          <li class="nav-item">
            <sidenav-item to="/product/mau-sac/hien-thi" navText="Màu Sắc" />
          </li>
          <li class="nav-item">
            <sidenav-item to="/product/kich-co/hien-thi" navText="Kích Cỡ" />
          </li>
          <li class="nav-item">
            <sidenav-item
              to="/product/thuong-hieu/hien-thi"
              navText="Thương Hiệu"
            />
          </li>
          <li class="nav-item">
            <sidenav-item
              to="/product/chat-lieu/hien-thi"
              navText="Chất Liệu"
            />
          </li>
          <li class="nav-item">
            <sidenav-item to="/product/xuat-xu/hien-thi" navText="Xuất Xứ" />
          </li>
        </ul>
      </li>

      <!-- Billing -->
      <li class="nav-item">
        <sidenav-item
          to="/billing"
          :class="getRoute() === 'billing' ? 'active' : ''"
          :navText="isRTL ? 'Billing' : 'Hóa đơn'"
        >
          <template v-slot:icon>
            <!-- <i class="ni ni-credit-card text-success text-sm opacity-10"></i> -->
             <i class="ni ni-single-copy-04 text-success text-sm opacity-10"></i> <!-- Biểu tượng hóa đơn -->
             
          </template>
        </sidenav-item>
      </li>
       <!-- Billing -->
      <li class="nav-item">
        <sidenav-item
          to="/returnmanagement"
          :class="getRoute() === 'returnmanagement' ? 'active' : ''"
          :navText="isRTL ? 'ReturnManagement' : 'Quản lý trả hàng'"
        >
          <template v-slot:icon>
            <!-- <i class="ni ni-credit-card text-success text-sm opacity-10"></i> -->
             <i class="ni ni-single-copy-04 text-success text-sm opacity-10"></i> <!-- Biểu tượng hóa đơn -->
             
          </template>
        </sidenav-item>
      </li>

      <!-- Voucher -->
      <li class="nav-item">
        <div
          class="d-flex justify-content-between align-items-center nav-link ms-3"
          @click="toggleMenu('voucher')"
          style="cursor: pointer"
        >
          <span>
            <!-- <i class="ni ni-tag text-info text-sm opacity-10"></i> -->
             <i class="ni ni-tag text-success text-sm opacity-10"></i> <!-- Thẻ giảm giá -->
            Quản Lý Giảm Giá
          </span>
          <i
            class="ni text-sm"
            :class="openMenus['voucher'] ? 'ni-bold-down' : 'ni-bold-right'"
          ></i>
        </div>
        <ul v-if="openMenus['voucher']" class="navbar-nav">
          <li class="nav-item">
            <!--            <sidenav-item to="/voucher/discount" navText="Discount"/>-->
            <sidenav-item to="/giam-gia/voucher" navText="Giảm giá" />
          </li>
          <li class="nav-item">
            <!--            <sidenav-item to="/voucher/gift" navText="Gift"/>-->
            <sidenav-item to="/giam-gia/khuyen-mai" navText="Khuyến mãi" />
          </li>
        </ul>
      </li>

      <!-- Chat -->
      <li class="nav-item">
        <sidenav-item
          to="/chat"
          :class="getRoute() === 'chat' ? 'active' : ''"
          :navText="isRTL ? 'Chat' : 'Hỗ trợ khách hàng'"
        >
          <template v-slot:icon>
            <i class="ni ni-chat-round text-info text-sm opacity-10"></i>
          </template>
        </sidenav-item>
      </li>

      <!-- ACCOUNT PAGES -->
      <li class="mt-3 nav-item">
        <h6
          class="text-xs ps-4 text-uppercase font-weight-bolder opacity-6"
          :class="isRTL ? 'me-4' : 'ms-2'"
        >
          <!--          ACCOUNT PAGES-->
          Quản lý tài khoản
        </h6>
      </li>

      <!-- Profile -->
      <li class="nav-item">
        <div
          class="d-flex justify-content-between align-items-center nav-link ms-3"
          @click="toggleMenu('profile')"
          style="cursor: pointer"
        >
          <span>
            <i class="ni ni-single-02 text-dark text-sm opacity-10"></i>
            Hồ Sơ
          </span>
          <i
            class="ni text-sm"
            :class="openMenus['profile'] ? 'ni-bold-down' : 'ni-bold-right'"
          ></i>
        </div>
        <ul v-if="openMenus['profile']" class="navbar-nav">
          <li class="nav-item">
            <!--            <sidenav-item to="/profile/staff" navText="Staff"/>-->
            <sidenav-item to="/profile/staff" navText="Nhân viên" />
          </li>
          <li class="nav-item">
            <!--            <sidenav-item to="/profile/client" navText="Client"/>-->
            <sidenav-item to="/profile/client" navText="Khách hàng" />
          </li>
        </ul>
      </li>
<!-- 
      Sign In
      <li class="nav-item">
        <sidenav-item
          to="/signin"
          :class="getRoute() === 'signin' ? 'active' : ''"
          :navText="isRTL ? 'Sign In' : 'Đăng nhập'"
        >
          <template v-slot:icon>
            <i class="ni ni-single-copy-04 text-danger text-sm opacity-10"></i>
          </template>
        </sidenav-item>
      </li> 

      Sign Up
      <li class="nav-item">
        <sidenav-item
          to="/signup"
          :class="getRoute() === 'signup' ? 'active' : ''"
          :navText="isRTL ? 'Sign Up' : 'Đăng ký'"
        >
          <template v-slot:icon>
            <i class="ni ni-collection text-info text-sm opacity-10"></i>
          </template>
        </sidenav-item>
      </li>   -->

    </ul>
  </div>

  <!-- FOOTER -->
  <div class="pt-3 mx-3 mt-3 sidenav-footer">
    <sidenav-card
      :card="{
        title: 'Cần trợ giúp?',
        description: 'Vui lòng kiểm tra tài liệu của chúng tôi',
        links: [
          {
            label: 'Tài liệu',
            route:
              'https://www.creative-tim.com/learning-lab/vue/overview/argon-dashboard/',
            color: 'dark',
          },
          {
            label: 'Mua ngay',
            route:
              'https://www.creative-tim.com/product/vue-argon-dashboard-pro?ref=vadp',
            color: 'success',
          },
        ],
      }"
    />
  </div>
</template>

<style scoped>
.navbar-nav .nav-item {
  margin-bottom: 0.5rem;
}

.navbar-nav .nav-item .nav-link {
  padding: 0.5rem 1rem;
}

.navbar-nav .nav-item .nav-link.active {
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 0.375rem;
}

.sidenav-footer {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.nav-item i {
  transition: transform 0.3s ease;
}

.nav-item span {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  /* Điều chỉnh khoảng cách giữa icon và text */
}

.nav-item i:first-child {
  width: 20px;
  /* Đảm bảo icon có kích thước cố định */
  text-align: center;
}
</style>