<!-- 
 2. chưa có tự cập nhật hết hạn khi voucher đã hết hạn
 3. xóa mã chi tiết chưa có gửi mail 
  -->
<script setup>
import { onMounted, ref, computed, watch } from 'vue';
import { getActiveVouchers, createVoucher, deleteVoucher, toggleVoucherStatus, updateVoucher } from '../../../services/RoleQuanLy/QuanLyPhieuGiamGia';
import {
  getListKhachHang,
  // getListVoucherHieuLuc,
  getListVoucherTatCa,
  getListVoucherDaSuDung,
  DeleteVoucherDetail

} from '../../../services/RoleQuanLy/PhieuGiamGiaDetailService';
import {
  // getListKhachHang 
} from '../../../services/RoleQuanLy/PhieuGiamGiaDetailService';
import * as XLSX from 'xlsx';
import { useToast } from 'vue-toastification';



const phieuGiamGiaList = ref([]);
const isEditMode = ref(false);
const showVoucherModal = ref(false);
const modalActiveTab = ref('thongTin');
const getInitialVoucherData = () => ({
  tenPhieuGiamGia: '',
  loaiGiamGia: 'SO_TIEN',
  giaTriGiam: null,
  giaTriToiDa: null,
  giaTriToiThieu: null,
  trangThai: 1,
  ngayBatDau: new Date().toISOString().split('T')[0],
  ngayKetThuc: '',
  doiTuongApDung: 'ALL',
  soLuong: 1,
  danhSachKhachHangId: [],
});
const errors = ref({});
const voucherInForm = ref(getInitialVoucherData());
const selectedPhieu = ref(null);
const listVoucherDetail = ref(null);
const isLoadingDetails = ref(false);
const listActiveTab = ref('details');
const filterName = ref('');
const filterUserStatus = ref('all');
const searchTerm = ref('');
const statusFilter = ref('all');
const itemsPerPage = ref(15);
const currentPage = ref(1);
const filterActivityStatus = ref('all');
const customerLevels = ref([
  { id: 1, name: 'Đồng' },
  { id: 2, name: 'Bạc' },
  { id: 3, name: 'Vàng' }
]);
const isSaving = ref(false);
const historyVoucer = ref([]);


const allCustomers = ref([

])

const customerLevelFilter = ref('');
const genderFilter = ref('');
const startDateFilter = ref('');
const endDateFilter = ref('');
const toast = useToast();




const fetchCustomers = async () => {
  try {
    const res = await getListKhachHang();
    allCustomers.value = res.data
  } catch (error) {
    console.log("lỗi khi tải danh sách khác hàng", error)
  }

};

const fetchVouCherDaSuDung = async (id) => {
  try {
    const res = await getListVoucherDaSuDung(id);
    historyVoucer.value = res.data
  } catch (error) {
    console.log("lỗi khi tải danh sách voucher đã sử dụng", error)
  }
}




const getAllPhieuGiamGia = async () => {
  try {

    const response = await getActiveVouchers();
    phieuGiamGiaList.value = response.data;
  } catch (error) {
    console.error("Lỗi khi tải danh sách voucher" + error);
  }


  if (phieuGiamGiaList.value && phieuGiamGiaList.value.length > 0) {
    // selectedPhieu.value = phieuGiamGiaList.value[0];
  }
};

const formatCurrency = (value) => {
  if (value === null || value === undefined) return '0';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const formatDate = (timestamp, showTime = false) => {
  if (!timestamp) return '...';
  const date = new Date(timestamp);
  const options = {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  };
  if (showTime) {
    options.hour = '2-digit';
    options.minute = '2-digit';
  }
  return new Intl.DateTimeFormat('vi-VN', options).format(date);
};

const formatDiscountValue = (phieu) => {
  if (!phieu) return '';
  if (phieu.loaiGiamGia === 'PHAN_TRAM') {
    return `${phieu.giaTriGiam}%`;
  }
  return formatCurrency(phieu.giaTriGiam);
};

const getStatusInfo = (statusByte) => {
  switch (statusByte) {
    case 1:
      return { text: 'Hoạt động', class: 'active' };
    case 0:
      return { text: 'Không hoạt động', class: 'inactive' };
    case 2:
      return { text: 'Hết hạn', class: 'expired' };
    default:
      return { text: 'Không xác định', class: 'unknown' };
  }
};

const getCustomerType = (typeByte) => {
  switch (typeByte) {
    case 0: return 'Tất cả khách hàng';
    case 1: return 'Khách hàng mới';
    case 2: return 'Khách hàng thân thiết';
    default: return 'Không xác định';
  }
}

const toggleDetails = (phieu) => {
  if (selectedPhieu.value && selectedPhieu.value.id === phieu.id) {
    selectedPhieu.value = null;
  } else {
    selectedPhieu.value = phieu;
    listActiveTab.value = 'details';
  }
};
const showNotification = (type, message) => {
  notification.value = { show: true, type, message };
  setTimeout(() => {
    notification.value.show = false;
  }, 5000);
};
const notification = ref({ show: false, type: 'success', message: '' });

const handleSaveVoucher = async () => {
  if (!validateForm()) {
    showNotification('error', "vui lòng kiểm tra lại thông tin");
    return;
  }
  isSaving.value = true;
  if (isEditMode.value) {
    try {
      const payload = {
        ...voucherInForm.value,
        ngayBatDau: voucherInForm.value.ngayBatDau ? new Date(voucherInForm.value.ngayBatDau).getTime() : null,
        ngayKetThuc: voucherInForm.value.ngayKetThuc ? new Date(voucherInForm.value.ngayKetThuc).getTime() : null,
      };
      console.log("Đang cập nhật voucher:", voucherInForm.value);
      await updateVoucher(payload)
      getAllPhieuGiamGia();
      toast.success('Đã cập nhật voucher thành công!', {
        timeout: 2000
      })
      isSaving.value = false;
      closeModal();
    } catch (error) {
      isSaving.value = false;
      console.error("Lỗi khi cập nhật voucher:", error);
      showNotification('error', "Sửa phiếu giảm giá thất bại");
    }
  } else {
    try {
      const payload = {
        ...voucherInForm.value,
        ngayBatDau: voucherInForm.value.ngayBatDau ? new Date(voucherInForm.value.ngayBatDau).getTime() : null,
        ngayKetThuc: voucherInForm.value.ngayKetThuc ? new Date(voucherInForm.value.ngayKetThuc).getTime() : null,
      };
      await createVoucher(payload);
      console.log("đến đây");
      getAllPhieuGiamGia();
      showNotification('error', "thêm phiếu giảm giá thành công");
      isSaving.value = false;
      closeModal();
    } catch (error) {
      isSaving.value = false;
      console.error("Lỗi khi thêm voucher:", error);
      showNotification('error', "thêm phiếu giảm giá thất bại");
    }
  }
};

const closeModal = () => {
  showVoucherModal.value = false;
  voucherInForm.value = getInitialVoucherData();
  errors.value = {}; // Reset lỗi khi đóng modal
}

// Hàm MỞ MODAL để THÊM MỚI
const openAddModal = () => {
  fetchCustomers()
  isEditMode.value = false; // Đặt chế độ là Thêm mới
  voucherInForm.value = getInitialVoucherData(); // Đảm bảo form trống
  modalActiveTab.value = 'thongTin'; // Reset về tab đầu tiên
  showVoucherModal.value = true; // Hiển thị modal
};



const openEditModal = (voucherToEdit) => {
  isEditMode.value = true;
  voucherInForm.value = {
    ...voucherToEdit,
    ngayBatDau: formatDateForInput(voucherToEdit.ngayBatDau),
    ngayKetThuc: formatDateForInput(voucherToEdit.ngayKetThuc)
  };
  modalActiveTab.value = 'thongTin';
  showVoucherModal.value = true;
  console.log("trạng thái showVoucher" + showVoucherModal.value)
};
const formatDateForInput = (dateValue) => {
  if (!dateValue) return '';
  try {
    const date = new Date(dateValue);
    return date.toISOString().split('T')[0];
  } catch (error) {
    console.error("Không thể định dạng ngày:", dateValue, error);
    return '';
  }
};
const validateForm = () => {
  errors.value = {};
  const form = voucherInForm.value;

  // Tab "Thông tin"
  if (!form.tenPhieuGiamGia.trim()) {
    errors.value.tenPhieuGiamGia = 'Tên đợt phát hành là bắt buộc.';
  }


  if (form.giaTriGiam == null || form.giaTriGiam === '') {
    errors.value.giaTriGiam = 'Mức giảm là bắt buộc.';
  } else if (form.giaTriGiam <= 0) {
    errors.value.giaTriGiam = 'Mức giảm phải là số dương.';
  } else if (form.loaiGiamGia === 'PHAN_TRAM' && form.giaTriGiam >= 100) {
    errors.value.giaTriGiam = 'Mức giảm % không được vượt quá 100.';
  }

  if (form.loaiGiamGia === 'PHAN_TRAM') {
    if (form.giaTriToiDa == null || form.giaTriToiDa === '') {
      errors.value.giaTriToiDa = 'Giảm tối đa là bắt buộc khi giảm theo %.';
    } else if (form.giaTriToiDa <= 0) {
      errors.value.giaTriToiDa = 'Số tiền giảm tối đa phải là số dương.';
    }
  }

  if (form.giaTriToiThieu == null && form.giaTriToiThieu < 0) {
    errors.value.giaTriToiThieu = 'Giá trị đơn hàng tối thiểu không được âm.';
  }

  // So sánh giá trị giảm với đơn hàng tối thiểu
  if (form.loaiGiamGia === 'SO_TIEN' && form.giaTriGiam && form.giaTriToiThieu != null && form.giaTriGiam >= form.giaTriToiThieu) {
    errors.value.giaTriToiThieu = 'Giá trị tối thiểu phải lớn hơn mức giảm giá.';
  }

  // Tab "Áp dụng"
  if (!form.ngayBatDau) {
    errors.value.ngayBatDau = 'Ngày bắt đầu là bắt buộc.';
  }
  if (form.ngayBatDau && form.ngayKetThuc && new Date(form.ngayKetThuc) < new Date(form.ngayBatDau)) {
    errors.value.ngayKetThuc = 'Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.';
  }

  if (form.doiTuongApDung === 'ALL') {
    if (form.soLuong == null || form.soLuong === '' || form.soLuong < 1) {
      errors.value.soLuong = 'Số lượng phát hành phải là số nguyên lớn hơn 0.';
    }
  }

  if (form.doiTuongApDung === 'SPECIFIC') {
    if (!form.danhSachKhachHangId || form.danhSachKhachHangId.length === 0) {
      errors.value.danhSachKhachHangId = 'Phải chọn ít nhất một khách hàng.';
    }
  }

  // Trả về true nếu không có lỗi
  return Object.keys(errors.value).length === 0;
};






const DeleteVoucher = async (id) => {
  const confirmed = window.confirm("Bạn có chắc chắn muốn xóa không?");
  if (!confirmed) return;

  try {
    await deleteVoucher(id);
    getAllPhieuGiamGia();
  } catch (error) {
    toast.error('Xóa thất bại!', {
      timeout: 2000
    })
    console.error("Lỗi khi xóa:", error);
  }
};

const XoaMaVoucher = async (id) => {
  const confirmed = window.confirm("Bạn có chắc chắn muốn xóa không?");
  if (!confirmed) return;

  try {
    await DeleteVoucherDetail(id);
    console.log("đã xóa")
    fetchVoucherDetails(selectedPhieu.value.id);
    console.log("đã fetch")
  } catch (error) {
    toast.error('Xóa thất bại!', {
      timeout: 2000
    })
    console.error("Lỗi khi xóa:", error);
  }
};



const changeVoucherStatus = async (voucher) => {
  const newStatusText = voucher.trangThai === 1 ? "Không hoạt động" : "Hoạt động";
  const isConfirmed = confirm(`Bạn có chắc muốn đổi trạng thái của phiếu "${voucher.tenPhieuGiamGia}" thành "${newStatusText}" không?`);
  if (isConfirmed) {
    try {

      await toggleVoucherStatus(voucher.id);
      voucher.trangThai = voucher.trangThai === 1 ? 0 : 1;
    } catch (error) {
      console.error("Có lỗi xảy ra khi cập nhật trạng thái:", error);
      toast.error('Cập nhật trạng thái thất bại!', {
        timeout: 2000
      })
    }
  }
};



const showVoucherDetailsTab = () => {
  listActiveTab.value = 'list';


  if (selectedPhieu.value && selectedPhieu.value.id) {
    fetchVoucherDetails(selectedPhieu.value.id);
  }
};


const fetchVoucherDetails = async (phieuId) => {
  if (!phieuId) {
    console.warn("Chưa có phiếu giảm giá nào được chọn.");
    return;
  }
  isLoadingDetails.value = true;
  listVoucherDetail.value = null;
  try {
    const response = await getListVoucherTatCa(phieuId)
    console.log("Dữ liệu chi tiết voucher từ API:", response.data);
    listVoucherDetail.value = response.data;
  } catch (error) {
    console.error("Lỗi khi tải chi tiết voucher:", error);
  } finally {
    isLoadingDetails.value = false;
  }
};

const filteredVouchers = computed(() => {
  if (!listVoucherDetail.value) {
    return [];
  }
  let vouchers = listVoucherDetail.value;
  if (filterActivityStatus.value !== 'all') {
    const targetStatus = (filterActivityStatus.value === 'active') ? 1 : 0;
    vouchers = vouchers.filter(ma => ma.trangThai === targetStatus);
  }
  if (filterUserStatus.value !== 'all') {
    const isUsed = filterUserStatus.value === 'used';
    vouchers = vouchers.filter(ma => ma.trangThaiSuDung === isUsed);
  }
  if (filterName.value) {
    const lowerCaseSearch = filterName.value.trim().toLowerCase();

    vouchers = vouchers.filter(ma => {
      // Điều kiện 1: Kiểm tra mã phiếu
      const maPhieuMatch = ma.maPhieu && ma.maPhieu.toLowerCase().includes(lowerCaseSearch);

      // Điều kiện 2: Kiểm tra tên khách hàng (chỉ khi voucher có khách hàng)
      const tenKhachHangMatch = ma.khachHang && ma.khachHang.tenKhachHang && ma.khachHang.tenKhachHang.toLowerCase().includes(lowerCaseSearch);

      // Trả về true nếu MỘT TRONG HAI điều kiện đúng
      return maPhieuMatch || tenKhachHangMatch;
    });
  }
  return vouchers;
});
const filteredCustomers = computed(() => {

  let result = allCustomers.value;

  if (customerLevelFilter.value) {
    result = result.filter(
      customer => customer.capDo === customerLevelFilter.value
    );
  }
  if (genderFilter.value !== '') {
    result = result.filter(

      customer => customer.gioiTinh == genderFilter.value
    );
  }
  if (startDateFilter.value) {
    const startDate = new Date(startDateFilter.value);
    result = result.filter(
      customer => new Date(customer.ngaySinh) >= startDate
    );
  }

  if (endDateFilter.value) {
    const endDate = new Date(endDateFilter.value);
    endDate.setDate(endDate.getDate() + 1);
    result = result.filter(
      customer => new Date(customer.ngaySinh) < endDate
    );
  }

  // Trả về kết quả cuối cùng sau khi đã qua tất cả các bộ lọc.
  return result;
});


const selectAll = computed({
  get() {
    return filteredCustomers.value.length > 0 &&
      filteredCustomers.value.every(customer =>
        voucherInForm.value.danhSachKhachHangId.includes(customer.id)
      );
  },
  set(value) {
    const filteredIds = filteredCustomers.value.map(c => c.id);
    if (value) {
      voucherInForm.value.danhSachKhachHangId = [...new Set([...voucherInForm.value.danhSachKhachHangId, ...filteredIds])];
    } else {
      voucherInForm.value.danhSachKhachHangId = voucherInForm.value.danhSachKhachHangId.filter(id => !filteredIds.includes(id));
    }
  }
});
// 2. Tính tổng số trang dựa trên danh sách CHA ĐÃ LỌC
const totalPages = computed(() => {
  if (filteredPhieuGiamGiaList.value.length === 0) return 1;
  return Math.ceil(filteredPhieuGiamGiaList.value.length / itemsPerPage.value);
});

// 3. Lấy ra các mục cho trang hiện tại từ danh sách CHA ĐÃ LỌC
const paginatedVouchers = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + parseInt(itemsPerPage.value, 10);
  return filteredPhieuGiamGiaList.value.slice(start, end);
});
const filteredPhieuGiamGiaList = computed(() => {
  let filtered = phieuGiamGiaList.value;

  // Lọc theo trạng thái
  if (statusFilter.value !== 'all') {
    const status = parseInt(statusFilter.value, 10);
    filtered = filtered.filter(phieu => phieu.trangThai === status);
  }

  // Lọc theo từ khóa tìm kiếm
  const lowerCaseSearch = searchTerm.value.trim().toLowerCase();
  if (lowerCaseSearch) {
    filtered = filtered.filter(phieu =>
      (phieu.maPhieuGiamGia && phieu.maPhieuGiamGia.toLowerCase().includes(lowerCaseSearch)) ||
      (phieu.tenPhieuGiamGia && phieu.tenPhieuGiamGia.toLowerCase().includes(lowerCaseSearch))
    );
  }
  return filtered;
}); const statusCounts = computed(() => {
  const counts = {
    all: phieuGiamGiaList.value.length,
    active: 0,
    inactive: 0,
    expired: 0,
  };
  for (const phieu of phieuGiamGiaList.value) {
    if (phieu.trangThai === 1) counts.active++;
    else if (phieu.trangThai === 0) counts.inactive++;
    else if (phieu.trangThai === 2) counts.expired++;
  }
  return counts;
});

watch([searchTerm, statusFilter, itemsPerPage], () => {
  currentPage.value = 1;
});

watch(voucherInForm, (newValue) => {
  for (const key in errors.value) {
    if (newValue[key] !== undefined) {
      if (key === 'ngayKetThuc' && (newValue.ngayBatDau || newValue.ngayKetThuc)) {
        delete errors.value.ngayKetThuc;
      }
      if (key === 'giaTriToiThieu' && (newValue.giaTriToiThieu || newValue.giaTriGiam)) {
        delete errors.value.giaTriToiThieu;
      }
      if (errors.value[key] && newValue[key]) {
        delete errors.value[key];
      }
    }
  }
}, { deep: true });


function exportToExcel() {
  const listToExport = filteredPhieuGiamGiaList.value;

  if (!listToExport || listToExport.length === 0) {
    alert("Không có dữ liệu để xuất.");
    return;
  }

  // A. Chuẩn bị dữ liệu (ánh xạ sang tên cột tiếng Việt)
  const dataForExcel = listToExport.map((phieu, index) => {
    // Sử dụng các hàm helper của bạn để định dạng dữ liệu
    const statusInfo = getStatusInfo(phieu.trangThai);
    const discountValue = formatDiscountValue(phieu);

    return {
      'STT': index + 1,
      'Mã Phiếu': phieu.maPhieuGiamGia,
      'Tên Phiếu Giảm Giá': phieu.tenPhieuGiamGia,
      'Ngày Bắt Đầu': formatDate(phieu.ngayBatDau),
      'Ngày Kết Thúc': formatDate(phieu.ngayKetThuc),
      'Sử Dụng': `${phieu.daSuDung || 0} / ${phieu.soLuong}`,
      'Giá Trị Giảm': discountValue,
      'Trạng Thái': statusInfo.text,
    };
  });

  // B. Tạo worksheet từ dữ liệu đã chuẩn bị
  const worksheet = XLSX.utils.json_to_sheet(dataForExcel);

  worksheet['!cols'] = [
    { wch: 5 },   // STT
    { wch: 20 },  // Mã Phiếu
    { wch: 40 },  // Tên Phiếu Giảm Giá
    { wch: 15 },  // Ngày Bắt Đầu
    { wch: 15 },  // Ngày Kết Thúc
    { wch: 15 },  // Sử Dụng
    { wch: 20 },  // Giá Trị Giảm
    { wch: 18 },  // Trạng Thái
  ];

  // D. Tạo workbook và thêm worksheet vào
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, 'DanhSachPhieuGiamGia');

  // E. Tạo và tải file Excel
  // Đặt tên file có ngày tháng để tránh trùng lặp
  const today = new Date();
  const fileName = `DanhSachPhieuGiamGia_${today.getFullYear()}_${today.getMonth() + 1}_${today.getDate()}.xlsx`;
  XLSX.writeFile(workbook, fileName);
}


// --- LOGIC MỚI: CÁC HÀM CHO BỘ LỌC VÀ PHÂN TRANG ---
const resetFilters = () => {
  searchTerm.value = '';
  statusFilter.value = 'all';
};

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
  }
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
  }
};



onMounted(() => {
  getAllPhieuGiamGia();
});
</script>

<template>
  <div class="container-fluid p-4">
    <!-- Header: Title và khối bộ lọc -->
    <div class="header-card">
      <div class="title">
        <i class="fas fa-tags p-2"></i>
        Quản lý Phiếu Giảm Giá
      </div>

      <div class="filter-section">
        <div class="filter-header">
          <i class="fa-solid fa-filter p-2"></i>
          <strong>Bộ lọc</strong>
        </div>

        <div class="filter-content">
          <div class="filter-row">
            <!-- Thanh tìm kiếm -->
            <div class="filter-group search-group">
              <label>Tìm kiếm</label>
              <div class="search-input-container">
                <i class="fa-solid fa-magnifying-glass search-icon"></i>
                <input type="text" placeholder="Theo mã, tên phiếu..." v-model="searchTerm" class="search-input">
              </div>
            </div>

            <!-- Bộ lọc trạng thái -->
            <div class="filter-group">
              <label for="status-filter">Trạng thái:</label>
              <select id="status-filter" v-model="statusFilter" class="form-select">
                <option value="all">Tất cả</option>
                <option value="1">Hoạt động</option>
                <option value="0">Không hoạt động</option>
                <option value="2">Hết hạn</option>
              </select>
            </div>
          </div>

          <div class="button-group">
            <button class="btn btn-secondary" @click="resetFilters">
              <i class="fas fa-sync-alt"></i> Làm mới
            </button>
            <button class="btn btn-primary" @click="exportToExcel()">
              <i class="fa-solid fa-file-export"></i>
              Xuất Excel
            </button>
            <button class="add-new-btn btn btn-success" @click="openAddModal()">
              <i class="fas fa-plus"></i> Thêm mới
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Khối danh sách Voucher -->
    <div class="list-card">
      <div class="list-header">
        <div class="list-title">
          <i class="fa-solid fa-list-ul p-2"></i>
          Danh sách đợt phát hành
        </div>

        <!-- Các tab lọc nhanh -->
        <div class="tab-container">
          <div :class="['tab-item', { 'active': statusFilter === 'all' }]" @click="statusFilter = 'all'">
            Tất cả <span class="badge">{{ statusCounts.all }}</span>
          </div>
          <div :class="['tab-item', { 'active': statusFilter === '1' }]" @click="statusFilter = '1'">
            Hoạt động <span class="badge">{{ statusCounts.active }}</span>
          </div>
          <div :class="['tab-item', { 'active': statusFilter === '0' }]" @click="statusFilter = '0'">
            Không hoạt động <span class="badge">{{ statusCounts.inactive }}</span>
          </div>
          <div :class="['tab-item', { 'active': statusFilter === '2' }]" @click="statusFilter = '2'">
            Hết hạn <span class="badge">{{ statusCounts.expired }}</span>
          </div>
        </div>
      </div>

      <!-- Loading Spinner (Bạn có thể thêm logic cho biến `isLoading` nếu cần) -->
      <!-- <div v-if="isLoading" class="loading-container"> ... </div> -->

      <!-- Trạng thái trống -->
      <div v-if="!paginatedVouchers || paginatedVouchers.length === 0" class="empty-state">
        <i class="fas fa-ticket-alt"></i>
        <h3>Không tìm thấy phiếu giảm giá nào</h3>
        <p>Hãy thử thay đổi bộ lọc hoặc thêm một phiếu giảm giá mới.</p>
      </div>

      <!-- Vùng chứa danh sách -->
      <div v-else class="list-content-area">

        <div class="voucher-list-header">
          <!-- <div class="col-check"><input type="checkbox" class="header-checkbox"></div> -->
          <div class="col-stt">STT</div>
          <div class="col-ma">Mã Phiếu</div>
          <div class="col-ten">Tên Phiếu Giảm Giá</div>
          <div class="col-ngay">Bắt đầu</div>
          <div class="col-ngay">Kết thúc</div>
          <div class="col-soluong">Sử dụng</div>
          <div class="col-menhgia">Giá trị giảm</div>
          <div class="col-menhgia">Đơn tối thiểu</div>
          <div class="col-congKhai">Công khai</div>
          <div class="col-trangthai">Trạng thái</div>
        </div>

        <!-- Các mục trong danh sách -->
        <div v-for="(phieu, index) in paginatedVouchers" :key="phieu.id" class="voucher-item-container">
          <div class="voucher-item-row" @click="toggleDetails(phieu)"
            :class="{ 'selected': selectedPhieu && selectedPhieu.id === phieu.id }">

            <!-- <div class="col-check">
              <i class="fas fa-chevron-down toggle-icon"
                :class="{ 'rotated': selectedPhieu && selectedPhieu.id === phieu.id }"></i>
              <input type="checkbox" class="row-checkbox" @click.stop>
            </div> -->

            <div class="col-stt">{{ index + 1 }}</div>
            <div class="col-ma">{{ phieu.maPhieuGiamGia }}</div>
            <div class="col-ten">{{ phieu.tenPhieuGiamGia }}</div>
            <div class="col-ngay">{{ formatDate(phieu.ngayBatDau) }}</div>
            <div class="col-ngay">{{ formatDate(phieu.ngayKetThuc) }}</div>
            <div class="col-soluong">{{ phieu.daSuDung || 0 }} / {{ phieu.soLuong }}</div>
            <div class="col-menhgia">{{ formatDiscountValue(phieu) }}</div>
            <div class="col-menhgia">{{ formatCurrency(phieu.giaTriToiThieu) }}</div>
            <div class="col-congKhai">{{ phieu.congKhai ? "Công khai" : "Cá nhân" }}</div>
            <div class="col-trangthai">
              <span class="status-badge" :class="getStatusInfo(phieu.trangThai).class">
                {{ getStatusInfo(phieu.trangThai).text }}
              </span>
            </div>
          </div>
          <!-- Chi tiết khi mở rộng -->
          <div v-if="selectedPhieu && selectedPhieu.id === phieu.id" class="voucher-details">
            <div class="details-tabs">
              <button class="tab-btn" :class="{ 'active': listActiveTab === 'details' }"
                @click="listActiveTab = 'details'">
                Thông tin chi tiết
              </button>
              <button class="tab-btn" :class="{ 'active': listActiveTab === 'list' }" @click="showVoucherDetailsTab">
                Danh sách Voucher
              </button>
              <button class="tab-btn" :class="{ 'active': listActiveTab === 'history' }" @click="() => {
                listActiveTab = 'history';
                fetchVouCherDaSuDung(selectedPhieu.id);
              }">
                Lịch sử sử dụng
              </button>
            </div>

            <!-- Tab 1: Thông tin chi tiết -->
            <div v-if="listActiveTab === 'details'" class="details-content">
              <h3 class="details-section-title">Chi Tiết Phiếu Giảm Giá</h3>
              <div class="details-grid">
                <div class="form-field"><label>Mã phiếu: </label><span>{{ selectedPhieu.maPhieuGiamGia }}</span>
                </div>
                <div class="form-field"><label>Tên phiếu: </label><span>{{ selectedPhieu.tenPhieuGiamGia }}</span>
                </div>
                <div class="form-field"><label>Ngày bắt đầu: </label><span>{{ formatDate(selectedPhieu.ngayBatDau,
                  true) }}</span></div>
                <div class="form-field"><label>Ngày kết thúc: </label><span>{{ formatDate(selectedPhieu.ngayKetThuc,
                  true) }}</span></div>
                <div class="form-field"><label>Số lượng phát hành: </label><span>{{ selectedPhieu.soLuong }}</span>
                </div>
                <div class="form-field"><label>Số lượng đã dùng: </label><span>{{ selectedPhieu.daSuDung }}</span>
                </div>
                <div class="form-field"><label>Loại giảm giá: </label><span>{{ selectedPhieu.loaiGiamGia ===
                  'PHAN_TRAM' ? 'Giảm theo phần trăm (%)' : 'Giảm theo số tiền (VND)' }}</span></div>
                <div class="form-field"><label>Giá trị giảm: </label><span>{{ formatDiscountValue(selectedPhieu)
                    }}</span></div>
                <div class="form-field"><label>Hóa đơn tối thiểu: </label><span>{{
                  formatCurrency(selectedPhieu.giaTriToiThieu) }}</span></div>
                <div class="form-field"><label>Giảm tối đa: </label><span>{{
                  formatCurrency(selectedPhieu.giaTriToiDa)
                    }}</span></div>
                <div class="form-field"><label>Đối tượng: </label><span>{{
                  getCustomerType(selectedPhieu.yeuCauKhachHang) }}</span></div>
                <div class="form-field">
                  <label>Trạng thái: </label>
                  <div class="form-check form-switch">
                    <input class="form-check-input" type="checkbox" :id="`status-detail-${selectedPhieu.id}`"
                      :checked="selectedPhieu.trangThai === 1" @click.prevent="changeVoucherStatus(selectedPhieu)">
                    <label class="form-check-label" :for="`status-detail-${selectedPhieu.id}`"
                      :class="selectedPhieu.trangThai === 1 ? 'text-success' : 'text-muted'">
                      {{ selectedPhieu.trangThai === 1 ? 'Hoạt động' : 'Không hoạt động' }}
                    </label>
                  </div>
                </div>
              </div>
              <div class="details-actions">
                <button @click="openEditModal(selectedPhieu)" class="update-btn btn btn-success"><i
                    class="far fa-edit"></i> Cập nhật</button>
                <button @click="DeleteVoucher(selectedPhieu.id)" class="delete-btn btn btn-danger"><i
                    class="far fa-trash-alt"></i> Xóa</button>
              </div>
            </div>

            <!-- THAY THẾ CHO PLACEHOLDER: Tab 2: Danh sách Voucher -->
            <div v-if="listActiveTab === 'list'" class="details-content">
              <h3 class="details-section-title">Danh sách mã Voucher</h3>

              <!-- KHỐI ĐIỀU KHIỂN BỘ LỌC -->
              <div class="filter-controls">
                <!-- Bộ lọc tìm theo tên (giữ nguyên) -->
                <div class="filter-item">
                  <label for="filter-by-name">Tìm kiếm:</label>
                  <input id="filter-by-name" type="text" v-model="filterName" placeholder="Theo tên khách hàng..."
                    class="filter-input" />
                </div>

                <!-- Bộ lọc trạng thái sử dụng -->
                <div class="filter-item">
                  <label for="filter-user-status">Trạng thái sử dụng:</label>
                  <select id="filter-user-status" v-model="filterUserStatus" class="filter-select">
                    <option value="all">Tất cả</option>
                    <option value="unused">Chưa sử dụng</option>
                    <option value="used">Đã sử dụng</option>
                  </select>
                </div>

                <!-- Bộ lọc trạng thái hoạt động -->
                <div class="filter-item">
                  <label for="filter-activity-status">Trạng thái hoạt động:</label>
                  <select id="filter-activity-status" v-model="filterActivityStatus" class="filter-select">
                    <option value="all">Tất cả</option>
                    <option value="active">Hoạt động</option>
                    <option value="inactive">Ngừng hoạt động</option>
                  </select>
                </div>
              </div>
              <div v-if="isLoadingDetails" class="text-center">
                Đang tải dữ liệu, vui lòng chờ...
              </div>
              <div v-else class="voucher-list-container">
                <table v-if="filteredVouchers.length > 0" class="tab-table">
                  <thead>
                    <tr>
                      <th>STT</th>
                      <th>Mã Voucher</th>
                      <th>Khách Hàng</th>
                      <th>liên hệ</th>
                      <th>Sử dụng</th>
                      <th>Trạng thái</th>
                      <th>Xóa</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(ma, index) in filteredVouchers" :key="ma.id">
                      <td>{{ index + 1 }}</td>
                      <td>{{ ma.maPhieu }}</td>
                      <td>{{ ma.khachHang ? ma.khachHang.tenKhachHang : 'Voucher công khai' }}</td>
                      <td>{{ ma.khachHang ? ma.khachHang.sdt : '' }}</td>
                      <td :class="{ 'status-unused': !ma.trangThaiSuDung }">
                        {{ ma.trangThaiSuDung ? 'Đã sử dụng' : 'Chưa sử dụng' }}
                      </td>

                      <td>
                        {{ ma.trangThai ? 'Hoạt động' : 'Ngừng hoạt động' }}
                      </td>

                      <td style="display: flex; justify-content: center; align-items: center;">
                        <!-- Chỉ hiển thị nút này nếu ma.trangThaiSuDung là false -->
                        <button v-if="!ma.trangThaiSuDung" @click="XoaMaVoucher(ma.id)"
                          class="delete-btn btn btn-danger">
                          <i class="far fa-trash-alt"></i> Xóa
                        </button>
                      </td>


                    </tr>
                  </tbody>
                </table>
                <div v-else class="text-center">
                  Không có voucher nào khớp với bộ lọc.
                </div>
              </div>
            </div>


            <!-- THAY THẾ CHO PLACEHOLDER: Tab 3: Lịch sử sử dụng -->
            <div v-if="listActiveTab === 'history'" class="details-content">
              <h3 class="details-section-title">Lịch sử sử dụng</h3>
              <table class="tab-table">
                <thead>
                  <tr>
                    <th>Mã Voucher</th>
                    <th>Người dùng</th>
                    <th>Giá trị giảm</th>
                    <th>Đơn hàng</th>
                    <th>Ngày mua</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="log in historyVoucer" :key="log.id">
                    <td>{{ log.maPhieu }}</td>
                    <td>{{ log.tenKhachHang || "Khách lẻ" }}</td>
                    <td>{{ formatCurrency(log.soTienGiam) }}</td>
                    <td>{{ log.maHoaDon }}</td>
                    <td>{{ log.ngayTao }}</td>
                  </tr>
                  <tr v-if="!historyVoucer.value || historyVoucer.value.length === 0">
                    <td colspan="4" class="text-center">Chưa có lịch sử sử dụng.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
      <!-- Phân trang -->
      <div class="pagination-container">
        <div class="page-size-selector">
          <label>Hiển thị:</label>
          <select v-model="itemsPerPage" class="form-select">
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="15">15</option>
          </select>
          <span>/ trang</span>
        </div>
        <div class="pagination-controls">
          <button @click="prevPage" class="btn btn-secondary" :disabled="currentPage === 1">
            <i class="fa-solid fa-chevron-left"></i>
          </button>
          <span class="page-info">
            Trang {{ currentPage }} / {{ totalPages }}
          </span>
          <button @click="nextPage" class="btn btn-secondary" :disabled="currentPage === totalPages">
            <i class="fa-solid fa-chevron-right"></i>
          </button>
        </div>
      </div>
    </div>

    <div v-if="showVoucherModal" class="modal-overlay">
      <div class="modal-content-custom">
        <div class="modal-header">
          <h3 class="modal-title">{{ isEditMode ? 'Cập nhật đợt phát hành' : 'Thêm mới đợt phát hành' }}</h3>
          <button @click="closeModal" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div class="modal-tabs">
            <button class="modal-tab-btn" :class="{ 'active': modalActiveTab === 'thongTin' }"
              @click="modalActiveTab = 'thongTin'">Thông tin
            </button>
            <button class="modal-tab-btn" :class="{ 'active': modalActiveTab === 'apDung' }"
              @click="modalActiveTab = 'apDung'">Áp dụng
            </button>
          </div>
          <div class="tab-content">
            <div v-show="modalActiveTab === 'thongTin'" class="form-grid">
              <div class="form-group full-width">
                <label for="tenPhieuGiamGia">Tên đợt phát hành <span class="required">*</span></label>
                <input type="text" id="tenPhieuGiamGia" v-model="voucherInForm.tenPhieuGiamGia"
                  placeholder="Ví dụ: Voucher giảm giá 20/11" :class="{ 'is-invalid': errors.tenPhieuGiamGia }">
                <div v-if="errors.tenPhieuGiamGia" class="error-message">{{ errors.tenPhieuGiamGia }}</div>
              </div>
              <div class="form-group">
                <label>Loại giảm giá</label>
                <div class="radio-group-inline">
                  <input type="radio" id="type-percent" value="PHAN_TRAM" v-model="voucherInForm.loaiGiamGia"><label
                    for="type-percent">Theo %</label>
                  <input type="radio" id="type-fixed" value="SO_TIEN" v-model="voucherInForm.loaiGiamGia"><label
                    for="type-fixed">Số tiền</label>
                </div>
              </div>
              <div class="form-group">
                <label for="giaTriGiam">{{ voucherInForm.loaiGiamGia === 'PHAN_TRAM' ? 'Mức giảm (%)' : 'Mức giảm (VND)'
                }}</label>
                <input type="number" id="giaTriGiam" v-model.number="voucherInForm.giaTriGiam"
                  :class="{ 'is-invalid': errors.giaTriGiam }">
                <div v-if="errors.giaTriGiam" class="error-message">{{ errors.giaTriGiam }}</div>
              </div>
              <div v-if="voucherInForm.loaiGiamGia === 'PHAN_TRAM'" class="form-group">
                <label for="giaTriToiDa">Giảm tối đa (VND)</label>
                <input type="number" id="giaTriToiDa" v-model.number="voucherInForm.giaTriToiDa"
                  placeholder="Số tiền giảm tối đa" :class="{ 'is-invalid': errors.giaTriToiDa }">
                <div v-if="errors.giaTriToiDa" class="error-message">{{ errors.giaTriToiDa }}</div>
              </div>
              <div class="form-group">
                <label for="giaTriToiThieu">Giá trị đơn hàng tối thiểu (VND)</label>
                <input type="number" id="giaTriToiThieu" v-model.number="voucherInForm.giaTriToiThieu"
                  placeholder="Áp dụng cho đơn hàng từ" :class="{ 'is-invalid': errors.giaTriToiThieu }">
                <div v-if="errors.giaTriToiThieu" class="error-message">{{ errors.giaTriToiThieu }}</div>
              </div>
              <div class="form-group">
                <label>Trạng thái</label>
                <div class="radio-group-inline">
                  <input type="radio" id="status-active" :value="1" v-model.number="voucherInForm.trangThai"><label
                    for="status-active">Kích hoạt</label>
                  <input type="radio" id="status-inactive" :value="0" v-model.number="voucherInForm.trangThai"><label
                    for="status-inactive">Chưa áp dụng</label>
                </div>
              </div>
            </div>

            <div v-show="modalActiveTab === 'apDung'" class="form-grid">
              <div class="form-group">
                <label for="ngayBatDau">Hiệu lực từ</label>
                <input type="date" id="ngayBatDau" v-model="voucherInForm.ngayBatDau"
                  :class="{ 'is-invalid': errors.ngayBatDau }">
                <div v-if="errors.ngayBatDau" class="error-message">{{ errors.ngayBatDau }}</div>
              </div>
              <div class="form-group">
                <label for="ngayKetThuc">Đến</label>
                <input type="date" id="ngayKetThuc" v-model="voucherInForm.ngayKetThuc"
                  :class="{ 'is-invalid': errors.ngayKetThuc }">
                <div v-if="errors.ngayKetThuc" class="error-message">{{ errors.ngayKetThuc }}</div>
              </div>

              <div class="form-group full-width">
                <label>Đối tượng áp dụng</label>
                <select v-model="voucherInForm.doiTuongApDung">
                  <option value="ALL">Tất cả khách hàng (Voucher công khai)</option>
                  <option value="SPECIFIC">Khách hàng cụ thể (Voucher cá nhân)</option>
                </select>
              </div>

              <!-- HIỂN THỊ KHI CHỌN "TẤT CẢ KHÁCH HÀNG" -->
              <div v-if="voucherInForm.doiTuongApDung === 'ALL'" class="form-group full-width">
                <label for="soLuong">Số lượng phát hành</label>
                <input type="number" id="soLuong" v-model.number="voucherInForm.soLuong"
                  placeholder="Nhập tổng số voucher có thể sử dụng" :class="{ 'is-invalid': errors.soLuong }">
                <div v-if="errors.soLuong" class="error-message">{{ errors.soLuong }}</div>
              </div>

              <!-- HIỂN THỊ KHI CHỌN "KHÁCH HÀNG CỤ THỂ" -->
              <div v-if="voucherInForm.doiTuongApDung === 'SPECIFIC'"
                class="form-group full-width customer-selection-container">

                <!-- SỬA ĐỔI KHU VỰC NÀY -->
                <div class="customer-filters">

                  <!-- Bộ lọc Cấp độ (Giữ nguyên) -->
                  <div class="filter-item">
                    <label for="customerLevelFilter">Lọc theo cấp độ</label>
                    <select id="customerLevelFilter" v-model="customerLevelFilter">
                      <option value="">Tất cả cấp độ</option>
                      <option v-for="level in customerLevels" :key="level.id" :value="level.id">
                        {{ level.name }}
                      </option>
                    </select>
                  </div>

                  <!-- NÂNG CẤP: Thêm bộ lọc Giới tính -->
                  <div class="filter-item">
                    <label for="genderFilter">Lọc theo giới tính</label>
                    <select id="genderFilter" v-model="genderFilter">
                      <option value="">Tất cả giới tính</option>
                      <option value="1">Nam</option>
                      <option value="0">Nữ</option>
                    </select>
                  </div>

                  <!-- NÂNG CẤP: Thêm bộ lọc Ngày sinh (Từ ngày) -->
                  <div class="filter-item">
                    <label for="startDateFilter">Sinh từ ngày</label>
                    <input type="date" id="startDateFilter" v-model="startDateFilter">
                  </div>

                  <!-- NÂNG CẤP: Thêm bộ lọc Ngày sinh (Đến ngày) -->
                  <div class="filter-item">
                    <label for="endDateFilter">Đến ngày</label>
                    <input type="date" id="endDateFilter" v-model="endDateFilter">
                  </div>

                </div>
                <!-- KẾT THÚC SỬA ĐỔI -->


                <!-- Phần "Chọn tất cả" và bảng dữ liệu giữ nguyên -->
                <div class="select-all-container">
                  <input type="checkbox" id="select-all-customers" v-model="selectAll"
                    :disabled="filteredCustomers.length === 0">
                  <label for="select-all-customers">
                    Chọn tất cả ({{ filteredCustomers.length }} khách hàng đang hiển thị)
                  </label>
                </div>

                <label>Danh sách khách hàng</label>
                <table class="customer-table">
                  <thead>
                    <tr>
                      <th></th>
                      <th>Tên khách hàng</th>
                      <th>Giới tính</th>
                      <th>Ngày sinh</th>
                      <th>Cấp độ</th>
                      <th>SĐT</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="customer in filteredCustomers" :key="customer.id">
                      <td>
                        <input type="checkbox" :id="'customer-' + customer.id" :value="customer.id"
                          v-model="voucherInForm.danhSachKhachHangId" />
                      </td>
                      <td>
                        <label :for="'customer-' + customer.id">
                          {{ customer.tenKhachHang }}
                        </label>
                      </td>
                      <td>{{ customer.gioiTinh === 1 ? 'Nam' : 'Nữ' }}</td>
                      <td>{{ customer.ngaySinh ? formatDate(customer.ngaySinh, true) : 'Chưa cập nhật' }}</td>
                      <td>{{ customer.capDo }}</td>
                      <td>{{ customer.sdt }}</td>
                    </tr>
                    <tr v-if="filteredCustomers.length === 0">
                      <td colspan="4" class="no-customer">
                        Không tìm thấy khách hàng phù hợp.
                      </td>
                    </tr>
                  </tbody>
                </table>
                <div class="customer-count">
                  Đã chọn: {{ voucherInForm.danhSachKhachHangId.length }} khách hàng
                </div>
              </div>
            </div>
          </div>
        </div>



        <div class="modal-footer">
          <button @click="handleSaveVoucher" class="btn btn-sucess btn-save"><i class="far fa-save"></i> Lưu
            <span v-if="isSaving">
              <i class="fa-solid fa-spinner fa-spin"></i>
              Đang lưu...
            </span>
          </button>
          <button @click="closeModal" class="btn btn-danger"><i class="far fa-trash-alt"></i> Đóng</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ---- GENERAL & LAYOUT ---- */
.container-fluid {
  background-color: #f0f2f5;
  font-family: 'Public Sans', sans-serif;
}

.header-card,
.list-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
  overflow: hidden;
}

.title {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  color: white;
  padding: 20px 24px;
  font-size: 22px;
  font-weight: 600;
  display: flex;
  align-items: center;
}

/* ---- FILTER SECTION ---- */
.filter-section {
  padding: 20px 24px;
}

.filter-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  color: #333;
  font-size: 16px;
}

.filter-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-row {
  display: flex;
  gap: 20px;
  align-items: flex-end;
}

.filter-group {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.filter-group.search-group {
  flex: 2;
}

.filter-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #555;
}

.search-input-container {
  position: relative;
  display: flex;
  align-items: center;
}

.search-input {
  width: 100%;
  padding: 10px 15px 10px 40px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  height: 40px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.search-input:focus {
  outline: none;
  border-color: #764ba2;
  box-shadow: 0 0 0 3px rgba(118, 75, 162, 0.2);
}


.search-icon {
  position: absolute;
  left: 14px;
  color: #888;
  font-size: 14px;
}

.form-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  width: 100%;
  height: 40px;
}

.button-group {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 10px;
}

/* ---- LIST SECTION ---- */
.list-header {
  background: #f8f9fa;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #dee2e6;
  flex-wrap: wrap;
  gap: 16px;
}

.list-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
}

.tab-container {
  display: flex;
  gap: 5px;
}

.tab-item {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
  background: #e9ecef;
  color: #495057;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s ease-in-out;
}

.tab-item:hover {
  background: #d8dce1;
}

.tab-item.active {
  background: #66ea8b;
  color: white;
}

.tab-item .badge {
  background: white;
  color: #495057;
  border-radius: 10px;
  padding: 2px 8px;
  font-size: 12px;
}

.tab-item.active .badge {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

/* ---- VOUCHER LIST STYLING (using DIVs like a table) ---- */
.list-content-area {
  padding: 0 8px;
}

.voucher-list-header,
.voucher-item-row {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e9ecef;
  font-size: 14px;
  transition: background-color 0.2s;
}

.voucher-list-header {
  background: #f8f9fa;
  font-weight: 600;
  color: #495057;
}

.voucher-item-row {
  cursor: pointer;
}

.voucher-item-row:hover {
  background-color: #f8f9fa;
}

.voucher-item-row.selected {
  background-color: #e8eaf6;
  /* A light purple to indicate selection */
}

/* Column Widths */
.col-check {
  flex: 0 0 60px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.voucher-list-header .col-check .header-checkbox {
  margin-left: 24px;
  /* Điều chỉnh giá trị này để dịch sang phải nhiều hay ít */
}

.col-stt {
  flex: 0 0 60px;
}

.col-ma {
  flex: 0 0 140px;
}

.col-ten {
  flex: 2;
  min-width: 150px;
  font-weight: 500;
  color: #333;
}

.col-ngay {
  flex: 0 0 110px;
  text-align: center;
}

.col-soluong {
  flex: 0 0 100px;
  text-align: center;
}

.col-congKhai {
  flex: 0 0 100px;
  text-align: center;
}

.col-menhgia {
  flex: 0 0 150px;
  min-width: 100px;
}

.col-trangthai {
  flex: 0 0 150px;
  text-align: center;
}

.toggle-icon {
  transition: transform 0.3s ease;
  color: #6c757d;
}

.toggle-icon.rotated {
  transform: rotate(180deg);
}

/* Status Badge Styling (from your original code, slightly tweaked) */
.status-badge {
  padding: 5px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
  color: white;
  min-width: 120px;
  display: inline-block;
}

.status-badge.active {
  background-color: #28a745;
}

.status-badge.inactive {
  background-color: #6c757d;
}

.status-badge.expired {
  background-color: #dc3545;
}

/* Expanded Details Section */
.voucher-details {
  background-color: #fafafa;
  border-bottom: 2px solid #764ba2;
  padding: 20px;
}

/* ---- PAGINATION ---- */
.pagination-container {
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #dee2e6;
  background: #fdfdff;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-size-selector label {
  font-size: 14px;
  color: #495057;
  margin: 0;
}

.page-size-selector .form-select {
  width: 75px;
  height: 36px;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pagination-controls .btn {
  width: 40px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-info {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}


/* ---- EMPTY & LOADING STATE ---- */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #6c757d;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 20px;
  color: #ced4da;
}

.empty-state h3 {
  margin-bottom: 10px;
  color: #495057;
}

/* ---- MODAL STYLING ---- */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content-custom {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  width: 90%;
  max-width: 800px;
  /* Adjust as needed */
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: 16px 24px;
  border-bottom: 1px solid #dee2e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header .modal-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.modal-header .close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6c757d;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  flex-grow: 1;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #dee2e6;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  background: #f8f9fa;
}

/* Responsive */
@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }

  .pagination-container {
    flex-direction: column;
    gap: 15px;
  }

  .list-header {
    flex-direction: column;
    align-items: flex-start;
  }

  /* Bạn có thể thêm các style ẩn cột trên mobile nếu cần */
  .col-ngay,
  .col-ma {
    display: none;
  }
}

.voucher-details {
  background-color: #ffffff;
  /* Nền trắng để nổi bật */
  border-bottom: 2px solid #764ba2;
  animation: fadeIn 0.4s ease;
  box-shadow: inset 0 5px 8px -5px rgba(0, 0, 0, 0.1);
  /* Đổ bóng bên trong để tạo chiều sâu */
}

/* ---- TABS IN DETAILS ---- */
.details-tabs {
  background-color: #f8f9fa;
  padding: 5px 20px 0 20px;
  /* Thêm padding, bỏ padding bottom */
  border-bottom: 1px solid #dee2e6;
  display: flex;
  gap: 10px;
}

.tab-btn {
  background: none;
  border: none;
  padding: 12px 18px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #495057;
  border-bottom: 3px solid transparent;
  transition: all 0.2s;
  position: relative;
  top: 1px;
  /* Dịch xuống 1px để khớp với border bottom */
}

.tab-btn:hover {
  color: #333;
  background-color: #e9ecef;
}

.tab-btn.active {
  color: #764ba2;
  border-bottom-color: #764ba2;
  background-color: #ffffff;
  /* Nền trắng khi active để liền mạch với content */
}

/* ---- CONTENT OF TABS ---- */
.details-content {
  padding: 24px;
}

.details-section-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e9ecef;
}

/* ---- TAB 1: GRID CHI TIẾT THÔNG TIN ---- */
.details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px 24px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 14px;
}

.form-field label {
  font-weight: 500;
  color: #6c757d;
}

.form-field span {
  font-weight: 500;
  color: #333;
}

/* Nút switch trạng thái */
.form-check.form-switch {
  display: flex;
  align-items: center;
  padding-left: 0;
}

.form-check-input {
  margin-right: 10px;
}

.details-actions {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* ---- TAB 2 & 3: BỘ LỌC VÀ BẢNG ---- */

/* Bộ lọc trong tab */
.filter-controls {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
  align-items: flex-end;
}

.customer-filters {
  display: grid;
  /* Tạo ra 2 cột có độ rộng bằng nhau */
  grid-template-columns: repeat(2, 1fr);
  /* Khoảng cách giữa các ô */
  gap: 15px;
  margin-bottom: 20px;
  /* Khoảng cách với phần tử bên dưới */
}

/* Kiểu cho từng mục bộ lọc bên trong */
.filter-item {
  /* KHÔNG cần flex: 1 nữa */
  display: flex;
  flex-direction: column;
}

.filter-item label {
  font-size: 13px;
  font-weight: 500;
  color: #555;
  margin-bottom: 6px;
}

/* CSS để responsive: trên màn hình nhỏ, chuyển thành 1 cột */
@media (max-width: 768px) {
  .customer-filters {
    grid-template-columns: 1fr;
    /* Chỉ 1 cột */
  }
}

.filter-input,
.filter-select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 14px;
  height: 38px;
}

.filter-input:focus,
.filter-select:focus {
  outline: none;
  border-color: #764ba2;
  box-shadow: 0 0 0 2px rgba(118, 75, 162, 0.2);
}

/* Bảng trong tab */
.voucher-list-container {
  max-height: 400px;
  /* Giới hạn chiều cao và cho phép cuộn */
  overflow-y: auto;
  border: 1px solid #dee2e6;
  border-radius: 6px;
}

.tab-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.tab-table th,
.tab-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #dee2e6;
}

.tab-table thead th {
  background-color: #f8f9fa;
  font-weight: 600;
  position: sticky;
  /* Giúp header cố định khi cuộn */
  top: 0;
  z-index: 1;
}

.tab-table tbody tr:last-child td {
  border-bottom: none;
}

.tab-table tbody tr:hover {
  background-color: #f1f3f5;
}

.text-center {
  text-align: center;
  padding: 20px;
  color: #6c757d;
}

/* CSS cho trạng thái "Chưa sử dụng" */
.status-unused {
  color: #28a745;
  font-weight: 500;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeInOverlay 0.3s ease;
}

@keyframes fadeInOverlay {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

.modal-content-custom {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  width: 90%;
  max-width: 800px;
  /* Tăng độ rộng để chứa form 2 cột */
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  animation: slideInModal 0.4s ease-out;
}

@keyframes slideInModal {
  from {
    transform: translateY(-30px);
    opacity: 0;
  }

  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  padding: 16px 24px;
  border-bottom: 1px solid #dee2e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f8f9fa;
  border-top-left-radius: 12px;
  border-top-right-radius: 12px;
}

.modal-header .modal-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.modal-header .close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6c757d;
  transition: color 0.2s;
}

.modal-header .close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 0;
  /* Bỏ padding để tab có thể full-width */
  overflow-y: auto;
  flex-grow: 1;
}

/* ---- MODAL TABS ---- */
.modal-tabs {
  display: flex;
  background-color: #f1f3f5;
  padding: 5px 24px 0 24px;
  border-bottom: 1px solid #dee2e6;
}

.modal-tab-btn {
  background: none;
  border: none;
  padding: 12px 20px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  color: #495057;
  border-bottom: 3px solid transparent;
  transition: all 0.2s;
}

.modal-tab-btn.active {
  color: #764ba2;
  border-bottom-color: #764ba2;
}

/* ---- MODAL FORM CONTENT ---- */
.tab-content {
  padding: 24px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  /* 2 cột */
  gap: 20px 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
  /* Chiếm toàn bộ chiều rộng grid */
}

.form-group label {
  margin-bottom: 8px;
  font-weight: 500;
  font-size: 14px;
  color: #343a40;
}

.form-group label .required {
  color: #dc3545;
  margin-left: 2px;
}

.form-group input[type="text"],
.form-group input[type="number"],
.form-group input[type="date"],
.form-group select {
  width: 100%;
  padding: 10px 12px;
  font-size: 14px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #764ba2;
  box-shadow: 0 0 0 3px rgba(118, 75, 162, 0.2);
}

.form-group .is-invalid {
  border-color: #dc3545;
}

.error-message {
  color: #dc3545;
  font-size: 12px;
  margin-top: 4px;
}

/* Radio buttons inline */
.radio-group-inline {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-top: 5px;
}

.radio-group-inline input[type="radio"] {
  margin-right: 5px;
}

.radio-group-inline label {
  margin-bottom: 0;
  font-weight: normal;
}

/* ---- KHU VỰC CHỌN KHÁCH HÀNG ---- */
.customer-selection-container {
  background-color: #f8f9fa;
  border: 1px solid #e9ecef;
  padding: 16px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.customer-filters {
  max-width: 300px;
}

.customer-filters label {
  font-size: 13px;
}

.select-all-container {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e9ecef;
}

.select-all-container label {
  margin: 0;
  font-weight: 500;
}

.customer-list-box {
  max-height: 250px;
  overflow-y: auto;
  border: 1px solid #ced4da;
  border-radius: 6px;
  padding: 8px;
  background-color: #fff;
}

.customer-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.customer-item:hover {
  background-color: #f1f3f5;
}

.customer-item input[type="checkbox"] {
  margin-right: 12px;
}

.customer-item label {
  margin-bottom: 0;
  font-weight: normal;
  cursor: pointer;
  flex-grow: 1;
}

.customer-level {
  color: #6c757d;
  font-size: 13px;
}

.no-customer {
  padding: 20px;
  text-align: center;
  color: #6c757d;
}

.customer-count {
  text-align: right;
  font-size: 13px;
  font-weight: 500;
  color: #333;
  margin-top: 8px;
}

/* ---- MODAL FOOTER ---- */
.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  background-color: #f8f9fa;
  border-bottom-left-radius: 12px;
  border-bottom-right-radius: 12px;
}

.btn-save {
  /* Thêm các style riêng cho nút lưu nếu cần */
  background-color: #66ea8b;
  /* Màu xanh lá */
  border-color: #66ea8b;
}

.btn-save:hover {
  background-color: #52a764;
  border-color: #1e7e34;
}

/* Spinner trong nút Lưu */
.btn-save span i {
  margin-right: 5px;
}

.customer-table {
  width: 100%;
  border-collapse: collapse;
}

.customer-table th,
.customer-table td {
  border: 1px solid #ddd;
  padding: 8px;
}

.customer-table th {
  background-color: #f4f4f4;
  text-align: left;
}

.no-customer {
  text-align: center;
  color: #999;
  font-style: italic;
}
</style>