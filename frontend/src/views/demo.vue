<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import client from '@/utils/api.js'
import QRCode from 'qrcode'

const route = useRoute()
const router = useRouter()

// State
const qrCodeUrl = ref('')
const hoaDon = ref(null)
const listLichSuHoaDonDto = ref([])
const listChiTietSanPham = ref([])
const ghiChu = ref('')
const showLichSu = ref(false)
const loading = ref(true)
const error = ref(null)

// Computed
const listLichSuThanhToan = computed(() => {
  return listLichSuHoaDonDto.value.filter(item => item.trangThai === 1)
})

const getNextStatus = computed(() => {
  if (!hoaDon.value) return null
  const currentStatus = hoaDon.value.trangThai

  if (hoaDon.value.loaiHoaDon === 0 && currentStatus >= 2) {
    if (currentStatus === 2) return 3
    if (currentStatus === 3) return 4
    if (currentStatus === 4) return 5
    return null
  }

  if (hoaDon.value.loaiHoaDon === 0) {
    if (currentStatus === 1) return 5
    return null
  }

  if (hoaDon.value.loaiHoaDon === 1) {
    const isPrepaid = hoaDon.value.thanhToanTruoc

    if (currentStatus === 0) return 1
    if (currentStatus === 1) return 2
    if (currentStatus === 2) return 3
    if (currentStatus === 3) return 4

    if (currentStatus === 4) {
      if (isPrepaid) {
        return 5
      } else {
        const tong = hoaDon.value.tongTienSauKhiGiam || hoaDon.value.tongTien || 0
        const daThanhToan = hoaDon.value.soTienKhachHangThanhToan || 0
        if (daThanhToan >= tong && (!hoaDon.value.tongTienThua || hoaDon.value.tongTienThua <= 0)) {
          return 5
        }
        return null
      }
    }
  }
  return null
})

// Methods
const generateQRCode = async (text) => {
  try {
    qrCodeUrl.value = await QRCode.toDataURL(text)
  } catch (err) {
    console.error('Lỗi tạo QR code:', err)
  }
}

const printInvoice = () => {
  window.print()
}

const downloadPdf = async () => {
  try {
    const response = await client.get(`/api/hoa-don/export-pdf/${route.params.id}`, {
      responseType: 'blob',
    })

    const blob = new Blob([response.data], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `hoa-don-${hoaDon.value.maHoaDon}.pdf`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (error) {
    console.error("Lỗi khi tải PDF:", error)
    alert("Tải PDF thất bại!")

    if (error.response && error.response.data instanceof Blob && error.response.data.type === "application/json") {
      const reader = new FileReader()
      reader.onload = function() {
        const errorText = reader.result
        console.error("Chi tiết lỗi từ server:", JSON.parse(errorText))
        alert("In PDF thất bại: " + JSON.parse(errorText).message)
      }
      reader.readAsText(error.response.data)
    } else {
      alert("In PDF thất bại!")
    }
  }
}

const fetchHoaDonDetail = async () => {
  try {
    loading.value = true
    error.value = null
    
    const response = await client.get(`/api/hoa-don/detail/${route.params.id}`)
    
    if (!response.data || !response.data.hoaDon) {
      throw new Error('Dữ liệu hóa đơn không hợp lệ')
    }
    
    hoaDon.value = response.data.hoaDon
    listLichSuHoaDonDto.value = response.data.listLichSuHoaDonDto || []
    listChiTietSanPham.value = response.data.listChiTietSanPham || []

    const tong = hoaDon.value.tongTienSauKhiGiam || hoaDon.value.tongTien || 0
    const daThanhToan = hoaDon.value.soTienKhachHangThanhToan || 0
    const tienThua = daThanhToan - tong
    hoaDon.value.tongTienThua = tienThua > 0 ? tienThua : 0

    await generateQRCode(hoaDon.value.maHoaDon)
  } catch (err) {
    console.error('Lỗi khi tải chi tiết hóa đơn:', err)
    error.value = err.message || 'Không thể tải thông tin hóa đơn'
  } finally {
    loading.value = false
  }
}

const getCurrentUserName = () => {
  const user = JSON.parse(localStorage.getItem("user"))
  return user?.tenNhanVien || "admin"
}

const getStatusClass = (status) => {
  if (status === undefined || status === null) return 'text-secondary'
  
  switch (status) {
    case 0: return 'text-warning'
    case 1: return 'text-success'
    case 2: return 'text-warning'
    case 3: return 'text-info'
    case 4: return 'text-success'
    case 5: return 'text-success'
    case 6: return 'text-danger'
    default: return 'text-secondary'
  }
}

const getStatusText = (status) => {
  if (status === undefined || status === null) return 'Không xác định'
  
  const statuses = [
    'Chờ xác nhận',
    'Đã xác nhận',
    'Chờ vận chuyển',
    'Đang vận chuyển',
    'Đã giao hàng',
    'Hoàn thành',
    'Trả hàng/Hủy'
  ]
  
  return statuses[status] || 'Không xác định'
}

const changeStatus = async () => {
  if (!hoaDon.value) return
  
  const nextStatus = getNextStatus.value
  if (nextStatus === null) return
  
  if (
    hoaDon.value.trangThai === 4 &&
    [4, 1, 2].includes(nextStatus) &&
    hoaDon.value.tongTienThua &&
    hoaDon.value.tongTienThua > 0
  ) {
    alert("Khách đã thanh toán dư. Cần hoàn tiền trước khi chuyển trạng thái!")
    return
  }

  if (hoaDon.value.trangThai === 3 && (!hoaDon.value.soTienKhachHangThanhToan || hoaDon.value.soTienKhachHangThanhToan <= 0)) {
    alert("Bạn cần thanh toán trước khi chuyển trạng thái!")
    return
  }

  const statusText = getStatusText(nextStatus)
  ghiChu.value = prompt(`Nhập ghi chú cho trạng thái ${statusText} (không bắt buộc):`) || ''

  const confirmed = confirm(`Bạn có chắc muốn chuyển sang trạng thái ${statusText}?`)
  if (!confirmed) return

  hoaDon.value.trangThai = nextStatus
  await capNhatTrangThai()
}

const chuyenDenGiaoHang = async () => {
  if (!hoaDon.value) return
  
  if (hoaDon.value.tongTienThua && hoaDon.value.tongTienThua > 0) {
    alert("Cần hoàn tiền thừa cho khách trước khi chuyển sang giao hàng!")
    return
  }

  if (confirm("Bạn có chắc muốn chuyển sang trạng thái giao hàng?")) {
    hoaDon.value.trangThai = 2
    ghiChu.value = "Chuyển từ tại quầy sang giao hàng"
    await capNhatTrangThai()
  }
}

const undoStatus = async () => {
  if (!hoaDon.value) return
  
  if (
    hoaDon.value.trangThai === 0 ||
    (hoaDon.value.loaiHoaDon === 0 && hoaDon.value.trangThai === 5)
  ) {
    alert("Không thể hoàn tác ở trạng thái hiện tại!")
    return
  }

  const confirmed = confirm("Bạn có chắc muốn hoàn tác trạng thái trước đó?")
  if (!confirmed) return

  const prevStatus = hoaDon.value.trangThai - 1
  hoaDon.value.trangThai = prevStatus

  const user = JSON.parse(localStorage.getItem("user"))
  const tenNhanVien = user?.tenNhanVien || "admin"
  
  listLichSuHoaDonDto.value.pop()
  
  try {
    await client.put(`/api/hoa-don/cap-nhat-trang-thai/${hoaDon.value.id}`, null, {
      params: {
        trangThai: prevStatus,
        ghiChu: "Hoàn tác trạng thái",
        tenNhanVien
      }
    })

    alert("Hoàn tác thành công!")
    await fetchHoaDonDetail()
  } catch (err) {
    console.error("Lỗi khi hoàn tác:", err)
    alert("Hoàn tác thất bại!")
  }
}

const getPaymentMethodIcon = (method) => {
  const methodIcons = {
    'Tiền mặt': 'fas fa-money-bill-wave',
    'Chuyển khoản': 'fas fa-university',
    'Kết hợp': 'fas fa-link',
    'default': 'fas fa-credit-card'
  }
  
  return methodIcons[method] || methodIcons.default
}

const capNhatTrangThai = async () => {
  if (!hoaDon.value) return
  
  try {
    const id = hoaDon.value.id
    const trangThai = hoaDon.value.trangThai
    // const ghiChuCapNhat = ghiChu.value || ''

    if (!id || typeof trangThai !== 'number') {
      alert("Thiếu thông tin cập nhật trạng thái!")
      return
    }

    const user = JSON.parse(localStorage.getItem("user"))
    const tenNhanVien = user?.tenNhanVien || "admin"

    await client.put(`/api/hoa-don/cap-nhat-trang-thai/${hoaDon.value.id}`, null, {
      params: {
        trangThai: hoaDon.value.trangThai,
        ghiChu: ghiChu.value,
        tenNhanVien
      }
    })
    
    alert("Cập nhật trạng thái thành công!")
    await fetchHoaDonDetail()
  } catch (err) {
    console.error("Lỗi khi cập nhật trạng thái:", err)

    if (err.response) {
      const errorMsg = err.response.data?.message || err.response.statusText
      alert(`Lỗi từ server: ${errorMsg}`)
    } else {
      alert("Cập nhật trạng thái thất bại!")
    }
  }
}

const cancelOrReturn = async () => {
  if (!hoaDon.value) return
  
  const isCancellable = [0, 1, 2].includes(hoaDon.value.trangThai)
  const action = isCancellable ? 'Hủy đơn' : 'Trả hàng'

  const confirmed = confirm(`Bạn có chắc muốn ${action.toLowerCase()}?`)
  if (!confirmed) return

  ghiChu.value = prompt(`Nhập lý do ${action.toLowerCase()} (không bắt buộc):`) || ''

  hoaDon.value.trangThai = 6
  await capNhatTrangThai()
}

const processPayment = async () => {
  if (!hoaDon.value) return
  
  const tongTien = hoaDon.value.tongTien
  const daThanhToan = parseFloat(prompt(`Số tiền đã thanh toán (Tổng tiền: ${formatCurrency(tongTien)}):`) || 0)

  if (isNaN(daThanhToan)) {
    alert("Số tiền không hợp lệ!")
    return
  }

  if (daThanhToan < tongTien) {
    alert("Khách hàng cần thanh toán đủ số tiền!")
    return
  }

  const hinhThucInput = prompt("Hình thức thanh toán (chuyen_khoan/tien_mat/ket_hop):")?.trim() || 'tien_mat'
  const mapHinhThuc = {tien_mat: '111111', chuyen_khoan: '222222', ket_hop: '333333'}
  const hinhThucId = mapHinhThuc[hinhThucInput]
  
  if (!hinhThucId) {
    alert("Hình thức thanh toán không hợp lệ!")
    return
  }

  let maGiaoDich = ''
  if (hinhThucInput === 'chuyen_khoan' || hinhThucInput === 'ket_hop') {
    maGiaoDich = prompt("Mã giao dịch chuyển khoản:") || ''
  }

  const confirmMessage = `Xác nhận thanh toán:
  - Tổng tiền: ${formatCurrency(tongTien)}
  - Đã thanh toán: ${formatCurrency(daThanhToan)}
  - Hình thức: ${hinhThucInput}
  ${maGiaoDich ? `- Mã giao dịch: ${maGiaoDich}` : ''}
  - Nhân viên: ${getCurrentUserName()}`

  if (!confirm(confirmMessage)) return

  try {
    await client.post(`/api/hoa-don/thanh-toan/${hoaDon.value.id}`, null, {
      params: {
        tongTien,
        daThanhToan,
        hinhThucId,
        maGiaoDich,
        ghiChu: ghiChu.value,
        tenNhanVien: getCurrentUserName()
      }
    })

    hoaDon.value.trangThai = 4
    alert("Thanh toán thành công!")
    await fetchHoaDonDetail()
  } catch (err) {
    console.error("Lỗi khi thanh toán:", err)
    const errorMsg = err.response?.data?.message || "Thanh toán thất bại!"
    alert(errorMsg)
  }
}

const refundExcess = async () => {
  if (!hoaDon.value) return
  
  const soTienHoan = hoaDon.value.tongTienThua
  if (soTienHoan <= 0) {
    alert("Không có tiền thừa để hoàn!")
    return
  }

  const confirmed = confirm(`Xác nhận hoàn ${formatCurrency(soTienHoan)} cho khách?`)
  if (!confirmed) return

  try {
    await client.post(`/api/hoa-don/hoan-tien/${hoaDon.value.id}`, null, {
      params: {
        soTienHoan,
        ghiChu: "Hoàn tiền thừa cho khách",
        tenNhanVien: getCurrentUserName()
      }
    })

    hoaDon.value.tongTienThua = 0
    alert("Hoàn tiền thành công!")
    await fetchHoaDonDetail()
  } catch (err) {
    console.error("Lỗi khi hoàn tiền:", err)
    alert(err.response?.data?.message || "Hoàn tiền thất bại!")
  }
}

const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

const formatDate = (date) => {
  return date
    ? new Date(date).toLocaleString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
      })
    : ''
}

// Lifecycle
onMounted(() => {
  if (!route.params.id) {
    router.push('/quan-ly/hoa-don')
    return
  }
  fetchHoaDonDetail()
})
</script>

<template>
  <div class="invoice-detail-container">
    <!-- Loading state -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Đang tải thông tin hóa đơn...</p>
    </div>
    
    <!-- Error state -->
    <div v-else-if="error" class="error-message">
      <i class="fas fa-exclamation-circle"></i>
      <p>{{ error }}</p>
      <button @click="fetchHoaDonDetail" class="btn btn-retry">
        <i class="fas fa-sync-alt"></i> Thử lại
      </button>
    </div>
    
    <!-- Main content -->
    <template v-else-if="hoaDon">
      <!-- Header -->
      <div class="invoice-header">
        <div class="header-left">
          <h1 class="invoice-title">Chi tiết hóa đơn</h1>
          <div class="invoice-meta">
            <span class="badge" :class="getStatusClass(hoaDon.trangThai)">
              {{ getStatusText(hoaDon.trangThai) }}
            </span>
            <span class="invoice-number">#{{ hoaDon.maHoaDon }}</span>
            <span class="invoice-date">{{ formatDate(hoaDon.ngayTao) }}</span>
          </div>
        </div>
        <div class="header-right">
          <div class="qr-code">
            <img :src="qrCodeUrl" alt="QR Code" width="80" height="80">
            <p class="qr-label">Mã hóa đơn</p>
          </div>
        </div>
      </div>

      <!-- Progress tracker -->
      <div class="progress-tracker">
        <div class="progress-bar" :style="{ width: `${((hoaDon.trangThai + 1) / 6) * 100}%` }"></div>
        <div class="progress-steps">
          <div v-for="(step, index) in progressSteps" :key="index" 
               class="step" :class="{ active: isStepActive(index) }">
            <div class="step-icon">
              <i :class="step.icon"></i>
            </div>
            <div class="step-label">{{ step.label }}</div>
          </div>
        </div>
      </div>

      <!-- Action buttons -->
      <div class="action-buttons">
        <button v-if="hoaDon.trangThai > 0 && hoaDon.trangThai < 5 && !(hoaDon.loaiHoaDon === 0 && hoaDon.trangThai === 5)"
                @click="undoStatus" class="btn btn-undo">
          <i class="fas fa-undo"></i> Hoàn tác
        </button>
        
        <button v-if="getNextStatus !== null && hoaDon.trangThai < 5" @click="changeStatus" class="btn btn-next-status">
          <i class="fas fa-arrow-right"></i> {{ getStatusText(getNextStatus) }}
        </button>
        
        <button v-if="[0, 1, 2, 5, 6].includes(hoaDon.trangThai)" @click="cancelOrReturn" class="btn btn-danger">
          <i class="fas fa-times-circle"></i> {{ [5, 6].includes(hoaDon.trangThai) ? 'Trả hàng' : 'Hủy đơn' }}
        </button>
        
        <button v-if="hoaDon.trangThai === 3 && hoaDon.loaiHoaDon === 1 && !hoaDon.thanhToanTruoc" 
                @click="processPayment" class="btn btn-success">
          <i class="fas fa-money-bill-wave"></i> Thanh toán
        </button>
        
        <button v-if="hoaDon.trangThai === 4 && hoaDon.tongTienThua > 0" @click="refundExcess" class="btn btn-warning">
          <i class="fas fa-exchange-alt"></i> Hoàn tiền {{ formatCurrency(hoaDon.tongTienThua) }}
        </button>
        
        <button v-if="hoaDon.loaiHoaDon === 0 && hoaDon.trangThai === 1" @click="chuyenDenGiaoHang" class="btn btn-info">
          <i class="fas fa-truck"></i> Chuyển đến giao hàng
        </button>
        
        <button @click="showLichSu = true" class="btn btn-history">
          <i class="fas fa-history"></i> Lịch sử
        </button>
        
        <div class="print-actions">
          <button @click="printInvoice" class="btn btn-print">
            <i class="fas fa-print"></i> In hóa đơn
          </button>
          <button @click="downloadPdf" class="btn btn-pdf">
            <i class="fas fa-file-pdf"></i> Tải PDF
          </button>
        </div>
      </div>

      <!-- Invoice sections -->
      <div class="invoice-sections">
        <!-- Customer info -->
        <div class="section customer-info">
          <div class="section-header">
            <h2><i class="fas fa-user"></i> Thông tin khách hàng</h2>
          </div>
          <div class="section-body">
            <div class="info-grid">
              <div class="info-item">
                <label>Tên khách hàng</label>
                <p>{{ hoaDon.khachHang?.tenKhachHang || 'Khách lẻ' }}</p>
              </div>
              <div class="info-item">
                <label>Số điện thoại</label>
                <p>{{ hoaDon.khachHang?.sdt || 'N/A' }}</p>
              </div>
              <div class="info-item">
                <label>Email</label>
                <p>{{ hoaDon.khachHang?.email || 'N/A' }}</p>
              </div>
              <div class="info-item">
                <label>Địa chỉ</label>
                <p>{{ hoaDon.khachHang?.diaChis?.find(dc => dc.trangThai === 1)?.diaChiChiTiet || 'Chưa có địa chỉ' }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Order summary -->
        <div class="section order-summary">
          <div class="section-header">
            <h2><i class="fas fa-receipt"></i> Tóm tắt đơn hàng</h2>
          </div>
          <div class="section-body">
            <div class="summary-grid">
              <div class="summary-item">
                <label>Tổng tiền hàng</label>
                <p>{{ formatCurrency(hoaDon.tongTien) }}</p>
              </div>
              <div class="summary-item">
                <label>Phí vận chuyển</label>
                <p>{{ formatCurrency(hoaDon.phiVanChuyen) }}</p>
              </div>
              <div class="summary-item">
                <label>Giảm giá</label>
                <p>{{ hoaDon.soTienGiam }}%</p>
              </div>
              <div class="summary-item highlight">
                <label>Tổng thanh toán</label>
                <p>{{ formatCurrency(hoaDon.tongTienSauKhiGiam || hoaDon.tongTien) }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Products list -->
        <div class="section products-list">
          <div class="section-header">
            <h2><i class="fas fa-box-open"></i> Sản phẩm đã đặt</h2>
          </div>
          <div class="section-body">
            <div class="product-card" v-for="(item, index) in listChiTietSanPham" :key="index">
              <div class="product-image">
                <img :src="item.maAnh" :alt="item.tenChiTietSp" width="80" height="80">
              </div>
              <div class="product-details">
                <h3>{{ item.tenChiTietSp }}</h3>
                <p class="product-sku">Mã SP: {{ item.maChiTietSp }}</p>
                <div class="product-meta">
                  <span class="price">{{ formatCurrency(item.donGia) }}</span>
                  <span class="quantity">x {{ item.soLuong }}</span>
                  <span class="total">{{ formatCurrency(item.donGia * item.soLuong) }}</span>
                </div>
              </div>
              <div class="product-status">
                <span :class="['status-badge', item.trangThai === 1 ? 'delivered' : 'pending']">
                  {{ item.trangThai === 1 ? 'Đã giao' : 'Chưa giao' }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Payment history -->
        <div class="section payment-history" v-if="listLichSuThanhToan.length > 0">
          <div class="section-header">
            <h2><i class="fas fa-credit-card"></i> Lịch sử thanh toán</h2>
          </div>
          <div class="section-body">
            <div class="payment-card" v-for="(payment, index) in listLichSuThanhToan" :key="index">
        <div class="payment-method">
  <i :class="getPaymentMethodIcon(payment.tenHinhThuc)"></i>
  <span>{{ payment.tenHinhThuc || 'Tiền mặt' }}</span>
</div>
              <div class="payment-amount">
                {{ formatCurrency(payment.soTienThanhToan || payment.tongTien) }}
              </div>
              <div class="payment-date">
                {{ formatDate(payment.ngayCapNhat || payment.ngayThanhToan) }}
              </div>
              <div class="payment-status" :class="payment.trangThai ? 'success' : 'failed'">
                {{ payment.trangThai ? 'Thành công' : 'Thất bại' }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Print area -->
      <div id="print-area" class="print-template">
        <!-- Print content here -->
      </div>

      <!-- Status history modal -->
      <div v-if="showLichSu" class="modal-overlay" @click.self="showLichSu = false">
        <div class="modal-content">
          <div class="modal-header">
            <h3>Lịch sử trạng thái đơn hàng</h3>
            <button @click="showLichSu = false" class="close-btn">
              <i class="fas fa-times"></i>
            </button>
          </div>
          <div class="modal-body">
            <div class="timeline">
              <p v-if="hoaDon.tongTienThua > 0" class="text-danger fw-bold">
                Khách thanh toán dư: {{ formatCurrency(hoaDon.tongTienThua) }} – cần hoàn tiền trước khi chuyển trạng thái!
              </p>
              <div v-for="(item, index) in listLichSuHoaDonDto" :key="index" class="timeline-item">
                <div class="timeline-marker" :class="getStatusClass(item.trangThai)"></div>
                <div class="timeline-content">
                  <div class="timeline-header">
                    <h4>{{ getStatusText(item.trangThai) }}</h4>
                    <span class="timeline-date">{{ formatDate(item.ngayCapNhat) }}</span>
                  </div>
                  <p v-if="item.ghiChu" class="timeline-note">{{ item.ghiChu }}</p>
                  <div class="timeline-footer">
                    <span class="timeline-user">NV: {{ item.tenNhanVien }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
/* Variables */
:root {
  --primary-color: #4f46e5;
  --success-color: #10b981;
  --warning-color: #f59e0b;
  --danger-color: #ef4444;
  --info-color: #3b82f6;
  --light-color: #f8fafc;
  --dark-color: #1e293b;
  --gray-color: #64748b;
  --border-color: #e2e8f0;
}

/* Base styles */
.invoice-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  color: var(--dark-color);
}

/* Loading state */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 5px solid #f3f3f3;
  border-top: 5px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Error state */
.error-message {
  padding: 2rem;
  text-align: center;
  background-color: #f8d7da;
  border-radius: 0.5rem;
  color: var(--danger-color);
  margin: 1rem 0;
}

.error-message i {
  font-size: 2rem;
  margin-bottom: 1rem;
}

.btn-retry {
  margin-top: 1rem;
  background-color: var(--danger-color);
  color: white;
}

/* Header */
.invoice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid var(--border-color);
}

.invoice-title {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0;
  color: var(--dark-color);
}

.invoice-meta {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-top: 0.5rem;
}

.invoice-number {
  font-weight: 500;
  color: var(--gray-color);
}

.invoice-date {
  color: var(--gray-color);
}

.badge {
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
  font-size: 0.875rem;
  font-weight: 500;
}

.text-warning {
  background-color: #fef3c7;
  color: #92400e;
}

.text-success {
  background-color: #d1fae5;
  color: #065f46;
}

.text-info {
  background-color: #dbeafe;
  color: #1e40af;
}

.text-danger {
  background-color: #fee2e2;
  color: #b91c1c;
}

.text-secondary {
  background-color: #e2e8f0;
  color: #475569;
}

.qr-code {
  text-align: center;
}

.qr-label {
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: var(--gray-color);
}

/* Progress tracker */
.progress-tracker {
  position: relative;
  height: 4px;
  background-color: var(--border-color);
  border-radius: 2px;
  margin: 2rem 0;
}

.progress-bar {
  position: absolute;
  height: 100%;
  background-color: var(--primary-color);
  border-radius: 2px;
  transition: width 0.3s ease;
}

.progress-steps {
  display: flex;
  justify-content: space-between;
  margin-top: 1rem;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 1;
}

.step-icon {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 50%;
  background-color: white;
  border: 2px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 0.5rem;
  color: var(--gray-color);
}

.step.active .step-icon {
  border-color: var(--primary-color);
  background-color: var(--primary-color);
  color: white;
}

.step-label {
  font-size: 0.875rem;
  color: var(--gray-color);
  text-align: center;
  max-width: 100px;
}

.step.active .step-label {
  color: var(--dark-color);
  font-weight: 500;
}

/* Action buttons */
.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-bottom: 2rem;
}

.btn {
  padding: 0.75rem 1.25rem;
  border-radius: 0.5rem;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn i {
  font-size: 0.875rem;
}

.btn-next-status {
  background-color: var(--primary-color);
  color: white;
}

.btn-next-status:hover {
  background-color: #4338ca;
}

.btn-undo {
  background-color: var(--warning-color);
  color: white;
}

.btn-undo:hover {
  background-color: #d97706;
}

.btn-history {
  background-color: var(--gray-color);
  color: white;
}

.btn-history:hover {
  background-color: #475569;
}

.btn-danger {
  background-color: var(--danger-color);
  color: white;
}

.btn-danger:hover {
  background-color: #b91c1c;
}

.btn-success {
  background-color: var(--success-color);
  color: white;
}

.btn-success:hover {
  background-color: #0d9488;
}

.btn-warning {
  background-color: var(--warning-color);
  color: white;
}

.btn-warning:hover {
  background-color: #d97706;
}

.btn-info {
  background-color: var(--info-color);
  color: white;
}

.btn-info:hover {
  background-color: #1d4ed8;
}

.print-actions {
  margin-left: auto;
  display: flex;
  gap: 0.75rem;
}

.btn-print {
  background-color: var(--info-color);
  color: white;
}

.btn-print:hover {
  background-color: #2563eb;
}

.btn-pdf {
  background-color: var(--danger-color);
  color: white;
}

.btn-pdf:hover {
  background-color: #dc2626;
}

/* Sections */
.invoice-sections {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}

.section {
  background-color: white;
  border-radius: 0.75rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.section-header {
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--border-color);
  background-color: #f8fafc;
}

.section-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.section-header i {
  color: var(--primary-color);
}

.section-body {
  padding: 1.5rem;
}

/* Customer info */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1.5rem;
}

.info-item {
  background-color: #f8fafc;
  padding: 1rem;
  border-radius: 0.5rem;
}

.info-item label {
  display: block;
  font-size: 0.875rem;
  color: var(--gray-color);
  margin-bottom: 0.25rem;
}

.info-item p {
  margin: 0;
  font-weight: 500;
}

/* Order summary */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.summary-item {
  padding: 1rem;
  border-radius: 0.5rem;
  background-color: #f8fafc;
}

.summary-item.highlight {
  background-color: var(--primary-color);
  color: white;
}

.summary-item label {
  display: block;
  font-size: 0.875rem;
  margin-bottom: 0.5rem;
}

.summary-item.highlight label {
  color: rgba(255, 255, 255, 0.9);
}

.summary-item p {
  margin: 0;
  font-size: 1.125rem;
  font-weight: 600;
}

/* Products list */
.product-card {
  display: flex;
  gap: 1.5rem;
  padding: 1rem 0;
  border-bottom: 1px solid var(--border-color);
  align-items: center;
}

.product-card:last-child {
  border-bottom: none;
}

.product-image {
  flex-shrink: 0;
}

.product-image img {
  border-radius: 0.5rem;
  object-fit: cover;
}

.product-details {
  flex-grow: 1;
}

.product-details h3 {
  margin: 0 0 0.25rem 0;
  font-size: 1rem;
}

.product-sku {
  margin: 0 0 0.5rem 0;
  font-size: 0.875rem;
  color: var(--gray-color);
}

.product-meta {
  display: flex;
  gap: 1rem;
  font-weight: 500;
}

.product-meta .price {
  color: var(--primary-color);
}

.product-meta .quantity {
  color: var(--gray-color);
}

.product-meta .total {
  margin-left: auto;
  font-weight: 600;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-badge.delivered {
  background-color: #d1fae5;
  color: #065f46;
}

.status-badge.pending {
  background-color: #fef3c7;
  color: #92400e;
}

/* Payment history */
.payment-card {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 1rem;
  border-radius: 0.5rem;
  background-color: #f8fafc;
  margin-bottom: 0.75rem;
}

.payment-card:last-child {
  margin-bottom: 0;
}

.payment-method {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
}

.payment-method i {
  font-size: 1.25rem;
  color: var(--primary-color);
}

.payment-amount {
  font-weight: 600;
  min-width: 100px;
  text-align: right;
}

.payment-date {
  color: var(--gray-color);
  min-width: 150px;
}

.payment-status {
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
  font-size: 0.75rem;
  font-weight: 500;
}

.payment-status.success {
  background-color: #d1fae5;
  color: #065f46;
}

.payment-status.failed {
  background-color: #fee2e2;
  color: #b91c1c;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 0.75rem;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.modal-header {
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.25rem;
  color: var(--gray-color);
  cursor: pointer;
  padding: 0.25rem;
}

.modal-body {
  padding: 1.5rem;
}

/* Timeline */
.timeline {
  position: relative;
  padding-left: 1.5rem;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 0.5rem;
  top: 0;
  bottom: 0;
  width: 2px;
  background-color: var(--border-color);
}

.timeline-item {
  position: relative;
  padding-bottom: 1.5rem;
}

.timeline-marker {
  position: absolute;
  left: -1.5rem;
  width: 1.25rem;
  height: 1.25rem;
  border-radius: 50%;
  background-color: var(--primary-color);
  border: 3px solid white;
}

.timeline-content {
  padding: 0.75rem 1rem;
  background-color: #f8fafc;
  border-radius: 0.5rem;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.timeline-header h4 {
  margin: 0;
  font-size: 1rem;
}

.timeline-date {
  font-size: 0.875rem;
  color: var(--gray-color);
}

.timeline-note {
  margin: 0.5rem 0 0 0;
  font-size: 0.875rem;
  color: var(--gray-color);
}

.timeline-footer {
  margin-top: 0.5rem;
  font-size: 0.75rem;
  color: var(--gray-color);
  text-align: right;
}

/* Print styles */
@media print {
  body * {
    visibility: hidden;
  }
  
  #print-area, #print-area * {
    visibility: visible;
  }
  
  #print-area {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    padding: 0;
    margin: 0;
  }
  
  .no-print {
    display: none !important;
  }
}

/* Responsive */
@media (max-width: 768px) {
  .invoice-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .qr-code {
    align-self: flex-end;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .print-actions {
    margin-left: 0;
    margin-top: 0.5rem;
  }
  
  .info-grid, .summary-grid {
    grid-template-columns: 1fr;
  }
  
  .product-card {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .payment-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
  
  .payment-amount, .payment-date {
    text-align: left;
    width: 100%;
  }
}
</style>