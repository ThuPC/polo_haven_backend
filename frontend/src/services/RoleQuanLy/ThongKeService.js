// @/services/RoleQuanLy/ThongKeService.js
import client from "@/utils/api";
const BASE_URL = "/api/thong-ke";

export const getThongKeHienTai = () => {
  return client.get(`${BASE_URL}/hien-tai`);
};

export const getTopProducts = (page = 0, size = 5) => {
  return client.get(`${BASE_URL}/san-pham-thinh-hanh`, {
    params: { page, size },
  });
};

export const getBarChartData = (timeframe, startDate, endDate) => {
  console.log("Gọi API bar-chart với params:", { timeframe, startDate, endDate });
  return client.get(`${BASE_URL}/bar-chart`, {
    params: { timeframe, startDate, endDate },
  });
};

export const getPieChartData = (startDate, endDate) => {
  console.log("Gọi API pie-chart với params:", { startDate, endDate });
  return client.get(`${BASE_URL}/pie-chart`, {
    params: { startDate, endDate },
  });
};