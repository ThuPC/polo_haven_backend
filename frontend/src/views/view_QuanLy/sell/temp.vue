<template>
  <div v-if="showOrderDetails" class="main-layout three-row-layout">
    <!-- 3 phần nằm 3 hàng ngang riêng biệt: Giỏ hàng, Thông tin khách hàng, Thông tin thanh toán -->
    <div class="cart-section">
      <!-- ...giữ nguyên toàn bộ nội dung giỏ hàng... -->
      <div class="cart-header">
        <h2 class="cart-title">
          <i class="fa-solid fa-cart-shopping"></i> Giỏ hàng
        </h2>
        <button class="btn btn-primary" @click="addProduct">
          <i class="fa-solid fa-plus"></i> Thêm sản phẩm
        </button>
      </div>
      <!-- danh sách sản phẩm đã thêm vô hóa đơn chơờ -->

      <div class="cart-items-list">
        <!-- Giao diện khi giỏ hàng trống -->
        <div
          v-if="getCurrentTab?.products.length === 0"
          class="text-center text-muted p-5 bg-light rounded"
        >
          <i class="fa-solid fa-shopping-basket fa-3x mb-3 text-secondary"></i>
          <p class="mb-0 fs-5">Chưa có sản phẩm nào trong giỏ hàng</p>
          <small>Hãy thêm sản phẩm vào giỏ hàng để tiếp tục nhéee!</small>
        </div>

        <!-- Lặp qua từng sản phẩm trong giỏ hàng -->
        <div
          v-for="product in pagedCartProducts"
          :key="product.id"
          class="cart-item-card d-flex align-items-center gap-3 py-3 border-bottom position-relative"
        >
          <!-- Checkbox cho sản phẩm (Nếu bạn có chức năng chọn nhiều sản phẩm) -->
          <div class="form-check ps-0" v-if="false">
            <!-- Thay v-if="false" bằng biến của bạn nếu cần dùng checkbox -->
            <input
              class="form-check-input mt-0 me-3"
              type="checkbox"
              value=""
              :id="'cartItemCheck' + product.id"
            />
          </div>

          <!-- Cột Ảnh sản phẩm -->
          <div class="item-image flex-shrink-0">
            <img
              :src="product.hinhAnh || product.url"
              :alt="product.tenCTSP || 'Product Image'"
              class="img-fluid rounded"
              style="width: 120px; height: 120px; object-fit: cover"
            />
          </div>

          <!-- Cột Thông tin chi tiết sản phẩm -->
          <div class="item-details flex-grow-1 d-flex flex-column">
            <!-- Tên sản phẩm và phân loại -->
            <div class="mb-2">
              <h6 class="fw-bold mb-0 text-dark">
                {{ product.tenCTSP || "Tên sản phẩm" }}
              </h6>
              <small class="text-muted"
                >{{ product.mauSac || "Màu sắc" }} /
                {{ product.kichThuoc || "Kích cỡ" }}</small
              >
            </div>

            <!-- Phần điều chỉnh số lượng và giá -->
            <div class="d-flex justify-content-between align-items-center">
              <!-- Nút Xóa sản phẩm -->
              <button
                @click="removeProduct(product.id)"
                class="btn btn-sm btn-link text-muted p-0 text-decoration-none"
                title="Xóa sản phẩm"
              >
                <i class="fa-solid fa-trash-can me-1"></i>Xóa
              </button>

              <!-- Bộ điều chỉnh số lượng -->
              <div
                class="quantity-control d-flex align-items-center border rounded"
              >
                <button
                  class="btn btn-sm btn-light border-0"
                  @click="updateQuantity(product.id, product.quantity - 1)"
                  :disabled="product.quantity <= 1"
                  style="width: 32px; height: 32px"
                >
                  -
                </button>
                <input
                  type="number"
                  class="form-control form-control-sm text-center border-0 p-0"
                  :value="product.quantity"
                  @input="updateQuantity(product.id, $event.target.value)"
                  min="1"
                  :max="product.warehouse"
                  style="
                    width: 45px;
                    box-shadow: none;
                    -moz-appearance: textfield;
                    height: 32px;
                  "
                />
                <button
                  class="btn btn-sm btn-light border-0"
                  @click="updateQuantity(product.id, product.quantity + 1)"
                  :disabled="product.quantity >= product.warehouse"
                  style="width: 32px; height: 32px"
                >
                  +
                </button>
              </div>

              <!-- Tổng tiền của sản phẩm -->
              <div class="item-total fw-bold text-end ms-3">
                {{ formatCurrency(product.currentPrice) }}
              </div>
              <!-- Giá hiện tại, Giá được tính, Tổng có thể hiển thị dưới dạng tooltips hoặc chi tiết hơn nếu cần -->
            </div>

            <!-- Thông tin thêm: Số lượng còn -->
            <small class="text-muted mt-2"
              ><i class="fa-solid fa-boxes-stacked me-1"></i>Còn lại:
              <span class="fw-semibold">{{ product.warehouse }}</span> sản
              phẩm</small
            >
            <!-- Cảnh báo nếu số lượng đã chọn bằng hoặc vượt quá số lượng còn -->
            <small
              v-if="product.quantity >= product.warehouse"
              class="text-danger fw-bold d-block mt-1"
            >
              <i class="fa-solid fa-exclamation-triangle me-1"></i>Đã đạt số
              lượng tồn tối đa
            </small>
          </div>
        </div>
      </div>

      <div class="pagination-wrapper">
        <ul class="pagination justify-content-end">
          <li class="page-item" :class="{ disabled: cartPage === 1 }">
            <button
              class="page-link"
              @click="cartPage = Math.max(1, cartPage - 1)"
              :disabled="cartPage === 1"
            >
              &laquo;
            </button>
          </li>
          <li
            class="page-item"
            v-for="n in cartTotalPages"
            :key="n"
            :class="{ active: cartPage === n }"
          >
            <button class="page-link" @click="cartPage = n">{{ n }}</button>
          </li>
          <li
            class="page-item"
            :class="{ disabled: cartPage === cartTotalPages }"
          >
            <button
              class="page-link"
              @click="cartPage = Math.min(cartTotalPages, cartPage + 1)"
              :disabled="cartPage === cartTotalPages"
            >
              &raquo;
            </button>
          </li>
          <!-- Đã xóa số dòng trên/trang -->
        </ul>
      </div>
    </div>

    <div class="right-section">
      <div v-if="getCurrentTab?.id" class="customer-info-section">
        <!-- ...giữ nguyên toàn bộ phần Thông tin khách hàng... -->
        <div class="info-block customer-info-block">
          <div
            class="block-header d-flex justify-content-between align-items-center"
          >
            <h2 class="cart-title">
              <i class="fa-solid fa-user-tie"></i> Thông tin khách hàng
            </h2>
            <div class="customer-actions">
              <button
                class="btn btn-primary btn-sm"
                @click="showCustomerSearchModal = true"
              >
                <i class="fa-solid fa-search"></i> Tìm khách hàng
              </button>
              <button
                class="btn btn-secondary btn-sm"
                @click="showAddCustomerForm = true"
              >
                <i class="fa-solid fa-user-plus"></i> Thêm khách hàng mới
              </button>
            </div>
          </div>
          <div class="customer-info">
            <div class="customer-label">
              Khách hàng #{{
                getCurrentTab?.customer?.tenKhachHang || "Khách lẻ"
              }}
            </div>
            <div v-if="getCurrentTab?.customer" class="customer-display">
              <div class="customer-avatar">
                {{ getCurrentTab?.customer?.tenKhachHang?.[0] || "?" }}
              </div>
              <div>
                <span class="customer-name">{{
                  getCurrentTab?.customer?.tenKhachHang || "Không có tên"
                }}</span>
                <p class="customer-phone">
                  {{ getCurrentTab?.customer?.sdt || "Chưa có số điện thoại" }}
                </p>
                <p class="customer-email">
                  {{ getCurrentTab?.customer?.email || "Chưa có email" }}
                </p>
              </div>
            </div>
            <div v-else class="customer-placeholder">
              Khách lẻ (Chưa chọn khách hàng)
            </div>
          </div>
        </div>

        <div class="customer-info-section delivery-method py-3">
          <h3 class="customer-list-title">Hình thức nhận hàng</h3>
          <div class="radio-group">
            <label class="radio-option">
              <input
                type="radio"
                name="deliveryMethod"
                value="at-store"
                :checked="getCurrentTab?.deliveryMethod === 'at-store'"
                @change="updateDeliveryMethod('at-store')"
              />
              <span>Nhận tại quầy</span>
            </label>
            <label class="radio-option">
              <input
                type="radio"
                name="deliveryMethod"
                value="delivery"
                :checked="getCurrentTab?.deliveryMethod === 'delivery'"
                @change="updateDeliveryMethod('delivery')"
              />
              <span>Giao hàng</span>
            </label>
          </div>
        </div>
        <div
          v-if="getCurrentTab?.deliveryMethod === 'delivery'"
          class="address-section"
        >
          <div v-if="getCurrentTab?.customer">
            <div
              class="address-selection"
              v-if="getCurrentTab.customer.addresses.length > 0"
            >
              <label class="address-label">Chọn địa chỉ có sẵn</label>
              <div
                class="address-display"
                @click="showAddressSelectionModal = true"
              >
                <div
                  v-if="getCurrentTab.selectedAddressId"
                  class="selected-address"
                >
                  <i class="fa-solid fa-map-marker-alt"></i>
                  <span>{{ getSelectedAddressText() }}</span>
                </div>
                <div v-else class="no-address-selected">
                  <i class="fa-solid fa-map-marker-alt"></i>
                  <span>Chọn địa chỉ giao hàng</span>
                </div>
                <i class="fa-solid fa-chevron-right"></i>
              </div>
            </div>
            <div v-else>
              <button class="btn btn-primary" @click="showAddressModal = true">
                <i class="fa-solid fa-plus"></i> Thêm địa chỉ mới
              </button>
              <div class="text-muted" style="margin-top: 8px">
                Khách hàng chưa có địa chỉ, hãy thêm mới!
              </div>
            </div>
          </div>
          <div v-else class="address-input guest-address-block">
            <div class="address-title mb-2">
              <i class="fa-solid fa-map-marker-alt"></i> Địa chỉ giao hàng
              chokhách lẻ
            </div>
            <div class="row g-2">
              <div class="col-md-6 form-group">
                <label>Tên người nhận</label>
                <input
                  v-model="getCurrentTab.guestCustomer.tenNguoiNhan"
                  type="text"
                  class="form-control"
                  placeholder="Nhập tên người nhận"
                />
              </div>
              <div class="col-md-6 form-group">
                <label>Số điện thoại</label>
                <input
                  v-model="getCurrentTab.guestCustomer.sdt"
                  maxlength="10"
                  type="text"
                  class="form-control"
                  placeholder="Nhập số điện thoại"
                />
              </div>
              <div class="col-md-6 form-group">
                <label>Tỉnh/Thành phố</label>
                <select
                  v-model="getCurrentTab.guestCustomer.provinceId"
                  @change="handleProvinceChange($event)"
                  class="form-select"
                >
                  <option :value="null">Chọn tỉnh/thành phố</option>
                  <option
                    v-for="province in provinces"
                    :key="province.ProvinceID"
                    :value="province.ProvinceID"
                  >
                    {{ province.ProvinceName }}
                  </option>
                </select>
              </div>
              <div class="col-md-6 form-group">
                <label>Quận/Huyện</label>
                <select
                  v-model="getCurrentTab.guestCustomer.districtId"
                  @change="handleDistrictChange($event)"
                  class="form-select"
                  :disabled="!getCurrentTab.guestCustomer.provinceId"
                >
                  <option :value="null">Chọn quận/huyện</option>
                  <option
                    v-for="district in districts"
                    :key="district.DistrictID"
                    :value="district.DistrictID"
                  >
                    {{ district.DistrictName }}
                  </option>
                </select>
              </div>
              <div class="col-md-6 form-group">
                <label>Xã/Phường</label>
                <select
                  v-model="getCurrentTab.guestCustomer.wardCode"
                  @change="handleWardChange($event)"
                  class="form-select"
                  :disabled="!getCurrentTab.guestCustomer.districtId"
                >
                  <option :value="''">Chọn xã/phường</option>
                  <option
                    v-for="ward in wards"
                    :key="ward.WardCode"
                    :value="ward.WardCode"
                  >
                    {{ ward.WardName }}
                  </option>
                </select>
              </div>
              <div class="col-md-6 form-group">
                <label>Số nhà/Ngõ/Đường</label>
                <input
                  v-model="getCurrentTab.guestCustomer.soNha"
                  type="text"
                  class="form-control"
                  placeholder="Nhập số nhà/ngõ/đường"
                />
              </div>
            </div>
            <div class="text-muted mt-2" style="font-size: 13px">
              Vui lòng nhập đầy đủ thông tin để giao hàng chính xác.
            </div>
          </div>
        </div>
      </div>

      <div v-if="getCurrentTab?.id" class="payment-info-section">
        <!-- ...giữ nguyên toàn bộ phần Thông tin thanh toán... -->
        <div class="info-block">
          <div class="block-header">
            <h2 class="cart-title">
              <i class="fa-solid fa-money-check-dollar"></i> Thông tin thanh
              toán
            </h2>
          </div>
          <div class="payment-method">
            <h3 class="customer-list-title">Hình thức thanh toán</h3>
            <div class="radio-group">
              <label class="radio-option">
                <input
                  type="radio"
                  value="vnpay-qr"
                  :checked="getCurrentTab?.paymentMethod === 'vnpay-qr'"
                  @change="updatePaymentMethod('vnpay-qr')"
                />
                <span
                  ><img
                    :src="require('@/assets/img/logos/vnpay-logo.jpg')"
                    alt="VNPay"
                    style="
                      height: 25px;
                      vertical-align: middle;
                      margin-right: 4px;
                    "
                  />VNPay QR</span
                >
              </label>
              <label class="radio-option"> </label>
              <label class="radio-option">
                <input
                  type="radio"
                  value="cash"
                  :checked="getCurrentTab?.paymentMethod === 'cash'"
                  @change="updatePaymentMethod('cash')"
                />
                <span>Tiền mặt</span>
              </label>
            </div>
          </div>
          <div class="payment-details">
            <div class="d-flex align-items-start">
              <span class="fw-bold mb-2">Phiếu giảm giá: </span><br />
              <select
                id="voucher-select"
                v-model="getCurrentTab.discountCode"
                class="form-select w-40 ms-2"
                @change="applyDiscount"
              >
                <option value="">-- Không áp dụng --</option>
                <option
                  v-for="voucher in uniqueVouchers"
                  :key="voucher.id"
                  :value="voucher.id"
                >
                  {{ formatVoucherDisplay(voucher) }}
                </option>
              </select>
            </div>
            <div class="detail-row">
              <span>Tổng tiền:</span>
              <span class="amount">{{
                formatCurrency(
                  getCurrentTab?.products.reduce(
                    (sum, p) => sum + p.total,
                    0
                  ) || 0
                )
              }}</span>
            </div>
            <div v-if="isGiaoHang" class="detail-row">
              <span
                >Phí vận chuyển:
                <img
                  :src="require('@/assets/img/logos/giao_hang_nhanh_logo.png')"
                  alt="GHN"
                  style="
                    height: 25px;
                    vertical-align: middle;
                    margin-right: 4px;
                  "
              /></span>
              <span class="amount positive"
                >+ {{ formatCurrency(getCurrentTab?.shipping || 0) }}</span
              >
            </div>
            <div class="detail-row">
              <span
                >Giảm giá ({{ getCurrentTab?.discountPercent || 0 }}%):</span
              >
              <span class="amount negative"
                >– {{ formatCurrency(getCurrentTab?.discount || 0) }}</span
              >
            </div>
          </div>
          <div class="grand-total">
            <span class="grand-total-label">Tổng thanh toán:</span>
            <span class="grand-total-amount">{{
              formatCurrency(calculateTotal())
            }}</span>
          </div>
          <div class="d-flex justify-content-end" style="width: 100%">
            <button class="btn btn-primary" @click="confirmOrder">
              Xác nhận thanh toán
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal thêm nhanh khách hàng -->
    <div v-if="showAddCustomerForm" class="modal">
      <div class="modal-content" style="max-width: 700px">
        <div class="modal-header">
          <h2 class="cart-title">
            <i class="fa-solid fa-user-plus"></i> Thêm nhanh khách hàng
          </h2>
          <button
            @click="showAddCustomerForm = false"
            class="btn btn-secondary"
          >
            ×
          </button>
        </div>
        <!-- <<<<<<< HEAD
          <div class="payment-method">
            <h3 class="customer-list-title">Hình thức thanh toán</h3>
            <div class="radio-group">
              <label class="radio-option">
                <input type="radio" value="vnpay-qr" :checked="getCurrentTab?.paymentMethod === 'vnpay-qr'"
                  @change="updatePaymentMethod('vnpay-qr')" />
                <span>
                  <img :src="require('@/assets/img/logos/vnpay-logo.jpg')" alt="VNPay"
                    style="height:25px;vertical-align:middle;margin-right:4px;">
                  VNPay QR
                </span>
              </label>
              <label class="radio-option">
              </label>
              <label class="radio-option">
                <input type="radio" value="cash" :checked="getCurrentTab?.paymentMethod === 'cash'"
                  @change="updatePaymentMethod('cash')" />
                <span>Tiền mặt</span>
              </label>
======= -->
        <div class="modal-body">
          <div class="form-group">
            <label>Tên khách hàng <span class="text-danger">*</span></label>
            <input
              v-model="newCustomer.tenNguoiNhan"
              type="text"
              class="form-control"
              :class="{ 'is-invalid': customerErrors.tenNguoiNhan }"
              placeholder="Nhập tên khách hàng"
              @blur="validateCustomerField('tenNguoiNhan')"
            />
            <div v-if="customerErrors.tenNguoiNhan" class="invalid-feedback">
              {{ customerErrors.tenNguoiNhan }}
            </div>
          </div>
          <div class="form-group">
            <label>Số điện thoại <span class="text-danger">*</span></label>
            <input
              v-model="newCustomer.sdt"
              maxlength="10"
              type="text"
              class="form-control"
              :class="{ 'is-invalid': customerErrors.sdt }"
              placeholder="Nhập số điện thoại"
              @blur="validateCustomerField('phone')"
            />
            <div v-if="customerErrors.sdt" class="invalid-feedback">
              {{ customerErrors.sdt }}
            </div>
          </div>
          <div class="form-group">
            <label>Email</label>
            <input
              v-model="newCustomer.email"
              type="email"
              class="form-control"
              :class="{ 'is-invalid': customerErrors.email }"
              placeholder="Nhập email"
              @blur="validateCustomerField('email')"
            />
            <div v-if="customerErrors.email" class="invalid-feedback">
              {{ customerErrors.email }}
            </div>
          </div>
          <div class="form-group">
            <label>Loại đăng nhập</label>
            <select v-model="newCustomer.loginProvider" class="form-select">
              <option value="LOCAL">Local</option>
              <option value="GOOGLE">Google</option>
            </select>
            <small class="form-text text-muted">
              Chọn "Local" để tạo mật khẩu tự động, chọn "Google" để đăng nhập
              bằng Google
            </small>
          </div>
          <div class="modal-footer d-flex justify-content-end">
            <button
              class="btn btn-secondary"
              @click="
                showAddCustomerForm = false;
                clearCustomerErrors();
              "
            >
              Hủy
            </button>
            <button class="btn btn-primary" @click="addNewCustomer">
              Lưu khách hàng
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showAddressModal" class="modal">
      <div class="modal-content" style="max-width: 700px">
        <div class="modal-header">
          <h2 class="cart-title">
            <i class="fa-solid fa-map-marker-alt"></i> Thêm địa chỉ nhận hàng
          </h2>
          <button
            @click="showAddressModal = false"
            class="btn btn-secondary close-modal-btn"
          >
            ×
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Tên người nhận</label>
            <input
              v-model="newAddress.tenNguoiNhan"
              type="text"
              class="form-control"
              placeholder="Nhập tên người nhận"
            />
          </div>
          <div class="form-group">
            <label>Số điện thoại</label>
            <input
              v-model="newAddress.sdt"
              maxlength="10"
              type="text"
              class="form-control"
              placeholder="Nhập số điện thoại"
            />
          </div>
          <div class="form-group">
            <label>Tỉnh/Thành phố</label>
            <select v-model="newAddress.provinceId" class="form-select">
              <option :value="null">Chọn tỉnh/thành phố</option>
              <option
                v-for="province in provinces"
                :key="province.ProvinceID"
                :value="province.ProvinceID"
              >
                {{ province.ProvinceName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>Quận/Huyện</label>
            <select
              v-model="newAddress.districtId"
              class="form-select"
              :disabled="!newAddress.provinceId"
            >
              <option :value="null">Chọn quận/huyện</option>
              <option
                v-for="district in districts"
                :key="district.DistrictID"
                :value="district.DistrictID"
              >
                {{ district.DistrictName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>Xã/Phường</label>
            <select
              v-model="newAddress.wardCode"
              class="form-select"
              :disabled="!newAddress.districtId"
            >
              <option :value="''">Chọn xã/phường</option>
              <option
                v-for="ward in wards"
                :key="ward.WardCode"
                :value="ward.WardCode"
              >
                {{ ward.WardName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>Số nhà/Ngõ/Đường</label>
            <input
              v-model="newAddress.soNha"
              type="text"
              class="form-control"
              placeholder="Nhập số nhà/ngõ/đường"
            />
          </div>
          <div class="modal-footer d-flex justify-content-end">
            <button
              class="btn btn-secondary"
              @click="
                showAddressModal = false;
                showAddressSelectionModal = true;
              "
            >
              Hủy
            </button>
            <button class="btn btn-primary" @click="addNewAddress">
              Lưu địa chỉ
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
