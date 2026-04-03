import client from "@/utils/api";

const routeConstant = "/api/kich-thuoc";

export const getAllKichThuoc = (page = 0, size = 5, keyword = "") => {
  return client.get(`${routeConstant}/getAll`, {
    params: { page, size, keyword },
    withCredentials: true,
  });
};

export const createKichThuoc = ({ size }) => {
  return client.post(
    `${routeConstant}/create`,
    { size: size.trim() },
    { withCredentials: true }
  );
};

export const updateKichThuoc = (id, size) => {
  return client.put(
    `${routeConstant}/update/${id}`,
    { size: size.trim() },
    { withCredentials: true }
  );
};

export const deleteKichThuoc = (id) => {
  return client.delete(`${routeConstant}/delete/${id}`, {
    withCredentials: true,
  });
};

export const changeStatusKichThuoc = (id, newStatus) => {
  return client.put(
    `${routeConstant}/change-status/${id}`,
    { trangThai: newStatus },
    { withCredentials: true }
  );
};