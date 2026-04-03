<template>
    <br>
    <br><br>
  <div class="order-management container py-5">
    <h2 class="mb-4 fw-bold text-center">Quản lý đơn hàng của bạn</h2>
    <div v-if="orders.length === 0" class="alert alert-info text-center">
      Bạn chưa có đơn hàng nào.
    </div>
    <div v-else>
      <table class="table table-bordered table-hover align-middle">
        <thead class="table-success">
          <tr>
            <th class="text-center">Mã đơn</th>
            <th class="text-center">Ngày đặt</th>
            <th class="text-center">Tổng tiền</th>
            <th class="text-center">Trạng thái</th>
            <th class="text-center">Chi tiết</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in orders" :key="order.id">
            <td class="text-center">{{ order.code }}</td>
            <td class="text-center">{{ formatDate(order.date) }}</td>
            <td class="text-center">{{ formatPrice(order.total) }}</td>
            <td class="text-center">
              <span :class="statusClass(order.status)" class="fw-semibold px-2 py-1 rounded">
                {{ statusText(order.status) }}
              </span>
            </td>
            <td class="text-center">
              <button class="btn btn-outline-primary btn-sm me-1" @click="showDetail(order)">
                Xem
              </button>
              <button 
                v-if="order.status === 5 && isWithin15Days(order.date)" 
                class="btn btn-outline-danger btn-sm" 
                @click="showReturnModal(order)"
              >
                Yêu cầu trả hàng
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal chi tiết đơn hàng -->
    <div v-if="selectedOrder" class="modal fade show d-block" tabindex="-1" style="background:rgba(0,0,0,0.3);">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Chi tiết đơn hàng #{{ selectedOrder.code }}</h5>
            <button type="button" class="btn-close" @click="selectedOrder = null"></button>
          </div>
          <div class="modal-body">
            <p><b>Ngày đặt:</b> {{ formatDate(selectedOrder.date) }}</p>
            <p><b>Trạng thái:</b> <span :class="statusClass(selectedOrder.status)">{{ statusText(selectedOrder.status) }}</span></p>
            <p><b>Tổng tiền:</b> {{ formatPrice(selectedOrder.total) }}</p>
            <hr>
            <h6>Danh sách sản phẩm:</h6>
            <ul>
              <li v-for="item in selectedOrder.items" :key="item.id">
                {{ item.name }} x {{ item.quantity }} ({{ formatPrice(item.price) }})
              </li>
            </ul>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="selectedOrder = null">Đóng</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal trả hàng -->
    <div v-if="returnOrderModal" class="modal fade show d-block" tabindex="-1" style="background:rgba(0,0,0,0.3);">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header bg-danger text-white">
            <h5 class="modal-title">Yêu cầu trả hàng #{{ returnOrder?.code }}</h5>
            <button type="button" class="btn-close btn-close-white" @click="closeReturnModal"></button>
          </div>
          <div class="modal-body">
            <div class="alert alert-info">
              <i class="fas fa-info-circle me-2"></i>
              Bạn có thể trả hàng trong vòng 15 ngày kể từ ngày nhận hàng. Vui lòng cung cấp đầy đủ thông tin để chúng tôi xử lý yêu cầu của bạn.
            </div>
            
            <h6 class="fw-bold mb-3">Danh sách sản phẩm:</h6>
            <div class="table-responsive">
              <table class="table table-bordered">
                <thead class="table-light">
                  <tr>
                    <th>Sản phẩm</th>
                    <th class="text-center">Đã mua</th>
                    <th class="text-center">Số lượng trả</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in returnItems" :key="item.id">
                    <td>
                      <div class="d-flex align-items-center">
                        <div class="product-image me-2" style="width: 60px; height: 60px;">
                          <img src="../assets/ao1.webp" class="img-fluid" alt="Product Image">
                        </div>
                        <div>
                          <div class="fw-semibold">{{ item.name }}</div>
                          <div class="text-muted small">{{ formatPrice(item.price) }}</div>
                        </div>
                      </div>
                    </td>
                    <td class="text-center align-middle">{{ item.quantity }}</td>
                    <td class="text-center align-middle" style="width: 150px;">
                      <div class="input-group">
                        <button 
                          class="btn btn-outline-secondary" 
                          type="button"
                          @click="item.returnQuantity = Math.max(0, item.returnQuantity - 1)"
                        >-</button>
                        <input 
                          type="number" 
                          class="form-control text-center" 
                          v-model.number="item.returnQuantity"
                          min="0"
                          :max="item.quantity"
                        >
                        <button 
                          class="btn btn-outline-secondary" 
                          type="button"
                          @click="item.returnQuantity = Math.min(item.quantity, item.returnQuantity + 1)"
                        >+</button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="mb-3">
              <label for="returnReason" class="form-label fw-bold">Lý do trả hàng <span class="text-danger">*</span></label>
              <textarea 
                id="returnReason" 
                class="form-control" 
                v-model="returnReason" 
                rows="3" 
                placeholder="Vui lòng mô tả lý do trả hàng..."
              ></textarea>
            </div>

            <div class="row">
              <div class="col-md-6 mb-3">
                <label class="form-label fw-bold">Hình ảnh <span class="text-danger">*</span> <small class="text-muted">(Tối thiểu 1 ảnh)</small></label>
                <input 
                  type="file" 
                  class="form-control" 
                  accept="image/*" 
                  multiple 
                  @change="handleImageUpload"
                >
                <div class="d-flex flex-wrap mt-2 gap-2">
                  <div v-for="(image, index) in returnImages" :key="index" class="position-relative" style="width: 80px; height: 80px;">
                    <img :src="URL.createObjectURL(image)" class="img-thumbnail" style="width: 100%; height: 100%; object-fit: cover;">
                    <button 
                      type="button" 
                      class="btn btn-danger btn-sm position-absolute top-0 end-0" 
                      style="padding: 0 5px; font-size: 10px;"
                      @click="removeImage(index)"
                    >×</button>
                  </div>
                </div>
              </div>
              <div class="col-md-6 mb-3">
                <label class="form-label fw-bold">Video <span class="text-danger">*</span> <small class="text-muted">(Tối thiểu 1 video)</small></label>
                <input 
                  type="file" 
                  class="form-control" 
                  accept="video/*" 
                  multiple 
                  @change="handleVideoUpload"
                >
                <div class="d-flex flex-wrap mt-2 gap-2">
                  <div v-for="(video, index) in returnVideos" :key="index" class="position-relative" style="width: 120px;">
                    <div class="border rounded p-1">
                      <div class="text-truncate" style="max-width: 100px;">{{ video.name }}</div>
                      <small class="text-muted">{{ (video.size / 1024 / 1024).toFixed(2) }} MB</small>
                    </div>
                    <button 
                      type="button" 
                      class="btn btn-danger btn-sm position-absolute top-0 end-0" 
                      style="padding: 0 5px; font-size: 10px;"
                      @click="removeVideo(index)"
                    >×</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeReturnModal">Hủy</button>
            <button 
              type="button" 
              class="btn btn-danger" 
              @click="submitReturnRequest"
              :disabled="isSubmitting"
            >
              <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
              {{ isSubmitting ? 'Đang xử lý...' : 'Gửi yêu cầu trả hàng' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useToast } from 'vue-toastification'

// Bảng trạng thái đơn hàng
const orderStatus = [
  'Chờ xác nhận', // 0
  'Đã xác nhận', // 1
  'Chờ vận chuyển', // 2
  'Đang vận chuyển', // 3
  'Đã giao hàng', // 4
  'Hoàn thành', // 5
  'Đã hủy',      // 6
  'Trả hàng',    // 7
  'Đang trả hàng', // 8
  'Trả hàng thành công', // 9
  'Từ chối trả hàng' // 10
]

// Mock data đơn hàng
const orders = ref([
  {
    id: 1,
    code: 'DH001',
    date: '2024-06-01',
    total: 1500000,
    status: 0,
    items: [
      { id: 1, name: 'Áo Polo Trắng', quantity: 2, price: 350000 },
      { id: 2, name: 'Áo Polo Đen', quantity: 1, price: 400000 }
    ]
  },
  {
    id: 2,
    code: 'DH002',
    date: '2024-06-03',
    total: 800000,
    status: 3,
    items: [
      { id: 3, name: 'Áo Polo Xanh', quantity: 2, price: 400000 }
    ]
  },
  {
    id: 3,
    code: 'DH003',
    date: '2024-06-05',
    total: 1200000,
    status: 5,
    items: [
      { id: 4, name: 'Áo Polo Đỏ', quantity: 3, price: 400000 }
    ]
  },
  {
    id: 4,
    code: 'DH004',
    date: '2024-06-06',
    total: 500000,
    status: 6,
    items: [
      { id: 5, name: 'Áo Polo Vàng', quantity: 1, price: 500000 }
    ]
  }
])

const selectedOrder = ref(null)
const returnOrderModal = ref(false)
const returnOrder = ref(null)
const returnItems = ref([])
const returnReason = ref('')
const returnImages = ref([])
const returnVideos = ref([])
const isSubmitting = ref(false)
const toast = useToast()

function showDetail(order) {
  selectedOrder.value = order
}

function showReturnModal(order) {
  returnOrder.value = order
  returnItems.value = order.items.map(item => ({
    ...item,
    returnQuantity: 0
  }))
  returnReason.value = ''
  returnImages.value = []
  returnVideos.value = []
  returnOrderModal.value = true
}

function closeReturnModal() {
  returnOrderModal.value = false
  returnOrder.value = null
  returnItems.value = []
  returnReason.value = ''
  returnImages.value = []
  returnVideos.value = []
}

function isWithin15Days(orderDate) {
  const orderTime = new Date(orderDate).getTime()
  const currentTime = new Date().getTime()
  const daysDiff = (currentTime - orderTime) / (1000 * 60 * 60 * 24)
  return daysDiff <= 15
}

function handleImageUpload(event) {
  const files = event.target.files
  if (files) {
    for (let i = 0; i < files.length; i++) {
      const file = files[i]
      if (file.type.startsWith('image/')) {
        returnImages.value.push(file)
      }
    }
  }
}

function handleVideoUpload(event) {
  const files = event.target.files
  if (files) {
    for (let i = 0; i < files.length; i++) {
      const file = files[i]
      if (file.type.startsWith('video/')) {
        returnVideos.value.push(file)
      }
    }
  }
}

function removeImage(index) {
  returnImages.value.splice(index, 1)
}

function removeVideo(index) {
  returnVideos.value.splice(index, 1)
}

function validateReturnForm() {
  // Kiểm tra số lượng sản phẩm trả
  const hasItems = returnItems.value.some(item => item.returnQuantity > 0)
  if (!hasItems) {
    toast.error('Vui lòng chọn ít nhất một sản phẩm để trả')
    return false
  }
  
  // Kiểm tra số lượng không vượt quá số lượng đã mua
  const validQuantities = returnItems.value.every(item => 
    item.returnQuantity <= item.quantity && item.returnQuantity >= 0
  )
  if (!validQuantities) {
    toast.error('Số lượng trả không hợp lệ')
    return false
  }
  
  // Kiểm tra lý do trả hàng
  if (!returnReason.value.trim()) {
    toast.error('Vui lòng nhập lý do trả hàng')
    return false
  }
  
  // Kiểm tra ảnh và video
  if (returnImages.value.length === 0) {
    toast.error('Vui lòng tải lên ít nhất một ảnh')
    return false
  }
  
  if (returnVideos.value.length === 0) {
    toast.error('Vui lòng tải lên ít nhất một video')
    return false
  }
  
  return true
}

async function submitReturnRequest() {
  if (!validateReturnForm()) return
  
  isSubmitting.value = true
  
  try {
    // Chuẩn bị dữ liệu
    const formData = new FormData()
    formData.append('hoaDonId', returnOrder.value.id)
    formData.append('lyDo', returnReason.value)
    
    // Thêm sản phẩm trả
    const sanPhamTra = returnItems.value
      .filter(item => item.returnQuantity > 0)
      .map(item => ({
        sanPhamId: item.id,
        soLuong: item.returnQuantity
      }))
    formData.append('sanPhamTra', JSON.stringify(sanPhamTra))
    
    // Thêm ảnh và video
    returnImages.value.forEach(image => {
      formData.append('hinhAnh', image)
    })
    
    returnVideos.value.forEach(video => {
      formData.append('video', video)
    })
    
    // Gửi yêu cầu API
    // const response = await axios.post('/api/tra-hang', formData)
    
    // Giả lập API thành công
    setTimeout(() => {
      // Cập nhật trạng thái đơn hàng
      const orderIndex = orders.value.findIndex(o => o.id === returnOrder.value.id)
      if (orderIndex !== -1) {
        orders.value[orderIndex].status = 8 // Đang trả hàng
      }
      
      toast.success('Gửi yêu cầu trả hàng thành công')
      closeReturnModal()
      isSubmitting.value = false
    }, 1000)
  } catch (error) {
    console.error('Lỗi khi gửi yêu cầu trả hàng:', error)
    toast.error('Có lỗi xảy ra khi gửi yêu cầu trả hàng')
    isSubmitting.value = false
  }
}

function statusText(status) {
  return orderStatus[status] || 'Không xác định'
}

function statusClass(status) {
  // Màu sắc cho từng trạng thái
  switch (status) {
    case 0: return 'bg-warning text-dark'
    case 1: return 'bg-info text-white'
    case 2: return 'bg-secondary text-white'
    case 3: return 'bg-primary text-white'
    case 4: return 'bg-success text-white'
    case 5: return 'bg-success text-white'
    case 6: return 'bg-danger text-white'
    case 7: return 'bg-dark text-white'
    case 8: return 'bg-warning text-dark'
    case 9: return 'bg-success text-white'
    case 10: return 'bg-danger text-white'
    default: return 'bg-light text-dark'
  }
}

function formatDate(date) {
  return new Date(date).toLocaleDateString('vi-VN')
}

function formatPrice(price) {
  return price.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' })
}
</script>

<style scoped>
.order-management {
  max-width: 900px;
}
.table th, .table td {
  vertical-align: middle;
}
.bg-warning {
  background-color: #ffe066 !important;
}
.bg-info {
  background-color: #63c2de !important;
}
.bg-secondary {
  background-color: #6c757d !important;
}
.bg-primary {
  background-color: #007bff !important;
}
.bg-success {
  background-color: #28a745 !important;
}
.bg-danger {
  background-color: #dc3545 !important;
}
.bg-dark {
  background-color: #343a40 !important;
}
</style>
