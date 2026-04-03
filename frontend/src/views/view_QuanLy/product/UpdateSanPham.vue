<template>
  <div class="py-4 container-fluid">
    <div class="row">
      <div class="col-md-12">
        <div id="app">
          <div class="card">
            <div class="card-body">
              <div class="d-flex justify-content-between mb-3">
                <div>
                  <a href="#" class="text-primary">Danh sách sản phẩm</a> /
                  <span>Thêm sản phẩm</span>
                </div>
              </div>
              <!-- bộ lọc -->
              <div class="container py-3">
                <form class="row g-3">
                  <div class="col-12 col-sm-4">
                    <label for="kich-co" class="form-label">Kích cỡ</label>
                    <select
                      id="kich-co"
                      class="form-select"
                      v-model="selectedFilters.kichCoId"
                    >
                      <option value="">Chọn kích cỡ...</option>
                      <option
                        v-for="item in comboBoxData.tenSize"
                        :key="item.id"
                        :value="item.id"
                      >
                        {{ item.tenSize }}
                      </option>
                    </select>
                  </div>
                  <div class="col-12 col-sm-4">
                    <label for="mau-sac" class="form-label">Màu sắc</label>
                    <select
                      id="mau-sac"
                      class="form-select"
                      v-model="selectedFilters.mauSacId"
                    >
                      <option value="">Chọn màu sắc...</option>
                      <option
                        v-for="item in comboBoxData.tenMauSac"
                        :key="item.id"
                        :value="item.id"
                      >
                        {{ item.tenMau }}
                      </option>
                    </select>
                  </div>
                  <div class="col-12 col-sm-4">
                    <label for="chat-lieu" class="form-label">Chất liệu</label>
                    <select
                      id="chat-lieu"
                      class="form-select"
                      v-model="selectedFilters.chatLieuId"
                    >
                      <option value="">Chọn chất liệu...</option>
                      <option
                        v-for="item in comboBoxData.tenChatLieu"
                        :key="item.id"
                        :value="item.id"
                      >
                        {{ item.tenChatLieu }}
                      </option>
                    </select>
                  </div>
                  <div class="col-12 d-flex justify-content-center gap-2 mt-2">
                    <button
                      type="reset"
                      class="btn btn-secondary d-flex align-items-center"
                      @click="resetFilters"
                    >
                      <i class="fas fa-undo me-1"></i> Làm mới
                    </button>

                    <button type="submit" class="btn btn-purple">
                      Tìm kiếm
                    </button>
                  </div>
                </form>
              </div>
              <div class="d-flex justify-content-end mb-3">
                <router-link
                  to="/add-product"
                  class="btn btn-success d-flex align-items-center"
                  style="
                    border-radius: 20px;
                    font-weight: 500;
                    padding: 8px 16px;
                  "
                >
                  <i class="fas fa-plus-circle me-2"></i>
                  Thêm mới sản phẩm chi tiết
                </router-link>
              </div>

              <!-- thông báo -->
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

              <!-- bảng -->
              <table
                class="table table-hover align-middle text-center border shadow-sm rounded"
              >
                <thead class="table-light">
                  <tr>
                    <th class="align-middle">#</th>
                    <th class="align-middle">Tên</th>
                    <th class="align-middle">Màu sắc</th>
                    <th class="align-middle">Kích thước</th>

                    <th class="align-middle">Số lượng</th>
                    <th class="align-middle">Đơn giá</th>
                    <th class="align-middle">Trạng thái</th>
                    <th class="align-middle">Ảnh</th>
                    <th class="align-middle">Hành động</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="(ctsp, index) in filteredChiTietList"
                    :key="ctsp.id"
                    class="align-middle"
                  >
                    <td>{{ index + 1 }}</td>
                    <td>{{ ctsp.tenSanPham }}</td>
                    <td>{{ ctsp.mauSac }}</td>
                    <td>{{ ctsp.kichThuoc }}</td>

                    <td>{{ ctsp.soLuong }}</td>
                    <td>{{ ctsp.donGia }}</td>
                    <td>
                      {{
                        ctsp.trangThai === 1 ? "Hoạt động" : "Không hoạt động"
                      }}
                    </td>
                    <td>
                      <img
                        v-if="ctsp.url"
                        :src="ctsp.url"
                        alt="Ảnh sản phẩm"
                        width="64"
                        height="64"
                        class="rounded shadow-sm border"
                      />
                    </td>
                    <td>
                      <button
                        type="button"
                        class="btn btn-sm text-white rounded px-3"
                        style="background-color: #5e72e4"
                        @click="editCTSP(ctsp.id)"
                      >
                        Sửa
                      </button>
                      <button
                        type="button"
                        class="btn btn-sm text-white rounded px-3"
                        style="background-color: #f5365c; margin-left: 8px"
                        @click="handleDeleteCTSP(ctsp.id)"
                      >
                        Xóa
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>

              <!-- Modal update -->
              <div
                class="modal fade"
                id="productModal"
                tabindex="-1"
                aria-labelledby="productModalLabel"
                aria-hidden="true"
              >
                <div
                  class="modal-dialog modal-xl modal-dialog-centered modal-dialog-right"
                >
                  <div class="modal-content p-4">
                    <div v-if="currentEditingCTSP">
                      <div class="modal-header border-0 pb-3">
                        <h5 class="modal-title" id="productModalLabel">
                          {{
                            currentEditingCTSP.tenSanPham || "Chi tiết sản phẩm"
                          }}
                          [{{ getMauSacTen(currentEditingCTSP.mauSacId) }} -
                          {{ getSizeTen(currentEditingCTSP.kichThuocId) }}]
                        </h5>

                        <button
                          type="button"
                          class="btn-close"
                          data-bs-dismiss="modal"
                          aria-label="Close"
                        ></button>
                      </div>

                      <form @submit.prevent="updateCTSP">
                        <div class="row row-cols-1 row-cols-md-3 g-4">
                          <!-- Cột 1 -->
                          <div class="col d-flex flex-column form-column">
                            <div>
                              <label for="size"
                                ><span class="required-star">*</span> Kích
                                cỡ</label
                              >
                              <select
                                id="size"
                                class="form-select"
                                v-model="currentEditingCTSP.kichThuocId"
                                required
                              >
                                <option
                                  v-for="size in comboBoxData.tenSize"
                                  :key="size.id"
                                  :value="String(size.id)"
                                >
                                  {{ size.tenSize }}
                                </option>
                              </select>
                            </div>

                            <div>
                              <label for="color"
                                ><span class="required-star">*</span> Màu
                                sắc</label
                              >
                              <select
                                id="color"
                                class="form-select"
                                v-model="currentEditingCTSP.mauSacId"
                                required
                              >
                                <option
                                  v-for="color in comboBoxData.tenMauSac"
                                  :key="color.id"
                                  :value="String(color.id)"
                                >
                                  {{ color.tenMau }}
                                </option>
                              </select>
                            </div>
                          </div>

                          <!-- Cột 2 -->
                          <div class="col d-flex flex-column form-column">
                            <div>
                              <label for="price"
                                ><span class="required-star">*</span> Đơn
                                giá</label
                              >
                              <input
                                type="text"
                                id="price"
                                class="form-control"
                                v-model="currentEditingCTSP.giaBan"
                                required
                              />
                            </div>
                          </div>

                          <!-- Cột 3 -->
                          <div class="col d-flex flex-column form-column">
                            <div>
                              <label for="quantity"
                                ><span class="required-star">*</span> Số
                                lượng</label
                              >
                              <input
                                type="text"
                                id="quantity"
                                class="form-control"
                                v-model="currentEditingCTSP.soLuong"
                                required
                              />
                            </div>
                          </div>
                        </div>

                        <!-- Hình ảnh -->
                        <div class="row mt-4">
                          <div class="col-12 mt-2">
                            <p class="fw-semibold">Hình ảnh sản phẩm:</p>
                            <div
                              class="d-flex gap-3 flex-wrap align-items-center"
                            >
                              <!-- Danh sách ảnh -->
                              <div
                                v-for="(
                                  img, index
                                ) in currentEditingCTSP.hinhAnhs"
                                :key="index"
                                class="product-image-wrapper position-relative"
                              >
                                <img
                                  :src="getImageUrl(img.url)"
                                  alt="Ảnh sản phẩm"
                                  class="preview-image"
                                  @error="
                                    console.error(
                                      'Không load được ảnh:',
                                      getImageUrl(url)
                                    )
                                  "
                                />
                                <button
                                  type="button"
                                  class="delete-btn"
                                  @click.prevent="removeImage(index)"
                                  title="Xóa ảnh"
                                >
                                  <i class="fas fa-trash-alt"></i>
                                </button>
                              </div>

                              <!-- Ô chọn ảnh -->
                              <label class="image-upload-box">
                                <input
                                  type="file"
                                  accept="image/*"
                                  multiple
                                  @change="handleImageUpload"
                                  hidden
                                />
                                <span class="plus-icon">+</span>
                              </label>
                            </div>
                          </div>
                        </div>

                        <!-- Nút cập nhật -->
                        <div class="row mt-4">
                          <div class="col-12 d-flex justify-content-end">
                            <button type="submit" class="btn-update">
                              Cập nhật
                            </button>
                          </div>
                        </div>
                      </form>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Tổng thể */
body {
  background-color: #f8f9fa;
}

/* Card chứa nội dung */
.card {
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  border: none;
}

/* Nội dung bên trong card */
.card-body {
  padding: 20px;
}

/* Tiêu đề */
.form-label {
  font-weight: 600;
  color: #333;
}
select.form-select {
  color: #9ca3af; /* text-gray-400 */
}
select.form-select option {
  color: #000;
}
select.form-select:focus {
  border-color: #4f46e5; /* indigo-600 */
  box-shadow: 0 0 0 0.25rem rgb(99 102 241 / 0.5);
  color: #000;
}
label {
  font-size: 0.875rem;
  color: #111827; /* text-gray-900 */
  margin-bottom: 0.25rem;
}
.btn-purple {
  background-color: #5e72e4;
  color: white;
}
.btn-purple:hover {
  background-color: #3b1464;
  color: white;
}

/* Modal */
.update-content {
  background-color: #f3f4f6;
  font-family: Arial, sans-serif;
  font-size: 14px;
  line-height: 1.3;
  margin: 0;
  padding: 1rem;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
}
.modal-content {
  max-width: 900px;
  width: 100%;
  padding: 2rem 2.5rem;
  position: relative;
  background: #fff;
  border-radius: 0.375rem;
  box-shadow: 0 0.5rem 1rem rgb(0 0 0 / 0.15);
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}
.modal-header h5 {
  font-weight: 600;
  font-size: 15px;
  margin: 0;
}
.required-star {
  color: #dc2626;
  margin-right: 0.15rem;
}
label {
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
  display: block;
}
select.form-select,
input.form-control {
  font-size: 14px;
  padding: 0.5rem 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 0.375rem;
  color: #111827;
  height: 42px;
}
select.form-select:focus,
input.form-control:focus {
  border-color: #7c3aed;
  box-shadow: 0 0 0 0.2rem rgb(124 58 237 / 0.25);
  outline: none;
}
.qr-code {
  width: 120px;
  height: 120px;
  object-fit: contain;
  margin-top: 1.5rem;
}
.product-images {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
}
.product-image-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
  border: 1px solid #e5e7eb;
  border-radius: 0.375rem;
  overflow: hidden;
  background: #f9fafb;
  flex-shrink: 0;
}
.product-image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.delete-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  background: #fff;
  border-radius: 50%;
  padding: 2px 5px;
  color: #dc2626;
  border: none;
  cursor: pointer;
  font-size: 12px;
  line-height: 1;
  transition: background-color 0.2s ease;
}
.delete-btn:hover {
  background-color: #fee2e2;
}
.empty-image {
  width: 80px;
  height: 80px;
  border: 1px solid #e5e7eb;
  border-radius: 0.375rem;
  background: #f9fafb;
  flex-shrink: 0;
}
.btn-update {
  background-color: #5c3aed;
  color: #fff;
  font-weight: 600;
  font-size: 14px;
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  border: none;
  transition: background-color 0.2s ease;
  height: 42px;
  margin-top: 1.5rem;
  width: auto;
  min-width: 100px;
  align-self: flex-start;
}
.btn-update:hover,
.btn-update:focus {
  background-color: #4c2cd9;
  color: #fff;
  outline: none;
  box-shadow: 0 0 0 0.2rem rgb(124 58 237 / 0.5);
}
.close-btn {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: transparent;
  border: none;
  font-size: 18px;
  color: #6b7280;
  cursor: pointer;
  transition: color 0.2s ease;
  z-index: 10;
}
.close-btn:hover {
  color: #111827;
}
.form-column {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  height: 100%;
}
.image-section p {
  font-size: 13px;
  color: #111827;
  margin-bottom: 0.5rem;
  font-weight: 600;
}
@media (max-width: 767.98px) {
  .row-cols-3 {
    --bs-columns: 1 !important;
  }
  .btn-update {
    width: 100%;
    min-width: auto;
    align-self: stretch;
  }
  .qr-code {
    margin-top: 1rem;
    align-self: center;
  }
}

/* ảnh */
.image-upload-box {
  width: 80px;
  height: 80px;
  border: 2px dashed #ccc;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  transition: border-color 0.3s;
  flex-shrink: 0; /* Không bị co lại khi wrap */
}

.image-upload-box:hover {
  border-color: #999;
}

.plus-icon {
  font-size: 36px;
  color: #bbb;
  font-weight: bold;
}

.preview-image {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #ccc;
  flex-shrink: 0;
}

.delete-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  background: red;
  border: none;
  color: white;
  border-radius: 50%;
  padding: 4px 6px;
  font-size: 12px;
  cursor: pointer;
}

/* thông báo */
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

.modal-dialog-right {
  margin-left: auto; /* đẩy modal sang phải */
  margin-right: 30px; /* cách lề phải một chút */
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
  getComboboxData,
  getCTSPBySanPham,
  updateChiTietSanPham,
  uploadImage,
  deleteCTSP,
} from "@/services/RoleQuanLy/SanPhamService";

import { onMounted, ref, nextTick, computed } from "vue";
import { useRoute } from "vue-router";
import Swal from "sweetalert2";
import * as bootstrap from "bootstrap";

export default {
  setup() {
    const route = useRoute();
    const chiTietList = ref([]);
    const currentEditingCTSP = ref(null);
    let productModalInstance = null;
    const selectedFilters = ref({
      kichCoId: "",
      mauSacId: "",
      chatLieuId: "",
    });

    const notification = ref({
      show: false,
      message: "",
      type: "success", // 'success', 'error', v.v.
    });

    const progressWidth = ref(100);
    let progressTimer = null;

    const comboBoxData = ref({
      tenSanPham: [],
      tenMauSac: [],
      tenThuongHieu: [],
      tenChatLieu: [],
      tenXuatXu: [],
      tenSize: [],
    });

    const fetchComboBoxData = async () => {
      try {
        const res = await getComboboxData();
        const data = res.data;

        comboBoxData.value.tenSanPham = (data.tenSanPham || []).map((item) => ({
          ...item,
          id: String(item.id),
        }));
        comboBoxData.value.tenMauSac = (data.tenMauSac || []).map((item) => ({
          ...item,
          id: String(item.id),
        }));
        comboBoxData.value.tenThuongHieu = (data.tenThuongHieu || []).map(
          (item) => ({
            ...item,
            id: String(item.id),
          })
        );
        comboBoxData.value.tenChatLieu = (data.tenChatLieu || []).map(
          (item) => ({
            ...item,
            id: String(item.id),
          })
        );
        comboBoxData.value.tenXuatXu = (data.tenXuatXu || []).map((item) => ({
          ...item,
          id: String(item.id),
        }));
        comboBoxData.value.tenSize = (data.tenSize || []).map((item) => ({
          id: String(item.id),
          tenSize: item.tenSize || item.size || "",
        }));
      } catch (err) {
        console.error("Lỗi khi lấy dữ liệu combobox:", err);
      }
    };

    const fetchCTSPBySanPham = async (idSanPham) => {
      try {
        const res = await getCTSPBySanPham(idSanPham);
        chiTietList.value = res.data;
      } catch (err) {
        console.error("Lỗi khi lấy chi tiết sản phẩm:", err);
      }
    };

    // const getIdByTen = (list, ten) => {
    //   const found = list.find(
    //     (item) => item.tenSize === ten || item.tenMau === ten
    //   );
    //   return found ? String(found.id) : "";
    // };

    const getMauSacTen = (id) => {
      const found = comboBoxData.value.tenMauSac.find(
        (m) => m.id === String(id)
      );
      return found ? found.tenMau : "---";
    };

    const filteredChiTietList = computed(() => {
      return chiTietList.value.filter((item) => {
        const matchKichCo =
          !selectedFilters.value.kichCoId ||
          comboBoxData.value.tenSize.find(
            (s) =>
              s.id === selectedFilters.value.kichCoId &&
              s.tenSize === item.kichThuoc
          );

        const matchMauSac =
          !selectedFilters.value.mauSacId ||
          comboBoxData.value.tenMauSac.find(
            (m) =>
              m.id === selectedFilters.value.mauSacId &&
              m.tenMau === item.mauSac
          );

        const matchChatLieu =
          !selectedFilters.value.chatLieuId ||
          comboBoxData.value.tenChatLieu.find(
            (c) =>
              c.id === selectedFilters.value.chatLieuId &&
              c.tenChatLieu === item.chatLieu
          );

        return matchKichCo && matchMauSac && matchChatLieu;
      });
    });

    const resetFilters = () => {
      selectedFilters.value = {
        kichCoId: "",
        mauSacId: "",
        chatLieuId: "",
      };
    };

    const getImageUrl = (url) => {
      if (!url) return "";
      if (url.startsWith("http")) return url;
      if (url.startsWith("/uploads/")) return `http://localhost:8080${url}`;
      if (url.startsWith("san_pham/"))
        return `http://localhost:8080/uploads/${url}`;
      if (!url.includes("/"))
        return `http://localhost:8080/uploads/san_pham/${url}`;
      return `http://localhost:8080/uploads/${url}`;
    };

    const getSizeTen = (id) => {
      const found = comboBoxData.value.tenSize.find((s) => s.id === String(id));
      return found ? found.tenSize : "---";
    };

    const showNotification = (message, type = "success") => {
      notification.value = {
        show: true,
        message,
        type,
      };

      progressWidth.value = 100;

      if (progressTimer) {
        clearInterval(progressTimer);
      }

      progressTimer = setInterval(() => {
        progressWidth.value -= 2;
        if (progressWidth.value <= 0) {
          clearInterval(progressTimer);
          notification.value.show = false;
        }
      }, 50); // 2% mỗi 50ms => toast tồn tại khoảng 2.5s
    };

    const closeNotification = () => {
      notification.value.show = false;
      if (progressTimer) {
        clearInterval(progressTimer);
      }
    };

    const handleDeleteCTSP = async (ctspId) => {
      if (!ctspId) {
        console.error("Không tìm thấy CTSP để xoá.");
        return;
      }

      if (confirm("Bạn có chắc muốn xoá biến thể sản phẩm này?")) {
        try {
          await deleteCTSP(ctspId);
          showNotification("Xoá chi tiết sản phẩm thành công!", "success");
          await fetchCTSPBySanPham(route.params.id);
        } catch (error) {
          console.error("Lỗi khi xoá CTSP:", error);
          showNotification("Lỗi khi xoá chi tiết sản phẩm.", "error");
        }
      }
    };

    const editCTSP = async (id) => {
      const ctsp = chiTietList.value.find((c) => c.id === id);
      if (!ctsp) return;

      console.log("Full CTSP from API:", JSON.stringify(ctsp, null, 2));

      currentEditingCTSP.value = {
        id: ctsp.id,
        giaBan: ctsp.donGia ?? "",
        soLuong: ctsp.soLuong ?? "",
        hinhAnhs: ctsp.url ? [{ url: ctsp.url }] : [],
        idHinhAnh: ctsp.idHinhAnh ?? null,
        tenAnh: ctsp.tenAnh ?? null,

        tenSanPham: ctsp.tenSanPham || "",

        kichThuocId:
          comboBoxData.value.tenSize.find((s) => s.tenSize == ctsp.kichThuoc)
            ?.id || "",

        mauSacId:
          comboBoxData.value.tenMauSac.find((m) => m.tenMau === ctsp.mauSac)
            ?.id || "",

        chatLieuId:
          comboBoxData.value.tenChatLieu.find(
            (c) => c.tenChatLieu === ctsp.chatLieu
          )?.id || "",

        thuongHieuId:
          comboBoxData.value.tenThuongHieu.find(
            (t) => t.tenThuongHieu === ctsp.thuongHieu
          )?.id || "",

        xuatXuId:
          comboBoxData.value.tenXuatXu.find((x) => x.noiXuatXu === ctsp.xuatXu)
            ?.id || "",

        sanPhamId:
          comboBoxData.value.tenSanPham.find(
            (sp) => sp.tenSanPham === ctsp.tenSanPham
          )?.id || "",
      };

      console.log("currentEditingCTSP:", currentEditingCTSP.value);

      await nextTick();
      const modalEl = document.getElementById("productModal");
      if (modalEl) {
        productModalInstance =
          bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
        productModalInstance.show();
      }
    };

    // ảnh
    const removeImage = (index) => {
      currentEditingCTSP.value.hinhAnhs.splice(index, 1);

      if (currentEditingCTSP.value.hinhAnhs.length === 0) {
        currentEditingCTSP.value.idHinhAnh = null;
        currentEditingCTSP.value.tenAnh = null;
      }
    };

    const handleImageUpload = async (event) => {
      const files = event.target.files;
      if (!files || files.length === 0) return;

      for (const file of files) {
        try {
          const res = await uploadImage(file); // sử dụng hàm từ service
          const { url, id, tenAnh } = res.data;

          currentEditingCTSP.value.hinhAnhs.push({ url });
          currentEditingCTSP.value.idHinhAnh = id;
          currentEditingCTSP.value.tenAnh = tenAnh;

          console.log("Upload thành công:", res.data);
        } catch (err) {
          console.error("Lỗi khi upload ảnh:", err);
          if (err.response) {
            console.error("Phản hồi từ server:", err.response.data);
            alert("Lỗi từ máy chủ: " + JSON.stringify(err.response.data));
          } else {
            alert(
              "Không thể upload ảnh. Kiểm tra kết nối hoặc định dạng file."
            );
          }
        }
      }

      event.target.value = null; // reset input
    };

    const closeModal = () => {
      if (productModalInstance) {
        productModalInstance.hide();
        document.activeElement.blur();
      }
    };

    const updateCTSP = async () => {
      try {
        if (
          !currentEditingCTSP.value.id ||
          !currentEditingCTSP.value.sanPhamId ||
          !currentEditingCTSP.value.mauSacId ||
          !currentEditingCTSP.value.kichThuocId
        ) {
          alert("Thiếu ID bắt buộc (Sản phẩm, Màu sắc, Kích thước).");
          return;
        }

        const soLuong = Number(currentEditingCTSP.value.soLuong || 0);
        if (soLuong < 0 || soLuong > 1001) {
          showNotification("Số lượng phải từ 0 đến 1001!", "error");
          return;
        }

        const donGia = Number(currentEditingCTSP.value.giaBan || 0);
        if (donGia < 0 || donGia > 10000000) {
          showNotification("Đơn giá phải từ 0 đến 10,000,000!", "error");
          return;
        }

        const payload = {
          idSanPham: currentEditingCTSP.value.sanPhamId,
          idMauSac: currentEditingCTSP.value.mauSacId,
          idKichThuoc: currentEditingCTSP.value.kichThuocId,
          idHinhAnh: currentEditingCTSP.value.idHinhAnh || null,

          maCTSP: currentEditingCTSP.value.maCTSP || "",
          tenCTSP: currentEditingCTSP.value.tenCTSP || "",
          soLuong: soLuong,
          donGia: donGia,
          ghiChu: currentEditingCTSP.value.ghiChu || null,
          trangThai: currentEditingCTSP.value.trangThai ?? 1,
          tenAnh: currentEditingCTSP.value.tenAnh || null,
          url:
            currentEditingCTSP.value.hinhAnhs?.[0]?.url ||
            currentEditingCTSP.value.url ||
            null,
        };

        console.log("Payload gửi lên:", JSON.stringify(payload, null, 2));

        const result = await Swal.fire({
          title: "Xác nhận cập nhật?",
          text: "Bạn có chắc chắn muốn cập nhật chi tiết sản phẩm này không?",
          icon: "question",
          showCancelButton: true,
          confirmButtonText: "Cập nhật",
          cancelButtonText: "Huỷ",
          confirmButtonColor: "#3085d6",
          cancelButtonColor: "#6c757d",
        });

        if (!result.isConfirmed) return;

        const res = await updateChiTietSanPham(
          currentEditingCTSP.value.id,
          payload
        );

        console.log("Cập nhật CTSP thành công:", res.data);
        showNotification("Cập nhật chi tiết sản phẩm thành công!", "success");

        await fetchCTSPBySanPham(route.params.id);
        closeModal();
      } catch (err) {
        console.error("Lỗi khi cập nhật CTSP:", err);
        if (err.response) {
          alert("Lỗi từ máy chủ: " + JSON.stringify(err.response.data));
        } else {
          alert("Đã xảy ra lỗi khi kết nối server.");
        }
      }
    };

    onMounted(() => {
      const modalEl = document.getElementById("productModal");
      if (modalEl) {
        modalEl.addEventListener("hidden.bs.modal", () => {
          currentEditingCTSP.value = null;
        });
      }

      const idSanPham = route.params.id;
      if (idSanPham) {
        fetchCTSPBySanPham(idSanPham);
        fetchComboBoxData();
      }
    });

    return {
      chiTietList,
      currentEditingCTSP,
      editCTSP,
      comboBoxData,
      updateCTSP,
      closeModal,
      getMauSacTen,
      getSizeTen,
      removeImage,
      handleImageUpload,
      getImageUrl,
      handleDeleteCTSP,
      notification,
      progressWidth,
      showNotification,
      closeNotification,
      selectedFilters,
      filteredChiTietList,
      resetFilters,
    };
  },
};
</script>
