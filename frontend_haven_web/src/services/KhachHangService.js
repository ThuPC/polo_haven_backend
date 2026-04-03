import client from "@/utils/api";
const API_BASE_URL =  '/api/v1/khach-hang/cong-khai';


export const danhSachDiaChi = (idKH) => {
 return client.get(`${API_BASE_URL}/danh-sach-dia-chi/${idKH}`);

}

