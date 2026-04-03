import client from '../../utils/api';

const GHN_BASE_URL = '/api/v1/ghn';

export default {
  // Lấy danh sách tỉnh/thành phố
  async getProvinces() {
    try {
      const response = await client.get(`${GHN_BASE_URL}/provinces`);
      return response.data;
    } catch (error) {
      console.error('Lỗi khi lấy danh sách tỉnh:', error);
      throw error;
    }
  },

  // Lấy danh sách quận/huyện
  async getDistricts(provinceId) {
    try {
      const response = await client.get(`${GHN_BASE_URL}/districts/${provinceId}`);
      return response.data;
    } catch (error) {
      console.error('Lỗi khi lấy danh sách quận/huyện:', error);
      throw error;
    }
  },

  // Lấy danh sách phường/xã
  async getWards(districtId) {
    try {
      const response = await client.get(`${GHN_BASE_URL}/wards/${districtId}`);
      return response.data;
    } catch (error) {
      console.error('Lỗi khi lấy danh sách phường/xã:', error);
      throw error;
    }
  },

  // Tính phí vận chuyển trực tiếp từ GHN API
  async calculateShippingFee(requestData) {
    try {
      const response = await client.post(`${GHN_BASE_URL}/shipping-fee`, requestData);
      return response.data;
    } catch (error) {
      console.error('Lỗi khi tính phí vận chuyển:', error);
      throw error;
    }
  },

  // Tính phí vận chuyển theo vùng khi không có API GHN
  async calculateRegionalShippingFee(toDistrictId) {
    try {
      const response = await client.post(`${GHN_BASE_URL}/regional-shipping-fee`, {
        to_district_id: toDistrictId
      });
      return response.data;
    } catch (error) {
      console.error('Lỗi khi tính phí vận chuyển theo vùng:', error);
      throw error;
    }
  },

  // Tính phí vận chuyển thông minh với fallback
  async calculateSmartShippingFee(address, products = []) {
    try {
      // Tính tổng trọng lượng từ sản phẩm
      const totalWeight = this.calculateTotalWeight(products);
      
      // Tính kích thước từ sản phẩm
      const dimensions = this.calculateDimensions(products);
      
      // Thử tính phí với API GHN trước
      const requestData = {
        from_district_id: 202, // Hà Nội
        to_district_id: parseInt(address.districtId),
        to_ward_code: address.wardCode,
        weight: totalWeight,
        height: dimensions.height,
        length: dimensions.length,
        width: dimensions.width
      };

      const result = await this.calculateShippingFee(requestData);
      
      return {
        ...result,
        serviceInfo: { 
          service_name: 'Giao hàng nhanh',
          expected_delivery_time: '2-3 ngày làm việc'
        },
        calculatedWeight: totalWeight,
        calculatedDimensions: dimensions,
        isGHNFee: true
      };

    } catch (error) {
      console.warn('API GHN không khả dụng, sử dụng phí theo vùng');
      
      try {
        const regionalResult = await this.calculateRegionalShippingFee(parseInt(address.districtId));
        return {
          ...regionalResult,
          serviceInfo: { 
            service_name: 'Giao hàng nhanh (Phí theo vùng)',
            expected_delivery_time: '3-5 ngày làm việc'
          },
          calculatedWeight: this.calculateTotalWeight(products),
          calculatedDimensions: this.calculateDimensions(products),
          isRegionalFee: true
        };
      } catch (regionalError) {
        console.warn('Không thể tính phí theo vùng, sử dụng phí mặc định');
        return {
          data: { total: 30000 },
          serviceInfo: { 
            service_name: 'Giao hàng nhanh (Mặc định)',
            expected_delivery_time: '5-7 ngày làm việc'
          },
          calculatedWeight: this.calculateTotalWeight(products),
          calculatedDimensions: this.calculateDimensions(products),
          isFallback: true
        };
      }
    }
  },

  // Tính tổng trọng lượng từ danh sách sản phẩm
  calculateTotalWeight(products) {
    if (!products || products.length === 0) {
      return 200; // Trọng lượng mặc định 200g
    }
    
    let totalWeight = 0;
    products.forEach(product => {
      // Ước tính trọng lượng dựa trên kích thước sản phẩm
      const weightPerItem = this.estimateProductWeight(product);
      totalWeight += weightPerItem * product.quantity;
    });
    
    return Math.max(totalWeight, 200); // Tối thiểu 200g
  },

  // Ước tính trọng lượng sản phẩm dựa trên kích thước
  estimateProductWeight(product) {
    const size = product.kichThuoc || '';
    const sizeLower = size.toLowerCase();
    
    if (sizeLower.includes('s') || sizeLower.includes('xs')) {
      return 150; // 150g cho size nhỏ
    } else if (sizeLower.includes('m')) {
      return 200; // 200g cho size trung bình
    } else if (sizeLower.includes('l') || sizeLower.includes('xl')) {
      return 250; // 250g cho size lớn
    } else if (sizeLower.includes('xxl') || sizeLower.includes('2xl')) {
      return 300; // 300g cho size rất lớn
    } else {
      return 200; // Mặc định 200g
    }
  },

  // Tính kích thước gói hàng từ danh sách sản phẩm
  calculateDimensions(products) {
    if (!products || products.length === 0) {
      return { height: 10, length: 20, width: 20 }; // Kích thước mặc định
    }
    
    // Tính toán kích thước dựa trên số lượng sản phẩm
    const totalItems = products.reduce((sum, product) => sum + product.quantity, 0);
    
    if (totalItems <= 2) {
      return { height: 10, length: 20, width: 20 };
    } else if (totalItems <= 5) {
      return { height: 15, length: 25, width: 25 };
    } else if (totalItems <= 10) {
      return { height: 20, length: 30, width: 30 };
    } else {
      return { height: 25, length: 35, width: 35 };
    }
  }
}; 