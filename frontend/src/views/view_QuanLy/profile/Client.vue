<template>
  <div class="container-fluid p-4">
    <!-- Notifications -->
    <div v-if="notification.show" :class="['notification', notification.type]">
      <i :class="notification.type === 'success' ? 'fa-solid fa-check-circle' : 'fa-solid fa-exclamation-triangle'"></i>
      {{ notification.message }}
      <button @click="hideNotification" class="close-btn">&times;</button>
    </div>

    <div class="header">
      <div class="title">
        <i class="fa-solid fa-users p-2"></i>
        Quản lý khách hàng
      </div>

      <div class="filter-section">
        <div class="filter-header">
          <i class="fa-solid fa-filter p-2"></i>
          <strong>Bộ lọc</strong>
        </div>

        <div class="filter-content">
          <div class="filter-row">
            <div class="filter-group">
              <label>Tìm kiếm:</label>
              <input type="text" v-model="searchFilters.keyword" placeholder="Tìm kiếm tên và sdt..."
                @input="debounceSearch">
            </div>
            <div class="filter-group">
              <label>Giới tính:</label>
              <select v-model="searchFilters.gioiTinh">
                <option value="">Tất cả</option>
                <option value="1">Nam</option>
                <option value="0">Nữ</option>
              </select>
            </div>
            <div class="filter-group">
              <label>Trạng thái:</label>
              <select v-model="searchFilters.trangThai">
                <option value="">Tất cả</option>
                <option value="1">Hoạt động</option>
                <option value="0">Ngừng hoạt động</option>
              </select>
            </div>
          </div>

          <div class="button-group">
            <button class="btn btn-primary" @click="performSearch" :disabled="loading">
              <i class="fa-solid fa-magnifying-glass"></i>
              {{ loading ? 'Đang tìm...' : 'Tìm kiếm' }}
            </button>
            <button class="btn btn-secondary" @click="resetFilters">
              <i class="fa-solid fa-rotate-right"></i>
              Làm mới
            </button>
            <button class="btn btn-primary" @click="exportExcel" :disabled="loading">
              <i class="fa-solid fa-file-export"></i>
              Xuất Excel
            </button>
            <button class="btn btn-success" @click="triggerImportExcel" :disabled="loading">
              <i class="fa-solid fa-file-import"></i>
              Import Excel
            </button>
            <button class="btn btn-info" @click="downloadTemplate" :disabled="loading">
              <i class="fa-solid fa-download"></i>
              Download Template
            </button>
            <input type="file" ref="excelInput" accept=".xlsx,.xls" style="display: none" @change="importExcel">
          </div>
        </div>
      </div>
    </div>

    <div class="client-list">
      <div class="list-header">
        <div class="list-title">
          <i class="fa-solid fa-list-ul p-2"></i>
          Danh sách khách hàng
        </div>
        <router-link to="/khach-hang/create">
          <button class="btn btn-primary">
            <i class="fa-solid fa-plus"></i>
            Thêm
          </button>
        </router-link>
      </div>

      <!-- Loading Spinner -->
      <div v-if="loading" class="loading-container">
        <div class="spinner"></div>
        <p>Đang tải dữ liệu...</p>
      </div>

      <!-- Empty State -->
      <div v-else-if="khachHangs.length === 0" class="empty-state">
        <i class="fa-solid fa-users-slash"></i>
        <h3>Không tìm thấy khách hàng nào</h3>
        <p>Thử thay đổi bộ lọc hoặc thêm khách hàng mới</p>
      </div>

      <!-- Table -->
      <div v-else class="table-container">
        <table>
          <thead>
            <tr>
              <th>STT</th>
              <th>Ảnh</th>
              <th>Tên khách hàng</th>
              <th>Email</th>
              <th>Số điện thoại</th>
              <th>Giới tính</th>
              <th>Ngày sinh</th>
              <th class="address-column">Địa chỉ</th>
              <th class="status-column">Trạng Thái</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(kh, index) in khachHangs" :key="kh.id">
              <td>{{ index + 1 + (currentPage - 1) * pageSize }}</td>
              <td>
                <div class="employee-photo">
                  <img v-if="kh.hinhAnh" :src="kh.hinhAnh" :alt="kh.tenKhachHang || 'Avatar'" @error="onImageError"
                    loading="lazy" />
                  <i v-else class="fa-solid fa-user"></i>
                </div>
              </td>
              <td>{{ kh.tenKhachHang || 'N/A' }}</td>
              <td>{{ kh.email || 'N/A' }}</td>
              <td>{{ kh.sdt || 'N/A' }}</td>
              <td>{{ kh.gioiTinh == 1 ? 'Nam' : 'Nữ' }}</td>
              <td>{{ kh.ngaySinh ? formatDate(kh.ngaySinh) : 'N/A' }}</td>
              <td class="address-cell" v-html="formatAddressClient(kh.defaultDiaChi)"></td>
              <td>
                <span :class="['status-badge', getStatusClass(kh.trangThai)]">
                  {{ formatStatus(kh.trangThai) }}
                </span>
              </td>
              <td>
                <div class="action-buttons">
                  <router-link :to="`/khach-hang/update/${kh.id}`">
                    <button class="action-btn btn-warning" title="Chỉnh sửa">
                      <i class="fa-solid fa-pen-to-square"></i>
                    </button>
                  </router-link>
                  <button class="action-btn btn-primary" @click="openAddressModal(kh)" title="Quản lý địa chỉ">
                    <i class="fa-solid fa-map-marker-alt"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- Pagination -->
        <div class="pagination-wrapper">
          <ul class="pagination justify-content-end">
            <li class="page-item" :class="{ disabled: currentPage === 1 }">
              <button class="page-link" @click="goToPage(currentPage - 1)"
                :disabled="currentPage === 1">&laquo;</button>
            </li>
            <li class="page-item" v-for="n in totalPages" :key="n" :class="{ active: currentPage === n }">
              <button class="page-link" @click="goToPage(n)">{{ n }}</button>
            </li>
            <li class="page-item" :class="{ disabled: currentPage === totalPages }">
              <button class="page-link" @click="goToPage(currentPage + 1)"
                :disabled="currentPage === totalPages">&raquo;</button>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Address Management Modal -->
    <div v-if="showAddressModal" class="modal-overlay" @click="closeAddressModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h4>Quản lý địa chỉ của {{ selectedClient?.tenKhachHang || 'N/A' }}</h4>
          <button @click="closeAddressModal" class="btn btn-secondary">X</button>
        </div>
        <div class="modal-body">
          <div v-if="loadingAddresses" class="loading-container">
            <div class="spinner"></div>
            <p>Đang tải danh sách địa chỉ...</p>
          </div>
          <div v-else-if="addresses.length === 0" class="empty-state">
            <i class="fa-solid fa-map-marker-alt"></i>
            <h3>Chưa có địa chỉ nào</h3>
            <p>Thêm địa chỉ mới cho khách hàng</p>
          </div>
          <div v-else class="address-list">
            <div v-for="address in addresses" :key="address.id" class="address-item">
              <label class="radio-label">
                <input type="radio" class="radio-custom" :value="address.id" v-model="selectedAddressId"
                  @change="setDefaultAddress(address.id)" :disabled="loadingAddresses" />
                <div class="address-info">
                  <p v-html="formatAddress(address)"></p>
                  <p v-if="address.isDefault" class="default-label">Mặc định</p>
                </div>
                <button class="action-btn btn-warning" @click="openEditAddressForm(address)" title="Chỉnh sửa">
                  <i class="fa-solid fa-pen-to-square"></i>
                </button>
                <button class="action-btn btn-danger" @click="openDeleteConfirmModal(address.id)" title="Xóa">
                  <i class="fa-solid fa-trash"></i>
                </button>
              </label>
            </div>
          </div>
          <button class="btn btn-primary" @click="openAddAddressForm" :disabled="loadingAddresses">
            <i class="fa-solid fa-plus"></i>
            Thêm địa chỉ
          </button>
        </div>
      </div>
    </div>

    <!-- Address Form Modal -->
    <div v-if="showAddressForm" class="modal-overlay" @click="closeAddressForm">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ isEditingAddress ? 'Chỉnh sửa địa chỉ' : 'Thêm địa chỉ' }}</h3>
          <button @click="closeAddressForm" class="btn btn-secondary">X</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitAddressForm" class="address-form">
            <div class="form-group">
              <label class="required">Tên người nhận</label>
              <input v-model="addressForm.tenNguoiNhan" type="text" placeholder="Nhập tên người nhận" class="form-input"
                :class="{ 'error': addressErrors.tenNguoiNhan }" @blur="validateAddressField('tenNguoiNhan')" />
              <div v-if="addressErrors.tenNguoiNhan" class="error-message">{{ addressErrors.tenNguoiNhan }}</div>
            </div>
            <div class="form-group">
              <label class="required">Số điện thoại</label>
              <input v-model="addressForm.sdt" type="text" placeholder="Nhập số điện thoại" class="form-input"
                maxlength="10" :class="{ 'error': addressErrors.sdt }" @blur="validateAddressField('sdt')" />
              <div v-if="addressErrors.sdt" class="error-message">{{ addressErrors.sdt }}</div>
            </div>
            <div class="form-group">
              <label class="required">Tỉnh/Thành phố</label>
              <select v-model="addressForm.tinh" class="form-select" :class="{ 'error': addressErrors.tinh }"
                @change="onProvinceChange" :disabled="loadingProvinces">
                <option value="">--Chọn Tỉnh/Thành phố--</option>
                <option v-for="province in provinces" :key="province.ProvinceID" :value="province.ProvinceID">
                  {{ province.ProvinceName }}
                </option>
              </select>
              <div v-if="addressErrors.tinh" class="error-message">{{ addressErrors.tinh }}</div>
            </div>
            <!-- Bỏ Quận/Huyện -->
            <div class="form-group">
              <label class="required">Xã/Phường</label>
              <select v-model="addressForm.phuong" class="form-select" :class="{ 'error': addressErrors.phuong }"
                :disabled="!addressForm.tinh || loadingWards">
                <option value="">--Chọn Xã/Phường--</option>
                <option v-for="ward in wards" :key="ward.WardCode" :value="ward.WardCode">
                  {{ ward.WardName }}
                </option>
              </select>
              <div v-if="addressErrors.phuong" class="error-message">{{ addressErrors.phuong }}</div>
            </div>
            <div class="form-group">
              <label class="required">Số nhà/Ngõ/Đường</label>
              <input v-model="addressForm.chiTiet" type="text" placeholder="123 Đường ABC, Ngõ XYZ" class="form-input"
                :class="{ 'error': addressErrors.chiTiet }" @blur="validateAddressField('chiTiet')" />
              <div v-if="addressErrors.chiTiet" class="error-message">{{ addressErrors.chiTiet }}</div>
            </div>
            <div class="form-group">
              <label>
                <input type="checkbox" v-model="addressForm.isDefault" />
                Đặt làm địa chỉ mặc định
              </label>
            </div>
            <div class="form-group full-width">
              <div class="button-group">
                <button type="button" @click="closeAddressForm" class="btn btn-secondary">Hủy</button>
                <button type="submit" class="btn btn-primary" :disabled="loadingAddresses">
                  <i v-if="loadingAddresses" class="fa-solid fa-spinner fa-spin"></i>
                  {{ isEditingAddress ? 'Cập nhật' : 'Thêm' }}
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteConfirmModal" class="modal-overlay" @click="closeDeleteConfirmModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Xác nhận xóa địa chỉ</h3>
          <button @click="closeDeleteConfirmModal" class="btn btn-secondary">X</button>
        </div>
        <div class="modal-body">
          <p>Bạn có chắc chắn muốn xóa địa chỉ này không?</p>
          <div class="button-group">
            <button class="btn btn-secondary" @click="closeDeleteConfirmModal">Hủy</button>
            <button class="btn btn-danger" @click="confirmDeleteAddress" :disabled="loadingAddresses">
              <i v-if="loadingAddresses" class="fa-solid fa-spinner fa-spin"></i>
              Xóa
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import KhachHangService from '@/services/RoleQuanLy/KhachHangService';
import { ref } from 'vue';
import { useRouter } from 'vue-router';

export default {
  name: 'Client',
  setup() {
    const router = useRouter();
    const khachHangs = ref([]);
    const currentPage = ref(1);
    const pageSize = ref(5);
    const totalPages = ref(0);
    const totalElements = ref(0);
    const isLastPage = ref(false);
    const loading = ref(false);
    const loadingAddresses = ref(false);
    const loadingProvinces = ref(false);
    const loadingDistricts = ref(false);
    const loadingWards = ref(false);
    const searchTimeout = ref(null);
    const showAddressModal = ref(false);
    const showAddressForm = ref(false);
    const showDeleteConfirmModal = ref(false);
    const isEditingAddress = ref(false);
    const selectedClient = ref(null);
    const selectedAddressId = ref(null);
    const addressToDelete = ref(null);
    const addresses = ref([]);
    const provinces = ref([]);
    const districts = ref([]);
    const wards = ref([]);
    const addressForm = ref({
      id: null,
      tinh: '',
      quan: '',
      phuong: '',
      chiTiet: '',
      tenNguoiNhan: '',
      sdt: '',
      isDefault: false
    });
    const addressErrors = ref({});
    const excelInput = ref(null);
    const notification = ref({
      show: false,
      type: 'success',
      message: ''
    });
    const searchFilters = ref({
      keyword: '',
      trangThai: '',
      gioiTinh: ''
    });

    const showNotification = (type, message) => {
      notification.value = { show: true, type, message };
      setTimeout(() => {
        hideNotification();
      }, 3000);
    };

    const hideNotification = () => {
      notification.value = { show: false, type: '', message: '' };
    };

    const fetchKhachHangs = async () => {
      try {
        loading.value = true;
        const params = {
          page: currentPage.value - 1, // API expects 0-based page
          size: pageSize.value,
          sortBy: 'ngayTao',
          sortDir: 'desc',
          ...buildSearchParams()
        };
        const response = await KhachHangService.getAllKhachHang(params);
        const data = response.data || response;
        khachHangs.value = data.content.map(kh => ({
          ...kh,
          hinhAnh: kh.hinhAnh && kh.hinhAnh.trim() !== ''
            ? `http://localhost:8080/uploads/nhan_vien/${kh.hinhAnh}`
            : null,
          defaultDiaChi: kh.diaChis && kh.diaChis.length > 0 ? kh.diaChis.find(addr => addr.isDefault) || kh.diaChis[0] : null
        }));
        console.log('Fetched khach hangs:', khachHangs.value);
        console.log('Sample khach hang ngaySinh:', khachHangs.value[0]?.ngaySinh);
        
        // Tối ưu hóa: Sửa dữ liệu địa chỉ thiếu tỉnh thành
        if (khachHangs.value.length > 0 && provinces.value.length > 0) {
          console.log('=== DEBUG: Fixing missing province names ===');
          console.log('Provinces loaded:', provinces.value.length);
          console.log('Available province IDs:', provinces.value.map(p => p.ProvinceID).slice(0, 10));
          let fixedCount = 0;
          khachHangs.value.forEach(kh => {
            if (kh.defaultDiaChi && kh.defaultDiaChi.provinceId) {
              // Kiểm tra thanhPho có rỗng, null, undefined hoặc chỉ có khoảng trắng
              const hasValidThanhPho = kh.defaultDiaChi.thanhPho && 
                kh.defaultDiaChi.thanhPho.trim() !== '' && 
                kh.defaultDiaChi.thanhPho !== 'null' && 
                kh.defaultDiaChi.thanhPho !== 'undefined';
              
              if (!hasValidThanhPho) {
                // Thử tìm theo ProvinceID (số)
                let foundProvince = provinces.value.find(p => p.ProvinceID === kh.defaultDiaChi.provinceId);
                
                // Nếu không tìm thấy, thử tìm theo ProvinceID (chuỗi)
                if (!foundProvince) {
                  foundProvince = provinces.value.find(p => p.ProvinceID == kh.defaultDiaChi.provinceId);
                }
                
                // Nếu vẫn không tìm thấy, thử tìm theo ProvinceID (string)
                if (!foundProvince) {
                  foundProvince = provinces.value.find(p => String(p.ProvinceID) === String(kh.defaultDiaChi.provinceId));
                }
                
                if (foundProvince) {
                  console.log(`Fixed ${kh.tenKhachHang}: ${kh.defaultDiaChi.provinceId} -> ${foundProvince.ProvinceName}`);
                  console.log(`Before: thanhPho='${kh.defaultDiaChi.thanhPho}'`);
                  kh.defaultDiaChi.thanhPho = foundProvince.ProvinceName;
                  console.log(`After: thanhPho='${kh.defaultDiaChi.thanhPho}'`);
                  fixedCount++;
                } else {
                  console.log(`Province not found for ID: ${kh.defaultDiaChi.provinceId} (type: ${typeof kh.defaultDiaChi.provinceId})`);
                  console.log(`Available IDs:`, provinces.value.map(p => `${p.ProvinceID}(${typeof p.ProvinceID})`).slice(0, 10));
                }
              } else {
                console.log(`${kh.tenKhachHang} already has valid thanhPho: '${kh.defaultDiaChi.thanhPho}'`);
              }
            } else {
              console.log(`${kh.tenKhachHang} missing defaultDiaChi or provinceId`);
            }
          });
          console.log(`Total fixed: ${fixedCount} addresses`);
        } else {
          console.log('=== DEBUG: Cannot fix addresses ===');
          console.log('KhachHangs:', khachHangs.value.length, 'Provinces:', provinces.value.length);
        }
        totalPages.value = data.totalPages || 0;
        totalElements.value = data.totalElements || 0;
        isLastPage.value = data.last || false;
      } catch (error) {
        console.error('API Error:', error.response || error);
        showNotification('error', 'Không thể tải danh sách khách hàng: ' + (error.response?.data?.message || error.message));
        khachHangs.value = [];
      } finally {
        loading.value = false;
      }
    };

    const buildSearchParams = () => {
      const params = {};
      if (searchFilters.value.keyword?.trim()) params.keyword = searchFilters.value.keyword.trim();
      if (searchFilters.value.trangThai !== '') {
        params.status = parseInt(searchFilters.value.trangThai);
      }
      if (searchFilters.value.gioiTinh !== '') {
        params.gioiTinh = parseInt(searchFilters.value.gioiTinh);
      }
      return params;
    };

    const debounceSearch = () => {
      clearTimeout(searchTimeout.value);
      searchTimeout.value = setTimeout(() => performSearch(), 500);
    };

    const performSearch = async () => {
      currentPage.value = 1; // Reset to first page on search
      // Đảm bảo có dữ liệu provinces trước khi fetch khách hàng
      if (provinces.value.length === 0) {
        await loadProvinces();
      }
      await fetchKhachHangs();
    };

    const resetFilters = async () => {
      searchFilters.value = {
        keyword: '',
        trangThai: '',
        gioiTinh: ''
      };
      currentPage.value = 1;
      // Đảm bảo có dữ liệu provinces trước khi fetch khách hàng
      if (provinces.value.length === 0) {
        await loadProvinces();
      }
      await fetchKhachHangs();
    };

    const exportExcel = async () => {
      try {
        loading.value = true;
        await KhachHangService.exportExcel();
        showNotification('success', 'Xuất Excel thành công!');
      } catch (error) {
        showNotification('error', 'Xuất Excel thất bại: ' + (error.response?.data?.message || error.message));
      } finally {
        loading.value = false;
      }
    };

    const triggerImportExcel = () => {
      excelInput.value.click();
    };

    const importExcel = async (event) => {
      const file = event.target.files[0];
      if (!file) return;

      try {
        loading.value = true;
        const response = await KhachHangService.importExcel(file);
        showNotification('success', response.data.message || 'Import Excel thành công!');
        await fetchKhachHangs(); // Refresh danh sách
      } catch (error) {
        const errorMessage = error.response?.data?.message || 'Import Excel thất bại: ' + error.message;
        showNotification('error', errorMessage);
      } finally {
        loading.value = false;
        // Reset input file
        event.target.value = '';
      }
    };

    const downloadTemplate = async () => {
      try {
        loading.value = true;
        await KhachHangService.downloadTemplate();
        showNotification('success', 'Tải mẫu Excel thành công!');
      } catch (error) {
        showNotification('error', 'Tải mẫu Excel thất bại: ' + (error.response?.data?.message || error.message));
      } finally {
        loading.value = false;
      }
    };


    const handleClientCreated = async () => {
      currentPage.value = 1;
      // Đảm bảo có dữ liệu provinces trước khi fetch khách hàng
      if (provinces.value.length === 0) {
        await loadProvinces();
      }
      await fetchKhachHangs();
      showNotification('success', 'Thêm khách hàng thành công!');
    };

    const goToPage = async (page) => {
      if (page >= 1 && page <= totalPages.value && page !== currentPage.value) {
        currentPage.value = page;
        // Đảm bảo có dữ liệu provinces trước khi fetch khách hàng
        if (provinces.value.length === 0) {
          await loadProvinces();
        }
        await fetchKhachHangs();
      }
    };

    const openAddressModal = async (client) => {
      selectedClient.value = client;
      showAddressModal.value = true;
      await fetchAddresses(client.id);
    };

    const closeAddressModal = () => {
      showAddressModal.value = false;
      selectedClient.value = null;
      addresses.value = [];
      selectedAddressId.value = null;
    };

    const fetchAddresses = async (customerId) => {
      try {
        loadingAddresses.value = true;
        const response = await KhachHangService.getAddressesByCustomerId(customerId);
        addresses.value = response.data || [];
        console.log('Addresses loaded:', addresses.value);
        const defaultAddress = addresses.value.find(addr => addr.isDefault);
        selectedAddressId.value = defaultAddress ? defaultAddress.id : null;
      } catch (error) {
        console.error('Error loading addresses:', error);
        showNotification('error', 'Không thể tải danh sách địa chỉ: ' + (error.response?.data?.message || error.message));
        addresses.value = [];
      } finally {
        loadingAddresses.value = false;
      }
    };

    const setDefaultAddress = async (addressId) => {
      console.log('Calling setDefaultAddress with customerId:', selectedClient.value.id, 'addressId:', addressId);
      try {
        loadingAddresses.value = true;
        await KhachHangService.setDefaultAddress(selectedClient.value.id, addressId);
        showNotification('success', 'Đặt địa chỉ mặc định thành công!');
        await fetchAddresses(selectedClient.value.id);
        // Đảm bảo có dữ liệu provinces trước khi fetch khách hàng
        if (provinces.value.length === 0) {
          await loadProvinces();
        }
        await fetchKhachHangs();
        selectedAddressId.value = addressId;
      } catch (error) {
        console.error('Error:', error.message, error.response?.status, error.response?.data);
        showNotification('error', 'Không thể đặt địa chỉ mặc định: ' + (error.response?.data?.message || error.message));
      } finally {
        loadingAddresses.value = false;
      }
    };

    const openAddAddressForm = () => {
      addressForm.value = { tinh: '', quan: '', phuong: '', chiTiet: '', tenNguoiNhan: '', sdt: '', isDefault: false };
      isEditingAddress.value = false;
      showAddressForm.value = true;
      loadProvinces();
    };

    const openEditAddressForm = async (address) => {
      try {
        addressForm.value = {
          id: address.id,
          tinh: '',
          quan: '',
          phuong: '',
          chiTiet: address.soNha || address.diaChiChiTiet || '',
          tenNguoiNhan: address.tenNguoiNhan || '',
          sdt: address.sdt || '',
          isDefault: address.isDefault || false
        };
        isEditingAddress.value = true;

        // Load provinces first
        await loadProvinces();
        console.log('Provinces loaded in openEditAddressForm:', provinces.value.length);

        // Ưu tiên dùng ID do backend lưu
        const provinceId = address.provinceId || '';
        const districtId = address.districtId || '';
        const wardCode = address.wardCode || '';
        let thanhPho = address.thanhPho;
        let quanHuyen = address.quanHuyen;
        let xaPhuong = address.xaPhuong;
        let soNha = address.soNha;

        // Nếu có diaChiChiTiet (từ Excel import), thử phân tách
        if (address.diaChiChiTiet && (!thanhPho || !quanHuyen || !xaPhuong)) {
          console.log('Parsing diaChiChiTiet:', address.diaChiChiTiet);
          const parsedAddress = parseAddressFromString(address.diaChiChiTiet);
          console.log('Parsed address result:', parsedAddress);
          
          thanhPho = thanhPho || parsedAddress.thanhPho;
          quanHuyen = quanHuyen || parsedAddress.quanHuyen;
          xaPhuong = xaPhuong || parsedAddress.xaPhuong;
          soNha = soNha || parsedAddress.soNha;
          
          console.log('Final address components:', { thanhPho, quanHuyen, xaPhuong, soNha });
          
          // Cập nhật chiTiet nếu chưa có
          if (!addressForm.value.chiTiet && soNha) {
            addressForm.value.chiTiet = soNha;
          }
        }

        // Tìm và set tỉnh/thành phố
        if (provinceId) {
          addressForm.value.tinh = provinceId;
        } else if (thanhPho) {
          console.log('Looking for province:', thanhPho);
          console.log('Available provinces:', provinces.value.map(p => p.ProvinceName));
          
                                const province = provinces.value.find(p => {
                        const provinceName = p.ProvinceName.trim().toLowerCase();
                        const searchName = thanhPho.trim().toLowerCase();
                        
                        console.log(`Comparing: "${provinceName}" with "${searchName}"`);
                        
                        const exactMatch = provinceName === searchName;
                        const tpMatch = provinceName.replace('tp. ', 'thành phố ').replace('tp ', 'thành phố ') === searchName;
                        const tinhMatch = provinceName.replace('tỉnh ', '') === searchName;
                        const hcmMatch = searchName.includes('hcm') && provinceName.includes('hồ chí minh');
                        const hanoiMatch = searchName.includes('hà nội') && provinceName.includes('hà nội');
                        const danangMatch = searchName.includes('đà nẵng') && provinceName.includes('đà nẵng');
                        
                        // Thêm logic matching cho các tỉnh khác
                        const caMauMatch = searchName.includes('cà mau') && provinceName.includes('cà mau');
                        const kienGiangMatch = searchName.includes('kiên giang') && provinceName.includes('kiên giang');
                        const anGiangMatch = searchName.includes('an giang') && provinceName.includes('an giang');
                        const canThoMatch = searchName.includes('cần thơ') && provinceName.includes('cần thơ');
                        const dongThapMatch = searchName.includes('đồng tháp') && provinceName.includes('đồng tháp');
                        
                        // Thêm logic matching chung cho các tỉnh
                        const generalMatch = provinceName.includes(searchName) || searchName.includes(provinceName);
                        
                        const isMatch = exactMatch || tpMatch || tinhMatch || hcmMatch || hanoiMatch || danangMatch || 
                                      caMauMatch || kienGiangMatch || anGiangMatch || canThoMatch || dongThapMatch || generalMatch;
                        
                        if (isMatch) {
                          console.log(`Match found: "${provinceName}" matches "${searchName}"`);
                        }
                        
                        return isMatch;
                      });
          
          if (province) {
            console.log('Found province:', province.ProvinceName);
            addressForm.value.tinh = province.ProvinceID;
          } else {
            console.log('Province not found for:', thanhPho);
          }
        }

        // Tìm và set quận/huyện
        if (addressForm.value.tinh && (districtId || quanHuyen)) {
          console.log('Loading districts for province ID:', addressForm.value.tinh);
          await loadDistricts(addressForm.value.tinh);
          console.log('Looking for district:', districtId || quanHuyen);
          console.log('Available districts:', districts.value.map(d => d.DistrictName));
          
          if (districtId) {
            const exists = districts.value.find(d => d.DistrictID === districtId);
            if (exists) {
              addressForm.value.quan = districtId;
            }
          }

          const district = !addressForm.value.quan && quanHuyen ? districts.value.find(d => {
            const districtName = d.DistrictName.trim().toLowerCase();
            const searchName = quanHuyen.trim().toLowerCase();
            
            console.log(`Comparing district: "${districtName}" with "${searchName}"`);
            
            const exactMatch = districtName === searchName;
            const qMatch = districtName.replace('q. ', 'quận ').replace('q ', 'quận ') === searchName;
            const huyenMatch = districtName.replace('huyện ', '') === searchName;
            const quanMatch = searchName.includes('quận') && districtName.includes('quận');
            const huyenGeneralMatch = searchName.includes('huyện') && districtName.includes('huyện');
            
            // Thêm logic matching chung
            const generalMatch = districtName.includes(searchName) || searchName.includes(districtName);
            
            const isMatch = exactMatch || qMatch || huyenMatch || quanMatch || huyenGeneralMatch || generalMatch;
            
            if (isMatch) {
              console.log(`District match found: "${districtName}" matches "${searchName}"`);
            }
            
            return isMatch;
          }) : null;
          
          if (!addressForm.value.quan && district) {
            console.log('Found district:', district.DistrictName);
            addressForm.value.quan = district.DistrictID;
          } else {
            console.log('District not found for:', quanHuyen);
          }
        }

        // Tìm và set xã/phường theo tỉnh (API wards theo tỉnh)
        if (addressForm.value.tinh) {
          console.log('Loading wards for province ID:', addressForm.value.tinh);
          await loadWards(addressForm.value.tinh);
          console.log('Looking for ward:', wardCode || xaPhuong);
          console.log('Available wards:', wards.value.map(w => w.WardName));

          if (wardCode) {
            const exists = wards.value.find(w => w.WardCode === wardCode);
            if (exists) {
              addressForm.value.phuong = wardCode;
            }
          }

          if (!addressForm.value.phuong && xaPhuong) {
            const ward = wards.value.find(w => {
              const wardName = w.WardName.trim().toLowerCase();
              const searchName = xaPhuong.trim().toLowerCase();

              console.log(`Comparing ward: "${wardName}" with "${searchName}"`);

              const exactMatch = wardName === searchName;
              const pMatch = wardName.replace('p. ', 'phường ').replace('p ', 'phường ') === searchName;
              const xaMatch = wardName.replace('xã ', '') === searchName;
              const phuongMatch = searchName.includes('phường') && wardName.includes('phường');
              const xaGeneralMatch = searchName.includes('xã') && wardName.includes('xã');

              const generalMatch = wardName.includes(searchName) || searchName.includes(wardName);

              const isMatch = exactMatch || pMatch || xaMatch || phuongMatch || xaGeneralMatch || generalMatch;

              if (isMatch) {
                console.log(`Ward match found: "${wardName}" matches "${searchName}"`);
              }

              return isMatch;
            });

            if (ward) {
              console.log('Found ward:', ward.WardName);
              addressForm.value.phuong = ward.WardCode;
            } else {
              console.log('Ward not found for:', xaPhuong);
            }
          }
        }

        showAddressForm.value = true;
      } catch (error) {
        console.error('Error loading address form:', error);
        showNotification('error', 'Không thể tải dữ liệu để chỉnh sửa địa chỉ');
        addressErrors.value = {};
      }
    };

    const closeAddressForm = () => {
      showAddressForm.value = false;
      addressForm.value = { tinh: '', quan: '', phuong: '', chiTiet: '', tenNguoiNhan: '', sdt: '', isDefault: false };
      addressErrors.value = {};
      provinces.value = [];
      districts.value = [];
      wards.value = [];
    };

    const openDeleteConfirmModal = (addressId) => {
      addressToDelete.value = addressId;
      showDeleteConfirmModal.value = true;
    };

    const closeDeleteConfirmModal = () => {
      showDeleteConfirmModal.value = false;
      addressToDelete.value = null;
    };

    const confirmDeleteAddress = async () => {
      if (!addressToDelete.value) return;
      try {
        loadingAddresses.value = true;
        await KhachHangService.deleteAddress(selectedClient.value.id, addressToDelete.value);
        showNotification('success', 'Xóa địa chỉ thành công!');
        await fetchAddresses(selectedClient.value.id);
        // Đảm bảo có dữ liệu provinces trước khi fetch khách hàng
        if (provinces.value.length === 0) {
          await loadProvinces();
        }
        await fetchKhachHangs();
        closeDeleteConfirmModal();
      } catch (error) {
        showNotification('error', 'Không thể xóa địa chỉ: ' + (error.response?.data?.message || error.message));
      } finally {
        loadingAddresses.value = false;
      }
    };

    const loadProvinces = async () => {
      try {
        provinces.value = [];
        districts.value = [];
        wards.value = [];
        loadingProvinces.value = true;
        console.log('Loading provinces...');
        const response = await KhachHangService.getProvinces();
        console.log('Provinces API response:', response);
        provinces.value = response.data.data || [];
        console.log('Provinces loaded:', provinces.value);
        console.log('Sample province names:', provinces.value.slice(0, 5).map(p => p.ProvinceName));
      } catch (error) {
        console.error('Error loading provinces:', error);
        showNotification('error', 'Không thể tải danh sách tỉnh/thành phố');
      } finally {
        loadingProvinces.value = false;
      }
    };

    const loadDistricts = async (provinceId) => {
      try {
        districts.value = [];
        wards.value = [];
        loadingDistricts.value = true;
        const response = await KhachHangService.getDistricts(provinceId);
        districts.value = response.data.data || [];
        console.log('Districts loaded for province', provinceId, ':', districts.value);
      } catch (error) {
        console.error('Error loading districts:', error);
        showNotification('error', 'Không thể tải danh sách quận/huyện');
      } finally {
        loadingDistricts.value = false;
      }
    };

    const loadWards = async (districtId) => {
      try {
        wards.value = [];
        loadingWards.value = true;
        const response = await KhachHangService.getWards(districtId);
        wards.value = response.data.data || [];
        console.log('Wards loaded for district', districtId, ':', wards.value);
      } catch (error) {
        console.error('Error loading wards:', error);
      } finally {
        loadingWards.value = false;
      }
    };

    const onProvinceChange = async () => {
      if (addressForm.value.tinh) {
        validateAddressField('tinh');
        await loadWards(addressForm.value.tinh);
        addressForm.value.phuong = '';
      } else {
        wards.value = [];
        addressForm.value.phuong = '';
      }
    };

    const onDistrictChange = async () => {};

    const validateAddressField = (fieldName) => {
      const rules = {
        tinh: [{ required: true, message: 'Vui lòng chọn tỉnh/thành phố' }],
        phuong: [{ required: true, message: 'Vui lòng chọn xã/phường' }],
        chiTiet: [
          { required: true, message: 'Vui lòng nhập địa chỉ chi tiết' },
          { minLength: 5, message: 'Địa chỉ phải có ít nhất 5 ký tự' }
        ],
        tenNguoiNhan: [
          { required: true, message: 'Vui lòng nhập tên người nhận' },
          { minLength: 2, message: 'Tên người nhận phải có ít nhất 2 ký tự' }
        ],
        sdt: [
          { required: true, message: 'Vui lòng nhập số điện thoại' },
          { pattern: /^[0-9]{10}$/, message: 'Số điện thoại phải có đúng 10 chữ số' }
        ]
      };
      const value = addressForm.value[fieldName];
      for (const rule of rules[fieldName] || []) {
        if (rule.required && (!value || value.toString().trim() === '')) {
          addressErrors.value[fieldName] = rule.message;
          return false;
        }
        if (rule.minLength && value && value.length < rule.minLength) {
          addressErrors.value[fieldName] = rule.message;
          return false;
        }
        if (rule.pattern && value && !rule.pattern.test(value)) {
          addressErrors.value[fieldName] = rule.message;
          return false;
        }
      }
      delete addressErrors.value[fieldName];
      return true;
    };

    const validateAddressForm = () => {
      addressErrors.value = {};
      return ['tinh', 'phuong', 'chiTiet', 'tenNguoiNhan', 'sdt'].every(field => validateAddressField(field));
    };

    const submitAddressForm = async () => {
      if (!validateAddressForm()) {
        showNotification('error', 'Vui lòng kiểm tra lại thông tin địa chỉ');
        return;
      }
      try {
        loadingAddresses.value = true;
        const addressData = {
          provinceId: addressForm.value.tinh || null,
          districtId: null,
          wardCode: addressForm.value.phuong || null,
          thanhPho: provinces.value.find(p => p.ProvinceID === addressForm.value.tinh)?.ProvinceName || '',
          quanHuyen: '',
          xaPhuong: wards.value.find(w => w.WardCode === addressForm.value.phuong)?.WardName || '',
          soNha: addressForm.value.chiTiet,
          diaChiChiTiet: addressForm.value.chiTiet,
          tenNguoiNhan: addressForm.value.tenNguoiNhan,
          sdt: addressForm.value.sdt,
          isDefault: addressForm.value.isDefault,
          trangThai: 1
        };
        console.log('Payload sent:', addressData);
        if (isEditingAddress.value) {
          await KhachHangService.updateAddress(selectedClient.value.id, addressForm.value.id, addressData);
          showNotification('success', 'Cập nhật địa chỉ thành công!');
        } else {
          await KhachHangService.addAddress(selectedClient.value.id, addressData);
          showNotification('success', 'Thêm địa chỉ thành công!');
        }
        await fetchAddresses(selectedClient.value.id);
        // Đảm bảo có dữ liệu provinces trước khi fetch khách hàng
        if (provinces.value.length === 0) {
          await loadProvinces();
        }
        await fetchKhachHangs();
        closeAddressForm();
      } catch (error) {
        showNotification('error', `Không thể ${isEditingAddress.value ? 'cập nhật' : 'thêm'} địa chỉ: ` + (error.response?.data?.message || error.message));
      } finally {
        loadingAddresses.value = false;
      }
    };

    const formatAddress = (address) => {
      if (!address) return 'N/A';
      const rawSoNha = address.soNha || address.diaChiChiTiet || '';
      const rawWard = address.xaPhuong || '';
      const rawDistrict = address.quanHuyen || '';
      const rawProvince = address.thanhPho || '';

      // Province display and type
      const { displayProvince, isCity } = getProvinceDisplay(rawProvince, address.provinceId);

      // Ward normalized with default based on province type
      const wardDefaultType = isCity ? 'Phường' : 'Xã';
      const ward = normalizeWardName(rawWard, wardDefaultType);

      // Province with prefix
      const province = displayProvince;

      // Optional: district (kept if present), otherwise omitted
      let district = '';
      if (rawDistrict && rawDistrict.trim()) {
        const ld = rawDistrict.trim().toLowerCase();
        if (ld.startsWith('quận ') || ld.startsWith('huyện ') || ld.startsWith('thị xã ') || ld.startsWith('q.') || ld.startsWith('h.')) {
          district = rawDistrict.trim();
        } else if (ld.includes('quận') || ld.startsWith('q')) {
          district = `Quận ${rawDistrict.trim()}`;
        } else if (ld.includes('thị xã')) {
          district = `Thị xã ${rawDistrict.trim()}`;
        } else {
          district = `Huyện ${rawDistrict.trim()}`;
        }
      }

      const addressLine = [rawSoNha && rawSoNha.trim(), ward, district, province]
        .filter(Boolean)
        .join(', ');

      const parts = [
        address.tenNguoiNhan ? `<strong>Tên người nhận:</strong> ${address.tenNguoiNhan}` : null,
        address.sdt ? `<strong>Số điện thoại:</strong> ${address.sdt}` : null,
        addressLine ? `<strong>Địa chỉ:</strong> ${addressLine}` : null
      ].filter(Boolean);
      return parts.join('<br>') || 'N/A';
    };

    const formatAddressClient = (address) => {
      if (!address) return 'N/A';
      
      const detail = address.soNha || address.diaChiChiTiet || '';
      const wardRaw = (address.xaPhuong || '').trim();
      const provinceRaw = (address.thanhPho || '').trim();

      // Province display and type
      const { displayProvince, isCity } = getProvinceDisplay(provinceRaw, address.provinceId);

      // Ward prefix default based on province type
      const wardDefaultType = isCity ? 'Phường' : 'Xã';
      const ward = normalizeWardName(wardRaw, wardDefaultType);

      const province = displayProvince;

      const parts = [detail && detail.trim(), ward, province].filter(Boolean);
      const result = parts.length ? parts.join(', ') : 'N/A';
      
      // Debug log để kiểm tra
      if (!province) {
        console.log(`formatAddressClient DEBUG:`, {
          address: address,
          provinceRaw: provinceRaw,
          provinceId: address.provinceId,
          provincesLength: provinces.value.length,
          result: result
        });
      }
      
      return result;
    };

    const formatDate = (dateString) => {
      console.log('formatDate called with dateString:', dateString, 'type:', typeof dateString);
      if (!dateString) return 'N/A';
      
      try {
        // Handle LocalDate format (YYYY-MM-DD)
        if (typeof dateString === 'string' && dateString.includes('-')) {
          const date = new Date(dateString);
          console.log('Parsed date from string:', date);
          return date.toLocaleDateString('vi-VN');
        }
        
        // Handle timestamp (fallback)
        const date = new Date(dateString);
        console.log('Parsed date from timestamp:', date);
        return date.toLocaleDateString('vi-VN');
      } catch (error) {
        console.error('Error formatting date:', error);
        return 'N/A';
      }
    };

    const formatStatus = (trangThai) => {
      return trangThai == 1 ? 'Hoạt động' : trangThai == 0 ? 'Ngừng hoạt động' : 'N/A';
    };

    const getStatusClass = (trangThai) => {
      return trangThai == 1 ? 'status-active' : trangThai == 0 ? 'status-inactive' : 'status-unknown';
    };

    const normalizeWardName = (input, defaultType = 'Xã') => {
      if (!input) return '';
      const trimmed = input.trim();
      if (!trimmed) return '';
      const lw = trimmed.toLowerCase();

      // Already has explicit prefix or abbreviations -> normalize to full prefix
      if (lw.startsWith('xã ')) return `Xã ${trimmed.slice(3).trim()}`;
      if (lw.startsWith('phường ')) return `Phường ${trimmed.slice(7).trim()}`;
      if (lw.startsWith('thị trấn ')) return `Thị trấn ${trimmed.slice(9).trim()}`;
      if (lw.startsWith('p.')) return `Phường ${trimmed.slice(2).trim()}`;
      if (lw.startsWith('p ')) return `Phường ${trimmed.slice(2).trim()}`;
      if (lw.startsWith('x.')) return `Xã ${trimmed.slice(2).trim()}`;
      if (lw.startsWith('x ')) return `Xã ${trimmed.slice(2).trim()}`;

      // Try infer from known keywords inside
      if (lw.includes('phường')) return `Phường ${trimmed.replace(/phường/ig, '').trim()}`;
      if (lw.includes('thị trấn')) return `Thị trấn ${trimmed.replace(/thị trấn/ig, '').trim()}`;
      if (lw.includes('xã')) return `Xã ${trimmed.replace(/xã/ig, '').trim()}`;

      // Unknown type -> use default from province context
      return `${defaultType} ${trimmed}`;
    };

    const getProvinceDisplay = (provinceRaw, provinceId) => {
      // Returns normalized province display with correct type and a flag
      const normalized = { displayProvince: '', provinceTypeWord: 'Tỉnh', isCity: false };
      const raw = (provinceRaw || '').trim();

      const findById = () => {
        if (!provinceId || provinces.value.length === 0) return null;
        let p = provinces.value.find(x => x.ProvinceID === provinceId);
        if (!p) p = provinces.value.find(x => x.ProvinceID == provinceId);
        if (!p) p = provinces.value.find(x => String(x.ProvinceID) === String(provinceId));
        return p || null;
      };

      // Prefer API data when possible
      let matched = findById();

      if (!matched && raw) {
        const target = raw.toLowerCase();
        matched = provinces.value.find(x => x.ProvinceName && x.ProvinceName.trim().toLowerCase() === target) || null;
      }

      if (matched) {
        const typeStr = String(matched.type || '').toLowerCase();
        const isCity = typeStr.includes('thành phố');
        const typeWord = isCity ? 'Thành phố' : 'Tỉnh';
        normalized.provinceTypeWord = typeWord;
        normalized.isCity = isCity;
        normalized.displayProvince = `${typeWord} ${matched.ProvinceName}`;
        return normalized;
      }

      // Fallback by heuristics when API data is not available
      if (raw) {
        const lower = raw.toLowerCase();
        const knownCities = ['hồ chí minh', 'hà nội', 'đà nẵng', 'cần thơ', 'hải phòng'];
        const isCity = lower.startsWith('thành phố') || lower.startsWith('tp.') || lower.startsWith('tp ') || knownCities.includes(lower);
        const typeWord = isCity ? 'Thành phố' : 'Tỉnh';
        normalized.provinceTypeWord = typeWord;
        normalized.isCity = isCity;
        // Avoid double prefix if already present
        if (lower.startsWith('tỉnh ') || lower.startsWith('thành phố') || lower.startsWith('tp.')) {
          normalized.displayProvince = raw;
        } else {
          normalized.displayProvince = `${typeWord} ${raw}`;
        }
        return normalized;
      }

      return normalized;
    };

    const parseAddressFromString = (addressString) => {
      if (!addressString) return { soNha: '', xaPhuong: '', quanHuyen: '', thanhPho: '' };
      
      // Chuẩn hóa chuỗi địa chỉ
      let cleanAddress = addressString.trim();
      
      // Debug log
      console.log('Parsing address:', cleanAddress);
      
      // Các pattern phổ biến cho địa chỉ Việt Nam
      const patterns = [
        // Pattern 1: "123 Đường ABC, Phường 1, Quận 1, TP.HCM"
        /^(.+?),\s*(Phường|Xã|Thị trấn)\s*(.+?),\s*(Quận|Huyện|Thị xã)\s*(.+?),\s*(TP\.|Thành phố|Tỉnh)\s*(.+)$/i,
        // Pattern 2: "123 Đường ABC, Quận 1, TP.HCM"
        /^(.+?),\s*(Quận|Huyện|Thị xã)\s*(.+?),\s*(TP\.|Thành phố|Tỉnh)\s*(.+)$/i,
        // Pattern 3: "123 Đường ABC, Quận 1, TP.HCM" (không có TP./Thành phố)
        /^(.+?),\s*(Quận|Huyện|Thị xã)\s*(.+?),\s*(.+)$/i,
        // Pattern 4: "Quận 1, TP.HCM"
        /^(Quận|Huyện|Thị xã)\s*(.+?),\s*(TP\.|Thành phố|Tỉnh)\s*(.+)$/i,
        // Pattern 5: "Phường 1, Quận 1, TP.HCM"
        /^(Phường|Xã|Thị trấn)\s*(.+?),\s*(Quận|Huyện|Thị xã)\s*(.+?),\s*(TP\.|Thành phố|Tỉnh)\s*(.+)$/i,
        // Pattern 6: "số 345, Xã Phú Mỹ, Huyện Phú Tân, Cà Mau" (format mới)
        /^(.+?),\s*(Xã|Phường|Thị trấn)\s*(.+?),\s*(Huyện|Quận|Thị xã)\s*(.+?),\s*(.+)$/i,
        // Pattern 7: "Xã Phú Mỹ, Huyện Phú Tân, Cà Mau" (không có số nhà)
        /^(Xã|Phường|Thị trấn)\s*(.+?),\s*(Huyện|Quận|Thị xã)\s*(.+?),\s*(.+)$/i
      ];
      
      for (let i = 0; i < patterns.length; i++) {
        const pattern = patterns[i];
        const match = cleanAddress.match(pattern);
        if (match) {
          console.log(`Pattern ${i + 1} matched:`, match);
          
          if (i === 0) { // Pattern 1: Có đầy đủ phường, quận, thành phố
            return {
              soNha: match[1].trim(),
              xaPhuong: match[2] + ' ' + match[3].trim(),
              quanHuyen: match[4] + ' ' + match[5].trim(),
              thanhPho: match[6] + ' ' + match[7].trim()
            };
          } else if (i === 1) { // Pattern 2: Có quận và thành phố
            return {
              soNha: match[1].trim(),
              xaPhuong: '',
              quanHuyen: match[2] + ' ' + match[3].trim(),
              thanhPho: match[4] + ' ' + match[5].trim()
            };
          } else if (i === 2) { // Pattern 3: Có quận nhưng không có TP./Thành phố
            return {
              soNha: match[1].trim(),
              xaPhuong: '',
              quanHuyen: match[2] + ' ' + match[3].trim(),
              thanhPho: match[4].trim()
            };
          } else if (i === 3) { // Pattern 4: Chỉ có quận và thành phố
            return {
              soNha: '',
              xaPhuong: '',
              quanHuyen: match[1] + ' ' + match[2].trim(),
              thanhPho: match[3] + ' ' + match[4].trim()
            };
          } else if (i === 4) { // Pattern 5: Có phường, quận, thành phố (không có số nhà)
            return {
              soNha: '',
              xaPhuong: match[1] + ' ' + match[2].trim(),
              quanHuyen: match[3] + ' ' + match[4].trim(),
              thanhPho: match[5] + ' ' + match[6].trim()
            };
          } else if (i === 5) { // Pattern 6: "số 345, Xã Phú Mỹ, Huyện Phú Tân, Cà Mau"
            return {
              soNha: match[1].trim(),
              xaPhuong: match[2] + ' ' + match[3].trim(),
              quanHuyen: match[4] + ' ' + match[5].trim(),
              thanhPho: match[6].trim()
            };
          } else if (i === 6) { // Pattern 7: "Xã Phú Mỹ, Huyện Phú Tân, Cà Mau" (không có số nhà)
            return {
              soNha: '',
              xaPhuong: match[1] + ' ' + match[2].trim(),
              quanHuyen: match[3] + ' ' + match[4].trim(),
              thanhPho: match[5].trim()
            };
          }
        }
      }
      
      // Nếu không match pattern nào, thử tách theo dấu phẩy
      console.log('No pattern matched, trying comma split...');
      const parts = cleanAddress.split(',').map(part => part.trim());
      console.log('Parts after split:', parts);
      
      if (parts.length >= 2) {
        const lastPart = parts[parts.length - 1];
        const secondLastPart = parts[parts.length - 2];
        
        // Kiểm tra xem phần cuối có phải là tỉnh/thành phố không
        if (lastPart.includes('TP.') || lastPart.includes('Thành phố') || lastPart.includes('Tỉnh') || 
            lastPart.includes('HCM') || lastPart.includes('Hà Nội') || lastPart.includes('Đà Nẵng')) {
          console.log('Last part looks like a province/city:', lastPart);
          return {
            soNha: parts.slice(0, -2).join(', '),
            xaPhuong: '',
            quanHuyen: secondLastPart,
            thanhPho: lastPart
          };
        }
      }
      
      // Fallback: trả về toàn bộ địa chỉ trong soNha
      console.log('Using fallback - putting entire address in soNha');
      return {
        soNha: cleanAddress,
        xaPhuong: '',
        quanHuyen: '',
        thanhPho: ''
      };
    };

    const onImageError = (event) => {
      event.target.src = '/default-avatar.png';
      console.error('Image load failed, switched to default:', event.target.src);
    };

    // Load provinces trước, sau đó mới load khách hàng để đảm bảo có dữ liệu tỉnh thành
    const initializeData = async () => {
      await loadProvinces();
      await fetchKhachHangs();
    };
    
    initializeData();
    router.afterEach((to, from) => {
      if (from.path === '/khach-hang/create' && to.path === '/profile/client') {
        handleClientCreated();
      }
    });

    // Test function để kiểm tra parseAddressFromString
    const testAddressParsing = () => {
      console.log('=== Testing Address Parsing ===');
      const testCases = [
        "123 Đường ABC, Quận 1, TP.HCM",
        "456 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM",
        "789 Lê Lợi, Quận 3, TP.HCM",
        "Quận 1, TP.HCM",
        "123 Đường ABC, Huyện Bình Chánh, TP.HCM",
        "123 Đường ABC, Phường 1, Quận 1, TP.HCM",
        "Phường 1, Quận 1, TP.HCM",
        "123 Đường ABC, Quận 1, HCM",
        "123 Đường ABC, Quận 1, Thành phố Hồ Chí Minh",
        // Test cases cho format mới
        "số 345, Xã Phú Mỹ, Huyện Phú Tân, Cà Mau",
        "Xã Phú Mỹ, Huyện Phú Tân, Cà Mau",
        "123 Đường ABC, Xã An Thới, Huyện Phú Quốc, Kiên Giang"
      ];
      
      testCases.forEach((testCase, index) => {
        console.log(`Test case ${index + 1}: "${testCase}"`);
        const result = parseAddressFromString(testCase);
        console.log('Result:', result);
        console.log('---');
      });
    };
    
    // Kích hoạt test function khi component mount
    testAddressParsing();

    return {
      khachHangs,
      currentPage,
      pageSize,
      totalPages,
      totalElements,
      isLastPage,
      loading,
      loadingAddresses,
      loadingProvinces,
      loadingDistricts,
      loadingWards,
      searchTimeout,
      showAddressModal,
      showAddressForm,
      showDeleteConfirmModal,
      isEditingAddress,
      selectedClient,
      selectedAddressId,
      addressToDelete,
      addresses,
      provinces,
      districts,
      wards,
      addressForm,
      addressErrors,
      excelInput,
      notification,
      searchFilters,
      showNotification,
      hideNotification,
      fetchKhachHangs,
      buildSearchParams,
      debounceSearch,
      performSearch,
      resetFilters,
      exportExcel,
      triggerImportExcel,
      importExcel,
      downloadTemplate,
      handleClientCreated,
      goToPage,
      openAddressModal,
      closeAddressModal,
      fetchAddresses,
      setDefaultAddress,
      openAddAddressForm,
      openEditAddressForm,
      closeAddressForm,
      openDeleteConfirmModal,
      closeDeleteConfirmModal,
      confirmDeleteAddress,
      loadProvinces,
      loadDistricts,
      loadWards,
      onProvinceChange,
      onDistrictChange,
      validateAddressField,
      validateAddressForm,
      submitAddressForm,
      formatAddress,
      formatAddressClient,
      formatDate,
      formatStatus,
      getStatusClass,
      parseAddressFromString,
      testAddressParsing,
      onImageError
    };
  },
  beforeUnmount() {
    clearTimeout(this.searchTimeout);
  }
};
</script>

<style scoped>
.container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

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

.filter-group input,
.filter-group select {
  padding: 8px 12px;
  border: 2px solid #e1e5e9;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
  width: 100%;
}

.filter-group input:focus,
.filter-group select:focus {
  outline: none;
  border-color: #667eea;
}

.button-group {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-top: 10px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background: #5a6268;
}

.btn-warning {
  background: #ffc107;
  color: #212529;
}

.btn-warning:hover:not(:disabled) {
  background: #e0a800;
}

.btn-danger {
  background: #dc3545;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background: #c82333;
}

.btn-info {
  background: #17a2b8;
  color: white;
}

.btn-info:hover:not(:disabled) {
  background: #138496;
}

.client-list {
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

.loading-container {
  text-align: center;
  padding: 60px 20px;
  color: #6c757d;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
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

.employee-photo {
  width: 90px;
  height: 90px;
  border-radius: 30%;
  background: #e9ecef;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.employee-photo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.employee-photo i {
  color: #6c757d;
  font-size: 18px;
}

.address-column {
  min-width: 250px;
}

.address-cell {
  white-space: normal;
  word-wrap: break-word;
}

.status-column {
  min-width: 120px;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.status-active {
  background: #d4edda;
  color: #155724;
}

.status-inactive {
  background: #f8d7da;
  color: #721c24;
}

.login-provider-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.login-provider-badge.local {
  background: #d1ecf1;
  color: #0c5460;
}

.login-provider-badge.google {
  background: #fff3cd;
  color: #856404;
}

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

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Pagination styles giống như trong sell.vue */
.pagination {
  text-align: right;
  font-size: 14px;
  color: #718096;
  padding: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.pagination .page-item .page-link {
  border-radius: 0.375rem;
  margin: 0 0.125rem;
  color: #718096;
  border: 1px solid #e2e8f0;
  background-color: #ffffff;
  padding: 0.5rem 0.75rem;
  transition: all 0.15s ease-in-out;
  cursor: pointer;
}

.pagination .page-item.active .page-link {
  background-color: #3182ce;
  border-color: #3182ce;
  color: #ffffff;
}

.pagination .page-item.disabled .page-link {
  color: #a0aec0;
  pointer-events: none;
  background-color: #f7fafc;
  border-color: #e2e8f0;
  cursor: not-allowed;
}

/* Responsive pagination */
@media (max-width: 768px) {
  .pagination-wrapper {
    justify-content: center;
  }
  
  .pagination {
    font-size: 12px;
    padding: 8px;
  }
  
  .pagination .page-item .page-link {
    padding: 0.375rem 0.5rem;
    margin: 0 0.0625rem;
  }
}

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
  max-width: 600px;
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

.address-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
}

.address-item {
  padding: 10px;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.address-info {
  flex: 1;
}

.default-label {
  color: #2196f3;
  font-size: 12px;
  font-weight: 500;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  width: 100%;
}

.radio-custom {
  width: 18px;
  height: 18px;
  border: 2px solid #ddd;
  border-radius: 50%;
  position: relative;
  transition: border-color 0.3s ease;
}

.radio-label input[type="radio"]:checked+.radio-custom {
  border-color: #2196f3;
}

.radio-label input[type="radio"]:checked+.radio-custom::after {
  content: '';
  width: 10px;
  height: 10px;
  background: #2196f3;
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.address-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.required::after {
  content: ' *';
  color: #f44336;
  font-weight: bold;
}

.form-input,
.form-select {
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s ease;
  background: white;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #2196f3;
}

.form-input.error,
.form-select.error {
  border-color: #f44336;
}

.form-input::placeholder {
  color: #999;
}

.form-select:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.error-message {
  color: #f44336;
  font-size: 12px;
  margin-top: 4px;
}

.import-guide {
  margin-top: 15px;
  padding: 12px 16px;
  background-color: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  border-left: 4px solid #17a2b8;
}

.import-guide small {
  color: #6c757d;
  line-height: 1.4;
}

.import-guide i {
  color: #17a2b8;
  margin-right: 8px;
}

@media (max-width: 768px) {
  .container {
    padding: 10px;
  }

  .filter-row {
    flex-direction: row;
    gap: 10px;
  }

  .filter-group {
    min-width: 150px;
  }

  .filter-group input,
  .filter-group select {
    font-size: 12px;
    padding: 6px 8px;
  }

  .list-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }

  .table-container {
    font-size: 12px;
  }

  th,
  td {
    padding: 8px 4px;
  }

  .address-column {
    min-width: 150px;
  }

  .status-column {
    min-width: 100px;
  }
}
</style>
