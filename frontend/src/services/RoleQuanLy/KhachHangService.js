import client from '@/utils/api.js';
const API_BASE_URL = '/api/khach-hang';
const ADDRESS_API_BASE_URL = '/api/customers';

export default {
  getAllKhachHang(params = {}) {
    const cleanParams = {
      page: params.page || 0,
      size: params.size || 5,
      ...params
    };

    Object.keys(cleanParams).forEach(key => {
      if (cleanParams[key] === '' || cleanParams[key] === null || cleanParams[key] === undefined) {
        delete cleanParams[key];
      }
    });

    return client.get(`${API_BASE_URL}/getAll`, { params: cleanParams });
  },

  getKhachHangById(id) {
    return client.get(`${API_BASE_URL}/${id}`);
  },

  createKhachHang(data) {
    return client.post(`${API_BASE_URL}/create`, data, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  },

  updateKhachHang(id, data) {
    return client.put(`${API_BASE_URL}/update/${id}`, data, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  },

  deleteKhachHang(id) {
    return client.delete(`${API_BASE_URL}/delete/${id}`);
  },

  resetPassword(id) {
    return client.post(`${API_BASE_URL}/reset-password/${id}`);
  },

  exportExcel() {
    return client.get(`${API_BASE_URL}/export-excel`, {
      responseType: 'blob'
    })
      .then(response => {
        if (response.status === 200 && response.data.type === 'application/octet-stream') {
          const url = window.URL.createObjectURL(new Blob([response.data]));
          const link = document.createElement('a');
          link.href = url;
          link.setAttribute('download', 'khach_hang.xlsx');
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          window.URL.revokeObjectURL(url);
          return response;
        } else {
          throw new Error('Phản hồi không phải file Excel');
        }
      })
      .catch(async error => {
        let errorMessage = 'Không thể xuất file Excel';
        if (error.response && error.response.data) {
          try {
            const text = await error.response.data.text();
            const errorData = JSON.parse(text);
            errorMessage = errorData.message || error.message || errorMessage;
          } catch (e) {
            console.error('DEBUG: Failed to parse error response:', e);
          }
        }
        console.error('Export Excel API Error:', errorMessage);
        throw new Error(errorMessage);
      });
  },

  importExcel(file) {
    const formData = new FormData();
    formData.append('file', file);

    return client.post(`${API_BASE_URL}/import-excel`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
      .then(response => {
        console.log('Import Excel API Response:', response);
        return response;
      })
      .catch(error => {
        console.error('Import Excel API Error:', error);
        throw error;
      });
  },

  downloadTemplate() {
    return client.get(`${API_BASE_URL}/download-template`, {
      responseType: 'blob'
    })
      .then(response => {
        if (response.status === 200 && response.data.type === 'application/octet-stream') {
          const url = window.URL.createObjectURL(new Blob([response.data]));
          const link = document.createElement('a');
          link.href = url;
          link.setAttribute('download', 'template_khach_hang.xlsx');
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          window.URL.revokeObjectURL(url);
          return response;
        } else {
          throw new Error('Phản hồi không phải file Excel');
        }
      })
      .catch(async error => {
        let errorMessage = 'Không thể tải template Excel';
        if (error.response && error.response.data) {
          try {
            const text = await error.response.data.text();
            const errorData = JSON.parse(text);
            errorMessage = errorData.message || error.message || errorMessage;
          } catch (e) {
            console.error('DEBUG: Failed to parse error response:', e);
          }
        }
        console.error('Download Template API Error:', errorMessage);
        throw new Error(errorMessage);
      });
  },

  getAddressesByCustomerId(customerId) {
    return client.get(`${ADDRESS_API_BASE_URL}/${customerId}/addresses`);
  },

  addAddress(customerId, data) {
    return client.post(`${ADDRESS_API_BASE_URL}/${customerId}/addresses`, data);
  },

  updateAddress(customerId, addressId, addressData) {
    return client.put(`${ADDRESS_API_BASE_URL}/${customerId}/addresses/${addressId}`, addressData);
  },

  deleteAddress(customerId, addressId) {
    return client.delete(`${ADDRESS_API_BASE_URL}/${customerId}/addresses/${addressId}`);
  },

  setDefaultAddress(customerId, addressId) {
    return client.patch(`${ADDRESS_API_BASE_URL}/${customerId}/addresses/${addressId}/set-default`);
  },

  getProvinces() {
    // Fetch all pages to avoid truncation
    const fetchAllPages = async (baseUrl) => {
      const results = [];
      const limit = 100;
      let page = 1;
      for (let i = 0; i < 100; i++) {
        const url = new URL(baseUrl);
        url.search = new URLSearchParams({ limit: String(limit), page: String(page) }).toString();
        const res = await fetch(url.toString());
        const json = await res.json();
        const items = Array.isArray(json)
          ? json
          : (Array.isArray(json.data) ? json.data
          : Array.isArray(json.items) ? json.items
          : Array.isArray(json.results) ? json.results
          : Array.isArray(json.records) ? json.records
          : []);
        results.push(...items);
        if (items.length < limit) break;
        page += 1;
      }
      return results;
    };

    return fetchAllPages('https://tinhthanhpho.com/api/v1/new-provinces')
      .then(items => {
        const mapped = items.map(p => ({
          ProvinceID: p.code,
          ProvinceName: p.name,
          // Extra fields for richer formatting (non-breaking for existing UI)
          code: p.code,
          name: p.name,
          type: p.type
        }));
        const responseLike = { data: { data: mapped } };
        return responseLike;
      })
      .catch(error => {
        console.error('Get Provinces API Error:', error);
        throw error;
      });
  },

  getDistricts(/* provinceId */) {
    // External API does not provide districts in provided spec -> return empty list for compatibility
    const responseLike = { data: { data: [] } };
    return Promise.resolve(responseLike);
  },

  getWards(provinceCode) {
    // Fetch all pages of wards by province
    const fetchAllPages = async (baseUrl) => {
      const results = [];
      const limit = 100;
      let page = 1;
      for (let i = 0; i < 100; i++) {
        const url = new URL(baseUrl);
        url.search = new URLSearchParams({ limit: String(limit), page: String(page) }).toString();
        const res = await fetch(url.toString());
        const json = await res.json();
        const items = Array.isArray(json)
          ? json
          : (Array.isArray(json.data) ? json.data
          : Array.isArray(json.items) ? json.items
          : Array.isArray(json.results) ? json.results
          : Array.isArray(json.records) ? json.records
          : Array.isArray(json.wards) ? json.wards
          : []);
        results.push(...items);
        if (items.length < limit) break;
        page += 1;
      }
      return results;
    };

    return fetchAllPages(`https://tinhthanhpho.com/api/v1/new-provinces/${provinceCode}/wards`)
      .then(items => {
        const mapped = items.map(w => ({
          WardCode: w.code,
          WardName: w.name,
          // Extra fields for richer formatting
          code: w.code,
          name: w.name,
          type: w.type
        }));
        const responseLike = { data: { data: mapped } };
        return responseLike;
      })
      .catch(error => {
        console.error('Get Wards API Error:', error);
        throw error;
      });
  }
};
