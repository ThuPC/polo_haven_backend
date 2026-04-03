<template>
  <div class="py-4 container-fluid">
    <div class="row">
      <div class="col-md-12">
        <div class="card custom-card">
          <div class="card shadow-sm border-0 mb-2">
            <!-- Tiêu đề -->
            <div
              class="card-header text-white d-flex align-items-center"
              style="
                background: linear-gradient(to right, #68e98c, #4ea77b);
                border-top-left-radius: 10px;
                border-top-right-radius: 10px;
              "
            >
              <i class="fas fa-box fa-2x mr-2"></i>
              <h4 class="mb-0" style="color: white; margin-left: 10px">
                Quản lý Sản Phẩm
              </h4>
            </div>

            <!-- Nội dung -->
            <div class="card-body">
              <!-- Bộ lọc -->
              <div class="mb-3">
                <strong><i class="fas fa-filter mr-2"></i> Bộ lọc</strong>
              </div>

              <div class="row align-items-end">
                <!-- Tìm kiếm -->
                <div class="col-md-6 mb-3">
                  <label for="search">Tìm kiếm</label>
                  <input
                    type="text"
                    id="search"
                    class="form-control"
                    v-model="searchKeyword"
                    placeholder="Theo tên sản phẩm..."
                  />
                </div>

                <!-- Trạng thái -->
                <div class="col-md-6 mb-3">
                  <label for="status">Trạng thái</label>
                  <select
                    id="status"
                    class="form-control"
                    v-model="selectedStatus"
                  >
                    <option value="">Tất cả</option>
                    <option value="1">Hoạt động</option>
                    <option value="0">Ngừng hoạt động</option>
                  </select>
                </div>
              </div>

              <!-- Nút Thêm mới ở dưới cùng bên phải -->
              <div class="d-flex justify-content-end mt-3">
                <button class="btn btn-success" @click="goToAddProduct">
                  <i class="fas fa-plus"></i> Thêm mới
                </button>
              </div>
            </div>
          </div>

          <div
            class="card-body custom-card-body"
            style="background-color: rgb(255, 255, 255)"
          >
            <div class="table-responsive">
              <table class="table table-hover mt-3 align-middle">
                <thead class="table-light text-center">
                  <tr>
                    <th>#</th>
                    <th>Mã</th>
                    <th>Tên</th>
                    <th>Số lượng CTSP</th>
                    <th>Khoảng giá</th>
                    <th>Trạng thái</th>
                    <th>Hành động</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="(sp, index) in sanPhamList"
                    :key="sp.id"
                    class="text-center"
                  >
                    <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
                    <td>{{ sp.maSanPham }}</td>
                    <td>{{ sp.tenSanPham }}</td>
                    <td>{{ sp.soLuongChiTiet }}</td>
                    <td>
                      <span v-if="sp.giaThapNhat && sp.giaCaoNhat">
                        <span v-if="sp.giaThapNhat === sp.giaCaoNhat">
                          {{ formatCurrency(sp.giaThapNhat) }}
                        </span>
                        <span v-else>
                          {{ formatCurrency(sp.giaThapNhat) }} -
                          {{ formatCurrency(sp.giaCaoNhat) }}
                        </span>
                      </span>
                      <span v-else> Chưa có giá </span>
                    </td>

                    <td>
                      <label class="switch">
                        <input
                          type="checkbox"
                          :checked="sp.trangThai === 1"
                          @change="toggleSanPhamStatus(sp)"
                        />
                        <span class="slider round"></span>
                      </label>
                    </td>
                    <td>
                      <button
                        class="btn btn-sm btn-outline-primary me-1"
                        @click="editSanPham(sp.id)"
                        title="Xem chi tiết"
                      >
                        <i class="fas fa-eye"></i>
                      </button>

                      <button
                        class="btn btn-sm btn-outline-secondary"
                        @click="openEditModal(sp)"
                        title="Chỉnh sửa sản phẩm"
                      >
                        <i class="fas fa-pen"></i>
                      </button>
                    </td>
                  </tr>
                  <tr v-if="!loading && sanPhamList.length === 0">
                    <td colspan="6" class="text-center text-danger">
                      Không có sản phẩm nào.
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div
              class="modal fade"
              id="editProductModal"
              tabindex="-1"
              aria-labelledby="editProductModalLabel"
              aria-hidden="true"
              ref="editProductModalRef"
            >
              <div
                class="modal-dialog modal-xl modal-right custom-modal-margin"
              >
                <div class="modal-content">
                  <div class="modal-header">
                    <h5 class="modal-title" id="editProductModalLabel">
                      Sửa sản phẩm
                    </h5>
                    <button
                      type="button"
                      class="btn-close"
                      data-bs-dismiss="modal"
                      aria-label="Close"
                    ></button>
                  </div>

                  <div class="modal-body">
                    <!-- Tên sản phẩm -->
                    <div class="mb-3">
                      <label for="edit-name" class="form-label"
                        >Tên sản phẩm</label
                      >
                      <input
                        v-model="editTenSanPham"
                        type="text"
                        class="form-control"
                        id="edit-name"
                        placeholder="Nhập tên sản phẩm..."
                      />
                    </div>

                    <!-- Mô tả -->
                    <div class="mb-3">
                      <label for="edit-mo-ta" class="form-label">Mô tả</label>
                      <textarea
                        v-model="editMoTa"
                        class="form-control"
                        id="edit-mo-ta"
                        rows="3"
                      ></textarea>
                    </div>

                    <!-- Thương hiệu, Chất liệu, Xuất xứ -->
                    <div class="row">
                      <div class="col-md-4 mb-3">
                        <label class="form-label">Thương hiệu</label>
                        <select v-model="editThuongHieu" class="form-select">
                          <option hidden value="">--Chọn--</option>
                          <option
                            v-for="item in tenThuongHieuList"
                            :key="item.id"
                            :value="item"
                          >
                            {{ item.tenThuongHieu }}
                          </option>
                        </select>
                      </div>

                      <div class="col-md-4 mb-3">
                        <label class="form-label">Chất liệu</label>
                        <select v-model="editChatLieu" class="form-select">
                          <option hidden value="">--Chọn--</option>
                          <option
                            v-for="item in tenChatLieuList"
                            :key="item.id"
                            :value="item"
                          >
                            {{ item.tenChatLieu }}
                          </option>
                        </select>
                      </div>

                      <div class="col-md-4 mb-3">
                        <label class="form-label">Xuất xứ</label>
                        <select v-model="editXuatXu" class="form-select">
                          <option hidden value="">--Chọn--</option>
                          <option
                            v-for="item in tenXuatXuList"
                            :key="item.id"
                            :value="item"
                          >
                            {{ item.noiXuatXu }}
                          </option>
                        </select>
                      </div>
                    </div>
                  </div>

                  <!-- Footer -->
                  <div class="modal-footer">
                    <button
                      type="button"
                      class="btn btn-secondary"
                      data-bs-dismiss="modal"
                    >
                      Hủy
                    </button>
                    <button
                      type="button"
                      class="btn btn-success"
                      @click="updateSanPham"
                    >
                      Lưu thay đổi
                    </button>
                  </div>
                </div>
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
          </div>
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

.modal-right {
  margin-left: auto;
  margin-right: 160px;
}

.custom-modal-margin {
  margin-top: 130px;
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
  z-index: 9999;
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
import { useRouter } from "vue-router";
import Swal from "sweetalert2";
import * as bootstrap from "bootstrap";
import {
  getAllSanPham,
  updateSanPham,
  changeSanPhamStatus,
  deleteSanPham,
  getThuongHieuList,
  getChatLieuList,
  getXuatXuList,
} from "@/services/RoleQuanLy/SanPhamService";

export default {
  name: "SanPham",

  setup() {
    const router = useRouter();

    const goToAddProduct = () => {
      router.push("/add-product");
    };

    const editSanPham = (id) => {
      router.push(`/update-product/${id}`);
    };

    return {
      goToAddProduct,
      editSanPham,
    };
  },

  data() {
    return {
      sanPhamList: [],
      tenThuongHieuList: [],
      tenChatLieuList: [],
      tenXuatXuList: [],
      currentPage: 1,
      pageSize: 5,
      totalPages: 0,
      loading: false,
      searchKeyword: "",
      searchDebounceTimeout: null,
      selectedStatus: "",
      error: "",
      editId: null,
      editTenSanPham: "",
      editMoTa: "",
      editThuongHieu: "",
      editChatLieu: "",
      editXuatXu: "",
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
    async fetchSanPham() {
      this.loading = true;
      this.error = "";

      try {
        const response = await getAllSanPham({
          page: this.currentPage - 1,
          size: this.pageSize,
          keyword: this.searchKeyword?.trim() || "",
          status:
            this.selectedStatus !== "" ? Number(this.selectedStatus) : null,
        });

        const data = response.data;
        if (data && Array.isArray(data.content)) {
          this.sanPhamList = data.content;
          console.log("Danh sách sản phẩm:", this.sanPhamList);
          this.totalPages = data.totalPages || 1;
        } else {
          this.sanPhamList = [];
          this.totalPages = 0;
          this.error = "Dữ liệu sản phẩm không hợp lệ!";
          console.error("Dữ liệu không hợp lệ:", data);
        }
      } catch (error) {
        this.sanPhamList = [];
        this.totalPages = 0;
        this.error = "Không thể tải sản phẩm!";
        console.error("Lỗi khi lấy sản phẩm:", error);
      } finally {
        this.loading = false;
      }
    },
    formatCurrency(value) {
      const number = Number(value);
      if (isNaN(number)) return "";

      return new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND",
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
      }).format(number);
    },

    openEditModal(sanPham) {
      console.log("Đã click chỉnh sửa sản phẩm:", sanPham);

      this.editId = sanPham.id;
      this.editTenSanPham = sanPham.tenSanPham;
      this.editMoTa = sanPham.moTa;

      this.editThuongHieu = this.tenThuongHieuList.find(
        (item) => item.tenThuongHieu === sanPham.tenThuongHieu
      );
      this.editChatLieu = this.tenChatLieuList.find(
        (item) => item.tenChatLieu === sanPham.tenChatLieu
      );
      this.editXuatXu = this.tenXuatXuList.find(
        (item) => item.noiXuatXu === sanPham.tenXuatXu
      );

      const modal = new bootstrap.Modal(this.$refs.editProductModalRef);
      modal.show();
    },

    async updateSanPham() {
      const payload = {
        tenSanPham: this.editTenSanPham,
        moTa: this.editMoTa,
        thuongHieuId: this.editThuongHieu?.id,
        chatLieuId: this.editChatLieu?.id,
        xuatXuId: this.editXuatXu?.id,
      };

      console.log("Payload gửi lên:", payload);

      const result = await Swal.fire({
        title: "Xác nhận cập nhật?",
        text: "Bạn có chắc chắn muốn cập nhật sản phẩm này không?",
        icon: "question",
        showCancelButton: true,
        confirmButtonText: "Cập nhật",
        cancelButtonText: "Huỷ",
        confirmButtonColor: "#3085d6",
        cancelButtonColor: "#6c757d",
      });

      if (!result.isConfirmed) return;

      try {
        await updateSanPham(this.editId, payload);
        this.fetchSanPham();
        this.showNotification("Cập nhật sản phẩm thành công!", "success");
        const modal = bootstrap.Modal.getInstance(
          this.$refs.editProductModalRef
        );
        modal.hide();
      } catch (error) {
        console.error("Lỗi cập nhật:", error);
        this.showNotification("Cập nhật sản phẩm thất bại!", "error");
      }
    },
    async fetchComboboxData() {
      try {
        const thuongHieuRes = await getThuongHieuList();
        const chatLieuRes = await getChatLieuList();
        const xuatXuRes = await getXuatXuList();

        this.tenThuongHieuList = thuongHieuRes.data;
        this.tenChatLieuList = chatLieuRes.data;
        this.tenXuatXuList = xuatXuRes.data;
      } catch (error) {
        console.error("Lỗi khi lấy dữ liệu combobox:", error);
      }
    },

    showNotification(message, type = "success") {
      this.notification.message = message;
      this.notification.type = type;
      this.notification.show = true;
      this.progressWidth = 100;

      if (this.progressInterval) {
        clearInterval(this.progressInterval);
      }

      this.progressInterval = setInterval(() => {
        if (this.progressWidth > 0) {
          this.progressWidth -= 1.5;
        } else {
          clearInterval(this.progressInterval);
          this.notification.show = false;
        }
      }, 50);
    },

    changePage(page) {
      if (page > 0 && page <= this.totalPages) {
        this.currentPage = page;
        this.fetchSanPham();
      }
    },

    async toggleSanPhamStatus(item) {
      const newStatus = item.trangThai === 1 ? 0 : 1;

      try {
        await changeSanPhamStatus(item.id, { trangThai: newStatus });
        item.trangThai = newStatus;
        this.showNotification("Thay đổi trạng thái thành công!", "success");
      } catch (error) {
        console.error("Lỗi cập nhật trạng thái:", error);
        this.showNotification("Không thể thay đổi trạng thái!", "error");
      }
    },

    async deleteSanPham(id) {
      if (confirm("Bạn có chắc chắn muốn xoá sản phẩm này?")) {
        try {
          await deleteSanPham(id);
          alert("Xoá sản phẩm thành công!");
          this.fetchSanPham();
        } catch (error) {
          console.error("Lỗi xoá sản phẩm:", error);
          alert("Xoá sản phẩm thất bại!");
        }
      }
    },
  },

  watch: {
    searchKeyword() {
      if (this.searchDebounceTimeout) {
        clearTimeout(this.searchDebounceTimeout);
      }
      this.searchDebounceTimeout = setTimeout(() => {
        this.currentPage = 1;
        this.fetchSanPham();
      }, 300);
    },
    selectedStatus() {
      this.currentPage = 1;
      this.fetchSanPham();
    },
  },

  mounted() {
    this.fetchSanPham();
    this.fetchComboboxData();
  },
};
</script>
