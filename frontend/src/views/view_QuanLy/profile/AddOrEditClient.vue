<template>
  <div class="add-client-container">
    <!-- Notifications -->
    <div v-if="notification.show" :class="['notification', notification.type]">
      <i :class="notification.type === 'success' ? 'fa-solid fa-check-circle' : 'fa-solid fa-exclamation-triangle'"></i>
      {{ notification.message }}
      <button @click="hideNotification" class="close-btn">&times;</button>
    </div>

    <div class="header">
      <h1 class="title">{{ isEditMode ? 'CHỈNH SỬA KHÁCH HÀNG' : 'THÊM KHÁCH HÀNG' }}</h1>
    </div>

    <div class="form-container">
      <div class="left-section">
        <div class="avatar-section">
          <div class="avatar-label">Ảnh đại diện</div>
          <div class="avatar-upload" :class="{ 'has-error': errors.hinhAnh }">
            <label for="avatarInput" class="upload-placeholder">
              <img v-if="avatarPreview" :src="avatarPreview" alt="Avatar Preview" class="avatar-preview"
                @error="handleImageError" />
              <div v-else class="upload-content">
                <div class="upload-icon">+</div>
                <div class="upload-text">Upload</div>
              </div>
            </label>
            <input id="avatarInput" type="file" accept="image/*" @change="handleFileChange" ref="fileInput"
              style="display: none" />
          </div>
          <div v-if="errors.hinhAnh" class="error-message">{{ errors.hinhAnh }}</div>
          <button v-if="avatarPreview" @click="removeImage" class="btn-remove-image">Xóa ảnh</button>
        </div>
      </div>

      <div class="right-section">
        <div class="section-header">
          <h2>Thông tin khách hàng</h2>
          <!-- Nút Gửi mail đổi mật khẩu ở header -->
          <div v-if="isEditMode && form.loginProvider === 'LOCAL'" class="header-action">
            <button type="button" class="btn btn-primary" @click="sendResetPasswordEmail" :disabled="loadingEmail">
              <i v-if="loadingEmail" class="fa-solid fa-spinner fa-spin"></i>
              <i v-else class="fa-solid fa-envelope"></i>
              GỬI MAIL ĐỔI MẬT KHẨU
            </button>
          </div>
        </div>

        <form @submit.prevent="submitForm" class="form-grid">

          <!-- Thông tin cơ bản -->
          <div class="form-row">
            <div class="form-group">
              <label class="required">Tên khách hàng</label>
              <input v-model="form.tenKhachHang" type="text" placeholder="Nguyen Van A" class="form-input"
                :class="{ 'error': errors.tenKhachHang }" @blur="validateField('tenKhachHang')" />
              <div v-if="errors.tenKhachHang" class="error-message">{{ errors.tenKhachHang }}</div>
            </div>
            <div class="form-group">
              <label class="required">Email</label>
              <input v-model="form.email" type="email" placeholder="example@email.com" class="form-input"
                :class="{ 'error': errors.email }" @blur="validateField('email')" />
              <div v-if="errors.email" class="error-message">{{ errors.email }}</div>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="required">Số điện thoại</label>
              <input v-model="form.sdt" type="tel" placeholder="0123456789" class="form-input"
                :class="{ 'error': errors.sdt }" @blur="validateField('sdt')" maxlength="10" />
              <div v-if="errors.sdt" class="error-message">{{ errors.sdt }}</div>
            </div>
            <div class="form-group">
              <label class="required">Giới tính</label>
              <div class="radio-group" :class="{ 'error': errors.gioiTinh }">
                <label class="radio-label">
                  <input v-model="form.gioiTinh" type="radio" name="gender" value="1" />
                  <span class="radio-custom"></span>
                  Nam
                </label>
                <label class="radio-label">
                  <input v-model="form.gioiTinh" type="radio" name="gender" value="0" />
                  <span class="radio-custom"></span>
                  Nữ
                </label>
              </div>
              <div v-if="errors.gioiTinh" class="error-message">{{ errors.gioiTinh }}</div>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Ngày sinh</label>
              <input v-model="form.ngaySinh" type="date" class="form-input"
                :class="{ 'error': errors.ngaySinh }" @blur="validateField('ngaySinh')" />
              <div v-if="errors.ngaySinh" class="error-message">{{ errors.ngaySinh }}</div>
            </div>
            <div class="form-group">
              <label class="required">Trạng thái</label>
              <select v-model="form.trangThai" class="form-select" :class="{ 'error': errors.trangThai }">
                <option value="1">Hoạt động</option>
                <option value="0">Ngừng hoạt động</option>
              </select>
              <div v-if="errors.trangThai" class="error-message">{{ errors.trangThai }}</div>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="required">Loại đăng nhập</label>
              <select v-model="form.loginProvider" class="form-select" :class="{ 'error': errors.loginProvider }">
                <option value="LOCAL">Local</option>
                <option value="GOOGLE">Google</option>
              </select>
              <div v-if="errors.loginProvider" class="error-message">{{ errors.loginProvider }}</div>
              <small class="form-text">
                Chọn "Local" để tạo mật khẩu tự động, chọn "Google" để đăng nhập qua Google
              </small>
            </div>
            <div class="form-group">
              <label class="required">Tỉnh/Thành phố</label>
              <select v-model="form.diaChi.tinh" class="form-select" :class="{ 'error': errors.tinh }"
                @change="onProvinceChange" :disabled="loadingProvinces">
                <option value="">--Chọn Tỉnh/Thành phố--</option>
                <option v-for="province in provinces" :key="province.ProvinceID" :value="province.ProvinceID">
                  {{ province.ProvinceName }}
                </option>
              </select>
              <div v-if="errors.tinh" class="error-message">{{ errors.tinh }}</div>
            </div>
          </div>

          <!-- Thông tin địa chỉ -->
          <div class="form-row">
            <div class="form-group">
              <label class="required">Xã/Phường</label>
              <select v-model="form.diaChi.phuong" class="form-select" :class="{ 'error': errors.phuong }"
                :disabled="!form.diaChi.tinh || loadingWards">
                <option value="">--Chọn Xã/Phường--</option>
                <option v-for="ward in wards" :key="ward.WardCode" :value="ward.WardCode">
                  {{ ward.WardName }}
                </option>
              </select>
              <div v-if="errors.phuong" class="error-message">{{ errors.phuong }}</div>
            </div>
            <div class="form-group">
              <label class="required">Số nhà/Ngõ/Đường</label>
              <input v-model="form.diaChi.chiTiet" type="text" placeholder="123 Đường XYZ, Ngõ ABC" class="form-input"
                :class="{ 'error': errors.chiTiet }" @blur="validateField('chiTiet')" />
              <div v-if="errors.chiTiet" class="error-message">{{ errors.chiTiet }}</div>
            </div>
          </div>

          <!-- Nút hành động -->
          <div class="form-row">
            <div class="form-group full-width">
              <div class="button-group">
                <button type="button" @click="cancelForm" class="btn btn-secondary">
                  <i class="fa-solid fa-times"></i>
                  Hủy
                </button>
                <button type="submit" class="btn btn-primary" :disabled="loading">
                  <i v-if="loading" class="fa-solid fa-spinner fa-spin"></i>
                  {{ loading ? 'Đang xử lý...' : (isEditMode ? 'Cập nhật' : 'Thêm mới') }}
                </button>
              </div>
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import KhachHangService from '@/services/RoleQuanLy/KhachHangService';
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

export default {
  name: 'AddOrEditClient',
  setup() {
    const route = useRoute();
    const router = useRouter();

    const isEditMode = ref(!!route.params.id);
    const loading = ref(false);
    const loadingEmail = ref(false);
    const loadingProvinces = ref(false);
    const loadingDistricts = ref(false);
    const loadingWards = ref(false);
    const avatarFile = ref(null);
    const avatarPreview = ref(null);
    const provinces = ref([]);
    const districts = ref([]);
    const wards = ref([]);
    const errors = ref({});
    const notification = ref({ show: false, type: 'success', message: '' });

    const form = ref({
      tenKhachHang: '',
      email: '',
      sdt: '',
      gioiTinh: '',
      ngaySinh: '',
      trangThai: '1',
      loginProvider: 'LOCAL',
      diaChi: { tinh: '', quan: '', phuong: '', chiTiet: '' }
    });

    const validationRules = {
      tenKhachHang: [
        { required: true, message: 'Vui lòng nhập tên khách hàng' },
        { minLength: 2, message: 'Tên phải có ít nhất 2 ký tự' },
        { maxLength: 50, message: 'Tên không được vượt quá 50 ký tự' },
        { pattern: /^[A-Za-zÀ-ỹ\s]+$/, message: 'Tên chỉ được chứa chữ cái và khoảng trắng' }
      ],
      email: [
        { required: true, message: 'Vui lòng nhập email' },
        { type: 'email', message: 'Email không đúng định dạng' }
      ],
      sdt: [
        { required: true, message: 'Vui lòng nhập số điện thoại' },
        { pattern: /^0\d{9}$/, message: 'Số điện thoại phải bắt đầu bằng 0 và có đúng 10 chữ số' }
      ],
      gioiTinh: [{ required: true, message: 'Vui lòng chọn giới tính' }],
      ngaySinh: [
        { 
          custom: (value) => {
            if (value) {
              const selectedDate = new Date(value);
              const today = new Date();
              if (selectedDate > today) {
                return 'Ngày sinh không thể lớn hơn ngày hiện tại';
              }
              const age = today.getFullYear() - selectedDate.getFullYear();
              if (age > 120) {
                return 'Ngày sinh không hợp lệ';
              }
            }
            return null;
          }
        }
      ],
      trangThai: [{ required: true, message: 'Vui lòng chọn trạng thái' }],
      loginProvider: [{ required: true, message: 'Vui lòng chọn loại đăng nhập' }],
      tinh: [{ required: false }],
      quan: [{ required: false }],
      phuong: [{ required: false }],
      chiTiet: [{ required: false }],
      hinhAnh: [
        { maxSize: 5 * 1024 * 1024, message: 'Ảnh không được vượt quá 5MB' },
        { types: ['image/jpeg', 'image/png'], message: 'Chỉ hỗ trợ định dạng JPEG hoặc PNG' }
      ]
    };


    const loadProvinces = async () => {
      try {
        loadingProvinces.value = true;
        const response = await KhachHangService.getProvinces();
        provinces.value = response.data.data || [];
      } catch (error) {
        console.error('Không thể tải danh sách tỉnh/thành phố:', error);
      } finally {
        loadingProvinces.value = false;
      }
    };


    const loadWards = async (districtId) => {
      try {
        loadingWards.value = true;
        const response = await KhachHangService.getWards(districtId);
        wards.value = response.data.data || [];
        form.value.diaChi.phuong = '';
      } catch (error) {
        console.error('Không thể tải danh sách xã/phường:', error);
      } finally {
        loadingWards.value = false;
      }
    };

    const onProvinceChange = async () => {
      if (form.value.diaChi.tinh) {
        validateField('tinh');
        // Load wards theo province (API mới)
        const response = await KhachHangService.getWards(form.value.diaChi.tinh);
        wards.value = response.data.data || [];
        form.value.diaChi.phuong = '';
      } else {
        wards.value = [];
        form.value.diaChi.phuong = '';
      }
    };


    const handleFileChange = (event) => {
      const file = event.target.files[0];
      if (!file) return;
      if (!file.type.startsWith('image/')) {
        errors.value.hinhAnh = 'Vui lòng chọn file ảnh';
        return;
      }
      if (file.size > 5 * 1024 * 1024) {
        errors.value.hinhAnh = 'Kích thước file không được vượt quá 5MB';
        return;
      }

      if (avatarPreview.value?.startsWith('blob:')) {
        URL.revokeObjectURL(avatarPreview.value);
      }
      avatarFile.value = file;
      avatarPreview.value = URL.createObjectURL(file);
      delete errors.value.hinhAnh;
    };

    const removeImage = () => {
      if (confirm('Bạn có chắc muốn xóa ảnh đại diện?')) {
        if (avatarPreview.value?.startsWith('blob:')) {
          URL.revokeObjectURL(avatarPreview.value);
        }
        avatarFile.value = null;
        avatarPreview.value = null;
        if (form.value.$refs?.fileInput) form.value.$refs.fileInput.value = '';
      }
    };

    const handleImageError = () => {
      console.warn('Không thể tải ảnh đại diện. Vui lòng kiểm tra file ảnh.');
    };

    const validateField = (fieldName) => {
      const rules = validationRules[fieldName];
      if (!rules) return true;
      const value = fieldName === 'hinhAnh' ? avatarFile.value : (fieldName in form.value.diaChi ? form.value.diaChi[fieldName] : form.value[fieldName]);

      for (const rule of rules) {
        if (rule.required && (!value || value.toString().trim() === '')) {
          errors.value[fieldName] = rule.message;
          return false;
        }
        if (rule.minLength && value && value.length < rule.minLength) {
          errors.value[fieldName] = rule.message;
          return false;
        }
        if (rule.maxLength && value && value.length > rule.maxLength) {
          errors.value[fieldName] = rule.message;
          return false;
        }
        if (rule.pattern && value && !rule.pattern.test(value)) {
          errors.value[fieldName] = rule.message;
          return false;
        }
        if (rule.type === 'email' && value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
          errors.value[fieldName] = rule.message;
          return false;
        }
        if (rule.maxSize && value && value.size > rule.maxSize) {
          errors.value[fieldName] = rule.message;
          return false;
        }
        if (rule.types && value && !rule.types.includes(value.type)) {
          errors.value[fieldName] = rule.message;
          return false;
        }
        if (rule.custom && value) {
          const customError = rule.custom(value);
          if (customError) {
            errors.value[fieldName] = customError;
            return false;
          }
        }
      }
      delete errors.value[fieldName];
      return true;
    };

    const validateForm = () => {
      errors.value = {};
      const hasAddressInput = form.value.diaChi.tinh || form.value.diaChi.phuong || form.value.diaChi.chiTiet;
      const requiredFields = ['tenKhachHang', 'email', 'sdt', 'gioiTinh', 'trangThai'];
      const addressFields = ['tinh', 'phuong', 'chiTiet'];

      let isValid = requiredFields.every(fieldName => validateField(fieldName));

      if (hasAddressInput) {
        isValid = isValid && addressFields.every(fieldName => {
          validationRules[fieldName] = [
            { required: true, message: `Vui lòng nhập ${fieldName === 'chiTiet' ? 'địa chỉ chi tiết' : fieldName}` },
            ...(fieldName === 'chiTiet' ? [{ minLength: 5, message: 'Địa chỉ phải có ít nhất 5 ký tự' }] : [])
          ];
          return validateField(fieldName);
        });
      }

      return isValid;
    };

    const findAddressCode = async (type, name) => {
      const list = type === 'province' ? provinces.value : type === 'district' ? districts.value : wards.value;
      const item = list.find(item => item[type === 'province' ? 'ProvinceName' : type === 'district' ? 'DistrictName' : 'WardName'].toLowerCase() === name.toLowerCase());
      return item ? item[type === 'province' ? 'ProvinceID' : type === 'district' ? 'DistrictID' : 'WardCode'] : '';
    };

    const loadClientData = async (id) => {
      try {
        loading.value = true;
        const response = await KhachHangService.getKhachHangById(id);
        const client = response.data || response;
        let addressParts = { tinh: '', quan: '', phuong: '', chiTiet: '' };
        if (client.diaChis && client.diaChis.length > 0) {
          // Ưu tiên lấy địa chỉ mặc định, nếu không có thì lấy địa chỉ đầu tiên
          const defaultAddress = client.diaChis.find(addr => addr.isDefault) || client.diaChis[0];
          // Ưu tiên dùng mã ID nếu backend có lưu
          const provinceId = defaultAddress.provinceId || '';
          const wardCode = defaultAddress.wardCode || '';

          addressParts.chiTiet = defaultAddress.soNha || defaultAddress.diaChiChiTiet || '';

          await loadProvinces();
          if (provinceId) {
            addressParts.tinh = provinceId;
          } else if (defaultAddress.thanhPho) {
            addressParts.tinh = await findAddressCode('province', defaultAddress.thanhPho);
          }

          // Load wards dựa trên provinceId (API mới không cần district)
          if (addressParts.tinh) {
            await loadWards(addressParts.tinh);
          }

          if (wardCode) {
            addressParts.phuong = wardCode;
          } else if (defaultAddress.xaPhuong && addressParts.tinh) {
            addressParts.phuong = await findAddressCode('ward', defaultAddress.xaPhuong);
          }
        }

        form.value = {
          tenKhachHang: client.tenKhachHang || '',
          email: client.email || '',
          sdt: client.sdt || '',
          gioiTinh: client.gioiTinh?.toString() || '',
          ngaySinh: client.ngaySinh ? new Date(client.ngaySinh).toISOString().split('T')[0] : '',
          trangThai: client.trangThai?.toString() || '1',
          loginProvider: client.loginProvider || 'LOCAL',
          diaChi: addressParts
        };

        if (client.hinhAnh && client.hinhAnh.trim()) {
          avatarPreview.value = `http://localhost:8080/uploads/nhan_vien/${client.hinhAnh}`;
        }
      } catch (error) {
        console.error('Không thể tải thông tin khách hàng:', error);
        showNotification('error', 'Không thể tải thông tin khách hàng. Vui lòng thử lại.');
        setTimeout(() => router.push('/profile/client'), 1500);
      } finally {
        loading.value = false;
      }
    };

    const prepareFormData = () => {
      const formData = new FormData();
      
      // Lấy tên tỉnh/thành phố và xã/phường từ danh sách đã load (chuẩn hóa tiền tố)
      const provinceObj = provinces.value.find(p => p.ProvinceID === form.value.diaChi.tinh);
      const provinceName = provinceObj?.ProvinceName || '';
      const { displayProvince, isCity } = getProvinceDisplay(provinceName, form.value.diaChi.tinh);
      const wardRawName = wards.value.find(w => w.WardCode === form.value.diaChi.phuong)?.WardName || '';
      const wardName = normalizeWardName(wardRawName, isCity ? 'Phường' : 'Xã');
      
      const clientData = {
        tenKhachHang: form.value.tenKhachHang?.trim() || '',
        email: form.value.email?.trim() || '',
        sdt: form.value.sdt?.trim() || '',
        gioiTinh: form.value.gioiTinh ? parseInt(form.value.gioiTinh) : 0,
        ngaySinh: form.value.ngaySinh || null,
        trangThai: form.value.trangThai ? parseInt(form.value.trangThai) : 1,
        loginProvider: form.value.loginProvider || 'LOCAL',
        diaChis: form.value.diaChi.chiTiet ? [{
          provinceId: form.value.diaChi.tinh || null,
          districtId: null,
          wardCode: form.value.diaChi.phuong || null,
          thanhPho: displayProvince,
          quanHuyen: '',
          xaPhuong: wardName,
          soNha: form.value.diaChi.chiTiet || '',
          diaChiChiTiet: form.value.diaChi.chiTiet || '',
          trangThai: 1,
          isDefault: true
        }] : []
      };
      console.log('Payload sent:', clientData);
      formData.append('data', new Blob([JSON.stringify(clientData)], { type: 'application/json' }));
      if (avatarFile.value) formData.append('hinhAnh', avatarFile.value);
      return formData;
    };

    const submitForm = async () => {
      if (!validateForm()) {
        showNotification('error', 'Vui lòng kiểm tra lại thông tin đã nhập');
        return;
      }

      const action = isEditMode.value ? 'cập nhật' : 'thêm mới';
      if (!confirm(`Bạn có chắc muốn ${action} thông tin khách hàng này?`)) {
        return;
      }

      try {
        loading.value = true;
        
        // Đảm bảo provinces và wards đã được load trước khi prepareFormData
        if (form.value.diaChi.tinh && provinces.value.length === 0) {
          await loadProvinces();
        }
        if (form.value.diaChi.phuong && wards.value.length === 0) {
          await loadWards(form.value.diaChi.tinh);
        }
        
        const formData = prepareFormData();
        let response;
        if (isEditMode.value) {
          const originalResponse = await KhachHangService.getKhachHangById(route.params.id);
          const originalClient = originalResponse.data || originalResponse;
          if (originalClient.sdt === form.value.sdt.trim()) {
            console.log('Số điện thoại không thay đổi, bỏ qua kiểm tra tính duy nhất');
          }
          response = await KhachHangService.updateKhachHang(route.params.id, formData);
        } else {
          response = await KhachHangService.createKhachHang(formData);
        }

        if (response && (response.status === 200 || response.status === 201)) {
          if (response.data && response.data.success === true) {
            showNotification('success', isEditMode.value ? 'Cập nhật thông tin khách hàng thành công!' : 'Thêm khách hàng thành công!');
            setTimeout(() => router.push("/profile/client"), 1500);
          } else if (response.data && response.data.success === false) {
            throw new Error(response.data.message || 'Cập nhật thất bại');
          } else {
            throw new Error('Phản hồi không hợp lệ từ server');
          }
        }
      } catch (error) {
        let errorMessage = 'Có lỗi xảy ra khi lưu thông tin khách hàng';
        if (error.response) {
          const { status, data } = error.response;
          errorMessage = status === 400 ? data.message || 'Dữ liệu đầu vào không hợp lệ'
            : status === 409 ? data.message || 'Số điện thoại hoặc email đã tồn tại'
              : status === 404 ? 'Không tìm thấy khách hàng cần cập nhật'
                : `Lỗi server: ${data.message || error.message}`;
        } else {
          errorMessage += `: ${error.message}`;
        }
        showNotification('error', errorMessage);
      } finally {
        loading.value = false;
      }
    };

    // Helpers copied from Client.vue for consistent formatting
    const normalizeWardName = (input, defaultType = 'Xã') => {
      if (!input) return '';
      const trimmed = input.trim();
      if (!trimmed) return '';
      const lw = trimmed.toLowerCase();
      if (lw.startsWith('xã ')) return `Xã ${trimmed.slice(3).trim()}`;
      if (lw.startsWith('phường ')) return `Phường ${trimmed.slice(7).trim()}`;
      if (lw.startsWith('thị trấn ')) return `Thị trấn ${trimmed.slice(9).trim()}`;
      if (lw.startsWith('p.')) return `Phường ${trimmed.slice(2).trim()}`;
      if (lw.startsWith('p ')) return `Phường ${trimmed.slice(2).trim()}`;
      if (lw.startsWith('x.')) return `Xã ${trimmed.slice(2).trim()}`;
      if (lw.startsWith('x ')) return `Xã ${trimmed.slice(2).trim()}`;
      if (lw.includes('phường')) return `Phường ${trimmed.replace(/phường/ig, '').trim()}`;
      if (lw.includes('thị trấn')) return `Thị trấn ${trimmed.replace(/thị trấn/ig, '').trim()}`;
      if (lw.includes('xã')) return `Xã ${trimmed.replace(/xã/ig, '').trim()}`;
      return `${defaultType} ${trimmed}`;
    };

    const getProvinceDisplay = (provinceRaw, provinceId) => {
      const normalized = { displayProvince: '', provinceTypeWord: 'Tỉnh', isCity: false };
      const raw = (provinceRaw || '').trim();

      const findById = () => {
        if (!provinceId || provinces.value.length === 0) return null;
        let p = provinces.value.find(x => x.ProvinceID === provinceId);
        if (!p) p = provinces.value.find(x => x.ProvinceID == provinceId);
        if (!p) p = provinces.value.find(x => String(x.ProvinceID) === String(provinceId));
        return p || null;
      };

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

      if (raw) {
        const lower = raw.toLowerCase();
        const knownCities = ['hồ chí minh', 'hà nội', 'đà nẵng', 'cần thơ', 'hải phòng'];
        const isCity = lower.startsWith('thành phố') || lower.startsWith('tp.') || lower.startsWith('tp ') || knownCities.includes(lower);
        const typeWord = isCity ? 'Thành phố' : 'Tỉnh';
        normalized.provinceTypeWord = typeWord;
        normalized.isCity = isCity;
        if (lower.startsWith('tỉnh ') || lower.startsWith('thành phố') || lower.startsWith('tp.')) {
          normalized.displayProvince = raw;
        } else {
          normalized.displayProvince = `${typeWord} ${raw}`;
        }
        return normalized;
      }

      return normalized;
    };

    const sendResetPasswordEmail = async () => {
      if (!confirm("Bạn có chắc muốn gửi email đặt lại mật khẩu?")) return;
      try {
        loadingEmail.value = true;
        const customerId = route.params.id || form.value.id;
        const response = await KhachHangService.resetPassword(customerId);
        showNotification("success", response.data.message || "Email đặt lại mật khẩu đã được gửi!");
      } catch (error) {
        let errorMessage = "Không thể gửi email đặt lại mật khẩu";
        if (error.code === 'ECONNABORTED') {
          errorMessage = "Yêu cầu gửi email mất quá lâu. Vui lòng kiểm tra kết nối hoặc thử lại sau.";
        } else if (error.response) {
          const { status, data } = error.response;
          errorMessage = status === 400 ? data.message || "Dữ liệu không hợp lệ"
            : status === 404 ? "Không tìm thấy khách hàng"
              : `Lỗi server: ${data.message || error.message}`;
        } else {
          errorMessage += `: ${error.message}`;
        }
        showNotification("error", errorMessage);
      } finally {
        loadingEmail.value = false;
      }
    };

    const cancelForm = () => {
      if (confirm('Bạn có chắc muốn hủy?')) {
        router.push('/profile/client');
      }
    };

    const showNotification = (type, message) => {
      notification.value = { show: true, type, message };
      setTimeout(() => hideNotification(), 5000);
    };

    const hideNotification = () => {
      notification.value.show = false;
    };

    watch(
      () => route.params.id,
      (newId) => {
        isEditMode.value = !!newId;
        if (newId) loadClientData(newId);
      },
      { immediate: true }
    );

    loadProvinces();

    return {
      isEditMode,
      loading,
      loadingEmail,
      loadingProvinces,
      loadingDistricts,
      loadingWards,
      avatarFile,
      avatarPreview,
      form,
      provinces,
      districts,
      wards,
      errors,
      notification,
      validationRules,
      onProvinceChange,
      handleFileChange,
      removeImage,
      handleImageError,
      validateField,
      submitForm,
      sendResetPasswordEmail,
      cancelForm,
      showNotification,
      hideNotification
    };
  },
  beforeUnmount() {
    if (this.avatarPreview?.startsWith('blob:')) {
      URL.revokeObjectURL(this.avatarPreview);
    }
  }
};
</script>

<style scoped>
.add-client-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: #f5f5f5;
  min-height: 100vh;
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
  text-align: center;
  margin-bottom: 30px;
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0;
  letter-spacing: 1px;
}

.form-container {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 30px;
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
}

.left-section {
  display: flex;
  flex-direction: column;
}

.avatar-section {
  text-align: center;
}

.avatar-label {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

.avatar-upload {
  width: 200px;
  height: 250px;
  border: 2px dashed #ddd;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  cursor: pointer;
  transition: border-color 0.3s ease;
  margin: 0 auto;
  position: relative;
  overflow: hidden;
}

.avatar-upload:hover {
  border-color: #2196f3;
}

.avatar-upload.has-error {
  border-color: #f44336;
}

.upload-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.avatar-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
}

.upload-content {
  text-align: center;
  color: #666;
}

.upload-icon {
  font-size: 40px;
  color: #999;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 14px;
  font-weight: 500;
}

.btn-remove-image {
  margin-top: 10px;
  padding: 6px 12px;
  background: #f44336;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.btn-remove-image:hover {
  background: #d32f2f;
}

.right-section {
  flex: 1;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}

.section-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.header-action {
  display: flex;
  align-items: center;
}

.form-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
  max-width: 50%;
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

.form-select {
  cursor: pointer;
  color: #666;
}

.form-select:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.radio-group {
  display: flex;
  gap: 20px;
  margin-top: 5px;
}

.radio-group.error .radio-custom {
  border-color: #f44336;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
}

.radio-label input[type='radio'] {
  display: none;
}

.radio-custom {
  width: 18px;
  height: 18px;
  border: 2px solid #ddd;
  border-radius: 50%;
  position: relative;
  transition: border-color 0.3s ease;
}

.radio-label input[type='radio']:checked+.radio-custom {
  border-color: #2196f3;
}

.radio-label input[type='radio']:checked+.radio-custom::after {
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

.error-message {
  color: #f44336;
  font-size: 12px;
  margin-top: 4px;
}

.form-text {
  color: #6c757d;
  font-size: 12px;
  margin-top: 4px;
}

.button-group {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 20px;
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

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: flex-start;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .form-container {
    grid-template-columns: 1fr;
    gap: 20px;
    padding: 20px;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .form-group.full-width {
    max-width: 100%;
  }

  .avatar-upload {
    width: 150px;
    height: 180px;
  }

  .section-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .button-group {
    flex-direction: column;
  }

  .btn-primary,
  .btn-secondary {
    width: 100%;
  }
}
</style>