import { createRouter, createWebHistory } from "vue-router";
import Signup from "../views/login/Signup.vue";
import Signin from "../views/login/Signin.vue";
import Voucher from "../views/view_QuanLy/voucher/Voucher.vue";
import KhuyenMai from "../views/view_QuanLy/voucher/KhuyenMai.vue";
import Oauth2Redirect from "../views/login/Oauth2Redirect.vue";
import Unauthenticated401 from "../views/login/Unauthenticated401.vue";
import Staff from "../views/view_QuanLy/profile/Staff.vue";
import Client from "../views/view_QuanLy/profile/Client.vue";
import Profile from "../views/Profile.vue";
import AddOrEdit from "../views/view_QuanLy/profile/AddOrEdit.vue";
import AddOrEditClient from "../views/view_QuanLy/profile/AddOrEditClient.vue";
import AddSanPham from "@/views/view_QuanLy/product/AddSanPham.vue";
import Billing from "../views/Billing.vue";
import InvoiceDetail from "../views/view_QuanLy/bills/InvoiceDetail.vue"; // Import component chi tiết hóa đơn
import ReturnManagement from "../views/view_QuanLy/bills/ReturnManagement.vue";
import UpdateSanPham from "@/views/view_QuanLy/product/UpdateSanPham.vue";
import sell from "../views/view_QuanLy/sell/sell.vue";
// import TrangChu from "../views/view_QuanLy/trangChu/TrangChu.vue";
import Unauthorize403 from "../views/login/Unauthorize403.vue";
import VnpayPayment from "../views/view_QuanLy/sell/VnpayPayment.vue";
// import PaymentResult from "../views/view_QuanLy/sell/PaymentResult.vue";
import AdminChat from "../views/view_QuanLy/chat/AdminChat.vue";
const routes = [
  {
    path: "/",
    name: "Home",
    component: Signin,
  },
  // {
  //   path: "/trang-chu",
  //   name: "trangChu",
  //   component: TrangChu,
  // },
  {
    path: "/banHangTaiQuay",
    name: "banHang",
    component: sell,
  },
  {
    path: '/payment',
    name: 'Payment',
    component: VnpayPayment,

  },
  // {
  //   path: '/payment-result',
  //   name: 'PaymentResult',
  //   component: PaymentResult,
  // },
  // {
  //   path: "/dashboard-default",
  //   name: "Dashboard",
  //   component: Dashboard,
  // },
  //sanpham
  {
    path: "/product/san-pham/hien-thi",
    name: "Product",
    component: () => import("../views/view_QuanLy/product/SanPham.vue"),
  },
  {
    path: "/product/thuong-hieu/hien-thi",
    name: "Trademark",
    component: () => import("../views/view_QuanLy/product/ThuongHieu.vue"),
  },
  {
    path: "/product/xuat-xu/hien-thi",
    name: "Origin",
    component: () => import("../views/view_QuanLy/product/XuatXu.vue"),
  },
  {
    path: "/product/chat-lieu/hien-thi",
    name: "material",
    component: () => import("../views/view_QuanLy/product/ChatLieu.vue"),
  },
  {
    path: "/product/kich-co/hien-thi",
    name: "size",
    component: () => import("../views/view_QuanLy/product/KichThuoc.vue"),
  },
  {
    path: "/product/mau-sac/hien-thi",
    name: "Color",
    component: () => import("../views/view_QuanLy/product/MauSac.vue"),
  },
  { path: "/add-product", name: "Add Product", component: AddSanPham },
  {
    path: "/update-product/:id",
    name: "Update Product",
    component: UpdateSanPham,
    props: true,
  },

  //thongke
  {
    path: "/thong-ke",
    name: "ThongKe",
    component: () =>
      import("../views/view_QuanLy/statistical/ThongKe.vue"),
  },

  {
    path: "/giam-gia",
    name: "Voucher",
    children: [
      {
        path: "voucher",
        name: "Discount",
        component: Voucher,
      },
      {
        path: "khuyen-mai",
        name: "KhuyenMai",
        component: KhuyenMai,
      },
    ],
  },
  {
    path: "/billing",
    name: "Billing",
    component: Billing,
  },
  {
    path: "/returnmanagement",
    name: "ReturnManagement",
    component: ReturnManagement,
  },
  // Thêm route cho chi tiết hóa đơn
  {
    path: "/api/hoa-don/detail/:id", // Sử dụng dynamic route với tham số `id`
    name: "InvoiceDetail",
    component: InvoiceDetail, // Component để hiển thị chi tiết hóa đơn
  },
  {
    path: "/profile",
    name: "Profile",
    component: Profile,
    children: [
      {
        path: "staff",
        name: "Staff",
        component: Staff,
      },
      {
        path: "client",
        name: "Client",
        component: Client,
      },
    ],
  },
  {
    path: "/nhan-vien/create",
    component: AddOrEdit,
    props: { employeeId: null },
  },
  {
    path: "/nhan-vien/update/:id",
    component: AddOrEdit,
    props: (route) => ({ employeeId: route.params.id }),
  },
  {
    path: "/khach-hang/create",
    name: "CreateKhachHang",
    component: AddOrEditClient,
  },
  {
    path: "/khach-hang/update/:id",
    name: "UpdateKhachHang",
    component: AddOrEditClient,
    props: true,
  },
  {
    path: "/signin",
    name: "Signin",
    component: Signin,
  },
  {
    path: "/signup",
    name: "Signup",
    component: Signup,
  },
  {
    path: "/oauth2-redirect",
    name: "Oauth2Redirect",
    component: Oauth2Redirect,
  },
  {
    path: "/401unauthenticated",
    name: "Unauthenticated401",
    component: Unauthenticated401,
  },
  {
    path: "/403unauthorize",
    name: "Unauthorize403",
    component: Unauthorize403,
  },
  {
    path: "/chat",
    name: "AdminChat",
    component: AdminChat,
  },
  {
    path: "/nhan-vien/create",
    component: AddOrEdit,
    props: { employeeId: null },
  },
  {
    path: "/nhan-vien/update/:id",
    component: AddOrEdit,
    props: (route) => ({ employeeId: route.params.id }),
  },
  {
    path: "/khach-hang/create",
    name: "CreateKhachHang",
    component: AddOrEditClient,
  },
  {
    path: "/khach-hang/update/:id",
    name: "UpdateKhachHang",
    component: AddOrEditClient,
    props: true,
  },
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
  linkActiveClass: "active",
});

router.beforeEach((to, from, next) => {
  document.body.classList.remove("modal-open");
  document.body.style.overflow = "auto";

  const backdrop = document.querySelector(".modal-backdrop");
  if (backdrop) backdrop.remove();

  next();
});


export default router;
