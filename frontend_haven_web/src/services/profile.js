import  client  from '@/utils/api.js';

// Lấy thông tin profile của user hiện tại
export const getProfile = async () => {
  try {
    const response = await client.get('/api/khach-hang/profile');
    return response.data;
  } catch (error) {
    console.error('Error getting profile:', error);
    throw error;
  }
};

// Cập nhật thông tin profile
export const updateProfile = async (userId, formData) => {
  try {
    const response = await client.put(`/api/khach-hang/update/${userId}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    return response.data;
  } catch (error) {
    console.error('Error updating profile:', error);
    throw error;
  }
};


// Gửi OTP để thay đổi mật khẩu
export const sendOtpForChangePassword = async () => {
  try {
    const response = await client.post('/api/khach-hang/send-otp-change-password');
    return response.data;
  } catch (error) {
    console.error('Error sending OTP:', error);
    throw error;
  }
};

// Thay đổi mật khẩu (với OTP)
export const changePassword = async (currentPassword, newPassword, otp) => {
  try {
    const response = await client.post('/api/khach-hang/change-password', {
      currentPassword,
      newPassword,
      otp
    });
    return response.data;
  } catch (error) {
    console.error('Error changing password:', error);
    throw error;
  }
};

// Test authentication
export const testAuth = async () => {
  try {
    const response = await client.get('/api/khach-hang/test-auth');
    return response.data;
  } catch (error) {
    console.error('Error testing auth:', error);
    throw error;
  }
};

// Lấy thông tin đầy đủ của user hiện tại
export const getCurrentUserInfo = async () => {
  try {
    const response = await client.get('/api/khach-hang/current-user-info');
    return response.data;
  } catch (error) {
    console.error('Error getting current user info:', error);
    throw error;
  }
}; 