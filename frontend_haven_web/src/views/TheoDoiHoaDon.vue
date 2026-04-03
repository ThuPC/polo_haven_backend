<template>
  <div class="min-vh-100 bg-light py-5">
    <div class="container">
      <!-- Header -->
      <div class="text-center mb-5">
        <h1 class="display-4 fw-bold text-success mb-3">
          <i class="fas fa-receipt me-3"></i>
          Theo dõi hóa đơn
        </h1>
        <p class="lead text-muted">Xem chi tiết và trạng thái các đơn hàng của bạn</p>
        
        <!-- Button đơn đã yêu cầu -->
        <div class="mt-4">
          <button @click="showReturnRequests" class="btn btn-outline-primary btn-lg">
            <i class="fas fa-undo me-2"></i>
            Đơn đã yêu cầu
          </button>
        </div>
      </div>

      <!-- Section Danh sách yêu cầu trả hàng -->
      <div v-if="showReturnRequestsSection" class="mb-5">
        <div class="card shadow-sm">
          <div class="card-header bg-primary text-white">
            <h4 class="mb-0">
              <i class="fas fa-undo me-2"></i>
              Danh sách yêu cầu trả hàng
            </h4>
          </div>
          <div class="card-body">
            <!-- Import component DanhSachTraHang ở đây -->
            <DanhSachTraHang />
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="d-flex justify-content-center align-items-center py-5">
        <div class="text-center">
          <div class="spinner-border text-primary mb-3" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
          <p class="text-muted">Đang tải danh sách hóa đơn...</p>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else-if="hoaDons.length === 0" class="text-center py-5">
        <div class="card shadow-sm mx-auto" style="max-width: 400px;">
          <div class="card-body p-5">
            <i class="fas fa-inbox text-muted" style="font-size: 4rem;"></i>
            <h3 class="h4 text-dark mt-3 mb-2">Chưa có hóa đơn nào</h3>
            <p class="text-muted mb-4">Bạn chưa có đơn hàng nào. Hãy mua sắm để tạo hóa đơn đầu tiên!</p>
            <router-link to="/products" class="btn btn-primary btn-lg">
              <i class="fas fa-shopping-cart me-2"></i>
              Mua sắm ngay
            </router-link>
          </div>
        </div>
      </div>

      <!-- Hóa đơn List -->
      <div v-else class="row g-4">
        <div v-for="hoaDon in hoaDons" :key="hoaDon.id" class="col-12">
          <div class="card shadow-sm h-100 border-0">
            <div class="card-body p-4">
              <div class="row align-items-center">
                <!-- Thông tin hóa đơn -->
                <div class="col-lg-8">
                  <div class="d-flex align-items-center mb-3">
                    <div class="bg-primary bg-opacity-10 rounded-circle p-3 me-3">
                      <i class="fas fa-receipt text-primary fs-4"></i>
                    </div>
                    <div>
                      <h5 class="card-title mb-1 fw-bold">{{ hoaDon.maHoaDon }}</h5>
                      <p class="text-muted mb-0">
                        <i class="fas fa-calendar-alt me-1"></i>
                        {{ formatDate(hoaDon.ngayTao) }}
                      </p>
                    </div>
                  </div>

                  <div class="row g-3">
                    <div class="col-md-4">
                      <div class="d-flex align-items-center">
                        <i class="fas fa-money-bill-wave text-success me-2"></i>
                        <span class="fw-medium">{{ formatCurrency(hoaDon.tongTien - hoaDon.soTienGiam + hoaDon.phiVanChuyen)
                          }}</span>
                      </div>
                    </div>
                    <div class="col-md-4">
                      <div class="d-flex align-items-center">
                        <i class="fas fa-info-circle text-primary me-2"></i>
                        <span :class="getStatusClass(hoaDon.trangThai)">
                          {{ getStatusText(hoaDon.trangThai) }}
                        </span>
                      </div>
                    </div>
                    <div class="col-md-4">
                      <div class="d-flex align-items-center">
                        <i class="fas fa-clock text-warning me-2"></i>
                        <span class="text-muted small">{{ getTimeAgo(hoaDon.ngayTao) }}</span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Buttons -->
                <div class="col-lg-4 text-lg-end mt-3 mt-lg-0">
                  <div class="d-flex flex-column flex-sm-row gap-2 justify-content-end">
                    <button @click="xemChiTiet(hoaDon.id)" class="btn btn-success btn-lg px-4 py-2">
                      <i class="fas fa-eye me-2"></i>
                      Xem chi tiết
                    </button>

                    <!-- Button yêu cầu hủy chỉ hiển thị cho đơn hàng có thể hủy -->
                    <button v-if="canCancelOrder(hoaDon.trangThai)" @click="openCancelModal(hoaDon)"
                      class="btn btn-danger btn-lg px-4 py-2">
                      <i class="fas fa-times me-2"></i>
                      Yêu cầu hủy
                    </button>

                    <!-- Button yêu cầu trả hàng chỉ hiển thị cho đơn hàng đã giao -->
                    <button v-if="canReturnOrder(hoaDon.trangThai)" @click="openReturnModal(hoaDon)"
                      class="btn btn-warning btn-lg px-4 py-2">
                      <i class="fas fa-undo me-2"></i>
                      Yêu cầu trả hàng
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Modal chi tiết hóa đơn -->
      <div v-if="chiTiet" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
        <div class="modal-dialog modal-xl modal-dialog-scrollable">
          <div class="modal-content border-0 shadow">
            <!-- Header -->
            <div class="modal-header bg-primary text-white border-0">
              <div class="d-flex align-items-center">
                <div class="bg-white bg-opacity-25 rounded-circle p-2 me-3">
                  <i class="fas fa-receipt text-white fs-4"></i>
                </div>
                <div>
                  <h4 class="modal-title fw-bold mb-1">Chi tiết hóa đơn</h4>
                  <p class="mb-0 opacity-75">{{ chiTiet.maHoaDon }}</p>
                </div>
              </div>
              <button @click="chiTiet = null" class="btn-close btn-close-white" type="button">
              </button>
            </div>

            <!-- Content -->
            <div class="modal-body p-4">
              <!-- Thông tin hóa đơn -->
              <div class="row g-4 mb-4">
                <div class="col-lg-6">
                  <div class="card border-0 bg-light">
                    <div class="card-body">
                      <h5 class="card-title text-primary mb-3">
                        <i class="fas fa-info-circle me-2"></i>
                        Thông tin hóa đơn
                      </h5>
                      <div class="row g-2">
                        <div class="col-6">
                          <small class="text-muted">Mã hóa đơn:</small>
                          <div class="fw-bold">{{ chiTiet.maHoaDon }}</div>
                        </div>
                        <div class="col-6">
                          <small class="text-muted">Ngày tạo:</small>
                          <div>{{ formatDate(chiTiet.ngayTao) }}</div>
                        </div>
                        <div class="col-6">
                          <small class="text-muted">Ngày thanh toán:</small>
                          <div>{{ formatDate(chiTiet.ngayThanhToan) || 'Chưa thanh toán' }}</div>
                        </div>
                        <div class="col-6">
                          <small class="text-muted">Trạng thái:</small>
                          <div>
                            <span :class="getStatusClass(chiTiet.trangThai)">
                              {{ getStatusText(chiTiet.trangThai) }}
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="col-lg-6">
                  <div class="card border-0 bg-success bg-opacity-10">
                    <div class="card-body">
                      <h5 class="card-title text-success mb-3">
                        <i class="fas fa-credit-card me-2"></i>
                        Thông tin thanh toán
                      </h5>
                      <div class="row g-2">
                        <div class="col-6">
                          <small class="text-muted">Hình thức TT:</small>
                          <div>{{ chiTiet.tenHTTT || 'Chưa chọn' }}</div>
                        </div>
                        <div class="col-6">
                          <small class="text-muted">Tổng tiền:</small>
                          <div class="fw-bold fs-5 text-success">{{ formatCurrency(chiTiet.tongTien || 0) }}</div>
                        </div>
                        <div class="col-12">
                          <small class="text-muted">Ghi chú:</small>
                          <div>{{ chiTiet.ghiChu || 'Không có' }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Thông tin khách hàng -->
              <div class="card border-0 bg-info bg-opacity-10 mb-4">
                <div class="card-body">
                  <h5 class="card-title text-info mb-3">
                    <i class="fas fa-user me-2"></i>
                    Thông tin khách hàng
                  </h5>
                  <div class="row g-3">
                    <div class="col-md-6">
                      <div class="row g-2">
                        <div class="col-6">
                          <small class="text-muted">Họ tên:</small>
                          <div class="fw-bold">{{ chiTiet.hoTen || 'N/A' }}</div>
                        </div>
                        <div class="col-6">
                          <small class="text-muted">Số điện thoại:</small>
                          <div>{{ chiTiet.soDienThoai || 'N/A' }}</div>
                        </div>
                        <div class="col-12">
                          <small class="text-muted">Email:</small>
                          <div>{{ chiTiet.email || 'N/A' }}</div>
                        </div>
                      </div>
                    </div>
                    <div class="col-md-6">
                      <div class="row g-2">
                        <div class="col-12">
                          <small class="text-muted">Địa chỉ:</small>
                          <div>{{ chiTiet.diaChi || 'N/A' }}</div>
                        </div>
                        <!-- <div class="col-6">
                          <small class="text-muted">Tỉnh/Thành:</small>
                          <div>{{ chiTiet.tenTinhThanh || 'N/A' }}</div>
                        </div>
                        <div class="col-6">
                          <small class="text-muted">Quận/Huyện:</small>
                          <div>{{ chiTiet.tenQuanHuyen || 'N/A' }}</div>
                        </div> -->
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Danh sách sản phẩm -->
              <div class="card border-0">
                <div class="card-header bg-light border-0">
                  <h5 class="card-title mb-0">
                    <i class="fas fa-shopping-bag me-2"></i>
                    Danh sách sản phẩm
                  </h5>
                </div>
                <div class="card-body p-0">
                  <div class="table-responsive">
                    <table class="table table-hover mb-0">
                      <thead class="table-light">
                        <tr>
                          <th class="border-0">Sản phẩm</th>
                          <th class="border-0 text-center">Số lượng</th>
                          <th class="border-0 text-end">Đơn giá</th>
                          <th class="border-0 text-end">Thành tiền</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="item in chiTiet.chiTietHoaDonList" :key="item.id">
                          <td class="border-0">
                            <div class="d-flex align-items-center">
                              <div class="flex-shrink-0 me-3" style="width: 60px; height: 60px;">
                                <img v-if="item.hinhAnh" :src="getImageUrl(item.hinhAnh)" :alt="item.tenSanPham"
                                  class="w-100 h-100 object-fit-cover rounded border"
                                  @error="handleImageError">
                                <div v-else
                                  class="w-100 h-100 bg-light rounded d-flex align-items-center justify-content-center">
                                  <i class="fas fa-image text-muted"></i>
                                </div>
                              </div>
                              <div class="flex-grow-1">
                                <div class="fw-medium">{{ item.tenSanPham }}</div>
                                <small class="text-muted">{{ item.mauSac }} - {{ item.kichThuoc }}</small>
                              </div>
                            </div>
                          </td>
                          <td class="border-0 text-center">
                            <span class="badge bg-primary rounded-pill">{{ item.soLuong }}</span>
                          </td>
                          <td class="border-0 text-end fw-medium">{{ formatCurrency(item.giaBan) }}</td>
                          <td class="border-0 text-end fw-bold text-success">{{ formatCurrency(item.giaBan *
                            item.soLuong) }}</td>
                        </tr>
                      </tbody>
                      <tfoot class="table-success">
                        <tr>
                          <td colspan="3" class="border-0 text-end  fs-5">Phí vận chuyển:</td>
                          <td class="border-0 text-end fw-bold fs-5 ">{{ formatCurrency(chiTiet.phiVanChuyen) }}</td>
                        </tr>
                        <tr>
                          <td colspan="3" class="border-0 text-end  fs-5">Giảm giá:</td>
                          <td class="border-0 text-end fw-bold fs-5 ">{{ formatCurrency(chiTiet.soTienGiam) }}</td>
                        </tr>
                        <tr>
                          <td colspan="3" class="border-0 text-end fw-bold fs-5">Tổng cộng:</td>
                          <td class="border-0 text-end fw-bold fs-5 text-success">{{
                            formatCurrency(chiTiet.tongTien - chiTiet.soTienGiam +chiTiet.phiVanChuyen) }}</td>
                        </tr>
                      </tfoot>
                    </table>
                  </div>
                </div>
              </div>

              <!-- Footer -->
              <div class="text-center mt-4">
                <button @click="chiTiet = null" class="btn btn-secondary btn-lg px-5">
                  <i class="fas fa-times me-2"></i>
                  Đóng
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Modal xác nhận hủy đơn hàng -->
      <div v-if="cancelModal.show" class="modal fade show d-block" tabindex="-1"
        style="background-color: rgba(0,0,0,0.5);">
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content border-0 shadow">
            <!-- Header -->
            <div class="modal-header bg-danger text-white border-0">
              <div class="d-flex align-items-center">
                <div class="bg-white bg-opacity-25 rounded-circle p-2 me-3">
                  <i class="fas fa-exclamation-triangle text-white fs-4"></i>
                </div>
                <div>
                  <h4 class="modal-title fw-bold mb-1">Xác nhận hủy đơn hàng</h4>
                  <p class="mb-0 opacity-75">{{ cancelModal.hoaDon?.maHoaDon }}</p>
                </div>
              </div>
              <button @click="closeCancelModal" class="btn-close btn-close-white" type="button">
              </button>
            </div>

            <!-- Content -->
            <div class="modal-body p-4">
              <div class="alert alert-warning" role="alert">
                <i class="fas fa-exclamation-triangle me-2"></i>
                <strong>Lưu ý:</strong> Việc hủy đơn hàng không thể hoàn tác. Vui lòng xác nhận lại.
              </div>

              <div class="mb-4">
                <h6 class="fw-bold mb-3">Thông tin đơn hàng:</h6>
                <div class="row g-3">
                  <div class="col-md-6">
                    <small class="text-muted">Mã đơn hàng:</small>
                    <div class="fw-bold">{{ cancelModal.hoaDon?.maHoaDon }}</div>
                  </div>
                  <div class="col-md-6">
                    <small class="text-muted">Tổng tiền:</small>
                    <div class="fw-bold text-danger">{{ formatCurrency(cancelModal.hoaDon?.tongTien) }}</div>
                  </div>
                  <div class="col-md-6">
                    <small class="text-muted">Ngày đặt:</small>
                    <div>{{ formatDate(cancelModal.hoaDon?.ngayTao) }}</div>
                  </div>
                  <div class="col-md-6">
                    <small class="text-muted">Trạng thái hiện tại:</small>
                    <div>
                      <span :class="getStatusClass(cancelModal.hoaDon?.trangThai)">
                        {{ getStatusText(cancelModal.hoaDon?.trangThai) }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <div class="mb-4">
                <label for="lyDoHuy" class="form-label fw-bold">
                  <i class="fas fa-comment me-2"></i>
                  Lý do hủy đơn hàng <span class="text-danger">*</span>
                </label>
                <textarea id="lyDoHuy" v-model="cancelModal.lyDo" class="form-control" rows="4"
                  placeholder="Vui lòng nhập lý do hủy đơn hàng..."
                  :class="{ 'is-invalid': cancelModal.errors.lyDo }"></textarea>
                <div v-if="cancelModal.errors.lyDo" class="invalid-feedback">
                  {{ cancelModal.errors.lyDo }}
                </div>
                <small class="text-muted">
                  <i class="fas fa-info-circle me-1"></i>
                  Lý do hủy sẽ được gửi đến admin để xem xét
                </small>
              </div>
            </div>

            <!-- Footer -->
            <div class="modal-footer border-0">
              <button @click="closeCancelModal" class="btn btn-secondary btn-lg px-4">
                <i class="fas fa-times me-2"></i>
                Hủy bỏ
              </button>
              <button @click="submitCancelRequest" class="btn btn-danger btn-lg px-4"
                :disabled="cancelModal.isSubmitting">
                <span v-if="cancelModal.isSubmitting" class="spinner-border spinner-border-sm me-2"
                  role="status"></span>
                <i v-else class="fas fa-check me-2"></i>
                {{ cancelModal.isSubmitting ? 'Đang gửi...' : 'Xác nhận hủy' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Modal xác nhận trả hàng -->
      <div v-if="returnModal.show" class="modal fade show d-block" tabindex="-1"
        style="background-color: rgba(0,0,0,0.5);">
        <div class="modal-dialog modal-xl modal-dialog-scrollable">
          <div class="modal-content border-0 shadow">
            <!-- Header -->
            <div class="modal-header bg-warning text-dark border-0">
              <div class="d-flex align-items-center">
                <div class="bg-dark bg-opacity-25 rounded-circle p-2 me-3">
                  <i class="fas fa-undo text-dark fs-4"></i>
                </div>
                <div>
                  <h4 class="modal-title fw-bold mb-1">Yêu cầu trả hàng</h4>
                  <p class="mb-0 opacity-75">{{ returnModal.hoaDon?.maHoaDon }}</p>
                </div>
              </div>
              <button @click="closeReturnModal" class="btn-close" type="button">
              </button>
            </div>

            <!-- Content -->
            <div class="modal-body p-4">
              <div class="alert alert-info" role="alert">
                <i class="fas fa-info-circle me-2"></i>
                <strong>Lưu ý:</strong> Yêu cầu trả hàng sẽ được admin xem xét. Vui lòng chuẩn bị sản phẩm và minh chứng
                theo hướng dẫn.
              </div>

              <div class="mb-4">
                <h6 class="fw-bold mb-3">Thông tin đơn hàng:</h6>
                <div class="row g-3">
                  <div class="col-md-6">
                    <small class="text-muted">Mã đơn hàng:</small>
                    <div class="fw-bold">{{ returnModal.hoaDon?.maHoaDon }}</div>
                  </div>
                  <div class="col-md-6">
                    <small class="text-muted">Tổng tiền:</small>
                    <div class="fw-bold text-warning">{{ formatCurrency(returnModal.hoaDon?.tongTien) }}</div>
                  </div>
                  <div class="col-md-6">
                    <small class="text-muted">Ngày đặt:</small>
                    <div>{{ formatDate(returnModal.hoaDon?.ngayTao) }}</div>
                  </div>
                  <div class="col-md-6">
                    <small class="text-muted">Trạng thái hiện tại:</small>
                    <div>
                      <span :class="getStatusClass(returnModal.hoaDon?.trangThai)">
                        {{ getStatusText(returnModal.hoaDon?.trangThai) }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Chọn sản phẩm trả hàng -->
              <div class="card border-0 bg-light mb-4">
                <div class="card-header bg-light border-0">
                  <h6 class="fw-bold mb-0">
                    <i class="fas fa-shopping-bag me-2"></i>
                    Chọn sản phẩm trả hàng
                  </h6>
                </div>
                <div class="card-body p-0">
                  <div class="table-responsive">
                    <table class="table table-hover mb-0">
                      <thead class="table-light">
                        <tr>
                          <th class="border-0" style="width: 50px;"></th>
                          <th class="border-0">Sản phẩm</th>
                          <th class="border-0 text-center">Đã mua</th>
                          <th class="border-0 text-center">Số lượng trả</th>
                          <th class="border-0">Lý do trả</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="item in returnModal.chiTietHoaDon" :key="item.id">
                          <td class="border-0 text-center">
                            <div class="form-check">
                              <input class="form-check-input" type="checkbox" :id="'return-item-' + item.id"
                                v-model="item.selected" @change="updateSelectedItems">
                            </div>
                          </td>
                          <td class="border-0">
                            <div class="d-flex align-items-center">
                              <div class="flex-shrink-0 me-3" style="width: 60px; height: 60px;">
                                <img v-if="item.hinhAnh" :src="getImageUrl(item.hinhAnh)" :alt="item.tenSanPham"
                                  class="w-100 h-100 object-fit-cover rounded border"
                                  @error="$event.target.src = '/default-avatar.png'">
                                <div v-else
                                  class="w-100 h-100 bg-light rounded d-flex align-items-center justify-content-center">
                                  <i class="fas fa-image text-muted"></i>
                                </div>
                              </div>
                              <div class="flex-grow-1">
                                <div class="fw-medium">{{ item.tenSanPham }}</div>
                                <small class="text-muted">{{ item.mauSac }} - {{ item.kichThuoc }}</small>
                              </div>
                            </div>
                          </td>
                          <td class="border-0 text-center">
                            <span class="badge bg-primary rounded-pill">{{ item.soLuong }}</span>
                          </td>
                          <td class="border-0 text-center">
                            <input type="number" class="form-control form-control-sm" v-model.number="item.soLuongTra"
                              :disabled="!item.selected" :min="1" :max="item.soLuong"
                              @input="validateReturnQuantity(item)" :class="{ 'is-invalid': item.error }">
                            <div v-if="item.error" class="invalid-feedback">
                              {{ item.error }}
                            </div>
                          </td>
                          <td class="border-0">
                            <input type="text" class="form-control form-control-sm" v-model="item.lyDo"
                              :disabled="!item.selected" placeholder="Lý do trả sản phẩm này...">
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>

              <!-- Upload minh chứng -->
              <div class="card border-0 bg-light mb-4">
                <div class="card-header bg-light border-0">
                  <h6 class="fw-bold mb-0">
                    <i class="fas fa-images me-2"></i>
                    Minh chứng trả hàng
                  </h6>
                </div>
                <div class="card-body">
                  <div class="mb-3">
                    <label class="form-label">Hình ảnh/Video minh chứng <span class="text-danger">*</span></label>
                    <div class="input-group mb-3">
                      <input type="file" class="form-control" id="mediaFiles" multiple accept="image/*,video/*"
                        @change="handleFileUpload" :class="{ 'is-invalid': returnModal.errors.media }">
                      <label class="input-group-text" for="mediaFiles">Tải lên</label>
                    </div>
                    <div v-if="returnModal.errors.media" class="text-danger small">
                      {{ returnModal.errors.media }}
                    </div>
                    <small class="text-muted">
                      <i class="fas fa-info-circle me-1"></i>
                      Hỗ trợ định dạng: JPG, PNG, GIF, MP4. Tối đa 5 file, mỗi file không quá 5MB.
                    </small>
                  </div>

                  <!-- Preview hình ảnh/video đã chọn -->
                  <div v-if="returnModal.mediaFiles.length > 0" class="row g-2 mt-2">
                    <div v-for="(file, index) in returnModal.mediaFiles" :key="index" class="col-md-3 col-sm-4 col-6">
                      <div class="position-relative border rounded h-100">
                        <!-- Preview cho ảnh -->
                        <img v-if="file.type.startsWith('image/')" :src="file.preview" class="img-fluid rounded"
                          alt="Preview">
                        <!-- Preview cho video -->
                        <video v-else-if="file.type.startsWith('video/')" class="img-fluid rounded" controls>
                          <source :src="file.preview" :type="file.type">
                          Trình duyệt không hỗ trợ video.
                        </video>
                        <!-- Nút xóa -->
                        <button @click="removeFile(index)"
                          class="btn btn-sm btn-danger position-absolute top-0 end-0 m-1 rounded-circle"
                          style="width: 24px; height: 24px; padding: 0; line-height: 24px;">
                          <i class="fas fa-times"></i>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="mb-4">
                <label for="lyDoTra" class="form-label fw-bold">
                  <i class="fas fa-comment me-2"></i>
                  Lý do trả hàng chung <span class="text-danger">*</span>
                </label>
                <textarea id="lyDoTra" v-model="returnModal.lyDo" class="form-control" rows="4"
                  placeholder="Vui lòng nhập lý do trả hàng chung..."
                  :class="{ 'is-invalid': returnModal.errors.lyDo }"></textarea>
                <div v-if="returnModal.errors.lyDo" class="invalid-feedback">
                  {{ returnModal.errors.lyDo }}
                </div>
                <small class="text-muted">
                  <i class="fas fa-info-circle me-1"></i>
                  Lý do trả hàng sẽ được gửi đến admin để xem xét
                </small>
              </div>
            </div>

            <!-- Footer -->
            <div class="modal-footer border-0">
              <button @click="closeReturnModal" class="btn btn-secondary btn-lg px-4">
                <i class="fas fa-times me-2"></i>
                Hủy bỏ
              </button>
              <button @click="submitReturnRequest" class="btn btn-warning btn-lg px-4"
                :disabled="returnModal.isSubmitting || !hasSelectedItems">
                <span v-if="returnModal.isSubmitting" class="spinner-border spinner-border-sm me-2"
                  role="status"></span>
                <i v-else class="fas fa-check me-2"></i>
                {{ returnModal.isSubmitting ? 'Đang gửi...' : 'Xác nhận trả hàng' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import DanhSachTraHang from './DanhSachTraHang.vue'

const hoaDons = ref([])
const chiTiet = ref(null)
const isLoading = ref(true)

// Biến để toggle hiển thị section yêu cầu trả hàng
const showReturnRequestsSection = ref(false)

// Modal hủy đơn hàng
const cancelModal = ref({
  show: false,
  hoaDon: null,
  lyDo: '',
  isSubmitting: false,
  errors: {}
})

// Modal trả hàng
const returnModal = ref({
  show: false,
  hoaDon: null,
  lyDo: '',
  chiTietHoaDon: [],
  mediaFiles: [],
  isSubmitting: false,
  errors: {}
})

// Biến để kiểm tra xem có sản phẩm nào được chọn không
const hasSelectedItems = ref(false)

const token = localStorage.getItem('authToken') || localStorage.getItem('token')
if (!token) {
  window.location.href = '/login'
}

const axiosInstance = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    Authorization: `Bearer ${token}`,
  }
})

onMounted(async () => {
  try {
    // Thêm tham số để đảm bảo sắp xếp theo thời gian mới nhất và lấy nhiều hơn
    const res = await axiosInstance.get('/khach-hang/hoa-don/me', {
      params: {
        page: 0,
        size: 50, // Tăng số lượng để hiển thị nhiều đơn hàng hơn
        sort: 'ngayTao,desc' // Đảm bảo sắp xếp theo ngày tạo giảm dần
      }
    })
    
    // Lấy dữ liệu và sắp xếp lại một lần nữa ở frontend để đảm bảo
    let hoaDonList = res.data.content || []
    
    // Sắp xếp theo ngày tạo mới nhất lên đầu (backup sorting)
    hoaDonList.sort((a, b) => {
      const dateA = new Date(a.ngayTao)
      const dateB = new Date(b.ngayTao)
      return dateB - dateA // Mới nhất lên đầu
    })
    
    hoaDons.value = hoaDonList
    console.log('✅ Đã tải', hoaDonList.length, 'hóa đơn, sắp xếp theo thời gian mới nhất')
  } catch (e) {
    console.error('Lỗi khi lấy danh sách hóa đơn:', e)
    if (e.response && e.response.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
  } finally {
    isLoading.value = false
  }
})

const xemChiTiet = async (id) => {
  try {
    console.log('🔄 Đang gọi API với ID:', id)
    console.log('🔗 URL:', `http://localhost:8080/khach-hang/hoa-don/theo-doi/${id}`)
    console.log('🔑 Token:', token ? 'Có token' : 'Không có token')

    const res = await axiosInstance.get(`/khach-hang/hoa-don/theo-doi/${id}`)
    console.log('✅ Response thành công:', res.data)

    // Normalize response so template can display customer fields even if nested
    chiTiet.value = normalizeChiTiet(res.data)
  } catch (e) {
    console.error('❌ Lỗi khi xem chi tiết hóa đơn:', e)
    console.error('📊 Status:', e.response?.status)
    console.error('📄 Response data:', e.response?.data)
    console.error('🔗 URL gọi:', e.config?.url)
    console.error('📋 Headers:', e.config?.headers)

    // Hiển thị thông báo lỗi chi tiết hơn
    let errorMessage = 'Có lỗi xảy ra khi tải chi tiết hóa đơn'

    if (e.response?.status === 400) {
      errorMessage = 'Dữ liệu không hợp lệ hoặc thiếu thông tin'
    } else if (e.response?.status === 401) {
      errorMessage = 'Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại'
      localStorage.removeItem('token')
      window.location.href = '/login'
      return
    } else if (e.response?.status === 403) {
      errorMessage = 'Không có quyền truy cập hóa đơn này'
    } else if (e.response?.status === 404) {
      errorMessage = 'Không tìm thấy hóa đơn'
    } else if (e.response?.status === 500) {
      errorMessage = 'Lỗi server, vui lòng thử lại sau'
    }

    alert(errorMessage)
  }
}

const formatDate = (dateStr) => {
  return dateStr ? new Date(dateStr).toLocaleDateString('vi-VN') : ''
}

const formatCurrency = (val) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(val || 0)
}

const getTimeAgo = (dateStr) => {
  if (!dateStr) return ''

  const now = new Date()
  const date = new Date(dateStr)
  const diffInHours = Math.floor((now - date) / (1000 * 60 * 60))

  if (diffInHours < 1) return 'Vừa xong'
  if (diffInHours < 24) return `${diffInHours} giờ trước`

  const diffInDays = Math.floor(diffInHours / 24)
  if (diffInDays < 7) return `${diffInDays} ngày trước`

  const diffInWeeks = Math.floor(diffInDays / 7)
  if (diffInWeeks < 4) return `${diffInWeeks} tuần trước`

  const diffInMonths = Math.floor(diffInDays / 30)
  return `${diffInMonths} tháng trước`
}

const getImageUrl = (tenAnh) => {
  if (!tenAnh) {
    console.log('No image path provided')
    return '/default-avatar.png'
  }
  
  // Nếu đã là URL đầy đủ thì trả về luôn
  if (tenAnh.startsWith('http://') || tenAnh.startsWith('https://')) {
    return tenAnh
  }
  
  // Sử dụng base URL từ environment hoặc mặc định
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  
  // Kiểm tra nếu path đã chứa 'uploads' thì không thêm nữa
  let fullUrl
  if (tenAnh.startsWith('uploads/') || tenAnh.startsWith('/uploads/')) {
    fullUrl = `${baseUrl}/${tenAnh.replace(/^\/+/, '')}`
  } else {
    // Ảnh sản phẩm nằm trong thư mục uploads/san_pham
    fullUrl = `${baseUrl}/uploads/san_pham/${tenAnh}`
  }
  
  console.log('Generated image URL:', fullUrl)
  return fullUrl
}

// Normalize API response for invoice detail so the template can read common customer fields
const normalizeChiTiet = (data) => {
  if (!data) return data

  const copy = { ...data }
  // Map common top-level DTO field names from backend to the template's expected names
  if (data.tenKhachHang) copy.hoTen = copy.hoTen || data.tenKhachHang
  if (data.sdt) copy.soDienThoai = copy.soDienThoai || data.sdt
  if (data.diaChi) copy.diaChi = copy.diaChi || data.diaChi

  // Many APIs return customer/receiver under different keys
  const customer = data.khachHang || data.nguoiNhan || data.khachHangDTO || data.nguoiNhanDTO || data.customer || data.receiver

  if (customer) {
    copy.hoTen = customer.hoTen || customer.ten || customer.fullName || copy.hoTen
    copy.soDienThoai = customer.soDienThoai || customer.sdt || customer.phone || copy.soDienThoai
    copy.email = customer.email || customer.mail || copy.email
    copy.diaChi = customer.diaChi || customer.address || copy.diaChi

    copy.tenTinhThanh = customer.tenTinhThanh || customer.provinceName || (customer.tinhThanh && customer.tinhThanh.ten) || copy.tenTinhThanh
    copy.tenQuanHuyen = customer.tenQuanHuyen || customer.districtName || (customer.quanHuyen && customer.quanHuyen.ten) || copy.tenQuanHuyen
  }

  // Normalize product list
  copy.chiTietHoaDonList = data.chiTietHoaDonList || data.chiTietHoaDon || data.items || data.details || []

  return copy
}

const getStatusText = (status) => {
  const statuses = [
    'Chờ xác nhận', // 0
    'Đã xác nhận', //1
    'Chờ vận chuyển', //2
    'Đang vận chuyển', //3
    'Đã giao hàng', //4
    'Hoàn thành', //5
    'Đã hủy',      //6
    'Đang yêu cầu Trả hàng',    //7
    'Đang trả hàng', //8
    'Trả hàng thành công', //9
    'Từ chối trả hàng' //10
  ];
  return statuses[status] || 'Không xác định';
}

const getStatusClass = (status) => {
  const classMap = {
    0: 'badge bg-warning text-dark',
    1: 'badge bg-success',
    2: 'badge bg-warning text-dark',
    3: 'badge bg-info',
    4: 'badge bg-success',
    5: 'badge bg-success',
    6: 'badge bg-danger',
    7: 'badge bg-warning text-dark',
    8: 'badge bg-info',
    9: 'badge bg-success',
    10: 'badge bg-danger'
  }
  return classMap[status] || 'badge bg-secondary'
}

// Chức năng hủy đơn hàng
const canCancelOrder = (status) => {
  // Chỉ cho phép hủy đơn hàng ở trạng thái: Chờ xác nhận (0), Đã xác nhận (1)
  return status === 0 || status === 1
}

const canReturnOrder = (status) => {
  // Chỉ cho phép trả hàng khi đã giao hàng (4) hoặc hoàn thành (5)
  return status === 4 || status === 5
}

const openCancelModal = (hoaDon) => {
  cancelModal.value = {
    show: true,
    hoaDon: hoaDon,
    lyDo: '',
    isSubmitting: false,
    errors: {}
  }
}

const closeCancelModal = () => {
  cancelModal.value = {
    show: false,
    hoaDon: null,
    lyDo: '',
    isSubmitting: false,
    errors: {}
  }
}

const openReturnModal = async (hoaDon) => {
  try {
    // Lấy chi tiết hóa đơn nếu chưa có
    if (!hoaDon.chiTietHoaDonList) {
      const res = await axiosInstance.get(`/khach-hang/hoa-don/theo-doi/${hoaDon.id}`)
      hoaDon = normalizeChiTiet(res.data)
    }

    console.log('🔍 Debug hoaDon data:', hoaDon)
    console.log('🔍 Debug chiTietHoaDonList:', hoaDon.chiTietHoaDonList)

    // Khởi tạo danh sách sản phẩm có thể trả - sử dụng trực tiếp dữ liệu từ API chính
    const chiTietHoaDon = (hoaDon.chiTietHoaDonList || []).map((item) => {
      console.log('🔍 Debug item structure:', item)
      console.log('🔍 Debug item keys:', Object.keys(item))
      
      return {
        ...item,
        selected: false,
        soLuongTra: 1,
        lyDo: '',
        error: null,
        // ID của HoaDonChiTiet
        hoaDonChiTietId: item.id,
        // Sử dụng trực tiếp dữ liệu từ API chính (backend đã map đúng)
        tenSanPham: item.tenSanPham || 'Không có tên',
        mauSac: item.mauSac || 'Không có màu',
        kichThuoc: item.kichThuoc || 'Không có size',
        hinhAnh: item.hinhAnh || null,
        // ID chi tiết sản phẩm
        chiTietSanPhamId: item.chiTietSanPhamId || item.id
      }
    })

    console.log('🔍 Debug processed chiTietHoaDon:', chiTietHoaDon)

    returnModal.value = {
      show: true,
      hoaDon: hoaDon,
      lyDo: '',
      chiTietHoaDon,
      mediaFiles: [],
      isSubmitting: false,
      errors: {}
    }

    hasSelectedItems.value = false
  } catch (error) {
    console.error('Lỗi khi lấy chi tiết hóa đơn:', error)
    alert('Không thể tải thông tin chi tiết đơn hàng. Vui lòng thử lại sau.')
  }
}

const closeReturnModal = () => {
  returnModal.value = {
    show: false,
    hoaDon: null,
    lyDo: '',
    chiTietHoaDon: [],
    mediaFiles: [],
    isSubmitting: false,
    errors: {}
  }
  hasSelectedItems.value = false
}

// Cập nhật trạng thái khi chọn/bỏ chọn sản phẩm
const updateSelectedItems = () => {
  hasSelectedItems.value = returnModal.value.chiTietHoaDon.some(item => item.selected)
}

// Kiểm tra số lượng trả hợp lệ
const validateReturnQuantity = (item) => {
  if (!item.selected) return

  if (!item.soLuongTra || item.soLuongTra < 1) {
    item.error = 'Số lượng phải lớn hơn 0'
    item.soLuongTra = 1
  } else if (item.soLuongTra > item.soLuong) {
    item.error = `Tối đa ${item.soLuong}`
    item.soLuongTra = item.soLuong
  } else {
    item.error = null
  }
}

// Xử lý upload file
const handleFileUpload = (event) => {
  const files = Array.from(event.target.files)

  // Kiểm tra số lượng file
  if (returnModal.value.mediaFiles.length + files.length > 5) {
    returnModal.value.errors.media = 'Chỉ được tải lên tối đa 5 file'
    return
  }

  // Kiểm tra kích thước và loại file
  const invalidFiles = files.filter(file => {
    const isValidType = file.type.startsWith('image/') || file.type.startsWith('video/')
    const isValidSize = file.size <= 5 * 1024 * 1024 // 5MB
    return !isValidType || !isValidSize
  })

  if (invalidFiles.length > 0) {
    returnModal.value.errors.media = 'Chỉ chấp nhận ảnh/video và kích thước tối đa 5MB'
    return
  }

  // Xóa thông báo lỗi nếu có
  returnModal.value.errors.media = null

  // Thêm file vào danh sách và tạo preview
  files.forEach(file => {
    const reader = new FileReader()
    reader.onload = (e) => {
      returnModal.value.mediaFiles.push({
        file,
        type: file.type,
        preview: e.target.result
      })
    }
    reader.readAsDataURL(file)
  })
}

// Xóa file đã chọn
const removeFile = (index) => {
  returnModal.value.mediaFiles.splice(index, 1)
}

const validateCancelForm = () => {
  const errors = {}

  if (!cancelModal.value.lyDo.trim()) {
    errors.lyDo = 'Vui lòng nhập lý do hủy đơn hàng'
  } else if (cancelModal.value.lyDo.trim().length < 10) {
    errors.lyDo = 'Lý do hủy phải có ít nhất 10 ký tự'
  } else if (cancelModal.value.lyDo.trim().length > 500) {
    errors.lyDo = 'Lý do hủy không được quá 500 ký tự'
  }

  cancelModal.value.errors = errors
  return Object.keys(errors).length === 0
}

const validateReturnForm = () => {
  const errors = {}

  // Kiểm tra lý do chung
  if (!returnModal.value.lyDo.trim()) {
    errors.lyDo = 'Vui lòng nhập lý do trả hàng chung'
  } else if (returnModal.value.lyDo.trim().length < 10) {
    errors.lyDo = 'Lý do trả hàng phải có ít nhất 10 ký tự'
  } else if (returnModal.value.lyDo.trim().length > 500) {
    errors.lyDo = 'Lý do trả hàng không được quá 500 ký tự'
  }

  // Kiểm tra có sản phẩm nào được chọn không
  const selectedItems = returnModal.value.chiTietHoaDon.filter(item => item.selected)
  if (selectedItems.length === 0) {
    errors.items = 'Vui lòng chọn ít nhất một sản phẩm để trả'
  } else {
    // Kiểm tra từng sản phẩm đã chọn
    const invalidItems = selectedItems.filter(item => {
      return !item.soLuongTra || item.soLuongTra < 1 || item.soLuongTra > item.soLuong || !item.lyDo.trim()
    })

    if (invalidItems.length > 0) {
      errors.items = 'Vui lòng nhập đầy đủ số lượng và lý do cho từng sản phẩm trả'
    }
  }

  // Kiểm tra có file minh chứng không
  if (returnModal.value.mediaFiles.length === 0) {
    errors.media = 'Vui lòng tải lên ít nhất một hình ảnh/video minh chứng'
  }

  returnModal.value.errors = errors
  return Object.keys(errors).length === 0
}

const submitCancelRequest = async () => {
  if (!validateCancelForm()) return

  try {
    cancelModal.value.isSubmitting = true

    const response = await axiosInstance.post('/khach-hang/hoa-don/huy', {
      hoaDonId: cancelModal.value.hoaDon.id,
      lyDo: cancelModal.value.lyDo
    })

    // Cập nhật trạng thái đơn hàng trong danh sách
    const index = hoaDons.value.findIndex(h => h.id === cancelModal.value.hoaDon.id)
    if (index !== -1) {
      hoaDons.value[index].trangThai = 6 // Đã hủy
    }

    // Đóng modal và thông báo thành công
    closeCancelModal()
    alert('Yêu cầu hủy đơn hàng đã được gửi thành công!')

  } catch (error) {
    console.error('Lỗi khi hủy đơn hàng:', error)

    let errorMessage = 'Có lỗi xảy ra khi hủy đơn hàng. Vui lòng thử lại sau.'

    if (error.response?.status === 400) {
      errorMessage = 'Dữ liệu không hợp lệ hoặc thiếu thông tin'
    } else if (error.response?.status === 401) {
      errorMessage = 'Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại'
      localStorage.removeItem('token')
      window.location.href = '/login'
      return
    } else if (error.response?.status === 403) {
      errorMessage = 'Không có quyền hủy đơn hàng này'
    } else if (error.response?.status === 404) {
      errorMessage = 'Không tìm thấy đơn hàng'
    } else if (error.response?.status === 409) {
      errorMessage = 'Đơn hàng không thể hủy ở trạng thái hiện tại'
    } else if (error.response?.status === 500) {
      errorMessage = 'Lỗi server, vui lòng thử lại sau'
    }

    alert(errorMessage)
  } finally {
    cancelModal.value.isSubmitting = false
  }
}

const submitReturnRequest = async () => {
  if (!validateReturnForm()) return

  try {
    returnModal.value.isSubmitting = true

    // Chuẩn bị dữ liệu gửi lên
    const selectedItems = returnModal.value.chiTietHoaDon.filter(item => item.selected)

    // Tạo form data để gửi cả file và dữ liệu
    const formData = new FormData()
    
    // Tạo đối tượng DonTraHangRequest theo cấu trúc backend mong đợi
    const donTraHangRequest = {
      hoaDonId: returnModal.value.hoaDon.id,
      lyDo: returnModal.value.lyDo,
      ghiChu: returnModal.value.lyDo, // Có thể dùng chung với lyDo
      chiTietTraHangList: selectedItems.map(item => ({
        hoaDonChiTietId: item.id,
        // Không cần gửi chiTietSanPhamId vì backend sẽ lấy từ hoaDonChiTiet
        soLuong: item.soLuongTra,
        lyDo: item.lyDo
      }))
    }

    // Thêm đối tượng request dưới dạng JSON blob với Content-Type
    const donTraHangBlob = new Blob([JSON.stringify(donTraHangRequest)], {
      type: 'application/json'
    })
    formData.append('donTraHang', donTraHangBlob)

    // Thêm media files - phân loại thành images và videos
    const images = []
    const videos = []
    
    returnModal.value.mediaFiles.forEach(media => {
      if (media.file.type.startsWith('image/')) {
        images.push(media.file)
      } else if (media.file.type.startsWith('video/')) {
        videos.push(media.file)
      }
    })

    // Thêm images
    images.forEach(image => {
      formData.append('images', image)
    })

    // Thêm videos  
    videos.forEach(video => {
      formData.append('videos', video)
    })

    console.log('🔍 Debug donTraHangRequest:', donTraHangRequest)
    console.log('📋 Chi tiết trả hàng:', donTraHangRequest.chiTietTraHangList)
    console.log('🔍 Debug token:', token)
    console.log('🔍 Debug formData entries:')
    for (let [key, value] of formData.entries()) {
      console.log(`${key}:`, value)
    }

    // Gọi API trả hàng
    const response = await axiosInstance.post('/api/tra-hang', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    console.log('Yêu cầu trả hàng thành công:', response.data)

    // Cập nhật trạng thái đơn hàng trong danh sách
    const index = hoaDons.value.findIndex(h => h.id === returnModal.value.hoaDon.id)
    if (index !== -1) {
      hoaDons.value[index].trangThai = 7 // Đang yêu cầu trả hàng
      hoaDons.value[index].coTheTraHang = false
    }

    // Đóng modal và thông báo thành công
    closeReturnModal()
    alert('Yêu cầu trả hàng đã được gửi thành công!')

  } catch (error) {
    console.error('Lỗi khi gửi yêu cầu trả hàng:', error)
    console.error('Response data:', error.response?.data)
    console.error('Response status:', error.response?.status)

    let errorMessage = 'Có lỗi xảy ra khi gửi yêu cầu trả hàng. Vui lòng thử lại sau.'

    if (error.response?.status === 400) {
      // Hiển thị thông báo lỗi chi tiết từ backend nếu có
      errorMessage = error.response?.data?.message || 'Dữ liệu không hợp lệ hoặc thiếu thông tin'
    } else if (error.response?.status === 401) {
      errorMessage = 'Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại'
      localStorage.removeItem('token')
      window.location.href = '/login'
      return
    } else if (error.response?.status === 403) {
      errorMessage = 'Không có quyền trả hàng đơn hàng này'
    } else if (error.response?.status === 404) {
      errorMessage = 'Không tìm thấy đơn hàng'
    } else if (error.response?.status === 409) {
      errorMessage = 'Đơn hàng không thể trả hàng ở trạng thái hiện tại'
    } else if (error.response?.status === 500) {
      errorMessage = 'Lỗi server, vui lòng thử lại sau'
    }

    alert(errorMessage)
  } finally {
    returnModal.value.isSubmitting = false
  }
}

// Method để chuyển đến trang danh sách yêu cầu trả hàng
const showReturnRequests = () => {
  // Toggle hiển thị danh sách yêu cầu trả hàng
  showReturnRequestsSection.value = !showReturnRequestsSection.value
}

// Hàm xử lý lỗi khi load ảnh
const handleImageError = (event) => {
  console.log('Image load error, using fallback image')
  event.target.src = '/default-avatar.png'
  // Thêm class để style khác biệt nếu cần
  event.target.classList.add('fallback-image')
}

</script>

<style scoped>
/* Custom styles for better appearance */
.card {
  transition: all 0.3s ease;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15) !important;
}

.btn {
  transition: all 0.3s ease;
}

.btn:hover {
  transform: translateY(-1px);
}

/* Modal animation */
.modal.show {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* Table improvements */
.table th {
  font-weight: 600;
  color: #495057;
}

.table td {
  vertical-align: middle;
}

/* Badge improvements */
.badge {
  font-size: 0.75rem;
  padding: 0.375rem 0.75rem;
}

/* Fallback image styling */
.fallback-image {
  opacity: 0.7;
  filter: grayscale(20%);
}

/* Responsive improvements */
@media (max-width: 768px) {
  .modal-xl .modal-dialog {
    max-width: 95%;
    margin: 1rem auto;
  }
  
  .card-body {
    padding: 1rem;
  }
  
  .btn-lg {
    padding: 0.5rem 1rem;
    font-size: 0.9rem;
  }
}

/* Object fit for images */
.object-fit-cover {
  object-fit: cover;
}
</style>
