import client from "../../utils/api";

const routeConstant = '/giam-gia/khuyen-mai';

// === KHUYẾN MÃI ===

export const createKhuyenMai = (data) => {
  return client.post(`${routeConstant}/add`, data);
};

export const getAllKhuyenMai = (page = 0, size = 10) => {
  return client.get(`${routeConstant}?page=${page}&size=${size}`);
};

export const getKhuyenMaiById = (id) => {
  return client.get(`${routeConstant}/${id}`);
};

export const updateKhuyenMai = (id, data) => {
  return client.put(`${routeConstant}/update/${id}`, data);
};

export const deleteKhuyenMai = (id) => {
  return client.delete(`${routeConstant}/delete/${id}`);
};
export const validateBeforeToggle = (id, newStatus) => {
  return client.get(`${routeConstant}/validate-before-toggle`, {
    params: {
      id: id,
      trangThai: newStatus,
    },
  });
};


