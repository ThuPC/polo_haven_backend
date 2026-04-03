import client from "@/utils/api";

const routeConstant = "/api/mau-sac";

export const getAllMauSac = (page = 0, size = 5, keyword = "") => {
  return client.get(`${routeConstant}/getAll`, {
    params: { page, size, keyword },
    withCredentials: true,
  });
};

export const createMauSac = ({ tenMau, maMauSac }) => {
  return client.post(
    `${routeConstant}/create`,
    {
      tenMau: tenMau.trim(),
      maMauSac: maMauSac.trim(),
    },
    { withCredentials: true }
  );
};

export const updateMauSac = (id, { tenMau, maMauSac }) => {
  return client.put(
    `${routeConstant}/update/${id}`,
    { tenMau: tenMau.trim(), maMauSac: maMauSac.trim() },
    { withCredentials: true }
  );
};

export const deleteMauSac = (id) => {
  return client.delete(`${routeConstant}/delete/${id}`, {
    withCredentials: true,
  });
};

export const changeStatusMauSac = (id, newStatus) => {
  return client.put(
    `${routeConstant}/change-status/${id}`,
    { trangThai: newStatus },
    { withCredentials: true }
  );
};
