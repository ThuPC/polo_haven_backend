import { createApp } from "vue";
import App from "./App.vue";
import store from "./store";
import router from "./router";
import "./assets/css/nucleo-icons.css";
import "./assets/css/nucleo-svg.css";
import ArgonDashboard from "./argon-dashboard";
import '@fortawesome/fontawesome-free/css/all.css';
// import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import Toast from 'vue-toastification'
import 'vue-toastification/dist/index.css'
import '@vueform/multiselect/themes/default.css';

const appInstance = createApp(App);

appInstance.use(Toast, {
    position: 'top-right',
    timeout: 3000,  // tgian tự động ẩn thông báo
    closeOnClick: true, // cho phép đóng khi người dùng click vào
    pauseOnFocusLoss: true, //tạm dừng đếm tgian khi người dùng chuyển tab
    pauseOnHover: true, // tạm dừng đếm tgian khi di chuột vào
    draggable: true, // cho phép người dùng kéo thả để đóng
    draggablePercent: 0.6, //phần trăm chiều rộng của thông báo để tính khoảng kéo
    showCloseButtonOnHover: false, // chỉ hiện thị nút đóng khi di chuột vào (false là luôn hiển thị hoặc mặc định)
    hideProgressBar: false, // hiển thị thanh tiến trình đếm tgain (false là hiện)
    closeButton: 'button', //kiểu nút đóng
    icon: true, // hiện icon trong thông báo
    rtl: false // đặt văn bản thông báo từ phải sang trái
})

appInstance.use(store);
appInstance.use(router);
appInstance.use(ArgonDashboard);
appInstance.mount("#app");
