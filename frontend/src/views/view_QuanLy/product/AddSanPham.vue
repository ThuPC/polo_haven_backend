<template>
  <div class="card">
    <div class="card-body">
      <!-- Form Thêm Sản Phẩm -->
      <div class="section-box">
        <div class="row">
          <div class="container-fluid mt-4">
            <h4 class="mb-3">Thêm sản phẩm</h4>

            <!-- Tên áo -->
            <div class="mb-3">
              <label for="product-name-input" class="form-label">Tên áo</label>
              <input
                v-model="selectedTenSanPham"
                type="text"
                class="form-control"
                id="product-name-input"
                placeholder="Nhập tên áo..."
              />
            </div>

            <!-- Mô tả -->
            <div class="mb-3">
              <label for="mo-ta-input" class="form-label">Mô tả</label>
              <textarea
                v-model="moTaSanPham"
                class="form-control"
                id="mo-ta-input"
                rows="3"
                placeholder="Nhập mô tả sản phẩm..."
              ></textarea>
            </div>

            <!-- Thuộc tính: Thương hiệu, Chất liệu, Xuất xứ -->
            <div class="mb-3">
              <label class="form-label" style="font-size: 18px"
                >Thuộc tính</label
              >
              <div class="mt-2">
                <div class="row">
                  <!-- Thương hiệu -->
                  <div class="col-md-6 mb-3">
                    <label for="thuong-hieu" class="form-label"
                      >Thương hiệu</label
                    >
                    <div class="d-flex">
                      <select
                        v-model="selectedTenThuongHieu"
                        id="thuong-hieu"
                        class="form-select"
                      >
                        <option hidden value="">--Chọn--</option>
                        <option
                          v-for="item in tenThuongHieuList"
                          :key="item.id"
                          :value="item"
                        >
                          {{ item.tenThuongHieu }}
                        </option>
                      </select>
                      <button
                        class="btn ms-2"
                        type="button"
                        data-bs-toggle="modal"
                        data-bs-target="#themThuongHieuModal"
                        style="
                          width: 50px;
                          padding: 0;
                          margin-bottom: 0;
                          background-color: #2dce89;
                          color: #fff;
                          border: none;
                        "
                      >
                        +
                      </button>
                    </div>
                  </div>

                  <!-- Chất liệu -->
                  <div class="col-md-6 mb-3">
                    <label for="chat-lieu" class="form-label">Chất liệu</label>
                    <div class="d-flex">
                      <select
                        v-model="selectedTenChatLieu"
                        id="chat-lieu"
                        class="form-select"
                      >
                        <option hidden value="">--Chọn--</option>
                        <option
                          v-for="item in tenChatLieuList"
                          :key="item.id"
                          :value="item"
                        >
                          {{ item.tenChatLieu }}
                        </option>
                      </select>
                      <button
                        class="btn ms-2"
                        type="button"
                        data-bs-toggle="modal"
                        data-bs-target="#themChatLieuModal"
                        style="
                          width: 50px;
                          padding: 0;
                          margin-bottom: 0;
                          background-color: #2dce89;
                          color: #fff;
                          border: none;
                        "
                      >
                        +
                      </button>
                    </div>
                  </div>

                  <!-- Xuất xứ -->
                  <div class="col-md-6 mb-3">
                    <label for="xuat-xu" class="form-label">Xuất xứ</label>
                    <div class="d-flex">
                      <select
                        v-model="selectedTenXuatXu"
                        id="xuat-xu"
                        class="form-select"
                      >
                        <option hidden value="">--Chọn--</option>
                        <option
                          v-for="item in tenXuatXuList"
                          :key="item.id"
                          :value="item"
                        >
                          {{ item.noiXuatXu }}
                        </option>
                      </select>
                      <button
                        class="btn ms-2"
                        type="button"
                        data-bs-toggle="modal"
                        data-bs-target="#themXuatXuModal"
                        style="
                          width: 50px;
                          padding: 0;
                          margin-bottom: 0;
                          background-color: #2dce89;
                          color: #fff;
                          border: none;
                        "
                      >
                        +
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Nút hành động -->
            <div class="d-flex justify-content-end mt-4">
              <!-- <button class="btn btn-secondary me-2" @click="clearForm">
                      Hủy
                    </button> -->
              <button
                class="btn btn btn-success"
                @click="addProduct"
                style="width: 150px; margin-right: 40px"
              >
                Thêm
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Kích cỡ và Màu sắc -->
      <div class="section-box">
        <div class="row">
          <h4 class="mb-3">Thêm chi tiết sản phẩm</h4>
          <div class="mb-3">
            <label for="product-name" class="form-label">Tên sản phẩm</label>
            <div class="d-flex align-items-start gap-2">
              <!-- Ô input -->
              <div class="flex-grow-1 position-relative">
                <input
                  type="text"
                  class="form-control"
                  id="product-name"
                  v-model="selectedTenSanPhamText"
                  placeholder="Nhập tên sản phẩm"
                  @input="filterSuggestions"
                  @focus="showSuggestions = true"
                  @blur="hideSuggestions"
                  autocomplete="off"
                />
                <!-- Danh sách gợi ý -->
                <div
                  v-if="showSuggestions && filteredTenSanPham.length > 0"
                  class="autocomplete-list"
                >
                  <div
                    class="autocomplete-item"
                    v-for="(suggestion, index) in filteredTenSanPham"
                    :key="index"
                    @mousedown.prevent="selectSuggestion(suggestion)"
                  >
                    {{ suggestion.tenSanPham }}
                  </div>
                </div>
              </div>

              <!-- Nút dấu cộng -->
              <!-- <button
                      type="button"
                      class="btn btn-success"
                      title="Thêm sản phẩm mới"
                      data-bs-toggle="modal"
                      data-bs-target="#addProductModal"
                    >
                      <i class="fas fa-plus"></i>
                    </button> -->
            </div>
          </div>

          <!-- Kích cỡ -->
          <div class="col-md-6 mb-3">
            <label for="selectSize" class="form-label">Kích cỡ</label>
            <select
              id="selectSize"
              class="form-select"
              multiple
              v-model="selectedTenSize"
              style="width: 100%"
            >
              <option
                v-for="item in tenSizeList"
                :key="item.id"
                :value="item.id"
              >
                {{ item.size }}
              </option>
            </select>
          </div>

          <!-- Màu sắc -->
          <div class="col-md-6 mb-3">
            <label for="selectMauSac" class="form-label">Màu sắc</label>
            <select
              id="selectMauSac"
              class="form-select"
              multiple
              style="width: 100%"
            >
              <option
                v-for="item in tenMauSacList"
                :key="item.id"
                :value="item.id"
              >
                {{ item.tenMau }}
              </option>
            </select>
          </div>
        </div>
      </div>

      <!-- Modal Thêm Thương hiệu -->
      <div
        class="modal fade"
        id="themThuongHieuModal"
        tabindex="-1"
        aria-hidden="true"
      >
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">Thêm thương hiệu</h5>
              <button
                type="button"
                class="btn-close"
                data-bs-dismiss="modal"
                aria-label="Đóng"
              ></button>
            </div>
            <div class="modal-body">
              <label class="form-label">* Tên thương hiệu</label>
              <input
                v-model="newThuongHieu"
                type="text"
                class="form-control"
                placeholder="Nhập thương hiệu"
              />
            </div>
            <div class="modal-footer">
              <button
                type="button"
                class="btn"
                style="background: #2dce89; color: #fff"
                @click="xacNhanThuongHieu"
                data-bs-dismiss="modal"
              >
                Xác nhận
              </button>
              <button
                type="button"
                class="btn btn-secondary"
                data-bs-dismiss="modal"
              >
                Hủy
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Modal Thêm Chất liệu -->
      <div
        class="modal fade"
        id="themChatLieuModal"
        tabindex="-1"
        aria-hidden="true"
      >
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">Thêm chất liệu</h5>
              <button
                type="button"
                class="btn-close"
                data-bs-dismiss="modal"
                aria-label="Đóng"
              ></button>
            </div>
            <div class="modal-body">
              <label class="form-label">* Tên chất liệu</label>
              <input
                v-model="newChatLieu"
                type="text"
                class="form-control"
                placeholder="Nhập chất liệu"
              />
            </div>
            <div class="modal-footer">
              <button
                type="button"
                class="btn"
                style="background: #2dce89; color: #fff"
                @click="xacNhanChatLieu"
                data-bs-dismiss="modal"
              >
                Xác nhận
              </button>
              <button
                type="button"
                class="btn btn-secondary"
                data-bs-dismiss="modal"
              >
                Hủy
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Modal Thêm Xuất xứ -->
      <div
        class="modal fade"
        id="themXuatXuModal"
        tabindex="-1"
        aria-hidden="true"
      >
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">Thêm xuất xứ</h5>
              <button
                type="button"
                class="btn-close"
                data-bs-dismiss="modal"
                aria-label="Đóng"
              ></button>
            </div>
            <div class="modal-body">
              <label class="form-label">* Nơi xuất xứ</label>
              <input
                v-model="newXuatXu"
                type="text"
                class="form-control"
                placeholder="Nhập nơi xuất xứ"
              />
            </div>
            <div class="modal-footer">
              <button
                type="button"
                class="btn"
                style="background: #2dce89; color: #fff"
                @click="xacNhanXuatXu"
                data-bs-dismiss="modal"
              >
                Xác nhận
              </button>
              <button
                type="button"
                class="btn btn-secondary"
                data-bs-dismiss="modal"
              >
                Hủy
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Modal add Sp -->
      <div
        class="modal fade"
        id="addProductModal"
        tabindex="-1"
        aria-labelledby="addProductModalLabel"
        aria-hidden="true"
        ref="addProductModal"
      >
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content rounded-3">
            <div class="modal-header">
              <h5 class="modal-title fw-semibold" id="addProductModalLabel">
                Thêm áo
              </h5>
              <button
                type="button"
                class="btn-close"
                data-bs-dismiss="modal"
                aria-label="Close"
              ></button>
            </div>

            <form @submit.prevent="addProduct">
              <div class="modal-body">
                <div class="mb-3">
                  <label for="name" class="form-label fw-semibold"
                    ><span class="text-danger">*</span> Tên áo</label
                  >
                  <input
                    type="text"
                    class="form-control"
                    id="name"
                    placeholder="Nhập tên áo..."
                    required
                  />
                </div>
                <div class="mb-3">
                  <label for="description" class="form-label fw-semibold"
                    ><span class="text-danger">*</span> Mô tả</label
                  >
                  <input
                    type="text"
                    class="form-control"
                    id="description"
                    placeholder="Nhập mô tả..."
                    required
                  />
                </div>
              </div>
              <div class="modal-footer">
                <button
                  type="submit"
                  class="btn btn-success d-flex align-items-center gap-2"
                >
                  <i class="fas fa-plus"></i> Thêm
                </button>
              </div>
            </form>
          </div>
        </div>
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

      <!-- Gen spct -->
      <div v-if="showVariantsTable">
        <div
          v-for="(variants, color) in groupedVariants"
          :key="color"
          class="mb-4 border rounded shadow-sm"
        >
          <!-- Tiêu đề nhóm màu -->
          <div
            class="bg-light px-3 py-2 fw-semibold text-uppercase small border-bottom"
          >
            Các sản phẩm màu {{ color }}
          </div>

          <!-- Hiển thị ảnh xem trước -->
          <div
            v-if="
              uploadedImageUrls &&
              uploadedImageUrls[normalizeRef(color)] &&
              uploadedImageUrls[normalizeRef(color)].length > 0
            "
            class="px-3 py-2 border-bottom bg-white d-flex flex-wrap gap-2"
          >
            <div
              v-for="(url, index) in uploadedImageUrls[normalizeRef(color)]"
              :key="index"
              class="position-relative"
              style="width: 64px; height: 64px"
            >
              <!-- Ảnh -->
              <img
                :src="getImageUrl(url)"
                alt="Ảnh sản phẩm"
                width="64"
                height="64"
                class="rounded shadow-sm"
              />

              <!-- Nút xoá ảnh -->
              <button
                class="btn btn-sm btn-danger position-absolute top-0 end-0 d-flex align-items-center justify-content-center"
                style="
                  width: 18px;
                  height: 18px;
                  padding: 0;
                  font-size: 12px;
                  border-radius: 50%;
                  transform: translate(30%, -30%);
                  line-height: 1;
                "
                @click="removeImage(color, index)"
              >
                ×
              </button>
            </div>
          </div>

          <!-- Bảng sản phẩm -->
          <div class="table-responsive">
            <table
              class="table table-bordered table-sm text-center align-middle mb-0"
            >
              <thead class="table-light">
                <tr>
                  <th><input type="checkbox" /></th>
                  <th>Sản phẩm</th>
                  <th>Số lượng</th>
                  <th>Đơn giá</th>
                  <th>Mô tả</th>
                  <th>Hành động</th>
                  <th>Ảnh</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(variant, index) in variants" :key="index">
                  <td><input type="checkbox" /></td>
                  <td
                    :title="`${variant.tenSanPham} [${getTenMau(variant.mauSac)} - ${getTenSize(variant.kichCo)}]`"
                  >
                    {{
                      `${variant.tenSanPham} [${getTenMau(variant.mauSac)} - ${getTenSize(variant.kichCo)}]`
                    }}
                  </td>
                  <td>
                    <input
                      type="number"
                      v-model="variant.soLuong"
                      class="form-control form-control-sm text-center"
                    />
                  </td>
                  <td>
                    <input
                      type="text"
                      v-model="variant.giaBan"
                      class="form-control form-control-sm text-center"
                    />
                  </td>
                  <td class="text-center">
                    {{ variant.moTa || "---" }}
                  </td>
                  <td>
                    <button
                      class="btn btn-sm btn-outline-danger"
                      @click="deleteVariant(variant)"
                    >
                      <i class="fas fa-trash-alt"></i>
                    </button>
                  </td>

                  <!-- Icon upload ảnh và input file ẩn -->
                  <td
                    v-show="index === 0"
                    :rowspan="variants.length"
                    class="align-middle"
                  >
                    <!-- ICON ảnh -->
                    <i
                      class="fas fa-image text-primary"
                      style="cursor: pointer; font-size: 1.2rem"
                      @click="
                        triggerColorImageInput(
                          typeof variant.mauSac === 'object'
                            ? variant.mauSac.tenMau
                            : variant.mauSac
                        )
                      "
                    ></i>

                    <!-- INPUT FILE -->
                    <input
                      type="file"
                      accept="image/*"
                      multiple
                      class="d-none"
                      :ref="
                        (el) => {
                          if (el) {
                            const colorName =
                              typeof variant.mauSac === 'object'
                                ? variant.mauSac.tenMau
                                : variant.mauSac;
                            const key = normalizeRef(colorName);
                            colorFileRefs[key] = el;
                          }
                        }
                      "
                      @change="handleImageUpload($event, variant.mauSac)"
                    />
                  </td>
                </tr>

                <tr v-if="variants.length === 0">
                  <td colspan="12" class="text-center text-muted py-4">
                    <img
                      src="https://storage.googleapis.com/a1aa/image/9a5bd5c7-9567-42f7-f99d-2886399a0be6.jpg"
                      alt="Không có ảnh"
                      width="64"
                      class="mb-2"
                    />
                    <div>Không có ảnh</div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Nút thêm sản phẩm -->
        <div class="text-end mt-3">
          <button @click="submitVariants" class="btn btn-primary btn-sm">
            <i class="fas fa-plus-circle me-1"></i>
            Thêm sản phẩm
          </button>
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

.input-group {
  display: flex;
  align-items: center;
  justify-content: center;
}

.input-group #product-name {
  padding: 10px;
  border-radius: 8px;
  border: 1px solid #2dce89;
  width: 1200px;
  margin-right: 20px;
}

.input-group #product-name:focus {
  outline: none;
}

.input-group .btn {
  height: 40px;
  width: 45px;
  margin-bottom: 0;
  background-color: #2dce89;
  border-color: #2dce89;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 5px;
}

/* Dropdown và Input */
.form-select,
.form-control {
  border: 1px solid #ccc;
  border-radius: 6px;
  padding: 10px;
  font-size: 14px;
}

.form-select:focus,
.form-control:focus {
  border-color: #2dce89;
  box-shadow: 0 0 5px rgba(45, 206, 137, 0.3);
}

/* Link "Danh sách sản phẩm" */
.text-primary {
  color: #2dce89 !important;
  font-weight: 600;
}

.text-primary:hover {
  text-decoration: underline;
}

#selectMauSac {
  height: 330px !important; /* Bắt buộc áp dụng chiều cao */
  overflow-y: auto; /* Thêm thanh cuộn nếu danh sách quá dài */
}

#selectMauSac option {
  padding: 5px;
  margin: 0;
  background-color: white;
}

::v-deep(
  .select2-container--default
    .select2-selection--multiple
    .select2-selection__clear
) {
  margin: 0 10px 5px !important;
}

::v-deep(
  .select2-container--default
    .select2-selection--multiple
    .select2-selection__choice__remove
) {
  left: 1px !important;
  top: 2.5px !important;
  border-radius: 10px !important;
}

/* Giữ nguyên chiều cao */
::v-deep(.select2-container--default .select2-selection--multiple) {
  padding: 5px;
  display: flex;
  flex-wrap: nowrap; /* Không cho xuống dòng */
  gap: 5px;
  min-height: 34px; /* Giữ nguyên chiều cao */
  align-items: center;
  overflow-x: auto; /* Cuộn ngang nếu quá dài */
  overflow-y: hidden;
}

/* Loại bỏ margin và padding thừa */
::v-deep(.select2-selection__rendered) {
  margin: 0 !important;
  padding: 0 !important;
  display: flex;
  align-items: center;
}

/* Điều chỉnh ô tìm kiếm */
::v-deep(.select2-search__field) {
  margin: 0 !important;
  width: 120px !important;
  height: 24px !important;
  display: flex;
  align-items: center;
}

::v-deep(.select2-search.select2-search--inline) {
  align-items: center;
}

/* Các tag được chọn */
::v-deep(.select2-selection__choice) {
  background: #28a745 !important; /* Màu xanh lá */
  color: white !important;
  border-radius: 4px;
  padding: 3px 8px;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  white-space: nowrap; /* Ngăn xuống dòng */
  height: 26px;
  margin: 0 !important;
}

/* Căn giữa dấu 'x' */
::v-deep(.select2-selection__choice__remove) {
  background-color: #218838 !important; /* Màu xanh lá đậm */
  color: white !important;
  font-weight: bold;
  cursor: pointer;
  border: none;
  border-radius: 50%; /* Bo tròn */
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  text-align: center;
  padding: 0;
  margin-right: 4px;
}

/* Dấu tích tích có màu xanh và căn phải */
.checkmark {
  float: right;
  color: green;
  font-weight: bold;
}

/* css list gợi ý */
.autocomplete-list {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 1050;
  background-color: #fff;
  border: 1px solid #dee2e6;
  border-top: none;
  border-radius: 0 0 0.5rem 0.5rem;
  max-height: 200px;
  overflow-y: auto;
  box-shadow: 0 0.25rem 0.5rem rgba(0, 0, 0, 0.1);
}

.autocomplete-item {
  padding: 0.5rem 1rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.autocomplete-item:hover {
  background-color: #f8f9fa;
  font-weight: 500;
}

.autocomplete-item:active {
  background-color: #e9ecef;
}

/* Mũi tên chỉ xuống */
.arrow-icon {
  position: absolute;
  top: 50%;
  right: 1rem;
  transform: translateY(-50%);
  pointer-events: none;
  color: #6c757d;
  font-size: 0.9rem;
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

.section-box {
  background-color: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb; /* light gray border */
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
  padding: 24px;
  margin-bottom: 32px;
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
  ref,
  onMounted,
  nextTick,
  watch,
  computed,
  getCurrentInstance,
  reactive,
} from "vue";
import { useRouter } from "vue-router";

import $ from "jquery";
import "select2/dist/css/select2.min.css";
import "select2";
import Swal from "sweetalert2";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import { Modal } from "bootstrap";

import {
  createSanPham,
  getComboboxData,
  uploadImage,
  createChiTietSanPham as batchCreateCTSP,
} from "@/services/RoleQuanLy/SanPhamService";
import {
  createThuongHieu,
  getAllThuongHieu,
} from "@/services/RoleQuanLy/ThuongHieuService";
import {
  createChatLieu,
  getAllChatLieu,
} from "@/services/RoleQuanLy/ChatLieuService";
import {
  createXuatXu,
  getAllXuatXu,
} from "@/services/RoleQuanLy/XuatXuService";

export default {
  setup() {
    const router = useRouter();
    const { proxy } = getCurrentInstance();
    const refs = proxy.$refs;

    const tenSanPhamList = ref([]);
    const selectedTenSanPhamText = ref("");
    const selectedSanPhamObj = ref(null);
    const filteredTenSanPham = ref([]);
    const showSuggestions = ref(false);

    const tenMauSacList = ref([]);
    const selectedTenMauSac = ref([]);

    const tenSizeList = ref([]);
    const selectedTenSize = ref([]);

    const tenThuongHieuList = ref([]);
    const selectedTenThuongHieu = ref("");
    const newThuongHieu = ref("");

    const tenXuatXuList = ref([]);
    const selectedTenXuatXu = ref("");
    const newXuatXu = ref("");

    const tenChatLieuList = ref([]);
    const selectedTenChatLieu = ref("");
    const newChatLieu = ref("");

    const variantsList = ref([]);
    const showVariantsTable = ref(false);

    const groupImages = ref({});
    const uploadedImageUrls = reactive({});
    const colorFileRefs = reactive({});

    const selectedTenSanPham = ref("");
    const moTaSanPham = ref("");
    const notification = ref({ show: false, message: "", type: "success" });

    const addProductModalRef = ref(null);

    const deletedCombinations = ref([]);

    const groupedVariants = computed(() => {
      const groups = {};
      variantsList.value.forEach((variant) => {
        const colorKey =
          typeof variant.mauSac === "object"
            ? variant.mauSac.tenMau
            : variant.mauSac;

        if (!groups[colorKey]) {
          groups[colorKey] = [];
        }

        groups[colorKey].push(variant);
      });
      return groups;
    });

    const filterSuggestions = () => {
      const query = selectedTenSanPhamText.value.toLowerCase();
      filteredTenSanPham.value = tenSanPhamList.value.filter((item) =>
        item.tenSanPham.toLowerCase().includes(query)
      );
      showSuggestions.value = true;
    };

    const selectSuggestion = (suggestion) => {
      selectedTenSanPhamText.value = suggestion.tenSanPham;
      selectedSanPhamObj.value = suggestion;
      showSuggestions.value = false;
      generateVariantsList();
    };

    const hideSuggestions = () => {
      setTimeout(() => {
        showSuggestions.value = false;
      }, 200);
    };

    const generateVariantsList = () => {
      const colors = selectedTenMauSac.value;
      const sizes = selectedTenSize.value;

      if (!selectedSanPhamObj.value) {
        console.warn("Chưa chọn sản phẩm hợp lệ!");
        return;
      }

      const idSanPham = selectedSanPhamObj.value.id;
      const tenSanPham = selectedSanPhamObj.value.tenSanPham;

      const newVariants = [];

      colors.forEach((mau) => {
        const colorObj =
          typeof mau === "object"
            ? mau
            : tenMauSacList.value.find((m) => m.id === mau) || {
                id: mau,
                tenMau: mau,
              };

        sizes.forEach((size) => {
          const sizeObj =
            typeof size === "object"
              ? size
              : tenSizeList.value.find((s) => s.id === size) || {
                  id: size,
                  tenSize: size,
                };

          console.log("selectedSanPhamObj:", selectedSanPhamObj.value);

          newVariants.push({
            idSanPham,
            tenSanPham,
            mauSac: colorObj,
            kichCo: sizeObj,
            chatLieu: selectedTenChatLieu.value || "---",
            xuatXu: selectedTenXuatXu.value || "---",
            thuongHieu: selectedTenThuongHieu.value || "---",
            soLuong: 10,
            giaBan: "",
            moTa: selectedSanPhamObj.value.moTa || "",
            trangThai: 1,
            tenAnh: null,
            idHinhAnh: null,
          });
        });
      });

      variantsList.value = newVariants;
      showVariantsTable.value = variantsList.value.length > 0;

      console.log("Generated variants:");
      console.table(
        variantsList.value.map((v) => ({
          tenSanPham: v.tenSanPham,
          mau: v.mauSac?.tenMau || v.mauSac,
          size: v.kichCo?.tenSize || v.kichCo,
          moTa: v.moTa,
        }))
      );
    };

    const getImageUrl = (url) => {
      if (!url) return ""; // fallback tránh lỗi null
      if (url.startsWith("http")) return url;
      if (url.startsWith("/uploads/")) return `http://localhost:8080${url}`;
      if (url.startsWith("san_pham/"))
        return `http://localhost:8080/uploads/${url}`;
      return `http://localhost:8080/uploads/san_pham/${url}`;
    };

    const extractArray = (res) => {
      if (!res) return [];
      const payload = res.data ?? res;
      if (Array.isArray(payload)) return payload;
      if (Array.isArray(payload.data)) return payload.data;
      if (Array.isArray(payload.content)) return payload.content;
      return [];
    };

    const closeModal = (modalId) => {
      const modalEl = document.getElementById(modalId);
      if (!modalEl) return; // không tồn tại

      const modal =
        Modal.getInstance(modalEl) || Modal.getOrCreateInstance(modalEl);
      try {
        modal.hide();
      } catch (err) {
        console.warn(`Modal ${modalId} đã đóng hoặc không tồn tại.`, err);
      }
    };

    const loadThuongHieuList = async () => {
      try {
        const res = await getAllThuongHieu();
        tenThuongHieuList.value = extractArray(res);
      } catch (err) {
        console.error("Lỗi load thương hiệu:", err);
        tenThuongHieuList.value = [];
      }
    };

    const loadChatLieuList = async () => {
      try {
        const res = await getAllChatLieu();
        tenChatLieuList.value = extractArray(res);
      } catch (err) {
        console.error("Lỗi load chất liệu:", err);
        tenChatLieuList.value = [];
      }
    };

    const loadXuatXuList = async () => {
      try {
        const res = await getAllXuatXu();
        tenXuatXuList.value = extractArray(res);
      } catch (err) {
        console.error("Lỗi load xuất xứ:", err);
        tenXuatXuList.value = [];
      }
    };

    // Thêm nhanh thương hiệu
    const xacNhanThuongHieu = async () => {
      const ten = (newThuongHieu.value ?? "").toString().trim();
      if (!ten) {
        showNotification("Vui lòng nhập tên thương hiệu!", "error");
        return;
      }
      try {
        const res = await createThuongHieu(ten);
        const added = res?.data?.data ?? res?.data ?? res;

        await loadThuongHieuList();

        const match =
          tenThuongHieuList.value.find(
            (item) =>
              String(item.id) === String(added.id) ||
              item.tenThuongHieu === added.tenThuongHieu
          ) ?? added;

        selectedTenThuongHieu.value = match;
        newThuongHieu.value = "";
        closeModal("themThuongHieuModal");
        showNotification("Thêm thương hiệu thành công!", "success");
      } catch (err) {
        console.error("Lỗi thêm thương hiệu:", err);
        showNotification("Thương hiệu đã tồn tại hoặc có lỗi xảy ra!", "error");
      }
    };

    //Thêm nhanh chất liệu
    const xacNhanChatLieu = async () => {
      const ten = (newChatLieu.value ?? "").toString().trim();
      if (!ten) {
        showNotification("Vui lòng nhập tên chất liệu!", "error");
        return;
      }
      try {
        const res = await createChatLieu({ tenChatLieu: ten });
        const added = res?.data?.data ?? res?.data ?? res;

        await loadChatLieuList();

        const match =
          tenChatLieuList.value.find(
            (item) =>
              String(item.id) === String(added.id) ||
              item.tenChatLieu === added.tenChatLieu
          ) ?? added;

        selectedTenChatLieu.value = match;
        newChatLieu.value = "";
        closeModal("themChatLieuModal");
        showNotification("Thêm chất liệu thành công!", "success");
      } catch (err) {
        console.error("Lỗi thêm chất liệu:", err);
        showNotification("Chất liệu đã tồn tại hoặc có lỗi xảy ra!", "error");
      }
    };

    //Thêm nhanh xuất xứ
    const xacNhanXuatXu = async () => {
      const ten = (newXuatXu.value ?? "").toString().trim();
      if (!ten) {
        showNotification("Vui lòng nhập tên xuất xứ!", "error");
        return;
      }
      try {
        const res = await createXuatXu(ten);
        const added = res?.data?.data ?? res?.data ?? res;

        await loadXuatXuList();

        const match =
          tenXuatXuList.value.find(
            (item) =>
              String(item.id) === String(added.id) ||
              item.noiXuatXu === added.noiXuatXu
          ) ?? added;

        selectedTenXuatXu.value = match;
        newXuatXu.value = "";
        closeModal("themXuatXuModal");
        showNotification("Thêm xuất xứ thành công!", "success");
      } catch (err) {
        console.error("Lỗi thêm xuất xứ:", err);
        showNotification("Xuất xứ đã tồn tại hoặc có lỗi xảy ra!", "error");
      }
    };

    // const addProduct = async () => {
    //   if (
    //     !selectedTenSanPham.value.trim() ||
    //     !selectedTenThuongHieu.value ||
    //     !selectedTenChatLieu.value ||
    //     !selectedTenXuatXu.value
    //   ) {
    //     showNotification("Vui lòng điền đầy đủ thông tin!", "error");
    //     return;
    //   }

    //   // Kiểm tra trùng
    //   const isDuplicate = tenSanPhamList.value.some(
    //     (sp) =>
    //       sp.tenSanPham?.trim().toLowerCase() ===
    //         selectedTenSanPham.value.trim().toLowerCase() &&
    //       sp.thuongHieu?.id === selectedTenThuongHieu.value.id &&
    //       sp.chatLieu?.id === selectedTenChatLieu.value.id &&
    //       sp.xuatXu?.id === selectedTenXuatXu.value.id
    //   );

    //   if (isDuplicate) {
    //     showNotification(
    //       "Sản phẩm với tên và thuộc tính này đã tồn tại!",
    //       "warning"
    //     );
    //     return;
    //   }

    //   const confirmed = window.confirm("Bạn có chắc chắn muốn thêm sản phẩm?");
    //   if (!confirmed) return;

    //   const formData = new FormData();
    //   formData.append("tenSanPham", selectedTenSanPham.value.trim());
    //   formData.append("moTa", moTaSanPham.value.trim());
    //   formData.append("chatLieuId", selectedTenChatLieu.value.id);
    //   formData.append("xuatXuId", selectedTenXuatXu.value.id);
    //   formData.append("thuongHieuId", selectedTenThuongHieu.value.id);
    //   formData.append("trangThai", "1");

    //   try {
    //     const res = await createSanPham(formData);
    //     console.log("Thêm sản phẩm thành công:", res.data);

    //     // Reset form
    //     selectedTenSanPham.value = "";
    //     moTaSanPham.value = "";
    //     selectedTenThuongHieu.value = "";
    //     selectedTenChatLieu.value = "";
    //     selectedTenXuatXu.value = "";

    //     await nextTick();

    //     if (addProductModalRef.value) {
    //       const modal =
    //         Modal.getInstance(addProductModalRef.value) ||
    //         new Modal(addProductModalRef.value);
    //       modal.hide();

    //       setTimeout(() => {
    //         document.body.classList.remove("modal-open");
    //         const backdrop = document.querySelector(".modal-backdrop");
    //         if (backdrop) backdrop.remove();
    //       }, 300);
    //     }

    //     await fetchAllData();
    //     showNotification("Đã thêm sản phẩm thành công!", "success");
    //   } catch (err) {
    //     console.error("Lỗi khi thêm sản phẩm:", err);
    //     showNotification("Sản phẩm đã tồn tại!", "error");
    //   }
    // };

    const addProduct = async () => {
      if (
        !selectedTenSanPham.value.trim() ||
        !selectedTenThuongHieu.value ||
        !selectedTenChatLieu.value ||
        !selectedTenXuatXu.value
      ) {
        showNotification("Vui lòng điền đầy đủ thông tin!", "error");
        return;
      }

      // Kiểm tra trùng
      const isDuplicate = tenSanPhamList.value.some(
        (sp) =>
          sp.tenSanPham?.trim().toLowerCase() ===
            selectedTenSanPham.value.trim().toLowerCase() &&
          sp.thuongHieu?.id === selectedTenThuongHieu.value.id &&
          sp.chatLieu?.id === selectedTenChatLieu.value.id &&
          sp.xuatXu?.id === selectedTenXuatXu.value.id
      );

      if (isDuplicate) {
        showNotification(
          "Sản phẩm với tên và thuộc tính này đã tồn tại!",
          "warning"
        );
        return;
      }

      const result = await Swal.fire({
        title: "Xác nhận thêm?",
        text: "Bạn có chắc chắn muốn thêm sản phẩm mới không?",
        icon: "question",
        showCancelButton: true,
        confirmButtonText: "Thêm",
        cancelButtonText: "Huỷ",
        confirmButtonColor: "#3085d6",
        cancelButtonColor: "#6c757d",
      });

      if (!result.isConfirmed) return;

      const formData = new FormData();
      formData.append("tenSanPham", selectedTenSanPham.value.trim());
      formData.append("moTa", moTaSanPham.value.trim());
      formData.append("chatLieuId", selectedTenChatLieu.value.id);
      formData.append("xuatXuId", selectedTenXuatXu.value.id);
      formData.append("thuongHieuId", selectedTenThuongHieu.value.id);
      formData.append("trangThai", "1");

      try {
        const res = await createSanPham(formData);
        console.log("Thêm sản phẩm thành công:", res.data);

        const newSanPham = res.data?.data ?? res.data;
        if (newSanPham) {
          // Đưa sản phẩm mới lên đầu combobox
          tenSanPhamList.value.unshift(newSanPham);
          filteredTenSanPham.value = [...tenSanPhamList.value];
        }

        // Reset form
        selectedTenSanPham.value = "";
        moTaSanPham.value = "";
        selectedTenThuongHieu.value = "";
        selectedTenChatLieu.value = "";
        selectedTenXuatXu.value = "";

        await nextTick();

        if (addProductModalRef.value) {
          const modal =
            Modal.getInstance(addProductModalRef.value) ||
            new Modal(addProductModalRef.value);
          modal.hide();

          setTimeout(() => {
            document.body.classList.remove("modal-open");
            const backdrop = document.querySelector(".modal-backdrop");
            if (backdrop) backdrop.remove();
          }, 300);
        }
        showNotification("Đã thêm sản phẩm thành công!", "success");
      } catch (err) {
        console.error("Lỗi khi thêm sản phẩm:", err);
        showNotification("Sản phẩm đã tồn tại!", "error");
      }
    };

    const progressWidth = ref(100);
    let progressInterval = null;

    const showNotification = (message, type = "success") => {
      notification.value.message = message;
      notification.value.type = type;
      notification.value.show = true;
      progressWidth.value = 100;

      if (progressInterval) clearInterval(progressInterval);
      progressInterval = setInterval(() => {
        if (progressWidth.value > 0) {
          progressWidth.value -= 1.5;
        } else {
          clearInterval(progressInterval);
          notification.value.show = false;
        }
      }, 50);
    };

    const closeNotification = () => {
      notification.value.show = false;
      if (progressInterval) clearInterval(progressInterval);
    };

    const handleImageUpload = async (event, mauSacValue) => {
      const files = event.target.files;
      if (!files || files.length === 0) return;

      const colorName =
        typeof mauSacValue === "object" ? mauSacValue.tenMau : mauSacValue;
      const normalizedColor = normalizeRef(colorName);

      if (!uploadedImageUrls[normalizedColor]) {
        uploadedImageUrls[normalizedColor] = [];
      }

      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        const localUrl = URL.createObjectURL(file);
        uploadedImageUrls[normalizedColor].push(localUrl);

        try {
          const res = await uploadImage(file);
          const { id, tenAnh, url } = res.data;
          console.log("URL ảnh trả về từ server:", url);

          variantsList.value
            .filter(
              (v) =>
                normalizeRef(v.mauSac.tenMau || v.mauSac) === normalizedColor
            )
            .forEach((v) => {
              v.idHinhAnh = id;
              v.tenAnh = tenAnh;
              v.urlAnh = url;
            });

          uploadedImageUrls[normalizedColor].splice(
            uploadedImageUrls[normalizedColor].length - 1,
            1,
            url
          );
        } catch (err) {
          console.error("Upload ảnh thất bại:", err);
        }
      }
    };

    const normalizeRef = (str) =>
      String(str || "")
        .toLowerCase()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .replace(/\s+/g, "_")
        .replace(/[^\w-]/g, "");

    const triggerColorImageInput = (color) => {
      const colorName = typeof color === "object" ? color.tenMau : color;
      const normalized = normalizeRef(colorName);
      const input = colorFileRefs[normalized];
      if (input) input.click();
    };

    const triggerFileInput = (variant) => {
      const refName = `fileInput_${variant.mauSac}_${variant.kichCo}`;
      const input = refs[refName];
      if (input) input.click();
    };

    const handleImageUploadInTable = async (event, variant) => {
      const file = event.target.files[0];
      if (!file) return;

      try {
        const res = await uploadImage(file);
        const { id, tenAnh } = res.data;

        const matched = variantsList.value.find(
          (v) =>
            v.idSanPham === variant.idSanPham &&
            v.mauSac.id === variant.mauSac.id &&
            v.kichCo.id === variant.kichCo.id
        );

        if (matched) {
          matched.idHinhAnh = id;
          matched.tenAnh = tenAnh;
        }
      } catch (err) {
        console.error("Lỗi upload ảnh:", err);
      }
    };

    const deleteVariant = (variant) => {
      const idSanPham = variant.idSanPham;
      const idMauSac =
        typeof variant.mauSac === "object" ? variant.mauSac.id : variant.mauSac;
      const idKichCo =
        typeof variant.kichCo === "object" ? variant.kichCo.id : variant.kichCo;

      deletedCombinations.value.push({
        idSanPham,
        idMauSac,
        idKichCo,
      });

      variantsList.value = variantsList.value.filter((v) => {
        const vMauId = typeof v.mauSac === "object" ? v.mauSac.id : v.mauSac;
        const vSizeId = typeof v.kichCo === "object" ? v.kichCo.id : v.kichCo;
        return !(
          v.idSanPham === idSanPham &&
          vMauId === idMauSac &&
          vSizeId === idKichCo
        );
      });

      if (variantsList.value.length === 0) {
        showVariantsTable.value = false;
      }
    };

    const showImage = (variant) => {
      console.log("Hiển thị ảnh:", variant);
    };

    const submitVariants = async () => {
      if (variantsList.value.length === 0) {
        showNotification("Chưa có sản phẩm chi tiết nào để thêm.", "danger");
        return;
      }

      for (const variant of variantsList.value) {
        const giaBanStr = variant.giaBan?.toString().replace(/,/g, "");
        const soLuongStr = variant.soLuong?.toString().trim();

        const donGia = Number(giaBanStr);
        const soLuong = Number(soLuongStr);

        if (!giaBanStr || isNaN(donGia) || donGia <= 0) {
          showNotification(
            `Giá bán không hợp lệ cho biến thể: ${variant.tenSanPham} - ${getTenMau(
              variant.mauSac
            )} - ${getTenSize(variant.kichCo)}`
          );
          return;
        }

        if (!soLuongStr || isNaN(soLuong) || soLuong <= 0) {
          showNotification(
            `Số lượng không hợp lệ cho biến thể: ${variant.tenSanPham} - ${getTenMau(
              variant.mauSac
            )} - ${getTenSize(variant.kichCo)}`
          );
          return;
        }
      }

      const result = await Swal.fire({
        title: "Xác nhận thêm?",
        text: "Bạn có chắc chắn muốn thêm các sản phẩm chi tiết này?",
        icon: "question",
        showCancelButton: true,
        confirmButtonText: "Thêm",
        cancelButtonText: "Huỷ",
        confirmButtonColor: "#3085d6",
        cancelButtonColor: "#6c757d",
      });
      if (!result.isConfirmed) return;

      const groupedMap = {};

      variantsList.value.forEach((v) => {
        const idSanPham = v.idSanPham;
        const idMauSac = typeof v.mauSac === "object" ? v.mauSac.id : v.mauSac;
        const idKichThuoc =
          typeof v.kichCo === "object" ? v.kichCo.id : v.kichCo;
        const key = `${idSanPham}-${idMauSac}-${idKichThuoc}`;

        if (!groupedMap[key]) {
          groupedMap[key] = {
            ...v,
            idMauSac,
            idKichThuoc,
            soLuong: Number(v.soLuong),
          };
        } else {
          groupedMap[key].soLuong += Number(v.soLuong);
        }
      });

      const requestData = Object.values(groupedMap).map((v) => {
        const donGiaRaw = v.giaBan?.toString().replace(/,/g, "");
        const donGia = Number(donGiaRaw);

        return {
          idSanPham: v.idSanPham,
          idMauSac: v.idMauSac,
          idKichThuoc: v.idKichThuoc,
          idHinhAnh: v.idHinhAnh || null,
          maCTSP: "",
          tenCTSP: `${v.tenSanPham} - ${v.mauSac?.tenMau || v.mauSac} - ${v.kichCo?.tenSize || v.kichCo}`,
          soLuong: v.soLuong,
          donGia,
          moTa: v.moTa || "",
          trangThai: 1,
        };
      });

      try {
        await batchCreateCTSP(requestData);

        showNotification("Thêm sản phẩm chi tiết thành công!", "success");
        setTimeout(() => {
          router.push("/product/san-pham/hien-thi");
        }, 2000);

        // Reset form
        variantsList.value = [];
        showVariantsTable.value = false;
        selectedTenSanPhamText.value = "";
        selectedSanPhamObj.value = null;
        selectedTenMauSac.value = [];
        selectedTenSize.value = [];
        selectedTenChatLieu.value = "";
        selectedTenXuatXu.value = "";
        selectedTenThuongHieu.value = "";

        nextTick(() => {
          $("#selectMauSac").val(null).trigger("change");
          $("#selectSize").val(null).trigger("change");
        });
      } catch (err) {
        console.error("Lỗi khi gửi request:", err);
        console.error("Lỗi chi tiết:", err.response?.data || err.message);
        showNotification("Đã xảy ra lỗi khi thêm sản phẩm chi tiết.");
      }
    };

    const fetchAllData = async () => {
      try {
        const res = await getComboboxData();
        const data = res.data;

        tenSanPhamList.value = data.tenSanPham || [];
        filteredTenSanPham.value = data.tenSanPham || [];

        tenMauSacList.value = data.tenMauSac || [];

        tenSizeList.value = (data.tenSize || []).map((item) => ({
          ...item,
          tenSize: `Size ${item.size}`,
        }));

        tenThuongHieuList.value = data.tenThuongHieu || [];
        tenXuatXuList.value = data.tenXuatXu || [];
        tenChatLieuList.value = data.tenChatLieu || [];

        console.log(
          "tenSizeList (thật):",
          JSON.parse(JSON.stringify(tenSizeList.value))
        );
      } catch (err) {
        console.error("Lỗi lấy dữ liệu combobox:", err);
      }
    };

    const initSelect2Color = () => {
      nextTick(() => {
        const $select = $("#selectMauSac");
        if ($select.hasClass("select2-hidden-accessible"))
          $select.select2("destroy");
        $select
          .select2({
            placeholder: "Chọn màu sắc...",
            allowClear: true,
            multiple: true,
            width: "100%",
          })
          .val(selectedTenMauSac.value)
          .trigger("change");
        $select.on("change", () => {
          selectedTenMauSac.value = $select.val() || [];
        });
      });
    };

    const initSelect2Size = () => {
      nextTick(() => {
        const $select = $("#selectSize");
        if ($select.hasClass("select2-hidden-accessible"))
          $select.select2("destroy");
        $select
          .select2({
            placeholder: "Chọn kích cỡ...",
            allowClear: true,
            multiple: true,
            width: "100%",
          })
          .val(selectedTenSize.value)
          .trigger("change");
        $select.on("change", () => {
          selectedTenSize.value = $select.val() || [];
        });
      });
    };

    onMounted(fetchAllData);

    watch(
      () => tenMauSacList.value,
      (val) => {
        if (val.length > 0) initSelect2Color();
      }
    );

    watch(
      () => tenSizeList.value,
      (val) => {
        if (val.length > 0) initSelect2Size();
      }
    );

    watch([selectedTenMauSac, selectedTenSize], ([mauMoi, sizeMoi]) => {
      if (selectedSanPhamObj.value && mauMoi.length && sizeMoi.length) {
        generateVariantsList();
      } else {
        variantsList.value = [];
        showVariantsTable.value = false;
      }
    });

    const goBack = () => router.push("/");

    const getTenMau = (mau) => {
      if (typeof mau === "object" && mau?.tenMau) return mau.tenMau;
      const found = tenMauSacList.value.find((m) => m.id === mau);
      return found ? found.tenMau : "---";
    };

    const getTenSize = (size) => {
      if (typeof size === "object" && size?.tenSize) return size.tenSize;
      const found = tenSizeList.value.find((s) => s.id === size);
      return found ? found.tenSize : "---";
    };

    const removeImage = (color, index) => {
      const key = normalizeRef(
        typeof color === "object" ? color.tenMau : color
      );
      if (uploadedImageUrls[key]) {
        uploadedImageUrls[key].splice(index, 1);
      }
    };

    return {
      selectedTenSanPhamText,
      selectedSanPhamObj,
      tenSanPhamList,
      filteredTenSanPham,
      showSuggestions,
      filterSuggestions,
      selectSuggestion,
      hideSuggestions,
      selectedTenSanPham,
      moTaSanPham,
      notification,
      addProduct,
      newThuongHieu,
      xacNhanThuongHieu,
      newXuatXu,
      newChatLieu,
      xacNhanChatLieu,
      xacNhanXuatXu,
      loadThuongHieuList,
      loadChatLieuList,
      loadXuatXuList,

      getImageUrl,
      tenMauSacList,
      selectedTenMauSac,
      tenSizeList,
      selectedTenSize,
      tenThuongHieuList,
      selectedTenThuongHieu,
      tenXuatXuList,
      selectedTenXuatXu,
      tenChatLieuList,
      selectedTenChatLieu,
      colorFileRefs,
      normalizeRef,
      variantsList,
      showVariantsTable,
      groupedVariants,
      groupImages,
      uploadedImageUrls,
      generateVariantsList,
      deleteVariant,
      showImage,
      submitVariants,
      handleImageUpload,
      handleImageUploadInTable,
      triggerFileInput,
      triggerColorImageInput,
      goBack,
      getTenMau,
      getTenSize,
      removeImage,
      progressWidth,
      showNotification,
      closeNotification,
      addProductModalRef,
    };
  },
};
</script>
