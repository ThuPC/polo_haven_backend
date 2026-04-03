<template>
  <div class="container-fluid p-4">
    <div class="header-card">
      <div class="title">
        <i class="fas fa-ticket-alt p-2"></i>
        Quản lý Đợt khuyến mại
      </div>
    </div>
    <!-- Phần bộ lọc để áp dụng tìm kiếm và lọc theo trạng thái -->
    <div class="filter-section">
      <div class="filter-header">
        <i class="fa-solid fa-filter p-2"></i>
        <strong>Bộ lọc</strong>
      </div>

      <div class="filter-content">
        <div class="filter-row">
          <div class="filter-group search-group">
            <label>Tìm kiếm</label>
            <div class="search-input-container">
              <i class="fa-solid fa-magnifying-glass search-icon"></i>
              <input type="text" placeholder="Theo mã, tên đợt..." v-model="searchQuery" class="search-input">
            </div>
          </div>

          <div class="filter-group">
            <label for="status-filter">Trạng thái:</label>
            <select id="status-filter" v-model="statusFilter" class="form-select">
              <option value="all">Tất cả</option>
              <option value="1">Đang diễn ra</option>
              <option value="0">Sắp tới</option>
              <option value="2">Hết hạn</option>
              <option value="3">Kết thúc sớm</option>
              <option value="4">Áp dụng sớm</option>
            </select>
          </div>

          <div class="filter-group">
            <label for="from-date">Từ ngày:</label>
            <input type="date" id="from-date" v-model="filterFromDate" class="form-select">
          </div>
          <div class="filter-group">
            <label for="to-date">Đến ngày:</label>
            <input type="date" id="to-date" v-model="filterToDate" class="form-select">
          </div>
        </div>

        <div class="button-group">
          <button class="btn btn-secondary" @click="resetFilters">
            <i class="fas fa-sync-alt"></i> Làm mới
          </button>
          <button class="add-new-btn btn btn-success" @click="openAddModal">
            <i class="fas fa-plus"></i> Thêm mới
          </button>
        </div>
      </div>
    </div>
    <!-- Phần bảng hiển thị danh sách đợt khuyến mại -->
    <div class="table-section">
      <div class="list-header">
        <div class="list-title">
          <i class="fa-solid fa-list-ul p-2"></i>
          Danh sách đợt khuyến mãi
        </div>

        <div class="tab-container">
          <div :class="['tab-item', { 'active': statusFilter === 'all' }]" @click="statusFilter = 'all'">
            Tất cả <span class="badge">{{ statusCounts.all }}</span>
          </div>
          <div :class="['tab-item', { 'active': statusFilter === '1' }]" @click="statusFilter = '1'">
            Đang diễn ra <span class="badge">{{ statusCounts.active }}</span>
          </div>
          <div :class="['tab-item', { 'active': statusFilter === '0' }]" @click="statusFilter = '0'">
            Sắp tới <span class="badge">{{ statusCounts.inactive }}</span>
          </div>
          <div :class="['tab-item', { 'active': statusFilter === '2' }]" @click="statusFilter = '2'">
            Hết hạn <span class="badge">{{ statusCounts.expired }}</span>
          </div>
          <div :class="['tab-item', { 'active': statusFilter === '3' }]" @click="statusFilter = '3'">
            Kết thúc sớm <span class="badge">{{ statusCounts.earlyEnded }}</span>
          </div>
          <div :class="['tab-item', { 'active': statusFilter === '4' }]" @click="statusFilter = '4'">
            Áp dụng sớm <span class="badge">{{ statusCounts.earlyApplied }}</span>
          </div>
        </div>
      </div>

      <div v-if="!paginatedKhuyenMaiList || paginatedKhuyenMaiList.length === 0" class="empty-state">
        <i class="fas fa-ticket-alt"></i>
        <h3>Không tìm thấy đợt khuyến mại nào</h3>
        <p>Vui lòng thêm đợt khuyến mại hoặc thay đổi bộ lọc.</p>
      </div>

      <div v-else class="list-content-area">
        <div class="voucher-list-header">
          <div class="col-stt">STT</div>
          <div class="col-ma">Mã khuyến mãi</div>
          <div class="col-ten">Tên khuyến mãi</div>
          <div class="col-phantram">Phần trăm giảm</div>
          <div class="col-ngay">Bắt đầu</div>
          <div class="col-ngay">Kết thúc</div>
          <div class="col-mota">Mô tả</div>
          <div class="col-trangthai">Trạng thái</div>
          <div class="col-action">Hành động</div>
        </div>

        <div v-for="(km, index) in paginatedKhuyenMaiList" :key="km.id" class="voucher-item-container">
          <div class="voucher-item-row" @click="openDetailModal(km)" :class="{ 'selected': selectedKhuyenMai && selectedKhuyenMai.id === km.id }">
            <div class="col-stt">{{ index + 1 + (currentPage - 1) * itemsPerPage }}</div>
            <div class="col-ma">{{ km.maKMTCT || 'N/A' }}</div>
            <div class="col-ten">{{ km.tenKhuyenMai || 'N/A' }}</div>
            <div class="col-phantram">{{ km.phanTramGiam ? `${km.phanTramGiam}%` : 'N/A' }}</div>
            <div class="col-ngay">{{ formatDate(km.ngayBatDau) }}</div>
            <div class="col-ngay">{{ formatDate(km.ngayKetThuc) }}</div>
            <div class="col-mota">{{ km.moTa || 'N/A' }}</div>
            <div class="col-trangthai">
              <span class="status-badge" :class="getStatusInfo(km.trangThai).class">
                {{ getStatusInfo(km.trangThai).text }}
              </span>
            </div>
            <div class="col-action">
              <button @click.stop="openProductModal(km.id)" class="btn btn-outline-primary btn-sm">
                <i class="fas fa-plus"></i><span>Thêm sản phẩm</span>
              </button>
              <span v-if="getProductCount(km.id) > 0" class="product-badge">{{ getProductCount(km.id) }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="pagination-container">
        <div class="page-size-selector">
          <label>Hiển thị:</label>
          <select v-model="itemsPerPage" class="form-select">
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="15">15</option>
          </select>
          <span>/ trang</span>
        </div>
        <div class="pagination-controls" v-if="totalPages > 1">
          <button @click="goToPreviousPage" class="btn btn-secondary" :disabled="currentPage === 1">
            <i class="fa-solid fa-chevron-left"></i>
          </button>
          <span class="page-info">
            Trang {{ currentPage }} / {{ totalPages }}
          </span>
          <button @click="goToNextPage" class="btn btn-secondary" :disabled="currentPage === totalPages">
            <i class="fa-solid fa-chevron-right"></i>
          </button>
        </div>
      </div>
    </div>
    <!-- Phần modal để thêm mới đợt khuyến mại -->
    <div v-if="showAddModal" class="modal-overlay">
      <div class="modal-content-custom">
        <div class="modal-header">
          <h3 class="modal-title">Thêm mới đợt khuyến mại</h3>
          <button @click="closeModal" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div class="form-grid">
            <div class="form-group full-width">
              <label for="tenKhuyenMai">Tên khuyến mãi <span class="required">*</span></label>
              <input type="text" id="tenKhuyenMai" v-model="newKhuyenMai.tenKhuyenMai" placeholder="Ví dụ: Khuyến mãi 20/11">
            </div>
            <div class="form-group full-width">
              <label for="moTa">Mô tả</label>
              <textarea id="moTa" v-model="newKhuyenMai.moTa" placeholder="Nhập mô tả khuyến mãi"></textarea>
            </div>
            <div class="form-group">
              <label for="phanTramGiam">Phần trăm giảm (%)</label>
              <input type="number" id="phanTramGiam" v-model.number="newKhuyenMai.phanTramGiam" placeholder="Nhập phần trăm giảm" min="0" max="100" step="0.01">
            </div>
            <div class="form-group">
              <label for="ngayBatDau">Ngày bắt đầu</label>
              <input type="date" id="ngayBatDau" v-model="newKhuyenMai.ngayBatDau">
            </div>
            <div class="form-group">
              <label for="ngayKetThuc">Ngày kết thúc</label>
              <input type="date" id="ngayKetThuc" v-model="newKhuyenMai.ngayKetThuc">
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="addKhuyenMai" class="btn btn-save"><i class="far fa-save"></i> Lưu</button>
          <button @click="closeModal" class="btn btn-danger"><i class="far fa-trash-alt"></i> Đóng</button>
        </div>
      </div>
    </div>
    <!-- Phần modal để xem chi tiết đợt khuyến mại -->
    <div v-if="showDetailModal" class="modal-overlay">
      <div class="modal-content-custom">
        <div class="modal-header">
          <h3 class="modal-title">Chi tiết đợt khuyến mại</h3>
          <button @click="closeModal" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div class="modal-tabs">
            <button class="modal-tab-btn" :class="{ 'active': modalActiveTab === 'thongTin' }" @click="modalActiveTab = 'thongTin'">Thông tin</button>
            <button class="modal-tab-btn" :class="{ 'active': modalActiveTab === 'sanPham' }" @click="modalActiveTab = 'sanPham'">Sản phẩm áp dụng</button>
          </div>
          <div class="tab-content">
            <div v-show="modalActiveTab === 'thongTin'" class="form-grid">
              <div class="form-group full-width">
                <label>Tên khuyến mãi</label>
                <input type="text" v-model="newKhuyenMai.tenKhuyenMai" placeholder="Ví dụ: Khuyến mãi 20/11">
              </div>
              <div class="form-group full-width">
                <label>Mô tả</label>
                <textarea v-model="newKhuyenMai.moTa" placeholder="Nhập mô tả khuyến mãi"></textarea>
              </div>
              <div class="form-group">
                <label>Phần trăm giảm (%)</label>
                <input type="number" v-model.number="newKhuyenMai.phanTramGiam" placeholder="Nhập phần trăm giảm" min="0" max="100" step="0.01">
              </div>
              <div class="form-group">
                <label>Ngày bắt đầu</label>
                <input type="date" v-model="newKhuyenMai.ngayBatDau">
              </div>
              <div class="form-group">
                <label>Ngày kết thúc</label>
                <input type="date" v-model="newKhuyenMai.ngayKetThuc">
              </div>
              <div class="form-group toggle-group" style="grid-column: span 1;">
                <label>Trạng thái</label>
                <div class="form-check form-switch">
                  <label class="form-check-label" :for="`status-detail-${newKhuyenMai.id}`"
                         :class="newKhuyenMai.isToggleOn ? 'text-success' : 'text-muted'">
                    {{ newKhuyenMai.isToggleOn ? 'Hoạt động' : 'Không hoạt động' }}
                    <input class="form-check-input" type="checkbox" :id="`status-detail-${newKhuyenMai.id}`"
                           :checked="newKhuyenMai.isToggleOn" @change="toggleStatus(newKhuyenMai)">
                  </label>
                </div>
              </div>
            </div>

            <div v-show="modalActiveTab === 'sanPham'" class="details-content">
              <h3 class="details-section-title">Sản phẩm áp dụng</h3>
              <div v-if="products[selectedKhuyenMai?.id]?.length === 0" class="empty-state">
                <i class="fas fa-info-circle"></i>
                <p>Không có sản phẩm nào được áp dụng.</p>
              </div>
              <div v-else class="voucher-list-container ">
                <table class="tab-table">
                  <thead>
                    <tr>
                      <th>STT</th>
                      <th>Mã SP</th>
                      <th>Tên SP</th>
                      <th>Đơn giá</th>
                      <th>Số tiền giảm</th>
                      <th>Giá sau giảm</th>
                      <th>Trạng thái</th>
                      <th>Hành động</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(product, index) in products[selectedKhuyenMai?.id] || []" :key="index">
                      <td>{{ index + 1 }}</td>
                      <td>{{ product.maSanPhamKhuyenMai || 'N/A' }}</td>
                      <td>{{ product.tenCTSP || 'N/A' }}</td>
                      <td>{{ formatCurrency(product.donGia) }}</td>
                      <td>{{ formatCurrency(tinhTienGiam(product.donGia, product.phanTramGiam)) }}</td>
                      <td>{{ formatCurrency(tinhTienSauGiam(product)) }}</td>
                      <td>
                        <span class="status-badge" :class="getStatusInfo(product.trangThai).class">
                          {{ getStatusInfo(product.trangThai).text }}
                        </span>
                      </td>
                      <td>
                        <button @click="xoaChiTiet(product.id)" class="btn-xoactkm"><i class="far fa-trash-alt"></i></button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="updateKhuyenMaiFunc" class="btn btn-save"><i class="far fa-save"></i> Cập nhật</button>
          <button @click="closeModal" class="btn btn-danger"><i class="far fa-trash-alt"></i> Đóng</button>
        </div>
      </div>
    </div>
    <!-- Phần modal để quản lý danh sách chi tiết sản phẩm -->
    <div v-if="showProductModal" class="modal-overlay">
      <div class="modal-content-custom">
        <div class="modal-header">
          <h3 class="modal-title">Danh sách chi tiết sản phẩm</h3>
          <button @click="closeProductModal" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div class="filter-controls">
            <div class="filter-item">
              <label for="filter-by-name">Tìm kiếm:</label>
              <input id="filter-by-name" type="text" v-model="productSearchQuery" placeholder="Theo tên sản phẩm..." class="filter-input" />
            </div>
            <div class="filter-item">
              <label for="filter-by-product-name">Tên sản phẩm:</label>
              <select id="filter-by-product-name" v-model="productNameFilter" class="form-select">
                <option value="">Tất cả</option>
                <option v-for="product in uniqueProductNames" :key="product" :value="product">{{ product }}</option>
              </select>
            </div>
            <div class="filter-item">
              <label for="filter-by-color">Màu sắc:</label>
              <select id="filter-by-color" v-model="colorFilter" class="form-select">
                <option value="">Tất cả</option>
                <option v-for="color in uniqueColors" :key="color" :value="color">{{ color }}</option>
              </select>
            </div>
            <div class="filter-item">
              <label for="filter-by-size">Kích thước:</label>
              <select id="filter-by-size" v-model="sizeFilter" class="form-select">
                <option value="">Tất cả</option>
                <option v-for="size in uniqueSizes" :key="size" :value="size">{{ size }}</option>
              </select>
            </div>
            <div class="filter-button">
              <button @click="resetProductFilters" class="btn btn-secondary btn-reset">
                <i class="fas fa-sync-alt"></i> Làm mới
              </button>
            </div>
          </div>
          <div v-if="chiTietSanPhamList.length === 0" class="empty-state">
            <i class="fas fa-info-circle"></i>
            <p>Không có dữ liệu để hiển thị</p>
          </div>
          <div v-else class="voucher-list-container">
            <table class="tab-table">
              <thead>
                <tr>
                  <th><input type="checkbox" v-model="selectAllChiTiet" @change="toggleSelectAllChiTiet"></th>
                  <th>STT</th>
                  <th>Mã CTSP</th>
                  <th>Tên sản phẩm</th>
                  <th>Hình ảnh</th>
                  <th>Màu sắc</th>
                  <th>Kích thước</th>
                  <th>Đơn giá</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(ctsp, index) in filteredChiTietSanPhamList" :key="ctsp.id">
                  <td><input type="checkbox" v-model="selectedChiTietIds" :value="ctsp.id" /></td>
                  <td>{{ index + 1 }}</td>
                  <td>{{ ctsp.maCTSP || 'N/A' }}</td>
                  <td>{{ ctsp.tenCTSP || 'N/A' }}</td>
                  <td>
                    <img
                      :src="ctsp.url"
                      :alt="ctsp.tenAnh || 'Ảnh sản phẩm'"
                      style="max-height: 60px; object-fit: contain"
                      v-if="ctsp.url"
                    />
                    <span v-else>N/A</span>
                  </td>
                  <td>{{ ctsp.mauSac || 'N/A' }}</td>
                  <td>{{ ctsp.kichThuoc || 'N/A' }}</td>
                  <td>{{ formatCurrency(ctsp.donGia) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-save" @click="applyKhuyenMai" :disabled="selectedChiTietIds.length === 0">
            <i class="fas fa-check"></i> Áp dụng
          </button>
          <button @click="closeProductModal" class="btn btn-danger"><i class="far fa-trash-alt"></i> Đóng</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { getAllKhuyenMai, createKhuyenMai, updateKhuyenMai, validateBeforeToggle } from '../../../services/RoleQuanLy/QuanLyKhuyenMai';
import { addChiTietKhuyenMai, getAllChiTietSanPham, getSanPhamApDungKhuyenMai, deleteChiTietKhuyenMai } from '../../../services/RoleQuanLy/ChiTietKhuyenMaiService';
import { useToast } from 'vue-toastification';
import Swal from 'sweetalert2';

const toast = useToast();

// Khởi tạo dữ liệu
const khuyenMaiList = ref([]);
const chiTietSanPhamList = ref([]);
const showAddModal = ref(false);
const showDetailModal = ref(false);
const showProductModal = ref(false);
const isEditing = ref(false);
const searchQuery = ref('');
const productSearchQuery = ref('');
const filterFromDate = ref('');
const filterToDate = ref('');
const statusFilter = ref('all');
const itemsPerPage = ref(10);
const currentPage = ref(1);
const selectAllChiTiet = ref(false);
const selectedChiTietIds = ref([]);
const selectedKhuyenMaiId = ref(null);
const selectedKhuyenMai = ref(null);
const modalActiveTab = ref('thongTin');

// Thêm các bộ lọc mới cho sản phẩm
const productNameFilter = ref('');
const colorFilter = ref('');
const sizeFilter = ref('');

const newKhuyenMai = ref({
  id: null,
  maKMTCT: '',
  tenKhuyenMai: '',
  moTa: '',
  phanTramGiam: null,
  ngayBatDau: '',
  ngayKetThuc: '',
  trangThai: 1,
  isToggleOn: true,
});

const products = ref({}); // Sử dụng object để lưu sản phẩm theo idKhuyenMai

// Tính toán số lượng theo trạng thái
const statusCounts = computed(() => {
  return {
    all: khuyenMaiList.value.length,
    active: khuyenMaiList.value.filter(km => km.trangThai === 1).length,
    inactive: khuyenMaiList.value.filter(km => km.trangThai === 0).length,
    expired: khuyenMaiList.value.filter(km => km.trangThai === 2).length,
    earlyEnded: khuyenMaiList.value.filter(km => km.trangThai === 3).length,
    earlyApplied: khuyenMaiList.value.filter(km => km.trangThai === 4).length,
  };
});

const totalPages = computed(() => {
  return Math.ceil(filteredKhuyenMaiList.value.length / itemsPerPage.value);
});

// Tính toán danh sách duy nhất cho combobox
const uniqueProductNames = computed(() => {
  const names = new Set(chiTietSanPhamList.value.map(ctsp => ctsp.tenCTSP || 'N/A'));
  return [...Array.from(names).filter(name => name !== 'N/A')];
});
//sau để danh sách sp vào đây

const uniqueColors = computed(() => {
  const colors = new Set(chiTietSanPhamList.value.map(ctsp => ctsp.mauSac || 'N/A'));
  return [...Array.from(colors).filter(color => color !== 'N/A')];
});

const uniqueSizes = computed(() => {
  const sizes = new Set(chiTietSanPhamList.value.map(ctsp => ctsp.kichThuoc || 'N/A'));
  return [...Array.from(sizes).filter(size => size !== 'N/A')];
});

// Lọc danh sách chi tiết sản phẩm
const filteredChiTietSanPhamList = computed(() => {
  let filteredList = [...chiTietSanPhamList.value];
  if (productSearchQuery.value) {
    filteredList = filteredList.filter(ctsp =>
      (ctsp.tenCTSP?.toLowerCase().includes(productSearchQuery.value.toLowerCase()) ||
       ctsp.maCTSP?.toLowerCase().includes(productSearchQuery.value.toLowerCase()))
    );
  }
  if (productNameFilter.value && productNameFilter.value !== 'Tất cả') {
    filteredList = filteredList.filter(ctsp => ctsp.tenCTSP === productNameFilter.value);
  }
  if (colorFilter.value && colorFilter.value !== 'Tất cả') {
    filteredList = filteredList.filter(ctsp => ctsp.mauSac === colorFilter.value);
  }
  if (sizeFilter.value && sizeFilter.value !== 'Tất cả') {
    filteredList = filteredList.filter(ctsp => ctsp.kichThuoc === sizeFilter.value);
  }
  return filteredList;
});

// Lọc danh sách khuyến mãi
const filteredKhuyenMaiList = computed(() => {
  let filteredList = [...khuyenMaiList.value];
  if (searchQuery.value) {
    filteredList = filteredList.filter(km =>
      (km.maKMTCT?.toLowerCase().includes(searchQuery.value.toLowerCase()) || 
       km.tenKhuyenMai?.toLowerCase().includes(searchQuery.value.toLowerCase()))
    );
  }
  if (filterFromDate.value) {
    filteredList = filteredList.filter(km => 
      km.ngayBatDau && new Date(km.ngayBatDau) >= new Date(filterFromDate.value)
    );
  }
  if (filterToDate.value) {
    filteredList = filteredList.filter(km => 
      km.ngayKetThuc && new Date(km.ngayKetThuc) <= new Date(filterToDate.value)
    );
  }
  if (statusFilter.value !== 'all') {
    filteredList = filteredList.filter(km => km.trangThai === parseInt(statusFilter.value));
  }
  return filteredList;
});

const paginatedKhuyenMaiList = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return filteredKhuyenMaiList.value.slice(start, end);
});

// Lấy danh sách khuyến mãi
const getAllKhuyenMaiData = async () => {
  try {
    const response = await getAllKhuyenMai(0, 100);
    khuyenMaiList.value = Array.isArray(response.data.content) ? response.data.content : [];
    await loadAllProducts();
  } catch (error) {
    console.error("Lỗi khi tải danh sách khuyến mãi:", error);
    khuyenMaiList.value = [];
  }
};

// Lấy danh sách chi tiết sản phẩm
const getAllChiTietSanPhamData = async () => {
  try {
    const response = await getAllChiTietSanPham();
    chiTietSanPhamList.value = response.data;
    selectedChiTietIds.value = [];
    selectAllChiTiet.value = false;
  } catch (error) {
    console.error("Lỗi khi tải danh sách chi tiết sản phẩm:", error);
    chiTietSanPhamList.value = [];
  }
};

// Lấy danh sách sản phẩm của một khuyến mãi
const getProductData = async (khuyenMaiId) => {
  try {
    const response = await getSanPhamApDungKhuyenMai(khuyenMaiId);
    const khuyenMai = khuyenMaiList.value.find(km => km.id === khuyenMaiId);
    const phanTramGiam = khuyenMai?.phanTramGiam || 0;
    
    const productList = Array.isArray(response.data)
      ? response.data.map(p => ({
          id: p.id || null,
          idKhuyenMai: khuyenMaiId,
          maSanPhamKhuyenMai: p.maSanPhamKhuyenMai || null,
          tenCTSP: p.tenCTSP || null,
          donGia: p.donGia || null,
          phanTramGiam: phanTramGiam,
          trangThai: p.trangThai ?? -1
        }))
      : [];
    products.value[khuyenMaiId] = productList;
  } catch (error) {
    console.error("Lỗi khi tải danh sách sản phẩm:", error);
    delete products.value[khuyenMaiId];
  }
};

// Lấy sản phẩm cho tất cả khuyến mãi
const loadAllProducts = async () => {
  products.value = {};
  for (const km of khuyenMaiList.value) {
    await getProductData(km.id);
  }
};

// Định dạng ngày
const formatDate = (date) => {
  if (!date || isNaN(new Date(date).getTime())) return 'N/A';
  return new Intl.DateTimeFormat('vi-VN', { year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date(date));
};

// Định dạng tiền tệ
const formatCurrency = (amount) => {
  if (!amount) return 'N/A';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
};

// Tính tiền giảm dựa trên phần trăm
const tinhTienGiam = (donGia, phanTramGiam) => {
  const gia = Number(donGia) || 0;
  const phanTram = Number(phanTramGiam) || 0;
  return Math.round((gia * phanTram) / 100);
};

// Tính tiền sau giảm
const tinhTienSauGiam = (product) => {
  const donGia = Number(product.donGia) || 0;
  const giam = tinhTienGiam(donGia, product.phanTramGiam);
  return donGia - giam;
};

// Lấy thông tin trạng thái khuyến mãi
const getStatusInfo = (statusByte) => {
  const status = Number(statusByte);
  switch (status) {
    case 1: return { text: 'Đang diễn ra', class: 'active' };
    case 0: return { text: 'Sắp tới', class: 'expired' };
    case 2: return { text: 'Hết hạn', class: 'inactive' };
    case 3: return { text: 'Kết thúc sớm', class: 'inactive' };
    case 4: return { text: 'Áp dụng sớm', class: 'active' };
    default: return { text: 'Không xác định', class: 'unknown' };
  }
};

// Đếm số lượng sản phẩm áp dụng cho khuyến mãi cụ thể
const getProductCount = (khuyenMaiId) => {
  return products.value[khuyenMaiId]?.length || 0;
};

// Reset bộ lọc chính
const resetFilters = () => {
  searchQuery.value = '';
  filterFromDate.value = '';
  filterToDate.value = '';
  statusFilter.value = 'all';
  currentPage.value = 1;
  getAllKhuyenMaiData();
};

// Reset bộ lọc sản phẩm
const resetProductFilters = () => {
  productSearchQuery.value = '';
  productNameFilter.value = '';
  colorFilter.value = '';
  sizeFilter.value = '';
};

// Mở modal thêm mới
const openAddModal = () => {
  isEditing.value = false;
  newKhuyenMai.value = {
    id: null,
    maKMTCT: '',
    tenKhuyenMai: '',
    moTa: '',
    phanTramGiam: null,
    ngayBatDau: '',
    ngayKetThuc: '',
    trangThai: 1,
    isToggleOn: true,
  };
  products.value = {};
  showAddModal.value = true;
  showDetailModal.value = false;
  showProductModal.value = false;
};

// Mở modal chi tiết
const openDetailModal = async (km) => {
  if (!km || !km.id) {
    console.error('ID khuyến mãi không hợp lệ:', km);
    toast.error("Không thể mở chi tiết khuyến mãi do dữ liệu không hợp lệ!");
    return;
  }
  isEditing.value = true;
  selectedKhuyenMai.value = km;
  newKhuyenMai.value = {
    id: km.id,
    maKMTCT: km.maKMTCT || '',
    tenKhuyenMai: km.tenKhuyenMai || '',
    moTa: km.moTa || '',
    phanTramGiam: km.phanTramGiam !== null && km.phanTramGiam !== undefined ? Number(km.phanTramGiam) : null,
    ngayBatDau: km.ngayBatDau || '',
    ngayKetThuc: km.ngayKetThuc || '',
    trangThai: km.trangThai !== null && km.trangThai !== undefined ? Number(km.trangThai) : 1,
    isToggleOn: km.trangThai === 1 || km.trangThai === 4,
  };
  await getProductData(km.id);
  showDetailModal.value = true;
  showAddModal.value = false;
  showProductModal.value = false;
  modalActiveTab.value = 'thongTin';
};

// Mở modal danh sách sản phẩm
const openProductModal = async (khuyenMaiId) => {
  selectedKhuyenMaiId.value = khuyenMaiId;
  await getAllChiTietSanPhamData();
  showProductModal.value = true;
  showAddModal.value = false;
  showDetailModal.value = false;
};

// Đóng modal
const closeModal = () => {
  showAddModal.value = false;
  showDetailModal.value = false;
  showProductModal.value = false;
  selectedKhuyenMai.value = null;
};

// Đóng modal sản phẩm
const closeProductModal = () => {
  showProductModal.value = false;
  selectedChiTietIds.value = [];
  selectAllChiTiet.value = false;
  selectedKhuyenMaiId.value = null;
  resetProductFilters(); // Reset bộ lọc khi đóng modal
};

// Xử lý checkbox chọn tất cả chi tiết sản phẩm
const toggleSelectAllChiTiet = () => {
  if (selectAllChiTiet.value) {
    selectedChiTietIds.value = filteredChiTietSanPhamList.value.map(ctsp => ctsp.id);
  } else {
    selectedChiTietIds.value = [];
  }
};

// Thêm mới khuyến mãi
const addKhuyenMai = async () => {
  const result = await Swal.fire({
    title: 'Xác nhận thêm?',
    text: 'Bạn có chắc chắn muốn thêm khuyến mãi mới không?',
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: 'Thêm',
    cancelButtonText: 'Huỷ',
    confirmButtonColor: '#3085d6',
    cancelButtonColor: '#6c757d'
  });

  if (!result.isConfirmed) return;

  if (!newKhuyenMai.value.tenKhuyenMai) {
    toast.error("Vui lòng nhập tên khuyến mãi!");
    return;
  }

  const normalizeDate = (date) => new Date(date.getFullYear(), date.getMonth(), date.getDate());

  const startDateRaw = new Date(newKhuyenMai.value.ngayBatDau);
  const endDateRaw = new Date(newKhuyenMai.value.ngayKetThuc);

  const startDate = normalizeDate(startDateRaw);
  const endDate = normalizeDate(endDateRaw);
  const today = normalizeDate(new Date());

  if (isNaN(startDate) || isNaN(endDate)) {
    toast.error("Ngày bắt đầu hoặc ngày kết thúc không hợp lệ!");
    return;
  }

  if (startDate > endDate) {
    toast.error("Ngày bắt đầu không được sau ngày kết thúc!");
    return;
  }

  if (newKhuyenMai.value.phanTramGiam === null || newKhuyenMai.value.phanTramGiam < 0 || newKhuyenMai.value.phanTramGiam > 100) {
    toast.error("Phần trăm giảm phải từ 0 đến 100!");
    return;
  }

  let trangThai;
  if (today > endDate) {
    toast.error("Không thể thêm khuyến mãi đã hết hạn!");
    return;
  } else if (today < startDate) {
    trangThai = 0; // Sắp tới
  } else {
    trangThai = 1; // Đang diễn ra
  }

  try {
    await createKhuyenMai({ ...newKhuyenMai.value, trangThai });
    toast.success("Thêm khuyến mãi thành công!");
    await getAllKhuyenMaiData();
    closeModal();
  } catch (error) {
    console.error("Lỗi khi thêm khuyến mãi:", error);
    toast.error("Đã có lỗi xảy ra khi thêm khuyến mãi!");
  }
};

// Cập nhật khuyến mãi
const updateKhuyenMaiFunc = async () => {
  const result = await Swal.fire({
    title: 'Xác nhận cập nhật?',
    text: 'Bạn có chắc muốn cập nhật khuyến mãi này không?',
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: 'Cập nhật',
    cancelButtonText: 'Huỷ',
    confirmButtonColor: '#3085d6',
    cancelButtonColor: '#6c757d'
  });

  if (!result.isConfirmed) return;

  if (!newKhuyenMai.value.tenKhuyenMai) {
    toast.error("Vui lòng nhập tên khuyến mãi!");
    return;
  }

  const normalizeDate = (date) => new Date(date.getFullYear(), date.getMonth(), date.getDate());

  const startDateRaw = new Date(newKhuyenMai.value.ngayBatDau);
  const endDateRaw = new Date(newKhuyenMai.value.ngayKetThuc);
  const today = normalizeDate(new Date());

  const startDate = normalizeDate(startDateRaw);
  const endDate = normalizeDate(endDateRaw);

  if (isNaN(startDate) || isNaN(endDate)) {
    toast.error("Ngày bắt đầu hoặc ngày kết thúc không hợp lệ!");
    return;
  }

  if (startDate > endDate) {
    toast.error("Ngày bắt đầu không được sau ngày kết thúc!");
    return;
  }

  if (
    newKhuyenMai.value.phanTramGiam === null ||
    newKhuyenMai.value.phanTramGiam < 0 ||
    newKhuyenMai.value.phanTramGiam > 100
  ) {
    toast.error("Phần trăm giảm phải từ 0 đến 100!");
    return;
  }

  let trangThai;
  if (today > endDate) {
    trangThai = 2; // Hết hạn
  } else if (today < startDate) {
    trangThai = 0; // Sắp tới
  } else {
    trangThai = 1; // Đang diễn ra
  }

  try {
    await updateKhuyenMai(newKhuyenMai.value.id, {
      ...newKhuyenMai.value,
      trangThai,
    });
    toast.success("Cập nhật thành công!");
    await getAllKhuyenMaiData();
    closeModal();
  } catch (error) {
    console.error("Lỗi khi cập nhật khuyến mãi:", error);
    toast.error("Lỗi khi cập nhật khuyến mãi!");
  }
};

// Áp dụng khuyến mãi cho chi tiết sản phẩm
const applyKhuyenMai = async () => {
  const result = await Swal.fire({
    title: 'Áp dụng sản phẩm?',
    text: 'Bạn có chắc muốn áp dụng khuyến mãi này cho sản phẩm đã chọn?',
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: 'Áp dụng',
    cancelButtonText: 'Huỷ',
    confirmButtonColor: '#3085d6',
    cancelButtonColor: '#6c757d'
  });

  if (!result.isConfirmed) return;

  if (selectedChiTietIds.value.length === 0) {
    toast.error("Vui lòng chọn ít nhất một sản phẩm!");
    return;
  }

  try {
    const khuyenMai = khuyenMaiList.value.find(km => km.id === selectedKhuyenMaiId.value);
    const payload = selectedChiTietIds.value.map(idChiTiet => ({
      idChiTietSanPham: idChiTiet,
      idKhuyenMai: selectedKhuyenMaiId.value,
      soTienGiam: tinhTienGiam(
        chiTietSanPhamList.value.find(ctsp => ctsp.id === idChiTiet)?.donGia || 0,
        khuyenMai?.phanTramGiam || 0
      )
    }));

    await addChiTietKhuyenMai(payload);
    await getProductData(selectedKhuyenMaiId.value);
    toast.success("Áp dụng khuyến mãi thành công!");
    closeProductModal();
    selectedChiTietIds.value = [];
  } catch (error) {
    console.error('Lỗi khi áp dụng khuyến mãi:', error);
    toast.error(error.response?.data || "Sản phẩm này chưa hết khuyến mãi cũ!");
  }
};

// Xóa chi tiết khuyến mãi
const xoaChiTiet = async (id) => {
  const result = await Swal.fire({
    title: 'Bạn có chắc muốn xoá?',
    text: 'Chi tiết khuyến mãi này sẽ bị xoá khỏi hệ thống!',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Xoá!',
    cancelButtonText: 'Huỷ',
    confirmButtonColor: '#f51e1e',
    cancelButtonColor: '#6c757d'
  });

  if (!result.isConfirmed) return;

  try {
    await deleteChiTietKhuyenMai(id);
    const idReload = selectedKhuyenMai.value?.id;
    await getProductData(idReload);
    toast.success('Xoá thành công!');
  } catch (error) {
    console.error('Lỗi khi xóa chi tiết khuyến mãi:', error);
    toast.error('Xoá thất bại, vui lòng thử lại!');
  }
};

// Toggle trạng thái khuyến mãi
const toggleStatus = async (km) => {
  const result = await Swal.fire({
    title: 'Xác nhận thay đổi?',
    text: 'Bạn có chắc muốn thay đổi trạng thái khuyến mãi?',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Xác nhận',
    cancelButtonText: 'Huỷ',
    confirmButtonColor: '#3085d6',
    cancelButtonColor: '#6c757d'
  });

  if (!result.isConfirmed) return;

  const originalToggleState = km.isToggleOn;
  const originalStatus = km.trangThai;

  km.isToggleOn = !km.isToggleOn;

  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const endDate = km.ngayKetThuc ? new Date(km.ngayKetThuc) : null;
  const end = endDate ? new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate()) : null;

  const computeNewStatus = () => {
    switch (km.trangThai) {
      case 1: return km.isToggleOn ? 1 : (end && today <= end ? 3 : 2);
      case 0: return km.isToggleOn ? 4 : 0;
      case 2: return km.isToggleOn ? 1 : 2;
      case 3: return km.isToggleOn ? 1 : 3;
      case 4: return km.isToggleOn ? 4 : 0;
      default: return null;
    }
  };

  const newStatus = computeNewStatus();

  if (newStatus === null) {
    toast.error("Trạng thái không hợp lệ!");
    km.isToggleOn = originalToggleState;
    return;
  }

  if ((originalStatus === 3 || originalStatus === 0 || originalStatus === 4) && (newStatus === 1 || newStatus === 4)) {
    try {
      await validateBeforeToggle(km.id, newStatus);
    } catch (e) {
      const errMsg = e.response?.data?.message || e.response?.data || "Không thể bật lại vì sản phẩm đang thuộc đợt khuyến mãi khác.";
      toast.error(errMsg);
      km.isToggleOn = originalToggleState;
      km.trangThai = originalStatus;
      return;
    }
  }

  try {
    const payload = { ...km, trangThai: newStatus };
    await updateKhuyenMai(km.id, payload);
    km.trangThai = newStatus;
    toast.success('Cập nhật trạng thái thành công!');
    await getAllKhuyenMaiData();
  } catch (error) {
    console.error('Lỗi khi cập nhật trạng thái:', error);
    km.isToggleOn = originalToggleState;
    km.trangThai = originalStatus;
    toast.error("Đã có lỗi xảy ra khi cập nhật trạng thái!");
  }
};

// Navigation
const goToPreviousPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
  }
};

const goToNextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
  }
};

// Theo dõi thay đổi
watch([itemsPerPage, statusFilter, searchQuery, filterFromDate, filterToDate], () => {
  currentPage.value = 1;
});

watch([productSearchQuery, productNameFilter, colorFilter, sizeFilter], () => {
  selectAllChiTiet.value = false; // Reset select all khi bộ lọc thay đổi
  selectedChiTietIds.value = []; // Reset selected items khi bộ lọc thay đổi
}, { deep: true });

watch(selectedChiTietIds, (newSelectedChiTietIds) => {
  selectAllChiTiet.value = newSelectedChiTietIds.length === filteredChiTietSanPhamList.value.length;
}, { deep: true });

// Khởi động component
onMounted(() => {
  getAllKhuyenMaiData();
  getAllChiTietSanPhamData();
});
</script>

<style scoped>
/* Phần CSS chung và bố cục chính */
.container-fluid {
  padding: 20px;
  max-width: 100%;
  margin: 0 auto;
  font-family: 'Public Sans', sans-serif;
}

.header-card {
  background: #ffffff;
  border-radius: 12px 12px 0 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.filter-section {
  background: #ffffff;
  border-radius: 0 0 12px 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
  overflow: hidden;
}

.table-section {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
  overflow: hidden;
}

.title {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  color: white;
  padding: 20px 24px;
  font-size: 22px;
  font-weight: 600;
  display: flex;
  align-items: center;
}

/* Phần CSS cho khu vực bộ lọc */
.filter-section {
  padding: 20px 24px;
}

.filter-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  color: #555;
  font-size: 16px;
}

.filter-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-row {
  display: flex;
  gap: 20px;
  align-items: flex-end;
}

.filter-group {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.filter-group.search-group {
  flex: 2;
}

.filter-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #666;
}

.search-input-container {
  position: relative;
  display: flex;
  align-items: center;
}

.search-input {
  width: 100%;
  padding: 10px 15px 10px 40px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  height: 40px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  background-color: #f9fbfd;
}

.search-input:focus {
  outline: none;
  border-color: #4ba27b;
  box-shadow: 0 0 0 3px rgba(107, 164, 180, 0.2);
}

.search-icon {
  position: absolute;
  left: 14px;
  color: #888;
  font-size: 14px;
}

.form-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  width: 100%;
  height: 40px;
  background-color: #f9fbfd;
}

.form-select:focus {
  outline: none;
  border-color: #4ba27b;
  box-shadow: 0 0 0 3px rgba(107, 164, 180, 0.2);
}

.button-group {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 10px;
}

/* Phần CSS cho khu vực bảng */
.table-section {
  padding: 0 8px;
}

.list-header {
  background: #f1f5f9;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e0e7ff;
  flex-wrap: wrap;
  gap: 16px;
}

.list-title {
  font-size: 18px;
  font-weight: 600;
  color: #444;
  display: flex;
  align-items: center;
}

.tab-container {
  display: flex;
  gap: 5px;
}

.tab-item {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
  background: #e0e7ff;
  color: #475569;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s ease-in-out;
}

.col-trangthai, .col-action {
  flex: 0 0 150px;
  text-align: center;
  min-height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px 0;
}

.voucher-item-row .col-trangthai,
.voucher-item-row .col-action {
  min-height: 60px; 
  display: flex;
  align-items: center;
  justify-content: center;
}

.col-action .btn {
  padding: 6px 12px;
  margin: 0;
  height: 100%;
  box-sizing: border-box;
  z-index: 0;
}

.tab-item:hover {
  background: #d1ddf1;
}

.tab-item.active {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  color: white;
}

.tab-item .badge {
  background: #f1f5f9;
  color: #475569;
  border-radius: 10px;
  padding: 2px 8px;
  font-size: 12px;
}

.tab-item.active .badge {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

/* Phần CSS cho danh sách phiếu giảm giá */
.list-content-area {
  padding: 0 8px;
}

.voucher-list-header{
  display: flex;
  font-weight: bold;
  align-items: center;
  border-bottom: 1px solid #e0e7ff;
  font-size: 14px;
  transition: background-color 0.2s;
}
.voucher-item-row {
  display: flex;
  font-weight: 500;
  align-items: center;
  border-bottom: 1px solid #e0e7ff;
  font-size: 14px;
  transition: background-color 0.2s;
}

.voucher-list-header {
  background: #f1f5f9;
  font-weight: 600;
  color: #475569;
}

.voucher-item-row {
  cursor: pointer;
}

.voucher-item-row:hover {
  background-color: #f1f5f9;
}

.voucher-item-row.selected {
  background-color: #e0eaff;
}

/* Độ rộng cột */
.col-stt {
  flex: 0 0 60px;
}

.col-ma {
  flex: 0 0 140px;
}

.col-ten {
  flex: 1;
  min-width: 100px;
}

.col-phantram {
  flex: 0 0 120px;
  text-align: center;
}

.col-ngay {
  flex: 0 0 110px;
  text-align: center;
}

.col-mota {
  flex: 1;
  min-width: 120px;
}

.col-trangthai {
  flex: 0 0 150px;
  text-align: center;
}

.col-action {
  flex: 0 0 150px;
  text-align: center;
  padding: 10px 0;
  position: relative;
}

.status-badge {
  padding: 5px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
  color: white;
  min-width: 120px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  box-sizing: border-box;
}

.status-badge.active {
  background-color: #4caf50;
}

.status-badge.inactive {
  background-color: #ef5350;
}

.status-badge.expired {
  background-color: #90a4ae;
}

.status-badge.unknown {
  background-color: #ef5350;
}

/* Phần CSS cho phân trang */
.pagination-container {
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #e0e7ff;
  background: #f9fbfd;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-size-selector label {
  font-size: 14px;
  color: #475569;
  margin: 0;
}

.page-size-selector .form-select {
  width: 75px;
  height: 36px;
  background-color: #f9fbfd;
}

.pagination-controls {
  padding-top: 13px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.pagination-controls .btn {
  width: 40px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-info {
  padding-bottom: 15px;
  font-size: 14px;
  font-weight: 500;
  color: #444;
}

/* Phần CSS cho trạng thái trống và tải */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #90a4ae;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 20px;
  color: #b0bec5;
}

.empty-state h3 {
  margin-bottom: 10px;
  color: #607d8b;
}

/* Phần CSS cho modal */
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
  animation: fadeInOverlay 0.3s ease;
}

@keyframes fadeInOverlay {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content-custom {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  width: 60%; /* Tự động điều chỉnh theo màn hình, nhưng không vượt quá max-width */
  max-width: 1200px; /* Tăng độ rộng tối đa lên 1200px (có thể thay đổi tùy ý) */
  min-width: 600px; /* Đảm bảo modal không quá hẹp */
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  animation: slideInModal 0.4s ease-out;
}

@keyframes slideInModal {
  from { transform: translateY(-30px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.modal-header {
  padding: 16px 24px;
  border-bottom: 1px solid #e0e7ff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f1f5f9;
  border-top-left-radius: 12px;
  border-top-right-radius: 12px;
}

.modal-header .modal-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #444;
}

.modal-header .close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #90a4ae;
  transition: color 0.2s;
}

.modal-header .close-btn:hover {
  color: #607d8b;
}

.modal-body {
  padding: 0;
  overflow-y: auto;
  flex-grow: 1;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #e0e7ff;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  background-color: #f9fbfd;
  border-bottom-left-radius: 12px;
  border-bottom-right-radius: 12px;
}

/* Phần CSS cho tab trong modal */
.modal-tabs {
  display: flex;
  background-color: #f1f5f9;
  padding: 5px 24px 0 24px;
  border-bottom: 1px solid #e0e7ff;
}

.modal-tab-btn {
  background: none;
  border: none;
  padding: 12px 20px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  color: #444;
  border-bottom: 3px solid transparent;
  transition: all 0.2s;
}

.modal-tab-btn.active {
  color: #4ba27b;
  border-bottom-color: #4ba27b;
}

/* Phần CSS cho nội dung form trong modal */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px 24px;
  padding: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group.toggle-group {
  max-width: 250px;
  grid-column: span 1;
  align-items: flex-start;
}

.form-group label {
  margin-bottom: 8px;
  font-weight: 500;
  font-size: 14px;
  color: #607d8b;
}

.form-group label .required {
  color: #ef5350;
  margin-left: 2px;
}

.form-group input[type="text"],
.form-group input[type="number"],
.form-group input[type="date"],
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 10px 12px;
  font-size: 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background-color: #f9fbfd;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-group textarea {
  min-height: 80px;
  resize: vertical;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: #4ba27b;
  box-shadow: 0 0 0 3px rgba(107, 164, 180, 0.2);
}

.radio-group-inline {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-top: 5px;
}

.radio-group-inline input[type="radio"] {
  margin-right: 5px;
}

.radio-group-inline label {
  margin-bottom: 0;
  font-weight: normal;
}

.form-check.form-switch {
  display: flex;
  align-items: center;
  padding-left: 50px;
}

.form-check-input {
  margin-right: 10px;
}

/* Phần CSS cho bảng sản phẩm */
.voucher-list-container {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #e0e7ff;
  border-radius: 6px;
}

.tab-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.tab-table th,
.tab-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #e0e7ff;
}

.tab-table thead th {
  background-color: #f1f5f9;
  font-weight: 600;
  position: sticky;
  top: 0;
  z-index: 1;
}

.tab-table tbody tr:last-child td {
  border-bottom: none;
}

.tab-table tbody tr:hover {
  background-color: #f1f5f9;
}

.details-section-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #444;
  margin-bottom: 20px;
  padding: 5px 0 5px 20px;
  border-bottom: 1px solid #e0e7ff;
}
.btn-xoactkm {
  background-color: #ef5350;
  border-color: #ef5350;
  color: white;
  padding: 8px 10px;
  border-radius: 6px;
  display: inline-flex;
  border: 1px solid transparent;
}

/* Phần CSS cho nút bấm */
.btn {

  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.btn-success {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  border-color: #4caf50;
  color: white;
}

.btn-success:hover {
  background-color: #45a049;
  border-color: #45a049;
}

.btn-danger {
  background-color: #ef5350;
  border-color: #ef5350;
  color: white;
}

.btn-danger:hover {
  background-color: #e53935;
  border-color: #e53935;
}

.btn-secondary {
  background-color: #90a4ae;
  border-color: #90a4ae;
  color: white;
}

.btn-secondary:hover {
  background-color: #78909c;
  border-color: #78909c;
}

.btn-outline-primary {
  background-color: transparent;
  border: 1px solid #6ba4b4;
  color: #6ba4b4;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease-in-out;
}

.btn-outline-primary:hover {
  background-color: #45a049;
  border-color: #45a049;
  color: white;
}

/* Đảm bảo toàn bộ nội dung bên trong button cũng đổi trắng */
.btn-outline-primary:hover i,
.btn-outline-primary:hover span,
.btn-outline-primary:hover * {
  color: #45a049;
}


.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}

.btn-save {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  color: white;
  border-color: #4caf50;
}

.btn-save:hover {
  color: white;
  background-color: #45a049;
  border-color: #45a049;
}

/* Phần CSS cho bộ lọc trong modal */
.filter-controls {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f1f5f9;
  border-radius: 8px;
  align-items: flex-end;
}

.filter-item {
  flex: 1;
  display: flex;
  flex-direction: column;
}


.filter-item label {
  font-size: 13px;
  font-weight: 500;
  color: #666;
  margin-bottom: 6px;
}

.filter-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  height: 38px;
  background-color: #f9fbfd;
}

.filter-input:focus {
  outline: none;
  border-color: #45a049;
  box-shadow: 0 0 0 2px rgba(107, 164, 180, 0.2);
}

/* CSS cho phần check số sp được áp dụng cho đợt */
.product-badge {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  width: 20px;
  height: 20px;
  background-color: #ef5350;
  color: white;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  position: absolute;
  top: 5px;
  right: 5px;
  vertical-align: middle;
}

/* Phần CSS đáp ứng (responsive) */
@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }

  .pagination-container {
    flex-direction: column;
    gap: 15px;
  }

  .list-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .col-phantram,
  .col-mota {
    display: none;
  }

  .voucher-item-row {
    flex-wrap: wrap;
  }

  .col-ten {
    flex: 1 1 100%;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .modal-content-custom {
    width: 95%;
  }
}
</style>