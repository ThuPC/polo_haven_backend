<template>
  <div class="container-fluid py-4">
    <!-- Header -->
    <div class="row">
      <div class="col-12">
        <div class="card mb-4">
          <div class="card-header pb-0">
            <div class="d-lg-flex">
              <div>
                <h5 class="mb-0">Quản lý đơn trả hàng</h5>
                <p class="text-sm mb-0">
                  Quản lý và xử lý các yêu cầu trả hàng từ khách hàng
                </p>
              </div>
              <div class="ms-auto my-auto mt-lg-0 mt-4">
                <div class="ms-auto my-auto">
                  <div class="input-group">
                    <span class="input-group-text text-body"><i class="fas fa-search" aria-hidden="true"></i></span>
                    <input 
                      type="text" 
                      class="form-control" 
                      placeholder="Tìm kiếm..." 
                      v-model="searchQuery"
                      @input="handleSearch">
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="card-body px-0 pb-0">
            <div class="tab-content">
              <div class="tab-pane fade show active">
                <!-- Filter Tabs -->
                <div class="nav-wrapper position-relative end-0">
                  <ul class="nav nav-pills nav-fill p-1" role="tablist">
                    <li class="nav-item" v-for="(filter, index) in statusFilters" :key="index">
                      <a 
                        class="nav-link mb-0 px-0 py-1" 
                        :class="{ active: selectedStatus === filter.value }"
                        @click="filterByStatus(filter.value)"
                        role="tab" 
                        aria-selected="true">
                        <i class="ni ni-app"></i>
                        <span class="ms-2">{{ filter.label }}</span>
                      </a>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-body text-center py-5">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">Loading...</span>
            </div>
            <p class="mt-2 text-muted">Đang tải dữ liệu...</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else-if="returnOrders.length === 0" class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-body text-center py-5">
            <i class="ni ni-cart text-muted" style="font-size: 4rem;"></i>
            <h4 class="mt-3">Không có đơn trả hàng nào</h4>
            <p class="text-muted">
              {{ selectedStatus === 'all' ? 'Hiện không có đơn trả hàng nào.' : 'Không có đơn trả hàng nào ở trạng thái này.' }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Data Table -->
    <div v-else class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-header pb-0">
            <h6>Danh sách đơn trả hàng</h6>
          </div>
          <div class="card-body px-0 pt-0 pb-2">
            <div class="table-responsive p-0">
              <table class="table align-items-center mb-0">
                <thead>
                  <tr>
                    <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7">Mã đơn</th>
                    <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2">Khách hàng</th>
                    <th class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7">Ngày yêu cầu</th>
                    <th class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7">Tổng tiền</th>
                    <th class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7">Trạng thái</th>
                    <th class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7">Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="order in returnOrders" :key="order.id">
                    <td>
                      <div class="d-flex px-2 py-1">
                        <div>
                          <img :src="getRepresentativeImage(order)" class="avatar avatar-sm me-3" alt="product" style="object-fit: cover;" @error="handleImageError">
                        </div>
                        <div class="d-flex flex-column justify-content-center">
                          <h6 class="mb-0 text-sm">{{ order.maDonTraHang || order.id }}</h6>
                          <p class="text-xs text-secondary mb-0">Mã HĐ: {{ order.maHoaDon || 'N/A' }}</p>
                        </div>
                      </div>
                    </td>
                    <td>
                      <div class="d-flex flex-column justify-content-center">
                        <h6 class="mb-0 text-sm">{{ order.tenKhachHang || 'Không có thông tin' }}</h6>
                        <p class="text-xs text-secondary mb-0">{{ order.sdt || 'Không có SĐT' }}</p>
                      </div>
                    </td>
                    <td class="align-middle text-center text-sm">
                      <span class="text-secondary text-xs font-weight-bold">{{ formatDate(order.ngayTao) }}</span>
                    </td>
                    <td class="align-middle text-center">
                      <span class="text-secondary text-xs font-weight-bold">{{ calculateTotalAmount(order) }}</span>
                    </td>
                    <td class="align-middle text-center text-sm">
                      <span :class="getStatusBadgeClass(order.trangThai)">
                        {{ getStatusText(order.trangThai) }}
                      </span>
                    </td>
                    <td class="align-middle text-center">
                      <button 
                        class="btn btn-link text-dark px-3 mb-0" 
                        type="button"
                        @click.prevent="showDetails(order)">
                        <i class="fas fa-pencil-alt text-dark me-2" aria-hidden="true"></i>Chi tiết
                      </button>
                      <!-- <button 
                        v-if="order.trangThai === 'YEU_CAU_TRA_HANG' || order.trangThai === 0" 
                        type="button"
                        class="btn btn-sm btn-success me-1" 
                        @click.prevent="approveReturn(order)"
                        title="Chấp nhận đơn trả hàng">
                        <i class="ni ni-check-bold me-1"></i>Chấp nhận
                      </button>
                      <button 
                        v-if="order.trangThai === 'YEU_CAU_TRA_HANG' || order.trangThai === 0" 
                        type="button"
                        class="btn btn-sm btn-danger me-1" 
                        @click.prevent="rejectReturn(order)"
                        title="Từ chối đơn trả hàng">
                        <i class="ni ni-fat-remove me-1"></i>Từ chối
                      </button> -->
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Chi tiết đơn trả hàng Modal -->
    <div class="modal fade" id="detailsModal" tabindex="-1" role="dialog" aria-labelledby="detailsModalLabel" aria-hidden="true">
      <div class="modal-dialog modal-xl modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header bg-gradient-primary text-white">
            <div class="d-flex align-items-center">
              <i class="ni ni-archive-2 me-2"></i>
              <div>
                <h5 class="modal-title mb-0" id="detailsModalLabel">Chi tiết đơn trả hàng</h5>
                <small class="opacity-8">{{ selectedOrder?.id || 'N/A' }}</small>
              </div>
            </div>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body p-0" v-if="selectedOrder">
            <!-- Status Timeline -->
            <div class="card border-0 mb-0">
              <div class="card-body bg-light">
                <div class="row align-items-center">
                  <div class="col-md-8">
                    <div class="d-flex align-items-center">
                      <div class="me-3">
                        <div class="avatar avatar-lg bg-gradient-primary rounded-circle">
                          <i class="ni ni-delivery-fast text-white"></i>
                        </div>
                      </div>
                      <div>
                        <h6 class="mb-1">Trạng thái hiện tại</h6>
                        <span :class="getStatusBadgeClass(selectedOrder?.trangThai)" style="font-size: 0.875rem;">
                          {{ getStatusText(selectedOrder?.trangThai) }}
                        </span>
                        <p class="text-sm text-muted mb-0 mt-1">
                          Yêu cầu từ {{ getTimeAgo(selectedOrder?.ngayTao) }}
                        </p>
                      </div>
                    </div>
                  </div>
                  <!-- <div class="col-md-4 text-end">
                    <h6 class="text-primary mb-0">Tổng tiền trả</h6>
                    <h4 class="text-primary mb-0">{{ formatCurrency(selectedOrder?.tongTien || 0) }}</h4>
                  </div> -->
                </div>
              </div>
            </div>

            <div class="p-4">
              <!-- Thông tin đơn hàng gốc -->
              <div class="card mb-4 border-0 shadow-sm">
                <div class="card-header bg-gradient-success text-white">
                  <div class="d-flex align-items-center">
                    <i class="ni ni-bag-17 me-2"></i>
                    <h6 class="mb-0">Thông tin đơn hàng gốc</h6>
                  </div>
                </div>
                <div class="card-body">
                  <div class="row">
                    <div class="col-md-4">
                      <div class="info-item">
                        <label class="form-label text-sm text-muted mb-1">Mã đơn hàng</label>
                        <p class="text-dark font-weight-bold mb-0">{{ selectedOrder?.maHoaDon || 'N/A' }}</p>
                      </div>
                    </div>
                    <div class="col-md-4">
                      <div class="info-item">
                        <label class="form-label text-sm text-muted mb-1">Ngày đặt hàng</label>
                        <p class="text-dark mb-0">{{ formatDate(selectedOrder?.ngayTao) }}</p>
                      </div>
                    </div>
                    <!-- <div class="col-md-3"> -->
                      <!-- <div class="info-item">
                        <label class="form-label text-sm text-muted mb-1">Tổng tiền trả hàng</label>
                        <p class="text-success font-weight-bold mb-0">{{ calculateTotalAmount(selectedOrder?.chiTietTraHangList) }}</p>
                      </div> -->
                    <!-- </div> -->
                    <div class="col-md-4">
                      <div class="info-item">
                        <label class="form-label text-sm text-muted mb-1">Trạng thái</label>
                        <p class="text-dark mb-0">{{ getStatusText(selectedOrder?.trangThai) }}</p>
                      </div>
                    </div>
                  </div>
                  <div class="row mt-3" v-if="selectedOrder?.lyDo">
                    <div class="col-12">
                      <div class="info-item">
                        <label class="form-label text-sm text-muted mb-1">Lý do trả hàng</label>
                        <div class="bg-light p-3 rounded">
                          <p class="text-dark mb-0">{{ selectedOrder?.lyDo }}</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Thông tin khách hàng -->
              <div class="card mb-4 border-0 shadow-sm">
                <div class="card-header bg-gradient-success text-white">
                  <div class="d-flex align-items-center">
                    <i class="ni ni-single-02 me-2"></i>
                    <h6 class="mb-0">Thông tin khách hàng</h6>
                  </div>
                </div>
                <div class="card-body">
                  <div class="row">
                    <div class="col-md-4">
                      <div class="info-item">
                        <label class="form-label text-sm text-muted mb-1">Tên khách hàng</label>
                        <p class="text-dark font-weight-bold mb-0">{{ selectedOrder?.tenKhachHang || 'Không có thông tin' }}</p>
                      </div>
                    </div>
                    <div class="col-md-4">
                      <div class="info-item">
                        <label class="form-label text-sm text-muted mb-1">Email</label>
                        <p class="text-dark mb-0">{{ selectedOrder?.email || 'Không có thông tin' }}</p>
                      </div>
                    </div>
                    <div class="col-md-4">
                      <div class="info-item">
                        <label class="form-label text-sm text-muted mb-1">Số điện thoại</label>
                        <p class="text-dark mb-0">{{ selectedOrder?.sdt || 'Không có thông tin' }}</p>
                      </div>
                    </div>
                  </div>
                  <div class="row mt-3" v-if="selectedOrder?.hoaDon?.diaChi">
                    <div class="col-12">
                      <div class="info-item">
                        <label class="form-label text-sm text-muted mb-1">Địa chỉ giao hàng</label>
                        <p class="text-dark mb-0">{{ selectedOrder?.hoaDon?.diaChi }}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Chi tiết sản phẩm trả -->
              <div class="card mb-4 border-0 shadow-sm">
                <div class="card-header bg-gradient-success text-white">
                  <div class="d-flex align-items-center justify-content-between">
                    <div class="d-flex align-items-center">
                      <i class="ni ni-box-2 me-2"></i>
                      <h6 class="mb-0">Chi tiết sản phẩm trả hàng</h6>
                    </div>
                    <span class="badge bg-white text-dark">
                       {{ selectedOrder?.chiTietTraHangList?.length || 0 }} sản phẩm
                     </span>
                  </div>
                </div>
                <div class="card-body p-0">
                  <div class="table-responsive">
                    <table class="table table-hover mb-0">
                      <thead class="bg-light">
                        <tr>
                          <th class="border-0 text-uppercase text-secondary text-xxs font-weight-bolder opacity-7">Sản phẩm</th>
                          <th class="border-0 text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 text-center">Đơn giá</th>
                          <th class="border-0 text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 text-center">SL trả</th>
                          <th class="border-0 text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 text-center">Thành tiền</th>
                          <th class="border-0 text-uppercase text-secondary text-xxs font-weight-bolder opacity-7">Lý do</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="(item, index) in selectedOrder?.chiTietTraHangList" :key="index" class="border-bottom">
                          <td class="py-3">
                            <div class="d-flex align-items-center">
                              <div class="me-3">
                                <img 
                                  :src="getImageUrl(item.anhSanPham)" 
                                  class="avatar avatar-lg rounded-3 shadow-sm" 
                                  alt="product image"
                                  style="object-fit: cover;"
                                  @error="handleImageError">
                              </div>
                              <div class="d-flex flex-column justify-content-center">
                                <h6 class="mb-1 text-sm font-weight-bold">{{ item.tenSanPham || 'Không có thông tin' }}</h6>
                                <p class="text-xs text-secondary mb-0">
                                  <span class="badge badge-sm bg-light text-dark me-1">{{ item.mauSac || 'N/A' }}</span>
                                  <span class="badge badge-sm bg-light text-dark">{{ item.kichThuoc || 'N/A' }}</span>
                                </p>
                              </div>
                            </div>
                          </td>
                          <td class="text-center py-3">
                            <p class="text-sm font-weight-bold mb-0 text-primary">{{ formatCurrency(item.donGia) }}</p>
                          </td>
                          <td class="text-center py-3">
                            <span class="badge bg-gradient-primary">{{ item.soLuong }}</span>
                          </td>
                          <td class="text-center py-3">
                            <p class="text-sm font-weight-bold mb-0 text-success">{{ formatCurrency(item.donGia * item.soLuong) }}</p>
                          </td>
                          <td class="py-3">
                            <div class="reason-box bg-light p-2 rounded">
                              <p class="text-sm mb-0">{{ item.lyDo || 'Không có lý do cụ thể' }}</p>
                            </div>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                  <!-- Tổng cộng -->
                  <div class="bg-light p-3 border-top">
                    <div class="row align-items-center">
                      <div class="col-md-8">
                        <p class="text-sm text-muted mb-0">
                          <i class="ni ni-check-bold text-success me-1"></i>
                          Tổng cộng {{ selectedOrder?.chiTietTraHangList?.length || 0 }} sản phẩm được yêu cầu trả
                        </p>
                      </div>
                      <div class="col-md-4 text-end">
                        <h6 class="text-muted mb-0">Tổng tiền trả:</h6>
                        <h4 class="text-success mb-0 font-weight-bold">{{ calculateTotalAmount(selectedOrder) }}</h4>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Hình ảnh/Video minh chứng -->
              <div class="card mb-4 border-0 shadow-sm">
                <div class="card-header bg-gradient-success text-white">
                  <div class="d-flex align-items-center justify-content-between">
                    <div class="d-flex align-items-center">
                      <i class="ni ni-image me-2"></i>
                      <h6 class="mb-0">Minh chứng trả hàng</h6>
                    </div>
                    <span class="badge bg-white text-dark">
                      {{ selectedOrder?.mediaTraHangList?.length || 0 }} file
                    </span>
                  </div>
                </div>
                <div class="card-body">
                  <div v-if="selectedOrder?.mediaTraHangList && selectedOrder.mediaTraHangList.length > 0" class="row g-3">
                    <div v-for="(media, index) in selectedOrder.mediaTraHangList" :key="index" class="col-lg-3 col-md-4 col-sm-6">
                      <div class="card border-0 shadow-sm h-100">
                        <!-- Hiển thị hình ảnh -->
                        <div 
                          v-if="isImage(media.duongDan)" 
                          class="position-relative overflow-hidden rounded-top"
                          style="height: 180px; cursor: pointer;"
                          @click="openMediaPreview(media)">
                          <img 
                            :src="getMediaUrl(media.duongDan)" 
                            class="w-100 h-100" 
                            alt="Minh chứng"
                            style="object-fit: cover; transition: transform 0.3s ease;"
                            @mouseover="$event.target.style.transform = 'scale(1.05)'"
                            @mouseout="$event.target.style.transform = 'scale(1)'"
                            @error="$event.target.src = '/default-avatar.png'">>
                          <div class="position-absolute top-0 end-0 m-2">
                            <span class="badge bg-primary">
                              <i class="ni ni-zoom-split-in"></i>
                            </span>
                          </div>
                        </div>
                        
                        <!-- Hiển thị video -->
                        <div 
                          v-else-if="isVideo(media.duongDan)" 
                          class="position-relative bg-gradient-dark d-flex align-items-center justify-content-center rounded-top"
                          style="height: 180px; cursor: pointer;"
                          @click="openMediaPreview(media)">
                          <div class="text-center">
                            <i class="ni ni-button-play text-white" style="font-size: 3rem; opacity: 0.8;"></i>
                            <p class="text-white text-sm mt-2 mb-0">Click để phát</p>
                          </div>
                          <div class="position-absolute top-0 end-0 m-2">
                            <span class="badge bg-danger">
                              <i class="ni ni-tv-2"></i>
                            </span>
                          </div>
                        </div>
                        
                        <!-- File khác -->
                        <div 
                          v-else 
                          class="bg-light d-flex align-items-center justify-content-center rounded-top"
                          style="height: 180px;">
                          <div class="text-center">
                            <i class="ni ni-single-copy-04 text-muted" style="font-size: 3rem;"></i>
                            <p class="text-muted text-sm mt-2 mb-0">File đính kèm</p>
                          </div>
                        </div>
                        
                        <div class="card-body p-3">
                          <div class="d-flex justify-content-between align-items-center">
                            <div>
                              <p class="text-xs text-muted mb-1">
                                <i class="ni ni-calendar-grid-58 me-1"></i>
                                {{ formatDate(media.ngayTao) }}
                              </p>
                              <p class="text-xs text-muted mb-0">
                                <i class="ni ni-folder-17 me-1"></i>
                                {{ getFileExtension(media.duongDan) }}
                              </p>
                            </div>
                            <button 
                              class="btn btn-sm btn-outline-primary"
                              @click="downloadMedia(media)">
                              <i class="ni ni-cloud-download-95"></i>
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div v-else class="text-center py-5">
                    <div class="mb-3">
                      <i class="ni ni-image text-muted" style="font-size: 4rem; opacity: 0.3;"></i>
                    </div>
                    <h6 class="text-muted mb-2">Không có minh chứng</h6>
                    <p class="text-sm text-muted mb-0">Khách hàng chưa tải lên hình ảnh hoặc video minh chứng nào</p>
                  </div>
                </div>
              </div>

              <!-- Ghi chú admin (nếu có) -->
              <div v-if="selectedOrder?.ghiChuAdmin" class="card mb-4 border-0 shadow-sm">
                <div class="card-header bg-gradient-secondary text-white">
                  <div class="d-flex align-items-center">
                    <i class="ni ni-chat-round me-2"></i>
                    <h6 class="mb-0">Ghi chú từ Admin</h6>
                  </div>
                </div>
                <div class="card-body">
                  <div class="bg-light p-3 rounded">
                    <p class="mb-0">{{ selectedOrder?.ghiChuAdmin || 'Chưa có ghi chú' }}</p>
                  </div>
                </div>
              </div>
            </div>
          <div class="modal-footer bg-light border-top-0">
            <div class="d-flex justify-content-between align-items-center w-100">
              <div class="d-flex align-items-center">
                <i class="ni ni-time-alarm text-muted me-2"></i>
                <small class="text-muted">
                  Cập nhật lần cuối: {{ formatDate(selectedOrder?.ngayCapNhat || selectedOrder?.ngayTao) }}
                </small>
              </div>
              <div class="d-flex gap-2">
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">
                  <i class="ni ni-bold-left me-1"></i> Đóng
                </button>
                <button 
                  v-if="selectedOrder?.trangThai === 0" 
                  type="button" 
                  class="btn btn-success"
                  @click="approveReturn(selectedOrder)">
                  <i class="ni ni-check-bold me-1"></i> Chấp nhận trả hàng
                </button>
                <button 
                  v-if="selectedOrder?.trangThai === 0" 
                  type="button" 
                  class="btn btn-danger"
                  @click="rejectReturn(selectedOrder)">
                  <i class="ni ni-fat-remove me-1"></i> Từ chối trả hàng
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Media Preview Modal -->
    <div class="modal fade" id="mediaPreviewModal" tabindex="-1" role="dialog" aria-labelledby="mediaPreviewModalLabel" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="mediaPreviewModalLabel">Xem minh chứng</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body text-center">
            <!-- Hiển thị hình ảnh -->
            <img 
              v-if="selectedMedia && isImage(selectedMedia.duongDan)" 
              :src="getMediaUrl(selectedMedia.duongDan)" 
              class="img-fluid" 
              alt="Minh chứng">
            
            <!-- Hiển thị video -->
            <video 
              v-else-if="selectedMedia && isVideo(selectedMedia.duongDan)" 
              class="img-fluid" 
              controls>
              <source :src="getMediaUrl(selectedMedia.duongDan)" type="video/mp4">
              Trình duyệt của bạn không hỗ trợ video.
            </video>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal chọn tình trạng hàng -->
    <div class="modal fade" id="productConditionModal" tabindex="-1" role="dialog" aria-labelledby="productConditionModalLabel" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header bg-gradient-success text-white">
            <h5 class="modal-title" id="productConditionModalLabel">
              <i class="ni ni-check-bold me-2"></i>
              Đánh giá tình trạng sản phẩm trả hàng
            </h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="alert alert-info">
              <i class="ni ni-bulb-61 me-2"></i>
              Vui lòng đánh giá tình trạng từng sản phẩm để xác định có thể bán lại hay không.
            </div>
            
            <div v-if="selectedOrder?.chiTietTraHangList" class="product-condition-list">
              <div 
                v-for="(item, index) in selectedOrder.chiTietTraHangList" 
                :key="index" 
                class="card mb-3 border-0 shadow-sm">
                <div class="card-body">
                  <div class="row align-items-center">
                    <!-- Thông tin sản phẩm -->
                    <div class="col-md-6">
                      <div class="d-flex align-items-center">
                        <div class="me-3">
                          <img 
                            :src="getImageUrl(item.anhSanPham)" 
                            class="avatar avatar-lg rounded-3 shadow-sm" 
                            alt="product image"
                            style="object-fit: cover;"
                            @error="handleImageError">
                        </div>
                        <div>
                          <h6 class="mb-1 text-sm font-weight-bold">{{ item.tenSanPham }}</h6>
                          <p class="text-xs text-secondary mb-0">
                            <span class="badge badge-sm bg-light text-dark me-1">{{ item.mauSac }}</span>
                            <span class="badge badge-sm bg-light text-dark me-1">{{ item.kichThuoc }}</span>
                            <span class="badge badge-sm bg-primary">SL: {{ item.soLuong }}</span>
                          </p>
                          <p class="text-xs text-muted mb-0 mt-1">
                            <strong>Lý do trả:</strong> {{ item.lyDo }}
                          </p>
                        </div>
                      </div>
                    </div>
                    
                    <!-- Chọn tình trạng -->
                    <div class="col-md-6">
                      <label class="form-label text-sm font-weight-bold">Tình trạng hàng:</label>
                      <select 
                        v-model="productConditions[item.id]" 
                        class="form-select form-select-sm"
                        :class="getConditionClass(productConditions[item.id])">
                        <option value="">-- Chọn tình trạng --</option>
                        <option value="1">✅ Tốt - Có thể bán lại</option>
                        <option value="0">❌ Hỏng - Không thể bán lại</option>
                      </select>
                      <div class="mt-2">
                        <small 
                          v-if="productConditions[item.id] === '1'" 
                          class="text-success">
                          <i class="ni ni-check-bold me-1"></i>
                          Sản phẩm sẽ được hoàn lại kho
                        </small>
                        <small 
                          v-else-if="productConditions[item.id] === '0'" 
                          class="text-danger">
                          <i class="ni ni-fat-remove me-1"></i>
                          Sản phẩm sẽ không được hoàn lại kho
                        </small>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">
              <i class="ni ni-bold-left me-1"></i> Hủy
            </button>
            <button 
              type="button" 
              class="btn btn-success"
              @click="confirmApproveWithConditions"
              :disabled="!allConditionsSelected || processing">
              <span v-if="processing" class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
              <i class="ni ni-check-bold me-1"></i>
              Xác nhận chấp nhận
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal từ chối đơn trả hàng -->
    <div class="modal fade" id="confirmModal" tabindex="-1" role="dialog" aria-labelledby="confirmModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-gradient-danger text-white">
            <h5 class="modal-title" id="confirmModalLabel">
              <i class="ni ni-fat-remove me-2"></i>
              Xác nhận từ chối đơn trả hàng
            </h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="alert alert-warning">
              <i class="ni ni-bell-55 me-2"></i>
              Bạn có chắc chắn muốn từ chối đơn trả hàng này? Hành động này không thể hoàn tác.
            </div>
            
            <div class="form-group">
              <label for="rejectReason" class="form-label font-weight-bold">
                <i class="ni ni-chat-round me-1"></i>
                Lý do từ chối <span class="text-danger">*</span>
              </label>
              <textarea 
                id="rejectReason" 
                class="form-control" 
                v-model="rejectReason" 
                rows="4" 
                placeholder="Vui lòng nhập lý do từ chối đơn trả hàng..."
                :class="{ 'is-invalid': rejectReasonError }"></textarea>
              <div class="invalid-feedback" v-if="rejectReasonError">
                {{ rejectReasonError }}
              </div>
              <small class="form-text text-muted">
                Lý do từ chối sẽ được gửi đến khách hàng qua email.
              </small>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">
              <i class="ni ni-bold-left me-1"></i> Hủy
            </button>
            <button 
              type="button" 
              class="btn btn-danger"
              @click="confirmReject()"
              :disabled="processing">
              <span v-if="processing" class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
              <i class="ni ni-fat-remove me-1"></i>
              Xác nhận từ chối
            </button>
          </div>
        </div>
      </div>
    </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import api from '@/utils/api'
import { Modal } from 'bootstrap'

export default {
  name: 'ReturnManagement',
  setup() {
    // Data
    const returnOrders = ref([])
    const loading = ref(true)
    const selectedStatus = ref('all')
    const searchQuery = ref('')
    const selectedOrder = ref(null)
    const selectedMedia = ref(null)
    const confirmAction = ref('')
    const rejectReason = ref('')
    const rejectReasonError = ref('')
    const processing = ref(false)
    
    // Product conditions for approval
    const productConditions = ref({})
    
    // Pagination
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalItems = ref(0)
    const totalPages = computed(() => Math.ceil(totalItems.value / pageSize.value))

    // Modals
    let detailsModal = null
    let mediaPreviewModal = null
    let confirmModal = null
    let productConditionModal = null

    // Computed properties
    const allConditionsSelected = computed(() => {
      if (!selectedOrder.value?.chiTietTraHangList) return false
      return selectedOrder.value.chiTietTraHangList.every(item => 
        productConditions.value[item.id] !== undefined && 
        productConditions.value[item.id] !== ''
      )
    })

    // Status filters
    const statusFilters = [
      { label: 'Tất cả', value: 'all' },
      { label: 'Chờ xác nhận', value: 0 },
      { label: 'Đã xác nhận', value: 1 },
      { label: 'Đã từ chối', value: 2 }
    ]

    // API base URL
    const apiUrl = '/api/admin/tra-hang'

    // Methods
    const fetchReturnOrders = async () => {
      loading.value = true
      try {
        let url = apiUrl
        if (selectedStatus.value !== 'all') {
          url = `${apiUrl}/trang-thai/${selectedStatus.value}`
        }
        
        const response = await api.get(url)
        returnOrders.value = response.data.data || response.data
        totalItems.value = returnOrders.value.length
        
        // Debug: Log dữ liệu để kiểm tra cấu trúc
        console.log('=== FETCH RETURN ORDERS DEBUG ===')
        console.log('Response data:', response.data)
        console.log('Return orders:', returnOrders.value)
        if (returnOrders.value.length > 0) {
          console.log('First order structure:', returnOrders.value[0])
          console.log('First order hoaDon:', returnOrders.value[0]?.hoaDon)
          console.log('First order chiTietTraHang:', returnOrders.value[0]?.chiTietTraHang)
        }
        console.log('=== END FETCH DEBUG ===')
      } catch (error) {
        console.error('Error fetching return orders:', error)
        alert('Có lỗi xảy ra khi tải dữ liệu đơn trả hàng')
      } finally {
        loading.value = false
      }
    }

    const filterByStatus = (status) => {
      selectedStatus.value = status
      currentPage.value = 1
      fetchReturnOrders()
    }

    const handleSearch = () => {
      // Implement search functionality
      currentPage.value = 1
      // For now, just filter client-side
      // In a real app, you might want to send the search query to the server
    }

    const changePage = (page) => {
      if (page < 1 || page > totalPages.value) return
      currentPage.value = page
    }



    const showDetails = (order) => {
      console.log('showDetails called with order:', order)
      selectedOrder.value = order
      console.log('selectedOrder set to:', selectedOrder.value)
      
      // Ensure modal element exists
      const modalElement = document.getElementById('detailsModal')
      console.log('Modal element found:', modalElement)
      
      if (detailsModal) {
        console.log('Showing modal...')
        detailsModal.show()
      } else {
        console.error('detailsModal is not initialized')
      }
    }

    const openMediaPreview = (media) => {
      selectedMedia.value = media
      if (mediaPreviewModal) {
        mediaPreviewModal.show()
      }
    }

    const approveReturn = (order) => {
      console.log('approveReturn called with order:', order)
      selectedOrder.value = order
      
      // Initialize product conditions
      productConditions.value = {}
      if (order.chiTietTraHangList) {
        order.chiTietTraHangList.forEach(item => {
          productConditions.value[item.id] = ''
        })
      }
      
      console.log('productConditionModal:', productConditionModal)
      // Show product condition modal instead of direct confirmation
      if (productConditionModal) {
        console.log('Showing productConditionModal...')
        productConditionModal.show()
      } else {
        console.error('productConditionModal is not initialized')
      }
    }

    const confirmApproveWithConditions = async () => {
      if (!selectedOrder.value || !allConditionsSelected.value) return
      
      processing.value = true
      try {
        // Prepare condition map for API
        const tinhTrangHangMap = {}
        selectedOrder.value.chiTietTraHangList.forEach(item => {
          tinhTrangHangMap[item.id] = parseInt(productConditions.value[item.id])
        })
        
        // Call API with condition map
        await api.put(`${apiUrl}/${selectedOrder.value.id}/chap-nhan?ghiChuAdmin=${encodeURIComponent('Đã chấp nhận')}`, tinhTrangHangMap)
        
        // Update order status in the list
        const index = returnOrders.value.findIndex(o => o.id === selectedOrder.value.id)
        if (index !== -1) {
          returnOrders.value[index].trangThai = 1
        }
        
        // Close modals
        if (productConditionModal) {
          productConditionModal.hide()
        }
        if (detailsModal) {
          detailsModal.hide()
        }
        
        alert('Đã chấp nhận đơn trả hàng thành công!')
      } catch (error) {
        console.error('Error approving return order:', error)
        alert('Có lỗi xảy ra khi chấp nhận đơn trả hàng')
      } finally {
        processing.value = false
      }
    }

    const getConditionClass = (condition) => {
      if (condition === '1') return 'border-success'
      if (condition === '0') return 'border-danger'
      return ''
    }

    const rejectReturn = (order) => {
      console.log('rejectReturn called with order:', order)
      selectedOrder.value = order
      confirmAction.value = 'reject'
      rejectReason.value = ''
      rejectReasonError.value = ''
      
      console.log('confirmModal:', confirmModal)
      if (confirmModal) {
        console.log('Showing confirmModal...')
        confirmModal.show()
      } else {
        console.error('confirmModal is not initialized')
      }
    }



    const confirmReject = async () => {
      if (!selectedOrder.value) return
      
      // Reset error
      rejectReasonError.value = ''
      
      // Validate reason
      if (!rejectReason.value.trim()) {
        rejectReasonError.value = 'Vui lòng nhập lý do từ chối'
        return
      }
      
      if (rejectReason.value.trim().length < 10) {
        rejectReasonError.value = 'Lý do từ chối phải có ít nhất 10 ký tự'
        return
      }
      
      processing.value = true
      try {
        await api.put(`${apiUrl}/${selectedOrder.value.id}/tu-choi?ghiChuAdmin=${encodeURIComponent(rejectReason.value.trim())}`)
        
        // Update order status in the list
        const index = returnOrders.value.findIndex(o => o.id === selectedOrder.value.id)
        if (index !== -1) {
          returnOrders.value[index].trangThai = 2
          returnOrders.value[index].ghiChuAdmin = rejectReason.value.trim()
        }
        
        // Close modal
        if (confirmModal) {
          confirmModal.hide()
        }
        
        // Close details modal if open
        if (detailsModal) {
          detailsModal.hide()
        }
        
        // Reset form
        rejectReason.value = ''
        rejectReasonError.value = ''
        
        alert('Đã từ chối đơn trả hàng thành công!')
      } catch (error) {
        console.error('Error rejecting return order:', error)
        alert('Có lỗi xảy ra khi từ chối đơn trả hàng')
      } finally {
        processing.value = false
      }
    }

    // Utility functions
    const calculateTotalAmount = (order) => {
      console.log('=== DEBUG calculateTotalAmount ===')
      console.log('Order data:', order)
      
      // Kiểm tra nếu có trường tongTien trực tiếp từ backend
      if (order.tongTien !== undefined && order.tongTien !== null) {
        console.log('Using tongTien from backend:', order.tongTien)
        return formatCurrency(order.tongTien)
      }
      
      // Sử dụng chiTietTraHangList từ backend response
      const chiTietList = order.chiTietTraHangList || []
      console.log('ChiTietList:', chiTietList, 'Length:', chiTietList.length)
      
      if (chiTietList.length === 0) {
        console.log('No chiTietList found, returning 0')
        return formatCurrency(0)
      }
      
      let total = 0
      chiTietList.forEach((item, index) => {
        console.log(`Item ${index}:`, item)
        console.log(`Item ${index} keys:`, Object.keys(item))
        
        // Sử dụng donGia từ backend response
        const donGia = item.donGia || 0
        const soLuong = item.soLuong || 0
        
        console.log(`Item ${index} - donGia: ${donGia}, soLuong: ${soLuong}`)
        total += donGia * soLuong
      })
      
      console.log('Final total:', total)
      console.log('=== END DEBUG ===')
      return formatCurrency(total)
    }

    const formatDate = (dateString) => {
      if (!dateString) return 'N/A'
      const date = new Date(dateString)
      return new Intl.DateTimeFormat('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      }).format(date)
    }

    const formatCurrency = (value) => {
      if (value == null) return '0 ₫'
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(value)
    }

    const getTimeAgo = (dateString) => {
      if (!dateString) return ''
      
      const date = new Date(dateString)
      const now = new Date()
      const diffMs = now - date
      const diffSec = Math.round(diffMs / 1000)
      const diffMin = Math.round(diffSec / 60)
      const diffHour = Math.round(diffMin / 60)
      const diffDay = Math.round(diffHour / 24)
      
      if (diffSec < 60) return 'Vừa xong'
      if (diffMin < 60) return `${diffMin} phút trước`
      if (diffHour < 24) return `${diffHour} giờ trước`
      if (diffDay < 30) return `${diffDay} ngày trước`
      
      return formatDate(dateString)
    }

    const getStatusText = (status) => {
      const statusMap = {
        0: 'Chờ xác nhận',
        1: 'Đã xác nhận',
        2: 'Đã từ chối'
      }
      return statusMap[status] || 'Không xác định'
    }

    const getStatusBadgeClass = (status) => {
      const classMap = {
        0: 'status-badge status-warning',
        1: 'status-badge status-info',
        2: 'status-badge status-danger'
      }
      return classMap[status] || 'status-badge status-secondary'
    }

    const getImageUrl = (path) => {
      if (!path) {
        console.log('No image path provided')
        return '/default-avatar.png'
      }
      
      // Nếu đã là URL đầy đủ thì trả về luôn
      if (path.startsWith('http://') || path.startsWith('https://')) {
        return path
      }
      
      // Sử dụng base URL từ api config hoặc environment
      const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
      
      // Kiểm tra nếu path đã chứa 'uploads' thì không thêm nữa
      let fullUrl
      if (path.startsWith('uploads/') || path.startsWith('/uploads/')) {
        fullUrl = `${baseUrl}/${path.replace(/^\/+/, '')}`
      } else {
        // Ảnh sản phẩm nằm trong thư mục san_pham, không phải images
        fullUrl = `${baseUrl}/uploads/san_pham/${path}`
      }
      
      console.log('Generated image URL:', fullUrl)
      return fullUrl
    }

    const getMediaUrl = (path) => {
      if (!path) {
        console.log('No media path provided')
        return ''
      }
      
      // Nếu đã là URL đầy đủ thì trả về luôn
      if (path.startsWith('http://') || path.startsWith('https://')) {
        return path
      }
      
      // Sử dụng base URL từ api config hoặc environment
      const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
      
      // Kiểm tra nếu path đã chứa 'uploads' thì không thêm nữa
      let fullUrl
      if (path.startsWith('uploads/') || path.startsWith('/uploads/')) {
        fullUrl = `${baseUrl}/${path.replace(/^\/+/, '')}`
      } else {
        fullUrl = `${baseUrl}/uploads/media/${path}`
      }
      
      console.log('Generated media URL:', fullUrl)
      return fullUrl
    }

    const isImage = (path) => {
      if (!path) return false
      const ext = path.split('.').pop().toLowerCase()
      return ['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext)
    }

    const getFileExtension = (path) => {
      if (!path) return 'N/A'
      const ext = path.split('.').pop().toLowerCase()
      return ext.toUpperCase()
    }

    const isVideo = (path) => {
      if (!path) return false
      const ext = path.split('.').pop().toLowerCase()
      return ['mp4', 'webm', 'ogg', 'mov'].includes(ext)
    }

    const downloadMedia = (media) => {
      if (!media.duongDan) return
      const url = getMediaUrl(media.duongDan)
      const link = document.createElement('a')
      link.href = url
      link.download = media.duongDan.split('/').pop() || 'file'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    }

    const handleImageError = (event) => {
      console.log('Image load error, using fallback image')
      event.target.src = '/default-avatar.png'
      // Thêm class để style khác biệt nếu cần
      event.target.classList.add('fallback-image')
    }

    // Hàm lấy ảnh đại diện cho đơn trả hàng
    const getRepresentativeImage = (order) => {
      // Kiểm tra nếu có chi tiết trả hàng và có ảnh sản phẩm
      if (order?.chiTietTraHangList && order.chiTietTraHangList.length > 0) {
        const firstItem = order.chiTietTraHangList[0]
        if (firstItem?.anhSanPham) {
          return getImageUrl(firstItem.anhSanPham)
        }
      }
      
      // Nếu không có ảnh sản phẩm, sử dụng ảnh mặc định
      return '/img/small-logos/logo-xd.svg'
    }

    // Lifecycle hooks
    onMounted(() => {
      fetchReturnOrders()
      
      // Initialize Bootstrap modals with proper configuration
      const detailsModalEl = document.getElementById('detailsModal')
      const mediaPreviewModalEl = document.getElementById('mediaPreviewModal')
      const confirmModalEl = document.getElementById('confirmModal')
      const productConditionModalEl = document.getElementById('productConditionModal')
      
      if (detailsModalEl) {
        detailsModal = new Modal(detailsModalEl, {
          backdrop: true,
          keyboard: true
        })
      }
      
      if (mediaPreviewModalEl) {
        mediaPreviewModal = new Modal(mediaPreviewModalEl, {
          backdrop: true,
          keyboard: true
        })
      }
      
      if (confirmModalEl) {
        confirmModal = new Modal(confirmModalEl, {
          backdrop: true,
          keyboard: true
        })
      }
      
      if (productConditionModalEl) {
        productConditionModal = new Modal(productConditionModalEl, {
          backdrop: true,
          keyboard: true
        })
      }
    })

    return {
      returnOrders,
      loading,
      selectedStatus,
      searchQuery,
      statusFilters,
      currentPage,
      totalPages,
      selectedOrder,
      selectedMedia,
      confirmAction,
      rejectReason,
      rejectReasonError,
      processing,
      productConditions,
      allConditionsSelected,
      filterByStatus,
      handleSearch,
      changePage,
      showDetails,
      openMediaPreview,
      approveReturn,
      rejectReturn,
      confirmReject,
      confirmApproveWithConditions,
      getConditionClass,
      calculateTotalAmount,
      formatDate,
      formatCurrency,
      getTimeAgo,
      getStatusText,
      getStatusBadgeClass,
      getImageUrl,
      getMediaUrl,
      isImage,
      isVideo,
      getFileExtension,
      downloadMedia,
      handleImageError,
      getRepresentativeImage
    }
  }
}
</script>

<style scoped>
.nav-pills .nav-link.active {
  background-color: #66ea8b;
  color: white;
  border-radius: 20px;
  padding: 8px 16px;
  font-weight: 500;
}

.nav-pills .nav-link {
  color: #495057;
  border-radius: 20px;
  background: #e9ecef;
  padding: 8px 16px;
  font-weight: 500;
  white-space: nowrap;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.3s ease;
}

.nav-pills .nav-link:hover {
  color: #495057;
  background: #dee2e6;
}

/* Status Badges */
.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  display: inline-block;
  min-width: 120px;
  text-align: center;
}

.status-warning {
  background: #fff3cd;
  color: #856404;
}

.status-success {
  background: #d4edda;
  color: #155724;
}

.status-info {
  background: #d1ecf1;
  color: #0c5460;
}

.status-danger {
  background: #f8d7da;
  color: #721c24;
}

.status-secondary {
  background: #e2e3e5;
  color: #383d41;
}

.badge {
  font-size: 0.75rem;
  padding: 0.35em 0.65em;
}

.card {
  border-radius: 0.5rem;
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}

.card-header {
  border-radius: 0.5rem 0.5rem 0 0 !important;
}

.modal-content {
  border: none;
  border-radius: 0.5rem;
}

.modal-header {
  border-radius: 0.5rem 0.5rem 0 0;
}

.reason-box {
  border-left: 3px solid #17a2b8;
  background-color: #f8f9fa;
}

.info-item {
  margin-bottom: 1rem;
}

.info-item:last-child {
  margin-bottom: 0;
}

.modal-xl .modal-dialog {
  max-width: 1200px;
}

.btn-link {
  text-decoration: none;
}

.btn-link:hover {
  text-decoration: none;
}

.table th {
  border-bottom: 1px solid #e9ecef;
}

.table td {
  border-bottom: 1px solid #e9ecef;
}

.fallback-image {
  opacity: 0.7;
  filter: grayscale(20%);
}

@media (max-width: 768px) {
  .modal-xl .modal-dialog {
    max-width: 95%;
    margin: 1rem auto;
  }
}
</style>
    }

    const isVideo = (path) => {
      if (!path) return false
      const ext = path.split('.').pop().toLowerCase()
      return ['mp4', 'webm', 'ogg', 'mov'].includes(ext)
    }

    const downloadMedia = (media) => {
      if (!media.duongDan) return
      const url = getMediaUrl(media.duongDan)
      const link = document.createElement('a')
      link.href = url
      link.download = media.duongDan.split('/').pop() || 'file'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    }

    const handleImageError = (event) => {
      console.log('Image load error, using fallback image')
      event.target.src = '/default-avatar.png'
      // Thêm class để style khác biệt nếu cần
      event.target.classList.add('fallback-image')
    }

    const getFileExtension = (path) => {
      if (!path) return 'N/A'
      const ext = path.split('.').pop().toLowerCase()
      return ext.toUpperCase()
    }

    const isVideo = (path) => {
      if (!path) return false
      const ext = path.split('.').pop().toLowerCase()
      return ['mp4', 'webm', 'ogg', 'mov'].includes(ext)
    }

    const downloadMedia = (media) => {
      if (!media.duongDan) return
      const url = getMediaUrl(media.duongDan)
      const link = document.createElement('a')
      link.href = url
      link.download = media.duongDan.split('/').pop() || 'file'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    }

    const handleImageError = (event) => {
      console.log('Image load error, using fallback image')
      event.target.src = '/default-avatar.png'
      // Thêm class để style khác biệt nếu cần
      event.target.classList.add('fallback-image')
    }

    const getFileExtension = (path) => {
      if (!path) return 'N/A'
      const ext = path.split('.').pop().toLowerCase()
      return ext.toUpperCase()
    }

    const isVideo = (path) => {
      if (!path) return false
      const ext = path.split('.').pop().toLowerCase()
      return ['mp4', 'webm', 'ogg', 'mov'].includes(ext)
    }

    const downloadMedia = (media) => {
      if (!media.duongDan) return
      const url = getMediaUrl(media.duongDan)
      const link = document.createElement('a')
      link.href = url
      link.download = media.duongDan.split('/').pop() || 'file'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    }

    const handleImageError = (event) => {
      console.log('Image load error, using fallback image')
      event.target.src = '/default-avatar.png'
      // Thêm class để style khác biệt nếu cần
      event.target.classList.add('fallback-image')
    }

    const getFileExtension = (path) => {
      if (!path) return 'N/A'
      const ext = path.split('.').pop().toLowerCase()
      return ext.toUpperCase()
    }

