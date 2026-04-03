import client from "@/utils/api";

const routeConstant = "/api/chat-lieu";

export const getAllChatLieu = (page = 0, size = 5, keyword = "") => {
  return client.get(`${routeConstant}/getAll`, {
    params: { page, size, keyword },
    withCredentials: true,
  });
};

export const createChatLieu = ({ tenChatLieu }) => {
  return client.post(
    `${routeConstant}/create`,
    { tenChatLieu: tenChatLieu.trim() },
    { withCredentials: true }
  );
};

export const updateChatLieu = (id, tenChatLieu) => {
  return client.put(
    `${routeConstant}/update/${id}`,
    { tenChatLieu: tenChatLieu.trim() },
    { withCredentials: true }
  );
};

export const deleteChatLieu = (id) => {
  return client.delete(`${routeConstant}/delete/${id}`, {
    withCredentials: true,
  });
};

export const changeStatusChatLieu = (id, newStatus) => {
  return client.put(
    `${routeConstant}/change-status/${id}`,
    { trangThai: newStatus },
    { withCredentials: true }
  );
};
