import client from '@/utils/api.js';

const API_BASE_URL = '/api/nhan-vien';

export default {
  getAllNhanVien(params = {}) {
    const cleanParams = {
      page: params.page || 0,
      size: params.size || 5,
      ...params
    };

    // Remove empty or invalid parameters
    Object.keys(cleanParams).forEach(key => {
      if (cleanParams[key] === '' || cleanParams[key] === null || cleanParams[key] === undefined) {
        delete cleanParams[key];
      }
    });

    return client.get(`${API_BASE_URL}/getAll`, { params: cleanParams })
      .then(response => {
        console.log('GetAll API Response:', response);
        return response;
      })
      .catch(error => {
        console.error('GetAll API Error:', error);
        throw error;
      });
  },

  getNhanVienById(id) {
    return client.get(`${API_BASE_URL}/${id}`)
      .then(response => {
        console.log('GetById API Response:', response);
        return response;
      })
      .catch(error => {
        console.error('GetById API Error:', error);
        throw error;
      });
  },

  createNhanVien(data) {
    return client.post(`${API_BASE_URL}/create`, data, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
      .then(response => {
        console.log('Create API Response:', response);
        return response;
      })
      .catch(error => {
        console.error('Create API Error:', error);
        throw error;
      });
  },

  updateNhanVien(id, data) {
    return client.put(`${API_BASE_URL}/update/${id}`, data, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
      .then(response => {
        console.log('Update API Response:', response);
        return response;
      })
      .catch(error => {
        console.error('Update API Error:', error);
        throw error;
      });
  },

  deleteNhanVien(id) {
    return client.delete(`${API_BASE_URL}/delete/${id}`)
      .then(response => {
        console.log('Delete API Response:', response);
        return response;
      })
      .catch(error => {
        console.error('Delete API Error:', error);
        throw error;
      });
  },

  resetPassword(id) {
    return client.post(`${API_BASE_URL}/reset-password/${id}`)
      .then(response => {
        console.log('Reset password API Response:', response);
        return response;
      })
      .catch(error => {
        console.error('Reset password API Error:', error);
        throw error;
      });
  },

  exportExcel(params = {}) {
    return client.get(`${API_BASE_URL}/export-excel`, {
      params,
      responseType: 'blob'
    })
      .then(response => {
        if (response.status === 200 && response.data.type === 'application/octet-stream') {
          const url = window.URL.createObjectURL(new Blob([response.data]));
          const link = document.createElement('a');
          link.href = url;
          link.setAttribute('download', 'nhan_vien.xlsx');
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
        if (error.response && error.response.data) {
          // Đọc phản hồi lỗi nếu server trả về JSON
          const text = await error.response.data.text();
          const errorMessage = JSON.parse(text)?.message || error.message;
          console.error('Export Excel API Error:', errorMessage);
          throw new Error(errorMessage);
        }
        console.error('Export Excel API Error:', error);
        throw error;
      });
  },
  downloadTemplate() {
    return client.get(`${API_BASE_URL}/download-template`, { responseType: 'blob' })
      .then(response => {
        if (response.status === 200 && response.data.type === 'application/octet-stream') {
          const url = window.URL.createObjectURL(new Blob([response.data]));
          const link = document.createElement('a');
          link.href = url;
          link.setAttribute('download', 'template_nhan_vien.xlsx');
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
        if (error.response && error.response.data) {
          const text = await error.response.data.text();
          const errorMessage = JSON.parse(text)?.message || error.message;
          console.error('Download Template API Error:', errorMessage);
          throw new Error(errorMessage);
        }
        console.error('Download Template API Error:', error);
        throw error;
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
  }
};