<!-- VnpayModal.vue -->
<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h3>Thanh toán qua VNPay QR</h3>
        <button @click="$emit('close')" class="close-button">×</button>
      </div>
      <div class="modal-body">
        <div v-if="paymentData.url && !paymentData.isPaid">
          <p>Quét mã QR hoặc nhấp vào link bên dưới để thanh toán.</p>
          <qrcode-vue :value="paymentData.url" :size="250" level="H" />

          <!-- =============================================== -->
          <!-- ====> THÊM ĐOẠN CODE NÀY VÀO <==== -->
          <!-- =============================================== -->
          <div class="payment-link-wrapper">
            <a :href="paymentData.url" target="_blank" class="payment-link">
              Mở trang thanh toán (cho máy tính)
            </a>
            <p class="link-note">(Mở trong tab mới)</p>
          </div>
          <!-- =============================================== -->

          <p class="order-info">
            Số tiền: <strong>{{ formatCurrency(paymentData.amount) }}</strong>
          </p>
          <p class="order-info">
            Nội dung: <strong>{{ paymentData.orderInfo }}</strong>
          </p>
          <div class="polling-status">
            <span class="spinner"></span>
            <span>Đang chờ thanh toán...</span>
          </div>
        </div>
        <div v-if="paymentData.isPaid" class="success-message">
          <svg class="icon success" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
            <circle class="checkmark__circle" cx="26" cy="26" r="25" fill="none" />
            <path class="checkmark__check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8" />
          </svg>
          <h2>Thanh toán thành công!</h2>
          <p>Đơn hàng sẽ được hoàn tất.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// Script không cần thay đổi
import QrcodeVue from 'qrcode.vue';

defineProps({
  paymentData: {
    type: Object,
    required: true,
  },
});

defineEmits(['close']);

const formatCurrency = (value) => {
  if (typeof value !== 'number') return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};
</script>

<style scoped>
/* Giữ nguyên CSS cũ */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.6); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-content { background: white; padding: 20px 30px; border-radius: 8px; width: 400px; text-align: center; }
.modal-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee; padding-bottom: 10px; margin-bottom: 20px; }
.close-button { background: none; border: none; font-size: 24px; cursor: pointer; }
.order-info { margin: 5px 0; font-size: 14px; }
.polling-status { display: flex; align-items: center; justify-content: center; margin-top: 20px; color: #555; }
.spinner { width: 20px; height: 20px; border: 3px solid #f3f3f3; border-top: 3px solid #3498db; border-radius: 50%; animation: spin 1s linear infinite; margin-right: 10px; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
.success-message { color: #28a745; }
.icon { width: 80px; height: 80px; border-radius: 50%; display: block; margin: 20px auto; }
.icon .checkmark__circle { stroke-dasharray: 166; stroke-dashoffset: 166; stroke-width: 2; stroke-miterlimit: 10; stroke: #7ac142; fill: none; animation: stroke 0.6s cubic-bezier(0.65, 0, 0.45, 1) forwards; }
.icon .checkmark__check { transform-origin: 50% 50%; stroke-dasharray: 48; stroke-dashoffset: 48; stroke-width: 2; stroke-linecap: round; stroke-linejoin: round; stroke: #7ac142; fill: none; animation: stroke 0.3s cubic-bezier(0.65, 0, 0.45, 1) 0.8s forwards; }
@keyframes stroke { 100% { stroke-dashoffset: 0; } }

/* =============================================== */
/* ====> THÊM CSS NÀY VÀO <==== */
/* =============================================== */
.payment-link-wrapper {
  margin-top: 15px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 5px;
}

.payment-link {
  font-weight: bold;
  color: #007bff;
  text-decoration: none;
  font-size: 16px;
}

.payment-link:hover {
  text-decoration: underline;
}

.link-note {
  font-size: 12px;
  color: #6c757d;
  margin: 5px 0 0 0;
}
/* =============================================== */
</style>