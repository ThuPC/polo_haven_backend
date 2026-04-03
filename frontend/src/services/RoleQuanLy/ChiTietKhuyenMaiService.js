import client from "../../utils/api";

const chiTietRoute = '/giam-gia/chi-tiet-khuyen-mai';

// Thêm danh sách chi tiết khuyến mãi
export const addChiTietKhuyenMai = (listData) => {
  console.log("chay vao");
  console.log("Payload gửi lên backend:", listData);
  return client.post("http://localhost:8080/giam-gia/chi-tiet-khuyen-mai/add", listData);
};

// Lấy tất cả chi tiết sản phẩm để áp dụng khuyến mãi
export const getAllChiTietSanPham = () => {
  return client.get(`${chiTietRoute}/all-chi-tiet-san-pham`);
};

// Lấy danh sách sản phẩm áp dụng khuyến mãi theo ID khuyến mãi
export const getSanPhamApDungKhuyenMai = (khuyenMaiId) => {
  return client.get(`${chiTietRoute}/san-pham-ap-dung/${khuyenMaiId}`);
};

export const deleteChiTietKhuyenMai = (id) => {
  return client.delete(`http://localhost:8080/giam-gia/chi-tiet-khuyen-mai/${id}`);
};