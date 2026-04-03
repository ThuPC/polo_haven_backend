import client from "@/utils/api";
const API_BASE_URL =  '/api/v1/khach-hang/cong-khai';


export const getListSanPhamChiTiet = () => {
 return client.get(`${API_BASE_URL}/danh-sach-chi-tiet-san-pham`);
}