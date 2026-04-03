import client from "@/utils/api";

const routeConstant = "/api/thuong-hieu";

export const getAllThuongHieu = (page = 0, size = 5, keyword = "") => {
  return client.get(`${routeConstant}/getAll`, {
    params: { page, size, keyword },
    withCredentials: true,
  });
};

export const createThuongHieu = (tenThuongHieu) => {
  return client.post(
    `${routeConstant}/create`,
    { tenThuongHieu: tenThuongHieu.trim() },
    { withCredentials: true }
  );
};

export const updateThuongHieu = (id, tenThuongHieu) => {
  return client.put(
    `${routeConstant}/update/${id}`,
    { tenThuongHieu: tenThuongHieu.trim() },
    { withCredentials: true }
  );
};

export const deleteThuongHieu = (id) => {
  return client.delete(`${routeConstant}/delete/${id}`, {
    withCredentials: true,
  });
};

export const changeStatusThuongHieu = (id, newStatus) => {
  return client.put(
    `${routeConstant}/change-status/${id}`,
    { trangThai: newStatus },
    { withCredentials: true }
  );
};



