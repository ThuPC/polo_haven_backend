<script setup>
import { ref, onMounted, computed, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import client from '@/utils/api.js';
import { Html5QrcodeScanner } from 'html5-qrcode';

const router = useRouter();
const listHoaDon = ref([]);
const filters = ref({
  keyword: '',
  loaiHoaDon: '',
  trangThai: '',
  fromDate: '',
  toDate: '',
});
const currentPage = ref(0);
const size = ref(10);
const totalPages = ref(0);
const loading = ref(false);
const expandedFilters = ref(false); // Thêm trạng thái mở rộng filter
const sortField = ref('ngayTao'); // Thêm sắp xếp
const sortDirection = ref('desc'); // Thêm hướng sắp xếp

const tabCounts = ref({});
const activeTab = ref('');

const notification = ref({
  show: false,
  type: 'success',
  message: ''
});

const showQRScanner = ref(false);
let qrScannerInstance = null;

// Computed properties
const orderedTabList = computed(() => {
  const entries = Object.entries({
    '': 'Tất cả',
    0: 'Chờ xác nhận',
    1: 'Đã xác nhận',
    2: 'Chờ vận chuyển',
    3: 'Đang vận chuyển',
    4: 'Đã giao hàng',
    5: 'Hoàn thành',
    6: 'Hủy',
    returnGroup: 'Trả hàng' // Tab gom nhóm
  });
  const allTab = entries.find(([key]) => key === '');
  const restTabs = entries.filter(([key]) => key !== '');
  return [allTab, ...restTabs];
});

const returnTabCount = computed(() => {
  return listHoaDon.value.filter(hd => [7, 8, 9, 10].includes(hd.trangThai)).length;
});

const debounceTimer = ref(null);
const selectedHoaDons = ref([]);

// Sắp xếp dữ liệu
const sortedHoaDons = computed(() => {
  let data = [...listHoaDon.value];
  // Nếu đang ở tab trả hàng, chỉ lấy các trạng thái 7,8,9,10
  if (filters.value.trangThai === 'returnGroup') {
    data = data.filter(hd => [7, 8, 9, 10].includes(hd.trangThai));
  }
  // ... giữ nguyên logic sort cũ
  let modifier = sortDirection.value === 'asc' ? 1 : -1;

  if (sortField.value === 'tongTien') {
    return data.sort((a, b) => (a.tongTien - b.tongTien) * modifier);
  } else if (sortField.value === 'ngayTao') {
    return data.sort((a, b) => (new Date(a.ngayTao) - new Date(b.ngayTao)) * modifier);
  } else if (sortField.value === 'trangThai') {
    return data.sort((a, b) => (a.trangThai - b.trangThai) * modifier);
  }
  return data;
});

// Thêm hàm sắp xếp
const sortData = (field) => {
  if (sortField.value === field) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc';
  } else {
    sortField.value = field;
    sortDirection.value = 'asc';
  }
};

// Thêm hàm getSortIcon
const getSortIcon = (field) => {
  if (sortField.value !== field) return 'fa-sort';
  return sortDirection.value === 'asc' ? 'fa-sort-up' : 'fa-sort-down';
};

const visiblePages = computed(() => {
  const pages = [];
  const total = totalPages.value;
  const current = currentPage.value + 1;

  if (total <= 7) {
    for (let i = 1; i <= total; i++) pages.push(i);
  } else {
    if (current <= 4) {
      for (let i = 1; i <= 5; i++) pages.push(i);
      pages.push('...');
      pages.push(total);
    } else if (current >= total - 3) {
      pages.push(1);
      pages.push('...');
      for (let i = total - 4; i <= total; i++) pages.push(i);
    } else {
      pages.push(1);
      pages.push('...');
      for (let i = current - 1; i <= current + 1; i++) pages.push(i);
      pages.push('...');
      pages.push(total);
    }
  }
  return pages.filter(page => page !== '...');
});

// Methods
const showNotification = (type, message) => {
  notification.value = { show: true, type, message };
  setTimeout(hideNotification, 3000);
};

const hideNotification = () => {
  notification.value = { show: false, type: '', message: '' };
};

const handleFilterChange = () => {
  if (debounceTimer.value) {
    clearTimeout(debounceTimer.value);
  }

  debounceTimer.value = setTimeout(() => {
    currentPage.value = 0;
    fetchHoaDon();
  }, 500);
};

const resetFilters = () => {
  if (debounceTimer.value) {
    clearTimeout(debounceTimer.value);
    debounceTimer.value = null;
  }

  filters.value = {
    keyword: '',
    loaiHoaDon: '',
    trangThai: '',
    fromDate: '',
    toDate: ''
  };
  currentPage.value = 0;
  activeTab.value = '';
  expandedFilters.value = false;
  fetchHoaDon();
};

const fetchTabCounts = async () => {
  try {
    const res = await client.get("/api/hoa-don/thong-ke-trang-thai");
    tabCounts.value = res.data;
  } catch (error) {
    console.error("Lỗi khi lấy số lượng trạng thái:", error);
    showNotification('error', 'Không thể tải thống kê trạng thái');
  }
};

const selectTab = (status) => {
  activeTab.value = status;
  if (status === 'returnGroup') {
    filters.value.trangThai = 'returnGroup';
    // Khi chọn tab trả hàng, truyền trạng thái '' để lấy tất cả hóa đơn
    fetchHoaDon('all');
  } else {
    filters.value.trangThai = status === '' ? '' : Number(status);
    fetchHoaDon();
  }
  currentPage.value = 0;
};

const toggleHoaDonSelection = (id) => {
  const index = selectedHoaDons.value.indexOf(id);
  if (index > -1) {
    selectedHoaDons.value.splice(index, 1);
  } else {
    selectedHoaDons.value.push(id);
  }
};

const toggleSelectAll = (event) => {
  if (event.target.checked) {
    selectedHoaDons.value = listHoaDon.value.map(hd => hd.id);
  } else {
    selectedHoaDons.value = [];
  }
};

const isChecked = (id) => selectedHoaDons.value.includes(id);

const exportToExcel = async () => {
  if (selectedHoaDons.value.length === 0) {
    showNotification('error', 'Vui lòng chọn ít nhất một hóa đơn');
    return;
  }

  try {
    loading.value = true;
    const response = await client.post('/api/hoa-don/export-excel', selectedHoaDons.value, {
      responseType: 'blob',
      headers: { 'Content-Type': 'application/json' }
    });

    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });

    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'danh_sach_hoa_don.xlsx';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);

    showNotification('success', 'Xuất Excel thành công!');
  } catch (error) {
    console.error("Lỗi khi xuất Excel:", error);
    showNotification('error', 'Xuất Excel thất bại!');
  } finally {
    loading.value = false;
  }
};

const startQRScanner = async () => {
  showQRScanner.value = true;
  await nextTick();

  qrScannerInstance = new Html5QrcodeScanner("qr-reader", {
    fps: 10,
    qrbox: { width: 250, height: 250 }
  });

  qrScannerInstance.render(
    (decodedText) => {
      filters.value.keyword = decodedText;
      stopQRScanner();
      fetchHoaDon();
    },
    (error) => console.warn("Scan error:", error)
  );
};

const stopQRScanner = () => {
  showQRScanner.value = false;
  if (qrScannerInstance) {
    qrScannerInstance.clear().catch(err => console.error("Clear error", err));
  }
};


const formatDateForQuery = (date, isToDate = false) => {
  if (!date) return null;
  const d = new Date(date);
  if (isNaN(d.getTime())) return null;

  if (isToDate) {
    d.setHours(23, 59, 59, 999);
  } else {
    d.setHours(0, 0, 0, 0);
  }

  return d.toISOString();
};

const fetchHoaDon = async (mode) => {
  try {
    loading.value = true;
    const params = {
      page: currentPage.value,
      size: size.value,
      sort: `${sortField.value},${sortDirection.value}`
    };

    if (filters.value.keyword) params.keyword = filters.value.keyword;
    if (filters.value.loaiHoaDon !== '') params.loaiHoaDon = filters.value.loaiHoaDon;
    // Nếu mode === 'all', không truyền trạng thái
    if (mode !== 'all' && filters.value.trangThai !== '' && filters.value.trangThai !== 'returnGroup') {
      params.trangThai = filters.value.trangThai;
    }
    if (filters.value.fromDate) {
      const formatted = formatDateForQuery(filters.value.fromDate);
      if (formatted) params.fromDate = formatted;
    }
    if (filters.value.toDate) {
      const formatted = formatDateForQuery(filters.value.toDate, true);
      if (formatted) params.toDate = formatted;
    }

    const response = await client.get('/api/hoa-don/hien-thi', { params });
    listHoaDon.value = response.data.content || [];
    totalPages.value = response.data.totalPages || 0;
  } catch (error) {
    console.error('Lỗi khi tải dữ liệu hóa đơn:', error);
    showNotification('error', 'Không thể tải danh sách hóa đơn');
    listHoaDon.value = [];
  } finally {
    loading.value = false;
  }
};

const prevPage = () => {
  if (currentPage.value > 0) {
    currentPage.value--;
    fetchHoaDon();
  }
};

const nextPage = () => {
  if (currentPage.value + 1 < totalPages.value) {
    currentPage.value++;
    fetchHoaDon();
  }
};

const goToPage = (pageNum) => {
  if (pageNum >= 0 && pageNum < totalPages.value) {
    currentPage.value = pageNum;
    fetchHoaDon();
  }
};

const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const formatDate = (date) => {
  return new Date(date).toLocaleDateString('vi-VN');
};

const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'status-warning';
    case 1: return 'status-success';
    case 2: return 'status-warning';
    case 3: return 'status-info';
    case 4: return 'status-success';
    case 5: return 'status-success';
    case 6: return 'status-danger';
    case 7: return 'status-danger';
    case 8: return 'status-info';
    case 9: return 'status-success';
    case 10: return 'status-danger';
    case 11: return 'status-warning';
    default: return 'status-secondary';
  }
};

const getStatusText = (status) => {
  switch (status) {
    case 0: return 'Chờ xác nhận';
    case 1: return 'Đã xác nhận';
    case 2: return 'Chờ vận chuyển';
    case 3: return 'Đang vận chuyển';
    case 4: return 'Đã giao hàng';
    case 5: return 'Hoàn thành';
    case 6: return 'Hủy';
    case 7: return 'Yêu cầu trả hàng';
    case 8: return 'Đang trả hàng';
    case 9: return 'Trả hàng thành công';
    case 10: return 'Từ chối trả hàng';
    case 11: return 'Khách chưa xác nhận';
    default: return 'Không xác định';
  }
};

const viewDetail = (id) => {
  router.push(`/api/hoa-don/detail/${id}`);
};

const printSingleInvoice = async (id) => {
  try {
    // Gọi API in hóa đơn
    const response = await client.get(`/api/hoa-don/print/${id}`, {
      responseType: 'blob' // Nhận file PDF dưới dạng blob
    });

    // Tạo URL tạm từ blob và mở trong tab mới
    const pdfBlob = new Blob([response.data], { type: 'application/pdf' });
    const pdfUrl = URL.createObjectURL(pdfBlob);
    window.open(pdfUrl, '_blank');

    // Giải phóng bộ nhớ sau 5 giây
    setTimeout(() => URL.revokeObjectURL(pdfUrl), 5000);
  } catch (error) {
    console.error('Lỗi khi in hóa đơn:', error);
    showNotification('error', 'In hóa đơn thất bại!');
  }
};

// Lifecycle hooks
onMounted(() => {
  fetchHoaDon();
  fetchTabCounts();
});
</script>

<template>
  <div class="container-fluid p-4">
    <!-- Notification -->
    <div v-if="notification.show" :class="['notification', notification.type]">
      <i :class="notification.type === 'success' ? 'fa-solid fa-check-circle' : 'fa-solid fa-exclamation-triangle'"></i>
      {{ notification.message }}
      <button @click="hideNotification" class="close-btn">&times;</button>
    </div>

    <!-- QR Scanner Modal -->
    <div v-if="showQRScanner" class="modal-overlay" @click="stopQRScanner">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Quét mã QR</h3>
          <button @click="stopQRScanner" class="btn btn-secondary">X</button>
        </div>
        <div class="modal-body">
          <div id="qr-reader" class="qr-box"></div>
        </div>
      </div>
    </div>

    <div class="header">
      <div class="title">
        <i class="fa-solid fa-receipt p-2"></i>
        Quản lý hóa đơn
      </div>

      <div class="filter-section">
        <div class="filter-header">
          <i class="fa-solid fa-filter p-2"></i>
          <strong>Bộ lọc</strong>
          <button @click="expandedFilters = !expandedFilters" class="expand-btn">
            <i :class="expandedFilters ? 'fa-solid fa-chevron-up' : 'fa-solid fa-chevron-down'"></i>
          </button>
        </div>

        <div class="filter-content" :class="{ 'expanded': expandedFilters }">
          <div class="filter-row">
            <div class="filter-group search-group">
              <label>Tìm kiếm</label>
              <div class="search-input-container">
                <i class="fa-solid fa-magnifying-glass search-icon"></i>
                <input type="text" v-model="filters.keyword" placeholder="Nhập mã HD, tên KH, SĐT..."
                  class="search-input" @input="handleFilterChange">
                <button v-if="filters.keyword" @click="filters.keyword = ''; handleFilterChange()" class="clear-btn">
                  <i class="fa-solid fa-times"></i>
                </button>
              </div>
            </div>

            <div class="filter-group">
              <label>Loại hóa đơn:</label>
              <select v-model="filters.loaiHoaDon" class="form-select" @change="handleFilterChange">
                <option value="">Tất cả</option>
                <option value="1">Online</option>
                <option value="0">Tại quầy</option>
              </select>
            </div>

            <div class="filter-group">
              <label>Trạng thái:</label>
              <select v-model="filters.trangThai" class="form-select" @change="handleFilterChange">
                <option value="">Tất cả</option>
                <option value="0">Chờ xác nhận</option>
                <option value="1">Đã xác nhận</option>
                <option value="2">Chờ vận chuyển</option>
                <option value="3">Đang vận chuyển</option>
                <option value="4">Đã giao hàng</option>
                <option value="5">Hoàn thành</option>
                <option value="6">Hủy</option>
                <option value="7">Yêu cầu trả hàng</option>
                <option value="8">Đang trả hàng</option>
                <option value="9">Trả hàng thành công</option>
                <option value="10">Từ chối trả hàng</option>
                <option value="11">Khách chưa xác nhận</option>
              </select>
            </div>
          </div>

          <div class="filter-row" v-if="expandedFilters">
            <div class="filter-group date-group">
              <label>Từ ngày</label>
              <input type="date" v-model="filters.fromDate" class="form-input date-input" @change="handleFilterChange">
            </div>

            <div class="filter-group date-group">
              <label>Đến ngày</label>
              <input type="date" v-model="filters.toDate" class="form-input date-input" @change="handleFilterChange">
            </div>
          </div>

          <div class="button-group">
            <button class="btn btn-secondary" @click="resetFilters">
              <i class="fa-solid fa-rotate-right"></i>
              Làm mới
            </button>
            <button class="btn btn-primary" @click="startQRScanner" :class="{ 'scanning': showQRScanner }">
              <i class="fa-solid fa-qrcode"></i>
              Quét QR
            </button>
            <button class="btn btn-success" @click="exportToExcel" :disabled="loading || selectedHoaDons.length === 0">
              <i class="fa-solid fa-file-export"></i>
              Xuất Excel
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="invoice-list">
      <div class="list-header">
        <div class="list-title">
          <i class="fa-solid fa-list-ul p-2"></i>
          Danh sách hóa đơn
        </div>

        <div class="tab-container">
          <div v-for="[status, label] in orderedTabList" :key="status"
            :class="['tab-item', { 'active': activeTab == status }]" @click="selectTab(status)">
            {{ label }}
            <span class="badge">
              <template v-if="status === 'returnGroup'">
                {{ returnTabCount }}
              </template>
              <template v-else>
                {{ status === '' ? (tabCounts[-1] || 0) : (tabCounts[status] || 0) }}
              </template>
            </span>
          </div>
        </div>
      </div>

      <!-- Loading Spinner -->
      <div v-if="loading" class="loading-container">
        <div class="spinner"></div>
        <p>Đang tải dữ liệu...</p>
      </div>

      <!-- Empty State -->
      <div v-else-if="listHoaDon.length === 0" class="empty-state">
        <i class="fa-solid fa-file-invoice"></i>
        <h3>Không tìm thấy hóa đơn nào</h3>
        <p>Thử thay đổi bộ lọc hoặc tạo hóa đơn mới</p>
      </div>

      <!-- Table -->
      <div v-else class="table-container">
        <table>
          <thead>
            <tr>
              <th><input type="checkbox" @change="toggleSelectAll"
                  :checked="selectedHoaDons.length === listHoaDon.length && listHoaDon.length > 0" /></th>
              <th>STT</th>
              <th @click="sortData('maHoaDon')">
                Mã HD
                <i :class="['fas', getSortIcon('maHoaDon')]"></i>
              </th>
              <th>Khách hàng</th>
              <th>SĐT</th>
              <th @click="sortData('tongTien')">
                Tổng tiền
                <i :class="['fas', getSortIcon('tongTien')]"></i>
              </th>
              <th @click="sortData('trangThai')">
                Trạng thái
                <i :class="['fas', getSortIcon('trangThai')]"></i>
              </th>
              <th @click="sortData('ngayTao')">
                Ngày tạo
                <i :class="['fas', getSortIcon('ngayTao')]"></i>
              </th>
              <th>Nhân viên</th>
              <th>Loại</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(hd, index) in sortedHoaDons" :key="hd.id">
              <td><input type="checkbox" :checked="isChecked(hd.id)" @change="() => toggleHoaDonSelection(hd.id)" />
              </td>
              <td>{{ index + 1 + currentPage * size }}</td>
              <td class="text-success">{{ hd.maHoaDon }}</td>
              <td>{{ hd.tenKhachHang || hd.khachHang?.tenKhachHang || 'Khách lẻ' }}</td>
              <td>{{ hd.sdt || 'N/A' }}</td>
              <!-- <td>{{ formatCurrency(hd.soTienKhachHangThanhToan) }}</td> -->
              <!-- <td>{{ formatCurrency(hd.tongTien - hd.soTienGiam + hd.phiVanChuyen ) }}</td> -->
              <td>{{ formatCurrency(hd.tongTienSauKhiGiam) }}</td>
              <td>
                <span :class="['status-badge', getStatusClass(hd.trangThai)]">
                  {{ getStatusText(hd.trangThai) }}
                </span>
              </td>
              <td>{{ formatDate(hd.ngayTao) }}</td>
              <td>{{ hd.tenNhanVien || 'N/A' }}</td>
              <td>
                <span :class="['type-badge', hd.loaiHoaDon ? 'type-online' : 'type-offline']">
                  {{ hd.loaiHoaDon ? "Online" : "Tại quầy" }}
                </span>
              </td>
              <td>
                <div class="action-buttons">
                  <button class="action-btn btn-primary" @click="viewDetail(hd.id)" title="Chi tiết">
                    <i class="fa-solid fa-eye"></i>
                  </button>
                  <button class="action-btn btn-secondary" @click="printSingleInvoice(hd.id)" title="In hóa đơn">
                    <i class="fa-solid fa-print"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- Pagination -->
        <div class="pagination-container">
          <div class="page-size-selector">
            <label>Hiển thị:</label>
            <select v-model="size" @change="fetchHoaDon" class="form-select">
              <option value="5">5</option>
              <option value="10">10</option>
              <option value="15">15</option>
              <option value="20">20</option>
            </select>
          </div>

          <div class="pagination-controls">
            <button @click="prevPage" class="page-nav" :disabled="currentPage === 0 || loading">
              <i class="fa-solid fa-chevron-left"></i>
            </button>

            <div class="page-numbers">
              <button v-for="pageNum in visiblePages" :key="pageNum" @click="goToPage(pageNum - 1)"
                :class="['page-btn', { 'active': pageNum - 1 === currentPage }]" :disabled="loading">
                {{ pageNum }}
              </button>
            </div>

            <button @click="nextPage" class="page-nav" :disabled="currentPage + 1 >= totalPages || loading">
              <i class="fa-solid fa-chevron-right"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Các style cũ giữ nguyên */

/* Thêm style mới */
.expand-btn {
  background: none;
  border: none;
  color: #495057;
  cursor: pointer;
  margin-left: auto;
}

.filter-content {
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.3s ease-out;
}

.filter-content.expanded {
  max-height: 500px;
  /* Đủ lớn để chứa nội dung */
}

th[clickable] {
  cursor: pointer;
  user-select: none;
}

th[clickable]:hover {
  background-color: #f8f9fa;
}

.scanning {
  background-color: #dc3545 !important;
}

/* Thêm style cho sắp xếp */
.fas {
  margin-left: 5px;
  font-size: 12px;
}

/* Tối ưu hiển thị trên mobile */
@media (max-width: 768px) {
  .filter-content.expanded {
    max-height: 800px;
    /* Cao hơn cho mobile */
  }

  .tab-container {
    flex-wrap: wrap;
    justify-content: center;
  }

  .tab-item {
    margin-bottom: 5px;
  }

  th,
  td {
    padding: 8px 4px;
    font-size: 12px;
  }

  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }

  .action-btn {
    padding: 4px;
  }
}

.pagination-container {
  padding: 20px 0;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-nav,
.page-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e1e5e9;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.page-nav {
  padding: 0;
}

.page-nav:hover:not(:disabled) {
  background: #f8f9fa;
}

.page-nav:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-btn {
  font-weight: 500;
  color: #495057;
}

.page-btn:hover:not(:disabled) {
  background: #f8f9fa;
}

.page-btn.active {
  background: #66ea8b;
  color: white;
  border-color: #66ea8b;
}

.page-numbers {
  display: flex;
  gap: 6px;
}

/* Responsive */
@media (max-width: 768px) {

  .page-nav,
  .page-btn {
    width: 36px;
    height: 36px;
    font-size: 13px;
  }

  .pagination-controls {
    gap: 4px;
  }
}

.container-fluid {
  padding: 20px;
}

.filter-row {
  display: flex;
  gap: 15px;
  align-items: flex-end;
}

.filter-group {
  flex: 1;
  min-width: 0;
}

.search-group {
  flex: 2;
  /* Chiếm nhiều không gian hơn */
}

.search-input-container {
  position: relative;
  display: flex;
  align-items: center;
}

.search-input {
  width: 100%;
  padding: 10px 15px 10px 35px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s;
  height: 40px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.search-input:focus {
  outline: none;
  border-color: #66ea8b;
  box-shadow: 0 0 0 3px rgba(102, 234, 139, 0.2);
}

.search-icon {
  position: absolute;
  left: 12px;
  color: #888;
  font-size: 14px;
}

.clear-btn {
  position: absolute;
  right: 10px;
  background: none;
  border: none;
  color: #888;
  cursor: pointer;
  padding: 5px;
}

.clear-btn:hover {
  color: #666;
}

.date-group {
  flex: 1;
}

.date-input {
  width: 100%;
  padding: 10px 15px;
  height: 40px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #555;
}

/* Responsive */
@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
    gap: 10px;
  }

  .search-group,
  .date-group {
    width: 100%;
  }
}

/* Header & Title */
.header {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  overflow: hidden;
}

.title {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  color: white;
  padding: 20px;
  font-size: 24px;
  font-weight: bold;
  display: flex;
  align-items: center;
}

/* Filter Section */
.filter-section {
  padding: 20px;
}

.filter-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  color: #333;
  font-size: 16px;
}

.filter-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.filter-row {
  display: flex;
  gap: 20px;
  align-items: flex-end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
  flex: 1;
  min-width: 200px;
}

.filter-group label {
  font-weight: 500;
  color: #333;
}

.search-input-wrapper {
  position: relative;
}

.search-input-wrapper .search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #6c757d;
  pointer-events: none;
}

.filter-group input {
  padding-left: 35px;
}

.form-input,
.form-select {
  padding: 8px 12px;
  border: 2px solid #e1e5e9;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
  width: 100%;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #667eea;
}

.button-group {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 10px;
}

/* Invoice List */
.invoice-list {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.list-header {
  background: #f8f9fa;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #dee2e6;
}

.list-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  display: flex;
  align-items: center;
}

.tab-container {
  display: flex;
  gap: 5px;
  overflow-x: auto;
}

.tab-item {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
  background: #e9ecef;
  color: #495057;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab-item.active {
  background: #66ea8b;
  color: white;
}

.tab-item .badge {
  background: white;
  color: #495057;
  border-radius: 10px;
  padding: 2px 8px;
  font-size: 12px;
}

.tab-item.active .badge {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

/* Table */
.table-container {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

th,
td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #dee2e6;
}

th {
  background: #f8f9fa;
  font-weight: 600;
  color: #495057;
  white-space: nowrap;
}

tr:hover {
  background: #f8f9fa;
}

/* Status Badges */
.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  display: inline-block;
  min-width: 120px;
  text-align: center;
}

.status-warning {
  background: #fff3cd;
  color: #856404;
}

.status-success {
  background: #d4edda;
  color: #155724;
}

.status-info {
  background: #d1ecf1;
  color: #0c5460;
}

.status-danger {
  background: #f8d7da;
  color: #721c24;
}

.status-secondary {
  background: #e2e3e5;
  color: #383d41;
}

.type-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  display: inline-block;
  text-align: center;
}

.type-online {
  background: #cce5ff;
  color: #004085;
}

.type-offline {
  background: #e2e3e5;
  color: #383d41;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s;
}

.action-btn.btn-primary {
  background: #66ea8b;
  color: white;
}

.action-btn.btn-secondary {
  background: #6c757d;
  color: white;
}

.action-btn:hover {
  opacity: 0.8;
}

/* Pagination */
.pagination-container {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #dee2e6;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-size-selector label {
  font-size: 14px;
  color: #495057;
}

.page-size-selector select {
  width: 70px;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-numbers {
  display: flex;
  gap: 4px;
}

.page-btn {
  padding: 6px 10px;
  border: 1px solid #dee2e9;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  background: #e9ecef;
}

.page-btn.active {
  background: #66ea8b;
  color: white;
  border-color: #66ea8b;
}

/* Loading & Empty States */
.loading-container {
  text-align: center;
  padding: 60px 20px;
  color: #6c757d;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #66ea8b;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #6c757d;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 20px;
  color: #dee2e6;
}

.empty-state h3 {
  margin-bottom: 10px;
  color: #495057;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #dee2e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.modal-body {
  padding: 20px;
}

.qr-box {
  width: 100%;
  height: 300px;
  margin: 0 auto;
}

/* Notification */
.notification {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 300px;
  animation: slideIn 0.3s ease-out;
}

.notification.success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.notification.error {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.notification .close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  margin-left: auto;
  color: inherit;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }

  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* Responsive */
@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
    gap: 10px;
  }

  .filter-group {
    min-width: 100%;
  }

  .button-group {
    flex-wrap: wrap;
    justify-content: center;
  }

  .list-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }

  .tab-container {
    overflow-x: auto;
    padding-bottom: 10px;
  }

  .pagination-container {
    flex-direction: column;
    gap: 15px;
  }

  th,
  td {
    padding: 8px 4px;
    font-size: 12px;
  }

  .status-badge {
    min-width: 100px;
  }
}
</style>