<template>
  <div class="cart-page">
    <!-- Header Section (Cart Title) -->
    <section class="cart-header py-4 bg-gradient text-white text-center">
      <div class="container">
        <h2 class="section-title fw-bold">
          <span><i class="fa-solid fa-cart-shopping"></i></span> Giỏ Hàng
        </h2>
      </div>
    </section>

    <!-- Cart Content Section -->
    <section class="cart-content py-5">
      <div class="container">
        <!-- Empty Cart Message -->
        <div v-if="cartItems.length === 0" class="text-center p-4 bg-white rounded-lg shadow-lg">
          <h3 class="fs-5 fw-bold mb-3" style="color: #4ba27b;">Giỏ hàng trống</h3>
          <p class="text-muted">Hãy thêm sản phẩm vào giỏ hàng để tiếp tục mua sắm!</p>
          <router-link to="/products" class="btn btn-gradient">Tiếp tục mua sắm</router-link>
        </div>

        <!-- Cart Items and Summary -->
        <div v-else class="row g-4">
          <!-- Cart Items -->
          <div class="col-12 col-md-8">
            <div class="cart-items p-2 bg-white rounded-lg shadow-lg">
              <div class="d-flex justify-content-between align-items-center mb-4">
                <h3 class="fs-5 fw-bold mb-0" style="color: #4ba27b;">Giỏ Hàng</h3>
                <div v-if="isCheckingStock" class="text-muted">
                  <span class="spinner-border spinner-border-sm me-1"></span>
                  Đang kiểm tra tồn kho...
                </div>
              </div>

              <!-- Thông báo khi có thay đổi -->
              <div v-if="hasStockOrPriceChange" class="alert alert-warning mb-3" role="alert">
                <i class="fa-solid fa-exclamation-triangle me-2"></i>
                <strong>Cảnh báo:</strong> Có thay đổi về tồn kho hoặc giá sản phẩm!
                <div class="mt-2">
                  <button
                      @click="checkStockAndPrice"
                      class="btn btn-sm btn-outline-warning"
                      :disabled="isCheckingStock"
                  >
                    <span v-if="isCheckingStock" class="spinner-border spinner-border-sm me-1"></span>
                    <i v-else class="fa-solid fa-refresh me-1"></i>
                    Kiểm tra lại
                  </button>
                </div>
              </div>

              <!-- Selection Controls -->
              <div class="d-flex justify-content-between align-items-center mb-3 p-3 bg-light rounded">
                <div class="d-flex align-items-center">
                  <input
                      type="checkbox"
                      :checked="selectAllChecked"
                      @change="toggleSelectAll"
                      class="form-check-input me-2"
                      id="selectAll"
                  />
                  <label for="selectAll" class="form-check-label fw-bold">Chọn tất cả</label>
                </div>
                <div class="text-muted">
                  <i class="fa-solid fa-check-circle me-1"></i>
                  Đã chọn {{ cartStore.selectedItems.length }}/{{ cartItems.length }} sản phẩm
                </div>
              </div>

              <table class="cart-table table-light w-100">
                <thead>
                <tr>
                  <th class="text-center" style="width: 50px;">
                    <i class="fa-solid fa-check"></i>
                  </th>
                  <th class="text-center">STT</th>
                  <th class="text-center">Ảnh</th>
                  <th class="text-center">Sản phẩm</th>
                  <th class="text-center">Giá</th>
                  <th class="text-center">Số lượng</th>
                  <th class="text-center">Tổng tiền</th>
                  <th class="text-center">Xóa</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="(item, index) in cartItems" :key="item.cartItemId || item.id" class="cart-item">
                  <td class="text-center">
                    <input
                        type="checkbox"
                        v-model="item.selected"
                        @change="updateSelectAll"
                        class="form-check-input"
                        :disabled="item.quantity > (item.soLuongTon || 999)"
                    />
                  </td>
                  <td class="text-center">
                    {{ index + 1 }}
                  </td>
                  <td class="text-center">
                    <img :src="item.image" :alt="item.tenSanPham || item.name"
                         class="img-fluid object-fit-cover rounded-lg" style="height: 120px;" />
                  </td>
                  <td class="text-center fit-content">
                    <div class="d-flex flex-column align-items-start text-start">
                      <div class="product-name fw-bold mb-1">
                        <router-link
                            v-if="item.productViewId"
                            :to="`/product/${item.productViewId}`"
                            class="text-decoration-none text-dark"
                        >
                          {{ item.tenSanPham || item.name }}
                        </router-link>
                        <span v-else class="text-dark">{{ item.tenSanPham || item.name }}</span>
                      </div>
                      <small class="text-muted d-block">Mã sản phẩm: {{ item.maSanPham || 'N/A' }}</small>
                      <small class="text-muted d-block">
                        Phân loại:
                        <span class="ms-1">{{ item.color || 'N/A' }}</span>,
                        <span class="ms-1">{{ item.size || 'N/A' }}</span>
                      </small>
                    </div>
                  </td>
                  <td class="text-center fit-content fw-bold">
                    <div v-if="item.phanTramGiam > 0 && item.originalPrice" class="d-flex flex-column align-items-center">
                      <del class="text-muted small">{{ formatPrice(item.originalPrice) }}</del>
                      <span class="discounted-price text-danger fw-bold">{{ formatPrice(item.price) }}</span>
                    </div>
                    <div v-else class="d-flex flex-column align-items-center">
                      <span class="fw-bold">{{ formatPrice(item.price) }}</span>
                    </div>
                  </td>
                  <td class="text-center">
                    <input type="number" min="1" v-model.number="item.quantity"
                           class="form-control w-75 border-gradient text-center"
                           @change="updateQuantity(item)" />
                  </td>
                  <td class="text-center fit-content fw-bold">
                    {{ formatPrice(item.price * item.quantity) }}
                  </td>
                  <td class="text-center fit-content">
                    <button
                        class="btn btn-outline-danger btn-sm"
                        @click="removeItem(item.cartItemId)"
                        :disabled="cartStore.isRemoving"
                    >
                      <span v-if="cartStore.isRemoving" class="spinner-border spinner-border-sm me-2"></span>
                      <i class="fa-solid fa-trash"></i>
                    </button>
                  </td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Order Summary -->
          <div class="col-12 col-md-4">
            <div class="order-summary p-4 bg-white rounded-lg shadow-lg">
              <h3 class="fs-5 fw-bold mb-4" style="color: #4ba27b;">Đơn Hàng</h3>
              <div v-if="cartStore.selectedItems.length === 0" class="text-center py-4">
                <i class="fa-solid fa-shopping-cart mb-3" style="font-size: 3rem; color: #ccc;"></i>
                <p class="text-muted mb-0">Chưa có sản phẩm nào được chọn</p>
              </div>
              <div v-else>
                <p class="mb-3">
                  Tổng cộng (<span>{{ cartStore.selectedItems.length }}</span> sản phẩm):
                  <span class="total-amount fs-4 fw-bold">{{ formatPrice(totalAmount) }}</span>
                </p>
              </div>
              <div v-if="cartStore.selectedItems.length === 0" class="alert alert-info mb-3" role="alert">
                <i class="fa-solid fa-info-circle me-2"></i>
                Vui lòng chọn ít nhất một sản phẩm để tiến hành thanh toán
              </div>
              <router-link to="/checkout">
                <button
                    class="btn btn-gradient w-100 py-2 fw-semibold"
                    :disabled="cartStore.selectedItems.length === 0 || isCheckingStock || hasStockOrPriceChange"
                >
                  <span v-if="isCheckingStock" class="spinner-border spinner-border-sm me-2"></span>
                  <i v-else class="fa-solid fa-credit-card me-2"></i>
                  {{ isCheckingStock ? 'Đang kiểm tra...' : (cartStore.selectedItems.length === 0 ? 'Chọn sản phẩm để thanh toán' : 'Tiến Hành Thanh Toán') }}
                </button>
              </router-link>
              <router-link to="/products" class="btn btn-outline-secondary w-100 py-2 mt-3 fw-semibold">
                Tiếp tục mua sắm
              </router-link>
              <p class="text-muted mt-3 fs-6">Đóng gói cùng gói T-shirt trong buổi chiều hôm nay.</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { useCartStore } from '@/stores/cart'
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'

const cartStore = useCartStore()
const router = useRouter()
const cartItems = computed(() => {
  console.log('Cart items in GioHang:', cartStore.items)
  return cartStore.items
})
const toast = useToast()

// State để kiểm tra tồn kho và giá
const isCheckingStock = ref(false)
const hasStockOrPriceChange = ref(false)

// Sử dụng computed properties từ store
const totalAmount = computed(() => cartStore.selectedTotal)

// Ref cho checkbox chọn tất cả
const selectAllChecked = ref(false)

onMounted(async () => {
  // if (cartStore.isLoggedIn() && cartStore.items.length === 0) {
  //   await cartStore.fetchCartFromBackend()
  // }

  // Kiểm tra tồn kho và giá khi vào trang giỏ hàng
  await checkStockAndPrice()

  // Đảm bảo tất cả sản phẩm có thuộc tính selected và mặc định là false
  cartItems.value.forEach(item => {
    if (item.selected === undefined) {
      item.selected = false
    }
  })

  // Cập nhật trạng thái checkbox chọn tất cả
  updateSelectAllStatus()
})

// Watcher để theo dõi thay đổi của cartItems
watch(cartItems, () => {
  updateSelectAllStatus()
}, { deep: true })

// Function để cập nhật trạng thái checkbox chọn tất cả
function updateSelectAllStatus() {
  const newStatus = cartItems.value.length > 0 && cartItems.value.every(item => Boolean(item.selected))
  selectAllChecked.value = newStatus
}

// Hàm kiểm tra tồn kho và giá
async function checkStockAndPrice() {
  if (cartItems.value.length === 0) return

  try {
    isCheckingStock.value = true
    hasStockOrPriceChange.value = false

    // Kiểm tra từng sản phẩm trong giỏ hàng
    let hasChanges = false

    for (const item of cartItems.value) {
      if (item.soLuongTon !== undefined && item.quantity > item.soLuongTon) {
        hasChanges = true
        // Tự động điều chỉnh số lượng nếu vượt quá tồn kho
        const oldQuantity = item.quantity
        item.quantity = item.soLuongTon
        cartStore.updateQuantity(item.id, item.quantity)

        toast.warning(`Sản phẩm "${item.tenSanPham}" đã được điều chỉnh từ ${oldQuantity} xuống ${item.quantity} do không đủ tồn kho.`)
      }
    }

    hasStockOrPriceChange.value = hasChanges

    if (hasChanges) {
      toast.info('Đã tự động điều chỉnh số lượng sản phẩm theo tồn kho hiện tại.')
    }

  } catch (error) {
    console.error('Lỗi khi kiểm tra tồn kho và giá:', error)
    toast.error('Không thể kiểm tra tồn kho và giá sản phẩm!')
  } finally {
    isCheckingStock.value = false
  }
}

// Checkbox "Chọn tất cả"
// const selectAll = computed({
//   get() {
//     const result = cartItems.value.length > 0 && cartItems.value.every(item => item.selected === true)
//     console.log('SelectAll computed get:', result, 'Items:', cartItems.value.map(item => ({ id: item.id, selected: item.selected })))
//     return result
//   },
//   set(value) {
//     console.log('SelectAll computed set:', value)
//     cartStore.toggleSelectAll(value)
//   }
// })

function formatPrice(price) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', minimumFractionDigits: 0 }).format(price)
}

function updateQuantity(item) {
  if (item.quantity < 1) {
    item.quantity = 1
  }

  // Kiểm tra tồn kho khi cập nhật số lượng
  if (item.soLuongTon !== undefined && item.quantity > item.soLuongTon) {
    toast.warning(`Số lượng vượt quá tồn kho (${item.soLuongTon}). Đã tự động điều chỉnh.`)
    item.quantity = item.soLuongTon
  }

  cartStore.updateQuantity(item.cartItemId, item.quantity)
}

function updateSelectAll() {
  // Cập nhật trạng thái checkbox "chọn tất cả" dựa trên trạng thái các sản phẩm
  updateSelectAllStatus()
}

function removeItem(item) {
  cartStore.removeFromCart(item)
}

function toggleSelectAll(event) {
  const isChecked = event.target.checked
  selectAllChecked.value = isChecked
  cartStore.toggleSelectAll(isChecked)
}
</script>

<style scoped>
@import 'animate.css';

.cart-page {
  padding-top: 130px;
  background-color: #f5f7fa;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

/* Gradient Definition */
.bg-gradient {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
}

.border-gradient {
  border: 2px solid transparent;
  background: linear-gradient(white, white) padding-box, linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%) border-box;
}


/* Cart Header */
.cart-header {
  position: relative;
  height: auto;
  min-height: 120px;
  overflow: hidden;
  border-bottom: 4px solid #4ba27b;
}

.cart-header .section-title {
  color: black;
  font-size: 28px;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.2);
}

/* Cart Content */
.cart-content .cart-items,
.cart-content .order-summary {
  border-radius: 15px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.cart-content .cart-items:hover,
.cart-content .order-summary:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(75, 162, 123, 0.2);
}

.cart-table th {
  background-color: #e9ecef;
  color: black;
  font-weight: bolder;
  text-transform: uppercase;
  letter-spacing: 1px;
  text-align: center;
}

.cart-table td {
  text-align: center;
  padding: 12px;
  vertical-align: middle;
}

.cart-table td {
  background-color: #fff;
}

.cart-table .fit-content {
  white-space: normal;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 250px;
}

.cart-table .cart-item img {
  transition: transform 0.5s ease;
}

.cart-table .cart-item:hover img {
  transform: scale(1.1);
}

.cart-table .quantity input {
  padding: 6px;
  text-align: center;
  border-radius: 5px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.cart-table .color-dot {
  display: inline-block;
  width: 15px;
  height: 15px;
  border-radius: 50%;
  border: 1px solid #eee;
  margin-right: 5px;
  vertical-align: middle;
}

.cart-table .product-name {
  font-size: 1rem;
  font-weight: 700;
  color: #333;
}

.cart-table .product-color,
.cart-table .product-size {
  font-size: 0.99rem;
  color: #666;
}

.cart-table .discounted-price {
  color: #28a745;
  font-weight: 700;
}

.cart-table .btn-outline-danger {
  border-color: #dc3545;
  color: #dc3545;
  padding: 6px 12px;
  border-radius: 5px;
  transition: all 0.3s ease;
}

.cart-table .btn-outline-danger:hover {
  background-color: #dc3545;
  color: white;
}

/* Checkbox styling */
.form-check-input {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.form-check-input:checked {
  background-color: #4ba27b;
  border-color: #4ba27b;
}

.form-check-input:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.form-check-label {
  cursor: pointer;
  user-select: none;
}

/* Empty state styling */
.order-summary .fa-shopping-cart {
  opacity: 0.3;
}




.order-summary .btn-gradient {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  border: none;
  color: #fff;
  transition: background 0.3s ease, box-shadow 0.3s ease;
}

.order-summary .btn-gradient:hover {
  background: linear-gradient(135deg, #4ba27b 0%, #66ea8b 100%);
  box-shadow: 0 8px 20px rgba(75, 162, 123, 0.4);
}

.order-summary .btn-gradient:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.order-summary .btn-outline-secondary {
  border-color: #6c757d;
  color: #6c757d;
  transition: all 0.3s ease;
}

.order-summary .btn-outline-secondary:hover {
  background-color: #6c757d;
  color: white;
}

/* Responsive Adjustments */
@media (max-width: 768px) {
  .cart-header .section-title {
    font-size: 22px;
  }

  .cart-table th,
  .cart-table td {
    padding: 8px;
    font-size: 14px;
  }

  .cart-item img {
    height: 100px;
  }

  .order-summary .total-amount {
    font-size: 18px;
  }

  .order-summary .btn-gradient,
  .order-summary .btn-outline-secondary {
    font-size: 14px;
  }

  .cart-table .fit-content {
    max-width: 200px;
  }

  .cart-table .product-name {
    font-size: 0.95rem;
  }

  .cart-table .product-color,
  .cart-table .product-size {
    font-size: 0.85rem;
  }
}

@media (max-width: 576px) {
  .cart-item img {
    height: 80px;
  }

  .order-summary .total-amount {
    font-size: 16px;
  }

  .order-summary .btn-gradient,
  .order-summary .btn-outline-secondary {
    font-size: 12px;
  }

  .container {
    padding: 0 10px;
  }

  .cart-table {
    font-size: 12px;
  }

  .cart-table th {
    font-size: 12px;
  }

  .cart-table .fit-content {
    max-width: 150px;
  }

  .cart-table .product-name {
    font-size: 0.9rem;
  }

  .cart-table .product-color,
  .cart-table .product-size {
    font-size: 0.8rem;
  }
}
</style>