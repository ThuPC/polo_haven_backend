import { jwtDecode } from 'jwt-decode';

/**
 * Lấy thông tin user từ JWT token
 * @returns {Object|null} Thông tin user hoặc null nếu không có token
 */
export const getUserFromToken = () => {
  try {
    const token = localStorage.getItem('authToken');
    if (!token) return null;
    
    const decoded = jwtDecode(token);
    console.log('Decoded JWT token:', decoded);
    
    return {
      id: decoded.sub || decoded.id,
      tenKhachHang: decoded.tenKhachHang || decoded.name || decoded.sub,
      email: decoded.email,
      hinhAnh: decoded.hinhAnh || decoded.picture || decoded.image,
      sdt: decoded.sdt || decoded.phone,
      gioiTinh: decoded.gioiTinh,
      ngaySinh: decoded.ngaySinh,
      cccd: decoded.cccd,
      loginProvider: decoded.loginProvider || 'LOCAL'
    };
  } catch (error) {
    console.error('Error decoding JWT token:', error);
    return null;
  }
};

/**
 * Kiểm tra xem user có đăng nhập bằng Google không
 * @returns {boolean}
 */
export const isGoogleUser = () => {
  const user = getUserFromToken();
  return user?.loginProvider === 'GOOGLE';
};

/**
 * Lấy URL avatar từ thông tin user
 * @param {string} imageName - Tên file hoặc URL
 * @returns {string|null} URL avatar
 */
export const getAvatarUrl = (imageName) => {
  if (!imageName) return null;
  
  // Nếu là URL từ Google (bắt đầu bằng http/https)
  if (imageName.startsWith('http://') || imageName.startsWith('https://')) {
    return imageName;
  }
  
  // Nếu là tên file local
  return `http://localhost:8080/uploads/khach_hang/${imageName}`;
};

/**
 * Debug JWT token - in ra console để kiểm tra
 */
export const debugJwtToken = () => {
  try {
    const token = localStorage.getItem('authToken');
    if (!token) {
      console.log('No JWT token found');
      return;
    }
    
    const decoded = jwtDecode(token);
    console.log('=== JWT Token Debug ===');
    console.log('Raw token:', token);
    console.log('Decoded token:', decoded);
    console.log('Token expiration:', new Date(decoded.exp * 1000));
    console.log('Current time:', new Date());
    console.log('Is expired:', decoded.exp * 1000 < Date.now());
    console.log('========================');
    
    return decoded;
  } catch (error) {
    console.error('Error debugging JWT token:', error);
  }
}; 