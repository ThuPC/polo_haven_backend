<template>
  <div class="min-vh-100 bg-light py-5">
    <div class="container">
      <!-- Header -->
      <div class="text-center mb-5">
        <h1 class="display-4 fw-bold text-primary mb-3">
          <i class="fas fa-undo me-3"></i>
          Danh sách yêu cầu trả hàng
        </h1>
        <p class="lead text-muted">Theo dõi trạng thái các yêu cầu trả hàng của bạn</p>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="d-flex justify-content-center align-items-center py-5">
        <div class="text-center">
          <div class="spinner-border text-primary mb-3" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
          <p class="text-muted">Đang tải danh sách yêu cầu trả hàng...</p>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else-if="danhSachTraHang.length === 0" class="text-center py-5">
        <div class="card shadow-sm mx-auto" style="max-width: 400px;">
          <div class="card-body p-5">
            <i class="fas fa-inbox text-muted" style="font-size: 4rem;"></i>
            <h3 class="h4 text-dark mt-3 mb-2">Chưa có yêu cầu trả hàng nào</h3>
            <p class="text-muted mb-4">Bạn chưa có yêu cầu trả hàng nào. Hãy vào theo dõi hóa đơn để tạo yêu cầu trả hàng!</p>
            <router-link to="/tracking" class="btn btn-primary btn-lg">
              <i class="fas fa-receipt me-2"></i>
              Theo dõi hóa đơn
            </router-link>
          </div>
        </div>
      </div>

      <!-- Danh sách trả hàng -->
      <div v-else class="row g-4">
        <div v-for="traHang in danhSachTraHang" :key="traHang.id" class="col-12">
          <div class="card shadow-sm h-100 border-0">
            <div class="card-body p-4">
              <div class="row">
                <!-- Thông tin đơn trả hàng -->
                <div class="col-lg-8">
                  <div class="d-flex align-items-center mb-3">
                    <div class="bg-primary bg-opacity-10 rounded-circle p-3 me-3">
                      <i class="fas fa-undo text-primary fs-4"></i>
                    </div>
                    <div>
                      <h5 class="mb-1 fw-bold">{{ traHang.maDonTraHang }}</h5>
                      <p class="text-muted mb-0">
                        <i class="fas fa-receipt me-1"></i>
                        Hóa đơn: {{ traHang.maHoaDon }}
                      </p>
                      <small class="text-muted">
                        <i class="fas fa-calendar me-1"></i>
                        {{ formatDate(traHang.ngayTao) }}
                      </small>
                    </div>
                  </div>

                  <!-- Lý do trả hàng -->
                  <div class="mb-3">
                    <strong class="text-dark">Lý do:</strong>
                    <span class="ms-2">{{ traHang.lyDo }}</span>
                  </div>

                  <!-- Ghi chú -->
                  <div v-if="traHang.ghiChu" class="mb-3">
                    <strong class="text-dark">Ghi chú:</strong>
                    <span class="ms-2">{{ traHang.ghiChu }}</span>
                  </div>

                  <!-- Ghi chú admin -->
                  <div v-if="traHang.ghiChuAdmin" class="mb-3">
                    <strong class="text-dark">Phản hồi từ admin:</strong>
                    <span class="ms-2 text-info">{{ traHang.ghiChuAdmin }}</span>
                  </div>
                </div>

                <!-- Trạng thái và hành động -->
                <div class="col-lg-4 text-lg-end">
                  <div class="mb-3">
                    <span :class="getStatusClass(traHang.trangThai)" class="badge fs-6 px-3 py-2">
                      {{ getStatusText(traHang.trangThai) }}
                    </span>
                  </div>
                  <button 
                    @click="xemChiTiet(traHang)" 
                    class="btn btn-outline-primary btn-sm"
                  >
                    <i class="fas fa-eye me-1"></i>
                    Xem chi tiết
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal chi tiết trả hàng -->
    <div v-if="showDetailModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog modal-xl">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="fas fa-undo me-2"></i>
              Chi tiết yêu cầu trả hàng: {{ selectedTraHang?.maDonTraHang }}
            </h5>
            <button type="button" class="btn-close" @click="dongModalChiTiet"></button>
          </div>
          <div class="modal-body">
            <div v-if="selectedTraHang" class="row">
              <!-- Thông tin đơn trả hàng -->
              <div class="col-md-6 mb-4">
                <div class="card h-100">
                  <div class="card-header bg-primary text-white">
                    <h6 class="mb-0">
                      <i class="fas fa-info-circle me-2"></i>
                      Thông tin đơn trả hàng
                    </h6>
                  </div>
                  <div class="card-body">
                    <div class="row g-3">
                      <div class="col-12">
                        <strong>Mã đơn trả hàng:</strong>
                        <span class="ms-2">{{ selectedTraHang.maDonTraHang }}</span>
                      </div>
                      <div class="col-12">
                        <strong>Hóa đơn gốc:</strong>
                        <span class="ms-2">{{ selectedTraHang.maHoaDon }}</span>
                      </div>
                      <div class="col-12">
                        <strong>Ngày tạo:</strong>
                        <span class="ms-2">{{ formatDate(selectedTraHang.ngayTao) }}</span>
                      </div>
                      <div class="col-12">
                        <strong>Trạng thái:</strong>
                        <span :class="getStatusClass(selectedTraHang.trangThai)" class="badge ms-2">
                          {{ getStatusText(selectedTraHang.trangThai) }}
                        </span>
                      </div>
                      <div class="col-12">
                        <strong>Lý do:</strong>
                        <p class="mt-2 mb-0">{{ selectedTraHang.lyDo }}</p>
                      </div>
                      <div v-if="selectedTraHang.ghiChu" class="col-12">
                        <strong>Ghi chú:</strong>
                        <p class="mt-2 mb-0">{{ selectedTraHang.ghiChu }}</p>
                      </div>
                      <div v-if="selectedTraHang.ghiChuAdmin" class="col-12">
                        <strong>Phản hồi từ admin:</strong>
                        <p class="mt-2 mb-0 text-info">{{ selectedTraHang.ghiChuAdmin }}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Danh sách sản phẩm trả -->
              <div class="col-md-6 mb-4">
                <div class="card h-100">
                  <div class="card-header bg-success text-white">
                    <h6 class="mb-0">
                      <i class="fas fa-box me-2"></i>
                      Sản phẩm trả hàng
                    </h6>
                  </div>
                  <div class="card-body">
                    <div class="table-responsive">
                      <table class="table table-sm">
                        <tbody>
                          <tr v-for="(item, index) in selectedTraHang.chiTietTraHangList" :key="index" class="border-bottom">
                            <td class="py-3">
                              <div class="d-flex align-items-center">
                                <div class="me-3">
                                  <img 
                                    :src="getImageUrl(item.anhSanPham)" 
                                    class="rounded-3 shadow-sm" 
                                    alt="product image"
                                    style="width: 60px; height: 60px; object-fit: cover;"
                                    @error="$event.target.src = '/default-product.svg'">
                                </div>
                                <div class="d-flex flex-column justify-content-center">
                                  <h6 class="mb-1 text-sm font-weight-bold">{{ item.tenSanPham || 'Không có thông tin' }}</h6>
                                  <p class="text-xs text-secondary mb-0">
                                    <span class="badge badge-sm bg-light text-dark me-1">{{ item.mauSac || 'N/A' }}</span>
                                    <span class="badge badge-sm bg-light text-dark">{{ item.kichThuoc || 'N/A' }}</span>
                                  </p>
                                  <div class="mt-1">
                                    <small class="text-muted">
                                      Số lượng: <strong>{{ item.soLuong }}</strong> | 
                                      Đơn giá: <strong>{{ formatCurrency(item.donGia) }}</strong>
                                    </small>
                                  </div>
                                  <div v-if="item.lyDo" class="mt-1">
                                    <small class="text-info">Lý do: {{ item.lyDo }}</small>
                                  </div>
                                </div>
                              </div>
                            </td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Minh chứng -->
              <div v-if="selectedTraHang.mediaTraHangList && selectedTraHang.mediaTraHangList.length > 0" class="col-12">
                <div class="card">
                  <div class="card-header bg-warning text-dark">
                    <h6 class="mb-0">
                      <i class="fas fa-images me-2"></i>
                      Minh chứng
                    </h6>
                  </div>
                  <div class="card-body">
                    <div class="row g-3">
                      <div v-for="(media, index) in selectedTraHang.mediaTraHangList" :key="index" class="col-md-3">
                        <div v-if="media.loaiMedia === 1" class="text-center">
                          <img 
                            :src="getMediaUrl(media.duongDan)" 
                            class="img-fluid rounded shadow-sm" 
                            alt="Minh chứng"
                            style="max-height: 150px; cursor: pointer;"
                            @click="xemAnhLon(getMediaUrl(media.duongDan))">
                        </div>
                        <div v-else class="text-center">
                          <video 
                            :src="getMediaUrl(media.duongDan)" 
                            class="img-fluid rounded shadow-sm" 
                            controls
                            style="max-height: 150px;">
                          </video>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="dongModalChiTiet">Đóng</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axiosInstance from '@/utils/api'

// State
const isLoading = ref(false)
const danhSachTraHang = ref([])
const showDetailModal = ref(false)
const selectedTraHang = ref(null)

// Methods
const loadDanhSachTraHang = async () => {
  try {
    isLoading.value = true
    const response = await axiosInstance.get('/api/tra-hang/khach-hang')
    
    if (response.data.success) {
      danhSachTraHang.value = response.data.data
    } else {
      console.error('Lỗi khi tải danh sách trả hàng:', response.data.message)
    }
  } catch (error) {
    console.error('Lỗi khi tải danh sách trả hàng:', error)
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken')
      window.location.href = '/login'
    }
  } finally {
    isLoading.value = false
  }
}

const xemChiTiet = (traHang) => {
  selectedTraHang.value = traHang
  showDetailModal.value = true
}

const dongModalChiTiet = () => {
  showDetailModal.value = false
  selectedTraHang.value = null
}

const getStatusText = (trangThai) => {
  const statusMap = {
    0: 'Chờ xử lý',
    1: 'Đã chấp nhận',
    2: 'Đã từ chối'
  }
  return statusMap[trangThai] || 'Không xác định'
}

const getStatusClass = (trangThai) => {
  const classMap = {
    0: 'bg-warning text-dark',
    1: 'bg-success text-white',
    2: 'bg-danger text-white'
  }
  return classMap[trangThai] || 'bg-secondary text-white'
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatCurrency = (amount) => {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

const getImageUrl = (imagePath) => {
  if (!imagePath) {
    console.log('No image path provided, using default')
    return '/default-product.svg'
  }
  if (imagePath.startsWith('http')) {
    console.log('Using full URL:', imagePath)
    return imagePath
  }
  
  // Xử lý đường dẫn tương đối
  let cleanPath = imagePath
  if (cleanPath.startsWith('/')) {
    cleanPath = cleanPath.substring(1)
  }
  
  const fullUrl = `http://localhost:8080/uploads/${cleanPath}`
  console.log('Generated image URL:', fullUrl)
  return fullUrl
}

const getMediaUrl = (mediaPath) => {
  if (!mediaPath) return ''
  if (mediaPath.startsWith('http')) return mediaPath
  return `http://localhost:8080/uploads/${mediaPath}`
}

const xemAnhLon = (imageUrl) => {
  window.open(imageUrl, '_blank')
}

// Lifecycle
onMounted(() => {
  loadDanhSachTraHang()
})
</script>

<style scoped>
.card {
  transition: transform 0.2s ease-in-out;
}

.card:hover {
  transform: translateY(-2px);
}

.badge {
  font-size: 0.75rem;
}

.modal.show {
  display: block !important;
}

.table-responsive {
  max-height: 400px;
  overflow-y: auto;
}

.img-fluid {
  transition: transform 0.2s ease-in-out;
}

.img-fluid:hover {
  transform: scale(1.05);
}
</style>