import client from "@/utils/api";

const routeConstantPrivate = '/api/v1/quan-ly/ban-hang-tai-quay';
const routeConstant = '/api/v1/khach-hang/cong-khai';

export const getListVoucherPublic = () => {
  return client.get(`${routeConstant}/danh-sach-ma-giam-gia/cong-khai`);
};

export const getListVoucherPrivate = (idKH) => {
  return client.get(`${routeConstantPrivate}/danh-sach-ma-giam-gia/${idKH}`);
};



