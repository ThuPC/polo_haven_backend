import client from "../../utils/api";

const routeConstant = '/api/v1/quan-ly/phieu-giam-gia';


export const createVoucher = (data) => {
  return client.post(`${routeConstant}`, data);
};


export const getActiveVouchers = () => {
  return client.get(`${routeConstant}`);
};

export const getAllVouchers = () => {
  return client.get(`${routeConstant}/full`);
};


export const updateVoucher = (data) => {
  return client.put(`${routeConstant}`, data);
};


export const deleteVoucher = (id) => {
  return client.delete(`${routeConstant}/${id}`);
};

export const toggleVoucherStatus = (id) => {
  return client.put(`${routeConstant}/thay-doi-trang-thai/${id}`);
};
