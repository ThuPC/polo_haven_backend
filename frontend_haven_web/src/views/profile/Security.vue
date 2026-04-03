<template>
  <div class="security">
    <!-- Header -->
    <div class="section-header">
      <h2>Bảo mật tài khoản</h2>
      <p>Thay đổi mật khẩu và cài đặt bảo mật</p>
    </div>

    <!-- Notification -->
    <div v-if="notification.show" :class="['notification', notification.type]">
      <i :class="notification.type === 'success' ? 'bi bi-check-circle' : 'bi bi-exclamation-triangle'"></i>
      {{ notification.message }}
      <button @click="hideNotification" class="close-btn">&times;</button>
    </div>

    <!-- Change Password Form -->
    <div class="security-card">
      <div class="card-header">
        <h3>Thay đổi mật khẩu</h3>
        <p>Đảm bảo mật khẩu mới của bạn mạnh và khác với mật khẩu cũ</p>
      </div>

      <form @submit.prevent="changePassword" class="password-form">
        <div class="form-group">
          <label for="currentPassword">Mật khẩu hiện tại *</label>
          <div class="password-input">
            <input 
              id="currentPassword"
              v-model="passwordForm.currentPassword" 
              :type="showCurrentPassword ? 'text' : 'password'"
              placeholder="Nhập mật khẩu hiện tại"
              :class="{ 'error': errors.currentPassword }"
              @blur="validateField('currentPassword')"
            />
            <button 
              type="button" 
              class="toggle-password"
              @click="showCurrentPassword = !showCurrentPassword"
            >
              <i :class="showCurrentPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
            </button>
          </div>
          <span v-if="errors.currentPassword" class="error-message">{{ errors.currentPassword }}</span>
        </div>

        <div class="form-group">
          <label for="newPassword">Mật khẩu mới *</label>
          <div class="password-input">
            <input 
              id="newPassword"
              v-model="passwordForm.newPassword" 
              :type="showNewPassword ? 'text' : 'password'"
              placeholder="Nhập mật khẩu mới"
              :class="{ 'error': errors.newPassword }"
              @blur="validateField('newPassword')"
              @input="checkPasswordStrength"
            />
            <button 
              type="button" 
              class="toggle-password"
              @click="showNewPassword = !showNewPassword"
            >
              <i :class="showNewPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
            </button>
          </div>
          <span v-if="errors.newPassword" class="error-message">{{ errors.newPassword }}</span>
          
          <!-- Password Strength Indicator -->
          <div v-if="passwordForm.newPassword" class="password-strength">
            <div class="strength-bar">
              <div 
                class="strength-fill" 
                :class="passwordStrength.level"
                :style="{ width: passwordStrength.percentage + '%' }"
              ></div>
            </div>
            <span class="strength-text" :class="passwordStrength.level">
              {{ passwordStrength.text }}
            </span>
          </div>
        </div>

        <div class="form-group">
          <label for="confirmPassword">Xác nhận mật khẩu mới *</label>
          <div class="password-input">
            <input 
              id="confirmPassword"
              v-model="passwordForm.confirmPassword" 
              :type="showConfirmPassword ? 'text' : 'password'"
              placeholder="Nhập lại mật khẩu mới"
              :class="{ 'error': errors.confirmPassword }"
              @blur="validateField('confirmPassword')"
            />
            <button 
              type="button" 
              class="toggle-password"
              @click="showConfirmPassword = !showConfirmPassword"
            >
              <i :class="showConfirmPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
            </button>
          </div>
          <span v-if="errors.confirmPassword" class="error-message">{{ errors.confirmPassword }}</span>
        </div>

        <!-- OTP Section -->
        <div class="otp-section">
          <div class="otp-header">
            <h4>Xác minh danh tính</h4>
            <p>Nhập mã xác minh được gửi đến email của bạn</p>
          </div>
          
          <div class="otp-input-group">
            <div class="form-group">
              <label for="otp">Mã xác minh *</label>
              <div class="otp-input">
                <input 
                  id="otp"
                  v-model="passwordForm.otp" 
                  type="text"
                  placeholder="Nhập mã 6 số"
                  :class="{ 'error': errors.otp }"
                  @blur="validateField('otp')"
                  maxlength="6"
                />
                <button 
                  type="button" 
                  class="send-otp-btn"
                  @click="sendOtp"
                  :disabled="otpLoading || otpCountdown > 0"
                >
                  <i v-if="otpLoading" class="bi bi-arrow-clockwise spin"></i>
                  <span v-else-if="otpCountdown > 0">{{ otpCountdown }}s</span>
                  <span v-else>Gửi mã</span>
                </button>
              </div>
              <span v-if="errors.otp" class="error-message">{{ errors.otp }}</span>
            </div>
          </div>
        </div>

        <!-- Password Requirements -->
        <div class="password-requirements">
          <h4>Yêu cầu mật khẩu:</h4>
          <ul>
            <li :class="{ 'met': passwordRequirements.length }">
              <i :class="passwordRequirements.length ? 'bi bi-check-circle-fill' : 'bi bi-circle'"></i>
              Ít nhất 8 ký tự
            </li>
            <li :class="{ 'met': passwordRequirements.uppercase }">
              <i :class="passwordRequirements.uppercase ? 'bi bi-check-circle-fill' : 'bi bi-circle'"></i>
              Có chữ hoa
            </li>
            <li :class="{ 'met': passwordRequirements.lowercase }">
              <i :class="passwordRequirements.lowercase ? 'bi bi-check-circle-fill' : 'bi bi-circle'"></i>
              Có chữ thường
            </li>
            <li :class="{ 'met': passwordRequirements.number }">
              <i :class="passwordRequirements.number ? 'bi bi-check-circle-fill' : 'bi bi-circle'"></i>
              Có số
            </li>
            <li :class="{ 'met': passwordRequirements.special }">
              <i :class="passwordRequirements.special ? 'bi bi-check-circle-fill' : 'bi bi-circle'"></i>
              Có ký tự đặc biệt
            </li>
          </ul>
        </div>

        <div class="form-actions">
          <button type="button" @click="resetPasswordForm" class="btn btn-secondary">
            <i class="bi bi-arrow-clockwise"></i>
            Làm mới
          </button>
          <button type="submit" class="btn btn-primary" :disabled="loading || !isFormValid">
            <i v-if="loading" class="bi bi-arrow-clockwise spin"></i>
            <i v-else class="bi bi-shield-check"></i>
            {{ loading ? 'Đang thay đổi...' : 'Thay đổi mật khẩu' }}
          </button>
        </div>
      </form>
    </div>

    <!-- Security Tips -->
    <div class="security-tips">
      <h3>Mẹo bảo mật</h3>
      <div class="tips-grid">
        <div class="tip-card">
          <i class="bi bi-shield-lock"></i>
          <h4>Mật khẩu mạnh</h4>
          <p>Sử dụng mật khẩu có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt</p>
        </div>
        <div class="tip-card">
          <i class="bi bi-key"></i>
          <h4>Không chia sẻ</h4>
          <p>Không bao giờ chia sẻ mật khẩu với người khác hoặc lưu trong các ứng dụng không an toàn</p>
        </div>
        <div class="tip-card">
          <i class="bi bi-arrow-repeat"></i>
          <h4>Thay đổi định kỳ</h4>
          <p>Thay đổi mật khẩu định kỳ để tăng cường bảo mật cho tài khoản</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { changePassword as changePasswordService, sendOtpForChangePassword } from '@/services/profile.js';

// Reactive data
const loading = ref(false);
const otpLoading = ref(false);
const otpCountdown = ref(0);
const notification = ref({ show: false, type: 'success', message: '' });

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
  otp: ''
});

const errors = reactive({});

// Password visibility toggles
const showCurrentPassword = ref(false);
const showNewPassword = ref(false);
const showConfirmPassword = ref(false);

// Password strength
const passwordStrength = ref({
  level: 'weak',
  percentage: 0,
  text: 'Yếu'
});

const passwordRequirements = reactive({
  length: false,
  uppercase: false,
  lowercase: false,
  number: false,
  special: false
});

// Validation rules
const validationRules = {
  currentPassword: [
    { required: true, message: 'Vui lòng nhập mật khẩu hiện tại' }
  ],
  newPassword: [
    { required: true, message: 'Vui lòng nhập mật khẩu mới' },
    { minLength: 8, message: 'Mật khẩu phải có ít nhất 8 ký tự' },
    { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/, 
      message: 'Mật khẩu phải có chữ hoa, chữ thường, số và ký tự đặc biệt' }
  ],
  confirmPassword: [
    { required: true, message: 'Vui lòng xác nhận mật khẩu mới' },
    { custom: () => passwordForm.newPassword === passwordForm.confirmPassword, 
      message: 'Mật khẩu xác nhận không khớp' }
  ],
  otp: [
    { required: true, message: 'Vui lòng nhập mã xác minh' },
    { pattern: /^\d{6}$/, message: 'Mã xác minh phải có đúng 6 chữ số' }
  ]
};

// Computed
const isFormValid = computed(() => {
  return passwordForm.currentPassword && 
         passwordForm.newPassword && 
         passwordForm.confirmPassword &&
         passwordForm.otp &&
         Object.keys(errors).length === 0 &&
         Object.values(passwordRequirements).every(req => req);
});

// Methods
const validateField = (fieldName) => {
  const rules = validationRules[fieldName];
  if (!rules) return true;

  const value = passwordForm[fieldName];
  
  for (const rule of rules) {
    if (rule.required && (!value || value.toString().trim() === '')) {
      errors[fieldName] = rule.message;
      return false;
    }
    if (rule.minLength && value && value.length < rule.minLength) {
      errors[fieldName] = rule.message;
      return false;
    }
    if (rule.pattern && value && !rule.pattern.test(value)) {
      errors[fieldName] = rule.message;
      return false;
    }
    if (rule.custom && !rule.custom()) {
      errors[fieldName] = rule.message;
      return false;
    }
  }
  
  delete errors[fieldName];
  return true;
};

const checkPasswordStrength = () => {
  const password = passwordForm.newPassword;
  
  if (!password) {
    passwordStrength.value = { level: 'weak', percentage: 0, text: 'Yếu' };
    return;
  }

  // Check requirements
  passwordRequirements.length = password.length >= 8;
  passwordRequirements.uppercase = /[A-Z]/.test(password);
  passwordRequirements.lowercase = /[a-z]/.test(password);
  passwordRequirements.number = /\d/.test(password);
  passwordRequirements.special = /[@$!%*?&]/.test(password);

  // Calculate strength
  let score = 0;
  let percentage = 0;

  if (passwordRequirements.length) score += 20;
  if (passwordRequirements.uppercase) score += 20;
  if (passwordRequirements.lowercase) score += 20;
  if (passwordRequirements.number) score += 20;
  if (passwordRequirements.special) score += 20;

  percentage = score;

  // Determine level
  let level = 'weak';
  let text = 'Yếu';

  if (percentage >= 80) {
    level = 'strong';
    text = 'Mạnh';
  } else if (percentage >= 60) {
    level = 'medium';
    text = 'Trung bình';
  } else if (percentage >= 40) {
    level = 'weak';
    text = 'Yếu';
  } else {
    level = 'very-weak';
    text = 'Rất yếu';
  }

  passwordStrength.value = { level, percentage, text };
};

const sendOtp = async () => {
  try {
    otpLoading.value = true;
    
    console.log('Security - Sending OTP...');
    const response = await sendOtpForChangePassword();
    
    console.log('Security - Send OTP response:', response);
    
    if (response.success) {
      showNotification('success', 'Mã xác minh đã được gửi đến email của bạn');
      startOtpCountdown();
    } else {
      showNotification('error', response.message || 'Không thể gửi mã xác minh');
    }
    
  } catch (error) {
    console.error('Lỗi khi gửi OTP:', error);
    showNotification('error', 'Có lỗi xảy ra khi gửi mã xác minh');
  } finally {
    otpLoading.value = false;
  }
};

const startOtpCountdown = () => {
  otpCountdown.value = 60; // 60 giây
  const timer = setInterval(() => {
    otpCountdown.value--;
    if (otpCountdown.value <= 0) {
      clearInterval(timer);
    }
  }, 1000);
};

const changePassword = async () => {
  if (!isFormValid.value) {
    showNotification('error', 'Vui lòng kiểm tra lại thông tin đã nhập');
    return;
  }

  try {
    loading.value = true;
    
    console.log('Security - Attempting to change password...');
    const response = await changePasswordService(
      passwordForm.currentPassword,
      passwordForm.newPassword,
      passwordForm.otp
    );
    
    console.log('Security - Change password response:', response);
    
    if (response.success) {
      showNotification('success', 'Thay đổi mật khẩu thành công! Vui lòng đăng nhập lại với mật khẩu mới.');
      resetPasswordForm();
      
      // Clear token và redirect về trang đăng nhập
      localStorage.removeItem('authToken');
      setTimeout(() => {
        window.location.href = '/login';
      }, 2000);
    } else {
      showNotification('error', response.message || 'Có lỗi xảy ra khi thay đổi mật khẩu');
    }
    
  } catch (error) {
    console.error('Lỗi khi thay đổi mật khẩu:', error);
    showNotification('error', 'Có lỗi xảy ra khi thay đổi mật khẩu');
  } finally {
    loading.value = false;
  }
};

const resetPasswordForm = () => {
  Object.assign(passwordForm, {
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
    otp: ''
  });
  
  Object.keys(errors).forEach(key => delete errors[key]);
  
  showCurrentPassword.value = false;
  showNewPassword.value = false;
  showConfirmPassword.value = false;
  
  Object.keys(passwordRequirements).forEach(key => {
    passwordRequirements[key] = false;
  });
  
  passwordStrength.value = { level: 'weak', percentage: 0, text: 'Yếu' };
  otpCountdown.value = 0;
};

const showNotification = (type, message) => {
  notification.value = { show: true, type, message };
  setTimeout(() => hideNotification(), 5000);
};

const hideNotification = () => {
  notification.value.show = false;
};
</script>

<style scoped>
.security {
  max-width: 100%;
}

.section-header {
  margin-bottom: 30px;
}

.section-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.section-header p {
  color: #6c757d;
  margin: 0;
}

.notification {
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.notification.success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.notification.error {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.notification .close-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  margin-left: auto;
  color: inherit;
}

.security-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.card-header {
  margin-bottom: 30px;
}

.card-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.card-header p {
  color: #6c757d;
  margin: 0;
}

.password-form {
  max-width: 500px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.password-input {
  position: relative;
  display: flex;
  align-items: center;
}

.password-input input {
  flex: 1;
  padding: 12px 16px;
  padding-right: 50px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.password-input input:focus {
  outline: none;
  border-color: #4ba27b;
}

.password-input input.error {
  border-color: #dc3545;
}

.toggle-password {
  position: absolute;
  right: 12px;
  background: none;
  border: none;
  color: #6c757d;
  cursor: pointer;
  padding: 4px;
  font-size: 16px;
  transition: color 0.3s ease;
}

.toggle-password:hover {
  color: #4ba27b;
}

.error-message {
  color: #dc3545;
  font-size: 12px;
  margin-top: 4px;
  display: block;
}

.password-strength {
  margin-top: 10px;
}

.strength-bar {
  width: 100%;
  height: 6px;
  background: #e0e0e0;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 5px;
}

.strength-fill {
  height: 100%;
  transition: all 0.3s ease;
}

.strength-fill.very-weak {
  background: #dc3545;
}

.strength-fill.weak {
  background: #fd7e14;
}

.strength-fill.medium {
  background: #ffc107;
}

.strength-fill.strong {
  background: #28a745;
}

.strength-text {
  font-size: 12px;
  font-weight: 500;
}

.strength-text.very-weak {
  color: #dc3545;
}

.strength-text.weak {
  color: #fd7e14;
}

.strength-text.medium {
  color: #ffc107;
}

.strength-text.strong {
  color: #28a745;
}

.password-requirements {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin: 20px 0;
}

.password-requirements h4 {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0 0 15px 0;
}

.password-requirements ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.password-requirements li {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
  color: #6c757d;
  transition: color 0.3s ease;
}

.password-requirements li.met {
  color: #28a745;
}

.password-requirements li i {
  font-size: 14px;
}

  .password-requirements li.met i {
    color: #28a745;
  }

  .otp-section {
    background: #f8f9fa;
    border-radius: 8px;
    padding: 20px;
    margin: 20px 0;
    border: 1px solid #e0e0e0;
  }

  .otp-header {
    margin-bottom: 15px;
  }

  .otp-header h4 {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0 0 5px 0;
  }

  .otp-header p {
    font-size: 13px;
    color: #6c757d;
    margin: 0;
  }

  .otp-input-group {
    display: flex;
    gap: 15px;
    align-items: flex-end;
  }

  .otp-input {
    display: flex;
    gap: 10px;
    align-items: center;
  }

  .otp-input input {
    flex: 1;
    padding: 12px 16px;
    border: 2px solid #e0e0e0;
    border-radius: 8px;
    font-size: 14px;
    text-align: center;
    letter-spacing: 2px;
    font-weight: 600;
    transition: border-color 0.3s ease;
  }

  .otp-input input:focus {
    outline: none;
    border-color: #4ba27b;
  }

  .otp-input input.error {
    border-color: #dc3545;
  }

  .send-otp-btn {
    padding: 12px 20px;
    background: #4ba27b;
    color: white;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    font-size: 14px;
    font-weight: 500;
    white-space: nowrap;
    transition: all 0.3s ease;
  }

  .send-otp-btn:hover:not(:disabled) {
    background: #3d8b6a;
  }

  .send-otp-btn:disabled {
    background: #6c757d;
    cursor: not-allowed;
    opacity: 0.6;
  }

  .form-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: #4ba27b;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #3d8b6a;
  transform: translateY(-1px);
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #5a6268;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.security-tips {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.security-tips h3 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px 0;
}

.tips-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.tip-card {
  text-align: center;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.tip-card:hover {
  border-color: #4ba27b;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(75, 162, 123, 0.1);
}

.tip-card i {
  font-size: 32px;
  color: #4ba27b;
  margin-bottom: 15px;
}

.tip-card h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 10px 0;
}

.tip-card p {
  font-size: 14px;
  color: #6c757d;
  margin: 0;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .password-form {
    max-width: 100%;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .btn {
    width: 100%;
    justify-content: center;
  }
  
  .tips-grid {
    grid-template-columns: 1fr;
  }
}
</style> 