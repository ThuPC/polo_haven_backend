/**
 * Utility functions for formatting Vietnamese addresses consistently
 */

/**
 * Format address with proper ward and province type prefixes
 * @param {Object} addressData - Object containing address components
 * @param {string} addressData.chiTiet - Detailed address (street, house number)
 * @param {string} addressData.phuong - Ward code
 * @param {string} addressData.tinh - Province code
 * @param {Array} wards - Array of wards with type information
 * @param {Array} provinces - Array of provinces with type information
 * @returns {string} Formatted address string
 */
export const formatAddressWithTypes = (addressData, wards = [], provinces = []) => {
  if (!addressData) return 'N/A';
  
  const { chiTiet, phuong, tinh } = addressData;
  
  // Find ward type and name
  let wardType = "Xã"; // Default
  let wardName = "";
  if (phuong && wards.length > 0) {
    const ward = wards.find(w => w.code === phuong);
    if (ward) {
      wardType = ward.type || "Xã";
      wardName = ward.name || "";
    }
  }
  
  // Find province type and name
  let provinceType = "Tỉnh"; // Default
  let provinceName = "";
  if (tinh && provinces.length > 0) {
    const province = provinces.find(p => p.code === tinh);
    if (province) {
      provinceType = province.type || "Tỉnh";
      provinceName = province.name || "";
    }
  }
  
  // Build address parts
  const parts = [
    chiTiet,
    wardName ? `${wardType} ${wardName}` : "",
    provinceName ? `${provinceType} ${provinceName}` : "",
  ];
  
  return parts.filter(part => part && part.trim()).join(", ");
};

/**
 * Format address string (for display in tables)
 * @param {string} diaChi - Address string from database
 * @param {Object} wardTypes - Map of ward names to types
 * @returns {string} Formatted address string
 */
export const formatAddressString = (diaChi, wardTypes = {}) => {
  if (!diaChi || typeof diaChi !== 'string') return 'N/A';
  
  const parts = diaChi.split(',').map(part => part.trim()).filter(part => part);
  
  if (parts.length === 0) return 'N/A';
  if (parts.length === 1) return parts[0];
  
  // If we have ward types from API, use them
  if (Object.keys(wardTypes).length > 0) {
    if (parts.length === 2) {
      const [chiTiet, tinh] = parts;
      return `${chiTiet}, Tỉnh ${tinh}`;
    }
    
    if (parts.length >= 3) {
      const chiTiet = parts[0];
      const xaPhuong = parts[parts.length - 2];
      const tinh = parts[parts.length - 1];
      
      const wardType = wardTypes[xaPhuong] || 'Xã';
      return `${chiTiet}, ${wardType} ${xaPhuong}, Tỉnh ${tinh}`;
    }
  }
  
  // Fallback logic
  if (parts.length === 2) {
    const [chiTiet, tinh] = parts;
    return `${chiTiet}, Tỉnh ${tinh}`;
  }
  
  if (parts.length >= 3) {
    const chiTiet = parts[0];
    const xaPhuong = parts[parts.length - 2];
    const tinh = parts[parts.length - 1];
    
    // Try to determine ward type from name
    let wardType = 'Xã';
    const lowerWard = xaPhuong.toLowerCase();
    if (lowerWard.includes('phường') || lowerWard.includes('p.')) {
      wardType = 'Phường';
    } else if (lowerWard.includes('thị trấn')) {
      wardType = 'Thị trấn';
    }
    
    return `${chiTiet}, ${wardType} ${xaPhuong}, Tỉnh ${tinh}`;
  }
  
  return parts.join(', ');
};

/**
 * Get ward type from ward name using API data
 * @param {string} wardName - Name of the ward
 * @param {Object} wardTypes - Map of ward names to types
 * @returns {string} Ward type (Phường/Xã/Thị trấn)
 */
export const getWardType = (wardName, wardTypes = {}) => {
  if (!wardName) return 'Xã';
  
  // Check API data first
  if (wardTypes[wardName]) {
    return wardTypes[wardName];
  }
  
  // Fallback logic
  const lowerWard = wardName.toLowerCase();
  if (lowerWard.includes('phường') || lowerWard.includes('p.')) {
    return 'Phường';
  } else if (lowerWard.includes('thị trấn')) {
    return 'Thị trấn';
  }
  
  return 'Xã';
};

/**
 * Get province type from province name using API data
 * @param {string} provinceName - Name of the province
 * @param {Object} provinceTypes - Map of province names to types
 * @returns {string} Province type (Tỉnh/Thành phố)
 */
export const getProvinceType = (provinceName, provinceTypes = {}) => {
  if (!provinceName) return 'Tỉnh';
  
  // Check API data first
  if (provinceTypes[provinceName]) {
    return provinceTypes[provinceName];
  }
  
  // Fallback logic
  const lowerProvince = provinceName.toLowerCase();
  if (lowerProvince.includes('thành phố') || lowerProvince.includes('tp.') || lowerProvince.includes('tp ')) {
    return 'Thành phố';
  }
  
  return 'Tỉnh';
};
