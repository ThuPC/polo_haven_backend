<script setup>
import KhachHangService from "../../../services/RoleQuanLy/KhachHangService";
import GHNService from "../../../services/RoleQuanLy/GHNService.js";
import { ref, computed, onMounted, watch } from "vue";

// Simple debounce implementation
function debounce(fn, delay) {
  let timeoutId;
  return function (...args) {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => {
      fn.apply(this, args);
    }, delay);
  };
}
import {
  getListSP,
  getListKhachHang,
  getListVoucherPublic,
  createInvoiceApi,
  getListVoucherPrivate,
  createForPayment,
  createPayment,
  orderStatusVnpay,
  getFilteredProducts,
  getFilterOptions,
} from "../../../services/RoleQuanLy/BanHangService";
import { jwtDecode } from "jwt-decode";
import VnpayModel from "./VnpayModel.vue";
import { useToast } from "vue-toastification";
// import { toDimension } from 'chart.js/helpers';

// Reactive data
const tabs = ref([]);
const activeTab = ref(null);
const nextTabId = ref(1);
const showOrderDetails = ref(false);
const showProductModal = ref(false);
const showProductDetailsModal = ref(false);
const showCustomerModal = ref(false);
const showAddCustomerForm = ref(false);
const showAddressModal = ref(false);
const showAddressSelectionModal = ref(false);
const showCustomerSearchModal = ref(false);
const selectedProduct = ref(null);
const vouchers = ref([]);
const products = ref([]);
const customers = ref([]);
const provinces = ref([]);
const districts = ref([]);
const wards = ref([]);
const shippingFee = ref(0);
const customerSearchQuery = ref("");
const customerFilterStatus = ref("");
const isGiaoHang = ref(false);
const toast = useToast();
const newOrder = ref(null);
const showVnpayModal = ref(false);
const vnpayPaymentData = ref(null);
let pollingInterval = null;

// Staff email from token
let staffEmail = "";
const token = localStorage.getItem("authToken");
if (token) {
  try {
    const decodedToken = jwtDecode(token);
    staffEmail = decodedToken.email || decodedToken.sub || "";
  } catch (e) {
    // Token không hợp lệ
  }
}

// Reactive data cho bộ lọc
const filterOptions = ref({
  kichThuoc: [],
  mauSac: [],
  chatLieu: [],
  thuongHieu: [],
  xuatXu: [],
});

const filterKichThuoc = ref("");
const filterMauSac = ref("");
const filterChatLieu = ref("");
const filterThuongHieu = ref("");
const filterXuatXu = ref("");
const productSearchQuery = ref("");

const newCustomer = ref({
  tenNguoiNhan: "",
  sdt: "",
  email: "",
  loginProvider: "LOCAL", // Mặc định là LOCAL
});

const customerErrors = ref({
  tenNguoiNhan: "",
  sdt: "",
  email: "",
});

const newAddress = ref({
  tenNguoiNhan: "",
  sdt: "",
  provinceId: null,
  districtId: null,
  wardCode: "",
  soNha: "",
});

// Computed để lọc khách hàng cho modal search
const filteredCustomers = computed(() => {
  // Bắt đầu với danh sách gốc
  let filtered = customers.value || [];

  // 1. Lọc theo trạng thái
  if (customerFilterStatus.value) {
    filtered = filtered.filter(
      (customer) => customer?.trangThai == customerFilterStatus.value
    );
  }

  // 2. Lọc theo từ khóa tìm kiếm
  const query = customerSearchQuery.value?.toLowerCase().trim() || "";
  if (query) {
    filtered = filtered.filter((customer) => {
      const tenKhachHang = customer?.tenKhachHang?.toLowerCase() || "";
      const sdt = customer?.sdt?.toLowerCase() || "";
      const email = customer?.email?.toLowerCase() || "";

      return (
        tenKhachHang.includes(query) ||
        sdt.includes(query) ||
        email.includes(query)
      );
    });
  }

  return filtered;
});

//
// Fetch data
const fetchProducts = async () => {
  try {
    const response = await getListSP();
    products.value = response.data;
  } catch (error) {
    showNotification("error", "Không thể tải danh sách sản phẩm.");
  }
};

const fetchCustomers = async () => {
  try {
    const response = await getListKhachHang();
    customers.value = response.data;
  } catch (error) {
    showNotification("error", "Không thể tải danh sách khách hàng.");
  }
};

const fetchPublicVouchers = async () => {
  try {
    const response = await getListVoucherPublic();
    vouchers.value = response.data;
  } catch (error) {
    showNotification("error", "Không thể tải danh sách voucher.");
  }
};

const fetchPrivateVouchers = async (idKhachHang) => {
  if (!idKhachHang) {
    return;
  }
  try {
    const response = await getListVoucherPrivate(idKhachHang);
    const privateVouchers = response.data || [];
    if (privateVouchers.length > 0) {
      vouchers.value = [...vouchers.value, ...privateVouchers];
    }
  } catch (error) {
    showNotification("error", "Không thể tải danh sách voucher cá nhân.");
  }
};

// GHN API calls
const fetchProvinces = async () => {
  try {
    const response = await GHNService.getProvinces();
    provinces.value = response.data;
  } catch (error) {
    showNotification("error", "Không thể tải danh sách tỉnh/thành phố.");
  }
};

const fetchDistricts = async (provinceId) => {
  try {
    const response = await GHNService.getDistricts(provinceId);
    districts.value = response.data;
  } catch (error) {
    showNotification("error", "Không thể tải danh sách quận/huyện.");
  }
};

const fetchWards = async (districtId) => {
  try {
    const response = await GHNService.getWards(districtId);
    wards.value = response.data;
  } catch (error) {
    showNotification("error", "Không thể tải danh sách xã/phường.");
  }
};

const calculateShippingFee = async (address) => {
  // Validate address fields
  if (!address || !address.districtId || !address.wardCode) {
    showNotification(
      "error",
      "Thông tin địa chỉ không đầy đủ để tính phí vận chuyển."
    );
    shippingFee.value = 30000; // Fallback fee
    const currentTab = getCurrentTab.value;
    if (currentTab) {
      currentTab.shipping = shippingFee.value;
      calculateTotal();
    }
    return;
  }

  try {
    const currentTab = getCurrentTab.value;
    const products = currentTab ? currentTab.products : [];

    // Sử dụng GHNService để tính phí vận chuyển thông minh
    const result = await GHNService.calculateSmartShippingFee(
      address,
      products
    );

    // Kiểm tra response từ backend
    if (result && result.data && result.data.total) {
      shippingFee.value = result.data.total;

      // Hiển thị thông tin dịch vụ vận chuyển
      const serviceInfo = result.serviceInfo;
      const serviceName = serviceInfo
        ? serviceInfo.service_name
        : "Giao hàng nhanh";
      const deliveryTime = serviceInfo
        ? serviceInfo.expected_delivery_time
        : "";

      // Hiển thị thông tin chi tiết về trọng lượng và kích thước
      const weightInfo = result.calculatedWeight
        ? ` (${result.calculatedWeight}g)`
        : "";

      // Kiểm tra xem có phải fallback không
      if (result.isFallback) {
        showNotification(
          "warning",
          `API GHN tạm thời không khả dụng. Áp dụng phí mặc định: ${formatCurrency(shippingFee.value)}`
        );
      } else if (result.isRegionalFee) {
        showNotification(
          "info",
          `Áp dụng phí vận chuyển theo vùng: ${formatCurrency(shippingFee.value)}${weightInfo}`
        );
      } else {
        showNotification(
          "success",
          `Phí vận chuyển ${serviceName}: ${formatCurrency(shippingFee.value)}${deliveryTime ? ` (${deliveryTime})` : ""}`
        );
      }
    } else {
      throw new Error("Không nhận được phí vận chuyển hợp lệ từ API");
    }

    if (currentTab) {
      currentTab.shipping = shippingFee.value;
      calculateTotal();
    }
  } catch (error) {
    // Hiển thị thông báo lỗi chi tiết hơn
    let errorMessage = "Không thể tính phí vận chuyển. Áp dụng phí mặc định.";

    // Kiểm tra nếu là lỗi SERVICE_UNAVAILABLE (503)
    if (error.response && error.response.status === 503) {
      errorMessage =
        "API GHN tạm thời không khả dụng hoặc không có tuyến giao hàng. Áp dụng phí mặc định.";
      showNotification("warning", errorMessage);
    } else if (
      error.response &&
      error.response.data &&
      error.response.data.message
    ) {
      const apiMessage = error.response.data.message;
      if (
        apiMessage.includes("tạm thời ngừng") ||
        apiMessage.includes("dịch bệnh") ||
        apiMessage.includes("route not found") ||
        apiMessage.includes("tuyến giao hàng")
      ) {
        errorMessage =
          "API GHN tạm thời không khả dụng hoặc không có tuyến giao hàng. Áp dụng phí mặc định.";
        showNotification("warning", errorMessage);
      } else {
        // errorMessage = `Lỗi tính phí vận chuyển: ${apiMessage}`;
        // showNotification("error", errorMessage);
      }
    } else if (error.message) {
      // errorMessage = `Lỗi tính phí vận chuyển: ${error.message}`;
      // showNotification("error", errorMessage);
    } else {
      // showNotification("error", errorMessage);
    }

    shippingFee.value = 30000; // Fallback fee
    const currentTab = getCurrentTab.value;
    if (currentTab) {
      currentTab.shipping = shippingFee.value;
      calculateTotal();
    }
  }
};

// Local storage
const saveStateToLocalStorage = () => {
  if (tabs.value.length > 0) {
    const stateToSave = {
      tabs: tabs.value,
      nextTabId: nextTabId.value,
      activeTabId: activeTab.value,
    };
    localStorage.setItem("pendingOrders", JSON.stringify(stateToSave));
  } else {
    localStorage.removeItem("pendingOrders");
  }
};

const loadStateFromLocalStorage = () => {
  const savedStateJSON = localStorage.getItem("pendingOrders");
  if (savedStateJSON) {
    try {
      const savedState = JSON.parse(savedStateJSON);
      tabs.value = savedState.tabs || [];
      nextTabId.value = savedState.nextTabId || 1;
      activeTab.value =
        savedState.activeTabId &&
          tabs.value.some((tab) => tab.id === savedState.activeTabId)
          ? savedState.activeTabId
          : tabs.value.length > 0
            ? tabs.value[0].id
            : null;
      showOrderDetails.value = tabs.value.length > 0;
    } catch (e) {
      localStorage.removeItem("pendingOrders");
    }
  }
};

// Computed
const getCurrentTab = computed(() => {
  const returnEmptyTab = () => ({
    id: null,
    orderCode: "",
    products: [],
    customer: null,
    paymentMethod: "cash",
    deliveryMethod: "at-store",
    discount: 0,
    discountPercent: 0,
    shipping: 0,
    discountCode: "",
    total: 0,
    soTienKhachHangThanhToan: 0,
    soTienHoan: 0,
    thanhToanTruoc: true,
    guestCustomer: {
      tenNguoiNhan: "",
      sdt: "",
      provinceId: null,
      districtId: null,
      wardCode: "",
      soNha: "",
      thanhPho: "",
      quanHuyen: "",
      xaPhuong: "",
    },
    selectedAddressId: null,
  });

  if (activeTab.value === null || tabs.value.length === 0) {
    return returnEmptyTab();
  }
  const foundTab = tabs.value.find((tab) => tab.id === activeTab.value);
  if (!foundTab) {
    return returnEmptyTab();
  }
  if (!foundTab.guestCustomer) {
    foundTab.guestCustomer = {
      tenNguoiNhan: "",
      sdt: "",
      provinceId: null,
      districtId: null,
      wardCode: "",
      soNha: "",
    };
  }
  if (foundTab.customer && !foundTab.customer.addresses) {
    foundTab.customer.addresses = [];
  }
  return foundTab;
});

// Fetch filter options
const fetchFilterOptions = async () => {
  try {
    const response = await getFilterOptions();

    filterOptions.value = response;
  } catch (error) {
    showNotification("error", "Không thể tải danh sách bộ lọc.");
  }
};

// Fetch filtered products
const fetchFilteredProducts = async () => {
  try {
    const filter = {
      kichThuoc: filterKichThuoc.value ? parseInt(filterKichThuoc.value) : null,
      mauSac: filterMauSac.value || null,
      chatLieu: filterChatLieu.value || null,
      thuongHieu: filterThuongHieu.value || null,
      xuatXu: filterXuatXu.value || null,
      searchQuery: productSearchQuery.value || null,
    };

    const response = await getFilteredProducts(filter);
    let filtered = Array.isArray(response) ? response : [];
    // Fuzzy search for product name/code if searchQuery is provided
    if (filter.searchQuery && filter.searchQuery.trim() !== "") {
      const q = filter.searchQuery.trim().toLowerCase();
      // Accept partial, non-exact, and non-accented matches
      const normalize = (str) =>
        str
          .toLowerCase()
          .normalize("NFD")
          .replace(/\p{Diacritic}/gu, "")
          .replace(/[^a-zA-Z0-9\s]/g, "");
      const qNorm = normalize(q);
      filtered = filtered.filter((p) => {
        const name = p.tenCTSP ? normalize(p.tenCTSP) : "";
        const code = p.maCTSP ? normalize(p.maCTSP) : "";
        return name.includes(qNorm) || code.includes(qNorm);
      });
    }
    products.value = filtered;

    if (products.value.length === 0) {
      showNotification("info", "Không tìm thấy sản phẩm phù hợp với bộ lọc.");
    }
  } catch (error) {
    showNotification("error", "Không thể lọc sản phẩm.");
  }
};

// Reset product filters
const resetProductFilters = () => {
  productSearchQuery.value = "";
  filterKichThuoc.value = "";
  filterMauSac.value = "";
  filterChatLieu.value = "";
  filterThuongHieu.value = "";
  filterXuatXu.value = "";
};
const uniqueVouchers = computed(() => {
  if (!vouchers.value || vouchers.value.length === 0) {
    return [];
  }
  const uniqueVoucherMap = new Map();
  vouchers.value.forEach((voucher) => {
    if (!uniqueVoucherMap.has(voucher.tenPhieuGiamGia)) {
      uniqueVoucherMap.set(voucher.tenPhieuGiamGia, voucher);
    }
  });
  return Array.from(uniqueVoucherMap.values());
});

// Methods
const createNewOrder = () => {
  // Kiểm tra số lượng tab hiện tại
  if (tabs.value.length >= 5) {
    showNotification("error", "Tối đa 5 hóa đơn được tạo cùng lúc!");
    return;
  }
  const newTab = {
    id: nextTabId.value,
    orderCode: `${String(nextTabId.value).padStart(3, "0")}`,
    products: [],
    customer: null,
    paymentMethod: "cash",
    deliveryMethod: "at-store",
    discount: 0,
    discountPercent: 0,
    shipping: 0,
    discountCode: "",
    total: 0,
    soTienKhachHangThanhToan: 0,
    soTienHoan: 0,
    thanhToanTruoc: true,
    guestCustomer: {
      tenNguoiNhan: "",
      sdt: "",
      provinceId: null,
      districtId: null,
      wardCode: "",
      soNha: "",
    },
  };
  tabs.value.push(newTab);
  activeTab.value = newTab.id;
  nextTabId.value++;
  showOrderDetails.value = true;
};

const closeTab = (tabIdToClose) => {
  const tabIndex = tabs.value.findIndex((tab) => tab.id === tabIdToClose);
  if (tabIndex === -1) return;
  const isClosingActiveTab = activeTab.value === tabIdToClose;
  tabs.value.splice(tabIndex, 1);
  if (isClosingActiveTab) {
    if (tabs.value.length > 0) {
      const newActiveIndex = Math.max(0, tabIndex - 1);
      activeTab.value = tabs.value[newActiveIndex].id;
    } else {
      activeTab.value = null;
      showOrderDetails.value = false;
    }
  }
};

const addProduct = () => {
  showProductModal.value = true;
};

const selectProduct = (product) => {
  "show confirm ở đây";
  selectedProduct.value = { ...product, quantity: 1 };
  closeModal("showProductModal");
  showProductDetailsModal.value = true;
};

const addToCart = () => {
  const currentTab = getCurrentTab.value;
  if (currentTab && selectedProduct.value) {
    const newProduct = {
      id: selectedProduct.value.id,
      tenCTSP: selectedProduct.value.tenCTSP,
      quantity: selectedProduct.value.quantity,
      warehouse: selectedProduct.value.soLuong,
      currentPrice: selectedProduct.value.donGia,
      calculatedPrice: selectedProduct.value.giaSauGiam,
      total: selectedProduct.value.quantity * selectedProduct.value.giaSauGiam,
      hinhAnh: selectedProduct.value.url,
      kichThuoc: selectedProduct.value.kichThuoc,
      mauSac: selectedProduct.value.mauSac,
      chatLieu: selectedProduct.value.chatLieu,
      thuongHieu: selectedProduct.value.thuongHieu,
      xuatXu: selectedProduct.value.xuatXu,
      soLuong: selectedProduct.value.soLuong,
    };
    currentTab.products.push(newProduct);
    calculateTotal();
    closeModal("showProductDetailsModal");
    toast.success("Đã thêm sản phẩm vào giỏ hàng!", {
      timeout: 2000,
    });
    selectedProduct.value = null;
  }
};

const updateQuantity = (productId, quantity) => {
  const currentTab = getCurrentTab.value;
  if (currentTab) {
    const product = currentTab.products.find((p) => p.id === productId);
    if (product) {
      product.quantity = parseInt(quantity) || 1;
      product.total = product.quantity * product.calculatedPrice;
      calculateTotal();
    }
  }
};

const updateSelectedQuantity = (quantity) => {
  if (selectedProduct.value) {
    selectedProduct.value.quantity = Math.min(
      parseInt(quantity) || 1,
      selectedProduct.value.soLuong
    );
  }
};

const removeProduct = (productId) => {
  const currentTab = getCurrentTab.value;
  if (currentTab) {
    const index = currentTab.products.findIndex((p) => p.id === productId);
    if (index > -1) {
      currentTab.products.splice(index, 1);
      calculateTotal();
    }
    toast.warning("Đã xóa sản phẩm khỏi giỏ hàng!", {
      timeout: 2000,
    });
  }
};

const isValidPhone = (phone) => {
  // Số điện thoại Việt Nam: 10 số, bắt đầu bằng 0, không chứa ký tự đặc biệt
  return /^0\d{9}$/.test(phone);
};

const isValidEmail = (email) => {
  // Validate email format
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

const validateCustomerField = (field) => {
  customerErrors.value[field] = "";

  switch (field) {
    case "name":
      if (!newCustomer.value.tenNguoiNhan?.trim()) {
        customerErrors.value.tenNguoiNhan = "Vui lòng nhập tên khách hàng";
      }
      break;
    case "phone":
      if (!newCustomer.value.sdt?.trim()) {
        customerErrors.value.sdt = "Vui lòng nhập số điện thoại";
      } else if (!isValidPhone(newCustomer.value.sdt)) {
        customerErrors.value.sdt =
          "Số điện thoại phải bắt đầu bằng 0 và có đúng 10 chữ số";
      }
      break;
    case "email":
      if (
        newCustomer.value.email?.trim() &&
        !isValidEmail(newCustomer.value.email)
      ) {
        customerErrors.value.email =
          "Email không hợp lệ. Vui lòng nhập đúng định dạng email";
      }
      break;
  }
};

const clearCustomerErrors = () => {
  customerErrors.value = {
    name: "",
    phone: "",
    email: "",
  };
};

const addNewCustomer = async () => {
  // Validate tất cả các trường
  validateCustomerField("name");
  validateCustomerField("phone");
  validateCustomerField("email");

  // Kiểm tra có lỗi không
  if (
    customerErrors.value.tenNguoiNhan ||
    customerErrors.value.sdt ||
    customerErrors.value.email
  ) {
    return;
  }

  try {
    // Tạo formData giống như Client.vue
    const formData = new FormData();
    const clientData = {
      tenKhachHang: newCustomer.value.tenNguoiNhan.trim(),
      sdt: newCustomer.value.phone.trim(),
      email: newCustomer.value.email?.trim() || "",
      gioiTinh: 1, // Mặc định là Nam
      trangThai: 1, // Mặc định là hoạt động
      loginProvider: newCustomer.value.loginProvider, // Thêm loginProvider
      diaChis: [], // Không có địa chỉ khi thêm nhanh
    };

    // Đảm bảo data được thêm đúng cách
    const dataBlob = new Blob([JSON.stringify(clientData)], {
      type: "application/json",
    });
    formData.append("data", dataBlob);

    const response = await KhachHangService.createKhachHang(formData);

    if (response && (response.status === 200 || response.status === 201)) {
      if (response.data && response.data.success === true) {
        // Thêm vào danh sách khách hàng
        const newCustomerFromAPI = response.data.data || response.data;
        customers.value.unshift(newCustomerFromAPI);

        // Chọn khách hàng mới với delay để đảm bảo database đã được cập nhật
        setTimeout(async () => {
          try {
            await confirmCustomer(newCustomerFromAPI);
          } catch (error) {
            // Fallback: chỉ set customer mà không load addresses
            const currentTab = getCurrentTab.value;
            if (currentTab) {
              currentTab.customer = { ...newCustomerFromAPI, addresses: [] };
              currentTab.selectedAddressId = null;
            }
          }
        }, 500);

        // Reset form
        newCustomer.value = {
          te: "",
          phone: "",
          email: "",
          loginProvider: "LOCAL",
        };
        clearCustomerErrors();
        showAddCustomerForm.value = false;
        showNotification("success", "Thêm khách hàng thành công!");
      } else {
        throw new Error(response.data.message || "Thêm khách hàng thất bại");
      }
    }
  } catch (error) {
    let errorMessage = "Có lỗi xảy ra khi thêm khách hàng";
    if (error.response) {
      const { status, data } = error.response;
      errorMessage =
        status === 400
          ? data.message || "Dữ liệu đầu vào không hợp lệ"
          : status === 409
            ? data.message || "Số điện thoại hoặc email đã tồn tại"
            : `Lỗi server: ${data.message || error.message}`;
    } else {
      errorMessage += `: ${error.message}`;
    }
    showNotification("error", errorMessage);
  }
};

const addNewAddress = async () => {
  try {
    // Validate form
    if (
      !newAddress.value.tenNguoiNhan ||
      !newAddress.value.sdt ||
      !newAddress.value.provinceId ||
      !newAddress.value.districtId ||
      !newAddress.value.wardCode ||
      !newAddress.value.soNha
    ) {
      showNotification("error", "Vui lòng điền đầy đủ thông tin địa chỉ!");
      return;
    }

    const currentTab = getCurrentTab.value;
    if (!currentTab || !currentTab.customer) {
      showNotification("error", "Vui lòng chọn khách hàng trước!");
      return;
    }

    // Tìm tên địa chỉ từ danh sách đã load
    const provinceObj = provinces.value.find(
      (p) => p.ProvinceID == newAddress.value.provinceId
    );
    const districtObj = districts.value.find(
      (d) => d.DistrictID == newAddress.value.districtId
    );
    const wardObj = wards.value.find(
      (w) => w.WardCode == newAddress.value.wardCode
    );

    // Tạo payload giống như Client.vue
    const addressPayload = {
      provinceId: newAddress.value.provinceId,
      districtId: newAddress.value.districtId,
      wardCode: newAddress.value.wardCode,
      thanhPho: provinceObj ? provinceObj.ProvinceName : "",
      quanHuyen: districtObj ? districtObj.DistrictName : "",
      xaPhuong: wardObj ? wardObj.WardName : "",
      soNha: newAddress.value.soNha,
      diaChiChiTiet: newAddress.value.soNha,
      tenNguoiNhan: newAddress.value.tenNguoiNhan,
      sdt: newAddress.value.sdt,
      isDefault: false,
      trangThai: 1,
    };

    // Gọi API thêm địa chỉ
    await KhachHangService.addAddress(currentTab.customer.id, addressPayload);

    // Refresh danh sách địa chỉ và map luôn
    const addresses =
      (
        await KhachHangService.getAddressesByCustomerId(currentTab.customer.id)
      ).data?.map((addr) => ({
        ...addr,
        thanhPho: addr.thanhPho || "",
        quanHuyen: addr.quanHuyen || "",
        xaPhuong: addr.xaPhuong || "",
        soNha: addr.soNha || "",
        tenNguoiNhan: addr.tenNguoiNhan || "",
        sdt: addr.sdt || "",
      })) || [];

    currentTab.customer.addresses = addresses;

    // Chọn địa chỉ mới vừa thêm
    const newAddedAddress = addresses.find(
      (addr) =>
        addr.tenNguoiNhan === newAddress.value.tenNguoiNhan &&
        addr.sdt === newAddress.value.sdt
    );

    if (newAddedAddress) {
      currentTab.selectedAddressId = newAddedAddress.id;
      if (currentTab.deliveryMethod === "delivery") {
        calculateShippingFee(newAddedAddress);
      }
    }

    // Reset form
    newAddress.value = {
      tenNguoiNhan: "",
      sdt: "",
      provinceId: "",
      districtId: "",
      wardCode: "",
      soNha: "",
    };

    showAddressModal.value = false;
    showAddressSelectionModal.value = false;
    showNotification("success", "Thêm địa chỉ thành công!");
  } catch (error) {
    showNotification(
      "error",
      "Thêm địa chỉ thất bại: " +
      (error.response?.data?.message || error.message)
    );
  }
};

const confirmCustomer = async (customer) => {
  const currentTab = getCurrentTab.value;
  if (currentTab) {
    try {
      // Load địa chỉ đầy đủ từ backend thay vì dùng customer.diaChis
      const addressesResponse = await KhachHangService.getAddressesByCustomerId(
        customer.id
      );
      const addresses = (addressesResponse.data || []).map((addr) => {
        return {
          ...addr,
          // Đảm bảo có đầy đủ thông tin
          thanhPho: addr.thanhPho || "",
          quanHuyen: addr.quanHuyen || "",
          xaPhuong: addr.xaPhuong || "",
          soNha: addr.soNha || "",
          tenNguoiNhan: addr.tenNguoiNhan || "",
          sdt: addr.sdt || "",
        };
      });

      currentTab.customer = { ...customer, addresses: addresses };
      let defaultAddress = null;
      if (currentTab.customer.addresses.length > 0) {
        defaultAddress = currentTab.customer.addresses.find(
          (addr) => addr.isDefault === true
        );
        if (!defaultAddress) {
          defaultAddress = currentTab.customer.addresses[0];
        }
      }
      if (defaultAddress) {
        currentTab.selectedAddressId = defaultAddress.id;
        if (currentTab.deliveryMethod === "delivery") {
          calculateShippingFee(defaultAddress);
        }
      } else {
        currentTab.selectedAddressId = null;
      }
      fetchPrivateVouchers(customer.id);
      closeModal("showCustomerModal");
      showNotification("success", "Chọn khách hàng thành công!");
    } catch (error) {
      // Fallback: sử dụng dữ liệu từ customer.diaChis nếu API fail
      const addresses = (customer.diaChis || []).map((addr) => ({
        ...addr,
        thanhPho: addr.thanhPho || "",
        quanHuyen: addr.quanHuyen || "",
        xaPhuong: addr.xaPhuong || "",
        soNha: addr.soNha || "",
        tenNguoiNhan: addr.tenNguoiNhan || "",
        sdt: addr.sdt || "",
      }));

      currentTab.customer = { ...customer, addresses: addresses };
      let defaultAddress = null;
      if (currentTab.customer.addresses.length > 0) {
        defaultAddress = currentTab.customer.addresses.find(
          (addr) => addr.isDefault === true
        );
        if (!defaultAddress) {
          defaultAddress = currentTab.customer.addresses[0];
        }
      }
      if (defaultAddress) {
        currentTab.selectedAddressId = defaultAddress.id;
        if (currentTab.deliveryMethod === "delivery") {
          calculateShippingFee(defaultAddress);
        }
      } else {
        currentTab.selectedAddressId = null;
      }
      fetchPrivateVouchers(customer.id);
      closeModal("showCustomerModal");
      showNotification("success", "Chọn khách hàng thành công!");
    }
  }
};

const selectCustomerFromModal = (customer) => {
  confirmCustomer(customer);
  showCustomerSearchModal.value = false;
};

const updatePaymentMethod = (method) => {
  const currentTab = getCurrentTab.value;
  if (currentTab) {
    currentTab.paymentMethod = method;
  }
};

const updateDeliveryMethod = async (method) => {
  const currentTab = getCurrentTab.value;
  if (!currentTab || currentTab.id === null) return;
  currentTab.deliveryMethod = method;
  if (method === "at-store") {
    isGiaoHang.value = false;
    currentTab.shipping = 0;
    calculateTotal();
  } else if (method === "delivery") {
    isGiaoHang.value = true;
    let address = null;
    if (currentTab.customer && currentTab.selectedAddressId) {
      address = currentTab.customer.addresses.find(
        (addr) => addr.id === currentTab.selectedAddressId
      );
    } else if (currentTab.guestCustomer && currentTab.guestCustomer.wardCode) {
      address = currentTab.guestCustomer;
    }
    if (address) {
      await calculateShippingFee(address);
    } else {
      currentTab.shipping = 30000; // Fallback fee
      calculateTotal();
    }
  }
};

const calculateTotal = () => {
  const currentTab = getCurrentTab.value;
  if (currentTab) {
    const subtotal = currentTab.products.reduce(
      (sum, product) => sum + product.total,
      0
    );
    currentTab.total = subtotal + currentTab.shipping - currentTab.discount;
    return currentTab.total;
  }
  return 0;
};

const formatCurrency = (amount) => {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(amount);
};

const formatVoucherDisplay = (voucher) => {
  let discountDetail = "";
  if (voucher.loaiGiamGia === 0) {
    discountDetail = `Giảm ${voucher.giaTriGiam}%`;
  } else if (voucher.loaiGiamGia === 1) {
    discountDetail = `Giảm ${formatCurrency(voucher.giaTriGiam)}`;
  }
  return `${voucher.tenPhieuGiamGia} - [${discountDetail}]`;
};

const applyDiscount = () => {
  const currentTab = getCurrentTab.value;
  const selectedVoucherId = currentTab.discountCode;
  if (!selectedVoucherId) {
    currentTab.discount = 0;
    currentTab.discountPercent = 0;
    calculateTotal();
    return;
  }
  const selectedVoucher = vouchers.value.find(
    (v) => v.id === selectedVoucherId
  );
  if (!selectedVoucher) {
    showNotification("error", "Mã giảm giá không hợp lệ!");
    return;
  }
  const productsTotal = currentTab.products.reduce(
    (sum, p) => sum + p.total,
    0
  );
  if (productsTotal < selectedVoucher.giaTriToiThieu) {
    showNotification(
      "error",
      `Đơn hàng tối thiểu ${formatCurrency(selectedVoucher.giaTriToiThieu)} để áp dụng mã này.`
    );
    currentTab.discount = 0;
    currentTab.discountCode = "";
    calculateTotal();
    return;
  }
  let discountAmount = 0;
  if (selectedVoucher.loaiGiamGia === 0) {
    discountAmount = (productsTotal * selectedVoucher.giaTriGiam) / 100;
    if (discountAmount > selectedVoucher.giaTriToiDa) {
      discountAmount =
        selectedVoucher.giaTriToiDa; /* nếu giá giảm lớn hơn giá tối đa thì lấy giá tối đa */
    }
  } else {
    discountAmount = selectedVoucher.giaTriGiam;
  }
  currentTab.discount = discountAmount;
  currentTab.discountPercent =
    selectedVoucher.loaiGiamGia === 0 ? selectedVoucher.giaTriGiam : 1;
  calculateTotal();
};

// Hàm tìm voucher tốt nhất cho đơn hàng hiện tại
const suggestBestVoucher = () => {
  const currentTab = getCurrentTab.value;
  if (!currentTab || !vouchers.value || vouchers.value.length === 0)
    return null;
  const productsTotal = currentTab.products.reduce(
    (sum, p) => sum + p.total,
    0
  );
  let bestVoucher = null;
  let maxDiscount = 0;
  vouchers.value.forEach((voucher) => {
    if (productsTotal < voucher.giaTriToiThieu) return;
    let discount = 0;
    if (voucher.loaiGiamGia === 0) {
      discount = (productsTotal * voucher.giaTriGiam) / 100;
      if (discount > voucher.giaTriToiDa) discount = voucher.giaTriToiDa;
    } else {
      discount = voucher.giaTriGiam;
    }
    if (discount > maxDiscount) {
      maxDiscount = discount;
      bestVoucher = voucher;
    }
  });
  return bestVoucher;
};
// Tự động gợi ý voucher tốt nhất khi thay đổi giỏ hàng hoặc danh sách voucher
watch(
  [() => getCurrentTab.value?.products, () => vouchers.value],
  () => {
    const currentTab = getCurrentTab.value;
    if (!currentTab) return;
    const bestVoucher = suggestBestVoucher();
    if (bestVoucher && currentTab.discountCode !== bestVoucher.id) {
      currentTab.discountCode = bestVoucher.id;
      applyDiscount();
    }
  },
  { deep: true }
);

const handleCreateInvoice = async () => {
  const currentTab = getCurrentTab.value;
  let shippingAddress = null;
  let addressId = null;
  if (currentTab.customer && currentTab.selectedAddressId) {
    shippingAddress = currentTab.customer.addresses?.find(
      (addr) => addr.id === currentTab.selectedAddressId
    );
    addressId = currentTab.selectedAddressId;
  } else {
    shippingAddress = currentTab.guestCustomer;
  }
  if (currentTab.deliveryMethod === 'delivery') {

    if (
      !shippingAddress ||
      !shippingAddress.tenNguoiNhan?.trim() ||
      !shippingAddress.sdt?.trim() ||
      !shippingAddress.thanhPho?.trim() ||
      !shippingAddress.quanHuyen?.trim() ||
      !shippingAddress.xaPhuong?.trim() ||
      !shippingAddress.soNha?.trim()
    ) {
      console.error("Thông tin địa chỉ khách lẻ không đầy đủ:", shippingAddress);
      showNotification("error", "Vui lòng điền đầy đủ thông tin giao hàng 1.");
      return;
    }
  }

  const originalTotalPrice = currentTab.products.reduce(
    (sum, p) => sum + p.currentPrice * p.quantity,
    0
  );
  const invoicePayload = {
    idKhachHang: currentTab.customer?.id || null,
    emailNhanVien: staffEmail,
    ghiChuHoaDon:
      currentTab.deliveryMethod === "at-store"
        ? "nhận tại quầy"
        : "đơn giao đi",
    // ghiChuHoaDon: currentTab.note || '',
    hinhThucNhanHang: currentTab.deliveryMethod === "at-store" ? 0 : 1,
    hinhThucThanhToan: currentTab.paymentMethod === "cash" ? 0 : 1,
    loaiHoaDon: 0,
    addressId: addressId,
    diaChiNhanHang: { ...shippingAddress },
    phiShip: currentTab.shipping,
    idPhieuGiamGia: currentTab.discountCode,
    giaGoc: originalTotalPrice,
    giaGiam: currentTab.discount,
    tongTienSauKhiGiam: currentTab.total,
    soTienKhachHangThanhToan: currentTab.total,
    soTienHoan: 0,
    thanhToanTruoc: true,
    danhSachSanPham: currentTab.products.map((p) => ({
      id: p.id,
      soLuong: p.quantity,
      giaGoc: p.currentPrice,
      giaSauKhiGiam: p.calculatedPrice,
    })),
  };

  try {
    const response = await createInvoiceApi(invoicePayload);
    showNotification(
      "success",
      `Tạo hóa đơn ${response.data.maHoaDon || ""} thành công!`
    );
    fetchPublicVouchers();
    fetchProducts();
    fetchCustomers();
    closeTab(currentTab.id);
  } catch (error) {
    const errorMessage =
      error.response?.data?.message || "Có lỗi xảy ra, không thể tạo hóa đơn.";
    showNotification("error", errorMessage);
  }
};
const handleVnpayPayment = async () => {
  const currentTab = getCurrentTab.value;
  const originalTotalPrice = currentTab.products.reduce(
    (sum, p) => sum + p.currentPrice * p.quantity,
    0
  );

  let shippingAddress = null;
  let addressId = null;
  if (currentTab.deliveryMethod === "delivery") {
    if (currentTab.customer && currentTab.selectedAddressId) {
      shippingAddress = currentTab.customer.addresses?.find(
        (addr) => addr.id === currentTab.selectedAddressId
      );
      addressId = currentTab.selectedAddressId;
    } else {
      shippingAddress = currentTab.guestCustomer;
    }

    if (
      !shippingAddress ||
      !shippingAddress.tenNguoiNhan?.trim() ||
      !shippingAddress.sdt?.trim() ||
      !shippingAddress.thanhPho?.trim() ||
      !shippingAddress.quanHuyen?.trim() ||
      !shippingAddress.xaPhuong?.trim() ||
      !shippingAddress.soNha?.trim()
    ) {
      console.error(
        "Thông tin địa chỉ khách lẻ không đầy đủ:",
        shippingAddress
      );
      showNotification("error", "Vui lòng điền đầy đủ thông tin giao hàng 2.");
      return;
    }
  }

  const orderPayload = {
    idKhachHang: currentTab.customer?.id || null,
    emailNhanVien: staffEmail,
    ghiChuHoaDon: "Chờ thanh toán VNPay QR",
    hinhThucNhanHang: currentTab.deliveryMethod === "at-store" ? 5 : 1,
    hinhThucThanhToan: 2, // 2: Mã định danh cho VNPAY QR
    loaiHoaDon: 0,
    addressId: addressId,
    diaChiNhanHang: { ...shippingAddress },
    phiShip: currentTab.shipping,
    idPhieuGiamGia: currentTab.discountCode,
    giaGoc: originalTotalPrice,
    giaGiam: currentTab.discount,
    tongTienSauKhiGiam: currentTab.total,
    danhSachSanPham: currentTab.products.map((p) => ({
      id: p.id,
      soLuong: p.quantity,
      giaGoc: p.currentPrice,
      giaSauKhiGiam: p.calculatedPrice,
    })),
  };

  try {
    const orderResponse = await createForPayment(orderPayload);
    newOrder.value = orderResponse.data;
    console.log("New Order for Payment:", newOrder.value);

    const vnpayUrlPayload = {
      orderId: newOrder.value.idHoaDon,
      amount: newOrder.value.tongTienSauKhiGiam,
      orderInfo: `Thanh toan HD ${newOrder.value.maHoaDon}`,
    };
    const paymentUrlResponse = await createPayment(vnpayUrlPayload);
    const paymentUrl = paymentUrlResponse.data;
    vnpayPaymentData.value = {
      url: paymentUrl,
      amount: newOrder.value.tongTienSauKhiGiam,
      orderInfo: `Thanh toan HD ${newOrder.value.maHoaDon}`,
      isPaid: false,
    };
    showVnpayModal.value = true;
    startPollingPaymentStatus(newOrder.value.idHoaDon);
    newOrder.value = null;
  } catch (error) {
    const errorMessage =
      error.response?.data?.message ||
      "Không thể tạo yêu cầu thanh toán. Vui lòng thử lại.";
    showNotification("error", errorMessage);
  }
};

// Biến pollingInterval phải được khai báo ở scope của <script setup>
// let pollingInterval = null; // Dòng này bạn đã có

const startPollingPaymentStatus = (orderId) => {
  stopPolling();

  pollingInterval = setInterval(async () => {
    try {
      const statusResponse = await orderStatusVnpay(orderId);
      const orderStatus =
        statusResponse.data.status ?? statusResponse.data.trangThai;

      if (Number(orderStatus) === 5 || Number(orderStatus) === 2) {
        stopPolling();
        if (vnpayPaymentData.value) {
          vnpayPaymentData.value.isPaid = true;
        }
        setTimeout(() => {
          closeVnpayModal();
          fetchProducts();
          closeTab(getCurrentTab.value.id);
          showNotification(
            "success",
            `Đơn hàng ${orderId} đã được thanh toán!`
          );
        }, 3000);
      } else {
        // Handle other payment status cases if needed
      }
    } catch (error) {
      stopPolling();
    }
  }, 4000);
};

const stopPolling = () => {
  if (pollingInterval) {
    clearInterval(pollingInterval);
    pollingInterval = null;
  }
};

const closeVnpayModal = () => {
  showVnpayModal.value = false;
  vnpayPaymentData.value = null;
  showNotification("warning", "hóa đơn đã được tạo vui lòng thanh toán trong 15 phút.");
  fetchProducts();
  closeTab(getCurrentTab.value.id);
  stopPolling();
};
const ShowVnpayModal = () => {
  showVnpayModal.value = true;
};

const confirmOrder = () => {
  const currentTab = getCurrentTab.value;
  if (!currentTab || currentTab.id === null) {
    showNotification("error", "Không có hóa đơn hợp lệ để thanh toán.");
    return;
  }
  if (currentTab.products.length === 0) {
    showNotification("error", "Hóa đơn phải có ít nhất một sản phẩm.");
    return;
  }
  if (currentTab.deliveryMethod === "delivery") {
    let shippingAddress = null;
    if (currentTab.customer && currentTab.selectedAddressId) {
      shippingAddress = currentTab.customer.addresses?.find(
        (addr) => addr.id === currentTab.selectedAddressId
      );
    } else {
      shippingAddress = currentTab.guestCustomer;
    }
    // Kiểm tra các trường địa chỉ
    if (
      !shippingAddress ||
      !shippingAddress.tenNguoiNhan?.trim() ||
      !shippingAddress.sdt?.trim() ||
      !shippingAddress.thanhPho?.trim() ||
      !shippingAddress.quanHuyen?.trim() ||
      !shippingAddress.xaPhuong?.trim() ||
      !shippingAddress.soNha?.trim()
    ) {
      console.error("Thông tin địa chỉ:", shippingAddress);
      showNotification("error", "Vui lòng điền đầy đủ thông tin giao hàng.");
      return;
    }
    if (!isValidPhone(shippingAddress.sdt)) {
      showNotification(
        "error",
        "Số điện thoại giao hàng không hợp lệ. Vui lòng nhập đúng định dạng 10 số bắt đầu bằng 0."
      );
      return;
    }
  }
  // PHÂN NHÁNH LOGIC THANH TOÁN
  if (currentTab.paymentMethod === "vnpay-qr") {
    handleVnpayPayment(); // Gọi hàm xử lý VNPay mới
  } else {
    // Với các phương thức khác (tiền mặt, chuyển khoản thường), gọi hàm cũ
    handleCreateInvoice();
  }
};

const showNotification = (type, message) => {
  notification.value = { show: true, type, message };
  setTimeout(() => {
    notification.value.show = false;
  }, 5000);
};

const closeModal = (modalName) => {
  if (modalName === "showProductModal") showProductModal.value = false;
  else if (modalName === "showProductDetailsModal")
    showProductDetailsModal.value = false;
  else if (modalName === "showCustomerModal") showCustomerModal.value = false;
  else if (modalName === "showAddressSelectionModal")
    showAddressSelectionModal.value = false;
};
// Thêm các hàm này vào trong <script setup>

const handleProvinceChange = (event) => {
  const tab = getCurrentTab.value;
  const selectedProvince = provinces.value.find(
    (p) => p.ProvinceID == event.target.value
  );
  if (selectedProvince) {
    tab.guestCustomer.thanhPho = selectedProvince.ProvinceName;
  } else {
    tab.guestCustomer.thanhPho = "";
  }
  tab.guestCustomer.districtId = null;
  tab.guestCustomer.quanHuyen = "";
  tab.guestCustomer.wardCode = "";
  tab.guestCustomer.xaPhuong = "";
};

const handleDistrictChange = (event) => {
  const tab = getCurrentTab.value;
  const selectedDistrict = districts.value.find(
    (d) => d.DistrictID == event.target.value
  );
  if (selectedDistrict) {
    tab.guestCustomer.quanHuyen = selectedDistrict.DistrictName;
  } else {
    tab.guestCustomer.quanHuyen = "";
  }
  tab.guestCustomer.wardCode = "";
  tab.guestCustomer.xaPhuong = "";
};

const handleWardChange = (event) => {
  const tab = getCurrentTab.value;
  const selectedWard = wards.value.find(
    (w) => w.WardCode == event.target.value
  );
  if (selectedWard) {
    tab.guestCustomer.xaPhuong = selectedWard.WardName;
  } else {
    tab.guestCustomer.xaPhuong = "";
  }
};

const notification = ref({ show: false, type: "success", message: "" });

onMounted(async () => {
  fetchProducts();
  fetchCustomers();
  fetchPublicVouchers();
  fetchProvinces();
  fetchFilterOptions();
  loadStateFromLocalStorage();
  // testFormatAddress();
});

watch(
  [tabs, activeTab],
  () => {
    saveStateToLocalStorage();
  },
  { deep: true }
);

// Debounce fetchFilteredProducts for productSearchQuery for better UX
const debouncedFetchFilteredProducts = debounce(fetchFilteredProducts, 300);

// Watch filters except productSearchQuery
watch(
  [
    filterKichThuoc,
    filterMauSac,
    filterChatLieu,
    filterThuongHieu,
    filterXuatXu,
  ],
  () => {
    fetchFilteredProducts();
  }
);

// Watch productSearchQuery with debounce
watch(productSearchQuery, () => {
  debouncedFetchFilteredProducts();
});

watch(
  () => getCurrentTab.value.guestCustomer.provinceId,
  (newProvinceId) => {
    if (newProvinceId) {
      fetchDistricts(newProvinceId);
      getCurrentTab.value.guestCustomer.districtId = null;
      getCurrentTab.value.guestCustomer.wardCode = "";
      wards.value = [];
    }
  }
);

watch(
  () => getCurrentTab.value.guestCustomer.districtId,
  (newDistrictId) => {
    if (newDistrictId) {
      fetchWards(newDistrictId);
      getCurrentTab.value.guestCustomer.wardCode = "";
    }
  }
);

watch(
  () => getCurrentTab.value.guestCustomer.wardCode,
  (newWardCode) => {
    if (newWardCode && getCurrentTab.value.deliveryMethod === "delivery") {
      calculateShippingFee(getCurrentTab.value.guestCustomer);
    }
  }
);

watch(
  () => getCurrentTab.value?.selectedAddressId,
  async (newAddressId) => {
    const tab = getCurrentTab.value;
    if (!tab || !tab.customer || tab.deliveryMethod !== "delivery") return;

    // Nếu người dùng bỏ chọn địa chỉ, xóa toàn bộ thông tin trong guestCustomer
    if (!newAddressId) {
      tab.guestCustomer = {
        tenNguoiNhan: "",
        sdt: "",
        thanhPho: "",
        quanHuyen: "",
        xaPhuong: "",
        soNha: "",
        provinceId: null,
        districtId: null,
        wardCode: "",
      };
      tab.shipping = 0;
      calculateTotal();
      return;
    }

    // Tìm địa chỉ mà người dùng đã chọn
    const selectedAddress = tab.customer.addresses.find(
      (addr) => addr.id === newAddressId
    );

    if (selectedAddress) {
      // Cập nhật guestCustomer với các tên trường mà handleCreateInvoice mong đợi
      tab.guestCustomer = {
        // Thông tin người nhận và địa chỉ dạng text
        tenNguoiNhan:
          selectedAddress.tenNguoiNhan || tab.customer.tenNguoiNhan || "",
        sdt: selectedAddress.sdt || tab.customer.sdt || "",
        thanhPho: selectedAddress.thanhPho || "",
        quanHuyen: selectedAddress.quanHuyen || "",
        xaPhuong: selectedAddress.xaPhuong || "",
        soNha: selectedAddress.soNha || "",

        // Vẫn lưu các ID để dùng cho việc tính phí ship
        provinceId: selectedAddress.provinceId || null,
        districtId: selectedAddress.districtId || null,
        wardCode: selectedAddress.wardCode || "",
      };
      // ======================================================================

      // Tạo đối tượng tạm thời để tính phí ship, logic này không đổi
      const shippingAddressForCalc = {
        districtId: selectedAddress.districtId,
        wardCode: selectedAddress.wardCode,
      };
      await calculateShippingFee(shippingAddressForCalc);
    } else {
      showNotification("error", "Địa chỉ được chọn không hợp lệ.");
    }
  }
);

// Watcher cho modal thêm địa chỉ giao hàng

// Watch province change in add address modal
watch(
  () => newAddress.value.provinceId,
  (newProvinceId) => {
    if (newProvinceId) {
      fetchDistricts(newProvinceId);
      newAddress.value.districtId = null;
      newAddress.value.wardCode = "";
      wards.value = [];
    }
  }
);

// Watch district change in add address modal
watch(
  () => newAddress.value.districtId,
  (newDistrictId) => {
    if (newDistrictId) {
      fetchWards(newDistrictId);
      newAddress.value.wardCode = "";
    }
  }
);

// --- PHÂN TRANG GIỎ HÀNG & DANH SÁCH SẢN PHẨM ---
const cartPage = ref(1);
const cartPageSize = 5;
const cartTotalPages = computed(() => {
  const total = getCurrentTab.value?.products?.length || 0;
  return Math.max(1, Math.ceil(total / cartPageSize));
});
const pagedCartProducts = computed(() => {
  const productsArr = getCurrentTab.value?.products || [];
  const start = (cartPage.value - 1) * cartPageSize;
  return productsArr.slice(start, start + cartPageSize);
});
watch(
  () => getCurrentTab.value?.products?.length,
  () => {
    cartPage.value = 1;
  }
);

const productListPage = ref(1);
const productListPageSize = 5;
const productListTotalPages = computed(() => {
  const total = products.value?.length || 0;
  return Math.max(1, Math.ceil(total / productListPageSize));
});
const pagedProducts = computed(() => {
  const list = products.value || [];
  const start = (productListPage.value - 1) * productListPageSize;
  return list.slice(start, start + productListPageSize);
});
watch(
  () => products.value?.length,
  () => {
    productListPage.value = 1;
  }
);

// Test function formatAddress
// const testFormatAddress = () => {
//   const testAddress = {
//     soNha: '123',
//     xaPhuong: 'Phường 1',
//     quanHuyen: 'Quận 1',
//     thanhPho: 'TP.HCM',
//     tenNguoiNhan: 'Nguyễn Văn A',
//     sdt: '0123456789'
//   };
// };

// Hàm xử lý chọn địa chỉ từ modal
const handleAddressSelection = (address) => {
  const currentTab = getCurrentTab.value;
  if (currentTab && currentTab.customer) {
    currentTab.selectedAddressId = address.id;
    showAddressSelectionModal.value = false;

    // Cập nhật guestCustomer với thông tin địa chỉ đã chọn
    currentTab.guestCustomer = {
      tenNguoiNhan:
        address.tenNguoiNhan || currentTab.customer.tenKhachHang || "",
      sdt: address.sdt || currentTab.customer.sdt || "",
      thanhPho: address.thanhPho || "",
      quanHuyen: address.quanHuyen || "",
      xaPhuong: address.xaPhuong || "",
      soNha: address.soNha || "",
      provinceId: address.provinceId || null,
      districtId: address.districtId || null,
      wardCode: address.wardCode || "",
    };

    // Tính phí vận chuyển nếu đang ở chế độ giao hàng
    if (currentTab.deliveryMethod === "delivery") {
      const shippingAddressForCalc = {
        districtId: address.districtId,
        wardCode: address.wardCode,
      };
      calculateShippingFee(shippingAddressForCalc);
    }

    showNotification("success", "Đã chọn địa chỉ giao hàng!");
  }
};

// Hàm lấy text hiển thị cho địa chỉ đã chọn
const getSelectedAddressText = () => {
  const currentTab = getCurrentTab.value;
  if (!currentTab || !currentTab.selectedAddressId || !currentTab.customer) {
    return "Chọn địa chỉ giao hàng";
  }

  const selectedAddress = currentTab.customer.addresses.find(
    (addr) => addr.id === currentTab.selectedAddressId
  );

  if (!selectedAddress) {
    return "Chọn địa chỉ giao hàng";
  }

  // Hiển thị đầy đủ thông tin: tên, SĐT, địa chỉ
  const parts = [];

  // Thêm tên người nhận
  if (selectedAddress.tenNguoiNhan) {
    parts.push(selectedAddress.tenNguoiNhan);
  }

  // Thêm số điện thoại
  if (selectedAddress.sdt) {
    parts.push(selectedAddress.sdt);
  }

  // Thêm địa chỉ đầy đủ
  const addressParts = [
    selectedAddress.soNha || selectedAddress.diaChiChiTiet,
    selectedAddress.xaPhuong,
    selectedAddress.quanHuyen,
    selectedAddress.thanhPho,
  ].filter((part) => part && part.trim());

  if (addressParts.length > 0) {
    parts.push(addressParts.join(", "));
  }

  return parts.join(" - ") || "Địa chỉ không đầy đủ";
};

// Hàm format địa chỉ cho modal (giống như Client.vue)
const formatAddressForModal = (address) => {
  if (!address) return "N/A";
  const parts = [
    address.tenNguoiNhan
      ? `<strong>Tên người nhận:</strong> ${address.tenNguoiNhan}`
      : null,
    address.sdt ? `<strong>Số điện thoại:</strong> ${address.sdt}` : null,
    [
      address.soNha || address.diaChiChiTiet,
      address.xaPhuong,
      address.quanHuyen,
      address.thanhPho,
    ]
      .filter((part) => part && part.trim())
      .join(", ")
      ? `<strong>Địa chỉ:</strong> ${[address.soNha || address.diaChiChiTiet, address.xaPhuong, address.quanHuyen, address.thanhPho].filter((part) => part && part.trim()).join(", ")}`
      : null,
  ].filter((part) => part);
  return parts.join("<br>") || "N/A";
};
</script>

<template>
  <div class="sales-app">
    <!-- Title -->
    <h1 class="page-title">Danh sách đơn hàng</h1>

    <!-- Order Tabs -->
    <div class="order-tabs">
      <div class="tabs-container">
        <div v-for="tab in tabs" :key="tab.id" :class="['order-tab', { active: activeTab === tab.id }]"
          @click="activeTab = tab.id">
          <span class="order-code">Hóa đơn #{{ tab.orderCode }}</span>

          <!-- THÊM HUY HIỆU SỐ LƯỢNG Ở ĐÂY -->
          <span v-if="tab.products.length > 0" class="product-count-badge">
            {{ tab.products.length }}
          </span>

          <button class="close-tab-btn" @click.stop="closeTab(tab.id)">
            ×
          </button>
        </div>
      </div>
      <button class="btn btn-primary" @click="createNewOrder" :disabled="tabs.length >= 5" title="Tạo hóa đơn">
        <i class="fa-solid fa-plus"></i> Tạo hóa đơn
      </button>
    </div>

    <!-- Notification -->
    <div v-if="notification.show" :class="['notification', notification.type]">
      <i :class="notification.type === 'success'
        ? 'fa-solid fa-check-circle'
        : 'fa-solid fa-exclamation-triangle'
        "></i>
      {{ notification.message }}
      <button @click="notification.show = false" class="close-btn">×</button>
    </div>

    <!-- Placeholder when no orders -->
    <div v-if="!showOrderDetails" class="placeholder">
      <span class="placeholder-icon"><i class="fa-solid fa-cart-plus"></i></span>
      Chưa có đơn hàng nào
    </div>

    <!-- Main Content Area -->
    <div v-if="showOrderDetails" class="main-layout three-row-layout">
      <!-- 3 phần nằm 3 hàng ngang riêng biệt: Giỏ hàng, Thông tin khách hàng, Thông tin thanh toán -->
      <div class="cart-section">
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
          <div v-if="getCurrentTab?.products.length === 0" class="text-center text-muted p-5 bg-light rounded">
            <i class="fa-solid fa-shopping-basket fa-3x mb-3 text-secondary"></i>
            <p class="mb-0 fs-5">Chưa có sản phẩm nào trong giỏ hàng</p>
            <small>Hãy thêm sản phẩm vào giỏ hàng để tiếp tục nhéee!</small>
          </div>

          <!-- Lặp qua từng sản phẩm trong giỏ hàng -->
          <div v-for="product in pagedCartProducts" :key="product.id"
            class="cart-item-card d-flex align-items-center gap-4 py-4 border-bottom position-relative">
            <!-- Cột Ảnh sản phẩm -->
            <div class="item-image flex-shrink-0">
              <img :src="product.hinhAnh ||
                product.url ||
                require('@/assets/img/ao_mac_dinh.png')
                " :alt="product.tenCTSP || 'Product Image'" class="img-fluid rounded"
                style="width: 150px; height: 150px; object-fit: cover" />
            </div>

            <!-- Cột Thông tin chi tiết sản phẩm -->
            <div class="item-details flex-grow-1 d-flex flex-column">
              <!-- Tên sản phẩm và phân loại -->
              <div class="mb-2">
                <h5 class="fw-bold mb-1 text-dark">
                  {{ product.tenCTSP || "Tên sản phẩm" }}
                </h5>
                <span class="text-muted fs-6">{{ product.mauSac || "Màu sắc" }} /
                  {{ product.kichThuoc || "Kích cỡ" }}</span>
              </div>

              <!-- Thông tin giá và số lượng còn -->
              <div class="d-flex flex-column gap-2 mb-3">
                <!-- Giá gốc (hiện tại) bị gạch ngang nếu khác với giá được tính -->
                <div v-if="product.currentPrice !== product.calculatedPrice"
                  class="d-flex justify-content-between align-items-center text-muted">
                  <small class="fs-6">Giá gốc:</small>
                  <span class="text-decoration-line-through fs-6">{{
                    formatCurrency(product.currentPrice)
                  }}</span>
                </div>
                <!-- Giá được tính -->
                <div class="d-flex justify-content-between align-items-center text-dark">
                  <small class="fs-6">Giá mỗi SP:</small>
                  <span class="fw-semibold fs-6 text-primary">{{
                    formatCurrency(product.calculatedPrice)
                  }}</span>
                </div>
                <!-- Số lượng còn -->
                <div class="d-flex justify-content-between align-items-center text-muted">
                  <small class="fs-6"><i class="fa-solid fa-boxes-stacked me-2 fs-6"></i>Còn lại: {{ product.warehouse
                  }}</small>

                </div>
              </div>
              <!-- Phần điều chỉnh số lượng và tổng tiền -->
              <div class="d-flex justify-content-between align-items-end mt-auto">
                <!-- Bộ điều chỉnh số lượng -->
                <div class="quantity-control d-flex align-items-center border rounded-pill">
                  <input type="number" :value="product.quantity"
                    @input="updateQuantity(product.id, $event.target.value)" class="form-control text-center" min="1" />
                </div>

                <!-- Tổng tiền của sản phẩm và nút xóa -->
                <div class="d-flex flex-column align-items-end ms-3">
                  <small class="text-muted d-block fs-6">Tổng:</small>
                  <span class="fw-bolder fs-4 text-primary mb-2">{{
                    formatCurrency(product.total)
                  }}</span>
                  <!-- Nút Xóa sản phẩm -->
                  <button @click="removeProduct(product.id)"
                    class="btn btn-sm btn-link text-danger p-0 text-decoration-none" title="Xóa sản phẩm">
                    <i class="fa-solid fa-trash-can me-1 fs-6"></i><span class="fs-6">Xóa</span>
                  </button>
                </div>
              </div>

              <!-- Cảnh báo nếu số lượng đã chọn bằng hoặc vượt quá số lượng còn -->
              <small v-if="product.quantity >= product.warehouse" class="text-danger fw-bold d-block mt-2">
                <i class="fa-solid fa-exclamation-triangle me-1 fs-6"></i><span class="fs-6">Đã đạt số lượng
                  tồn tối đa</span>
              </small>
            </div>
          </div>
        </div>

        <div class="pagination-wrapper">
          <ul class="pagination justify-content-end">
            <li class="page-item" :class="{ disabled: cartPage === 1 }">
              <button class="page-link" @click="cartPage = Math.max(1, cartPage - 1)" :disabled="cartPage === 1">
                &laquo;
              </button>
            </li>
            <li class="page-item" v-for="n in cartTotalPages" :key="n" :class="{ active: cartPage === n }">
              <button class="page-link" @click="cartPage = n">{{ n }}</button>
            </li>
            <li class="page-item" :class="{ disabled: cartPage === cartTotalPages }">
              <button class="page-link" @click="cartPage = Math.min(cartTotalPages, cartPage + 1)"
                :disabled="cartPage === cartTotalPages">
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
            <div class="block-header d-flex justify-content-between align-items-center">
              <h2 class="cart-title">
                <i class="fa-solid fa-user-tie"></i> Thông tin khách hàng
              </h2>
              <div class="customer-actions">
                <button class="btn btn-primary btn-sm" @click="showCustomerSearchModal = true">
                  <i class="fa-solid fa-search"></i> Tìm khách hàng
                </button>
                <button class="btn btn-secondary btn-sm" @click="showAddCustomerForm = true">
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
                    {{
                      getCurrentTab?.customer?.sdt || "Chưa có số điện thoại"
                    }}
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
                <input type="radio" name="deliveryMethod" value="at-store"
                  :checked="getCurrentTab?.deliveryMethod === 'at-store'" @change="updateDeliveryMethod('at-store')" />
                <span>Nhận tại quầy</span>
              </label>
              <label class="radio-option">
                <input type="radio" name="deliveryMethod" value="delivery"
                  :checked="getCurrentTab?.deliveryMethod === 'delivery'" @change="updateDeliveryMethod('delivery')" />
                <span>Giao hàng</span>
              </label>
            </div>
          </div>
          <div v-if="getCurrentTab?.deliveryMethod === 'delivery'" class="address-section">
            <div v-if="getCurrentTab?.customer">
              <div class="address-selection" v-if="getCurrentTab.customer.addresses.length > 0">
                <label class="address-label">Chọn địa chỉ có sẵn</label>
                <div class="address-display" @click="showAddressSelectionModal = true">
                  <div v-if="getCurrentTab.selectedAddressId" class="selected-address">
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
                  <input v-model="getCurrentTab.guestCustomer.tenNguoiNhan" type="text" class="form-control"
                    placeholder="Nhập tên người nhận" />
                </div>
                <div class="col-md-6 form-group">
                  <label>Số điện thoại</label>
                  <input v-model="getCurrentTab.guestCustomer.sdt" maxlength="10" type="text" class="form-control"
                    placeholder="Nhập số điện thoại" />
                </div>
                <div class="col-md-6 form-group">
                  <label>Tỉnh/Thành phố</label>
                  <select v-model="getCurrentTab.guestCustomer.provinceId" @change="handleProvinceChange($event)"
                    class="form-select">
                    <option :value="null">Chọn tỉnh/thành phố</option>
                    <option v-for="province in provinces" :key="province.ProvinceID" :value="province.ProvinceID">
                      {{ province.ProvinceName }}
                    </option>
                  </select>
                </div>
                <div class="col-md-6 form-group">
                  <label>Quận/Huyện</label>
                  <select v-model="getCurrentTab.guestCustomer.districtId" @change="handleDistrictChange($event)"
                    class="form-select" :disabled="!getCurrentTab.guestCustomer.provinceId">
                    <option :value="null">Chọn quận/huyện</option>
                    <option v-for="district in districts" :key="district.DistrictID" :value="district.DistrictID">
                      {{ district.DistrictName }}
                    </option>
                  </select>
                </div>
                <div class="col-md-6 form-group">
                  <label>Xã/Phường</label>
                  <select v-model="getCurrentTab.guestCustomer.wardCode" @change="handleWardChange($event)"
                    class="form-select" :disabled="!getCurrentTab.guestCustomer.districtId">
                    <option :value="''">Chọn xã/phường</option>
                    <option v-for="ward in wards" :key="ward.WardCode" :value="ward.WardCode">
                      {{ ward.WardName }}
                    </option>
                  </select>
                </div>
                <div class="col-md-6 form-group">
                  <label>Số nhà/Ngõ/Đường</label>
                  <input v-model="getCurrentTab.guestCustomer.soNha" type="text" class="form-control"
                    placeholder="Nhập số nhà/ngõ/đường" />
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
                  <input type="radio" value="vnpay-qr" :checked="getCurrentTab?.paymentMethod === 'vnpay-qr'"
                    @change="updatePaymentMethod('vnpay-qr')" />
                  <span><img :src="require('@/assets/img/logos/vnpay-logo.jpg')" alt="VNPay" style="
                        height: 25px;
                        vertical-align: middle;
                        margin-right: 4px;
                      " />VNPay QR</span>
                </label>
                <label class="radio-option"> </label>
                <label class="radio-option">
                  <input type="radio" value="cash" :checked="getCurrentTab?.paymentMethod === 'cash'"
                    @change="updatePaymentMethod('cash')" />
                  <span>Tiền mặt</span>
                </label>
              </div>
            </div>
            <div class="payment-details">
              <div class="d-flex align-items-start">
                <span class="fw-bold mb-2">Phiếu giảm giá: </span><br />
                <select id="voucher-select" v-model="getCurrentTab.discountCode" class="form-select w-40 ms-2"
                  @change="applyDiscount">
                  <option value="">-- Không áp dụng --</option>
                  <option v-for="voucher in uniqueVouchers" :key="voucher.id" :value="voucher.id">
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
                <span>Phí vận chuyển:
                  <img :src="require('@/assets/img/logos/giao_hang_nhanh_logo.png')
                    " alt="GHN" style="
                      height: 25px;
                      vertical-align: middle;
                      margin-right: 4px;
                    " /></span>
                <span class="amount positive">+ {{ formatCurrency(getCurrentTab?.shipping || 0) }}</span>
              </div>
              <div class="detail-row">
                <span>Giảm giá ({{ getCurrentTab?.discountPercent || 0 }}%):</span>
                <span class="amount negative">– {{ formatCurrency(getCurrentTab?.discount || 0) }}</span>
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
              <!-- Nút xem mã thanh toán -->
              <button v-if="newOrder && getCurrentTab?.paymentMethod === 'vnpay-qr'" class="btn btn-success"
                @click="ShowVnpayModal()">
                Xem mã thanh toán
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
            <button @click="showAddCustomerForm = false" class="btn btn-secondary">
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
              <input v-model="newCustomer.tenNguoiNhan" type="text" class="form-control"
                :class="{ 'is-invalid': customerErrors.tenNguoiNhan }" placeholder="Nhập tên khách hàng"
                @blur="validateCustomerField('tenNguoiNhan')" />
              <div v-if="customerErrors.tenNguoiNhan" class="invalid-feedback">
                {{ customerErrors.tenNguoiNhan }}
              </div>
            </div>
            <div class="form-group">
              <label>Số điện thoại <span class="text-danger">*</span></label>
              <input v-model="newCustomer.sdt" maxlength="10" type="text" class="form-control"
                :class="{ 'is-invalid': customerErrors.sdt }" placeholder="Nhập số điện thoại"
                @blur="validateCustomerField('phone')" />
              <div v-if="customerErrors.sdt" class="invalid-feedback">
                {{ customerErrors.sdt }}
              </div>
            </div>
            <div class="form-group">
              <label>Email</label>
              <input v-model="newCustomer.email" type="email" class="form-control"
                :class="{ 'is-invalid': customerErrors.email }" placeholder="Nhập email"
                @blur="validateCustomerField('email')" />
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
              <button class="btn btn-secondary" @click="
                showAddCustomerForm = false;
              clearCustomerErrors();
              ">
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
            <button @click="showAddressModal = false" class="btn btn-secondary close-modal-btn">
              ×
            </button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>Tên người nhận</label>
              <input v-model="newAddress.tenNguoiNhan" type="text" class="form-control"
                placeholder="Nhập tên người nhận" />
            </div>
            <div class="form-group">
              <label>Số điện thoại</label>
              <input v-model="newAddress.sdt" maxlength="10" type="text" class="form-control"
                placeholder="Nhập số điện thoại" />
            </div>
            <div class="form-group">
              <label>Tỉnh/Thành phố</label>
              <select v-model="newAddress.provinceId" class="form-select">
                <option :value="null">Chọn tỉnh/thành phố</option>
                <option v-for="province in provinces" :key="province.ProvinceID" :value="province.ProvinceID">
                  {{ province.ProvinceName }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>Quận/Huyện</label>
              <select v-model="newAddress.districtId" class="form-select" :disabled="!newAddress.provinceId">
                <option :value="null">Chọn quận/huyện</option>
                <option v-for="district in districts" :key="district.DistrictID" :value="district.DistrictID">
                  {{ district.DistrictName }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>Xã/Phường</label>
              <select v-model="newAddress.wardCode" class="form-select" :disabled="!newAddress.districtId">
                <option :value="''">Chọn xã/phường</option>
                <option v-for="ward in wards" :key="ward.WardCode" :value="ward.WardCode">
                  {{ ward.WardName }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>Số nhà/Ngõ/Đường</label>
              <input v-model="newAddress.soNha" type="text" class="form-control" placeholder="Nhập số nhà/ngõ/đường" />
            </div>
            <div class="modal-footer d-flex justify-content-end">
              <button class="btn btn-secondary" @click="
                showAddressModal = false;
              showAddressSelectionModal = true;
              ">
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
  </div>

  <!-- Product Selection Modal -->
  <div v-if="showProductModal" class="modal">
    <div class="modal-content" style="max-width: 2300px">
      <div class="modal-header">
        <h2 class="cart-title">
          <i class="fa-solid fa-rectangle-list"></i> Danh sách sản phẩm có sẵn
        </h2>
        <button @click="closeModal('showProductModal')" class="btn btn-secondary">
          ×
        </button>
      </div>
      <div class="modal-body">
        <div v-if="filterOptions" class="filter-section product-filter-block position-relative">
          <div class="filter-header mb-2 d-flex align-items-center gap-2">
            <i class="fa-solid fa-filter filter-icon"></i>
            <span class="fw-bold" style="font-size: 18px">Bộ lọc sản phẩm</span>
          </div>

          <!-- phần bộ lọc -->
          <div class="container-fluid mb-4 p-3 bg-light rounded shadow-sm">
            <div class="row g-3 align-items-center">
              <!-- Tìm kiếm sản phẩm -->
              <div class="col-md-6 col-lg-8">
                <input v-model="productSearchQuery" type="text" class="form-control"
                  placeholder="Tìm kiếm theo mã hoặc tên sản phẩm..." />
              </div>

              <!-- Nút hủy lọc -->
              <div class="col-md-6 col-lg-4 text-end">
                <button class="btn btn-outline-secondary" @click="resetProductFilters" title="Hủy tất cả bộ lọc">
                  <i class="fa-solid fa-xmark me-1"></i>
                  Hủy lọc
                </button>
              </div>
            </div>

            <hr class="my-3" />

            <!-- Các tùy chọn bộ lọc -->
            <div class="row g-3">
              <div class="col-lg-3 col-md-4 col-sm-6 col-12">
                <label for="filterKichThuoc" class="form-label visually-hidden">Kích cỡ</label>
                <select v-model="filterKichThuoc" class="form-select" id="filterKichThuoc">
                  <option value="">Tất cả kích cỡ</option>
                  <option v-for="size in filterOptions.kichThuoc || []" :key="size" :value="size">
                    {{ size || "Không xác định" }}
                  </option>
                </select>
              </div>
              <div class="col-lg-3 col-md-4 col-sm-6 col-12">
                <label for="filterMauSac" class="form-label visually-hidden">Màu sắc</label>
                <select v-model="filterMauSac" class="form-select" id="filterMauSac">
                  <option value="">Tất cả màu sắc</option>
                  <option v-for="mauSac in filterOptions.mauSac || []" :key="mauSac" :value="mauSac">
                    {{ mauSac || "Không xác định" }}
                  </option>
                </select>
              </div>
              <div class="col-lg-3 col-md-4 col-sm-6 col-12">
                <label for="filterChatLieu" class="form-label visually-hidden">Chất liệu</label>
                <select v-model="filterChatLieu" class="form-select" id="filterChatLieu">
                  <option value="">Tất cả chất liệu</option>
                  <option v-for="chatLieu in filterOptions.chatLieu || []" :key="chatLieu" :value="chatLieu">
                    {{ chatLieu || "Không xác định" }}
                  </option>
                </select>
              </div>
              <div class="col-lg-3 col-md-4 col-sm-6 col-12">
                <label for="filterThuongHieu" class="form-label visually-hidden">Thương hiệu</label>
                <select v-model="filterThuongHieu" class="form-select" id="filterThuongHieu">
                  <option value="">Tất cả thương hiệu</option>
                  <option v-for="thuongHieu in filterOptions.thuongHieu || []" :key="thuongHieu" :value="thuongHieu">
                    {{ thuongHieu || "Không xác định" }}
                  </option>
                </select>
              </div>
              <div class="col-lg-3 col-md-4 col-sm-6 col-12">
                <label for="filterXuatXu" class="form-label visually-hidden">Xuất xứ</label>
                <select v-model="filterXuatXu" class="form-select" id="filterXuatXu">
                  <option value="">Tất cả xuất xứ</option>
                  <option v-for="xuatXu in filterOptions.xuatXu || []" :key="xuatXu" :value="xuatXu">
                    {{ xuatXu || "Không xác định" }}
                  </option>
                </select>
              </div>
            </div>
          </div>

          <table class="table text-center">
            <thead class="table-secondary">
              <tr>
                <th>STT</th>
                <th>Ảnh</th>
                <th>Mã</th>
                <th>Sản phẩm</th>
                <!-- <th>Kích cỡ</th>
                <th>Màu sắc</th> -->
                <th>Chất liệu</th>
                <th>Thương hiệu</th>
                <th>Xuất xứ</th>
                <th>Giá gốc</th>
                <th>Giá khuyến mãi</th>
                <th>Số lượng trong kho</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(product, index) in pagedProducts" :key="product.id">
                <td>
                  {{ (productListPage - 1) * productListPageSize + index + 1 }}
                </td>
                <td>
                  <img v-if="product.url" :src="product.url" class="product-img" />
                  <img v-else src="@/assets/img/ao_mac_dinh.png" class="product-img" />
                </td>
                <td>{{ product.maCTSP }}</td>
                <td>{{ product.tenCTSP }}</td>
                <!-- <td>{{ product.kichThuoc }}</td>
                <td>{{ product.mauSac }}</td> -->
                <td>{{ product.chatLieu }}</td>
                <td>{{ product.thuongHieu }}</td>
                <td>{{ product.xuatXu }}</td>
                <td>{{ formatCurrency(product.donGia) }}</td>
                <td>{{ formatCurrency(product.giaSauGiam) }}</td>
                <td>{{ product.soLuong }}</td>
                <td>
                  <button @click="selectProduct(product)" class="btn btn-choose">
                    Chọn
                  </button>
                </td>
              </tr>
            </tbody>
          </table>

          <div class="pagination-wrapper" style="margin-top: 12px">
            <ul class="pagination justify-content-end">
              <li class="page-item" :class="{ disabled: productListPage === 1 }">
                <button class="page-link" @click="productListPage = Math.max(1, productListPage - 1)"
                  :disabled="productListPage === 1">
                  &laquo;
                </button>
              </li>
              <li class="page-item" v-for="n in productListTotalPages" :key="n"
                :class="{ active: productListPage === n }">
                <button class="page-link" @click="productListPage = n">
                  {{ n }}
                </button>
              </li>
              <li class="page-item" :class="{ disabled: productListPage === productListTotalPages }">
                <button class="page-link" @click="
                  productListPage = Math.min(
                    productListTotalPages,
                    productListPage + 1
                  )
                  " :disabled="productListPage === productListTotalPages">
                  &raquo;
                </button>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Product Details Modal -->
  <div v-if="showProductDetailsModal" class="modal">
    <div class="modal-content" style="max-width: 700px">
      <div class="modal-header">
        <h2 class="section-title">
          <i class="bi bi-info-circle"></i> Thông tin sản phẩm
        </h2>
        <button @click="closeModal('showProductDetailsModal')" class="btn btn-secondary">
          ×
        </button>
      </div>
      <div class="modal-body">
        <div class="product-details">
          <img :src="selectedProduct?.url" alt="Product Image" class="product-img" />
          <p><strong>Mã:</strong> {{ selectedProduct?.maCTSP }}</p>
          <p><strong>Tên:</strong> {{ selectedProduct?.tenCTSP }}</p>
          <p><strong>Kích cỡ:</strong> {{ selectedProduct?.kichThuoc }}</p>
          <p><strong>Màu sắc:</strong> {{ selectedProduct?.mauSac }}</p>
          <p><strong>Chất liệu:</strong> {{ selectedProduct?.chatLieu }}</p>
          <p><strong>Thương hiệu:</strong> {{ selectedProduct?.thuongHieu }}</p>
          <p><strong>Xuất xứ:</strong> {{ selectedProduct?.xuatXu }}</p>
          <p>
            <strong>Số lượng trong kho:</strong> {{ selectedProduct?.soLuong }}
          </p>
          <p>
            <strong>Giá gốc:</strong>
            {{ formatCurrency(selectedProduct?.donGia) }}
          </p>
          <p>
            <strong>Giá khuyến mãi:</strong>
            {{ formatCurrency(selectedProduct?.giaSauGiam) }}
          </p>
          <div>
            <label>Số lượng:</label>
            <input type="number" v-model.number="selectedProduct.quantity"
              @input="updateSelectedQuantity($event.target.value)" min="1" :max="selectedProduct?.soLuong"
              class="form-control" />
          </div>
        </div>
        <button @click="addToCart" class="btn btn-primary">
          Thêm sản phẩm
        </button>
      </div>
    </div>
  </div>

  <!-- Customer Search Modal -->
  <div v-if="showCustomerSearchModal" class="modal">
    <div class="modal-content" style="max-width: 800px">
      <div class="modal-header">
        <h2 class="cart-title">
          <i class="fa-solid fa-search"></i> Tìm kiếm khách hàng
        </h2>
        <button @click="showCustomerSearchModal = false" class="btn btn-secondary">
          ×
        </button>
      </div>
      <div class="modal-body">
        <div class="search-section">
          <div class="form-group">
            <label>Tìm kiếm</label>
            <input v-model="customerSearchQuery" type="text" class="form-control search-input"
              placeholder="Nhập tên, SĐT, hoặc email khách hàng..." />
          </div>
          <div class="form-group">
            <label>Trạng thái</label>
            <select v-model="customerFilterStatus" class="form-select">
              <option value="">Tất cả trạng thái</option>
              <option value="1">Hoạt động</option>
              <option value="0">Ngừng hoạt động</option>
            </select>
          </div>
        </div>

        <div class="customer-results">
          <div v-if="filteredCustomers.length === 0" class="no-results">
            <i class="fa-solid fa-search"></i>
            <p>Không tìm thấy khách hàng phù hợp</p>
          </div>
          <div v-else class="customer-cards">
            <div v-for="customer in filteredCustomers" :key="customer.id" class="customer-card"
              @click="selectCustomerFromModal(customer)">
              <div class="customer-avatar">
                {{ customer?.tenKhachHang?.[0] || "?" }}
              </div>
              <div class="customer-details">
                <h4 class="customer-name">
                  {{ customer?.tenKhachHang || "Không có tên" }}
                </h4>
                <p class="customer-phone">
                  {{ customer?.sdt || "Không có SĐT" }}
                </p>
                <p class="customer-email">
                  {{ customer?.email || "Chưa có email" }}
                </p>
              </div>
              <div class="customer-action">
                <button class="btn btn-choose">Chọn</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <VnpayModel v-if="showVnpayModal" :paymentData="vnpayPaymentData" @close="closeVnpayModal" />

  <!-- Address Selection Modal -->
  <div v-if="showAddressSelectionModal" class="address-selection-modal">
    <div class="modal-overlay" @click="showAddressSelectionModal = false"></div>
    <div class="modal-content">
      <!-- Header -->
      <div class="modal-header">
        <h2 class="modal-title">
          <i class="fa-solid fa-location-dot fw-bold text-center"></i> Chọn địa
          chỉ
        </h2>
        <button class="close-btn" @click="showAddressSelectionModal = false">
          ×
        </button>
      </div>

      <!-- Address List -->
      <div class="address-list">
        <div v-for="address in getCurrentTab?.customer?.addresses || []" :key="address.id" class="address-item"
          :class="{ selected: getCurrentTab?.selectedAddressId === address.id }"
          @click="handleAddressSelection(address)">
          <!-- Radio Button -->
          <div class="radio-container">
            <div class="radio-button" :class="{
              checked: getCurrentTab?.selectedAddressId === address.id,
            }">
              <div v-if="getCurrentTab?.selectedAddressId === address.id" class="radio-dot"></div>
            </div>
          </div>

          <!-- Address Content -->
          <div class="address-content">
            <div class="address-info" v-html="formatAddressForModal(address)"></div>
            <div v-if="address.isDefault" class="default-label">Mặc định</div>
          </div>
        </div>

        <!-- No Addresses Message -->
        <div v-if="!getCurrentTab?.customer?.addresses?.length" class="no-addresses">
          <i class="fa-solid fa-map-marker-alt"></i>
          <p>Chưa có địa chỉ nào</p>
        </div>
      </div>

      <!-- Add Address Button -->
      <div class="add-address-section">
        <button class="add-address-btn" @click="
          showAddressModal = true;
        showAddressSelectionModal = false;
        ">
          <i class="fa-solid fa-plus"></i>
          Thêm địa chỉ
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.sales-app {
  height: 100vh;
  display: flex;
  flex-direction: column;
  font-family:
    "Inter",
    -apple-system,
    BlinkMacSystemFont,
    "Segoe UI",
    Roboto,
    sans-serif;
  background-color: #f5f7fa;
  color: #2d3748;
}

/* Page Title */
.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #2d3748;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
}

/* Notification */
.notification {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 300px;
  animation: slideIn 0.3s ease-out;
}

.notification.success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.notification.error {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.notification .close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  margin-left: auto;
  color: inherit;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }

  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* Placeholder */
.placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-style: italic;
  color: #718096;
  background: #f7fafc;
  border-radius: 12px;
  margin: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  gap: 12px;
}

.placeholder-icon {
  font-size: 32px;
}

/* Order Tabs */
.order-tabs {
  background: white;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  padding: 12px 24px;
  gap: 12px;
  min-height: 60px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.tabs-container {
  display: flex;
  gap: 8px;
  flex: 1;
  overflow-x: auto;

  /* --- THÊM CÁC DÒNG NÀY --- */
  padding-top: 10px;
  /* Tạo không gian ở phía trên */
  margin-top: -10px;
  /* Đẩy container lên để bù lại cho padding, giữ nguyên vị trí trực quan */
}

.order-tab {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  background: #edf2f7;
  border: 1px solid #e2e8f0;
  border-radius: 8px 8px 0 0;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  gap: 8px;
  transition: all 0.3s ease;
}

.order-tab:hover {
  background: #e2e8f0;
  transform: translateY(-2px);
}

.order-tab.active {
  background: white;
  border-bottom: none;
  box-shadow: 0 -2px 4px rgba(0, 0, 0, 0.1);
}

.order-code {
  font-weight: 600;
  color: #2d3748;
}

.close-tab-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #718096;
  font-size: 16px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.close-tab-btn:hover {
  background: #fed7d7;
  color: #c53030;
}

/* Button Styles */
.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* Hiệu ứng hover cho nút chọn ở các bảng */
.btn-choose {
  background: #e2e8f0;
  color: #2d3748;
  border: 1px solid #cbd5e1;
  transition:
    background 0.2s,
    color 0.2s,
    box-shadow 0.2s;
}

.btn-choose:hover,
.btn-choose:focus {
  background: #38a169;
  color: #fff;
  border-color: #38a169;
  box-shadow: 0 2px 8px rgba(56, 161, 105, 0.15);
}

/* Main Layout */
.main-layout {
  flex: 1;
  display: flex;
  background: #f5f7fa;
}

/* Cart Section */
.cart-section {
  flex: 1;
  padding: 24px;
  background: white;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.cart-title {
  font-size: 24px;
  font-weight: 700;
  color: #2d3748;
  display: flex;
  align-items: center;
  gap: 8px;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-img {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

.product-name {
  font-weight: 500;
  color: #2d3748;
}

.total-amount {
  font-weight: 600;
  color: #38a169;
}

.delete-btn {
  background: none;
  border: none;
  color: #c53030;
  cursor: pointer;
  font-size: 16px;
  padding: 8px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.delete-btn:hover {
  background: #fed7d7;
}

.empty-cart {
  text-align: center;
  padding: 48px;
  color: #718096;
  font-size: 16px;
  font-style: italic;
}

.pagination {
  text-align: right;
  font-size: 14px;
  color: #718096;
  padding: 12px;
}

/* Order Info Section */
.order-info-section {
  width: 100%;
  max-width: 420px;
  padding: 24px;
  background: #edf2f7;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-block {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.block-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

/* Customer Selection Section */
.customer-selection-section {
  margin-top: 20px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.customer-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.block-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

.customer-info {
  margin-bottom: 24px;
}

.customer-label {
  font-size: 12px;
  color: #718096;
  margin-bottom: 12px;
}

.customer-display {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.customer-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #3182ce;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 16px;
}

.customer-name {
  font-weight: 500;
  color: #2d3748;
}

.customer-phone,
.customer-email {
  font-size: 12px;
  color: #718096;
}

.customer-placeholder {
  text-align: center;
  color: #718096;
  font-style: italic;
}

.delivery-method,
.payment-method {
  margin-bottom: 24px;
}

.delivery-method h4,
.payment-method h4 {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #2d3748;
}

.radio-group {
  display: flex;
  gap: 24px;
}

.radio-option {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
}

.radio-option input[type="radio"] {
  margin: 0;
  accent-color: #38a169;
}

.payment-details {
  margin-bottom: 24px;
}

.detail-row,
.discount-code-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
}

.amount {
  font-weight: 500;
}

.amount.positive {
  color: #38a169;
}

.amount.negative {
  color: #c53030;
}

.grand-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-top: 2px solid #e2e8f0;
  margin-bottom: 24px;
}

.grand-total-label {
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

.grand-total-amount {
  font-size: 20px;
  font-weight: 700;
  color: #38a169;
}

/* Modal Styles */
.modal {
  position: fixed;
  top: 0;
  left: 250px;
  width: calc(100% - 250px);
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 24px;
  border-radius: 12px;
  width: 95%;
  max-width: 1400px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-shrink: 0;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
  color: #2d3748;
}

.modal-body {
  flex-grow: 1;
  padding: 0;
  overflow: auto;
}

.close-button {
  background: none;
  border: none;
  font-size: 1.8rem;
  cursor: pointer;
  color: #718096;
  padding: 0;
  line-height: 1;
}

.close-button:hover {
  color: #2d3748;
}

.filter-section {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.product-details {
  margin-bottom: 20px;
}

/* Customer Info Block */
.customer-info-block {
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.customer-info {
  margin-bottom: 20px;
}

.customer-label {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.customer-display {
  display: flex;
  align-items: center;
  gap: 12px;
}

.customer-placeholder {
  color: #718096;
  font-style: italic;
  text-align: center;
  padding: 10px;
}

.customer-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #3182ce;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.customer-name {
  font-size: 16px;
  font-weight: 500;
  color: #2d3748;
}

.customer-phone,
.customer-email {
  font-size: 14px;
  color: #718096;
}

/* Delivery Method */
.delivery-method {
  margin-bottom: 20px;
}

.delivery-method h4 {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 12px;
}

.radio-group {
  display: flex;
  gap: 20px;
}

.radio-option {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  cursor: pointer;
}

.radio-option input[type="radio"] {
  accent-color: #38a169;
}

/* Address Section */
.address-section {
  margin-top: 20px;
}

.address-section h4 {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 12px;
}

.address-selection {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.address-label {
  font-size: 14px;
  font-weight: 500;
  color: #2d3748;
}

/* Address Input Form */

.address-input {
  display: flex;
  flex-wrap: wrap;
  gap: 20px 24px;
  background: #f9fafb;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 24px 28px 12px 28px;
  margin-top: 12px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
  max-width: 100%;
  box-sizing: border-box;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 8px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #2d3748;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.form-group input,
.form-group select {
  padding: 10px 18px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 15px;
  background: #fff;
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border: none;
  border-color: #4ba27b;
  box-shadow: 0 0 0 2px rgba(75, 162, 123, 0.15);
}

/* New Customer Form in Modal */
.add-customer-section {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
}

.new-customer-form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px;
  background: #f7fafc;
  border-radius: 8px;
}

.new-customer-form .form-group {
  margin-bottom: 0;
}

.new-customer-form .btn {
  grid-column: span 2;
  justify-self: end;
}

/* Responsive Design */

@media (max-width: 992px) {
  .address-input {
    flex-direction: column;
    gap: 12px;
  }

  .new-customer-form {
    grid-template-columns: 1fr;
  }

  .order-info-section {
    width: 100%;
    padding: 16px;
  }

  .radio-group {
    flex-direction: column;
    gap: 12px;
  }
}

@media (max-width: 1200px) {
  .main-layout {
    flex-direction: column;
  }

  .cart-section {
    border-right: none;
    border-bottom: 1px solid #e2e8f0;
  }

  .order-info-section {
    width: 100%;
    max-width: none;
  }
}

/* Bắt buộc: Cần đặt position: relative cho thẻ cha để định vị huy hiệu bên trong nó */
.order-tab {
  position: relative;
  /* Các thuộc tính CSS hiện có của bạn cho tab... */
  /* Ví dụ: padding, border, background-color... */
}

/* Đây là phần CSS cho huy hiệu thông báo */
.product-count-badge {
  /* Định vị tuyệt đối ở góc trên bên phải của tab */
  position: absolute;
  top: -8px;
  /* Điều chỉnh để huy hiệu hơi nhô ra ngoài */
  right: 20px;
  /* Điều chỉnh để không đè lên nút đóng (X) */

  /* Màu sắc và kích thước */
  background-color: #e74c3c;
  /* Màu đỏ thông báo */
  color: white;
  /* Chữ màu trắng */
  min-width: 20px;
  /* Chiều rộng tối thiểu */
  height: 20px;
  /* Chiều cao */
  border-radius: 50%;
  /* Bo tròn để tạo thành hình tròn */
  font-size: 12px;
  /* Cỡ chữ nhỏ */
  font-weight: bold;
  /* Chữ đậm */

  /* Căn giữa số bên trong huy hiệu */
  display: flex;
  align-items: center;
  justify-content: center;

  /* Đảm bảo huy hiệu luôn nằm trên các phần tử khác */
  z-index: 10;
}

/* Đảm bảo 3 phần nằm dọc, không phải 3 cột */

.three-row-layout>div {
  width: 100%;
  margin-bottom: 24px;
}

/* Đảm bảo nút không bị tràn ra ngoài block */
.info-block {
  overflow: hidden;
}

/* Đổi màu chữ Tổng thanh toán thành đỏ */
.grand-total-label {
  color: #e53935;
  font-weight: bold;
}

/* Căn phải phân trang */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

/* Customer List Section nằm bên phải, rộng hơn */
.customer-list-section {
  float: right;
  width: 60%;
  margin-left: 2%;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  padding: 20px 18px 16px 18px;
  min-width: 340px;
  max-width: 650px;
  display: block;
}

.customer-list-title {
  font-size: 20px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

@media (max-width: 1100px) {
  .customer-list-section {
    width: 100%;
    min-width: 0;
    max-width: 100%;
    float: none;
    margin-left: 0;
    margin-top: 16px;
  }
}

/* Đảm bảo customer-list-section nằm full width, không float, không flex phải */
.customer-list-section.full-width {
  width: 100%;
  max-width: 100%;
  float: none !important;
  align-self: unset !important;
  margin: 24px 0 0 0;
  box-sizing: border-box;
}

.customer-info-block {
  width: 100%;
}

/* Nếu có flex hoặc grid ở cha, ép customer-list-section chiếm full dòng */
.customer-info-section {
  display: block !important;
}

/* Đẹp hơn cho block địa chỉ khách lẻ */
.guest-address-block {
  background: #f8fafc;
  border-radius: 8px;
  padding: 18px 16px 10px 16px;
  margin-top: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
}

.guest-address-block .address-title {
  font-weight: 600;
  color: #3182ce;
  font-size: 16px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* Tăng chiều rộng cho các combobox trong bộ lọc sản phẩm */
.filter-select-lg {
  min-width: 180px;
  max-width: 100%;
  /* font-size: 1.08rem; */
  padding: 8px 12px;
}

/* Customer Search Modal */
.search-section {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.search-input {
  flex: 1;
}

.customer-results {
  max-height: 400px;
  overflow-y: auto;
}

.no-results {
  text-align: center;
  padding: 40px;
  color: #718096;
}

.no-results i {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.customer-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.customer-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.customer-card:hover {
  background: #f7fafc;
  border-color: #cbd5e1;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.customer-details {
  flex: 1;
}

.customer-details h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
}

.customer-details p {
  margin: 0;
  font-size: 14px;
  color: #718096;
}

.customer-action {
  flex-shrink: 0;
}

/* Address Cards */
.address-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.address-card {
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.address-card:hover {
  border-color: #4ba27b;
  box-shadow: 0 2px 8px rgba(75, 162, 123, 0.15);
  transform: translateY(-1px);
}

.address-card.active {
  border-color: #4ba27b;
  background: #f0f9ff;
  box-shadow: 0 2px 8px rgba(75, 162, 123, 0.2);
}

.address-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.address-card-title {
  font-weight: 600;
  color: #2d3748;
  display: flex;
  align-items: center;
  gap: 8px;
}

.address-card-title i {
  color: #4ba27b;
}

.address-card-actions {
  display: flex;
  gap: 8px;
}

.default-badge {
  background: #4ba27b;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.address-card-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.address-text {
  color: #4a5568;
  font-size: 14px;
  line-height: 1.4;
}

.address-phone {
  color: #718096;
  font-size: 13px;
  font-weight: 500;
}

/* Responsive cho address cards */
@media (max-width: 768px) {
  .address-card {
    padding: 12px;
  }

  .address-card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .address-card-actions {
    align-self: flex-end;
  }
}

/* Address Display Styles */
.address-display {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 12px;
}

.address-display:hover {
  background: #edf2f7;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

.selected-address {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.selected-address i {
  color: #4ba27b;
  font-size: 16px;
}

.selected-address span {
  color: #2d3748;
  font-weight: 500;
  font-size: 14px;
}

.no-address-selected {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  color: #718096;
}

.no-address-selected i {
  color: #a0aec0;
  font-size: 16px;
}

.no-address-selected span {
  font-size: 14px;
}

.address-display .fa-chevron-right {
  color: #a0aec0;
  font-size: 12px;
  transition: transform 0.2s;
}

.address-display:hover .fa-chevron-right {
  transform: translateX(2px);
  color: #4ba27b;
}

/* Address Selection Modal Styles */
.address-selection-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
}

.modal-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  max-width: 600px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #dee2e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #f5f5f5;
  color: #666;
}

.address-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0;
}

.address-item {
  display: flex;
  align-items: flex-start;
  padding: 16px 24px;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 1px solid #f8f8f8;
}

.address-item:hover {
  background: #f9f9f9;
}

.address-item.selected {
  background: #f0f9ff;
}

.radio-container {
  margin-right: 16px;
  margin-top: 2px;
}

.radio-button {
  width: 20px;
  height: 20px;
  border: 2px solid #ddd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.radio-button.checked {
  border-color: #4ba27b;
  background: #4ba27b;
}

.radio-dot {
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
}

.address-content {
  flex: 1;
  min-width: 0;
}

.address-info {
  color: #666;
  font-size: 14px;
  line-height: 1.4;
  margin-bottom: 8px;
}

.default-label {
  color: #4ba27b;
  font-size: 12px;
  font-weight: 500;
}

.no-addresses {
  text-align: center;
  padding: 40px 24px;
  color: #999;
}

.no-addresses i {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.no-addresses p {
  margin: 0;
  font-size: 16px;
}

.add-address-section {
  padding: 20px 24px;
  border-top: 1px solid #f0f0f0;
  background: white;
}

.add-address-btn {
  width: 100%;
  background: #4ba27b;
  color: white;
  border: none;
  padding: 8px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s;
}

.add-address-btn:hover {
  background: #3d8b6a;
  transform: translateY(-1px);
}

.add-address-btn i {
  font-size: 14px;
}

/* Filter Section Styles */
.filter-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e9ecef;
  margin-bottom: 20px;
}

.filter-header {
  border-bottom: 1px solid #dee2e6;
  padding-bottom: 15px;
  margin-bottom: 20px;
}

.filter-header h5 {
  color: #495057;
  font-size: 16px;
}

/* Responsive cho filter header */
@media (max-width: 768px) {
  .filter-header .d-flex {
    flex-direction: column;
    align-items: flex-start !important;
    gap: 10px !important;
  }

  .filter-header .form-control {
    width: 100% !important;
  }
}

/* Đảm bảo dropdown có đủ không gian */
.filter-section .form-select {
  min-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.filter-section .form-select option {
  white-space: normal;
  word-wrap: break-word;
}

/* Responsive cho modal */
@media (max-width: 768px) {
  .modal-content {
    width: 95%;
    max-height: 90vh;
  }

  .modal-header {
    padding: 16px 20px;
  }

  .address-item {
    padding: 12px 20px;
  }

  .add-address-section {
    padding: 16px 20px;
  }

  .filter-section {
    padding: 15px;
  }

  .filter-header {
    padding-bottom: 10px;
  }
}

.filter-search-input {
  border: 1.5px solid #4ba27b;
  border-radius: 8px;
  box-shadow: none;
  transition: border-color 0.2s;
  background: #fff;
}

.filter-search-input:focus {
  border-color: #2e8b57;
  box-shadow: 0 0 0 2px rgba(75, 162, 123, 0.15);
  background: #f6fffa;
}

.btn-reset-filter {
  height: 38px;
  padding: 0 14px;
  border-radius: 8px;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.btn-reset-filter i {
  font-size: 16px;
}

.two-column-layout {
  display: flex;
  flex-direction: row;
  gap: 24px;
  align-items: flex-start;
  min-height: 80vh;
}

.cart-section {
  flex: 1.2;
  min-width: 0;
  max-width: 55%;
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.right-section {
  flex: 1;
  min-width: 0;
  max-width: 45%;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.customer-info-section,
.payment-info-section {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 24px;
  margin-bottom: 0;
}

/* Đảm bảo payment-info-section nằm dưới customer-info-section */
.payment-info-section {
  margin-top: 24px;
}

/* Responsive: chuyển về dọc khi màn hình nhỏ */
@media (max-width: 1100px) {
  .two-column-layout {
    flex-direction: column;
    gap: 0;
  }

  .cart-section,
  .right-section {
    max-width: 100%;
    padding: 12px;
  }

  .customer-info-section,
  .payment-info-section {
    padding: 12px;
    margin-top: 0;
  }
}

@media (max-width: 768px) {
  .filter-header .d-flex.flex-wrap {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }

  .filter-search-input {
    max-width: 100%;
  }

  /* CSS cho modal */
  .modal {
    display: flex;
    /* Hoặc block, tùy thuộc cách bạn quản lý modal */
    position: fixed;
    z-index: 1050;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    overflow: auto;
    background-color: rgba(0, 0, 0, 0.5);
    align-items: center;
    justify-content: center;
  }

  .modal-content {
    background-color: #fff;
    margin: auto;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
    max-height: 90vh;
    /* Giới hạn chiều cao modal */
    overflow-y: auto;
    /* Cho phép cuộn bên trong modal nếu nội dung quá dài */
  }

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 15px;
    margin-bottom: 20px;
    border-bottom: 1px solid #eee;
  }

  .modal-header .cart-title {
    margin: 0;
    font-size: 24px;
    color: #333;
  }

  .modal-header .btn-secondary {
    background: none;
    border: none;
    font-size: 2rem;
    color: #aaa;
    cursor: pointer;
    padding: 0 10px;
  }

  .modal-header .btn-secondary:hover {
    color: #777;
  }

  /* CSS cho filter section */
  .filter-section {
    background-color: #f8f9fa;
    padding: 15px;
    border-radius: 8px;
    margin-bottom: 20px;
  }

  .filter-header {
    color: #007bff;
  }

  .filter-icon {
    font-size: 20px;
  }

  .btn-reset-filter {
    display: flex;
    align-items: center;
  }

  .btn-reset-filter i {
    margin-right: 5px;
  }

  /* CSS cho danh sách sản phẩm mới */
  .product-selection-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
    /* Khoảng cách giữa các item sản phẩm */
  }

  .product-selection-item {
    display: flex;
    align-items: center;
    /* Căn chỉnh các phần tử con theo chiều dọc */
    gap: 15px;
    /* Khoảng cách giữa ảnh, chi tiết và nút */
    padding: 10px 15px;
    border: 1px solid #ddd;
    border-radius: 8px;
    background-color: #fff;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
    flex-wrap: wrap;
    /* Cho phép các item con xuống dòng nếu không đủ chỗ */
  }

  .product-selection-image {
    flex-shrink: 0;
    /* Ngăn ảnh bị co lại */
  }

  .product-img-small {
    width: 60px;
    /* Kích thước ảnh nhỏ */
    height: 60px;
    object-fit: cover;
    border-radius: 4px;
  }

  .product-selection-details {
    flex-grow: 1;
    /* Cho phép phần chi tiết chiếm không gian còn lại */
    display: flex;
    flex-direction: column;
    gap: 5px;
    /* Khoảng cách giữa các hàng chi tiết */
  }

  .detail-row {
    display: flex;
    flex-wrap: wrap;
    /* Cho phép các span xuống dòng */
    gap: 10px;
    /* Khoảng cách giữa các span trong cùng một hàng */
    align-items: baseline;
    /* Căn chỉnh theo baseline */
  }

  .product-stt {
    font-weight: bold;
    color: #555;
    min-width: 25px;
    /* Đảm bảo STT có độ rộng nhất định */
  }

  .product-code,
  .product-name,
  .product-size-color,
  .product-material,
  .product-brand-origin,
  .product-quantity {
    font-size: 0.9rem;
    color: #666;
  }

  .product-pricing {
    margin-top: 5px;
    /* Tạo khoảng cách với các thông tin trên */
    font-size: 1rem;
  }

  .product-original-price {
    color: #999;
    margin-right: 10px;
  }

  .product-sale-price {
    color: #dc3545;
    /* Màu đỏ cho giá khuyến mãi */
    font-weight: bold;
  }

  .product-selection-actions {
    flex-shrink: 0;
    /* Ngăn nút bị co lại */
    margin-left: auto;
    /* Đẩy nút sang phải */
  }

  .btn-choose {
    background-color: #007bff;
    color: white;
    border: none;
    padding: 8px 15px;
    border-radius: 5px;
    cursor: pointer;
    transition: background-color 0.2s ease;
  }

  .btn-choose:hover {
    background-color: #0056b3;
  }

  /* Pagination */
  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }

  .pagination .page-item .page-link {
    color: #007bff;
    border: 1px solid #dee2e6;
    border-radius: 4px;
    margin: 0 2px;
  }

  .pagination .page-item.active .page-link {
    background-color: #007bff;
    border-color: #007bff;
    color: white;
  }

  .pagination .page-item.disabled .page-link {
    color: #6c757d;
    pointer-events: none;
    background-color: #e9ecef;
  }

  @media (max-width: 768px) {
    .product-selection-item {
      flex-direction: column;
      /* Trên màn hình nhỏ, xếp dọc */
      align-items: flex-start;
    }

    .product-selection-actions {
      margin-left: 0;
      /* Bỏ đẩy sang phải */
      width: 100%;
      /* Nút chọn chiếm toàn bộ chiều rộng */
      text-align: right;
    }

    .product-selection-details {
      width: 100%;
    }

    .detail-row {
      flex-direction: column;
      gap: 2px;
    }

    .product-name {
      margin-top: 5px;
      /* Khoảng cách sau tên */
    }
  }

  @media (max-width: 576px) {
    .modal-content {
      padding: 15px;
      width: 95%;
      /* Chiếm nhiều hơn trên màn hình rất nhỏ */
    }

    .modal-header .cart-title {
      font-size: 20px;
    }

    .filter-section {
      padding: 10px;
    }

    .btn-reset-filter span {
      display: none !important;
      /* Ẩn chữ "Hủy lọc" trên mobile rất nhỏ */
    }
  }
}
</style>