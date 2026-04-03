<template>
  <div class="container-fluid p-4">
    <!-- Success/Error Notifications -->
    <div v-if="notification.show" :class="['notification', notification.type]">
      <i :class="notification.type === 'success' ? 'fa-solid fa-check-circle' : 'fa-solid fa-exclamation-triangle'"></i>
      {{ notification.message }}
      <button @click="hideNotification" class="close-btn">&times;</button>
    </div>

    <div class="header">
      <div class="title">
        <i class="fa-solid fa-people-roof p-2"></i>
        Quản lý tài khoản nhân viên
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
              <select v-model="searchFilters.status">
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
            <button class="btn btn-primary" @click="triggerImportExcel" :disabled="loading">
              <i class="fa-solid fa-file-import"></i>
              Import Excel
            </button>
            <!-- Thêm nút Download Template mới -->
            <button class="btn btn-info" @click="downloadTemplate" :disabled="loading">
              <i class="fa-solid fa-download"></i>
              Download Template
            </button>
            <input type="file" ref="excelInput" accept=".xlsx,.xls" style="display: none" @change="importExcel">
          </div>
        </div>
      </div>
    </div>

    <div class="employee-list">
      <div class="list-header">
        <div class="list-title">
          <i class="fa-solid fa-list-ul p-2"></i>
          Danh sách nhân viên
        </div>
        <router-link to="/nhan-vien/create">
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
      <div v-else-if="nhanViens.length === 0" class="empty-state">
        <i class="fa-solid fa-users-slash"></i>
        <h3>Không tìm thấy nhân viên nào</h3>
        <p>Thử thay đổi bộ lọc hoặc thêm nhân viên mới</p>
      </div>

      <!-- Table -->
      <div v-else class="table-container">
        <table>
          <thead>
            <tr>
              <th>STT</th>
              <th>Ảnh</th>
              <th>Mã nhân viên</th>
              <th>Tên nhân viên</th>
              <th>Email</th>
              <th>Số điện thoại</th>
              <th>Ngày sinh</th>
              <th>Giới tính</th>
              <th>Chức vụ</th>
              <th class="address-column">Địa chỉ</th>
              <th class="status-column">Trạng Thái</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(nv, index) in nhanViens" :key="nv.id">
              <td>{{ index + 1 + currentPage * pageSize }}</td>
              <td>
                <div class="employee-photo">
                  <img v-if="nv.hinhAnh" :src="nv.hinhAnh" :alt="nv.tenNhanVien || 'Avatar'" @error="onImageError"
                    loading="lazy" />
                  <i v-else class="fa-solid fa-user"></i>
                </div>
              </td>
              <td>{{ nv.maNhanVien || 'N/A' }}</td>
              <td>{{ nv.tenNhanVien || 'N/A' }}</td>
              <td>{{ nv.email || 'N/A' }}</td>
              <td>{{ nv.sdt || 'N/A' }}</td>
              <td>{{ formatDate(nv.ngaySinh) }}</td>
              <td>{{ nv.gioiTinh == 1 ? 'Nam' : 'Nữ' }}</td>
              <td>
                <span :class="['status-badge', getChucVuClass(nv.chucVu)]">
                  {{ nv.chucVu || 'N/A' }}
                </span>
              </td>
              <td class="address-cell">{{ formatAddress(nv.diaChi) }}</td>
              <td>
                <span :class="['status-badge', getStatusClass(nv.trangThai)]">
                  {{ formatStatus(nv.trangThai) }}
                </span>
              </td>
              <td>
                <div class="action-buttons">
                  <router-link :to="`/nhan-vien/update/${nv.id}`">
                    <button class="action-btn btn-warning" title="Chỉnh sửa">
                      <i class="fa-solid fa-pen-to-square"></i>
                    </button>
                  </router-link>
                  <button class="action-btn btn-danger" @click="confirmDelete(nv)" title="Xóa" :disabled="!nv.id">
                    <i class="fa-solid fa-trash-can"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- Pagination -->
        <div class="pagination-container">
          <div class="pagination-controls">
            <button @click="goToPage(currentPage - 1)" class="btn btn-secondary"
              :disabled="currentPage === 0 || loading">
              <i class="fa-solid fa-chevron-left"></i>
            </button>
            <span class="page-numbers">
              <button v-for="page in visiblePages" :key="page" @click="goToPage(page - 1)"
                :class="['page-btn', { 'active': page - 1 === currentPage }]" :disabled="loading">
                {{ page }}
              </button>
            </span>
            <button @click="goToPage(currentPage + 1)" class="btn btn-secondary" :disabled="isLastPage || loading">
              <i class="fa-solid fa-chevron-right"></i>
            </button>
          </div>
        </div>

      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteModal" class="modal-overlay" @click="closeDeleteModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Xác nhận xóa</h3>
          <button @click="closeDeleteModal" class="btn btn-secondary">&times;</button>
        </div>
        <div class="modal-body">
          <i class="fa-solid fa-exclamation-triangle warning-icon"></i>
          <p>Bạn có chắc chắn muốn xóa nhân viên <strong>{{ selectedEmployee?.tenNhanVien || 'N/A' }}</strong>?</p>
          <p class="warning-text">Hành động này không thể hoàn tác!</p>
          <p class="info-text">ID: {{ selectedEmployee?.id || 'N/A' }}</p>
        </div>
        <div class="modal-footer">
          <button @click="closeDeleteModal" class="btn btn-secondary">Hủy</button>
          <button @click="deleteEmployee" class="btn btn-danger" :disabled="deleteLoading">
            <i v-if="deleteLoading" class="fa-solid fa-spinner fa-spin"></i>
            {{ deleteLoading ? 'Đang xóa...' : 'Xóa' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import NhanVienService from "@/services/RoleQuanLy/NhanVienService";

export default {
  name: 'Staff',
  data() {
    return {
      nhanViens: [],
      currentPage: 0,
      pageSize: 5,
      totalPages: 0,
      totalElements: 0,
      isLastPage: false,
      loading: false,
      deleteLoading: false,
      searchTimeout: null,
      showDeleteModal: false,
      selectedEmployee: null,
      searchFilters: {
        keyword: '',
        status: '',
        gioiTinh: ''
      },
      notification: {
        show: false,
        type: 'success',
        message: ''
      },

    };
  },
  computed: {
    visiblePages() {
      const pages = [];
      const totalPages = this.totalPages;
      const current = this.currentPage + 1;

      if (totalPages <= 7) {
        for (let i = 1; i <= totalPages; i++) pages.push(i);
      } else {
        if (current <= 4) {
          for (let i = 1; i <= 5; i++) pages.push(i);
          pages.push('...');
          pages.push(totalPages);
        } else if (current >= totalPages - 3) {
          pages.push(1);
          pages.push('...');
          for (let i = totalPages - 4; i <= totalPages; i++) pages.push(i);
        } else {
          pages.push(1);
          pages.push('...');
          for (let i = current - 1; i <= current + 1; i++) pages.push(i);
          pages.push('...');
          pages.push(totalPages);
        }
      }
      return pages.filter(page => page !== '...');
    }
  },
  mounted() {
    this.fetchNhanViens();

  },
  created() {
    this.$router.afterEach((to, from) => {
      if (from.path === '/nhan-vien/create' && to.path === '/nhan-vien') {
        this.handleEmployeeCreated();
      }
    });
  },
  beforeUnmount() {
    clearTimeout(this.searchTimeout);
  },
  methods: {
    // Notification handling
    showNotification(type, message) {
      this.notification = { show: true, type, message };
      setTimeout(() => this.hideNotification(), 5000);
    },
    hideNotification() {
      this.notification.show = false;
    },

    // Employee data handling
    async fetchNhanViens(extraParams = {}) {
      try {
        if (!extraParams._silent) {
          this.loading = true;
        }
        const params = {
          page: this.currentPage,
          size: this.pageSize,
          sortBy: 'ngayTao',
          sortDir: 'desc',
          // Cache-busting to avoid stale responses
          _t: Date.now(),
          ...this.buildSearchParams(),
          ...extraParams
        };

        const response = await NhanVienService.getAllNhanVien(params);
        const data = response.data || response;
        console.log('API Response:', data.content);

        // Cập nhật danh sách nhân viên
        this.nhanViens = data.content
          .map(nv => {
            const imageUrl = nv.hinhAnh && nv.hinhAnh.trim() !== ''
              ? `http://localhost:8080/uploads/nhan_vien/${nv.hinhAnh}`
              : null;
            console.log(`Image URL for ${nv.tenNhanVien}:`, imageUrl);
            return { ...nv, hinhAnh: imageUrl };
          });

        // Cập nhật thông tin phân trang
        this.totalPages = data.totalPages || 0;
        this.totalElements = data.totalElements || 0;
        this.isLastPage = data.last || false;



        // Force re-render để đảm bảo dữ liệu mới hiển thị
        this.$nextTick(() => {
          this.$forceUpdate();
        });

      } catch (error) {
        console.error('API Error:', error.response);
        this.showNotification('error', 'Không thể tải danh sách nhân viên: ' + (error.response?.data?.message || error.message));
        this.nhanViens = [];
      } finally {
        if (!extraParams._silent) {
          this.loading = false;
        }
      }
    },
    async pollForNewData(previousTotalElements) {
      const backoffMs = [200, 600, 1200, 2000];
      for (let i = 0; i < backoffMs.length; i++) {
        await new Promise(resolve => setTimeout(resolve, backoffMs[i]));
        await this.fetchNhanViens();
        if (this.totalElements > previousTotalElements) {
          return true;
        }
      }
      return false;
    },
    buildSearchParams() {
      const params = {};
      if (this.searchFilters.keyword?.trim()) params.keyword = this.searchFilters.keyword.trim();
      if (this.searchFilters.status !== '') {
        const status = parseInt(this.searchFilters.status);
        if (!isNaN(status)) params.status = status;
      }
      if (this.searchFilters.gioiTinh !== '') {
        const gioiTinh = parseInt(this.searchFilters.gioiTinh);
        if (!isNaN(gioiTinh)) params.gioiTinh = gioiTinh;
      }
      params.chucVu = "NHANVIEN";
      console.log('Search Params:', params);
      return params;
    },
    debounceSearch() {
      clearTimeout(this.searchTimeout);
      this.searchTimeout = setTimeout(() => this.performSearch(), 500);
    },
    async performSearch() {
      this.currentPage = 0;
      await this.fetchNhanViens();
    },
    async resetFilters() {
      this.searchFilters = {
        keyword: '',
        status: '',
        gioiTinh: ''
      };
      this.currentPage = 0;
      await this.fetchNhanViens();
    },
    async exportExcel() {
      try {
        this.loading = true;
        await NhanVienService.exportExcel(this.buildSearchParams());
        this.showNotification('success', 'Xuất Excel thành công!');
      } catch (error) {
        const errorMessage = error.message || 'Xuất Excel thất bại';
        this.showNotification('error', errorMessage);
      } finally {
        this.loading = false;
      }
    },
    triggerImportExcel() {
      this.$refs.excelInput.click();
    },
    async downloadTemplate() {
      try {
        this.loading = true;
        await NhanVienService.downloadTemplate();
        this.showNotification('success', 'Download template thành công');
      } catch (error) {
        this.showNotification('error', 'Không thể download template');
      } finally {
        this.loading = false;
      }
    },
    async importExcel(event) {
      const file = event.target.files[0];
      if (!file) return;
      try {
        this.loading = true;
        const previousTotal = this.totalElements;
        const response = await NhanVienService.importExcel(file);
        const imported = response?.data?.data || response?.data || [];

        // Optimistic update: chèn ngay các bản ghi mới nếu đang ở trang đầu
        if (Array.isArray(imported) && imported.length > 0) {
          const normalize = (nv) => ({
            ...nv,
            hinhAnh: nv?.hinhAnh && String(nv.hinhAnh).trim() !== ''
              ? `http://localhost:8080/uploads/nhan_vien/${nv.hinhAnh}`
              : null
          });
          const importedNormalized = imported.map(normalize);
          if (this.currentPage === 0) {
            const byId = new Map();
            [...importedNormalized, ...this.nhanViens].forEach(nv => {
              byId.set(nv.id, nv);
            });
            this.nhanViens = Array.from(byId.values()).slice(0, this.pageSize);
          }
          this.totalElements = (this.totalElements || 0) + importedNormalized.length;
          this.totalPages = Math.max(1, Math.ceil(this.totalElements / this.pageSize));
          this.$nextTick(() => this.$forceUpdate());
        }

        // Refresh nhẹ ở nền để đồng bộ hoá dữ liệu thật từ backend
        setTimeout(() => {
          this.currentPage = 0;
          this.fetchNhanViens({ _silent: true });
        }, 200);

        // Poll ngắn để đợi backend hoàn tất xử lý import (tránh dữ liệu cũ)
        const updated = await this.pollForNewData(previousTotal);

        // Hiển thị thông báo thành công
        const message = response?.data?.message || response?.message || 'Import Excel thành công!';
        this.showNotification('success', updated ? message : message + ' (danh sách sẽ cập nhật trong giây lát)');

        // Reset input file
        this.$refs.excelInput.value = '';

        // Force re-render
        this.$forceUpdate();

      } catch (error) {
        const errorMessage = error.response?.data?.message || 'Import Excel thất bại: ' + error.message;
        this.showNotification('error', errorMessage);
      } finally {
        this.loading = false;
      }
    },
    handleEmployeeCreated() {
      this.currentPage = 0;
      this.fetchNhanViens();
      this.showNotification('success', 'Thêm nhân viên thành công!');
    },

    // Pagination handling
    async goToPage(page) {
      if (page >= 0 && page < this.totalPages && page !== this.currentPage) {
        this.currentPage = page;
        await this.fetchNhanViens();
      }
    },

    // Delete modal handling
    confirmDelete(employee) {
      if (!employee || !employee.id) {
        this.showNotification('error', 'Không thể xác định nhân viên cần xóa');
        return;
      }
      this.selectedEmployee = employee;
      this.showDeleteModal = true;
    },
    async deleteEmployee() {
      if (!this.selectedEmployee) return;
      try {
        this.deleteLoading = true;
        await NhanVienService.deleteNhanVien(this.selectedEmployee.id);
        this.showNotification('success', `Đã xóa nhân viên ${this.selectedEmployee.tenNhanVien || 'N/A'} thành công!`);
        if (this.nhanViens.length === 1 && this.currentPage > 0) {
          this.currentPage--;
        }
        await this.fetchNhanViens();
        this.closeDeleteModal();
      } catch (error) {
        let errorMessage = `Không thể xóa nhân viên ${this.selectedEmployee.tenNhanVien || 'N/A'}`;
        if (error.response?.status === 404) {
          errorMessage += '. Nhân viên không tồn tại';
        } else if (error.response?.status === 409) {
          errorMessage += '. Nhân viên đang được sử dụng trong hệ thống';
        } else {
          errorMessage += `: ${error.response?.data?.message || error.message}`;
        }
        this.showNotification('error', errorMessage);
      } finally {
        this.deleteLoading = false;
      }
    },
    closeDeleteModal() {
      this.showDeleteModal = false;
      this.selectedEmployee = null;
    },

    // Utility functions
    formatDate(dateString) {
      if (!dateString) return 'N/A';
      try {
        const date = new Date(dateString);
        return isNaN(date.getTime()) ? 'N/A' : date.toLocaleDateString('vi-VN');
      } catch {
        return 'N/A';
      }
    },
    formatStatus(trangThai) {
      return trangThai == 1 ? 'Hoạt động' : trangThai == 0 ? 'Ngừng hoạt động' : 'N/A';
    },
    getStatusClass(trangThai) {
      return trangThai == 1 ? 'status-active' : trangThai == 0 ? 'status-inactive' : 'status-unknown';
    },
    getChucVuClass(chucVu) {
      return chucVu === "NHANVIEN" ? "status-employee" : "status-manager";
    },

    // Local method để format địa chỉ
    formatAddress(diaChi) {
      if (!diaChi || typeof diaChi !== 'string') return 'N/A';

      const parts = diaChi.split(',').map(part => part.trim()).filter(part => part);

      if (parts.length === 0) return 'N/A';
      if (parts.length === 1) return parts[0];

      // Hàm kiểm tra và chuẩn hóa tên tỉnh/thành phố
      const normalizeProvince = (tinh) => {
        const lowerTinh = tinh.toLowerCase();
        // Nếu đã có tiền tố Tỉnh hoặc Thành phố, trả về nguyên gốc
        if (lowerTinh.startsWith('tỉnh ') || lowerTinh.startsWith('thành phố ') ||
          lowerTinh.startsWith('tp.') || lowerTinh.startsWith('tp ')) {
          return tinh;
        }
        // Danh sách các thành phố không cần tiền tố "Tỉnh"
        const knownCities = ['hà nội', 'hồ chí minh', 'đà nẵng', 'cần thơ', 'hải phòng'];
        if (knownCities.includes(lowerTinh)) {
          return `Thành phố ${tinh}`;
        }
        return `Tỉnh ${tinh}`;
      };

      // Xử lý địa chỉ có 2 phần: chi tiết và tỉnh
      if (parts.length === 2) {
        const [chiTiet, tinh] = parts;
        return `${chiTiet}, ${normalizeProvince(tinh)}`;
      }

      // Xử lý địa chỉ có 3 phần trở lên: chi tiết, xã/phường, tỉnh
      if (parts.length >= 3) {
        const chiTiet = parts[0];
        const xaPhuong = parts[parts.length - 2];
        const tinh = parts[parts.length - 1];

        // Kiểm tra xem xã/phường đã có prefix chưa
        let wardType = '';
        const lowerWard = xaPhuong.toLowerCase();

        // Nếu đã có prefix thì không thêm nữa
        if (lowerWard.startsWith('xã ') || lowerWard.startsWith('phường ') ||
          lowerWard.startsWith('thị trấn ') || lowerWard.startsWith('p.') ||
          lowerWard.startsWith('tt.')) {
          wardType = '';
        } else {
          // Xác định ward type từ tên
          if (lowerWard.includes('phường') || lowerWard.includes('p.')) {
            wardType = 'Phường ';
          } else if (lowerWard.includes('thị trấn')) {
            wardType = 'Thị trấn ';
          } else {
            wardType = 'Xã ';
          }
        }

        return `${chiTiet}, ${wardType}${xaPhuong}, ${normalizeProvince(tinh)}`;
      }

      return parts.join(', ');
    },


    onImageError(event) {
      event.target.src = '/default-avatar.png';
      console.error('Image load failed, switched to default:', event.target.src);
    },
    handleImageError(employee) {
      console.error(`Failed to load image for employee ${employee.id}: ${employee.hinhAnh}`);
      this.showNotification('warning', `Không thể tải ảnh cho nhân viên ${employee.tenNhanVien}`);
    }
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

.employee-list {
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

.pagination-container {
  padding: 20px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  border-top: 1px solid #dee2e6;
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
  border: 1px solid #dee2e6;
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
  background: #667eea;
  color: white;
  border-color: #667eea;
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
  text-align: center;
}

.warning-icon {
  font-size: 48px;
  color: #ffc107;
  margin-bottom: 20px;
}

.warning-text {
  color: #dc3545;
  font-size: 14px;
  margin-top: 10px;
}

.info-text {
  color: #6c757d;
  font-size: 14px;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #dee2e6;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.status-employee {
  background: #d1ecf1;
  color: #0c5460;
}

.status-manager {
  background: #ffeeba;
  color: #856404;
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

  .pagination-container {
    flex-direction: column;
    gap: 15px;
    text-align: center;
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