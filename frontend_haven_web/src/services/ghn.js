import client from '@/utils/api'

// Căn chỉnh endpoint giống file GHNService của sell.vue
const GHN_BASE_URL = '/api/v1/ghn'

export const ghnService = {
  // Lấy danh sách tỉnh/thành phố
  async getProvinces() {
    try {
      const response = await client.get(`${GHN_BASE_URL}/provinces`)
      return response.data
    } catch (error) {
      console.error('Lỗi khi lấy danh sách tỉnh:', error)
      throw error
    }
  },

  // Lấy danh sách quận/huyện
  async getDistricts(provinceId) {
    try {
      const response = await client.get(`${GHN_BASE_URL}/districts/${provinceId}`)
      return response.data
    } catch (error) {
      console.error('Lỗi khi lấy danh sách quận/huyện:', error)
      throw error
    }
  },

  // Lấy danh sách phường/xã
  async getWards(districtId) {
    try {
      const response = await client.get(`${GHN_BASE_URL}/wards/${districtId}`)
      return response.data
    } catch (error) {
      console.error('Lỗi khi lấy danh sách phường/xã:', error)
      throw error
    }
  },

  // Tính phí vận chuyển trực tiếp (khi đã có đủ tham số GHN)
  async calculateShippingFee(requestData) {
    try {
      // Back-compat: nếu người gọi truyền { address, products } thì chuyển sang smart calc
      if (requestData && requestData.address && requestData.products) {
        return await this.calculateSmartShippingFee(requestData.address, requestData.products)
      }
      const response = await client.post(`${GHN_BASE_URL}/shipping-fee`, requestData)
      return response.data
    } catch (error) {
      console.error('Lỗi khi tính phí vận chuyển:', error)
      throw error
    }
  },

  // Tính phí vận chuyển theo vùng (fallback trên backend)
  async calculateRegionalShippingFee(toDistrictId) {
    try {
      const response = await client.post(`${GHN_BASE_URL}/regional-shipping-fee`, {
        to_district_id: toDistrictId
      })
      return response.data
    } catch (error) {
      console.error('Lỗi khi tính phí vận chuyển theo vùng:', error)
      throw error
    }
  },

  // Tính phí vận chuyển thông minh với fallback (giống sell.vue)
  async calculateSmartShippingFee(address, products = []) {
    try {
      const totalWeight = this.calculateTotalWeight(products)
      const dimensions = this.calculateDimensions(products)

      const requestData = {
        from_district_id: 202, // ví dụ cấu hình mặc định tại cửa hàng
        to_district_id: parseInt(address.districtId),
        to_ward_code: address.wardCode,
        weight: totalWeight,
        height: dimensions.height,
        length: dimensions.length,
        width: dimensions.width
      }

      const result = await this.calculateShippingFee(requestData)
      return {
        ...result,
        serviceInfo: {
          service_name: 'Giao hàng nhanh',
          expected_delivery_time: '2-3 ngày làm việc'
        },
        calculatedWeight: totalWeight,
        calculatedDimensions: dimensions,
        isGHNFee: true
      }
    } catch (error) {
      console.warn('API GHN không khả dụng, sử dụng phí theo vùng')
      try {
        const regionalResult = await this.calculateRegionalShippingFee(parseInt(address.districtId))
        return {
          ...regionalResult,
          serviceInfo: {
            service_name: 'Giao hàng nhanh (Phí theo vùng)',
            expected_delivery_time: '3-5 ngày làm việc'
          },
          calculatedWeight: this.calculateTotalWeight(products),
          calculatedDimensions: this.calculateDimensions(products),
          isRegionalFee: true
        }
      } catch (regionalError) {
        console.warn('Không thể tính phí theo vùng, sử dụng phí mặc định')
        return {
          data: { total: 30000 },
          serviceInfo: {
            service_name: 'Giao hàng nhanh (Mặc định)',
            expected_delivery_time: '5-7 ngày làm việc'
          },
          calculatedWeight: this.calculateTotalWeight(products),
          calculatedDimensions: this.calculateDimensions(products),
          isFallback: true
        }
      }
    }
  },

  // Helpers: tính tổng trọng lượng dựa trên sản phẩm
  calculateTotalWeight(products) {
    if (!products || products.length === 0) {
      return 200
    }
    let totalWeight = 0
    products.forEach((product) => {
      const weightPerItem = this.estimateProductWeight(product)
      const qty = product.quantity || product.soLuong || 1
      totalWeight += weightPerItem * qty
    })
    return Math.max(totalWeight, 200)
  },

  // Ước tính trọng lượng mỗi sản phẩm theo kích cỡ
  estimateProductWeight(product) {
    const size = (product.kichThuoc || product.size || '').toString().toLowerCase()
    if (size.includes('xs') || size.includes('s')) return 150
    if (size.includes('m')) return 200
    if (size.includes('xl') || size.includes('l')) return 250
    if (size.includes('xxl') || size.includes('2xl')) return 300
    return 200
  },

  // Tính kích thước gói hàng đơn giản theo tổng số lượng
  calculateDimensions(products) {
    if (!products || products.length === 0) {
      return { height: 10, length: 20, width: 20 }
    }
    const totalItems = products.reduce((sum, p) => sum + (p.quantity || p.soLuong || 1), 0)
    if (totalItems <= 2) return { height: 10, length: 20, width: 20 }
    if (totalItems <= 5) return { height: 15, length: 25, width: 25 }
    if (totalItems <= 10) return { height: 20, length: 30, width: 30 }
    return { height: 25, length: 35, width: 35 }
  }
}