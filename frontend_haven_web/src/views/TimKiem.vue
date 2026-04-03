<template>
  <div class="search-modal-backdrop" @click.self="$emit('close')">
    <div class="search-modal-content">
      <!-- Header -->
      <div class="modal-header">
        <h5 class="modal-title">Kết quả cho: "{{ keyword }}"</h5>
        <button type="button" class="btn-close" @click="$emit('close')"></button>
      </div>

      <!-- Body - Danh sách kết quả -->
      <div class="modal-body">
        <div v-if="results.length === 0" class="text-center text-muted p-4">
          Không tìm thấy sản phẩm nào.
        </div>
        <ul v-else class="list-group list-group-flush">
          <li v-for="product in results" :key="product.id" class="list-group-item">
            <!-- Sử dụng router-link để đến trang chi tiết -->
            <router-link 
              :to="`/product/${product.id}`" 
              class="d-flex align-items-center text-decoration-none text-dark"
              @click="$emit('close')"
            >
              <img :src="product.imageList[0]" :alt="product.tenSanPham" class="search-result-img me-3">
              <div class="flex-grow-1">
                <p class="mb-0 fw-semibold">{{ product.tenSanPham }}</p>
                <span class="text-success fw-bold">{{ formatPriceRange(product.khoangGia) }}</span>
              </div>
            </router-link>
          </li>
        </ul>
      </div>

      <!-- Footer -->
      <div class="modal-footer" v-if="results.length > 0">
        <!-- Nút này sẽ phát sự kiện để HeaderLayout xử lý -->
        <button class="btn btn-success w-100" @click="$emit('view-all')">
          Xem tất cả kết quả
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  results: {
    type: Array,
    required: true
  },
  keyword: {
    type: String,
    default: ''
  }
});

defineEmits(['close', 'view-all']);

// Hàm format giá (bạn có thể import từ một file utils chung)
const formatPriceRange = (priceString) => {
    if (!priceString) return 'N/A';
    // Logic format giá của bạn ở đây...
    return priceString;
};
</script>

<style scoped>
.search-modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  padding-top: 5%;
  z-index: 1050;
}
.search-modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  height: fit-content;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}
.modal-body {
  overflow-y: auto;
}
.search-result-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}
</style>