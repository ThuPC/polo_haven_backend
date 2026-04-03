import client from "@/utils/api";
const API_BASE_URL = "/api/v1/khach-hang/cong-khai";

export const getListSanPham = () => {
  return client.get(`${API_BASE_URL}/danh-sach-san-pham`);
};

export const getListSanPhamFill = (filters) => {
  console.log(
    ">>> Sending POST /danh-sach-san-pham-fill with filters:",
    filters
  );
  return client
    .post(`${API_BASE_URL}/danh-sach-san-pham-fill`, filters)
    .then((res) => {
      console.log(">>> Response from backend:", res.data);
      return res;
    })
    .catch((err) => {
      console.error(">>> Axios error:", err.message);
      if (err.response) {
        console.error(">>> Error response status:", err.response.status);
        console.error(">>> Error response data:", err.response.data);
      }
      throw err;
    });
};

export const getSanPhamChiTiet = (id) => {
  return client.get(`${API_BASE_URL}/danh-sach-san-pham/chi-tiet/${id}`);
};

// API để lấy danh sách màu sắc
export const getAllMauSac = () => {
  return client.get(`/api/mau-sac/get-all-ten`);
};

// API để lấy danh sách kích thước
export const getAllKichThuoc = () => {
  return client.get(`/api/kich-thuoc/get-all-active`);
};

// API để lấy danh sách chất liệu
export const getAllChatLieu = () => {
  return client.get(`/api/chat-lieu/get-all-ten`);
};
