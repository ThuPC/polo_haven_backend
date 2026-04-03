import client from "@/utils/api";

const routeConstant = "/api/xuat-xu";

export const getAllXuatXu = (page = 0, size = 5, keyword = "") => {
  return client.get(`${routeConstant}/getAll`, {
    params: { page, size, keyword },
    withCredentials: true,
  });
};

export const createXuatXu = (noiXuatXu) => {
  return client.post(
    `${routeConstant}/create`,
    { noiXuatXu: noiXuatXu.trim() },
    { withCredentials: true }
  );
};

export const updateXuatXu = (id, noiXuatXu) => {
  return client.put(
    `${routeConstant}/update/${id}`,
    { noiXuatXu: noiXuatXu.trim() },
    { withCredentials: true }
  );
};

export const deleteXuatXu = (id) => {
  return client.delete(`${routeConstant}/delete/${id}`, {
    withCredentials: true,
  });
};

export const changeStatusXuatXu = (id, newStatus) => {
  return client.put(
    `${routeConstant}/change-status/${id}`,
    { trangThai: newStatus },
    { withCredentials: true }
  );
};

export const getAllTenXuatXu = () => {
  return client.get(`${routeConstant}/get-all-ten`, {
    withCredentials: true,
  });
};

