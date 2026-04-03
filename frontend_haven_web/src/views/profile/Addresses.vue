<template>
  <div class="addresses">
    <!-- Header -->
    <div class="section-header">
      <h2>Quản lý địa chỉ</h2>
      <p>Thêm, chỉnh sửa và quản lý địa chỉ giao hàng</p>
    </div>

    <!-- Notification -->
    <div v-if="notification.show" :class="['notification', notification.type]">
      <i :class="notification.type === 'success' ? 'bi bi-check-circle' : 'bi bi-exclamation-triangle'"></i>
      {{ notification.message }}
      <button @click="hideNotification" class="close-btn">&times;</button>
    </div>

    <!-- Address List -->
    <div class="address-list-container">
      <div class="list-header">
        <h3>Danh sách địa chỉ</h3>
        <button @click="openAddAddressForm" class="btn btn-primary">
          <i class="bi bi-plus"></i>
          Thêm địa chỉ
        </button>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading-container">
        <div class="spinner"></div>
        <p>Đang tải danh sách địa chỉ...</p>
      </div>

      <!-- Empty State -->
      <div v-else-if="addresses.length === 0" class="empty-state">
        <i class="bi bi-geo-alt"></i>
        <h3>Chưa có địa chỉ nào</h3>
        <p>Thêm địa chỉ mới để thuận tiện cho việc giao hàng</p>
        <button @click="openAddAddressForm" class="btn btn-primary">
          <i class="bi bi-plus"></i>
          Thêm địa chỉ đầu tiên
        </button>
      </div>

      <!-- Address List -->
      <div v-else class="address-grid">
        <div v-for="address in addresses" :key="address.id" class="address-card">
          <div class="address-header">
            <div class="address-info">
              <h4>{{ address.tenNguoiNhan || 'N/A' }}</h4>
              <p class="phone">{{ address.sdt || 'N/A' }}</p>
            </div>
            <div class="address-actions">
              <button @click="setDefaultAddress(address.id)" 
                      :class="['btn', 'btn-sm', address.isDefault ? 'btn-success' : 'btn-outline']"
                      :disabled="address.isDefault || loading">
                <i class="bi bi-check-circle"></i>
                {{ address.isDefault ? 'Mặc định' : 'Đặt mặc định' }}
              </button>
              <button @click="openEditAddressForm(address)" class="btn btn-sm btn-warning" title="Chỉnh sửa">
                <i class="bi bi-pencil"></i>
              </button>
              <button @click="openDeleteConfirmModal(address.id)" class="btn btn-sm btn-danger" title="Xóa">
                <i class="bi bi-trash"></i>
              </button>
            </div>
          </div>
          <div class="address-content">
            <p class="address-text">{{ formatAddress(address) }}</p>
            <div v-if="address.isDefault" class="default-badge">
              <i class="bi bi-star-fill"></i>
              Địa chỉ mặc định
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Address Form Modal -->
    <div v-if="showAddressForm" class="modal-overlay" @click="closeAddressForm">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ isEditingAddress ? 'Chỉnh sửa địa chỉ' : 'Thêm địa chỉ' }}</h3>
          <button @click="closeAddressForm" class="btn btn-secondary">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitAddressForm" class="address-form">
            <div class="form-group">
              <label class="required">Tên người nhận</label>
              <input v-model="addressForm.tenNguoiNhan" type="text" placeholder="Nhập tên người nhận" 
                     class="form-input" :class="{ 'error': addressErrors.tenNguoiNhan }" 
                     @blur="validateAddressField('tenNguoiNhan')" />
              <div v-if="addressErrors.tenNguoiNhan" class="error-message">{{ addressErrors.tenNguoiNhan }}</div>
            </div>
            
            <div class="form-group">
              <label class="required">Số điện thoại</label>
              <input v-model="addressForm.sdt" type="text" placeholder="Nhập số điện thoại" 
                     class="form-input" maxlength="10" :class="{ 'error': addressErrors.sdt }" 
                     @blur="validateAddressField('sdt')" />
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
            
            <div class="form-group">
              <label class="required">Quận/Huyện</label>
              <select v-model="addressForm.quan" class="form-select" :class="{ 'error': addressErrors.quan }"
                      @change="onDistrictChange" :disabled="!addressForm.tinh || loadingDistricts">
                <option value="">--Chọn Quận/Huyện--</option>
                <option v-for="district in districts" :key="district.DistrictID" :value="district.DistrictID">
                  {{ district.DistrictName }}
                </option>
              </select>
              <div v-if="addressErrors.quan" class="error-message">{{ addressErrors.quan }}</div>
            </div>
            
            <div class="form-group">
              <label class="required">Xã/Phường</label>
              <select v-model="addressForm.phuong" class="form-select" :class="{ 'error': addressErrors.phuong }"
                      :disabled="!addressForm.quan || loadingWards">
                <option value="">--Chọn Xã/Phường--</option>
                <option v-for="ward in wards" :key="ward.WardCode" :value="ward.WardCode">
                  {{ ward.WardName }}
                </option>
              </select>
              <div v-if="addressErrors.phuong" class="error-message">{{ addressErrors.phuong }}</div>
            </div>
            
            <div class="form-group">
              <label class="required">Số nhà/Ngõ/Đường</label>
              <input v-model="addressForm.chiTiet" type="text" placeholder="123 Đường ABC, Ngõ XYZ" 
                     class="form-input" :class="{ 'error': addressErrors.chiTiet }" 
                     @blur="validateAddressField('chiTiet')" />
              <div v-if="addressErrors.chiTiet" class="error-message">{{ addressErrors.chiTiet }}</div>
            </div>
            
            <div class="form-group">
              <label>
                <input type="checkbox" v-model="addressForm.isDefault" />
                Đặt làm địa chỉ mặc định
              </label>
            </div>
            
            <div class="form-actions">
              <button type="button" @click="closeAddressForm" class="btn btn-secondary">Hủy</button>
              <button type="submit" class="btn btn-primary" :disabled="loading">
                <i v-if="loading" class="bi bi-arrow-clockwise spin"></i>
                {{ isEditingAddress ? 'Cập nhật' : 'Thêm' }}
              </button>
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
          <button @click="closeDeleteConfirmModal" class="btn btn-secondary">×</button>
        </div>
        <div class="modal-body">
          <p>Bạn có chắc chắn muốn xóa địa chỉ này không?</p>
          <div class="form-actions">
            <button class="btn btn-secondary" @click="closeDeleteConfirmModal">Hủy</button>
            <button class="btn btn-danger" @click="confirmDeleteAddress" :disabled="loading">
              <i v-if="loading" class="bi bi-arrow-clockwise spin"></i>
              Xóa
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { getCurrentUserInfo } from '@/services/profile.js';
import client from '@/utils/api.js';

// Reactive data
const loading = ref(false);
const loadingProvinces = ref(false);
const loadingDistricts = ref(false);
const loadingWards = ref(false);
const addresses = ref([]);
const provinces = ref([]);
const districts = ref([]);
const wards = ref([]);
const userId = ref(null);

const showAddressForm = ref(false);
const showDeleteConfirmModal = ref(false);
const isEditingAddress = ref(false);
const addressToDelete = ref(null);

const addressForm = reactive({
  id: null,
  tinh: '',
  quan: '',
  phuong: '',
  chiTiet: '',
  tenNguoiNhan: '',
  sdt: '',
  isDefault: false
});

const addressErrors = reactive({});
const notification = ref({ show: false, type: 'success', message: '' });

// Methods
const showNotification = (type, message) => {
  notification.value = { show: true, type, message };
  setTimeout(() => hideNotification(), 3000);
};

const hideNotification = () => {
  notification.value.show = false;
};

const loadUserInfo = async () => {
  try {
    const response = await getCurrentUserInfo();
    if (response.success && response.data) {
      userId.value = response.data.id;
      await fetchAddresses();
    }
  } catch (error) {
    console.error('Error loading user info:', error);
    showNotification('error', 'Không thể tải thông tin người dùng');
  }
};

const fetchAddresses = async () => {
  if (!userId.value) return;
  
  try {
    loading.value = true;
    const response = await client.get(`/api/customers/${userId.value}/addresses`);
    addresses.value = response.data || [];
  } catch (error) {
    console.error('Error loading addresses:', error);
    showNotification('error', 'Không thể tải danh sách địa chỉ');
    addresses.value = [];
  } finally {
    loading.value = false;
  }
};

const setDefaultAddress = async (addressId) => {
  if (!userId.value) return;
  
  try {
    loading.value = true;
    await client.patch(`/api/customers/${userId.value}/addresses/${addressId}/set-default`);
    showNotification('success', 'Đặt địa chỉ mặc định thành công!');
    await fetchAddresses();
  } catch (error) {
    console.error('Error setting default address:', error);
    showNotification('error', 'Không thể đặt địa chỉ mặc định');
  } finally {
    loading.value = false;
  }
};

const openAddAddressForm = () => {
  addressForm.id = null;
  addressForm.tinh = '';
  addressForm.quan = '';
  addressForm.phuong = '';
  addressForm.chiTiet = '';
  addressForm.tenNguoiNhan = '';
  addressForm.sdt = '';
  addressForm.isDefault = false;
  isEditingAddress.value = false;
  showAddressForm.value = true;
  loadProvinces();
};

const openEditAddressForm = async (address) => {
  try {
    addressForm.id = address.id;
    addressForm.chiTiet = address.soNha || address.diaChiChiTiet || '';
    addressForm.tenNguoiNhan = address.tenNguoiNhan || '';
    addressForm.sdt = address.sdt || '';
    addressForm.isDefault = address.isDefault || false;
    isEditingAddress.value = true;

    await loadProvinces();
    
    if (address.thanhPho) {
      const province = provinces.value.find(p =>
        p.ProvinceName.trim().toLowerCase() === address.thanhPho.trim().toLowerCase()
      );
      addressForm.tinh = province?.ProvinceID || '';
    }

    if (addressForm.tinh) {
      await loadDistricts(addressForm.tinh);
      if (address.quanHuyen) {
        const district = districts.value.find(d =>
          d.DistrictName.trim().toLowerCase() === address.quanHuyen.trim().toLowerCase()
        );
        addressForm.quan = district?.DistrictID || '';
      }
    }

    if (addressForm.quan) {
      await loadWards(addressForm.quan);
      if (address.xaPhuong) {
        const ward = wards.value.find(w =>
          w.WardName.trim().toLowerCase() === address.xaPhuong.trim().toLowerCase()
        );
        addressForm.phuong = ward?.WardCode || '';
      }
    }

    showAddressForm.value = true;
  } catch (error) {
    console.error('Error loading address form:', error);
    showNotification('error', 'Không thể tải dữ liệu để chỉnh sửa địa chỉ');
  }
};

const closeAddressForm = () => {
  showAddressForm.value = false;
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
  if (!addressToDelete.value || !userId.value) return;
  
  try {
    loading.value = true;
    await client.delete(`/api/customers/${userId.value}/addresses/${addressToDelete.value}`);
    showNotification('success', 'Xóa địa chỉ thành công!');
    await fetchAddresses();
    closeDeleteConfirmModal();
  } catch (error) {
    console.error('Error deleting address:', error);
    showNotification('error', 'Không thể xóa địa chỉ');
  } finally {
    loading.value = false;
  }
};

const loadProvinces = async () => {
  try {
    provinces.value = [];
    districts.value = [];
    wards.value = [];
    loadingProvinces.value = true;
    const response = await client.get('/api/v1/ghn/provinces');
    provinces.value = response.data.data || [];
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
    const response = await client.get(`/api/v1/ghn/districts/${provinceId}`);
    districts.value = response.data.data || [];
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
    const response = await client.get(`/api/v1/ghn/wards/${districtId}`);
    wards.value = response.data.data || [];
  } catch (error) {
    console.error('Error loading wards:', error);
    showNotification('error', 'Không thể tải danh sách xã/phường');
  } finally {
    loadingWards.value = false;
  }

  
};

const onProvinceChange = async () => {
  if (addressForm.tinh) {
    await loadDistricts(addressForm.tinh);
    validateAddressField('tinh');
    addressForm.quan = '';
    addressForm.phuong = '';
  }
};

const onDistrictChange = async () => {
  if (addressForm.quan) {
    await loadWards(addressForm.quan);
    validateAddressField('quan');
    addressForm.phuong = '';
  }
};

const validateAddressField = (fieldName) => {
  const rules = {
    tinh: [{ required: true, message: 'Vui lòng chọn tỉnh/thành phố' }],
    quan: [{ required: true, message: 'Vui lòng chọn quận/huyện' }],
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
  
  const value = addressForm[fieldName];
  for (const rule of rules[fieldName] || []) {
    if (rule.required && (!value || value.toString().trim() === '')) {
      addressErrors[fieldName] = rule.message;
      return false;
    }
    if (rule.minLength && value && value.length < rule.minLength) {
      addressErrors[fieldName] = rule.message;
      return false;
    }
    if (rule.pattern && value && !rule.pattern.test(value)) {
      addressErrors[fieldName] = rule.message;
      return false;
    }
  }
  delete addressErrors[fieldName];
  return true;
};

const validateAddressForm = () => {
  addressErrors.value = {};
  return ['tinh', 'quan', 'phuong', 'chiTiet', 'tenNguoiNhan', 'sdt'].every(field => validateAddressField(field));
};

const submitAddressForm = async () => {
  if (!validateAddressForm()) {
    showNotification('error', 'Vui lòng kiểm tra lại thông tin địa chỉ');
    return;
  }
  
  try {
    loading.value = true;
    const addressData = {
      provinceId: addressForm.tinh,
      districtId: addressForm.quan,
      wardCode: addressForm.phuong,
      thanhPho: provinces.value.find(p => p.ProvinceID === addressForm.tinh)?.ProvinceName || '',
      quanHuyen: districts.value.find(d => d.DistrictID === addressForm.quan)?.DistrictName || '',
      xaPhuong: wards.value.find(w => w.WardCode === addressForm.phuong)?.WardName || '',
      soNha: addressForm.chiTiet,
      diaChiChiTiet: addressForm.chiTiet,
      tenNguoiNhan: addressForm.tenNguoiNhan,
      sdt: addressForm.sdt,
      isDefault: addressForm.isDefault,
      trangThai: 1
    };

    if (isEditingAddress.value) {
      await client.put(`/api/customers/${userId.value}/addresses/${addressForm.id}`, addressData);
      showNotification('success', 'Cập nhật địa chỉ thành công!');
    } else {
      await client.post(`/api/customers/${userId.value}/addresses`, addressData);
      showNotification('success', 'Thêm địa chỉ thành công!');
    }
    
    await fetchAddresses();
    closeAddressForm();
  } catch (error) {
    console.error('Error submitting address:', error);
    showNotification('error', `Không thể ${isEditingAddress.value ? 'cập nhật' : 'thêm'} địa chỉ`);
  } finally {
    loading.value = false;
  }
};

const formatAddress = (address) => {
  if (!address) return 'N/A';
  const parts = [
    address.soNha || address.diaChiChiTiet,
    address.xaPhuong,
    address.quanHuyen,
    address.thanhPho
  ].filter(part => part && part.trim());
  return parts.join(', ') || 'N/A';
};

onMounted(() => {
  loadUserInfo();
});
</script>

<style scoped>
.addresses {
  max-width: 100%;
}

.section-header {
  margin-bottom: 30px;
}

.section-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.section-header p {
  color: #6c757d;
  margin: 0;
}

.notification {
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
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
  font-size: 18px;
  cursor: pointer;
  margin-left: auto;
  color: inherit;
}

.address-list-container {
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

.list-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
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
  border-top: 4px solid #4ba27b;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
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

.address-grid {
  padding: 20px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.address-card {
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;
}

.address-card:hover {
  border-color: #4ba27b;
  box-shadow: 0 4px 12px rgba(75, 162, 123, 0.1);
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.address-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 5px 0;
}

.address-info .phone {
  font-size: 14px;
  color: #6c757d;
  margin: 0;
}

.address-actions {
  display: flex;
  gap: 8px;
}

.address-content {
  position: relative;
}

.address-text {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  margin: 0 0 10px 0;
}

.default-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 8px;
  background: #d4edda;
  color: #155724;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}

.btn-primary {
  background: #4ba27b;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #3d8b6a;
  transform: translateY(-1px);
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #5a6268;
}

.btn-success {
  background: #28a745;
  color: white;
}

.btn-outline {
  background: transparent;
  color: #4ba27b;
  border: 1px solid #4ba27b;
}

.btn-outline:hover:not(:disabled) {
  background: #4ba27b;
  color: white;
}

.btn-warning {
  background: #ffc107;
  color: #212529;
}

.btn-warning:hover {
  background: #e0a800;
}

.btn-danger {
  background: #dc3545;
  color: white;
}

.btn-danger:hover {
  background: #c82333;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
  color: #dc3545;
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
  border-color: #4ba27b;
}

.form-input.error,
.form-select.error {
  border-color: #dc3545;
}

.form-select:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.error-message {
  color: #dc3545;
  font-size: 12px;
  margin-top: 4px;
}

.form-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

@media (max-width: 768px) {
  .address-grid {
    grid-template-columns: 1fr;
    padding: 15px;
  }
  
  .address-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .address-actions {
    justify-content: flex-start;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .btn {
    width: 100%;
    justify-content: center;
  }
}
</style> 