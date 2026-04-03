import client from "../../utils/api";

const routeConstant = '/api/v1/quan-ly/ban-hang-tai-quay';



export const getListSP = () => {
  return client.get(`${routeConstant}/danh-sach-san-pham`);
};


export const getListKhachHang = () => {
  return client.get(`${routeConstant}/danh-sach-khach-hang`);
};


export const getListVoucherPublic = () => {
  return client.get(`${routeConstant}/danh-sach-ma-giam-gia/cong-khai`);
};

export const getListVoucherPrivate = (idKH) => {
  return client.get(`${routeConstant}/danh-sach-ma-giam-gia/${idKH}`);
};



export const createInvoiceApi = (invoicePayload) => {
  console.log(invoicePayload);
  return client.post(`${routeConstant}`, invoicePayload);
};



export const createForPayment = (orderPayload) => {
  return client.post(`${routeConstant}/create-for-payment`,orderPayload);
};


export const createPayment = (vnpayUrlPayload) => {
  console.log(vnpayUrlPayload)
  return client.post('http://localhost:8080/api/v1/payment/create-payment', vnpayUrlPayload);

};

export const orderStatusVnpay = (orderId) => {
  return client.get(`${routeConstant}/status/${orderId}`);

};

// export const createForPayment = (orderPayload) => {
//   return client.post(`${routeConstant}/create-for-payment`, orderPayload);
// };
// BanHangService.js
export const getFilteredProducts = async (filter) => {
    try {
        const response = await client.post(`${routeConstant}/products/filter`, filter);
        if (response.status === 200) {
            console.log('API response:', response.data);
            return response.data;
        } else {
            throw new Error(`API trả về mã trạng thái ${response.status}`);
        }
    } catch (error) {
        console.error('Lỗi khi gọi API lọc sản phẩm:', error.response?.data || error.message);
        throw new Error('Không thể lấy danh sách sản phẩm đã lọc');
    }
};

export const getFilterOptions = async () => {
    try {
        const response = await client.get(`${routeConstant}/filter-options`);
        return response.data;
    } catch (error) {
        throw new Error('Không thể lấy danh sách bộ lọc');
    }
};


