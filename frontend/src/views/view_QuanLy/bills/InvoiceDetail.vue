<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import client from '@/utils/api.js';
import QRCode from 'qrcode';
import Swal from 'sweetalert2';
import { useToast } from "vue-toastification";
import 'vue-toastification/dist/index.css'
import OrderProgressTracker from './OrderProgressTracker.vue';
const toast = useToast();
// const showSuccess = (message) => toast.success(message, { timeout: 3000 });
// const showError = (message) => toast.error(message, { timeout: 4000 });
// const showWarning = (message) => toast.warning(message, { timeout: 3500 });



const qrCodeUrl = ref('');
const route = useRoute();
const hoaDon = ref({
  trangThai: 0,
  loaiHoaDon: 0,
  maHoaDon: '',
  ngayTao: null,
  tongTien: 0,
  phiVanChuyen: 0,
  soTienGiam: 0,
  soTienKhachHangThanhToan: 0,
  tongTienThua: 0,
  thanhToanTruoc: false,
  khachHang: null
});
// const hoaDon = ref({
//   loaiHoaDon: 1, // 0: tại quầy, 1: online
//   trangThai: 2,
//   times: [] // 👈 Đảm bảo khởi tạo là mảng
// })
const listLichSuHoaDonDto = ref([]);
const listChiTietSanPham = ref([]);
const ghiChu = ref('');
const showLichSu = ref(false);
const paymentMethods = ref([]); // Thêm reactive variable để lưu danh sách hình thức thanh toán

const listLichSuThanhToan = computed(() =>
  listLichSuHoaDonDto.value.filter(item =>
    item.trangThai === 1 // Chỉ lọc trạng thái "Đã thanh toán"
  )
);


const generateQRCode = async (text) => {
  try {
    qrCodeUrl.value = await QRCode.toDataURL(text);
  } catch (err) {
    console.error('Lỗi tạo QR code:', err);
  }
};


const printInvoice = () => {
  window.print();
};


const downloadPdf = async () => {
  try {
    const response = await client.get(`/api/hoa-don/export-pdf/${route.params.id}`, {
      responseType: 'blob',
    });

    const blob = new Blob([response.data], { type: 'application/pdf' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `hoa-don-${hoaDon.value.maHoaDon}.pdf`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (error) {
    console.error("Lỗi khi tải PDF:", error);
    alert("Tải PDF thất bại!");

    // if có thể dùng cho mọi trường họp lỗi
    if (error.response && error.response.data instanceof Blob && error.response.data.type === "application/json") {
      const reader = new FileReader();
      reader.onload = function () {
        const errorText = reader.result;
        console.error("Chi tiết lỗi từ server:", JSON.parse(errorText));
        alert("In PDF thất bại: " + JSON.parse(errorText).message);
      };
      reader.readAsText(error.response.data);
    } else {
      alert("In PDF thất bại!");
    }
  }
};



// Hàm fetch chi tiết hóa đơn
const fetchHoaDonDetail = async () => {
  try {
    const response = await client.get(`/api/hoa-don/detail/${route.params.id}`);
    console.log(response.data); // Kiểm tra dữ liệu trả về
    hoaDon.value = response.data.hoaDon; // thêm .hoaDon để hiển thị dữ liệu ;))
    listLichSuHoaDonDto.value = response.data.listLichSuHoaDonDto || [];
    listChiTietSanPham.value = response.data.listChiTietSanPham || [];

    // ✅ Tính tiền thừa nếu có
    const tong = hoaDon.value.tongTienSauKhiGiam || hoaDon.value.tongTien || 0;
    const daThanhToan = hoaDon.value.soTienKhachHangThanhToan || 0;
    const tienThua = daThanhToan - tong;
    hoaDon.value.tongTienThua = tienThua > 0 ? tienThua : 0;

    // Tạo QR từ mã hóa đơn (hoặc URL, hoặc chuỗi tùy bạn)
    await generateQRCode(hoaDon.value.maHoaDon);
  } catch (error) {
    console.error('Lỗi khi tải chi tiết hóa đơn:', error);
  }
};


const getCurrentUserName = () => {
  const user = JSON.parse(localStorage.getItem("user"));
  return user?.tenNhanVien || "admin";
};

// Safe address formatter to avoid showing 'undefined' and handle missing parts
const formatAddress = (khachHang) => {
  try {
    if (!khachHang || !Array.isArray(khachHang.diaChis) || khachHang.diaChis.length === 0) return 'N/A';
    // Prefer active address (trangThai === 1), otherwise first address
    const addr = khachHang.diaChis.find(dc => dc.trangThai === 1) || khachHang.diaChis[0];
    if (!addr) return 'N/A';
    const parts = [];
    if (addr.soNha) parts.push(addr.soNha);
    if (addr.diaChiChiTiet) parts.push(addr.diaChiChiTiet);
    if (addr.xaPhuong) parts.push(addr.xaPhuong);
    if (addr.quanHuyen) parts.push(addr.quanHuyen);
    if (addr.thanhPho) parts.push(addr.thanhPho);
    return parts.length ? parts.join(', ') : 'N/A';
  } catch (e) {
    return 'N/A';
  }
};


const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'text-warning'; //0
    case 1: return 'text-success'; //1
    case 2: return 'text-warning';//2
    case 3: return 'text-info';//3
    case 4: return 'text-success';//4
    case 5: return 'text-success';//5
    case 6: return 'text-danger';//6
    case 7: return 'text-warning';//7 (Trả hàng)
    case 8: return 'text-info';   //8 (Đang trả hàng)
    case 9: return 'text-success';//9 (Trả hàng thành công)
    case 10: return 'text-danger';//10 (Từ chối trả hàng)
    case 11: return 'text-warning';//11 (Khách chưa xác nhận)
    default: return 'text-dark';
  }
};

const getStatusText = (status) => {
  const statuses = [
    'Chờ xác nhận', // 0
    'Đã xác nhận', //1
    'Chờ vận chuyển', //2
    'Đang vận chuyển', //3
    'Đã giao hàng', //4
    'Hoàn thành', //5
    'Đã hủy',      //6
    'Yêu cầu Trả hàng',    //7
    'Đang trả hàng', //8
    'Trả hàng thành công', //9
    'Từ chối trả hàng', //10
    'Khách chưa xác nhận' //11
  ];
  return statuses[status] || 'Không xác định';
};

// Xác nhận chuyển trạng thái
const changeStatus = async () => {
  try {
    const nextStatus = getNextStatus.value;
    if (nextStatus === null) return;

    // Kiểm tra điều kiện tiền thừa
    if (
      hoaDon.value.trangThai === 4 &&
      [4, 1, 2].includes(nextStatus) &&
      hoaDon.value.tongTienThua &&
      hoaDon.value.tongTienThua > 0
    ) {
      await Swal.fire({
        title: 'Không thể chuyển trạng thái',
        html: `Khách đã thanh toán dư <strong>${formatCurrency(hoaDon.value.tongTienThua)}</strong>. Cần hoàn tiền trước khi chuyển trạng thái!`,
        icon: 'warning',
        confirmButtonText: 'Đã hiểu'
      });
      return;
    }

    // Kiểm tra thanh toán
    if (
      hoaDon.value.trangThai === 3 &&
      (!hoaDon.value.soTienKhachHangThanhToan || hoaDon.value.soTienKhachHangThanhToan <= 0)
    ) {
      await Swal.fire({
        title: 'Thiếu thanh toán',
        text: 'Bạn cần thanh toán trước khi chuyển trạng thái!',
        icon: 'error',
        confirmButtonText: 'Đã hiểu'
      });
      return;
    }

    const statusText = getStatusText(nextStatus);
    const currentStatusText = getStatusText(hoaDon.value.trangThai);

    // Hiển thị popup xác nhận + nhập ghi chú
    const { value: note, isConfirmed } = await Swal.fire({
      title: `Chuyển trạng thái đơn hàng`,
      html: `
        <div class="text-start mb-3">
          <p><strong>Từ:</strong> ${currentStatusText}</p>
          <p><strong>Sang:</strong> <span class="text-success">${statusText}</span></p>
        </div>
        <div class="mb-3">
          <label class="form-label">Ghi chú (không bắt buộc)</label>
          <textarea 
            id="swal-status-note" 
            class="swal2-textarea" 
            placeholder="Lý do chuyển trạng thái..."
          ></textarea>
        </div>
      `,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Xác nhận',
      cancelButtonText: 'Hủy',
      reverseButtons: true,
      focusConfirm: false,
      preConfirm: () => {
        return Swal.getPopup().querySelector('#swal-status-note').value.trim();
      }
    });

    if (!isConfirmed) return;

    // Lưu trạng thái cũ để khôi phục nếu có lỗi
    const oldStatus = hoaDon.value.trangThai;

    // Cập nhật ghi chú và trạng thái
    ghiChu.value = note || '';
    hoaDon.value.trangThai = nextStatus;

    // Hiệu ứng loading trong khi cập nhật
    Swal.fire({
      title: 'Đang xử lý...',
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      }
    });

    try {
      await capNhatTrangThai();

      // Chỉ hiển thị thông báo thành công khi API thực sự thành công
      await Swal.fire({
        title: 'Thành công!',
        text: `Đã chuyển sang trạng thái ${statusText}`,
        icon: 'success',
        timer: 2000,
        showConfirmButton: false
      });
    } catch (capNhatError) {
      // Khôi phục trạng thái cũ ngay lập tức
      hoaDon.value.trangThai = oldStatus;
      
      // Ném lại lỗi để catch bên ngoài xử lý
      throw capNhatError;
    }

  } catch (error) {
    console.error("Lỗi khi chuyển trạng thái:", error);

    await Swal.fire({
      title: 'Lỗi!',
      text: error.response?.data?.message || 'Cập nhật trạng thái thất bại',
      icon: 'error',
      confirmButtonText: 'Đóng'
    });

    // Khôi phục trạng thái cũ nếu có lỗi
    await fetchHoaDonDetail();
  }
};



const getNextStatus = computed(() => {
  const currentStatus = hoaDon.value.trangThai;

  // Nếu là hóa đơn tại quầy và đã chuyển sang giao hàng (trạng thái >= 2)
  if (hoaDon.value.loaiHoaDon === 0 && currentStatus >= 2) {
    if (currentStatus === 2) return 3;
    if (currentStatus === 3) return 4;
    if (currentStatus === 4) return 5;
    return null;
  }

  // Luồng bình thường cho tại quầy
  if (hoaDon.value.loaiHoaDon === 0) {
    if (currentStatus === 1) return 5;
    return null;
  }

  // Luồng online
  if (hoaDon.value.loaiHoaDon === 1) {
    const isPrepaid = hoaDon.value.thanhToanTruoc;

    // Với thanhToanTruoc = true: phải thanh toán đủ thì mới chuyển trạng thái gì cũng được.(đã thanh toán trước)
    // Với thanhToanTruoc = false: cho phép chuyển qua giao hàng, nhưng khi đến trạng thái hoàn thành (chưa thanh toán trước)

    if (currentStatus === 0) return 1;
    if (currentStatus === 1) return 2;
    if (currentStatus === 2) return 3;
    if (currentStatus === 3) return 4;

    if (currentStatus === 4) {
      if (isPrepaid) {
        return 5; // Khách đã thanh toán trước => cho phép hoàn thành
      } else {
        // Nếu khách chưa thanh toán trước => kiểm tra đã thanh toán đủ chưa
        const tong = hoaDon.value.tongTienSauKhiGiam - hoaDon.value.soTienGiam + hoaDon.value.phiVanChuyen || hoaDon.value.tongTien - hoaDon.value.soTienGiam + hoaDon.value.phiVanChuyen || 0;
        const daThanhToan = hoaDon.value.soTienKhachHangThanhToan - hoaDon.value.soTienGiam + hoaDon.value.phiVanChuyen || 0;
        // const tienThua = daThanhToan - tong;

        // Nếu đã thanh toán đủ và hoàn tiền thừa nếu có => cho chuyển sang hoàn thành
        if (daThanhToan >= tong && (!hoaDon.value.tongTienThua || hoaDon.value.tongTienThua <= 0)) {
          return 5;
        }
        return null;
      }
    }
  }
  return null;
});



// Chuyển đến trạng thái giao hàng (chỉ dành cho hóa đơn tại quầy)
const chuyenDenGiaoHang = async () => {
  try {
    // Kiểm tra tiền thừa
    if (hoaDon.value.tongTienThua && hoaDon.value.tongTienThua > 0) {
      await Swal.fire({
        title: 'Không thể chuyển trạng thái',
        html: `
          <div class="text-start">
            <p>Cần hoàn tiền thừa cho khách trước khi chuyển trạng thái:</p>
            <p class="fw-bold text-danger">${formatCurrency(hoaDon.value.tongTienThua)}</p>
          </div>
        `,
        icon: 'warning',
        confirmButtonText: 'Đã hiểu',
        confirmButtonColor: '#d33'
      });
      return;
    }

    // Popup xác nhận chuyển trạng thái
    const { value: note, isConfirmed } = await Swal.fire({
      title: 'Xác nhận chuyển trạng thái',
      html: `
        <div class="text-start mb-3">
          <p>Bạn đang chuyển từ <strong>Đã xác nhận</strong> sang:</p>
          <p class="text-success fw-bold">Chờ vận chuyển</p>
        </div>
        <div class="mb-3">
          <label class="form-label">Ghi chú (không bắt buộc)</label>
          <textarea 
            id="swal-shipping-note" 
            class="swal2-textarea" 
            placeholder="Nhập ghi chú nếu cần..."
          >Chuyển từ tại quầy sang giao hàng</textarea>
        </div>
      `,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Xác nhận',
      cancelButtonText: 'Hủy',
      reverseButtons: true,
      focusConfirm: false,
      preConfirm: () => {
        return Swal.getPopup().querySelector('#swal-shipping-note').value.trim();
      }
    });

    if (!isConfirmed) return;

    // Cập nhật trạng thái
    hoaDon.value.trangThai = 2;
    ghiChu.value = note || 'Chuyển từ tại quầy sang giao hàng';

    // Hiển thị loading
    Swal.fire({
      title: 'Đang xử lý...',
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      }
    });

    await capNhatTrangThai();

    // Thông báo thành công
    await Swal.fire({
      title: 'Thành công!',
      text: 'Đơn hàng đã chuyển sang trạng thái chờ vận chuyển',
      icon: 'success',
      timer: 2000,
      showConfirmButton: false
    });

  } catch (error) {
    console.error("Lỗi khi chuyển trạng thái:", error);

    await Swal.fire({
      title: 'Lỗi!',
      text: error.response?.data?.message || 'Chuyển trạng thái thất bại',
      icon: 'error',
      confirmButtonText: 'Đóng'
    });
  }
};



const undoStatus = async () => {
  try {
    // Kiểm tra điều kiện không thể hoàn tác
    if (hoaDon.value.trangThai === 0 ||
      (hoaDon.value.loaiHoaDon === 0 && hoaDon.value.trangThai === 5)) {
      await Swal.fire({
        title: 'Không thể hoàn tác',
        html: `
          <div class="text-start">
            <p>Không thể hoàn tác ở trạng thái hiện tại:</p>
            <p class="fw-bold">${getStatusText(hoaDon.value.trangThai)}</p>
          </div>
        `,
        icon: 'warning',
        confirmButtonText: 'Đã hiểu'
      });
      return;
    }

    const currentStatus = getStatusText(hoaDon.value.trangThai);
    const prevStatus = hoaDon.value.trangThai - 1;
    const prevStatusText = getStatusText(prevStatus);

    // Popup xác nhận hoàn tác
    const { isConfirmed } = await Swal.fire({
      title: 'Xác nhận hoàn tác',
      html: `
        <div class="text-start mb-3">
          <p>Bạn đang hoàn tác từ:</p>
          <p class="fw-bold">${currentStatus}</p>
          <p>Sang trạng thái:</p>
          <p class="text-success fw-bold">${prevStatusText}</p>
        </div>
        <div class="text-muted small">
          <i class="bi bi-info-circle"></i> Hành động này sẽ xóa bản ghi lịch sử gần nhất
        </div>
      `,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Xác nhận hoàn tác',
      cancelButtonText: 'Hủy bỏ',
      confirmButtonColor: '#d33',
      reverseButtons: true,
      focusConfirm: false
    });

    if (!isConfirmed) return;

    // Cập nhật trạng thái
    hoaDon.value.trangThai = prevStatus;
    listLichSuHoaDonDto.value.pop();

    // Lấy thông tin nhân viên
    const user = JSON.parse(localStorage.getItem("user"));
    const tenNhanVien = user?.tenNhanVien || "admin";

    // Hiển thị loading
    Swal.fire({
      title: 'Đang xử lý...',
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      }
    });

    // Gọi API cập nhật
    await client.put(`/api/hoa-don/cap-nhat-trang-thai/${hoaDon.value.id}`, null, {
      params: {
        trangThai: prevStatus,
        ghiChu: "Hoàn tác trạng thái",
        tenNhanVien
      }
    });

    // Thông báo thành công
    await Swal.fire({
      title: 'Hoàn tác thành công!',
      html: `
        <div class="text-start">
          <p>Đã hoàn tác về trạng thái:</p>
          <p class="text-success fw-bold">${prevStatusText}</p>
        </div>
      `,
      icon: 'success',
      timer: 2000,
      showConfirmButton: false
    });

    // Refresh dữ liệu
    await fetchHoaDonDetail();

  } catch (error) {
    console.error("Lỗi khi hoàn tác:", error);

    await Swal.fire({
      title: 'Hoàn tác thất bại!',
      text: error.response?.data?.message || 'Có lỗi xảy ra khi hoàn tác',
      icon: 'error',
      confirmButtonText: 'Đóng'
    });
  }
};

const capNhatTrangThai = async () => {
  try {
    const id = hoaDon.value.id;
    const trangThai = hoaDon.value.trangThai;
    const ghiChuCapNhat = ghiChu.value || '';

    if (!id || typeof trangThai !== 'number') {
      toast.error("Thiếu thông tin cập nhật trạng thái!", { timeout: 3000 });
      return;
    }

    const payload = {
      trangThai: trangThai,
      ghiChu: ghiChuCapNhat,
    };

    const user = JSON.parse(localStorage.getItem("user"));
    const tenNhanVien = user?.tenNhanVien || "admin";

    console.log("Gửi cập nhật trạng thái:", id, payload);

    // Hiển thị loading
    toast.info("Đang cập nhật trạng thái...", { timeout: false });

    await client.put(`/api/hoa-don/cap-nhat-trang-thai/${hoaDon.value.id}`, null, {
      params: {
        trangThai: hoaDon.value.trangThai,
        ghiChu: ghiChu.value,
        tenNhanVien
      }
    });

    // Đóng toast loading và hiển thị thành công
    toast.clear();
    toast.success("Cập nhật trạng thái thành công!", { timeout: 2000 });

    await fetchHoaDonDetail();
  } catch (error) {
    console.error("Lỗi khi cập nhật trạng thái:", error);

    // Đóng toast loading nếu có
    toast.clear();

    let errorMessage = "Cập nhật trạng thái thất bại!";
    if (error.response) {
      try {
        // Xử lý cả trường hợp response là Blob
        if (error.response.data instanceof Blob) {
          const errorText = await error.response.data.text();
          errorMessage = JSON.parse(errorText).message || errorMessage;
        } else {
          errorMessage = error.response.data?.message || error.response.statusText || errorMessage;
        }
      } catch (e) {
        console.error("Lỗi xử lý error response:", e);
      }
    }

    toast.error(errorMessage, { timeout: 3000 });
    
    // Ném lại lỗi để hàm gọi có thể xử lý
    throw error;
  }
};

const cancelOrReturn = async (actionType) => {
  // Nếu không truyền actionType, xác định theo trạng thái
  if (!actionType) {
    if ([0, 1, 2].includes(hoaDon.value.trangThai)) actionType = 'cancel';
    else if (hoaDon.value.trangThai === 5) actionType = 'return';
    else if (hoaDon.value.trangThai === 7) actionType = 'processingReturn';
    else if (hoaDon.value.trangThai === 8) actionType = 'acceptReturn';
    else if (hoaDon.value.trangThai === 10) actionType = 'rejectReturn';
    else return;
  }
  let action = '';
  let actionText = '';
  let newStatus = null;
  switch (actionType) {
    case 'cancel':
      action = 'Hủy đơn';
      actionText = 'hủy đơn';
      newStatus = 6;
      break;
    case 'return':
      action = 'Trả hàng';
      actionText = 'trả hàng';
      newStatus = 7;
      break;
    case 'processingReturn':
      action = 'Đang trả hàng';
      actionText = 'đang trả hàng';
      newStatus = 8;
      break;
    case 'acceptReturn':
      action = 'Xác nhận trả hàng thành công';
      actionText = 'trả hàng thành công';
      newStatus = 9;
      break;
    case 'rejectReturn':
      action = 'Từ chối trả hàng';
      actionText = 'từ chối trả hàng';
      newStatus = 10;
      break;
    default:
      return;
  }
  try {
    const currentStatus = getStatusText(hoaDon.value.trangThai);
    // Hiển thị popup xác nhận
    const { value: reason, isConfirmed } = await Swal.fire({
      title: `Xác nhận ${actionText}`,
      html: `
        <div class="text-start mb-3">
          <p>Bạn đang thực hiện: <strong class="text-danger">${action}</strong></p>
          <p>Trạng thái hiện tại: <strong>${currentStatus}</strong></p>
        </div>
        <div class="mb-3">
          <label class="form-label">Lý do ${actionText} (không bắt buộc)</label>
          <textarea 
            id="swal-cancel-reason" 
            class="swal2-textarea" 
            placeholder="Nhập lý do ${actionText}..."
            rows="3"
          ></textarea>
        </div>
      `,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: `Xác nhận ${action}`,
      cancelButtonText: 'Hủy bỏ',
      confirmButtonColor: '#d33',
      reverseButtons: true,
      focusConfirm: false,
      preConfirm: () => {
        return Swal.getPopup().querySelector('#swal-cancel-reason').value.trim();
      }
    });
    if (!isConfirmed) return;
    // Cập nhật giá trị
    ghiChu.value = reason || '';
    hoaDon.value.trangThai = newStatus;
    // Hiển thị loading
    Swal.fire({
      title: 'Đang xử lý...',
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      }
    });
    await capNhatTrangThai();
    // Thông báo thành công
    await Swal.fire({
      title: 'Thành công!',
      html: `
        <div class="text-start">
          <p>Đã ${actionText} đơn hàng thành công</p>
          ${reason ? `<p><strong>Lý do:</strong> ${reason}</p>` : ''}
        </div>
      `,
      icon: 'success',
      timer: 2500,
      showConfirmButton: false
    });
    // Refresh dữ liệu
    await fetchHoaDonDetail();
  } catch (error) {
    console.error(`Lỗi khi ${actionText}:`, error);
    await Swal.fire({
      title: 'Lỗi!',
      text: error.response?.data?.message || `${action} thất bại!`,
      icon: 'error',
      confirmButtonText: 'Đóng'
    });
  }
};

const processPayment = async () => {
  try {
    const tongTien = hoaDon.value.tongTien - hoaDon.value.soTienGiam + hoaDon.value.phiVanChuyen;
    const { value: formValues } = await Swal.fire({
      title: 'THANH TOÁN HÓA ĐƠN',
      html: `
        <div class="text-start mb-3">
          <p><strong>Tổng tiền:</strong> ${formatCurrency(tongTien)}</p>
        </div>
        
        <div class="mb-3">
          <label class="form-label">Số tiền thanh toán</label>
          <input 
            id="swal-payment-amount" 
            class="swal2-input" 
            type="number" 
            min="${tongTien}" 
            value="${tongTien}"
            placeholder="Nhập số tiền"
          >
        </div>
        
        <div class="mb-3">
          <label class="form-label">Hình thức thanh toán</label>
          <select id="swal-payment-method" class="swal2-input">
            ${paymentMethods.value.map(method => 
              `<option value="${method.id}" ${method.tenHinhThuc.toLowerCase().includes('tiền mặt') ? 'selected' : ''}>${method.tenHinhThuc}</option>`
            ).join('')}
          </select>
        </div>
        
        <div id="swal-transaction-container" class="mb-3" style="display: none;">
          <label class="form-label">Mã giao dịch</label>
          <input 
            id="swal-transaction-code" 
            class="swal2-input" 
            type="text" 
            placeholder="Nhập mã giao dịch"
          >
        </div>
        
        <div class="mb-3">
          <label class="form-label">Ghi chú (không bắt buộc)</label>
          <textarea 
            id="swal-payment-note" 
            class="swal2-textarea" 
            placeholder="Nhập ghi chú..."
          ></textarea>
        </div>
      `,
      focusConfirm: false,
      showCancelButton: true,
      confirmButtonText: 'Xác nhận thanh toán',
      cancelButtonText: 'Hủy',
      preConfirm: () => {
        const amount = parseFloat(Swal.getPopup().querySelector('#swal-payment-amount').value);
        const method = Swal.getPopup().querySelector('#swal-payment-method').value;
        const note = Swal.getPopup().querySelector('#swal-payment-note').value;

        // Validate số tiền
        if (isNaN(amount) || amount < tongTien) {
          Swal.showValidationMessage(`Số tiền phải ≥ ${formatCurrency(tongTien)}`);
          return false;
        }

        // Validate mã giao dịch nếu cần (kiểm tra theo tên hình thức)
        const selectedMethod = paymentMethods.value.find(m => m.id === method);
        if (selectedMethod && selectedMethod.tenHinhThuc.toLowerCase() !== 'tiền mặt') {
          const transactionCode = Swal.getPopup().querySelector('#swal-transaction-code').value.trim();
          if (!transactionCode) {
            Swal.showValidationMessage('Vui lòng nhập mã giao dịch');
            return false;
          }
        }

        return { amount, method, note };
      },
      didOpen: () => {
        // Xử lý hiển thị mã giao dịch khi chọn hình thức
        const methodSelect = Swal.getPopup().querySelector('#swal-payment-method');
        const transactionContainer = Swal.getPopup().querySelector('#swal-transaction-container');

        methodSelect.addEventListener('change', (e) => {
          const selectedMethod = paymentMethods.value.find(m => m.id === e.target.value);
          transactionContainer.style.display = 
            selectedMethod && selectedMethod.tenHinhThuc.toLowerCase() === 'tiền mặt' ? 'none' : 'block';
        });
      }
    });

    if (!formValues) return;

    const { amount, method, note } = formValues;
    
    // Xử lý mã giao dịch
    let maGiaoDich = '';
    const selectedMethod = paymentMethods.value.find(m => m.id === method);
    if (selectedMethod && selectedMethod.tenHinhThuc.toLowerCase() !== 'tiền mặt') {
      maGiaoDich = Swal.getPopup().querySelector('#swal-transaction-code').value.trim();
    }

    // Ghi chú
    ghiChu.value = note;

    // Gọi API thanh toán
    await client.post(`/api/hoa-don/thanh-toan/${hoaDon.value.id}`, null, {
      params: {
        tongTien: tongTien,
        daThanhToan: amount,
        hinhThucId: method, // Sử dụng trực tiếp ID từ API
        maGiaoDich,
        ghiChu: note,
        tenNhanVien: getCurrentUserName()
      }
    });

    // Cập nhật trạng thái
    hoaDon.value.trangThai = 4;

    // Thông báo thành công
    await Swal.fire({
      title: 'Thành công!',
      text: 'Thanh toán đã được ghi nhận',
      icon: 'success',
      timer: 2000,
      showConfirmButton: false
    });

    await fetchHoaDonDetail();

  } catch (error) {
    console.error("Lỗi thanh toán:", error);

    let errorMessage = "Thanh toán thất bại!";
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message;
    }

    await Swal.fire({
      title: 'Lỗi!',
      text: errorMessage,
      icon: 'error',
      confirmButtonText: 'Đóng'
    });
  }
};

const daQuaTrangThaiGiaoHang = computed(() => {
  return listLichSuHoaDonDto.value.some(ls =>
    [2, 3, 4].includes(ls.trangThai)
  );
});

// Computed property để tạo mảng times từ lịch sử
const orderTimes = computed(() => {
  try {
    // Nếu hoaDon.times tồn tại và có dữ liệu, sử dụng nó
    if (hoaDon.value.times && Array.isArray(hoaDon.value.times) && hoaDon.value.times.length > 0) {
      console.log('Using hoaDon.times:', hoaDon.value.times);
      return hoaDon.value.times;
    }
    
    // Nếu không có, tạo từ lịch sử
    const times = [];
    
    // Sắp xếp lịch sử theo thời gian để đảm bảo thứ tự đúng
    const sortedHistory = [...listLichSuHoaDonDto.value].sort((a, b) => {
      const timeA = new Date(a.ngayCapNhat || 0);
      const timeB = new Date(b.ngayCapNhat || 0);
      return timeA - timeB;
    });
    
    // Tạo mảng times dựa trên lịch sử thực tế
    sortedHistory.forEach(history => {
      if (history.ngayCapNhat) {
        times[history.trangThai] = history.ngayCapNhat;
      }
    });
    
    // Nếu có ngày tạo hóa đơn, sử dụng làm thời gian cho trạng thái đầu tiên
    if (hoaDon.value.ngayTao && times.length === 0) {
      times[0] = hoaDon.value.ngayTao;
    }
    
    console.log('Generated times from history:', times);
    return times;
  } catch (error) {
    console.error('Error generating times:', error);
    return [];
  }
});




const refundExcess = async () => {
  try {
    const soTienHoan = hoaDon.value.tongTienThua;
    const formattedAmount = formatCurrency(soTienHoan);
    const currentUser = getCurrentUserName();

    // Kiểm tra số tiền hoàn
    if (!soTienHoan || soTienHoan <= 0) {
      await Swal.fire({
        title: 'Không có tiền thừa',
        text: 'Không có số tiền nào cần hoàn lại cho khách',
        icon: 'info',
        confirmButtonText: 'Đã hiểu'
      });
      return;
    }

    // Ghi chú mặc định
    let ghiChuHoanTien = "Hoàn tiền thừa cho khách";

    // Popup xác nhận hoàn tiền
    const { value: note, isConfirmed } = await Swal.fire({
      title: 'XÁC NHẬN HOÀN TIỀN',
      html: `
        <div class="text-start">
          <p><strong>Số tiền hoàn:</strong> <span class="text-success fw-bold">${formattedAmount}</span></p>
          <p><strong>Nhân viên thực hiện:</strong> ${currentUser}</p>
          <div class="mt-3">
            <label class="form-label">Ghi chú (không bắt buộc)</label>
            <textarea 
              id="swal-refund-note" 
              class="swal2-textarea" 
              placeholder="Nhập ghi chú hoàn tiền..."
              rows="2"
            >${ghiChuHoanTien}</textarea>
          </div>
        </div>
      `,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Xác nhận hoàn tiền',
      cancelButtonText: 'Hủy bỏ',
      confirmButtonColor: '#28a745',
      reverseButtons: true,
      focusConfirm: false,
      preConfirm: () => {
        const popup = Swal.getPopup();
        if (!popup) return ghiChuHoanTien;
        const textarea = popup.querySelector('#swal-refund-note');
        return textarea ? textarea.value.trim() : ghiChuHoanTien;
      }
    });

    if (!isConfirmed) return;
    ghiChuHoanTien = note || ghiChuHoanTien;

    // Hiển thị loading (không dùng await ở đây để không chặn)
    Swal.fire({
      title: 'Đang xử lý hoàn tiền...',
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      }
    });

    // Gọi API hoàn tiền
    await client.post(`/api/hoa-don/hoan-tien/${hoaDon.value.id}`, null, {
      params: {
        soTienHoan,
        ghiChu: ghiChuHoanTien,
        tenNhanVien: currentUser
      }
    });

    // Đóng loading
    Swal.close();

    // Cập nhật giao diện
    hoaDon.value.tongTienThua = 0;

    // Thông báo thành công
    await Swal.fire({
      title: 'Hoàn tiền thành công!',
      html: `
        <div class="text-center">
          <p>Đã hoàn trả <strong class="text-success">${formattedAmount}</strong> cho khách hàng</p>
          <i class="bi bi-check-circle-fill text-success" style="font-size: 3rem;"></i>
        </div>
      `,
      icon: 'success',
      timer: 2000,
      showConfirmButton: false
    });

    // Tải lại dữ liệu hóa đơn
    await fetchHoaDonDetail();

  } catch (error) {
    console.error("Lỗi khi hoàn tiền:", error);

    // Đóng loading nếu còn
    Swal.close();

    // Thông báo lỗi
    await Swal.fire({
      title: 'Hoàn tiền thất bại!',
      html: `
        <div class="text-start">
          <p>Lỗi khi hoàn tiền:</p>
          <p class="text-danger">${error.response?.data?.message || 'Vui lòng thử lại sau'}</p>
        </div>
      `,
      icon: 'error',
      confirmButtonText: 'Đóng'
    });
  }
};


const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};
// import dayjs from 'dayjs';

// const formatDate = (date) => {
//   return date ? dayjs(date).format('DD/MM/YYYY HH:mm') : '';
// };


// Helper function để parse format dd/MM/yyyy HH:mm
const parseCustomDateTime = (timeString) => {
  if (!timeString) return null;
  
  try {
    // Nếu đã là Date object, trả về luôn
    if (timeString instanceof Date) {
      return timeString;
    }
    
    // Nếu là string có format dd/MM/yyyy HH:mm
    if (typeof timeString === 'string') {
      // Kiểm tra pattern dd/MM/yyyy HH:mm
      const pattern = /^(\d{1,2})\/(\d{1,2})\/(\d{4})\s+(\d{1,2}):(\d{1,2})$/;
      const match = timeString.match(pattern);
      
      if (match) {
        const [, day, month, year, hour, minute] = match;
        // Tạo Date object với timezone Việt Nam
        const date = new Date(parseInt(year), parseInt(month) - 1, parseInt(day), parseInt(hour), parseInt(minute));
        return date;
      }
      
      // Thử parse với Date constructor (cho các format khác)
      const date = new Date(timeString);
      if (!isNaN(date.getTime())) {
        return date;
      }
    }
    
    // Nếu là number (timestamp)
    if (typeof timeString === 'number') {
      const date = new Date(timeString);
      if (!isNaN(date.getTime())) {
        return date;
      }
    }
    
    return null;
  } catch (error) {
    console.warn('Lỗi parse thời gian:', error, 'Time value:', timeString);
    return null;
  }
};

// Format thời gian thông minh - hiển thị tương đối cho gần đây, tuyệt đối cho xa
const formatSmartDate = (date) => {
  if (!date) return '';
  
  try {
    const parsedDate = parseCustomDateTime(date);
    
    if (!parsedDate) {
      return '';
    }
    
    const now = new Date();
    const diffInMs = now.getTime() - parsedDate.getTime();
    const diffInMinutes = Math.floor(diffInMs / (1000 * 60));
    const diffInHours = Math.floor(diffInMs / (1000 * 60 * 60));
    const diffInDays = Math.floor(diffInMs / (1000 * 60 * 60 * 24));
    
    // Nếu thời gian cách đây quá 1 ngày, hiển thị ngày tháng năm
    if (diffInDays >= 1) {
      return parsedDate.toLocaleString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
      });
    }
    
    // Nếu thời gian cách đây quá 1 giờ, hiển thị số giờ
    if (diffInHours >= 1) {
      return `${diffInHours} giờ trước`;
    }
    
    // Nếu thời gian cách đây quá 1 phút, hiển thị số phút
    if (diffInMinutes >= 1) {
      return `${diffInMinutes} phút trước`;
    }
    
    // Nếu thời gian cách đây dưới 1 phút, hiển thị "Vừa xong"
    return 'Vừa xong';
  } catch (error) {
    console.warn('Lỗi format thời gian:', error, 'Date value:', date);
    return '';
  }
};

// Format thời gian đầy đủ (luôn hiển thị ngày giờ đầy đủ)
// const formatDate = (date) => {
//   if (!date) return '';
  
//   try {
//     const parsedDate = parseCustomDateTime(date);
    
//     if (!parsedDate) {
//       return '';
//     }
    
//     return parsedDate.toLocaleString('vi-VN', {
//       day: '2-digit',
//       month: '2-digit',
//       year: 'numeric',
//       hour: '2-digit',
//       minute: '2-digit',
//       hour12: false, // 24h format
//     });
//   } catch (error) {
//     console.warn('Lỗi format date:', error, 'Date value:', date);
//     return '';
//   }
// };





// Hàm lấy danh sách hình thức thanh toán
const fetchPaymentMethods = async () => {
  try {
    const response = await client.get('/api/httt/all');
    paymentMethods.value = response.data;
  } catch (error) {
    console.error('Lỗi khi tải danh sách hình thức thanh toán:', error);
  }
};


onMounted(() => {
  fetchHoaDonDetail();
  fetchPaymentMethods(); // Thêm gọi API lấy danh sách hình thức thanh toán
});
</script>




<template>
  <div class="invoice-detail-container">
    <!-- Toast notifications sẽ được hiển thị tự động bởi vue-toastification -->

    <!-- Header với thông tin đơn hàng -->
    <div class="invoice-header-bg">
      <div class="container">
        <div class="invoice-header-content">
          <div class="header-text">
            <h1 class="invoice-title">
              <i class="fas fa-file-invoice"></i> Chi tiết hóa đơn #{{ hoaDon.maHoaDon }}
            </h1>
            <div class="invoice-meta">
              <span class="badge" :class="getStatusClass(hoaDon.trangThai)">
                {{ getStatusText(hoaDon.trangThai) }}
              </span>
              <span class="date">
                <i class="fas fa-calendar-alt"></i> {{ formatSmartDate(hoaDon.ngayTao) }}
              </span>
            </div>
          </div>
          <div class="header-actions">
            <button @click="printInvoice" class="btn btn-light">
              <i class="fas fa-print"></i> In
            </button>
            <button @click="downloadPdf" class="btn btn-light">
              <i class="fas fa-file-pdf"></i> PDF
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Thanh tiến trình nâng cấp -->
    <OrderProgressTracker
      :current-status="hoaDon.trangThai || 0"
      :order-type="hoaDon.loaiHoaDon || 0"
      :times="orderTimes"
      :has-shipping-history="daQuaTrangThaiGiaoHang"
    />

    <!-- Action buttons -->
    <div class="action-buttons">
      <button v-if="getNextStatus !== null && hoaDon.trangThai < 5 && hoaDon.trangThai < 7" @click="changeStatus" class="btn btn-primary">
        <i class="fas fa-check-circle"></i> {{ getStatusText(getNextStatus) }}
      </button>

      <button
        v-if="hoaDon.trangThai > 0 && hoaDon.trangThai < 5 && !(hoaDon.loaiHoaDon === 0 && hoaDon.trangThai === 5) && hoaDon.trangThai < 7"
        @click="undoStatus" class="btn btn-secondary">
        <i class="fas fa-undo"></i> Hoàn tác
      </button>

      <!-- Hủy đơn chỉ khi trạng thái 0,1,2 và không phải đã hủy/trả hàng -->
      <button v-if="[0, 1, 2].includes(hoaDon.trangThai) && hoaDon.trangThai < 7" @click="cancelOrReturn('cancel')" class="btn btn-danger">
        <i class="fas fa-times-circle"></i> Hủy đơn
      </button>

      <!-- Trả hàng chỉ khi trạng thái hoàn thành (5), không phải đã hủy/trả hàng -->
      <button v-if="hoaDon.trangThai === 5" @click="cancelOrReturn('return')" class="btn btn-danger">
        <i class="fas fa-undo-alt"></i> Trả hàng
      </button>

      <!-- Đang trả hàng: cho phép xác nhận thành công hoặc từ chối -->
      <button v-if="hoaDon.trangThai === 8" @click="cancelOrReturn('acceptReturn')" class="btn btn-success">
        <i class="fas fa-check"></i> Xác nhận trả hàng thành công
      </button>
      <button v-if="hoaDon.trangThai === 8" @click="cancelOrReturn('rejectReturn')" class="btn btn-warning">
        <i class="fas fa-times"></i> Từ chối trả hàng
      </button>

      <!-- Thêm nút chuyển sang Đang trả hàng khi trạng thái là 7 -->
      <button v-if="hoaDon.trangThai === 7" @click="cancelOrReturn('processingReturn')" class="btn btn-info">
        <i class="fas fa-spinner"></i> Đang trả hàng
      </button>

      <button v-if="hoaDon.trangThai === 3 && hoaDon.loaiHoaDon === 1 && !hoaDon.thanhToanTruoc" @click="processPayment"
        class="btn btn-success">
        <i class="fas fa-money-bill-wave"></i> Thanh toán
      </button>

      <button v-if="hoaDon.trangThai === 4 && hoaDon.tongTienThua > 0" @click="refundExcess" class="btn btn-warning">
        <i class="fas fa-exchange-alt"></i> Hoàn tiền {{ formatCurrency(hoaDon.tongTienThua) }}
      </button>

      <button v-if="hoaDon.loaiHoaDon === 0 && hoaDon.trangThai === 1" @click="chuyenDenGiaoHang" class="btn btn-info">
        <i class="fas fa-truck"></i> Chuyển giao hàng
      </button>

      <button @click="showLichSu = true" class="btn btn-outline">
        <i class="fas fa-history"></i> Lịch sử
      </button>
    </div>

    <!-- Main content -->
    <div class="invoice-content">
      <!-- Customer info -->
      <div class="card customer-card">
        <div class="card-header">
          <h3><i class="fas fa-user"></i> Thông tin khách hàng</h3>
        </div>
        <div class="card-body">
          <div class="customer-grid">
            <div class="customer-field">
              <label>Tên khách hàng</label>
              <!-- <p class="text-warning">{{ hoaDon.khachHang?.tenKhachHang || 'Khách lẻ' }}</p> -->
              <p class="text-warning">{{ hoaDon.tenKhachHang || 'Khách lẻ' }}</p>
            </div>
            <div class="customer-field">
              <label>Số điện thoại</label>
              <!-- <p>{{ hoaDon.khachHang?.sdt || 'N/A' }}</p> -->
              <p>{{ hoaDon.sdt || 'N/A' }}</p>
            </div>
            <div class="customer-field">
              <label>Email</label>
              <p>{{ hoaDon.khachHang?.email || 'N/A' }}</p>
            </div>
            <div class="customer-field">
              <label>Địa chỉ</label>
              <!-- <p>{{ formatAddress(hoaDon.khachHang)|| hoaDon.diaChi  }}</p> -->
              <p>{{ hoaDon.diaChi || formatAddress(hoaDon.khachHang)}}</p>
            </div>
            <div class="customer-field">
              <label>Hình thức nhận hàng</label>
              <p>{{ hoaDon.hinhThucNhanHang === 0 ? "Tại quầy":"Giao hàng" || 'N/A' }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Order summary -->
      <div class="card summary-card">
        <div class="card-header">
          <h3><i class="fas fa-receipt"></i> Tóm tắt đơn hàng</h3>
        </div>
        <div class="card-body">
          <div class="summary-grid">
            <div class="summary-item">
              <label>Mã hóa đơn</label>
              <p class="text-success">{{ hoaDon.maHoaDon }}</p>
            </div>
            <div class="summary-item">
              <label>Ngày tạo</label>
              <p>{{ formatSmartDate(hoaDon.ngayTao) }}</p>
            </div>
            <div class="summary-item">
              <label>Phí vận chuyển</label>
              <p>{{ formatCurrency(hoaDon.phiVanChuyen) }}</p>
            </div>
            <div class="summary-item">
              <label>Giảm giá</label>
              <p>{{ formatCurrency(hoaDon.soTienGiam) }}</p>
            </div>
            <div class="summary-item">
              <label>Tổng tiền</label>
              <p class="text-bold">{{ formatCurrency(hoaDon.tongTienSauKhiGiam ) }}</p>
                      <!-- <p class="text-success">{{ formatCurrency(hoaDon.soTienKhachHangThanhToan || 0) }}</p> -->
            </div>
            <div class="summary-item">
              <label>Đã thanh toán</label>
              <p class="text-success">{{ formatCurrency(hoaDon.soTienKhachHangThanhToan || 0) }}</p>
                <!-- <p class="text-success">{{ formatCurrency(hoaDon.tongTien) }}</p> -->
            </div>
            <div class="summary-item" v-if="hoaDon.tongTienThua > 0">
              <label>Tiền thừa cần hoàn</label>
              <p class="text-danger">{{ formatCurrency(hoaDon.tongTienThua) }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Products list -->
      <div class="card products-card">
        <div class="card-header">
          <h3><i class="fas fa-box-open"></i> Sản phẩm</h3>
        </div>
        <div class="card-body">
          <div class="product-list-grid">
            <div v-for="(item, index) in listChiTietSanPham" :key="index" class="product-item-grid">
              <div class="product-image">
                <img :src="item.tenAnh" :alt="item.tenChiTietSp" />
              </div>
              <div class="product-info">
                <h4>{{ item.tenChiTietSp }}</h4>
                <div class="product-meta-grid">
                  <span class="sku"><i class="fas fa-barcode"></i> {{ item.maCTSP }}</span>
                  <span class="name"><i class="fas fa-tag"></i> {{ item.tenCTSP }}</span>
                </div>
                <div class="product-meta-grid">
                  <span class="price"><i class="fas fa-money-bill"></i> {{ formatCurrency(item.donGia) }}</span>
                  <span class="quantity"><i class="fas fa-cubes"></i> x {{ item.soLuong }}</span>
                  <span class="total"><i class="fas fa-calculator"></i> {{ formatCurrency(item.donGia * item.soLuong) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Payment history -->
      <div class="card payment-card">
        <div class="card-header">
          <h3><i class="fas fa-credit-card"></i> Lịch sử thanh toán</h3>
        </div>
        <div class="card-body">
          <div v-if="listLichSuThanhToan.length > 0" class="payment-history">
            <div v-for="(item, index) in listLichSuThanhToan" :key="index" class="payment-item">
              <div class="payment-meta">
                <span class="payment-number">#{{ index + 1 }}</span>
                <span class="payment-date">{{ formatSmartDate(item.ngayCapNhat || item.ngayThanhToan) }}</span>
              </div>
              <div class="payment-details">
                <span class="payment-amount">{{ formatCurrency(item.soTienThanhToan || item.tongTien) }}</span>
                <span class="payment-method">{{ item.tenHinhThuc || 'Tiền mặt' }}</span>
                <span class="payment-status" :class="item.trangThai ? 'success' : 'danger'">
                  {{ item.trangThai ? 'Đã thanh toán' : 'Chưa thanh toán' }}
                </span>
              </div>
              <div class="payment-staff">
                <i class="fas fa-user"></i> {{ item.tenNhanVien }}
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            <i class="fas fa-info-circle"></i>
            <p>Không có lịch sử thanh toán</p>
          </div>
        </div>
      </div>
    </div>

    <!-- QR Code section -->
    <div class="qr-section">
      <div class="qr-code">
        <img :src="qrCodeUrl" alt="QR Code" />
        <p>Quét mã để xem hóa đơn</p>
      </div>
    </div>

    <!-- Status history modal -->
    <div v-if="showLichSu" class="modal-overlay" @click.self="showLichSu = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>Lịch sử trạng thái</h3>
          <button @click="showLichSu = false" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="timeline">
            <div v-for="(item, index) in listLichSuHoaDonDto" :key="index" class="timeline-item">
              <div class="timeline-marker" :class="getStatusClass(item.trangThai)"></div>
              <div class="timeline-content">
                <div class="timeline-header">
                  <h4>{{ getStatusText(item.trangThai) }}</h4>
                  <span class="timeline-date">{{ formatSmartDate(item.ngayCapNhat) }}</span>
                </div>
                <p v-if="item.ghiChu" class="timeline-note">{{ item.ghiChu }}</p>
                <div class="timeline-footer">
                  <span class="staff"><i class="fas fa-user"></i> {{ item.tenNhanVien }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Danh sách sản phẩm -->
    <div class="card mb-3">

      <!-- <div class="card-body">
        <table class="table table-bordered">
          <thead>
            <tr>
              <th><strong>#</strong></th>
              <th><strong>Ảnh</strong></th>
              <th><strong>Sản phẩm</strong></th>
              <th><strong>Số lượng</strong></th>
              <th><strong>Tổng tiền</strong></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(ctspdto, index) in listChiTietSanPham" :key="index">
              <td>{{ index + 1 }}</td>
              <td>
                <img :src="ctspdto.tenAnh" alt="Ảnh sản phẩm" width="50" height="50">
              </td>
              <td>
                <h5>{{ ctspdto.tenChiTietSp }}</h5>
                <p>Mã SP: <span class="text-primary">{{ ctspdto.maCTSP }}</span></p>
                <p>Đơn giá: {{ formatCurrency(ctspdto.donGia) }}</p>
              </td>
              <td>
                <div class="product-actions d-flex align-items-center">
                  <span class="quantity-value mx-3">{{ ctspdto.soLuong }}</span>
                </div>
              </td>
              <td>{{ formatCurrency(ctspdto.donGia * ctspdto.soLuong) }}</td>

            </tr>
          </tbody>
        </table>
      </div> -->
    </div>
  </div>
  <div id="print-area" class="print-area">
    <div class="text-center mb-4">
      <h2 class="mb-1">HAVEN POLO</h2>
      <p class="mb-0">Địa chỉ cửa hàng: 123 Đường ABC, Quận XYZ, Hà Nội</p>
      <p>SĐT: 0366 200 005</p>
      <h4 class="mb-0">HÓA ĐƠN BÁN HÀNG</h4>
    </div>

    <div class="d-flex justify-content-end align-items-center mb-3">
      <img :src="qrCodeUrl" alt="QR Code" width="100" height="100" />
    </div>

    <div class="row mb-4">
      <div class="col-6">
        <p><strong>Tên khách hàng:</strong> {{ hoaDon.khachHang?.tenKhachHang }}</p>
        <p><strong>SĐT khách hàng:</strong> {{ hoaDon.khachHang?.sdt }}</p>
      </div>
      <div class="col-6 text-end">
        <p><strong>Mã hóa đơn:</strong> {{ hoaDon.maHoaDon }}</p>
        <p><strong>Thời gian:</strong> {{ hoaDon.ngayTao }}</p>
      </div>
    </div>

    <h5 class="mb-3">Danh sách sản phẩm</h5>
    <table class="table table-bordered">
      <thead class="table-light text-center">
        <tr>
          <th>STT</th>
          <th>Tên SP</th>
          <th>Số lượng</th>
          <th>Đơn giá</th>
          <th>Thành tiền</th>
          <th>Trạng thái</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(ctspdto, index) in listChiTietSanPham" :key="index" class="text-center">
          <td>{{ index + 1 }}</td>
          <td class="text-start">{{ ctspdto.tenCTSP }}</td>
          <td>{{ ctspdto.soLuong }}</td>
          <td>{{ formatCurrency(ctspdto.donGia) }}</td>
          <td>{{ formatCurrency(ctspdto.soLuong * ctspdto.donGia) }}</td>
          <td>{{ ctspdto.trangThai === 1 ? 'Đã giao' : 'Chưa giao' }}</td>
        </tr>
      </tbody>
    </table>

    <div class="text-end mt-4">
      <p>Giảm giá: {{ formatCurrency(hoaDon.soTienGiam) }}</p>
      <p>Phí vận chuyển: {{ formatCurrency(hoaDon.phiVanChuyen) }}</p>
      <p><strong>Tổng tiền: {{ formatCurrency(hoaDon.tongTienSauKhiGiam) }}</strong></p>
      <p><em>Cảm ơn quý khách đã mua hàng!</em></p>

    </div>
  </div>
</template>


<style scoped>
/* Thêm style cho toast */
.Vue-Toastification__toast {
  font-family: inherit;
  border-radius: 8px;
  padding: 12px 20px;
}

.Vue-Toastification__toast--success {
  background-color: #4caf50;
}

.Vue-Toastification__toast--error {
  background-color: #f44336;
}

.Vue-Toastification__toast--warning {
  background-color: #ff9800;
}

.swal2-popup {
  width: 500px;
}

.swal2-textarea {
  width: 100%;
  min-height: 100px;
  resize: vertical;
}

.swal2-input,
.swal2-select,
.swal2-textarea {
  margin: 0.5em 0 1em;
}

.swal2-validation-message {
  text-align: left;
}


.text-danger {
  color: #dc3545;
  font-weight: bold;
}

.swal2-html-container {
  text-align: left !important;
}


.fw-bold {
  font-weight: bold;
}

.text-muted {
  color: #6c757d;
}

.small {
  font-size: 0.875em;
}

.timeline {
  position: relative;
  padding-left: 1rem;
  margin-left: 1rem;
  border-left: 2px solid #dee2e6;
}

.timeline-item {
  position: relative;
  padding-bottom: 1.5rem;
}

.timeline-point {
  position: absolute;
  left: -1.5rem;
  width: 1rem;
  height: 1rem;
  border-radius: 50%;
  background-color: #6c757d;
}

.timeline-item-success .timeline-point {
  background-color: #28a745;
}

.timeline-item-danger .timeline-point {
  background-color: #dc3545;
}

.timeline-content {
  padding: 0.5rem 1rem;
  background-color: #f8f9fa;
  border-radius: 0.25rem;
}

/* Ẩn phần hóa đơn in bình thường */
#print-area {
  display: none;
}

/* Khi in, chỉ hiển thị phần print-area, ẩn mọi thứ khác */
@media print {
  body * {
    visibility: hidden !important;
  }

  #print-area,
  #print-area * {
    visibility: visible !important;
  }

  #print-area {
    display: block !important;
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    background-color: white;
    padding: 20px;
    font-size: 14px;
  }

  /* Ẩn các nút, sidebar, modal... */
  .no-print,
  .btn,
  nav,
  aside,
  .card,
  .modal {
    display: none !important;
  }
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
  z-index: 1050;
}

.modal-box {
  background: #fff;
  border-radius: 8px;
  width: 600px;
  max-width: 90%;
  padding: 1rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.modal-header,
.modal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-body {
  padding-top: 1rem;
}

.btn-close {
  border: none;
  background: transparent;
  font-size: 1.5rem;
  cursor: pointer;
}

/* Tổng thể trang */
.page-container {
  background-color: #f0f2f5;
  /* Màu nền xám nhạt */
  padding: 24px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  color: #333;
}

/* Tiêu đề chính */
.main-title {
  text-align: center;
  font-size: 24px;
  font-weight: 600;
  color: #172b4d;
  margin-bottom: 24px;
  text-transform: uppercase;
}

/* Lưới thông tin tổng quan */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.summary-box {
  background-color: #ffffff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  text-align: center;
}

.summary-box .label {
  color: #6c757d;
  font-size: 14px;
  margin-bottom: 8px;
}

.summary-box .value {
  font-size: 1.2rem;
  font-weight: 600;
  color: #0052cc;
}

/* Khung nội dung chung */
.content-section {
  background-color: #ffffff;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #172b4d;
  margin: 0;
}

.btn-change-info {
  background-color: #007bff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s;
}

.btn-change-info:hover {
  background-color: #0056b3;
}


/* Thông tin khách hàng */
.customer-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-field {
  background-color: #fafbfc;
  padding: 12px 16px;
  border-radius: 6px;
  border: 1px solid #dfe1e6;
}

.info-field .label {
  font-size: 13px;
  color: #6c757d;
  margin-bottom: 4px;
}

.info-field .value {
  font-size: 16px;
  font-weight: 500;
  color: #172b4d;
  margin: 0;
}

/* Bảng lịch sử thanh toán */
.payment-table {
  width: 100%;
  border-collapse: collapse;
}

.payment-table thead th {
  background-color: #f8f9fa;
  text-align: left;
  padding: 12px 16px;
  color: #6c757d;
  font-weight: 600;
  font-size: 14px;
  border-bottom: 2px solid #dee2e6;
}

.payment-table tbody td {
  padding: 14px 16px;
  border-bottom: 1px solid #e9ecef;
  vertical-align: middle;
  font-size: 14px;
}

.payment-table tbody tr:last-child td {
  border-bottom: none;
}

/* Các nhãn trạng thái (Pills/Tags) */
.status-tag {
  display: inline-block;
  padding: 5px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.2;
}

.product-actions .quantity-btn {
  /* padding: 2px 6px; */
  font-size: 10px;
  line-height: 1;
  border-radius: 6px;
  margin-top: 13px;
  /* Giúp nút hạ xuống một chút */
}

.quantity-value {
  font-size: 16px;
  font-weight: 500;
}


.tag-transfer {
  background-color: #ffebe6;
  /* Màu hồng nhạt */
  color: #bf2600;
}

.tag-cash {
  background-color: #e3fcef;
  /* Màu xanh lá nhạt */
  color: #006644;
}

.tag-paid {
  background-color: #e6fcff;
  /* Màu xanh dương nhạt */
  color: #0052cc;
}

.tag-postpaid {
  /* Trả sau */
  background-color: #deebff;
  /* Màu tím/xanh dương nhạt */
  color: #0747a6;
}

/* =============================================================================== */
/* Base styles */
.invoice-detail-container {
  max-width: 1500px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  color: #333;
}

/* Products list: ensure each product is horizontal and consistent */
.product-list-grid {
  display: grid;
  grid-template-columns: 1fr; /* single column list that flows vertically; each item is horizontal */
  gap: 14px;
}

.product-item-grid {
  display: flex;
  flex-direction: row;
  gap: 16px;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #eef2f6;
}

.product-item-grid .product-image {
  width: 110px;
  height: 110px;
  flex: 0 0 110px;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fbfcfd;
  border: 1px solid #f0f0f0;
}

.product-item-grid .product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-item-grid .product-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
}

.product-item-grid h4 {
  margin: 0;
  font-size: 16px;
  color: #213547;
}

.product-meta-grid {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.product-meta-grid span {
  background: #f7f9fb;
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 13px;
  color: #495057;
}

@media (min-width: 900px) {
  .product-list-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* Header styles */
.invoice-header-container {
  margin-bottom: 30px;
}

.invoice-header-bg {
  /* background: linear-gradient(135deg, #4b6cb7 0%, #182848 100%); */
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  color: white;
  padding: 20px 0;
  border-radius: 8px 8px 0 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.invoice-header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.header-text {
  flex: 1;
  min-width: 300px;
}

.invoice-title {
  color: white;
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.invoice-title i {
  font-size: 28px;
}

.invoice-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  background-color: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(5px);
}

.date {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.btn-light {
  background-color: rgba(255, 255, 255, 0.9);
  color: #2c3e50;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-light:hover {
  background-color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.btn-light i {
  font-size: 16px;
}


/* Responsive */
@media (max-width: 768px) {
  .invoice-header-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .invoice-title {
    font-size: 20px;
  }
}



/* Action buttons */
.action-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 30px;
}

.btn {
  padding: 10px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.btn i {
  font-size: 14px;
}

.btn-primary {
  background-color: #3498db;
  color: white;
  border-color: #3498db;
}

.btn-primary:hover {
  background-color: #2980b9;
}

.btn-secondary {
  background-color: #95a5a6;
  color: white;
  border-color: #95a5a6;
}

.btn-secondary:hover {
  background-color: #7f8c8d;
}

.btn-danger {
  background-color: #e74c3c;
  color: white;
  border-color: #e74c3c;
}

.btn-danger:hover {
  background-color: #c0392b;
}

.btn-success {
  background-color: #2ecc71;
  color: white;
  border-color: #2ecc71;
}

.btn-success:hover {
  background-color: #27ae60;
}

.btn-warning {
  background-color: #f39c12;
  color: white;
  border-color: #f39c12;
}

.btn-warning:hover {
  background-color: #d35400;
}

.btn-info {
  background-color: #1abc9c;
  color: white;
  border-color: #1abc9c;
}

.btn-info:hover {
  background-color: #16a085;
}

.btn-outline {
  background-color: transparent;
  border: 1px solid #bdc3c7;
  color: #7f8c8d;
}

.btn-outline:hover {
  background-color: #f8f9fa;
}

/* Card styles */
.card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
  overflow: hidden;
}

.card-header {
  padding: 16px 20px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #eee;
}

.card-header h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-header h3 i {
  color: #7f8c8d;
}

.card-body {
  padding: 20px;
}

/* Customer info */
.customer-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.customer-field {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 6px;
}

.customer-field label {
  display: block;
  font-size: 13px;
  color: #7f8c8d;
  margin-bottom: 5px;
}

.customer-field p {
  margin: 0;
  font-size: 15px;
  font-weight: 500;
  color: #2c3e50;
}

/* Order summary */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.summary-item {
  padding: 12px;
  border: 1px solid #eee;
  border-radius: 6px;
}

.summary-item label {
  display: block;
  font-size: 13px;
  color: #7f8c8d;
  margin-bottom: 5px;
}

.summary-item p {
  margin: 0;
  font-size: 15px;
  color: #2c3e50;
}


.text-bold {
  font-weight: 600;
}

/* Products list */
.product-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.product-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 6px;
  transition: all 0.2s;
}

.product-item:hover {
  background-color: #f8f9fa;
}

.product-image {
  width: 90px;
  height: 90px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e0e0e0;
  background: #fff;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.product-info h4 {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  color: #2c3e50;
}

.product-meta-grid {
  display: flex;
  gap: 18px;
  align-items: center;
  flex-wrap: wrap;
}

.product-meta-grid .sku,
.product-meta-grid .name {
  font-size: 13px;
  color: #7f8c8d;
  background: #f0f4f8;
  border-radius: 6px;
  padding: 3px 10px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.product-meta-grid .price {
  font-weight: 600;
  color: #27ae60;
  background: #eafaf1;
  border-radius: 6px;
  padding: 3px 10px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.product-meta-grid .quantity {
  color: #2980b9;
  background: #eaf4fb;
  border-radius: 6px;
  padding: 3px 10px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.product-meta-grid .total {
  font-weight: 600;
  color: #e67e22;
  background: #fff6e3;
  border-radius: 6px;
  padding: 3px 10px;
  display: flex;
  align-items: center;
  gap: 5px;
}

/* Payment history */
.payment-history {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.payment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.payment-meta {
  display: flex;
  flex-direction: column;
}

.payment-number {
  font-size: 13px;
  color: #7f8c8d;
}

.payment-date {
  font-size: 14px;
  font-weight: 500;
}

.payment-details {
  display: flex;
  align-items: center;
  gap: 15px;
}

.payment-amount {
  font-weight: 600;
  color: #2c3e50;
}

.payment-method {
  padding: 3px 8px;
  background-color: #ecf0f1;
  border-radius: 4px;
  font-size: 13px;
}

.payment-status {
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
}

.payment-status.success {
  background-color: #d5f5e3;
  color: #27ae60;
}

.payment-status.danger {
  background-color: #fadbd8;
  color: #e74c3c;
}

.payment-staff {
  font-size: 13px;
  color: #7f8c8d;
  display: flex;
  align-items: center;
  gap: 5px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px;
  color: #95a5a6;
}

.empty-state i {
  font-size: 40px;
  margin-bottom: 10px;
}

.empty-state p {
  margin: 0;
  font-size: 15px;
}

/* QR section */
.qr-section {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.qr-code {
  text-align: center;
}

.qr-code img {
  width: 120px;
  height: 120px;
  border: 1px solid #eee;
  padding: 10px;
  background-color: white;
}

.qr-code p {
  margin-top: 10px;
  font-size: 14px;
  color: #7f8c8d;
}

/* Modal styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  width: 600px;
  max-width: 95%;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: #95a5a6;
  cursor: pointer;
  padding: 5px;
}

.close-btn:hover {
  color: #7f8c8d;
}

.modal-body {
  padding: 20px;
}

/* Timeline styles */
.timeline {
  position: relative;
  padding-left: 30px;
}

.timeline-item {
  position: relative;
  padding-bottom: 20px;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-marker {
  position: absolute;
  left: -30px;
  top: 0;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.timeline-content {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 6px;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.timeline-header h4 {
  margin: 0;
  font-size: 15px;
}

.timeline-date {
  font-size: 13px;
  color: #7f8c8d;
}

.timeline-note {
  margin: 0;
  font-size: 14px;
  color: #555;
}

.timeline-footer {
  margin-top: 8px;
  font-size: 13px;
  color: #7f8c8d;
}

.staff {
  display: flex;
  align-items: center;
  gap: 5px;
}

/* Status classes */
.text-warning {
  /* background-color: #fff3cd; */
  background-color: white;
  color: #856404;
}

.text-success {
  /* background-color: #d4edda; */
  background-color: white;
  color: #155724;
  font-weight: bold;
}

.text-info {
  background-color: #d1ecf1;
  color: #0c5460;
}

.text-danger {
  background-color: #f8d7da;
  color: #721c24;
}

.text-dark {
  background-color: #e2e3e5;
  color: #383d41;
}

/* Print styles */
#print-area {
  display: none;
}

@media print {
  body * {
    visibility: hidden;
  }

  #print-area,
  #print-area * {
    visibility: visible;
  }

  #print-area {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    padding: 20px;
    background-color: white;
  }
}

/* Responsive styles */
@media (max-width: 768px) {
  .invoice-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .customer-grid,
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
  }

  .btn {
    width: 100%;
    justify-content: center;
  }
}
</style>