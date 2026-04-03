<!-- VnpayPayment.vue -->
<template>
  <div class="payment-container">
    <h3 class="title">Thanh toán qua VNPay QR</h3>
    
    <!-- Trạng thái đang tải -->
    <div v-if="isLoading" class="loading-spinner">
      <p>Đang tạo mã thanh toán...</p>
      <!-- Bạn có thể thêm một spinner CSS đẹp mắt ở đây -->
    </div>
    
    <!-- Hiển thị mã QR khi đã có URL -->
    <div v-if="paymentUrl && !error" class="qr-code-wrapper">
      <p>Quét mã QR dưới đây bằng ứng dụng ngân hàng của bạn.</p>
      <qrcode-vue :value="paymentUrl" :size="300" level="H" />
      <p class="note">Mã QR sẽ hết hạn trong 15 phút.</p>
    </div>
    
    <!-- Hiển thị lỗi nếu có -->
    <div v-if="error" class="error-message">
      <p>{{ error }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import QrcodeVue from 'qrcode.vue';
import axios from 'axios';

// 1. Định nghĩa props để nhận dữ liệu từ component cha
const props = defineProps({
  orderId: {
    type: String,
    required: true,
  },
  amount: {
    type: Number,
    required: true,
  },
  orderInfo: {
    type: String,
    default: 'Thanh toan don hang',
  },
});

// 2. Định nghĩa các biến trạng thái (reactive state) bằng `ref`
const paymentUrl = ref(null);
const isLoading = ref(false);
const error = ref(null);

// 3. Định nghĩa hàm logic để gọi API
const createPaymentUrl = async () => {
  isLoading.value = true;
  error.value = null;
  try {
    const payload = {
      orderId: props.orderId,
      amount: props.amount,
      orderInfo: `${props.orderInfo} #${props.orderId}`,
    };
    
    // URL API của Spring Boot
    const apiUrl = 'http://localhost:8080/api/v1/payment/create-payment';
    const response = await axios.post(apiUrl, payload);
    
    // Cập nhật trạng thái
    paymentUrl.value = response.data;

  } catch (err) {
    console.error('Lỗi khi tạo URL thanh toán VNPay:', err);
    error.value = 'Không thể tạo mã thanh toán. Vui lòng thử lại sau.';
  } finally {
    isLoading.value = false;
  }
};

// 4. Sử dụng hook onMounted để gọi hàm khi component được gắn vào DOM
onMounted(() => {
  createPaymentUrl();
});
</script>

<style scoped>
/* Giữ nguyên CSS từ ví dụ trước */
.payment-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 8px;
  max-width: 400px;
  margin: 2rem auto;
}
.qr-code-wrapper {
  text-align: center;
}
.note {
    font-size: 0.9em;
    color: #555;
}
.error-message {
    color: red;
}
.loading-spinner {
  padding: 40px;
}
</style>