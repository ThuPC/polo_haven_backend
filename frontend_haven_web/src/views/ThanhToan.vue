<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useCartStore } from "@/stores/cart";
import { useToast } from "vue-toastification";
import Swal from "sweetalert2";
import axios from "axios";
import {
  createInvoice,
  createForPayment,
  createPayment,
  orderStatusVnpay,
} from "@/services/BanHang";
import {
  getListVoucherPublic,
  getListVoucherPrivate,
} from "@/services/VoucherService";
import { jwtDecode } from "jwt-decode";
import { danhSachDiaChi } from "@/services/KhachHangService";
import VnpayModel from "./VnpayModel.vue";
import client from "@/utils/api.js";
import { ghnService } from "@/services/ghn.js";
const isCartLoading = ref(false);

const cartStore = useCartStore();
const toast = useToast();

// --- State được tổ chức lại
const fullName = ref("");
const tenNguoiNhan = ref("");
const phone = ref("");
const email = ref("");
const note = ref("");
const paymentType = ref("cod");

// Các state cho địa chỉ giao hàng hiện tại
const provinceId = ref(null);
const provinceName = ref("");
const districtId = ref(null);
const districtName = ref("");
const wardCode = ref("");
const wardName = ref("");
const address = ref(""); // Chỉ dùng cho số nhà/đường

// State cho đơn hàng & thanh toán
const shippingFee = ref(0);
const discountAmount = ref(0);
const selectedVoucherId = ref(null);

// State cho dữ liệu tải từ server
const vouchers = ref([]);
const savedAddresses = ref([]);
const selectedAddressId = ref(null);

// State cho việc chọn địa chỉ động
const provinces = ref([]);
const districts = ref([]);
const wards = ref([]);
const iisCartLoading = ref(true);

// State cho modal thêm địa chỉ mới
const showAddAddressModal = ref(false);
const newAddress = ref({
  name: "",
  phone: "",
  provinceId: null,
  districtId: null,
  wardCode: "",
  street: "",
});

// --- State mới cho VNPay ---
const showVnpayModal = ref(false);
const vnpayPaymentData = ref(null);
let pollingInterval = null;

const isLoading = ref(false);
const showConfirmModal = ref(false);

// Sử dụng backend GHN service thay vì gọi trực tiếp public API

let userId = null;
const token = localStorage.getItem("authToken");
if (token) {
  try {
    const decoded = jwtDecode(token);
    userId = decoded.userId;
    email.value = decoded.email || "";
    fullName.value = decoded.sub || "";
  } catch (e) {
    userId = null;
  }
}

const subTotal = computed(() => cartStore.selectedTotal);
const totalAmount = computed(() => {
  const finalTotal = subTotal.value + shippingFee.value - discountAmount.value;
  return finalTotal > 0 ? finalTotal : 0;
});
const selectedCartItems = computed(() => cartStore.selectedItems || []);

// === CÁC HÀM GỌI API ===
async function fetchProvincesGHN() {
  try {
    const response = await ghnService.getProvinces();
    provinces.value = response?.data || response || [];
  } catch (error) {
    toast.error("Không thể tải danh sách tỉnh/thành.");
  }
}
async function fetchDistrictsGHN(pId) {
  try {
    const response = await ghnService.getDistricts(pId);
    districts.value = response?.data || response || [];
  } catch (error) {
    toast.error("Không thể tải danh sách quận/huyện.");
  }
}
async function fetchWardsGHN(dId) {
  try {
    const response = await ghnService.getWards(dId);
    wards.value = response?.data || response || [];
  } catch (error) {
    toast.error("Không thể tải danh sách xã/phường.");
  }
}
async function calculateShippingFee(dId, wCode) {
  if (!dId || !wCode) {
    shippingFee.value = 0;
    return;
  }
  try {
    const products = (cartStore.selectedItems || []).map((i) => ({
      id: i.productId,
      soLuong: i.quantity,
    }));
    const payload = {
      address: {
        districtId: parseInt(dId),
        wardCode: String(wCode),
      },
      products,
    };
    const result = await ghnService.calculateShippingFee(payload);
    const data = result?.data || result || {};
    const totalFee = data.total ?? data.fee ?? 0;
    shippingFee.value = Number(totalFee) || 0;

    if (data.isFallback) {
      toast.warning(
          `API GHN tạm thời không khả dụng. Áp dụng phí mặc định: ${formatPrice(shippingFee.value)}`
      );
    } else if (data.isRegionalFee) {
      toast.info(
          `Áp dụng phí vận chuyển theo vùng: ${formatPrice(shippingFee.value)}`
      );
    }
  } catch (error) {
    shippingFee.value = 30000;
    // Có thể thông báo nhẹ để không làm phiền người dùng
    // toast.warning('Không thể tính phí vận chuyển, áp dụng phí tạm tính.');
  }
}
async function loadSavedAddresses() {
  if (!userId) return;
  try {
    const res = await danhSachDiaChi(userId);
    // Map the incoming data to the format expected by the template
    savedAddresses.value = res.data.map(addr => ({
      id: addr.id,
      tenNguoiNhan: addr.tenNguoiNhan, // Map 'tenNguoiNhan' to 'name'
      phone: addr.sdt,         // Map 'sdt' to 'phone'
      street: addr.soNha,      // Map 'soNha' to 'street'
      wardName: addr.xaPhuong, // Map 'xaPhuong' to 'wardName'
      districtName: addr.quanHuyen, // Map 'quanHuyen' to 'districtName'
      provinceName: addr.thanhPho, // Map 'thanhPho' to 'provinceName'
      isDefault: false,        // Assuming you don't have this field in the response, set a default or add if available
      // Add other fields if necessary, like 'districtId', 'provinceId', 'wardCode'
      districtId: addr.districtId,
      provinceId: addr.provinceId,
      wardCode: addr.wardCode,
    })) || [];

    // Nếu có địa chỉ, tự động chọn và fill thông tin
    if (savedAddresses.value.length > 0) {
      selectedAddressId.value = savedAddresses.value[0].id;
      onSelectAddress();
    }
    console.log("Địa chỉ đã lưu:", savedAddresses.value);
  } catch (e) {
    console.error("Lỗi khi tải địa chỉ đã lưu:", e);
    savedAddresses.value = [];
  }
}

// async function fetchVouchers() {
//   try {
//     console.log("Fetching vouchers...");
//     // 1. Luôn luôn gọi API để lấy danh sách voucher công khai
//     const publicVouchersPromise = getListVoucherPublic();

//     // 2. Chỉ gọi API lấy voucher cá nhân NẾU người dùng đã đăng nhập
//     const privateVouchersPromise = userId ? getListVoucherPrivate(userId) : Promise.resolve({ data: [] });
//     // Promise.resolve({ data: [] }) tạo ra một promise "giả" ngay lập tức trả về một mảng rỗng,
//     // để Promise.all không bị lỗi khi người dùng chưa đăng nhập.

//     // 3. Chờ cả hai promise hoàn thành
//     const [publicVouchers, privateVouchers] = await Promise.all([
//       publicVouchersPromise,
//       privateVouchersPromise
//     ]);

//     // 4. Gộp kết quả lại (logic này vẫn giữ nguyên)
//     const allVouchers = [...(publicVouchers.data || []), ...(privateVouchers.data || [])];
//     const uniqueVouchers = new Map();

//     // Dùng mã giảm giá làm key để loại bỏ các voucher trùng lặp (nếu có)
//     allVouchers.forEach(v => {
//       if (v && v.maPhieuGiamGia) { // Đảm bảo voucher hợp lệ
//         uniqueVouchers.set(v.maPhieuGiamGia, v);
//       }
//     });

//     vouchers.value = Array.from(uniqueVouchers.values());
//     console.log("Danh sách voucher cuối cùng để hiển thị:", vouchers.value);

//   } catch (error) {
//     console.error("Lỗi khi tải danh sách voucher:", error);
//     toast.error("Không thể tải danh sách voucher.");
//   }
// }

// Trong file ThanhToan.vue

async function fetchVouchers() {
  try {
    const publicVouchersPromise = getListVoucherPublic();
    const privateVouchersPromise = userId
        ? getListVoucherPrivate(userId)
        : Promise.resolve({ data: [] });

    const [publicVouchers, privateVouchers] = await Promise.all([
      publicVouchersPromise,
      privateVouchersPromise,
    ]);

    const allVouchers = [
      ...(publicVouchers.data || []),
      ...(privateVouchers.data || []),
    ];
    const uniqueVouchersByName = new Map();

    allVouchers.forEach((v) => {
      if (v && v.tenPhieuGiamGia) {
        uniqueVouchersByName.set(v.tenPhieuGiamGia, v);
      }
    });
    vouchers.value = Array.from(uniqueVouchersByName.values());
  } catch (error) {
    toast.error("Không thể tải danh sách voucher.");
  }
}

function applyVoucher() {
  if (!selectedVoucherId.value) {
    discountAmount.value = 0;
    return;
  }
  const voucher = vouchers.value.find((v) => v.id === selectedVoucherId.value);
  if (!voucher) {
    toast.error("Voucher không hợp lệ!");
    return;
  }
  if (subTotal.value < voucher.giaTriToiThieu) {
    toast.error(
        `Đơn hàng phải đạt tối thiểu ${formatPrice(
            voucher.giaTriToiThieu
        )} để dùng voucher này.`
    );
    selectedVoucherId.value = null;
    discountAmount.value = 0;
    return;
  }
  let discount = 0;
  if (voucher.loaiGiamGia === 0) {
    discount = (subTotal.value * voucher.giaTriGiam) / 100;
    if (discount > voucher.giaTriToiDa) {
      discount = voucher.giaTriToiDa;
    }
  } else {
    discount = voucher.giaTriGiam;
  }
  discountAmount.value = discount;
  toast.success("Áp dụng voucher thành công!");
}

// Gợi ý phiếu giảm giá tốt nhất dựa trên tổng tiền hàng
function suggestBestVoucher() {
  if (!vouchers.value || vouchers.value.length === 0) return null;

  const productsTotal = subTotal.value || 0;
  let bestVoucher = null;
  let maxDiscount = 0;

  vouchers.value.forEach((voucher) => {
    if (!voucher) return;
    if (productsTotal < voucher.giaTriToiThieu) return;

    let discount = 0;
    if (voucher.loaiGiamGia === 0) {
      // Giảm theo %
      discount = (productsTotal * voucher.giaTriGiam) / 100;
      if (voucher.giaTriToiDa && discount > voucher.giaTriToiDa) {
        discount = voucher.giaTriToiDa;
      }
    } else {
      // Giảm theo số tiền cố định
      discount = voucher.giaTriGiam || 0;
    }

    if (discount > maxDiscount) {
      maxDiscount = discount;
      bestVoucher = voucher;
    }
  });

  return bestVoucher;
}

async function handleAddNewAddress() {
  // Validate form
  if (
      !newAddress.value.name ||
      !newAddress.value.phone ||
      !newAddress.value.provinceId ||
      !newAddress.value.districtId ||
      !newAddress.value.wardCode ||
      !newAddress.value.street
  ) {
    toast.error("Vui lòng điền đầy đủ thông tin địa chỉ mới.");
    return;
  }

  // Lấy tên từ ID/Code đã chọn
  const provinceObj = provinces.value.find(
      (p) => p.ProvinceID == newAddress.value.provinceId
  );
  const districtObj = districts.value.find(
      (d) => d.DistrictID == newAddress.value.districtId
  );
  const wardObj = wards.value.find(
      (w) => w.WardCode == newAddress.value.wardCode
  );

  const addressPayload = {
    provinceId: newAddress.value.provinceId,
    districtId: newAddress.value.districtId,
    wardCode: newAddress.value.wardCode,
    thanhPho: provinceObj ? provinceObj.ProvinceName : "",
    quanHuyen: districtObj ? districtObj.DistrictName : "",
    xaPhuong: wardObj ? wardObj.WardName : "",
    soNha: newAddress.value.street,
    diaChiChiTiet: newAddress.value.street,
    tenNguoiNhan: newAddress.value.name,
    sdt: newAddress.value.phone,
    isDefault: false,
    trangThai: 1,
  };

  try {
    if (!userId) {
      toast.error("Không xác định được người dùng.");
      return;
    }

    console.log(
        "[DEBUG] Adding new address with payload:",
        JSON.stringify(addressPayload, null, 2)
    );
    // Sử dụng client giống Addresses.vue
    await client.post(`/api/customers/${userId}/addresses`, addressPayload);

    toast.success("Thêm địa chỉ mới thành công!");
    showAddAddressModal.value = false;
    // Reset form
    Object.assign(newAddress.value, {
      name: "",
      phone: "",
      provinceId: null,
      districtId: null,
      wardCode: "",
      street: "",
    });

    // Tải lại danh sách địa chỉ đã lưu
    await loadSavedAddresses();
  } catch (error) {
    console.error("[DEBUG] Error adding new address:", error);
    if (error.response) {
      console.error("[DEBUG] error.response.data:", error.response.data);
      toast.error(
          `Thêm địa chỉ thất bại! ${error.response.data?.message || ""}`
      );
    } else {
      toast.error("Thêm địa chỉ thất bại! Không nhận được phản hồi từ server.");
    }
  }
}
// === LOGIC CHÍNH CỦA COMPONENT ===
function validateForm() {
  if (
      !tenNguoiNhan.value.trim() ||
      !phone.value.trim() ||
      !provinceId.value ||
      !districtId.value ||
      !wardCode.value ||
      !address.value.trim()
  ) {
    toast.error(
        "Vui lòng điền đầy đủ thông tin người nhận và địa chỉ giao hàng."
    );
    return false;
  }
  for (const item of cartStore.selectedItems) {
    if (item.quantity > item.soLuongTon) {
      toast.error(`Sản phẩm "${item.tenSanPham}" không đủ hàng`);
      return false;
    }
  }
  return true;
}

function buildOrderRequest() {
  return {
    idKhachHang: userId,
    ghiChuHoaDon: note.value,
    hinhThucNhanHang: 1,
    hinhThucThanhToan: paymentType.value === "cod" ? 0 : 1,
    loaiHoaDon: 1,
    addressId: selectedAddressId.value,
    emailNguoiNhan: email.value,
    diaChiNhanHang: {
      id: null,
      tenNguoiNhan: tenNguoiNhan.value,
      sdt: phone.value,
      provinceId: provinceId.value,
      thanhPho: provinceName.value,
      districtId: districtId.value,
      quanHuyen: districtName.value,
      wardCode: wardCode.value,
      xaPhuong: wardName.value,
      soNha: address.value,
    },
    phiShip: shippingFee.value,
    idPhieuGiamGia: selectedVoucherId.value,
    giaGoc: subTotal.value,
    giaGiam: discountAmount.value,
    tongTienSauKhiGiam: totalAmount.value,
    soTienKhachHangThanhToan: totalAmount.value,
    thanhToanTruoc: paymentType.value !== "cod",
    danhSachSanPham: cartStore.selectedItems.map((i) => ({
      id: i.productId,
      soLuong: i.quantity,
      giaGoc: i.originalPrice || i.price,
      giaSauKhiGiam: i.price,
    })),
  };
}

async function handleCodPayment() {
  if (!validateForm()) return;
  try {
    const req = buildOrderRequest();
    console.log("Dữ liệu gửi đi (Payload):", JSON.stringify(req, null, 2));
    const res = await createInvoice(req);
    toast.success(
        `Đặt hàng thành công! Mã đơn hàng của bạn là ${res.data.maHoaDon}`
    );
    await cartStore.removeSelectedItemsAfterPurchase();
    closeConfirmOrderModal();
    // router.push('/thank-you');
  } catch (err) {
    const errorMessage = err.response?.data?.message || "Đặt hàng thất bại!";
    console.error("Order error:", err);
    toast.error(errorMessage);
  }
}

function formatPrice(price) {
  if (price === undefined || price === null) return "0₫";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(price);
}

function handleOrder() {
  isLoading.value = true;

  if (!validateForm()) {
    isLoading.value = false;
    return;
  }

  // Bước 2: Dựa vào phương thức thanh toán đã chọn để gọi hàm tương ứng
  if (paymentType.value === "vnpay") {
    console.log("Người dùng chọn thanh toán VNPay. Bắt đầu luồng VNPay...");
    handleVnpayPayment();
  } else {
    console.log("Người dùng chọn thanh toán COD. Bắt đầu luồng COD...");
    handleCodPayment();
  }
}

// Modal confirm order
const closeConfirmOrderModal = () => {
  showConfirmModal.value = false; //
  isLoading.value = false;
};
const openConfirmOrderModal = () => {
  showConfirmModal.value = true;
};

const removeItemInCartCheckout = (cartItemId) => {
  Swal.fire({
    title: "Bạn chắc chắn muốn xóa?",
    text: "Sản phẩm sẽ bị xóa khỏi giỏ hàng của bạn!",
    icon: "warning",
    showCancelButton: true,
    confirmButtonColor: "#d33",
    cancelButtonColor: "#3085d6",
    confirmButtonText: "Đúng, xóa nó!",
    cancelButtonText: "Hủy",
  }).then((result) => {
    if (result.isConfirmed) {
      cartStore.removeFromCart(cartItemId);
    }
  });
};

const decreaseQuantity = (item) => {
  if (item.quantity > 1) {
    // Chỉ giảm và cập nhật nếu số lượng lớn hơn 1
    cartStore.updateQuantity(item.cartItemId, item.quantity - 1);
  } else {
    // Nếu số lượng là 1, hỏi để xóa
    removeItemInCartCheckout(item.cartItemId);
  }
};

/**
 * Tăng số lượng của một sản phẩm.
 * Kiểm tra với số lượng tồn kho trước khi tăng.
 * @param {object} item - Toàn bộ đối tượng sản phẩm trong giỏ hàng.
 */
const increaseQuantity = (item) => {
  if (item.quantity < item.soLuongTon) {
    // Chỉ tăng nếu số lượng hiện tại nhỏ hơn số lượng tồn
    cartStore.updateQuantity(item.cartItemId, item.quantity + 1);
  } else {
    // Thông báo nếu đã đạt giới hạn
    toast.warning("Số lượng sản phẩm đã đạt tối đa!");
  }
};

/**
 * Được gọi khi người dùng thay đổi trực tiếp số lượng trong ô input.
 * Hàm này sẽ validate giá trị nhập vào.
 * @param {object} item - Toàn bộ đối tượng sản phẩm trong giỏ hàng.
 */
const updateQuantity = (item) => {
  let finalQuantity = item.quantity;

  // Validate: Nếu số lượng không hợp lệ hoặc nhỏ hơn 1, đặt lại là 1
  if (!finalQuantity || finalQuantity < 1) {
    finalQuantity = 1;
    toast.error("Số lượng phải lớn hơn hoặc bằng 1.");
  }

  // Validate: Nếu số lượng lớn hơn tồn kho, đặt lại bằng số lượng tồn
  if (finalQuantity > item.soLuongTon) {
    finalQuantity = item.soLuongTon;
    toast.warning(`Chỉ còn ${item.soLuongTon} sản phẩm trong kho.`);
  }

  // Cập nhật lại giá trị trong giỏ hàng và gọi API
  item.quantity = finalQuantity;
  cartStore.updateQuantity(item.cartItemId, finalQuantity);
};

function stopPolling() {
  if (pollingInterval) {
    clearInterval(pollingInterval);
    pollingInterval = null;
  }
}

function closeVnpayModal() {
  showVnpayModal.value = false;
  vnpayPaymentData.value = null;
  stopPolling(); // Luôn dừng polling khi đóng modal
}

async function startPollingPaymentStatus(orderId, maHoaDon) {
  stopPolling();
  pollingInterval = setInterval(async () => {
    try {
      const statusResponse = await orderStatusVnpay(orderId);
      const orderStatus =
          statusResponse.data.status ?? statusResponse.data.trangThai;

      // Giả sử status 2 hoặc 5 là đã thanh toán thành công
      if (Number(orderStatus) === 5 || Number(orderStatus) === 2) {
        stopPolling();
        if (vnpayPaymentData.value) {
          vnpayPaymentData.value.isPaid = true;
        }

        // Đợi 3 giây để người dùng xem thông báo "Thanh toán thành công" trên modal
        setTimeout(async () => {
          closeVnpayModal();
          toast.success(`Đơn hàng ${maHoaDon} đã được thanh toán thành công!`);
          await cartStore.removeSelectedItemsAfterPurchase(); // Xóa sản phẩm đã mua khỏi giỏ hàng
          router.push("/cam-on"); // Chuyển hướng đến trang cảm ơn hoặc trang đơn hàng
        }, 3000);
      }
    } catch (error) {
      console.error("Lỗi khi polling trạng thái thanh toán:", error);
      stopPolling(); // Dừng lại nếu có lỗi
    }
  }, 4000); // Kiểm tra mỗi 4 giây
}

async function handleVnpayPayment() {
  const orderPayload = buildOrderRequest();

  // Cập nhật lại payload cho phù hợp với yêu cầu của API tạo hóa đơn chờ thanh toán
  orderPayload.hinhThucThanhToan = 2; // 2: Mã định danh cho VNPAY
  orderPayload.ghiChuHoaDon = "Khách hàng thanh toán bằng VNPay";
  // (Các trường khác đã được buildOrderRequest chuẩn bị)

  console.log(
      "Payload để tạo hóa đơn chờ VNPay:",
      JSON.stringify(orderPayload, null, 2)
  );

  try {
    console.log("Đang tạo hóa đơn chờ thanh toán VNPay...");
    const orderResponse = await createForPayment(orderPayload);
    const newOrder = orderResponse.data; // Backend cần trả về hóa đơn vừa tạo

    // Bước 2: Dùng thông tin hóa đơn để tạo link thanh toán VNPay
    const vnpayUrlPayload = {
      orderId: newOrder.idHoaDon,
      amount: newOrder.tongTienSauKhiGiam,
      orderInfo: `Thanh toan don hang ${newOrder.maHoaDon}`,
    };
    const paymentUrlResponse = await createPayment(vnpayUrlPayload);

    // Bước 3: Hiển thị Modal và bắt đầu kiểm tra trạng thái
    vnpayPaymentData.value = {
      url: paymentUrlResponse.data, // URL chứa mã QR
      amount: newOrder.tongTienSauKhiGiam,
      orderInfo: `Thanh toán cho đơn hàng ${newOrder.maHoaDon}`,
      isPaid: false,
    };
    showVnpayModal.value = true;
    startPollingPaymentStatus(newOrder.idHoaDon, newOrder.maHoaDon);
    closeConfirmOrderModal();
  } catch (error) {
    const errorMessage =
        error.response?.data?.message ||
        "Không thể tạo yêu cầu thanh toán VNPay. Vui lòng thử lại.";
    console.error("Lỗi khi tạo yêu cầu thanh toán VNPay:", error);
    toast.error(errorMessage);
  }
}

const openAddAddressModal = async () => {
  showAddAddressModal.value = true;
  await fetchProvincesGHN();
  console.log("[DEBUG] provinces after openAddAddressModal:", provinces.value);
  console.log("[DEBUG] districts after openAddAddressModal:", districts.value);
  console.log("[DEBUG] wards after openAddAddressModal:", wards.value);
};

// Hàm để đóng modal và reset form
const closeAddAddressModal = () => {
  showAddAddressModal.value = false;
  Object.assign(newAddress, {
    name: "",
    phone: "",
    provinceId: null,
    districtId: null,
    wardCode: "",
    street: "",
  });
  districts.value = [];
  wards.value = [];
};


async function onSelectAddress() {

  console.log("Selected address ID:", selectedAddressId.value);
  console.log("savedAddresses:", savedAddresses.value);
  const addr = savedAddresses.value.find(
      (a) => a.id === selectedAddressId.value
  );
  console.log('Địa chỉ vừa chọn:', addr);
  if (!addr) return;
  tenNguoiNhan.value = addr.tenNguoiNhan;
  phone.value = addr.phone;
  address.value = addr.street;
  provinceId.value = addr.provinceId;
  await fetchDistrictsGHN(addr.provinceId);
  districtId.value = addr.districtId;
  await fetchWardsGHN(addr.districtId);
  wardCode.value = addr.wardCode;
  calculateShippingFee(addr.districtId, addr.wardCode);
}

// watch(provinceId, async (newId, oldId) => {
//   if (newId === oldId || isSelectingAddress) return;

//   const selectedProvince = provinces.value.find((p) => p.ProvinceID == newId);
//   provinceName.value = selectedProvince ? selectedProvince.ProvinceName : "";

//   // Reset các cấp dưới
//   districts.value = [];
//   wards.value = [];
//   districtId.value = null;
//   wardCode.value = "";

//   if (newId) {
//     await fetchDistrictsGHN(newId);
//   }
// });

// Watcher cho việc chọn HUYỆN thủ công
watch(districtId, async (newId, oldId) => {
  if (newId === oldId ) return;

  const selectedDistrict = districts.value.find((d) => d.DistrictID == newId);
  districtName.value = selectedDistrict ? selectedDistrict.DistrictName : "";

  // Reset cấp dưới
  wards.value = [];
  wardCode.value = "";

  if (newId) {
    await fetchWardsGHN(newId);
  }
});

// Watcher cho việc chọn XÃ (hoạt động cho cả 2 trường hợp)
watch(wardCode, (newCode) => {
  const selectedWard = wards.value.find((w) => w.WardCode == newCode);
  wardName.value = selectedWard ? selectedWard.WardName : "";

  // Luôn tính lại phí ship khi xã thay đổi
  calculateShippingFee(districtId.value, newCode);
});

// === WATCHERS ĐỘC LẬP KHÁC ===

// Watcher cho Voucher
watch(selectedVoucherId, () => {
  applyVoucher();
});

// Tự động chọn voucher tốt nhất khi giỏ hàng hoặc danh sách voucher thay đổi
watch([
  () => cartStore.selectedItems,
  () => vouchers.value,
], () => {
  const best = suggestBestVoucher();
  if (best && selectedVoucherId.value !== best.id) {
    selectedVoucherId.value = best.id;
    // applyVoucher sẽ được gọi bởi watcher selectedVoucherId ở trên
  }
}, { deep: true });

// Watchers cho Modal "Thêm địa chỉ mới"
watch(
    () => newAddress.value.provinceId,
    (newId) => {
      districts.value = [];
      wards.value = [];
      newAddress.value.districtId = null;
      newAddress.value.wardCode = "";
      if (newId) fetchDistrictsGHN(newId);
    }
);

watch(
    () => newAddress.value.districtId,
    (newId) => {
      wards.value = [];
      newAddress.value.wardCode = "";
      if (newId) fetchWardsGHN(newId);
    }
);
watch(
    () => newAddress.provinceId,
    async (newVal, oldVal) => {
      if (newVal && newVal !== oldVal) {
        newAddress.districtId = null;
        newAddress.wardCode = "";
        districts.value = [];
        wards.value = [];
        await loadDistricts(newVal);
      } else if (!newVal) {
        newAddress.districtId = null;
        newAddress.wardCode = "";
        districts.value = [];
        wards.value = [];
      }
    }
);

watch(
    () => newAddress.districtId,
    async (newVal, oldVal) => {
      if (newVal && newVal !== oldVal) {
        newAddress.wardCode = "";
        wards.value = [];
        await loadWards(newVal);
      } else if (!newVal) {
        newAddress.wardCode = "";
        wards.value = [];
      }
    }
);
onMounted(() => {
  if (userId) {
    loadSavedAddresses();
  }
  fetchVouchers();
  fetchProvincesGHN();
});
</script>

<template>
  <div class="order-page">
    <!-- Header Section (Order Title) -->
    <section class="order-header py-4 bg-gradient text-white text-center">
      <div class="container">
        <h2 class="section-title fw-bold">
          <i class="fa-solid fa-bag-shopping"></i> ĐẶT HÀNG
        </h2>
      </div>
    </section>

    <!-- Order Content Section -->
    <section class="order-content py-5">
      <div class="container">
        <!-- Loading State -->
        <div
            v-if="isCartLoading"
            class="text-center p-4 bg-white rounded-lg shadow-lg"
        >
          <div class="spinner-border text-primary mb-3" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
          <h3 class="fs-5 fw-bold mb-3" style="color: #4ba27b">
            Đang tải giỏ hàng...
          </h3>
          <p class="text-muted">Vui lòng chờ trong giây lát</p>
        </div>

        <!-- Empty Order Message -->
        <div
            v-else-if="selectedCartItems.length === 0"
            class="text-center p-4 bg-white rounded-lg shadow-lg"
        >
          <div class="mb-4">
            <i
                class="fa-solid fa-shopping-cart"
                style="font-size: 4rem; color: #4ba27b"
            ></i>
          </div>
          <h3 class="fs-5 fw-bold mb-3" style="color: #4ba27b">
            Không có sản phẩm nào được chọn
          </h3>
          <p class="text-muted mb-4">
            Vui lòng quay lại giỏ hàng để chọn sản phẩm cần thanh toán!
          </p>
          <router-link to="/cart" class="btn btn-gradient me-2">
            <i class="fa-solid fa-arrow-left me-2"></i>Quay lại giỏ hàng
          </router-link>
          <router-link to="/products" class="btn btn-outline-secondary">
            <i class="fa-solid fa-shopping-bag me-2"></i>Tiếp tục mua sắm
          </router-link>
        </div>

        <div v-else class="row g-4">
          <!-- Left Column (Shipping Address & User Info) -->
          <div class="col-12 col-lg-7">
            <div class="order-section p-4 bg-white rounded-lg shadow-lg mb-4">
              <h3 class="fs-5 fw-bold mb-4" style="color: #4ba27b">
                <i class="fa-solid fa-truck"></i> THÔNG TIN GIAO HÀNG
              </h3>

              <!-- SAVED ADDRESSES BLOCK -->
              <div
                  v-if="userId && savedAddresses.length > 0"
                  class="mb-4 p-3 bg-light rounded"
              >
                <h4 class="fs-6 fw-bold mb-3">Chọn địa chỉ đã lưu</h4>
                <div class="list-group">
                  <label
                      v-for="addr in savedAddresses"
                      :key="addr.id"
                      class="list-group-item list-group-item-action d-flex justify-content-between align-items-center"
                  >
                    <div class="d-flex align-items-center">
                      <input
                          class="form-check-input me-3"
                          type="radio"
                          name="savedAddress"
                          :value="addr.id"
                          v-model="selectedAddressId"
                          @change="onSelectAddress"
                      />
                      <span class="d-block">
                        <strong>{{ addr.tenNguoiNhan }}</strong> - {{ addr.phone
                        }}<br />
                        <small class="text-muted"
                        >{{ addr.street }}, {{ addr.wardName }},
                          {{ addr.districtName }},
                          {{ addr.provinceName }}</small
                        >
                      </span>
                    </div>
                    <span
                        v-if="addr.isDefault"
                        class="badge bg-success rounded-pill"
                    >Mặc định</span
                    >
                  </label>
                </div>
                <div class="mt-3">
                  <button
                      class="btn btn-sm btn-outline-primary"
                      @click="showAddAddressModal = true"
                  >
                    <i class="fa-solid fa-plus me-1"></i> Thêm địa chỉ mới
                  </button>
                </div>
              </div>
              <p v-if="userId" class="text-center text-muted my-3 small"></p>


              <!-- Main Form -->
              <form @submit.prevent>
                <fieldset class="mb-4 p-3 border-gradient rounded-lg">
                  <legend class="text-muted fw-bold">Thông tin người nhận</legend>
                  <div class="mb-3">
                    <label for="fullName" class="form-label text-muted">Họ tên <span class="text-danger">*</span></label>
                    <input type="text" id="fullName" class="form-control" placeholder="Nhập họ và tên" v-model="tenNguoiNhan" />
                  </div>
                  <div class="row">
                    <div class="col-md-6 mb-3">
                      <label for="phone" class="form-label text-muted">SĐT <span class="text-danger">*</span></label>
                      <input type="tel" id="phone" class="form-control" placeholder="Nhập số điện thoại" v-model="phone" />
                    </div>
                    <div class="col-md-6 mb-3">
                      <label for="email" class="form-label text-muted">Email</label>
                      <input type="email" id="email" class="form-control" placeholder="Nhập email" v-model="email" />
                    </div>
                  </div>
                </fieldset>

                <fieldset  class="mb-4 p-3 border-gradient rounded-lg">
                  <legend class="text-muted fw-bold">Địa chỉ chi tiết</legend>
                  <div class="row g-3">
                    <div class="col-md-6">
                      <label for="province" class="form-label text-muted">Tỉnh/Thành phố <span class="text-danger">*</span></label>
                      <select id="province" class="form-select" v-model="provinceId" @change="fetchDistrictsGHN(provinceId)">
                        <option :value="null">-- Chọn Tỉnh/Thành --</option>
                        <option v-for="province in provinces" :key="province.ProvinceID" :value="province.ProvinceID">
                          {{ province.ProvinceName }}
                        </option>
                      </select>
                    </div>
                    <div class="col-md-6">
                      <label for="district" class="form-label text-muted">Quận/Huyện <span class="text-danger">*</span></label>
                      <select id="district" class="form-select" v-model="districtId" :disabled="!provinceId" @change="fetchWardsGHN(districtId)">
                        <option :value="null">-- Chọn Quận/Huyện --</option>
                        <option v-for="district in districts" :key="district.DistrictID" :value="district.DistrictID">
                          {{ district.DistrictName }}
                        </option>
                      </select>
                    </div>
                    <div class="col-md-6">
                      <label for="ward" class="form-label text-muted">Phường/Xã <span class="text-danger">*</span></label>
                      <select id="ward" class="form-select" v-model="wardCode" :disabled="!districtId">
                        <option value="">-- Chọn Phường/Xã --</option>
                        <option v-for="ward in wards" :key="ward.WardCode" :value="ward.WardCode">
                          {{ ward.WardName }}
                        </option>
                      </select>
                    </div>
                    <div class="col-md-6">
                      <label for="address" class="form-label text-muted">Số nhà, tên đường</label>
                      <input type="text" id="address" class="form-control" placeholder="Nhập địa chỉ chi tiết" v-model="address" />
                    </div>
                  </div>
                </fieldset>

                <fieldset class="p-3 border-gradient rounded-lg">
                  <legend class="text-muted fw-bold">Ghi chú bổ sung</legend>
                  <textarea
                      id="note"
                      class="form-control"
                      rows="3"
                      placeholder="Nhập ghi chú cho đơn hàng..."
                      v-model="note"
                  ></textarea>
                </fieldset>
              </form>
            </div>
          </div>

          <!-- Right Column (Cart & Order Summary) -->
          <div class="col-12 col-lg-5">
            <div>
              <!-- Cart Items Summary -->
              <div class="order-section p-4 bg-white rounded-lg shadow-lg mb-4">
                <!-- TIÊU ĐỀ GIỎ HÀNG -->
                <h3 class="fs-5 fw-bold mb-0" style="color: #4ba27b">
                  <i class="fa-solid fa-cart-shopping"></i> GIỎ HÀNG ({{
                    selectedCartItems.length
                  }})
                </h3>
                <hr />

                <!-- DANH SÁCH SẢN PHẨM TRONG GIỎ HÀNG -->
                <div
                    class="cart-items-container"
                    style="max-height: 450px; overflow-y: auto"
                >
                  <!-- Giao diện khi giỏ hàng trống -->
                  <div
                      v-if="selectedCartItems.length === 0"
                      class="text-center text-muted p-5"
                  >
                    <i class="fa-solid fa-cart-plus fa-3x mb-3"></i>
                    <p class="mb-0 fs-5">Giỏ hàng của bạn đang trống</p>
                    <small>Hãy thêm sản phẩm vào giỏ hàng nhé!</small>
                  </div>

                  <!-- Lặp qua từng sản phẩm -->
                  <div
                      v-for="item in selectedCartItems"
                      :key="item.id"
                      class="cart-item d-flex gap-3 py-3 border-bottom"
                  >
                    <!-- Cột Ảnh sản phẩm -->
                    <div class="item-image">
                      <img
                          :src="item.image"
                          :alt="item.tenSanPham"
                          class="img-fluid rounded"
                          style="width: 90px; height: 90px; object-fit: cover"
                      />
                    </div>

                    <!-- Cột Thông tin chi tiết sản phẩm -->
                    <div
                        class="item-details flex-grow-1 d-flex flex-column justify-content-between"
                    >
                      <!-- Hàng 1: Tên sản phẩm và nút xóa -->
                      <div
                          class="d-flex justify-content-between align-items-start"
                      >
                        <div>
                          <router-link
                              v-if="item.productViewId"
                              :to="`/product/${item.productViewId}`"
                              class="fw-bold text-dark text-decoration-none mb-1 d-block"
                          >{{ item.tenSanPham }}</router-link
                          >
                          <span
                              v-else
                              class="fw-bold text-dark mb-1 d-block"
                          >{{ item.tenSanPham }}</span>
                          <small class="text-muted d-block">Mã sản phẩm: {{ item.maSanPham || 'N/A' }}</small>
                          <small class="text-muted"
                          >Phân loại: {{ item.color }}, {{ item.size }}</small
                          >
                          <small
                              v-if="item.quantity >= item.soLuongTon"
                              class="d-block text-danger fw-bold mt-1"
                          >
                            Đã đạt số lượng tồn tối đa
                          </small>
                        </div>
                        <button
                            class="btn btn-sm btn-light p-0"
                            @click="removeItemInCartCheckout(item.cartItemId)"
                            title="Xóa sản phẩm"
                            style="width: 28px; height: 28px"
                        >
                          <i class="fa-solid fa-xmark"></i>
                        </button>
                      </div>

                      <!-- Hàng 2: Giá cả và bộ điều chỉnh số lượng -->
                      <div
                          class="d-flex justify-content-between align-items-center mt-2"
                      >
                        <!-- Giá gốc và giá khuyến mãi -->
                        <div class="item-price">
                          <span class="fw-bold fs-6" style="color: #4ba27b">{{
                              formatPrice(item.price)
                            }}</span>
                          <del
                              v-if="item.originalPrice > item.price"
                              class="text-muted small ms-2"
                          >{{ formatPrice(item.originalPrice) }}</del
                          >
                        </div>

                        <!-- Bộ điều chỉnh số lượng -->
                        <div
                            class="quantity-control d-flex align-items-center border rounded"
                        >
                          <button
                              class="btn btn-sm btn-light border-0"
                              @click="decreaseQuantity(item)"
                              style="width: 32px"
                          >
                            -
                          </button>
                          <input
                              type="number"
                              class="form-control form-control-sm text-center border-0"
                              v-model.number="item.quantity"
                              min="1"
                              style="
                              width: 45px;
                              box-shadow: none;
                              -moz-appearance: textfield;
                            "
                              @change="updateQuantity(item)"
                          />
                          <button
                              class="btn btn-sm btn-light border-0"
                              @click="increaseQuantity(item)"
                              style="width: 32px"
                          >
                            +
                          </button>
                        </div>

                        <!-- Tổng tiền của sản phẩm -->
                        <div
                            class="item-total fw-bold text-end"
                            style="width: 100px"
                        >
                          {{ formatPrice(item.price * item.quantity) }}
                        </div>
                      </div>
                    </div>
                  </div>

                </div>


              </div>

              <!-- Order Summary & Payment -->
              <div class="order-section p-4 bg-white rounded-lg shadow-lg">
                <h3 class="fs-5 fw-bold mb-4" style="color: #4ba27b">
                  <i class="fa-solid fa-money-check-dollar"></i> TỔNG KẾT ĐƠN
                  HÀNG
                </h3>
                <!-- Voucher Section -->
                <div class="mb-3">
                  <label for="voucher-select" class="form-label text-muted"
                  >Phiếu giảm giá</label
                  >
                  <select
                      id="voucher-select"
                      class="form-select"
                      v-model="selectedVoucherId"
                      :disabled="vouchers.length === 0"
                  >
                    <option :value="null">
                      {{
                        vouchers.length > 0
                            ? "-- Chọn voucher --"
                            : "Không có voucher nào hợp lệ"
                      }}
                    </option>
                    <option
                        v-for="voucher in vouchers"
                        :key="voucher.id"
                        :value="voucher.id"
                    >
                      {{ voucher.tenPhieuGiamGia }} - (Giảm
                      {{
                        voucher.loaiGiamGia === 1
                            ? formatPrice(voucher.giaTriGiam)
                            : `${voucher.giaTriGiam}%`
                      }})
                    </option>
                  </select>
                </div>
                <!-- Totals Calculation -->
                <div class="mb-3">
                  <div class="d-flex justify-content-between mb-2">
                    <span>Tạm tính:</span>
                    <span>{{ formatPrice(subTotal) }}</span>
                  </div>
                  <div class="d-flex justify-content-between mb-2">
                    <span>Phí vận chuyển:</span>
                    <span>+ {{ formatPrice(shippingFee) }}</span>
                  </div>
                  <div class="d-flex justify-content-between mb-2 text-danger">
                    <span>Giảm giá:</span>
                    <span>- {{ formatPrice(discountAmount) }}</span>
                  </div>
                  <hr />
                  <div class="d-flex justify-content-between fw-bold fs-5">
                    <span>Tổng thanh toán:</span>
                    <span class="text-danger">{{
                        formatPrice(totalAmount)
                      }}</span>
                  </div>
                </div>

                <!-- === PAYMENT METHOD (UPDATED) === -->
                <div class="mb-4">
                  <h4 class="fs-6 fw-bold mb-2" style="color: #4ba27b">
                    HÌNH THỨC THANH TOÁN
                  </h4>
                  <div class="form-check">
                    <input
                        type="radio"
                        id="cod"
                        name="payment"
                        class="form-check-input"
                        value="cod"
                        v-model="paymentType"
                        checked
                    />
                    <label for="cod" class="form-check-label text-muted"
                    >COD – Thanh toán khi nhận hàng</label
                    >
                  </div>
                  <div class="form-check">
                    <input
                        type="radio"
                        id="vnpay"
                        name="payment"
                        class="form-check-input"
                        value="vnpay"
                        v-model="paymentType"
                    />
                    <label
                        for="vnpay"
                        class="form-check-label text-muted d-flex align-items-center"
                    >
                      Thanh toán qua
                      <img
                          src="@/assets/img/logos/vnpay-logo.jpg"
                          alt="VNPay"
                          class="ms-2"
                          style="height: 20px"
                      />
                    </label>
                  </div>
                </div>
                <button
                    class="btn btn-success w-100 py-2 fw-semibold"
                    @click="openConfirmOrderModal"
                >
                  <i class="fa-solid fa-lock me-2"></i> ĐẶT HÀNG
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Modal xác nhận đặt hàng -->
    <div
        v-if="showConfirmModal"
        class="modal fade show custom-modal-fade"
        tabindex="-1"
        role="dialog"
        style="display: block"
        aria-modal="true"
    >
      <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content custom-modal-content">
          <div class="modal-header custom-modal-header">
            <h5 class="modal-title text-white">
              <i class="fas fa-check-circle me-2"></i> Xác nhận đặt hàng
            </h5>
            <button
                type="button"
                class="btn-close btn-close-white"
                @click="closeConfirmOrderModal"
                aria-label="Close"
            ></button>
          </div>
          <div class="modal-body custom-modal-body text-center">
            <p class="mb-4 lead">Bạn có chắc chắn muốn đặt đơn hàng này?</p>
            <div class="alert alert-info custom-alert-info p-3" role="alert">
              <h6 class="alert-heading">
                <i class="fas fa-info-circle me-2"></i>Thông tin quan trọng
              </h6>
              <p class="mb-0">
                Vui lòng kiểm tra kỹ các thông tin đơn hàng trước khi xác nhận.
              </p>
            </div>
          </div>
          <div
              class="modal-footer custom-modal-footer d-flex justify-content-center"
          >
            <button
                type="button"
                class="btn btn-outline-secondary custom-btn-outline"
                @click="closeConfirmOrderModal"
            >
              Hủy
            </button>
            <button
                type="button"
                class="btn btn-primary custom-btn-primary"
                @click="handleOrder"
                :disabled="isLoading"
            >
              <template v-if="isLoading">
                <span
                    class="spinner-border spinner-border-sm me-2"
                    role="status"
                    aria-hidden="true"
                ></span>
                Đang xử lý...
              </template>
              <template v-else>
                <i class="fas fa-paper-plane me-2"></i> Xác nhận và đặt hàng
              </template>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ADD NEW ADDRESS MODAL -->
    <div
        v-if="showAddAddressModal"
        class="modal fade show"
        style="display: block"
        tabindex="-1"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Thêm địa chỉ giao hàng mới</h5>
            <button
                type="button"
                class="btn-close"
                @click="closeAddAddressModal"
            ></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="handleAddNewAddress">
              <div class="mb-3">
                <label class="form-label">Họ tên người nhận</label>
                <input
                    type="text"
                    class="form-control"
                    v-model="newAddress.name"
                    placeholder="Tên người nhận"
                />
              </div>
              <div class="mb-3">
                <label class="form-label">Số điện thoại</label>
                <input
                    type="text"
                    class="form-control"
                    v-model="newAddress.phone"
                    placeholder="Số điện thoại người nhận"
                    maxlength="10"
                />
              </div>
              <div class="mb-3">
                <label class="form-label">Tỉnh/Thành phố</label>
                <select
                    class="form-select"
                    v-model="newAddress.provinceId"
                    :disabled="false"
                >
                  <option :value="null">-- Chọn tỉnh/thành --</option>
                  <option
                      v-for="p in provinces"
                      :key="p.ProvinceID"
                      :value="p.ProvinceID"
                  >
                    {{ p.ProvinceName }}
                  </option>
                </select>
              </div>
              <div class="mb-3">
                <label class="form-label">Quận/Huyện</label>
                <select
                    class="form-select"
                    v-model="newAddress.districtId"
                    :disabled="!newAddress.provinceId"
                >
                  <option :value="null">-- Chọn quận/huyện --</option>
                  <option
                      v-for="d in districts"
                      :key="d.DistrictID"
                      :value="d.DistrictID"
                  >
                    {{ d.DistrictName }}
                  </option>
                </select>
              </div>
              <div class="mb-3">
                <label class="form-label">Phường/Xã</label>
                <select
                    class="form-select"
                    v-model="newAddress.wardCode"
                    :disabled="!newAddress.districtId"
                >
                  <option value="">-- Chọn phường/xã --</option>
                  <option
                      v-for="w in wards"
                      :key="w.WardCode"
                      :value="w.WardCode"
                  >
                    {{ w.WardName }}
                  </option>
                </select>
              </div>
              <div class="mb-3">
                <label class="form-label">Số nhà, tên đường</label>
                <input
                    type="text"
                    class="form-control"
                    v-model="newAddress.street"
                    placeholder="Ví dụ: Số 123, Ngõ A, Đường B"
                />
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button
                type="button"
                class="btn btn-secondary"
                @click="closeAddAddressModal"
            >
              Hủy
            </button>
            <button
                type="button"
                class="btn btn-primary"
                @click="handleAddNewAddress"
            >
              Lưu địa chỉ
            </button>
          </div>
        </div>
      </div>
    </div>
    <!-- Backdrop for modal -->
    <div v-if="showAddAddressModal" class="modal-backdrop fade show"></div>
  </div>
  <VnpayModel
      v-if="showVnpayModal"
      :paymentData="vnpayPaymentData"
      @close="closeVnpayModal"
  />
</template>

<style scoped>
@import "animate.css";

.order-page {
  padding-top: 130px;
  background-color: #f5f7fa;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

/* Gradient Definition */
.bg-gradient {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
}

.border-gradient {
  border: 2px solid transparent;
  background: linear-gradient(white, white) padding-box,
  linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%) border-box;
}

/* Order Header */
.order-header {
  position: relative;
  height: auto;
  min-height: 120px;
  overflow: hidden;
  border-bottom: 4px solid #4ba27b;
}

.order-header .section-title {
  color: black;
  font-size: 28px;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.2);
}

/* Order Content */
.order-content .order-section,
.order-content .order-summary {
  border-radius: 15px;
}

.order-content .row {
  min-height: 100%;
}

.order-section {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.order-section .form-control,
.order-section .form-select,
.order-section .input-group .form-control {
  padding: 8px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.cart-table {
  border-collapse: separate;
  border-spacing: 0 15px;
  width: 100%;
  table-layout: auto;
}

.cart-table th,
.cart-table td {
  text-align: center;
  padding: 12px 8px;
  vertical-align: middle;
  background-color: #fff;
}

.cart-table th {
  background-color: #e9ecef;
  color: #333;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
  white-space: nowrap;
}

.cart-table td.fit-content {
  white-space: normal;
  overflow: visible;
  text-overflow: unset;
  max-width: 220px;
  word-break: break-word;
}

.cart-table td img {
  max-width: 90px;
  height: auto;
}

.cart-table td {
  font-size: 1rem;
}

.cart-table .cart-item td {
  vertical-align: middle;
}

.cart-table .fit-content {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 0;
}

.cart-table .cart-item img {
  transition: transform 0.5s ease;
}

.cart-table .cart-item:hover img {
  transform: scale(1.1);
}

.order-summary .btn-gradient {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  border: none;
  color: #fff;
  transition: background 0.3s ease, box-shadow 0.3s ease;
}

.order-summary .btn-gradient:hover {
  background: linear-gradient(135deg, #4ba27b 0%, #66ea8b 100%);
  box-shadow: 0 8px 20px rgba(75, 162, 123, 0.4);
}

.input-group .btn-outline-secondary {
  background: #fff;
  border-color: #ccc;
}

.input-group .btn-outline-secondary .fa-check::before {
  content: "✔";
}

.dropdown .btn-outline-primary {
  border-color: #4ba27b;
  color: #4ba27b;
}

.dropdown .btn-outline-primary:hover {
  background-color: #4ba27b;
  color: white;
}

.dropdown .fa-ticket::before {
  content: "🎫";
}

/* Form Styling */
form fieldset {
  border: none;
}

form fieldset legend {
  padding: 0 10px;
  font-size: 16px;
  margin-bottom: 10px;
}

form .border-gradient {
  border-radius: 10px;
  padding: 15px;
}

/* Responsive Adjustments */
@media (max-width: 768px) {
  .order-header .section-title {
    font-size: 22px;
  }

  .order-section .form-control,
  .order-section .form-select {
    font-size: 14px;
  }

  .cart-table th,
  .cart-table td {
    padding: 8px;
    font-size: 14px;
  }

  .cart-item img {
    height: 100px;
  }

  .order-summary .total-amount {
    font-size: 18px;
  }

  .order-summary .btn-gradient {
    font-size: 14px;
  }

  .row {
    flex-direction: column;
  }

  .col-md-6 {
    flex: 0 0 100%;
    max-width: 100%;
  }
}

@media (max-width: 992px) {
  .cart-table td.fit-content {
    max-width: 140px;
    font-size: 0.95rem;
  }

  .cart-table td img {
    max-width: 60px;
  }
}

@media (max-width: 576px) {
  .cart-table th,
  .cart-table td {
    padding: 6px 2px;
    font-size: 0.9rem;
  }

  .cart-table td.fit-content {
    max-width: 90px;
    font-size: 0.85rem;
  }

  .cart-table td img {
    max-width: 40px;
  }

  .container {
    padding: 0 10px;
  }

  .cart-table {
    font-size: 12px;
  }

  .cart-table th {
    font-size: 12px;
  }
}

.color-dot {
  display: inline-block;
  width: 15px;
  height: 15px;
  border-radius: 50%;
  border: 1px solid #eee;
  margin-right: 5px;
  vertical-align: middle;
}

:root {
  --primary-color: #66ea8b;
  /* Màu xanh lá cây chủ đạo */
  --primary-dark: #58c977;
  /* Một biến thể màu đậm hơn */
  --secondary-bg: #f8f9fa;
  /* Màu nền phụ */
  --text-color: #343a40;
  /* Màu chữ chính */
  --shadow-light: rgba(0, 0, 0, 0.1);
}

.custom-modal-fade.show {
  background-color: rgba(0, 0, 0, 0.6);
  /* Nền overlay mờ hơn */
}

.custom-modal-content {
  border-radius: 1rem;
  box-shadow: 0 10px 25px var(--shadow-light);
  border: none;
  overflow: hidden;
  /* Đảm bảo các góc bo tròn */
}

.custom-modal-header {
  background-color: var(--primary-color);
  color: #fff;
  border-bottom: none;
  padding: 1.5rem 2rem;
  display: flex;
  align-items: center;
}

.custom-modal-header .btn-close {
  color: #fff;
  /* Đảm bảo nút đóng có màu trắng */
  font-size: 1.25rem;
  opacity: 1;
  /* Nút đóng rõ ràng hơn */
}

.custom-modal-header .modal-title {
  font-weight: 600;
  font-size: 1.25rem;
}

.custom-modal-body {
  padding: 2rem;
  color: var(--text-color);
}

.custom-modal-body .lead {
  font-weight: 500;
}

.custom-alert-info {
  background-color: rgba(102, 234, 139, 0.15);
  /* Nền alert nhạt hơn */
  border: 1px solid var(--primary-color);
  color: var(--text-color);
  border-radius: 0.5rem;
  padding: 1.25rem !important;
}

.custom-alert-info .alert-heading {
  color: var(--primary-dark);
  font-weight: bold;
}

.custom-modal-footer {
  padding: 1.5rem 2rem;
  border-top: none;
  background-color: var(--secondary-bg);
}

.custom-btn-outline {
  border-color: #ced4da;
  color: #6c757d;
  transition: all 0.3s ease;
}

.custom-btn-outline:hover {
  background-color: #e9ecef;
  color: #495057;
}

.custom-btn-primary {
  background-color: #66ea8b;
  /* Đổi màu nền thành màu xanh lá cây bạn muốn */
  border-color: #66ea8b;
  /* Đổi màu viền tương ứng */
  font-weight: 500;
  transition: all 0.3s ease;
}

.custom-btn-primary:hover {
  background-color: #58c977;
  /* Màu xanh đậm hơn khi hover */
  border-color: #58c977;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(102, 234, 139, 0.4);
}
</style>