<template>
  <div class="page-wrapper">
    <div class="page-header">
      <i class="fas fa-chart-bar p-2"></i>
      Thống kê doanh số
    </div>
    <!-- Nội dung trang của bạn ở đây -->
    <div class="page-content">
      <div class="container py-4">
        <!-- Phần Tiêu đề -->
        <header class="mb-4 d-flex justify-content-between align-items-start flex-wrap">
          <div>
            <h1 class="h4 fw-semibold text-gray-900">Tổng quan bán hàng</h1>
            <p class="text-xs text-gray-500 mb-0">Xem doanh số và tóm tắt hiện tại của bạn</p>
          </div>
          <div class="d-flex gap-2 mt-2 mt-md-0" style="min-width: 280px">
            <input type="date" class="date-input form-control" v-model="dateRange.start" @change="fetchChartData"/>
            <input type="date" class="date-input form-control" v-model="dateRange.end" @change="fetchChartData"/>
          </div>
        </header>

        <!-- Thẻ Tóm tắt Bán hàng -->
        <div class="d-flex flex-wrap gap-3 mb-4 align-items-center">
          <div v-for="(item, index) in salesSummary" :key="index" class="card flex-grow-1" style="min-width: 220px">
            <div class="card-body p-3">
              <p class="text-xs fw-semibold text-gray-600 mb-1">{{ item.title }}</p>
              <p class="h5 fw-extrabold text-gray-900 mb-1">{{ item.amount }}</p>
              <p class="text-xs text-gray-400 mb-2">{{ item.subText }}</p>
              <div class="small-green-badge">
                <i class="fas fa-arrow-up"></i> {{ item.percent }}
              </div>
            </div>
          </div>
        </div>

        <!-- Biểu đồ Doanh số và Trạng thái Đơn hàng -->
        <div class="row g-3 mb-4">
          <!-- Phần Biểu đồ Cột -->
          <div class="col-lg-8 d-flex flex-column">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <div>
                <button class="btn btn-link p-0 text-gray-600 fw-semibold border-b-indigo me-3" style="font-size: 0.75rem" @click="selectedStat = 'quantity'; updateBarChart()">Số lượng</button>
                <button class="btn btn-link p-0 text-indigo-600" style="font-size: 0.75rem" @click="selectedStat = 'revenue'; updateBarChart()">Doanh số</button>
              </div>
              <div class="d-flex gap-3 align-items-center" style="font-size: 0.75rem">
                <label class="d-flex align-items-center gap-1 cursor-pointer text-gray-700 fw-semibold mb-0">
                  <input type="radio" name="timeframe" v-model="timeFilter" value="day" @change="fetchBarChartData"/>Ngày</label>
                <label class="d-flex align-items-center gap-1 cursor-pointer text-gray-400 mb-0">
                  <input type="radio" name="timeframe" v-model="timeFilter" value="week" @change="fetchBarChartData"/>Tuần</label>
                <label class="d-flex align-items-center gap-1 cursor-pointer text-gray-400 mb-0">
                  <input type="radio" name="timeframe" v-model="timeFilter" value="month" @change="fetchBarChartData"/>Tháng</label>
              </div>
            </div>
            <div class="card flex-grow-1">
              <div class="card-body p-3 d-flex flex-column justify-content-center">
                <v-chart class="chart" :option="barChartOption" autoresize style="height: 300px;" />
              </div>
            </div>
          </div>

          <!-- Phần Biểu đồ Tròn -->
          <div class="col-lg-4 d-flex flex-column">
            <div class="card flex-grow-1">
              <div class="card-body p-3 d-flex flex-column align-items-center justify-content-center">
                <h2 class="h6 fw-semibold text-gray-900 w-100 mb-3">Danh mục</h2>
                <v-chart class="chart" :option="pieChartOption" autoresize style="height: 320px;" />
                <div class="total-orders mt-3 mb-2 fw-semibold text-gray-900">
                  Tổng số hóa đơn: {{ totalOrders }}
                </div>
                <ul class="legend-list legend-small">
                  <li v-for="(category, index) in categories" :key="index" class="legend-item legend-item-small">
                    <span class="badge-dot" :style="{ backgroundColor: category.color }"></span>
                    {{ category.name }}
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>

      <div class="row g-3">
        <div class="col-lg-8">
          <div class="card h-100 overflow-auto" style="max-height: 420px">
            <div class="card-body p-3">
              <h2 class="h6 fw-semibold text-gray-900 mb-3">
                Đơn hàng gần đây
              </h2>
              <div class="table-responsive">
                <table
                  class="table table-hover align-middle text-gray-800"
                  style="font-size: 0.85rem"
                >
                  <thead class="table-light">
                    <tr>
                      <th scope="col">ĐƠN HÀNG</th>
                      <th scope="col">TRẠNG THÁI</th>
                      <th scope="col">LOẠI</th>
                      <th scope="col">NGÀY TẠO</th>
                      <th scope="col">KHÁCH HÀNG</th>
                      <th scope="col">DOANH SỐ</th>
                      <th scope="col">CẦN TT</th>
                      <!-- <th scope="col" class="text-center">HÀNH ĐỘNG</th> -->
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(order, index) in recentOrders" :key="index">
                      <td>{{ order.id }}</td>
                      <td>
                        <span
                          class="badge"
                          :class="getBadgeClass(order.status)"
                        >
                          {{ order.status }}
                        </span>
                      </td>
                      <td>
                        <span
                          class="badge"
                          :class="getTypeBadgeClass(order.type)"
                        >
                          {{
                            order.type === "Tại Quầy" ? "Tại quầy" : "Online"
                          }}
                        </span>
                      </td>

                      <td>{{ order.createdAt }}</td>
                      <td>{{ order.customer }}</td>
                      <td>{{ order.revenue }}</td>
                      <td>{{ order.needPayment }}</td>
                      <!-- <td class="text-center">
                        <i
                          class="fas fa-eye text-secondary cursor-pointer"
                          @click="handleView(order.id)"
                        ></i>
                      </td> -->
                    </tr>
                    <tr v-if="recentOrders.length === 0">
                      <td colspan="8" class="text-center text-muted">
                        Không có đơn hàng nào gần đây.
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>

          <!-- Phần Top Sản phẩm Thịnh hành -->
          <div class="col-lg-4">
            <div class="card h-100">
              <div class="card-body p-3">
                <h2 class="h6 fw-semibold text-gray-900 mb-3">Top thịnh hành</h2>
                <table class="table table-borderless align-middle mb-0 text-gray-700" style="font-size: 0.75rem">
                  <thead>
                    <tr>
                      <th style="color: #94a3b8">PRODUCT</th>
                      <th style="color: #94a3b8">SOLD</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in topProducts" :key="item.id">
                      <td class="d-flex align-items-center gap-2">
                        <div class="bg-secondary rounded d-flex justify-content-center align-items-center" style="width: 48px; height: 48px">
                          <img v-if="item.tenAnh" :src="getImageUrl(item.tenAnh)" alt="Ảnh sản phẩm" style="width: 100%; height: 100%; object-fit: cover"/>
                        </div>
                        <span>{{ item.tenSanPham || "Không có tên" }}</span>
                      </td>
                      <td>{{ item.tongSoLuong }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getThongKeHienTai, getTopProducts, getBarChartData, getPieChartData } from "@/services/RoleQuanLy/ThongKeService";
import HoaDonService from "@/services/RoleQuanLy/HoaDonService";
import VChart from 'vue-echarts';
import { use } from 'echarts/core';
import { BarChart, PieChart } from 'echarts/charts';
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

use([BarChart, PieChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer]);

export default {
  components: {
    VChart
  },
  data() {
    return {
      salesSummary: [],
      dateRange: {
        start: '2023-01-09',
        end: '2025-01-09'
      },
      selectedStat: 'revenue',
      timeFilter: 'day',
      topProducts: [],
      categories: [
        { name: "Chờ xác nhận", color: "#b2bec3" },      // vàng nhạt
        { name: "Đã xác nhận", color: "#ffe066" },       // xám nhạt
        { name: "Chờ vận chuyển", color: "#fab1a0" },    // cam nhạt
        { name: "Đang vận chuyển", color: "#74b9ff" },   // xanh dương nhạt
        { name: "Hoàn thành", color: "#55efc4" },        // xanh lá nhạt
        { name: "Hủy", color: "#ff7675" }                // đỏ nhạt
      ],
      recentOrders: [],
      barChartOption: {
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: [] },
        yAxis: { type: 'value' },
        series: [{ type: 'bar', data: [], itemStyle: { color: '#4338ca' } }]
      },
      pieChartOption: {
        tooltip: { trigger: 'item' },
        series: [{
          type: 'pie',
          radius: '50%',
          data: [],
          itemStyle: { borderColor: '#ffffff', borderWidth: 1 },
          label: { show: false }
        }]
      },
      barChartData: { labels: [], quantities: [], revenues: [] },
      pieChartData: { labels: [], counts: [] },
      parameterId: 'default-id'
    };
  },
  mounted() {
    console.log("Component thống kê được mount.");
    this.fetchThongKe();
    this.loadTopProducts();
    this.loadRecentOrders();
    this.$nextTick(() => {
      this.fetchBarChartData();
      this.fetchPieChartData();
    });
  },
  watch: {
    selectedStat() {
      this.updateBarChart();
    },
    dateRange: {
      handler() {
        this.fetchChartData();
      },
      deep: true
    }
  },
  computed: {
    totalOrders() {
      const counts = Array.isArray(this.pieChartData.counts) ? this.pieChartData.counts : [];
      return counts.reduce((sum, val) => sum + (Number(val) || 0), 0);
    }
  },
  methods: {
    async fetchThongKe() {
      try {
        const res = await getThongKeHienTai();
        const data = res.data;
        if (!data) {
          console.warn("Không có dữ liệu thống kê trả về!");
          return;
        }
        this.salesSummary = [
          {
            title: "Doanh số hôm nay",
            amount: this.formatCurrency(data.doanhSoHomNay),
            subText: `So với ${this.formatDate(data.startDate)}`,
            percent: `${data.tiLeHomNay}%`,
          },
          {
            title: "Doanh số tuần này",
            amount: this.formatCurrency(data.doanhSoTuanNay),
            subText: "So với tuần trước",
            percent: `${data.tiLeTuan}%`,
          },
          {
            title: "Doanh số tháng này",
            amount: this.formatCurrency(data.doanhSoThangNay),
            subText: "So với tháng trước",
            percent: `${data.tiLeThang}%`,
          },
        ];
        this.dateRange = {
          start: data.startDate,
          end: data.endDate,
        };
      } catch (err) {
        console.error("Lỗi khi lấy thống kê:", err);
      }
    },
    getImageUrl(tenAnh) {
      return tenAnh ? `http://localhost:8080/uploads/san_pham/${tenAnh}` : "";
    },
    async loadTopProducts(page = 0, size = 5) {
      console.log("Đang gọi API getTopProducts với page =", page, "size =", size);
      try {
        const res = await getTopProducts(page, size);
        this.topProducts = res.data;
        if (!Array.isArray(this.topProducts) || this.topProducts.length === 0) {
          console.warn(
            "Không có sản phẩm nào trong danh sách top sản phẩm."
          );
          return;
        }
        this.topProducts.forEach((item, index) => {
          console.log(
            `#${index + 1} Tên sản phẩm: ${item.tenSanPham || "Không có"} | Ảnh: ${
              item.tenAnh || "Không có"
            } | Số lượng bán: ${
              item.tongSoLuong !== undefined ? item.tongSoLuong : "Không có"
            }`
          );
        });
      } catch (error) {
        console.error("Lỗi khi lấy top sản phẩm:", error);
      }
    },
    async loadRecentOrders() {
      try {
        const res = await HoaDonService.getAllHoaDon({
          page: 0,
          size: 10,
          sort: "ngayTao,desc",
        });

        console.log("Response từ API:", res);

        const data = res.data.content || [];
        const trangThaiLabels = [
          "Chờ xác nhận",
          "Đã xác nhận",
          "Chờ vận chuyển",
          "Đang vận chuyển",
          "Đã giao hàng",
          "Hoàn thành",
          "Hủy",
          "Trả hàng",
          "Đang trả hàng",
          "Trả hàng thành công",
          "Từ chối trả hàng",
        ];
        this.recentOrders = data.map((item, index) => {
          const statusLabel = trangThaiLabels[item.trangThai] || "Không rõ";
          const statusColor = this.getStatusColor(item.trangThai);
          const type = item.loaiHoaDon === 0 ? "Tại Quầy" : "Online";
          const typeColor = item.loaiHoaDon === 0 ? "text-[#4338CA]" : "text-[#F59E0B]";
          const createdAt = new Date(item.ngayTao).toLocaleString("vi-VN");
          const customer = item.tenKhachHang || "Khách lẻ";
          const revenue = this.formatCurrency(item.tongTienSauKhiGiam);
          const needPayment = this.formatCurrency(item.tienKhachPhaiTra);

          console.log(`Đơn hàng [${index}]`, {
            id: `#${item.maHoaDon}`,
            statusLabel,
            statusColor,
            type,
            typeColor,
            createdAt,
            customer,
            revenue,
            needPayment,
          });
          return {
            id: `#${item.maHoaDon}`,
            status: statusLabel,
            statusColor,
            type,
            typeColor,
            createdAt,
            customer,
            revenue,
            needPayment,
          };
        });
      } catch (error) {
        console.error("Lỗi khi tải đơn hàng gần đây:", error);
      }
    },
    getStatusColor(status) {
      switch (status) {
        case 0: return "bg-warning text-white";
        case 1: return "bg-info text-white";
        case 2:
        case 3: return "bg-primary text-white";
        case 4:
        case 5: return "bg-success text-white";
        case 6:
        case 7:
        case 10: return "bg-danger text-white";
        case 8:
        case 9: return "bg-secondary text-white";
        default: return "bg-dark text-white";
      }
    },
    formatCurrency(value) {
      if (value == null || isNaN(value)) {
        console.warn("Giá trị không hợp lệ cho formatCurrency:", value);
        return "0đ";
      }
      return new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND",
        minimumFractionDigits: 0,
      }).format(value);
    },
    getBadgeClass(statusText) {
      switch (statusText) {
        case "Chờ xác nhận": return "badge-pending";
        case "Đã xác nhận": return "badge-confirmed";
        case "Chờ vận chuyển":
        case "Đang vận chuyển": return "badge-shipping";
        case "Đã giao hàng":
        case "Hoàn thành":
        case "Trả hàng thành công": return "badge-complete";
        case "Hủy":
        case "Trả hàng":
        case "Từ chối trả hàng": return "badge-cancelled";
        case "Đang trả hàng": return "badge-confirmed";
        default: return "badge-confirmed";
      }
    },
    getTypeBadgeClass(typeText) {
      return typeText === "Tại Quầy" ? "badge-counter" : "badge-online";
    },
    formatDate(dateStr) {
      if (!dateStr) {
        console.warn("Không có ngày để format:", dateStr);
        return "";
      }
      const date = new Date(dateStr);
      return date.toLocaleDateString("vi-VN");
    },
    handleView(orderId) {
      console.log("Xem chi tiết đơn hàng:", orderId);
    },
    async fetchBarChartData() {
      try {
        if (!this.dateRange.start || !this.dateRange.end) {
          console.error("Ngày bắt đầu hoặc kết thúc không hợp lệ:", this.dateRange);
          return;
        }
        const startDate = this.dateRange.start;
        const endDate = this.dateRange.end;
        const timeframe = this.timeFilter.toUpperCase();
        console.log("Gọi API bar-chart với:", { timeframe, startDate, endDate });
        const res = await getBarChartData(timeframe, startDate, endDate);
        this.barChartData = res.data;
        console.log("Dữ liệu từ API:", res.data);
        this.updateBarChart();
      } catch (error) {
        console.error("Lỗi khi lấy dữ liệu biểu đồ cột:", {
          message: error.message,
          response: error.response ? {
            status: error.response.status,
            data: error.response.data
          } : null,
          request: error.request,
          code: error.code
        });
      }
    },
    async fetchPieChartData() {
      try {
        if (!this.dateRange.start || !this.dateRange.end) {
          console.error("Ngày bắt đầu hoặc kết thúc không hợp lệ:", this.dateRange);
          return;
        }
        const startDate = this.dateRange.start;
        const endDate = this.dateRange.end;
        console.log("Gọi API pie-chart với:", { startDate, endDate, parameterId: this.parameterId });
        const res = await getPieChartData(startDate, endDate, this.parameterId);
        // Đảm bảo dữ liệu là mảng
        this.pieChartData = {
          labels: Array.isArray(res.data?.labels) ? res.data.labels : [],
          counts: Array.isArray(res.data?.counts) ? res.data.counts : [],
        };
        console.log("Dữ liệu pie-chart từ API:", this.pieChartData);
        this.updatePieChart();
      } catch (error) {
        console.error("Lỗi khi lấy dữ liệu biểu đồ tròn:", {
          message: error.message,
          response: error.response ? {
            status: error.response.status,
            data: error.response.data
          } : null,
          request: error.request,
          code: error.code
        });
      }
    },
    updateBarChart() {
      this.barChartOption = {
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: this.barChartData.labels,
          name: this.timeFilter === 'day' ? 'Ngày' : this.timeFilter === 'week' ? 'Tuần' : 'Tháng'
        },
        yAxis: {
          type: 'value',
          name: this.selectedStat === 'quantity' ? 'Số lượng' : 'Doanh số (VND)'
        },
        series: [{
          type: 'bar',
          data: this.selectedStat === 'quantity' ? this.barChartData.quantities : this.barChartData.revenues,
          itemStyle: { color: '#4338ca' }
        }]
      };
    },
    updatePieChart() {
      // Nếu labels rỗng, gán dữ liệu mẫu để test
      let labels = Array.isArray(this.pieChartData.labels) ? this.pieChartData.labels : [];
      let counts = Array.isArray(this.pieChartData.counts) ? this.pieChartData.counts : [];
      if (labels.length === 0 && counts.length > 0) {
        labels = this.categories.slice(0, counts.length).map(c => c.name);
      }
      this.pieChartOption = {
        tooltip: { trigger: 'item' },
        series: [{
          type: 'pie',
          radius: '50%',
          data: labels.map((label, index) => ({
            value: counts[index] ?? 0,
            name: label,
            itemStyle: { color: this.categories[index]?.color || '#000000' }
          })),
          itemStyle: { borderColor: '#ffffff', borderWidth: 1 },
          label: { show: false }
        }]
      };
    },
    fetchChartData() {
      this.fetchBarChartData();
      this.fetchPieChartData();
    }
  }
};
</script>

<style scoped>
body {
  background-color: #f8fafc;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    "Helvetica Neue", Arial, sans-serif;
  color: #1e293b;
}
.card {
  border: 1px solid #f1f5f9;
  box-shadow: 0 1px 2px rgb(0 0 0 / 0.05);
  border-radius: 0.5rem;
}
.small-green-badge {
  font-size: 10px;
  font-weight: 600;
  color: #22c55e;
  background-color: #dcfce7;
  border-radius: 0.25rem;
  padding: 0.125rem 0.5rem;
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
}
.small-green-badge i {
  margin-right: 0.25rem;
}
.text-xs {
  font-size: 0.75rem;
}
.font-extrabold {
  font-weight: 800;
}
.font-semibold {
  font-weight: 600;
}
.text-indigo {
  color: #4338ca;
}
.text-indigo-light {
  color: #a5b4fc;
}
.text-gray-400 {
  color: #94a3b8;
}
.text-gray-500 {
  color: #64748b;
}
.text-gray-600 {
  color: #475569;
}
.text-gray-900 {
  color: #0f172a;
}
.border-b-indigo {
  border-bottom: 2px solid #4338ca;
}
.cursor-pointer {
  cursor: pointer;
}
.table thead th {
  border-bottom: 1px solid #e2e8f0;
  font-weight: 600;
  text-transform: uppercase;
  font-size: 0.75rem;
  color: #94a3b8;
  padding: 0.5rem 0.75rem;
  vertical-align: bottom;
  white-space: nowrap;
}
.table tbody td {
  font-size: 0.75rem;
  padding: 0.5rem 0.75rem;
  vertical-align: middle;
  color: #475569;
  white-space: nowrap;
}
.table tbody tr {
  border-bottom: 1px solid #f1f5f9;
}
.legend-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 24px;
  list-style: none;
  padding: 0;
  margin: 16px 0 0 0;
}
.legend-list.legend-small {
  margin-top: 8px;
  gap: 4px 12px;
}
.legend-item {
  display: flex;
  align-items: center;
  font-size: 0.85rem;
  color: #475569;
  margin-bottom: 4px;
}
.legend-item.legend-item-small {
  font-size: 0.72rem;
  margin-bottom: 2px;
}
.badge-dot {
  width: 0.6rem;
  height: 0.6rem;
  margin-right: 0.35rem;
}
.bg-indigo {
  background-color: #4338ca;
}
.bg-red {
  background-color: #ef4444;
}
.bg-yellow {
  background-color: #eab308;
}
.bg-sky {
  background-color: #0ea5e9;
}
.bg-orange {
  background-color: #f97316;
}
.bg-emerald {
  background-color: #34d399;
}
.date-input {
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border: 1px solid #cbd5e1;
  border-radius: 0.375rem;
  color: #475569;
  min-width: 140px;
}
.date-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 1px #3b82f6;
}
.btn-link {
  color: #4338ca;
  font-weight: 600;
  padding: 0;
  border-bottom: 2px solid #4338ca;
  font-size: 0.75rem;
  background: none;
  text-decoration: none;
}
.btn-link:hover {
  color: #4338ca;
  text-decoration: none;
}
.text-indigo-600 {
  color: #4338ca !important;
}
.text-yellow-500 {
  color: #eab308 !important;
}
.text-red-600 {
  color: #ef4444 !important;
}
.text-sky-500 {
  color: #0ea5e9 !important;
}
.text-orange-400 {
  color: #f97316 !important;
}
.text-emerald-400 {
  color: #34d399 !important;
}
.fw-monospace {
  font-family: monospace, monospace;
}
.cursor-pointer {
  cursor: pointer;
}
.badge {
  display: inline-block;
  padding: 4px 12px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 999px;
  text-align: center;
  min-width: 80px;
}

.badge-counter {
  background-color: #e0e0e0;
  color: #333;
}

.badge-online {
  background-color: #d1eaff;
  color: #007bff;
}

/* css dữ liệu trạng thái */
.badge {
  display: inline-block;
  padding: 6px 14px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 9999px; /* Bo tròn hoàn toàn */
  text-align: center;
  white-space: nowrap;
  vertical-align: middle;
  line-height: 1.4;
}

/* Màu theo trạng thái */
.badge {
  display: inline-block;
  padding: 6px 14px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 9999px;
  text-align: center;
  white-space: nowrap;
  vertical-align: middle;
  line-height: 0.7;
}
.badge-complete {
  background-color: #d1f2d9;
  color: #1a7f37;
}
.badge-pending {
  background-color: #fff3cd;
  color: #856404;
}
.badge-cancelled {
  background-color: #f8d7da;
  color: #721c24;
}
.badge-shipping {
  background-color: #cce5ff;
  color: #004085;
}
.badge-confirmed {
  background-color: #e2e3e5;
  color: #383d41;
}
@media (max-width: 991.98px) {
  .flex-lg-row {
    flex-direction: column !important;
  }
  .w-lg-1-3 {
    width: 100% !important;
  }
}
.chart {
  width: 100%;
  /* Tăng chiều cao nếu muốn to hơn nữa */
}
.total-orders {
  font-size: 0.95rem;
  color: #1e293b;
  text-align: center;
}
.page-wrapper {
  background: #fbf9f9;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  margin: 24px auto;
  padding: 0;
  /* max-width: 1200px; */
  max-width: 1550px;

  min-height: 80vh;
  overflow: hidden;
}

.page-header {
  background: linear-gradient(135deg, #66ea8b 0%, #4ba27b 100%);
  color: white;
  padding: 24px 32px;
  font-size: 22px;
  font-weight: 600;
  display: flex;
  align-items: center;
  border-radius: 16px 16px 0 0;
}

.page-content {
  padding: 32px;
}
</style>