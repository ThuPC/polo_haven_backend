<template>
  <div class="order-progress-tracker">
    

    
    <div v-if="visibleSteps.length === 0" class="no-data">
      <i class="fas fa-info-circle"></i>
      <p>Không có dữ liệu tiến trình</p>
    </div>
    
    <div v-else class="progress-container">
      <!-- Ẩn thanh progress-line chính vì chúng ta dùng step-connector -->
      <!-- <div class="progress-line" :style="{ width: `${progressWidth}%` }"></div> -->
      
      <div class="steps-container">
        <div 
          v-for="(step, index) in visibleSteps" 
          :key="index"
          class="step-item"
          :class="{ 
            'active': isStepActive(index),
            'completed': isStepCompleted(index),
            'current': isCurrentStep(index),
            'appear-after-jump': isAppearAfterJump(index)
          }"
        >
          <!-- Circle with icon -->
          <div class="step-circle">
            <i :class="step.icon"></i>
          </div>
          
          <!-- Step label -->
          <div class="step-label" :title="step.time ? `Cập nhật lúc: ${formatTime(step.time)}` : 'Không có thông tin thời gian'">
            <span class="step-name">{{ step.label }}</span>
            <span v-if="step.time" class="step-time">
              <i class="fas fa-clock"></i>
              {{ formatTime(step.time) }}
            </span>
            <span v-if="step.time" class="step-date">
              <i class="fas fa-calendar-day"></i>
              {{ formatRelativeTime(step.time) }}
            </span>
            <span v-else class="step-time text-muted">
              <i class="fas fa-clock"></i>
              Không có thông tin thời gian
            </span>
          </div>
          
          <!-- Connecting line (except for last step) -->
          <div 
            v-if="index < visibleSteps.length - 1" 
            class="step-connector"
            :class="{ 
              'active': isStepCompleted(index),
              'direct-jump': isDirectJump(index)
            }"
            :style="{ 
              '--connector-width': getConnectorWidth(index),
              '--connector-delay': `${index * 0.2}s`
            }"
          ></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  currentStatus: {
    type: Number,
    required: true,
    validator: (value) => value >= 0 && value <= 10 // Mở rộng để hỗ trợ trạng thái trả hàng
  },
  orderType: {
    type: Number,
    required: true, // 0: tại quầy, 1: online
    validator: (value) => [0, 1].includes(value)
  },
  times: {
    type: Array,
    default: () => []
  },
  hasShippingHistory: {
    type: Boolean,
    default: false
  }
});



// Định nghĩa các bước cho từng loại hóa đơn
const onlineSteps = [
  { label: 'Chờ xác nhận', icon: 'fas fa-clock', status: 0 },
  { label: 'Đã xác nhận', icon: 'fas fa-check-circle', status: 1 },
  { label: 'Chờ vận chuyển', icon: 'fas fa-truck-loading', status: 2 },
  { label: 'Đang vận chuyển', icon: 'fas fa-shipping-fast', status: 3 },
  { label: 'Đã giao hàng', icon: 'fas fa-box-open', status: 4 },
  { label: 'Hoàn thành', icon: 'fas fa-flag-checkered', status: 5 },
  { label: 'Đã hủy', icon: 'fas fa-ban', status: 6 },
  { label: 'Yêu cầu trả hàng', icon: 'fas fa-undo-alt', status: 7 },
  { label: 'Đang trả hàng', icon: 'fas fa-spinner', status: 8 },
  { label: 'Trả hàng thành công', icon: 'fas fa-check', status: 9 },
  { label: 'Từ chối trả hàng', icon: 'fas fa-times', status: 10 }
];

const offlineSteps = [
  { label: 'Đã xác nhận', icon: 'fas fa-check-circle', status: 1 },
  { label: 'Chờ vận chuyển', icon: 'fas fa-truck-loading', status: 2 },
  { label: 'Đang vận chuyển', icon: 'fas fa-shipping-fast', status: 3 },
  { label: 'Đã giao hàng', icon: 'fas fa-box-open', status: 4 },
  { label: 'Hoàn thành', icon: 'fas fa-flag-checkered', status: 5 },
  { label: 'Đã hủy', icon: 'fas fa-ban', status: 6 },
  { label: 'Yêu cầu trả hàng', icon: 'fas fa-undo-alt', status: 7 },
  { label: 'Đang trả hàng', icon: 'fas fa-spinner', status: 8 },
  { label: 'Trả hàng thành công', icon: 'fas fa-check', status: 9 },
  { label: 'Từ chối trả hàng', icon: 'fas fa-times', status: 10 }
];

// Tính toán các bước hiển thị dựa trên loại hóa đơn và trạng thái
const visibleSteps = computed(() => {
  try {
    if (props.orderType === 1) {
      if (props.currentStatus === 6) {
        return [
          ...onlineSteps.slice(0, 1),
          {
            ...onlineSteps[6],
            time: getTimeForStatus(6)
          }
        ];
      }
      // Trả hàng các trạng thái mới
      if ([7,8,9,10].includes(props.currentStatus)) {
        return [
          ...onlineSteps.slice(0, 6),
          {
            ...onlineSteps[7],
            time: getTimeForStatus(7)
          },
          ...(props.currentStatus >= 8 ? [{
            ...onlineSteps[8],
            time: getTimeForStatus(8)
          }] : []),
          ...(props.currentStatus >= 9 ? [{
            ...onlineSteps[9],
            time: getTimeForStatus(9)
          }] : []),
          ...(props.currentStatus === 10 ? [{
            ...onlineSteps[10],
            time: getTimeForStatus(10)
          }] : [])
        ];
      }
      const steps = onlineSteps.slice(0, Math.min(props.currentStatus + 1, onlineSteps.length));
      return steps.map((step) => ({
        ...step,
        time: getTimeForStatus(step.status)
      }));
    } else {
      const current = props.currentStatus;
      if (current === 6) {
        return [
          {
            ...offlineSteps[0],
            time: getTimeForStatus(1)
          },
          {
            ...offlineSteps[5],
            time: getTimeForStatus(6)
          }
        ];
      }
      // Trả hàng các trạng thái mới
      if ([7,8,9,10].includes(current)) {
        return [
          {
            ...offlineSteps[0],
            time: getTimeForStatus(1)
          },
          {
            ...offlineSteps[4],
            time: getTimeForStatus(5)
          },
          {
            ...offlineSteps[6],
            time: getTimeForStatus(7)
          },
          ...(current >= 8 ? [{
            ...offlineSteps[7],
            time: getTimeForStatus(8)
          }] : []),
          ...(current >= 9 ? [{
            ...offlineSteps[8],
            time: getTimeForStatus(9)
          }] : []),
          ...(current === 10 ? [{
            ...offlineSteps[9],
            time: getTimeForStatus(10)
          }] : [])
        ];
      }
      if (current === 1 && !props.hasShippingHistory) {
        return [{
          ...offlineSteps[0],
          time: getTimeForStatus(1)
        }];
      }
      if (current === 5 && !props.hasShippingHistory) {
        return [
          {
            ...offlineSteps[0],
            time: getTimeForStatus(1)
          },
          {
            ...offlineSteps[4],
            time: getTimeForStatus(5)
          }
        ];
      }
      if (props.hasShippingHistory) {
        const steps = offlineSteps.slice(0, Math.min(current + 1, offlineSteps.length));
        return steps.map((step) => ({
          ...step,
          time: getTimeForStatus(step.status)
        }));
      }
      const currentStepIndex = offlineSteps.findIndex(step => step.status === current);
      if (currentStepIndex !== -1) {
        return [{
          ...offlineSteps[currentStepIndex],
          time: getTimeForStatus(current)
        }];
      }
      return [];
    }
  } catch (error) {
    console.error('Lỗi tính toán visibleSteps:', error);
    return [];
  }
});



// Kiểm tra bước có đang active không
const isStepActive = (index) => {
  if (index >= visibleSteps.value.length) return false;
  const stepStatus = visibleSteps.value[index].status;
  return props.currentStatus >= stepStatus;
};

// Kiểm tra bước đã hoàn thành chưa
const isStepCompleted = (index) => {
  try {
    if (index >= visibleSteps.value.length) return false;
    const stepStatus = visibleSteps.value[index].status;
    return props.currentStatus > stepStatus;
  } catch (error) {
    console.warn('Lỗi kiểm tra step completed:', error);
    return false;
  }
};

// Kiểm tra bước hiện tại
const isCurrentStep = (index) => {
  try {
    if (index >= visibleSteps.value.length) return false;
    const stepStatus = visibleSteps.value[index].status;
    return props.currentStatus === stepStatus;
  } catch (error) {
    console.warn('Lỗi kiểm tra current step:', error);
    return false;
  }
};

// Kiểm tra có phải chuyển thẳng không
const isDirectJump = (index) => {
  try {
    if (index >= visibleSteps.value.length - 1) return false;
    
    const currentStep = visibleSteps.value[index];
    const nextStep = visibleSteps.value[index + 1];
    
    // Nếu chuyển thẳng từ trạng thái 1 → 5 (tại quầy)
    return props.orderType === 0 && 
           currentStep.status === 1 && 
           nextStep.status === 5 && 
           !props.hasShippingHistory;
  } catch (error) {
    console.warn('Lỗi kiểm tra direct jump:', error);
    return false;
  }
};

// Kiểm tra có phải xuất hiện sau direct jump không
const isAppearAfterJump = (index) => {
  try {
    if (index === 0) return false;
    
    const currentStep = visibleSteps.value[index];
    const prevStep = visibleSteps.value[index - 1];
    
    // Nếu là trạng thái 5 và trước đó là trạng thái 1 (direct jump)
    return props.orderType === 0 && 
           currentStep.status === 5 && 
           prevStep.status === 1 && 
           !props.hasShippingHistory;
  } catch (error) {
    console.warn('Lỗi kiểm tra appear after jump:', error);
    return false;
  }
};

// Tính toán độ rộng thanh nối
const getConnectorWidth = (index) => {
  try {
    if (index >= visibleSteps.value.length - 1) return '0%';
    
    const currentStep = visibleSteps.value[index];
    const nextStep = visibleSteps.value[index + 1];
    
    // Nếu chuyển thẳng từ trạng thái 1 → 5 (tại quầy)
    if (props.orderType === 0 && 
        currentStep.status === 1 && 
        nextStep.status === 5 && 
        !props.hasShippingHistory) {
      return '100%'; // Thanh nối dài hơn để bỏ qua các bước trung gian
    }
    
    return '100%'; // Mặc định
  } catch (error) {
    console.warn('Lỗi tính toán connector width:', error);
    return '100%';
  }
};

// Lấy thời gian cho trạng thái cụ thể
const getTimeForStatus = (status) => {
  try {
    // Kiểm tra nếu props.times là array và có dữ liệu
    if (!Array.isArray(props.times) || props.times.length === 0) {
      // Fallback: trả về null nếu không có dữ liệu
      return null;
    }
    
    // Kiểm tra nếu status hợp lệ
    if (typeof status !== 'number' || status < 0) {
      // Fallback: trả về null
      return null;
    }
    
    const timeValue = props.times[status];
    
    // Nếu timeValue là null/undefined, trả về null
    if (!timeValue) {
      return null;
    }
    
    // Thử parse thời gian với helper function
    const parsedTime = parseCustomDateTime(timeValue);
    return parsedTime;
  } catch (error) {
    console.warn('Lỗi lấy thời gian cho status:', error, 'Status:', status);
    return null;
  }
};

// Helper function để parse format dd/MM/yyyy HH:mm
const parseCustomDateTime = (timeString) => {
  if (!timeString) return null;
  
  try {
    // Nếu đã là Date object, trả về luôn
    if (timeString instanceof Date) {
      return timeString;
    }
    
    // Nếu là string có format dd/MM/yyyy HH:mm
    if (typeof timeString === 'string') {
      // Kiểm tra pattern dd/MM/yyyy HH:mm
      const pattern = /^(\d{1,2})\/(\d{1,2})\/(\d{4})\s+(\d{1,2}):(\d{1,2})$/;
      const match = timeString.match(pattern);
      
      if (match) {
        const [, day, month, year, hour, minute] = match;
        // Tạo Date object với timezone Việt Nam
        const date = new Date(parseInt(year), parseInt(month) - 1, parseInt(day), parseInt(hour), parseInt(minute));
        return date;
      }
      
      // Thử parse với Date constructor (cho các format khác)
      const date = new Date(timeString);
      if (!isNaN(date.getTime())) {
        return date;
      }
    }
    
    // Nếu là number (timestamp)
    if (typeof timeString === 'number') {
      const date = new Date(timeString);
      if (!isNaN(date.getTime())) {
        return date;
      }
    }
    
    return null;
  } catch (error) {
    console.warn('Lỗi parse thời gian:', error, 'Time value:', timeString);
    return null;
  }
};

// Format thời gian đầy đủ
const formatTime = (time) => {
  if (!time) return '';
  
  try {
    const date = parseCustomDateTime(time);
    
    if (!date) {
      console.warn('Không thể parse thời gian:', time);
      return '';
    }
    
    // Kiểm tra nếu thời gian quá cũ hoặc quá mới (có thể là lỗi)
    const now = new Date();
    const diffInYears = Math.abs(now.getFullYear() - date.getFullYear());
    if (diffInYears > 10) {
      console.warn('Thời gian có vẻ không hợp lệ (quá cũ hoặc quá mới):', time);
      return '';
    }
    
    return date.toLocaleString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    });
  } catch (error) {
    console.warn('Lỗi format thời gian:', error, 'Time value:', time);
    return '';
  }
};

// Format chỉ ngày
const formatDateOnly = (time) => {
  if (!time) return '';
  
  try {
    const date = parseCustomDateTime(time);
    
    if (!date) {
      return '';
    }
    
    return date.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  } catch (error) {
    return '';
  }
};

// Format thời gian tương đối
const formatRelativeTime = (time) => {
  if (!time) return '';
  
  try {
    const date = parseCustomDateTime(time);
    
    if (!date) {
      return '';
    }
    
    const now = new Date();
    const diffInMs = now.getTime() - date.getTime();
    const diffInMinutes = Math.floor(diffInMs / (1000 * 60));
    const diffInHours = Math.floor(diffInMs / (1000 * 60 * 60));
    const diffInDays = Math.floor(diffInMs / (1000 * 60 * 60 * 24));
    
    // Nếu thời gian cách đây quá 1 ngày, hiển thị ngày tháng năm
    if (diffInDays >= 1) {
      return formatDateOnly(time);
    }
    
    // Nếu thời gian cách đây quá 1 giờ, hiển thị số giờ
    if (diffInHours >= 1) {
      return `${diffInHours} giờ trước`;
    }
    
    // Nếu thời gian cách đây quá 1 phút, hiển thị số phút
    if (diffInMinutes >= 1) {
      return `${diffInMinutes} phút trước`;
    }
    
    // Nếu thời gian cách đây dưới 1 phút, hiển thị "Vừa xong"
    return 'Vừa xong';
  } catch (error) {
    return '';
  }
};



</script>

<style scoped>
.order-progress-tracker {
  margin: 30px 0;
  padding: 30px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
}

.no-data {
  text-align: center;
  padding: 40px 20px;
  color: #6c757d;
}

.no-data i {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.no-data p {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.progress-container {
  position: relative;
  padding: 20px 0;
}

.progress-line {
  display: none; /* Ẩn hoàn toàn thanh progress-line */
  position: absolute;
  top: 50%;
  left: 0;
  height: 4px;
  background: linear-gradient(90deg, #4CAF50, #2196F3);
  border-radius: 2px;
  transition: width 1.2s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1;
  transform: translateY(-50%);
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
}

.steps-container {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 2;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  flex: 1;
}

.step-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #f5f5f5;
  border: 3px solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 3;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.step-circle i {
  font-size: 24px;
  color: #9e9e9e;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Active step */
.step-item.active .step-circle {
  background: #fff;
  border-color: #2196F3;
  box-shadow: 0 0 0 4px rgba(33, 150, 243, 0.2), 0 4px 12px rgba(33, 150, 243, 0.3);
  transform: scale(1.05);
}

.step-item.active .step-circle i {
  color: #2196F3;
  transform: scale(1.1);
}

/* Completed step */
.step-item.completed .step-circle {
  background: #4CAF50;
  border-color: #4CAF50;
  box-shadow: 0 0 0 4px rgba(76, 175, 80, 0.2), 0 4px 12px rgba(76, 175, 80, 0.3);
  transform: scale(1.02);
}

.step-item.completed .step-circle i {
  color: white;
  transform: scale(1.05);
}

/* Current step */
.step-item.current .step-circle {
  background: #FF9800;
  border-color: #FF9800;
  box-shadow: 0 0 0 4px rgba(255, 152, 0, 0.2), 0 4px 12px rgba(255, 152, 0, 0.3);
  animation: pulse 2s infinite;
  transform: scale(1.1);
}

.step-item.current .step-circle i {
  color: white;
  transform: scale(1.15);
}

/* Hiệu ứng đặc biệt cho trạng thái xuất hiện sau direct jump */
.step-item.appear-after-jump .step-circle {
  animation: appearAfterJump 0.8s ease-out forwards;
}

@keyframes appearAfterJump {
  0% {
    opacity: 0;
    transform: scale(0.5);
  }
  50% {
    opacity: 0.7;
    transform: scale(0.8);
  }
  100% {
    opacity: 1;
    transform: scale(1.1);
  }
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 4px rgba(255, 152, 0, 0.2);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(255, 152, 0, 0.1);
  }
  100% {
    box-shadow: 0 0 0 4px rgba(255, 152, 0, 0.2);
  }
}

.step-label {
  text-align: center;
  min-width: 120px;
  transition: all 0.3s ease;
  cursor: help;
  position: relative;
}

.step-name {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  transition: all 0.3s ease;
}

.step-time {
  display: block;
  font-size: 11px;
  color: #666;
  font-style: italic;
  transition: all 0.3s ease;
  margin-bottom: 2px;
}

.step-time i {
  margin-right: 4px;
  font-size: 10px;
}

.step-date {
  display: block;
  font-size: 10px;
  color: #999;
  font-weight: 500;
  transition: all 0.3s ease;
}

.step-date i {
  margin-right: 3px;
  font-size: 9px;
}

.text-muted {
  color: #6c757d !important;
  opacity: 0.7;
}

.step-item.active .step-name {
  color: #2196F3;
  font-weight: 700;
}

.step-item.completed .step-name {
  color: #4CAF50;
  font-weight: 700;
}

.step-item.current .step-name {
  color: #FF9800;
  font-weight: 700;
}

.step-item.active .step-time,
.step-item.completed .step-time,
.step-item.current .step-time {
  color: #333;
  font-weight: 500;
}

.step-item.active .step-date,
.step-item.completed .step-date,
.step-item.current .step-date {
  color: #555;
  font-weight: 600;
}

/* Hiệu ứng hover cho step label */
.step-label:hover {
  transform: translateY(-2px);
}

.step-label:hover .step-time,
.step-label:hover .step-date {
  color: #333;
}

.step-connector {
  position: absolute;
  top: 30px;
  left: 50%;
  width: var(--connector-width, 100%);
  height: 4px;
  background: #e0e0e0;
  border-radius: 2px;
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  transform: translateY(-50%);
  animation-delay: var(--connector-delay, 0s);
}

.step-connector.active {
  background: linear-gradient(90deg, #4CAF50, #2196F3);
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.4);
  animation: flow 2s ease-in-out infinite;
  animation-delay: var(--connector-delay, 0s);
}

@keyframes flow {
  0%, 100% {
    box-shadow: 0 2px 8px rgba(76, 175, 80, 0.4);
  }
  50% {
    box-shadow: 0 4px 16px rgba(76, 175, 80, 0.6);
  }
}

@keyframes directFlow {
  0% {
    width: 0%;
    opacity: 0;
    background: linear-gradient(90deg, #4CAF50, #2196F3);
  }
  25% {
    width: 25%;
    opacity: 0.5;
    background: linear-gradient(90deg, #4CAF50, #2196F3);
  }
  50% {
    width: 50%;
    opacity: 0.8;
    background: linear-gradient(90deg, #4CAF50, #2196F3);
  }
  75% {
    width: 75%;
    opacity: 0.9;
    background: linear-gradient(90deg, #4CAF50, #2196F3);
  }
  100% {
    width: 100%;
    opacity: 1;
    background: linear-gradient(90deg, #4CAF50, #2196F3);
    box-shadow: 0 2px 8px rgba(76, 175, 80, 0.4);
  }
}

/* Animation đặc biệt cho chuyển thẳng từ 1 → 5 */
.step-connector.direct-jump {
  animation: directFlow 2s ease-in-out forwards;
  background: linear-gradient(90deg, #4CAF50, #2196F3);
}

/* Responsive */
@media (max-width: 768px) {
  .steps-container {
    flex-direction: column;
    gap: 20px;
  }
  
  .step-item {
    flex-direction: row;
    width: 100%;
    text-align: left;
  }
  
  .step-circle {
    margin-bottom: 0;
    margin-right: 15px;
    flex-shrink: 0;
  }
  
  .step-connector {
    display: none;
  }
  
  .progress-line {
    display: none;
  }
}

@media (max-width: 480px) {
  .step-circle {
    width: 50px;
    height: 50px;
  }
  
  .step-circle i {
    font-size: 20px;
  }
  
  .step-name {
    font-size: 13px;
  }
  
  .step-time {
    font-size: 11px;
  }
}
</style>