// src/services/RoleQuanLy/HoaDonService.js
import client from "@/utils/api.js";

export default {
  async getAllHoaDon(params) {
    return await client.get("/api/hoa-don/hien-thi", { params });
  },

  async getDetailHoaDon(id) {
    return await client.get(`/api/hoa-don/detail/${id}`);
  },

  async changeStatus(id) {
    return await client.put(`/api/hoa-don/thay-doi-trang-thai/${id}`);
  },

  async exportToExcel(selectedIds) {
    return await client.post("/api/hoa-don/export-excel", selectedIds, {
      responseType: "blob",
    });
  },

  async getHoaDonGanDay() {
    return await client.get('/api/hoa-don/gan-day');
  },

};
