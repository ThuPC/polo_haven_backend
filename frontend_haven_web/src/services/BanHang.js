import client from "@/utils/api";
const API_BASE_URL =  '/api/v1/khach-hang/online';


export const createInvoice = (payLoad) => {
 return client.post(`${API_BASE_URL}`, payLoad);
}


export const createForPayment = (orderPayload) => {
    console.log("vào hàm service createForPayment")
  return client.post(`${API_BASE_URL}/create-for-payment`,orderPayload);
};


export const createPayment = (vnpayUrlPayload) => {
  console.log(vnpayUrlPayload)
  return client.post('http://localhost:8080/api/v1/payment/create-payment', vnpayUrlPayload);

};

export const orderStatusVnpay = (orderId) => {
  return client.get(`${API_BASE_URL}/status/${orderId}`);

};

