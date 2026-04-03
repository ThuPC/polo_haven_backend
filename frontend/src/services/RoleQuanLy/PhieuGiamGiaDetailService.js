import client from "../../utils/api";

const routeConstant = '/api/v1/quan-ly/phieu-giam-gia';


export const createDetailVoucher = (data) => {
  return client.post(`${routeConstant}/tao-ma`, data);
};


export const getListVoucherHieuLuc =(idVoucher)=>{
  return client.get(`${routeConstant}/danh-sach-ma/hieu-luc/${idVoucher}`);
}

export const getListVoucherDaSuDung =(idVoucher)=>{
  return client.get(`${routeConstant}/danh-sach-ma/da-su-dung/${idVoucher}`);
}

export const getListVoucherTatCa =(idVoucher)=>{
  return client.get(`${routeConstant}/danh-sach-ma/tat-ca/${idVoucher}`);
}

export const getListKhachHang = ()=>{
  return client.get(`${routeConstant}/danh-sach-khach-hang`);
}


export const DeleteVoucherDetail = (data) => {
  return client.delete(`${routeConstant}/xoa-ma/${data}`);
};

