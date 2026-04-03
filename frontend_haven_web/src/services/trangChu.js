import client from "@/utils/api";
const API_BASE_URL =  '/api/v1/khach-hang/cong-khai';

export const getListArrivalProduct = () => {
 return client.get(`${API_BASE_URL}/danh-sach-san-pham/moi-ve`);
}


export const getListBestSelling = () => {
 return client.get(`${API_BASE_URL}/danh-sach-san-pham/noi-bat`);
}


