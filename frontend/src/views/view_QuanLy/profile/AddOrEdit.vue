<template>
  <div class="add-employee-container">
    <!-- Notifications -->
    <div v-if="notification.show" :class="['notification', notification.type]">
      <i :class="notification.type === 'success' ? 'fa-solid fa-check-circle' : 'fa-solid fa-exclamation-triangle'"></i>
      {{ notification.message }}
      <button @click="hideNotification" class="close-btn">&times;</button>
    </div>


    
    <div class="header">
      <h1 class="title">{{ isEditMode ? 'CHỈNH SỬA NHÂN VIÊN' : 'THÊM NHÂN VIÊN' }}</h1>
    </div>

    <div class="form-container">
      <div class="left-section">
        <div class="avatar-section">
          <div class="avatar-label">Ảnh đại diện</div>
          <div class="avatar-upload" :class="{ 'has-error': errors.avatar }">
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
          <div v-if="errors.avatar" class="error-message">{{ errors.avatar }}</div>
          <button v-if="avatarPreview" @click="removeImage" class="btn-remove-image">Xóa ảnh</button>
        </div>
      </div>

      <div class="right-section">
        <div class="section-header">
          <h2>Thông tin nhân viên</h2>
          <div class="action-buttons">
            <button v-if="isEditMode && form.loginProvider === 'LOCAL'" class="btn btn-primary"
              @click="sendResetPasswordEmail" :disabled="loadingEmail">
              <i v-if="loadingEmail" class="fa-solid fa-spinner fa-spin"></i>
              <i v-else class="fa-solid fa-envelope"></i>
              GỬI MAIL ĐỔI MẬT KHẨU
            </button>
          </div>
        </div>

        <form @submit.prevent="submitForm" class="form-grid">
          <div class="form-row">
            <div class="form-group">
              <label class="required">Tên nhân viên</label>
              <input v-model="form.tenNhanVien" type="text" placeholder="Nguyen Van A" class="form-input"
                :class="{ 'error': errors.tenNhanVien }" @blur="validateField('tenNhanVien')" />
              <div v-if="errors.tenNhanVien" class="error-message">{{ errors.tenNhanVien }}</div>
            </div>
            <div class="form-group">
              <label class="required">Ngày sinh</label>
              <input v-model="form.ngaySinh" type="date" class="form-input" :class="{ 'error': errors.ngaySinh }"
                @blur="validateField('ngaySinh')" />
              <div v-if="errors.ngaySinh" class="error-message">{{ errors.ngaySinh }}</div>
            </div>
          </div>

          <div class="form-row">
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

            <div class="form-group">
              <label class="required">Hình thức đăng nhập</label>
              <div class="radio-group" :class="{ 'error': errors.loginProvider }">
                <label class="radio-label">
                  <input v-model="form.loginProvider" type="radio" name="loginProvider" value="LOCAL" />
                  <span class="radio-custom"></span>
                  Local
                </label>
                <label class="radio-label">
                  <input v-model="form.loginProvider" type="radio" name="loginProvider" value="GOOGLE" />
                  <span class="radio-custom"></span>
                  Google
                </label>
              </div>
              <div v-if="errors.loginProvider" class="error-message">{{ errors.loginProvider }}</div>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="required">Căn cước công dân</label>
              <input v-model="form.cccd" type="text" placeholder="012345678901" class="form-input"
                :class="{ 'error': errors.cccd }" @blur="validateField('cccd')" maxlength="12" />
              <div v-if="errors.cccd" class="error-message">{{ errors.cccd }}</div>
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
              <label class="required">Trạng thái</label>
              <select v-model="form.trangThai" class="form-select">
                <option value="1">Kích hoạt</option>
                <option value="0">Vô hiệu hóa</option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="required">Tỉnh/Thành phố</label>
              <select v-model="form.diaChi.tinh" class="form-select" :class="{ 'error': errors.tinh }"
                @change="onProvinceChange" :disabled="loadingProvinces">
                <option value="">--Chọn Tỉnh/Thành phố--</option>
                <option v-for="province in provinces" :key="province.code" :value="province.code">
                  {{ province.name }}
                </option>
              </select>
              <div v-if="errors.tinh" class="error-message">{{ errors.tinh }}</div>
            </div>
            <div class="form-group">
              <label class="required">Xã/Phường</label>
              <select v-model="form.diaChi.phuong" class="form-select" :class="{ 'error': errors.phuong }"
                :disabled="!form.diaChi.tinh || loadingWards">
                <option value="">--Chọn Xã/Phường--</option>
                <option v-for="ward in wards" :key="ward.code" :value="ward.code">
                  {{ ward.name }}
                </option>
              </select>
              <div v-if="errors.phuong" class="error-message">{{ errors.phuong }}</div>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="required">Số nhà/Ngõ/Đường</label>
              <input v-model="form.diaChi.chiTiet" type="text" placeholder="123 Đường ABC, Ngõ XYZ" class="form-input"
                :class="{ 'error': errors.chiTiet }" @blur="validateField('chiTiet')" />
              <div v-if="errors.chiTiet" class="error-message">{{ errors.chiTiet }}</div>
            </div>
          </div>

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
import { ref, computed, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import NhanVienService from "@/services/RoleQuanLy/NhanVienService";
import { formatAddressWithTypes } from "@/utils/addressFormatter";

export default {
  name: "AddOrEdit",
  props: {
    employeeId: {
      type: [String, Number],
      default: null,
    },
  },
  setup() {
    const route = useRoute();
    const router = useRouter();

    const isEditMode = ref(!!route.params.id);
    const loading = ref(false);
    const loadingEmail = ref(false);
    const loadingProvinces = ref(false);
    const loadingWards = ref(false);
    const avatarFile = ref(null);
    const avatarPreview = ref(null);
    const provinces = ref([]);
    const wards = ref([]);
    const errors = ref({});
    const notification = ref({ show: false, type: "success", message: "" });

    const form = ref({
      tenNhanVien: "",
      ngaySinh: "",
      cccd: "",
      gioiTinh: "",
      email: "",
      sdt: "",
      diaChi: { tinh: "", phuong: "", chiTiet: "" },
      trangThai: "1",
      loginProvider: "LOCAL",
    });

    const validationRules = {
      tenNhanVien: [
        { required: true, message: "Vui lòng nhập tên nhân viên" },
        { minLength: 2, message: "Tên phải có ít nhất 2 ký tự" },
        { maxLength: 50, message: "Tên không được vượt quá 50 ký tự" },
        { pattern: /^[A-Za-zÀ-ỹ\s]+$/, message: "Tên chỉ được chứa chữ cái và khoảng trắng" },
      ],
      ngaySinh: [
        { required: true, message: "Vui lòng chọn ngày sinh" },
        { type: "date", message: "Ngày sinh không hợp lệ" },
      ],
      cccd: [
        { required: true, message: "Vui lòng nhập số CCCD" },
        { pattern: /^\d{12}$/, message: "CCCD phải có đúng 12 số" },
      ],
      gioiTinh: [{ required: true, message: "Vui lòng chọn giới tính" }],
      email: [
        { required: true, message: "Vui lòng nhập email" },
        { type: "email", message: "Email không đúng định dạng" },
      ],
      sdt: [
        { required: true, message: "Vui lòng nhập số điện thoại" },
        { pattern: /^(0[3|5|9])+([0-9]{8})$/, message: "Số điện thoại không hợp lệ" },
      ],
      tinh: [{ required: true, message: "Vui lòng chọn tỉnh/thành phố" }],
      phuong: [{ required: true, message: "Vui lòng chọn xã/phường" }],
      chiTiet: [
        { required: true, message: "Vui lòng nhập địa chỉ chi tiết" },
        { minLength: 5, message: "Địa chỉ phải có ít nhất 5 ký tự" },
      ],
      loginProvider: [{ required: true, message: "Vui lòng chọn hình thức đăng nhập" }],
    };


    const fullAddress = computed(() => {
      return formatAddressWithTypes(form.value.diaChi, wards.value, provinces.value);
    });

    const loadProvinces = async () => {
      try {
        loadingProvinces.value = true;
        // Fetch all pages of provinces
        const fetchAllPages = async (baseUrl, extraParams = {}) => {
          const results = [];
          const limit = 100;
          let page = 1;
          for (let i = 0; i < 100; i++) {
            const url = new URL(baseUrl);
            url.search = new URLSearchParams({ ...extraParams, limit: String(limit), page: String(page) }).toString();
            const response = await fetch(url.toString());
            const data = await response.json();
            const items = Array.isArray(data)
              ? data
              : (Array.isArray(data.data) ? data.data
              : Array.isArray(data.items) ? data.items
              : Array.isArray(data.results) ? data.results
              : Array.isArray(data.records) ? data.records
              : Array.isArray(data.provinces) ? data.provinces
              : []);
            results.push(...items);
            if (items.length < limit) break;
            page += 1;
          }
          return results;
        };

        provinces.value = await fetchAllPages("https://tinhthanhpho.com/api/v1/new-provinces");
      } catch (error) {
        showNotification("error", "Không thể tải danh sách tỉnh/thành phố");
      } finally {
        loadingProvinces.value = false;
      }
    };

    const loadWards = async (provinceCode) => {
      try {
        loadingWards.value = true;
        // Fetch all pages of wards by province
        const fetchAllPages = async (baseUrl, extraParams = {}) => {
          const results = [];
          const limit = 100;
          let page = 1;
          for (let i = 0; i < 100; i++) {
            const url = new URL(baseUrl);
            url.search = new URLSearchParams({ ...extraParams, limit: String(limit), page: String(page) }).toString();
            const response = await fetch(url.toString());
            const data = await response.json();
            const items = Array.isArray(data)
              ? data
              : (Array.isArray(data.data) ? data.data
              : Array.isArray(data.items) ? data.items
              : Array.isArray(data.results) ? data.results
              : Array.isArray(data.records) ? data.records
              : Array.isArray(data.wards) ? data.wards
              : []);
            results.push(...items);
            if (items.length < limit) break;
            page += 1;
          }
          return results;
        };

        wards.value = await fetchAllPages(`https://tinhthanhpho.com/api/v1/new-provinces/${provinceCode}/wards`);
        form.value.diaChi.phuong = "";
      } catch (error) {
        showNotification("error", "Không thể tải danh sách xã/phường");
      } finally {
        loadingWards.value = false;
      }
    };

    const onProvinceChange = async () => {
      if (form.value.diaChi.tinh) {
        await loadWards(form.value.diaChi.tinh);
        validateField("tinh");
        form.value.diaChi.phuong = "";
      }
    };

    const handleFileChange = (event) => {
      const file = event.target.files[0];
      if (!file) return;
      if (!file.type.startsWith("image/")) {
        errors.value.avatar = "Vui lòng chọn file ảnh";
        return;
      }
      if (file.size > 5 * 1024 * 1024) {
        errors.value.avatar = "Kích thước file không được vượt quá 5MB";
        return;
      }

      if (avatarPreview.value?.startsWith("blob:")) {
        URL.revokeObjectURL(avatarPreview.value);
      }
      avatarFile.value = file;
      avatarPreview.value = URL.createObjectURL(file);
      delete errors.value.avatar;
    };

    const removeImage = () => {
      if (avatarPreview.value?.startsWith("blob:")) {
        URL.revokeObjectURL(avatarPreview.value);
      }
      avatarFile.value = null;
      avatarPreview.value = null;
      if (form.value.$refs?.fileInput) form.value.$refs.fileInput.value = "";
    };

    const handleImageError = () => {
      console.error("Failed to load image:", avatarPreview.value);
      showNotification("warning", "Không thể tải ảnh đại diện. Vui lòng kiểm tra file ảnh.");
    };

    const validateField = (fieldName) => {
      const rules = validationRules[fieldName];
      if (!rules) return true;
      const value = fieldName in form.value.diaChi ? form.value.diaChi[fieldName] : form.value[fieldName];

      for (const rule of rules) {
        if (rule.required && (!value || value.toString().trim() === "")) {
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
        if (rule.type === "email" && value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
          errors.value[fieldName] = rule.message;
          return false;
        }
        if (rule.type === "date" && value) {
          const today = new Date();
          const birthDate = new Date(value);
          const age = today.getFullYear() - birthDate.getFullYear();
          if (birthDate > today) {
            errors.value[fieldName] = "Ngày sinh không thể lớn hơn ngày hiện tại";
            return false;
          }
          if (age < 18) {
            errors.value[fieldName] = "Nhân viên phải từ 18 tuổi trở lên";
            return false;
          }
          if (age > 65) {
            errors.value[fieldName] = "Nhân viên không được quá 65 tuổi";
            return false;
          }
        }
      }
      delete errors.value[fieldName];
      return true;
    };

    const validateForm = () => {
      errors.value = {};
      return Object.keys(validationRules).every((fieldName) => validateField(fieldName));
    };
    
    const loadEmployeeData = async (id) => {
      try {
        loading.value = true;
        const employeeId = id || route.params.id;
        if (!employeeId) throw new Error("Không tìm thấy ID nhân viên");

        // Đảm bảo danh sách tỉnh đã được load trước
        if (provinces.value.length === 0) {
          console.log("Loading provinces first...");
          await loadProvinces();
        }

        const response = await NhanVienService.getNhanVienById(employeeId);
        const employee = response.data?.data || response.data || response;
        if (!employee) throw new Error("Không tìm thấy thông tin nhân viên");
        console.log("Employee data:", employee);

        let addressParts = { tinh: "", phuong: "", chiTiet: "" };
        if (employee.diaChi && typeof employee.diaChi === "string") {
          const parts = employee.diaChi.split(",").map((part) => part.trim());
          console.log("Parsing address parts:", parts);
          
          if (parts.length >= 3) {
            // Format: "chi tiết, xã/phường, tỉnh"
            addressParts = { 
              chiTiet: parts[0], 
              phuong: parts[parts.length - 2], 
              tinh: parts[parts.length - 1] 
            };
          } else if (parts.length === 2) {
            // Format: "chi tiết, tỉnh" (không có xã/phường)
            addressParts = { 
              chiTiet: parts[0], 
              phuong: "", 
              tinh: parts[1] 
            };
          } else {
            addressParts = { chiTiet: employee.diaChi };
          }
        }

        console.log("Initial address parts:", addressParts);
        console.log("Available provinces:", provinces.value.map(p => ({ name: p.name, code: p.code })));

        // Tìm mã tỉnh từ tên tỉnh
        if (addressParts.tinh && provinces.value.length > 0) {
          console.log("Looking for province:", addressParts.tinh);
          
          const province = provinces.value.find(p => {
            const provinceName = p.name.toLowerCase();
            const searchName = addressParts.tinh.toLowerCase();
            
            // Loại bỏ các prefix để so sánh
            const cleanProvinceName = provinceName
              .replace(/^(tỉnh|thành phố|tp\.?)\s*/i, '')
              .trim();
            const cleanSearchName = searchName
              .replace(/^(tỉnh|thành phố|tp\.?)\s*/i, '')
              .trim();
            
            const exactMatch = cleanProvinceName === cleanSearchName;
            const nameMatch = provinceName === searchName;
            const containsMatch = provinceName.includes(searchName) || searchName.includes(cleanProvinceName);
            
            console.log(`Comparing: "${cleanProvinceName}" vs "${cleanSearchName}" - exact: ${exactMatch}, name: ${nameMatch}, contains: ${containsMatch}`);
            
            return exactMatch || nameMatch || containsMatch;
          });
          
          if (province) {
            console.log("Found province:", province.name, "with code:", province.code);
            addressParts.tinh = province.code;
            // Load danh sách xã/phường cho tỉnh này
            await loadWards(province.code);
          } else {
            console.log("Province not found for:", addressParts.tinh);
            console.log("Available province names:", provinces.value.map(p => p.name));
          }
        }

        // Tìm mã xã/phường từ tên xã/phường (sau khi đã load wards)
        if (addressParts.phuong && wards.value.length > 0) {
          console.log("Looking for ward:", addressParts.phuong);
          console.log("Available wards:", wards.value.map(w => ({ name: w.name, code: w.code })));
          
          const ward = wards.value.find(w => {
            const wardName = w.name.toLowerCase();
            const searchName = addressParts.phuong.toLowerCase();
            
            // Loại bỏ các prefix để so sánh
            const cleanWardName = wardName
              .replace(/^(phường|xã|thị trấn|p\.?|x\.?)\s*/i, '')
              .trim();
            const cleanSearchName = searchName
              .replace(/^(phường|xã|thị trấn|p\.?|x\.?)\s*/i, '')
              .trim();
            
            const exactMatch = cleanWardName === cleanSearchName;
            const nameMatch = wardName === searchName;
            const containsMatch = wardName.includes(searchName) || searchName.includes(cleanWardName);
            
            console.log(`Comparing ward: "${cleanWardName}" vs "${cleanSearchName}" - exact: ${exactMatch}, name: ${nameMatch}, contains: ${containsMatch}`);
            
            return exactMatch || nameMatch || containsMatch;
          });
          
          if (ward) {
            console.log("Found ward:", ward.name, "with code:", ward.code);
            addressParts.phuong = ward.code;
          } else {
            console.log("Ward not found for:", addressParts.phuong);
            console.log("Available ward names:", wards.value.map(w => w.name));
          }
        }

        form.value = {
          tenNhanVien: employee.tenNhanVien || "",
          ngaySinh: formatDateForInput(employee.ngaySinh),
          cccd: employee.cccd || "",
          gioiTinh: (employee.gioiTinh ?? employee.gender)?.toString() || "",
          email: employee.email || "",
          sdt: employee.sdt || "",
          diaChi: addressParts,
          trangThai: (employee.trangThai ?? employee.status ?? "1").toString(),
          loginProvider: employee.loginProvider || "LOCAL",
        };

        if (employee.hinhAnh && employee.hinhAnh.trim()) {
          const imageUrl = `http://localhost:8080/uploads/nhan_vien/${employee.hinhAnh}`;
          console.log("Attempting to load image:", imageUrl);
          avatarPreview.value = imageUrl;
        } else {
          console.log("No valid hinhAnh found in employee data:", employee.hinhAnh);
          avatarPreview.value = null;
        }
      } catch (error) {
        showNotification("error", `Không thể tải thông tin nhân viên: ${error.message}`);
        setTimeout(() => router.push("/profile/staff"), 1500);
      } finally {
        loading.value = false;
      }
    };

    const formatDateForInput = (dateString) => {
      if (!dateString) return "";
      try {
        const date = new Date(dateString.includes("/") ? dateString.split("/").reverse().join("-") : dateString);
        return isNaN(date.getTime()) ? "" : date.toISOString().split("T")[0];
      } catch {
        return "";
      }
    };

    const prepareFormData = () => {
      const formData = new FormData();
      const employeeData = {
        tenNhanVien: form.value.tenNhanVien.trim(),
        ngaySinh: form.value.ngaySinh,
        cccd: form.value.cccd.trim(),
        gioiTinh: parseInt(form.value.gioiTinh),
        email: form.value.email.trim(),
        sdt: form.value.sdt.trim(),
        diaChi: fullAddress.value,
        trangThai: parseInt(form.value.trangThai),
        loginProvider: form.value.loginProvider,
        chucVu: "NHANVIEN", // Fixed chucVu to NHANVIEN
      };
      Object.entries(employeeData).forEach(([key, value]) => {
        if (value !== null && value !== undefined && value !== "") {
          formData.append(key, value);
        }
      });
      if (avatarFile.value) formData.append("hinhAnh", avatarFile.value);
      return formData;
    };

    const submitForm = async () => {
      if (!validateForm()) {
        showNotification("error", "Vui lòng kiểm tra lại thông tin đã nhập");
        return;
      }
      try {
        loading.value = true;
        const formData = prepareFormData();
        const employeeId = route.params.id || form.value.employeeId;
        let response;
        if (isEditMode.value && employeeId) {
          response = await NhanVienService.updateNhanVien(employeeId, formData);
          showNotification("success", "Cập nhật thông tin nhân viên thành công!");
        } else {
          response = await NhanVienService.createNhanVien(formData);
          showNotification("success", "Thêm nhân viên thành công! Mật khẩu đã được gửi đến email của nhân viên.");
        }
        if (response && (response.data || response.status === 200 || response.status === 201)) {
          setTimeout(() => router.push("/profile/staff"), 1500);
        }
      } catch (error) {
        let errorMessage = "Có lỗi xảy ra khi lưu thông tin nhân viên";
        if (error.code === 'ECONNABORTED') {
          errorMessage = "Yêu cầu mất quá lâu để xử lý. Vui lòng kiểm tra kết nối hoặc thử lại sau.";
        } else if (error.response) {
          const { status, data } = error.response;
          errorMessage = status === 400 ? data.message || "Dữ liệu đầu vào không hợp lệ"
            : status === 409 ? data.message || "Email, số điện thoại hoặc CCCD đã tồn tại"
              : status === 404 ? "Không tìm thấy nhân viên cần cập nhật"
                : `Lỗi server: ${data.message || error.message}`;
        } else {
          errorMessage += `: ${error.message}`;
        }
        showNotification("error", errorMessage);
      } finally {
        loading.value = false;
      }
    };

    const sendResetPasswordEmail = async () => {
      if (!confirm("Bạn có chắc muốn gửi email đặt lại mật khẩu?")) return;
      try {
        loadingEmail.value = true;
        const employeeId = route.params.id || form.value.employeeId;
        const response = await NhanVienService.resetPassword(employeeId);
        showNotification("success", response.data.message || "Email đặt lại mật khẩu đã được gửi!");
      } catch (error) {
        let errorMessage = "Không thể gửi email đặt lại mật khẩu";
        if (error.code === 'ECONNABORTED') {
          errorMessage = "Yêu cầu gửi email mất quá lâu. Vui lòng kiểm tra kết nối hoặc thử lại sau.";
        } else if (error.response) {
          const { status, data } = error.response;
          errorMessage = status === 400 ? data.message || "Dữ liệu không hợp lệ"
            : status === 404 ? "Không tìm thấy nhân viên"
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
      router.push("/profile/staff");
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
        if (newId) loadEmployeeData(newId);
      },
      { immediate: true }
    );

    watch(
      () => form.value.loginProvider,
      (newValue) => {
        if (newValue === "GOOGLE") {
          delete errors.value.matKhau;
          delete errors.value.xacNhanMatKhau;
        }
      }
    );

    loadProvinces();

    return {
      isEditMode,
      loading,
      loadingEmail,
      loadingProvinces,
      loadingWards,
      avatarFile,
      avatarPreview,
      form,
      provinces,
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
      hideNotification,
    };
  },
  beforeUnmount() {
    if (this.avatarPreview?.startsWith("blob:")) {
      URL.revokeObjectURL(this.avatarPreview);
    }
  },
};
</script>

<style scoped>
.add-employee-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
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

.action-buttons {
  display: flex;
  gap: 10px;
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
  content: " *";
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

.radio-label input[type="radio"] {
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

.radio-label input[type="radio"]:checked+.radio-custom {
  border-color: #2196f3;
}

.radio-label input[type="radio"]:checked+.radio-custom::after {
  content: "";
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