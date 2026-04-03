import client from "@/utils/api"; 

const BASE_ROUTE = "/api/san-pham";
const CTSP_ROUTE = "/api/san-pham-chi-tiet";
// const UPLOAD_ROUTE = "/api/upload";

// SẢN PHẨM
export const getAllSanPham = (params) => {
  return client.get(`${BASE_ROUTE}/getAll`, { params });
};

export const updateSanPham = (id, payload) => {
  return client.put(`${BASE_ROUTE}/update/${id}`, payload);
};

export const changeSanPhamStatus = (id, payload) => {
  return client.put(`${BASE_ROUTE}/change-status/${id}`, payload);
};

export const deleteSanPham = (id) => {
  return client.delete(`${BASE_ROUTE}/delete/${id}`);
};

export const createSanPham = (formData) => {
  return client.post(`${BASE_ROUTE}/create`, formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

export const getComboboxData = () => {
  return client.get(`${BASE_ROUTE}/combobox-data`);
};

// CHI TIẾT SẢN PHẨM
export const createChiTietSanPham = (payloadList) => {
  return client.post(`${CTSP_ROUTE}/batch`, payloadList);
};

export const getCTSPBySanPham = (idSanPham) => {
  return client.get(`${CTSP_ROUTE}/by-san-pham/${idSanPham}`);
};

export const updateChiTietSanPham = (id, payload) => {
  return client.put(`${CTSP_ROUTE}/${id}`, payload, {
    withCredentials: true,
  });
};

export const uploadImage = (file) => {
  const formData = new FormData();
  formData.append("file", file); // key chính xác là "file"

  return client.post("/api/upload", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
    withCredentials: true,
  });
};

export const getThuongHieuList = () => {
  return client.get(`/api/thuong-hieu/get-all-ten`);
};

export const getChatLieuList = () => {
  return client.get(`/api/chat-lieu/get-all-ten`);
};

export const getXuatXuList = () => {
  return client.get(`/api/xuat-xu/get-all-ten`);
};

export const deleteCTSP = (id) => {
  return client.delete(`${CTSP_ROUTE}/${id}`);
};


