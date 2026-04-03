import { createRouter, createWebHistory } from 'vue-router'

// Import views
import TrangChu from '../views/TrangChu.vue'
import TinTuc from '../views/TinTuc.vue'
import SanPham from '../views/SanPham.vue'
import ChiTietSanPham from '../views/ChiTietSanPham.vue'
import GioHang from '../views/GioHang.vue'
import ThanhToan from '../views/ThanhToan.vue'
import LienHe from '@/views/LienHe.vue'
import GioiThieu from '@/views/GioiThieu.vue'
import DangNhap from '@/views/DangNhap.vue'
import DangKy from '@/views/DangKy.vue'
import DonHang from '@/views/DonHang.vue'

// Import profile views
import TheoDoiHoaDon from '@/views/TheoDoiHoaDon.vue'
import Profile from '@/views/profile/Profile.vue'
import PersonalInfo from '@/views/profile/PersonalInfo.vue'
import Security from '@/views/profile/Security.vue'
import Addresses from '@/views/profile/Addresses.vue'
import Orders from '@/views/profile/Orders.vue'
import GoogleInfo from '@/views/profile/GoogleInfo.vue'

const routes = [
  {
    path: '/',
    name: 'TrangChu',
    component: TrangChu
  },
  {
    path: '/news',
    name: 'TinTuc',
    component: TinTuc
  },
  {
    path: '/products',
    name: 'SanPham',
    component: SanPham
  },
  {
    path: '/product/:id',
    name: 'ChiTietSanPham',
    component: ChiTietSanPham,
    props: true
  },
  {
    path: '/cart',
    name: 'GioHang',
    component: GioHang
  },
  {
    path: '/checkout',
    name: 'ThanhToan',
    component: ThanhToan
  },
  {
    path: '/contact',
    name: 'LienHe',
    component: LienHe
  },
  {
    path: '/about',
    name: 'GioiThieu',
    component: GioiThieu
  },
  {
    path: '/login',
    name: 'DangNhap',
    component: DangNhap
  },
  {
    path: '/register',
    name: 'DangKy',
    component: DangKy
  },
  {
    path: '/orders',
    name: 'DonHang',
    component: DonHang
  },
  {
    path: '/tracking',
    name: 'TheoDoiHoaDon',
    component: TheoDoiHoaDon
  },
  // {
  //   path: '/khach-hang/hoa-don/:maHD',
  //   name: 'ChiTietHoaDon',
  //   component: () => import('@/views/khachhang/ChiTietHoaDon.vue')
  // }
  
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    children: [
      {
        path: '',
        redirect: '/profile/personal-info'
      },
      {
        path: 'personal-info',
        name: 'PersonalInfo',
        component: PersonalInfo
      },
      {
        path: 'security',
        name: 'Security',
        component: Security
      },
      {
        path: 'addresses',
        name: 'Addresses',
        component: Addresses
      },
      {
        path: 'orders',
        name: 'Orders',
        component: Orders
      },
      {
        path: 'google-info',
        name: 'GoogleInfo',
        component: GoogleInfo
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
