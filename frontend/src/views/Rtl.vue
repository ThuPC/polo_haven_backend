<script setup>
import { onBeforeMount, onBeforeUnmount } from "vue";
import { useStore } from "vuex";
import MiniStatisticsCard from "@/examples/Cards/MiniStatisticsCard.vue";
import DeveloperCard from "./components/DeveloperCard.vue";
import RocketCard from "./components/RocketCard.vue";
import ActiveUsersChart from "@/examples/Charts/ActiveUsersChart.vue";
import GradientLineChart from "@/examples/Charts/GradientLineChart.vue";
import TimelineList from "@/examples/Cards/TimelineList.vue";
import TimelineItem from "@/examples/Cards/TimelineItem.vue";
import ProjectCard from "./components/ProjectCard.vue";

import logoXD from "@/assets/img/small-logos/logo-xd.svg";
import logoAtlassian from "@/assets/img/small-logos/logo-atlassian.svg";
import logoSlack from "@/assets/img/small-logos/logo-slack.svg";
import logoSpotify from "@/assets/img/small-logos/logo-spotify.svg";
import logoJira from "@/assets/img/small-logos/logo-jira.svg";
import logoInvision from "@/assets/img/small-logos/logo-invision.svg";

import team1 from "@/assets/img/team-1.jpg";
import team2 from "@/assets/img/team-2.jpg";
import team3 from "@/assets/img/team-3.jpg";
import team4 from "@/assets/img/team-4.jpg";

const store = useStore();

onBeforeMount(() => {
  store.state.isRTL = true;
  document.querySelector("html").setAttribute("lang", "ar");
  document.querySelector("html").setAttribute("dir", "rtl");
  document.querySelector("#app").classList.add("rtl");
});
onBeforeUnmount(() => {
  store.state.isRTL = false;
  document.querySelector("html").removeAttribute("lang");
  document.querySelector("html").removeAttribute("dir");
  document.querySelector("#app").classList.remove("rtl");
});
</script>
<template>
  <div class="py-4 container-fluid">
    <div class="row">
      <div class="col-lg-3 col-sm-6 mb-lg-0">
        <mini-statistics-card
            class="p-0 bg-white"
            :title="{ text: 'Tiền hôm nay', color: 'text-dark opacity-7' }"
            :value="{ text: '$53,000', color: 'text-dark' }"
            :icon="{
            component: 'ni ni-money-coins text-white',
            background: 'bg-gradient-success ',
          }"
            :percentage="{ value: '55%+', color: 'text-success' }"
        />
      </div>
      <div class="col-lg-3 col-sm-6 mb-lg-0 mb-4">
        <mini-statistics-card
            class="p-0 bg-white"
            :title="{ text: 'Người dùng hôm nay', color: 'text-dark opacity-7' }"
            :value="{ text: '2,300', color: 'text-dark' }"
            :icon="{
            component: 'text-white ni ni-world',
            background: 'bg-gradient-success ',
          }"
            :percentage="{ value: '+33%', color: 'text-success' }"
        />
      </div>
      <div class="col-lg-3 col-sm-6 mb-lg-0 mb-4">
        <mini-statistics-card
            class="p-0 bg-white"
            :title="{ text: 'Khách hàng mới', color: 'text-dark opacity-7' }"
            :value="{ text: '+3,462', color: 'text-dark' }"
            :icon="{
            component: 'ni ni-paper-diploma text-white',
            background: 'bg-gradient-success ',
          }"
            :percentage="{ value: '-2%', color: 'text-danger' }"
        />
      </div>
      <div class="col-lg-3 col-sm-6 mb-lg-0 mb-4">
        <mini-statistics-card
            class="p-0 bg-white"
            :title="{ text: 'Doanh số', color: 'text-dark opacity-7' }"
            :value="{ text: '$103,430', color: 'text-dark' }"
            :icon="{
            component: 'ni ni-cart text-white',
            background: 'bg-gradient-success ',
          }"
            :percentage="{ value: '+5%', color: 'text-success' }"
            rowReverse
        />
      </div>
    </div>
    <div class="row">
      <div class="col-lg-7 mb-lg-0 mb-4">
        <developer-card />
      </div>
      <div class="col-lg-5">
        <rocket-card />
      </div>
    </div>
    <div class="mt-4 row">
      <div class="col-lg-5 mb-lg-0 mb-4">
        <active-users-chart />
      </div>
      <div class="col-lg-7">
        <gradient-line-chart
            id="chart-line-rtl"
            title="Tổng quan doanh số"
            description="<i class='fa fa-arrow-up text-success'></i>
      <span class='font-weight-bold'>4% nhiều hơn</span> trong năm 2021"
            :chart="{
            labels: [
              'Tháng 4',
              'Tháng 5',
              'Tháng 6',
              'Tháng 7',
              'Tháng 8',
              'Tháng 9',
              'Tháng 10',
              'Tháng 11',
              'Tháng 12',
            ],
            datasets: [
              {
                label: 'Ứng dụng di động',
                data: [50, 40, 300, 220, 500, 250, 400, 230, 500],
              },
            ],
          }"
        />
      </div>
    </div>
    <div class="row mt-4">
      <div class="col-lg-8 col-md-6 mb-md-0 mb-4">
        <project-card
            title="Dự án"
            description='<i class="fa fa-check text-info" aria-hidden="true"></i>
            <span class="font-weight-bold ms-1">Hoàn thành 30</span>
            tháng này'
            :headings="['Dự án', 'Thành viên', 'Ngân sách', 'Hoàn thành']"
            :rows="[
            {
              logo: logoXD,
              tool: 'Phiên bản Material XD',
              teamMembers: [team1, team2, team3, team4],
              price: '$14,000',
              progress: 60,
            },
            {
              logo: logoAtlassian,
              tool: 'Thêm lộ trình tiến độ vào ứng dụng nội bộ',
              teamMembers: [team1, team2],
              price: '$3,000',
              progress: 10,
            },
            {
              logo: logoSlack,
              tool: 'Sửa lỗi nền tảng',
              teamMembers: [team2, team4],
              price: 'Không xác định',
              progress: 100,
            },
            {
              logo: logoSpotify,
              tool: 'Ra mắt ứng dụng di động của chúng tôi',
              teamMembers: [team1, team2, team3, team4],
              price: '$20,500',
              progress: 60,
            },
            {
              logo: logoJira,
              tool: 'Thêm trang định giá mới',
              teamMembers: [team1],
              price: '$500',
              progress: 25,
            },
            {
              logo: logoInvision,
              tool: 'Thiết kế lại cửa hàng trực tuyến mới',
              teamMembers: [team1, team4],
              price: '$2,000',
              progress: 40,
            },
          ]"
            :action="[
            {
              route: 'javascript:;',
              label: 'Hành động',
            },
            {
              route: 'javascript:;',
              label: 'Hành động khác',
            },
            {
              route: 'javascript:;',
              label: 'Một cái gì đó khác',
            },
          ]"
        />
      </div>
      <div class="col-lg-4 col-md-6">
        <timeline-list
            class="h-100"
            title="Tổng quan đơn hàng"
            description="<i class='fa fa-arrow-up text-success' aria-hidden='true'></i>
        <span class='font-weight-bold'>24%</span> tháng này"
        >
          <timeline-item
              :icon="{ component: 'ni ni-bell-55', color: 'success' }"
              title="$2400, thay đổi thiết kế"
              date-time="22 tháng 12, 7:20 PM"
          />
          <TimelineItem
              :icon="{ component: 'ni ni-html5', color: 'danger' }"
              title="Đơn hàng mới # 1832412"
              date-time="21 tháng 12, 11 PM"
          />
          <TimelineItem
              :icon="{ component: 'ni ni-cart', color: 'info' }"
              title="Thanh toán máy chủ cho tháng 4"
              date-time="21 tháng 12, 9:34 PM"
          />
          <TimelineItem
              :icon="{ component: 'ni ni-credit-card', color: 'warning' }"
              title="Thêm thẻ mới cho đơn hàng #4395133"
              date-time="20 tháng 12, 2:20 AM"
          />
          <TimelineItem
              :icon="{ component: 'ni ni-key-25', color: 'primary' }"
              title="Mở gói cho phát triển"
              date-time="18 tháng 12, 4:54 AM"
          />
          <TimelineItem
              :icon="{ component: 'ni ni-money-coins', color: 'dark' }"
              title="Đơn hàng mới # 9583120"
              date-time="17 tháng 12"
          />
        </timeline-list>
      </div>
    </div>
  </div>
</template>