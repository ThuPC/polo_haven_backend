<template>
  <div class="py-4 container-fluid">
    <div class="row">
      <div class="col-md-12">
        <div class="card custom-card">
          <div class="card custom-card">
            <div class="card-header custom-card-header text-center">
              <h1 class="mb-0">QUẢN LÝ THƯƠNG HIỆU</h1>
            </div>
            <div class="card-body custom-card-body">
              <div class="mb-4">
                <label for="search" class="font-weight-bold"
                  >Tên Thương hiệu</label
                >
                <div class="d-flex align-items-center">
                  <div
                    class="input-group custom-input-group mb-3"
                    style="max-width: 600px"
                  >
                    <input
                      id="search"
                      type="text"
                      class="form-control"
                      v-model="searchKeyword"
                      placeholder="Tìm kiếm thương hiệu theo tên..."
                      style="border: 1px solid black; border-radius: 5px"
                    />
                  </div>
                  <button
                    class="btn btn-purple ml-2 mb-3"
                    @click="goToAddTrademark"
                  >
                    <i class="fas fa-plus"></i> Thêm tên thương hiệu
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div
            class="card-body custom-card-body"
            style="background-color: rgb(255, 255, 255)"
          >
            <div class="table-responsive">
              <table class="table table-hover mt-3">
                <thead class="table-light text-center">
                  <tr>
                    <th>STT</th>
                    <th>Mã Thương Hiệu</th>
                    <th>Tên Thương Hiệu</th>
                    <th>Trạng Thái</th>
                    <th>Hành Động</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="(thuongHieu, index) in thuongHieuList"
                    :key="thuongHieu.id"
                    class="text-center"
                  >
                    <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
                    <td>{{ thuongHieu.maThuongHieu }}</td>
                    <td>{{ thuongHieu.tenThuongHieu }}</td>
                    <td>
                      <label class="switch">
                        <input
                          type="checkbox"
                          :checked="thuongHieu.trangThai === 1"
                          @change="toggleStatus(thuongHieu)"
                        />
                        <span class="slider round"></span>
                      </label>
                    </td>
                    <td>
                      <button
                        class="btn btn-sm btn-outline-primary"
                        @click="editThuongHieu(thuongHieu)"
                        title="Sửa"
                      >
                        ✏️
                      </button>
                    </td>
                  </tr>
                  <tr v-if="!loading && thuongHieuList.length === 0">
                    <td colspan="5" class="text-center text-danger">
                      Không có thương hiệu nào.
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Modal sửa -->
            <div
              v-if="showEditModal"
              class="modal-overlay"
              @click.self="showEditModal = false"
            >
              <!-- Container modal -->
              <div class="modal-custom">
                <!-- Nút đóng -->
                <button class="btn-close-custom" @click="showEditModal = false">
                  ×
                </button>
                <h4 class="modal-title">Sửa thương hiệu</h4>

                <!-- Form -->
                <form @submit.prevent="updateThuongHieu">
                  <div class="mb-3">
                    <label for="editBrandInput" class="form-label"
                      >Tên thương hiệu:</label
                    >
                    <input
                      type="text"
                      class="form-control"
                      id="editBrandInput"
                      v-model="editTrademarkName"
                    />
                  </div>
                  <div class="modal-footer">
                    <button
                      type="button"
                      class="btn btn-secondary"
                      @click="showEditModal = false"
                    >
                      Hủy bỏ
                    </button>
                    <button type="submit" class="btn btn-success">
                      Cập nhật
                    </button>
                  </div>
                </form>
              </div>
            </div>

            <!-- Modal add -->
            <div
              v-if="showModal"
              class="modal-overlay"
              @click.self="closeModal"
            >
              <!-- Container modal -->
              <div class="modal-custom">
                <!-- Nút đóng -->
                <button class="btn-close-custom" @click="closeModal">×</button>

                <!-- Tiêu đề -->
                <h4 class="modal-title">Thêm thương hiệu</h4>

                <!-- Form -->
                <form @submit.prevent="handleAddTrademark">
                  <div class="mb-3">
                    <label for="brandInput" class="form-label"
                      >Tên thương hiệu:</label
                    >
                    <input
                      type="text"
                      class="form-control"
                      id="brandInput"
                      v-model="newTrademarkName"
                    />
                  </div>

                  <!-- Footer -->
                  <div class="modal-footer">
                    <button
                      type="button"
                      class="btn btn-secondary"
                      @click="closeModal"
                    >
                      Hủy bỏ
                    </button>
                    <button type="submit" class="btn btn-success">
                      Xác nhận
                    </button>
                  </div>
                </form>
              </div>
            </div>

            <!-- Phân trang -->
            <nav>
              <ul class="pagination">
                <li class="page-item" :class="{ disabled: currentPage === 1 }">
                  <button
                    class="page-link"
                    @click="changePage(currentPage - 1)"
                  >
                    &#60;
                  </button>
                </li>
                <li
                  v-for="page in totalPages"
                  :key="page"
                  class="page-item"
                  :class="{ active: page === currentPage }"
                >
                  <button class="page-link" @click="changePage(page)">
                    {{ page }}
                  </button>
                </li>
                <li
                  class="page-item"
                  :class="{ disabled: currentPage === totalPages }"
                >
                  <button
                    class="page-link"
                    @click="changePage(currentPage + 1)"
                  >
                    &#62;
                  </button>
                </li>
              </ul>
            </nav>

            <!-- Thông báo -->
            <div
              v-if="notification.show"
              class="notification-toast"
              :class="notification.type"
            >
              <div class="toast-content">
                <span class="toast-icon">
                  <span class="icon-check">✔</span>
                </span>
                <span class="toast-message">{{ notification.message }}</span>
                <span class="toast-close" @click="closeNotification">×</span>
              </div>
              <div
                class="toast-progress"
                :style="{ width: progressWidth + '%' }"
              ></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.card-body {
  background-color: white;
  border-radius: 0;
}

.custom-card-body {
  background-color: white;
  border-radius: 0;
}
/* Ô tìm kiếm */
.custom-input-group input {
  border: 1px solid #2ecc71; /* Xanh lá */
  border-radius: 5px;
  padding: 6px;
  transition: 0.3s;
}

.custom-input-group input:focus {
  border-color: #27ae60;
  box-shadow: 0 0 5px rgba(39, 174, 96, 0.5);
}

/* Nút Thêm sản phẩm */
.btn-purple {
  background-color: #27ae60; /* Xanh lá */
  color: white;
  border-radius: 5px;
  padding: 8px 15px;
  transition: 0.3s;
  margin-left: 50px;
}

.btn-purple:hover {
  background-color: #219150;
}

/* Bộ lọc trạng thái & danh mục */
.bg-light {
  background-color: #eafaf1 !important; /* Xanh lá nhạt */
  padding: 15px;
  border-radius: 8px;
}

/* Căn chỉnh trạng thái và danh mục thẳng hàng */
.form-row {
  display: flex;
  align-items: center; /* Căn giữa theo chiều dọc */
  justify-content: space-between;
}

/* Radio button */
.form-check-input {
  accent-color: #2ecc71; /* Xanh lá */
}

/* Dropdown danh mục */
.custom-select {
  border: 1px solid #2ecc71;
  border-radius: 5px;
  padding: 8px;
  transition: 0.3s;
  width: 100%;
}

.custom-select:focus {
  border-color: #27ae60;
  box-shadow: 0 0 5px rgba(39, 174, 96, 0.5);
}

/* Đảm bảo radio button không bị lệch */
.form-check-inline {
  margin-right: 10px;
}

/* Bảng chung */
.table {
  width: 100%;
  border-collapse: collapse;
  background-color: #fff;
  border-radius: 8px; /* Bo góc bảng */
  overflow: hidden;
  box-shadow: 0px 2px 5px rgba(0, 0, 0, 0.1); /* Đổ bóng nhẹ */
}

/* Header */
.thead-light {
  background-color: #2ecc71; /* Xanh lá đậm */
  color: white;
  font-weight: bold;
}

/* Dòng kẻ */
.table-bordered th,
.table-bordered td {
  border: 1px solid #ddd;
  padding: 10px;
  text-align: center;
}

/* Hover */
.table tbody tr:hover {
  background-color: rgba(46, 204, 113, 0.2);
  transition: background 0.3s ease-in-out;
}

/* Nút hành động */
.btn-success {
  background-color: #27ae60;
  border-color: #27ae60;
  color: white;
  transition: 0.3s;
}

.btn-success:hover {
  background-color: #219150;
}

.btn-outline-danger {
  border-color: #e74c3c;
  color: #e74c3c;
  transition: 0.3s;
}

.btn-outline-danger:hover {
  background-color: #e74c3c;
  color: white;
}

.custom-card-header {
  border-top-left-radius: 1rem;
  border-top-right-radius: 1rem;
}

.custom-input-group input {
  border: 1px solid #ced4da;
  box-shadow: none;
}

.custom-input-group .input-group-append .btn {
  border: none;
  background-color: #6f42c1;
  color: white;
}

.pagination {
  display: flex;
  justify-content: flex-end; /* Căn phải */
  align-items: center;
  font-size: 14px; /* Giảm kích thước chữ */
  padding-right: 10px;
}

.pagination .page-item .page-link {
  width: 30px; /* Giảm kích thước nút */
  height: 30px;
  font-size: 12px; /* Giảm font chữ */
  line-height: 28px;
}

.pagination .page-item.active .page-link {
  background-color: rgba(0, 128, 0, 0.1); /* Xanh lá cây nhạt */
  border: 1px solid rgba(0, 128, 0, 0.3); /* Viền xanh */
  color: #008000;
}

.pagination .page-item .page-link:hover {
  background-color: rgba(0, 128, 0, 0.6); /* Xanh lá cây đậm hơn */
}

/* Chỉnh nút Previous và Next */
.pagination .page-item.prev .page-link,
.pagination .page-item.next .page-link {
  font-size: 18px; /* Làm lớn biểu tượng */
  font-weight: bold;
}

.pagination {
  margin-right: 60px;
}

.modal-content {
  border-radius: 0.375rem;
  padding: 1.25rem 1.5rem 1.5rem 1.5rem;
  position: relative;
  font-family:
    system-ui,
    -apple-system,
    BlinkMacSystemFont,
    "Segoe UI",
    Roboto,
    "Helvetica Neue",
    Arial,
    sans-serif;
  max-width: 480px;
  width: 100%;
}
.btn-close-custom {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  font-size: 1rem;
  color: #6b7280; /* gray-500 */
  opacity: 1;
  border: none;
  background: transparent;
  cursor: pointer;
}
.btn-close-custom:hover {
  color: #374151; /* gray-700 */
}
h5.modal-title {
  font-weight: 600;
  font-size: 0.875rem;
  color: #111827; /* gray-900 */
  margin-bottom: 0.75rem;
}
label {
  font-size: 0.75rem;
  font-weight: 600;
  color: #374151; /* gray-700 */
  margin-bottom: 0.25rem;
}
input.form-control {
  font-size: 0.75rem;
  border-radius: 0.375rem;
  border: 1px solid #d1d5db; /* gray-300 */
  padding: 0.375rem 0.75rem;
}
input.form-control:focus {
  border-color: #22c55e; /* green-500 */
  box-shadow: 0 0 0 0.2rem rgba(34, 197, 94, 0.25);
}
.btn-cancel {
  font-size: 0.75rem;
  color: #4b5563; /* gray-600 */
  background: transparent;
  border: none;
  padding: 0;
  cursor: pointer;
}
.btn-cancel:hover {
  color: #1f2937; /* gray-800 */
  text-decoration: underline;
}
.btn-confirm {
  font-size: 0.75rem;
  background-color: #22c55e; /* green-500 */
  color: white;
  border-radius: 0.375rem;
  padding: 0.375rem 1rem;
  border: none;
  cursor: pointer;
}
.btn-confirm:hover {
  background-color: #16a34a; /* green-600 */
  color: white;
}
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1.5rem;
  margin-top: 0.5rem;
}

/* css thay đổi trạng thái */
.switch {
  position: relative;
  display: inline-block;
  width: 46px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: 0.4s;
  border-radius: 24px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.4s;
  border-radius: 50%;
}

input:checked + .slider {
  background-color: #4caf50;
}

input:checked + .slider:before {
  transform: translateX(22px);
}

/* nền xám */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 11000;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-custom {
  background-color: white;
  padding: 24px;
  border-radius: 10px;
  min-width: 700px;
  max-width: 90%;
  height: 220px;
  z-index: 11001;
  position: relative;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.25);
  animation: fadeIn 0.25s ease;
}

.modal-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 16px;
}

.btn-close-custom {
  position: absolute;
  top: 10px;
  right: 14px;
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}

#brandInput {
  height: 40px;
}

@keyframes fadeIn {
  from {
    transform: translateY(-20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* Thông báo */
.notification-toast {
  position: fixed;
  top: 20px;
  right: 20px;
  width: 320px;
  background-color: #fff;
  border-left: 5px solid #28a745;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  border-radius: 6px;
  padding: 12px 16px 8px 16px;
  z-index: 12000;
  font-family: "Segoe UI", sans-serif;
}

.toast-content {
  display: flex;
  align-items: center;
  position: relative;
}

.toast-icon {
  background-color: #28a745;
  color: #fff;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  font-size: 12px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
}

.toast-message {
  flex: 1;
  font-size: 14px;
  color: #28a745;
}

.toast-close {
  color: #999;
  font-size: 16px;
  cursor: pointer;
  margin-left: 10px;
  font-weight: bold;
  transition: color 0.2s ease;
}

.toast-close:hover {
  color: #333;
}

.toast-progress {
  height: 3px;
  background-color: #28a745;
  margin-top: 8px;
  transition: width 0.1s linear;
  border-radius: 0 0 4px 4px;
}

/* Error style (optional) */
.notification-toast.error {
  border-left-color: #dc3545;
}

.notification-toast.error .toast-icon {
  background-color: #dc3545;
}

.notification-toast.error .toast-message {
  color: #dc3545;
}

.notification-toast.error .toast-progress {
  background-color: #dc3545;
}
</style>

<script>
import {
  getAllThuongHieu,
  createThuongHieu,
  updateThuongHieu,
  deleteThuongHieu,
  changeStatusThuongHieu,
} from "@/services/RoleQuanLy/ThuongHieuService";
export default {
  name: "ThuongHieu",

  data() {
    return {
      thuongHieuList: [],
      currentPage: 1,
      pageSize: 5,
      totalPages: 0,
      loading: false,
      error: "",
      showModal: false,
      newTrademarkName: "",
      selectedThuongHieu: null,
      searchKeyword: "",
      searchDebounce: null,
      editTrademarkName: "",
      showEditModal: false,
      notification: {
        show: false,
        message: "",
        type: "success",
      },
      progressWidth: 100,
      progressInterval: null,
    };
  },

  methods: {
    goToAddTrademark() {
      this.showModal = true;
    },

    closeModal() {
      this.showModal = false;
      this.newTrademarkName = "";
    },

    closeNotification() {
      this.notification.show = false;
      clearInterval(this.progressInterval);
    },

    async fetchThuongHieu() {
      this.loading = true;
      this.error = "";

      try {
        const response = await getAllThuongHieu(
          this.currentPage - 1,
          this.pageSize,
          this.searchKeyword || ""
        );

        const data = response.data;
        if (data && Array.isArray(data.content)) {
          this.thuongHieuList = data.content;
          this.totalPages = data.totalPages || 1;
        } else {
          this.thuongHieuList = [];
          this.totalPages = 0;
          this.error = "Dữ liệu thương hiệu không hợp lệ!";
          console.error("Dữ liệu không hợp lệ:", data);
        }
      } catch (error) {
        this.thuongHieuList = [];
        this.totalPages = 0;
        this.error = "Không thể tải danh sách thương hiệu!";
        console.error("Lỗi khi lấy thương hiệu:", error);
      } finally {
        this.loading = false;
      }
    },

    changePage(page) {
      if (page > 0 && page <= this.totalPages) {
        this.currentPage = page;
        this.fetchThuongHieu();
      }
    },

    async updateThuongHieu() {
      const tenMoi = this.editTrademarkName
        ? this.editTrademarkName.trim()
        : "";

      if (!tenMoi) {
        this.showNotification("Vui lòng nhập tên thương hiệu!", "error");
        return;
      }

      // Kiểm tra trùng trong danh sách (bỏ qua chính nó)
      const isDuplicate = this.thuongHieuList.some(
        (th) =>
          th.id !== this.selectedThuongHieu.id &&
          String(th.tenThuongHieu ?? "")
            .trim()
            .toLowerCase() === tenMoi.toLowerCase()
      );

      if (isDuplicate) {
        this.showNotification("Thương hiệu đã tồn tại!", "error");
        return;
      }

      try {
        await updateThuongHieu(this.selectedThuongHieu.id, tenMoi);
        this.showNotification("Cập nhật thương hiệu thành công!", "success");
        this.showEditModal = false;
        this.fetchThuongHieu();
      } catch (error) {
        console.error("Lỗi khi cập nhật thương hiệu:", error);
        this.showNotification("Cập nhật thương hiệu thất bại!", "error");
      }
    },
    editThuongHieu(item) {
      this.selectedThuongHieu = item;
      this.editTrademarkName = item.tenThuongHieu;
      this.showEditModal = true;
    },

    async deleteThuongHieu(id) {
      if (confirm("Bạn có chắc chắn muốn xoá thương hiệu này?")) {
        try {
          await deleteThuongHieu(id);
          this.showNotification("Xoá thương hiệu thành công!", "success");
          this.fetchThuongHieu();
        } catch (error) {
          console.error("Lỗi xoá thương hiệu:", error);
          this.showNotification("Xoá thương hiệu thất bại!", "error");
        }
      }
    },

    async toggleStatus(item) {
      const newStatus = item.trangThai === 1 ? 0 : 1;
      try {
        await changeStatusThuongHieu(item.id, newStatus);
        item.trangThai = newStatus;
        this.showNotification("Thay đổi trạng thái thành công!", "success");
      } catch (error) {
        console.error("Lỗi cập nhật trạng thái:", error);
        this.showNotification("Không thể thay đổi trạng thái!", "error");
      }
    },

    async handleAddTrademark() {
      const tenMoi = this.newTrademarkName ? this.newTrademarkName.trim() : "";

      if (!tenMoi) {
        this.showNotification("Vui lòng nhập tên thương hiệu!", "error");
        return;
      }

      const isDuplicate = this.thuongHieuList.some(
        (th) =>
          String(th.tenThuongHieu ?? "")
            .trim()
            .toLowerCase() === tenMoi.toLowerCase()
      );

      if (isDuplicate) {
        this.showNotification("Thương hiệu đã tồn tại!", "error");
        return;
      }

      try {
        const response = await createThuongHieu(tenMoi);
        const newItem = response.data.data;

        this.showNotification("Thêm thương hiệu thành công!", "success");
        this.closeModal();

        if (this.currentPage === 1) {
          this.thuongHieuList.unshift(newItem);
          if (this.thuongHieuList.length > this.pageSize) {
            this.thuongHieuList.pop();
          }
        } else {
          this.currentPage = 1;
          this.fetchThuongHieu();
        }
      } catch (error) {
        console.error("Lỗi khi thêm thương hiệu:", error);
        this.showNotification("Thêm thương hiệu thất bại!", "error");
      }
    },
    showNotification(message, type = "success") {
      this.notification.message = message;
      this.notification.type = type;
      this.notification.show = true;
      this.progressWidth = 100;

      // Clear previous progress
      if (this.progressInterval) {
        clearInterval(this.progressInterval);
      }

      // Progress bar auto-hide
      this.progressInterval = setInterval(() => {
        if (this.progressWidth > 0) {
          this.progressWidth -= 1.5;
        } else {
          clearInterval(this.progressInterval);
          this.notification.show = false;
        }
      }, 50); // ≈ 3.3s
    },
  },

  watch: {
    searchKeyword() {
      clearTimeout(this.searchDebounce);
      this.searchDebounce = setTimeout(() => {
        this.currentPage = 1;
        this.fetchThuongHieu(); // hoặc fetchMauSac(), fetchChatLieu()
      }, 500);
    },
  },

  mounted() {
    this.fetchThuongHieu();
  },
};
</script>
