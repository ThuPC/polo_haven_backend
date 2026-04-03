<template>
  <div class="admin-chat-test">
    <h3>Admin Chat Debug</h3>
    
    <div class="debug-info">
      <h4>Admin Info:</h4>
      <p>Admin ID: {{ adminId || 'Not found' }}</p>
      <p>Admin Name: {{ adminName || 'Not found' }}</p>
      <p>Token: {{ hasToken ? 'Found' : 'Not found' }}</p>
    </div>
    
    <div class="debug-actions">
      <button @click="testGetAllRooms" class="btn btn-primary me-2">
        Test Get All Rooms
      </button>
      <button @click="testGetAdminRooms" class="btn btn-secondary me-2">
        Test Get Admin Rooms
      </button>
      <button @click="reloadAdminInfo" class="btn btn-info">
        Reload Admin Info
      </button>
    </div>
    
    <div class="debug-results">
      <h4>API Results:</h4>
      <pre>{{ apiResults }}</pre>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import api from '@/utils/api'

export default {
  name: 'AdminChatTest',
  setup() {
    const adminId = ref(null)
    const adminName = ref(null)
    const hasToken = ref(false)
    const apiResults = ref('')

    const getAdminInfo = () => {
      const token = localStorage.getItem('authToken')
      hasToken.value = !!token
      
      if (token) {
        try {
          const decoded = JSON.parse(atob(token.split('.')[1]))
          adminId.value = decoded.sub
          adminName.value = decoded.name || 'Admin'
          
          apiResults.value += `Decoded token: ${JSON.stringify(decoded, null, 2)}\n`
        } catch (error) {
          apiResults.value += `Error decoding token: ${error.message}\n`
        }
      }
    }

    const testGetAllRooms = async () => {
      try {
        apiResults.value += '\n--- Testing Get All Rooms ---\n'
        const response = await api.get('/api/chat/room/all')
        apiResults.value += `Status: ${response.status}\n`
        apiResults.value += `Data: ${JSON.stringify(response.data, null, 2)}\n`
      } catch (error) {
        apiResults.value += `Error: ${error.message}\n`
        if (error.response) {
          apiResults.value += `Status: ${error.response.status}\n`
          apiResults.value += `Data: ${JSON.stringify(error.response.data, null, 2)}\n`
        }
      }
    }

    const testGetAdminRooms = async () => {
      if (!adminId.value) {
        apiResults.value += '\nError: Admin ID not found\n'
        return
      }
      
      try {
        apiResults.value += `\n--- Testing Get Admin Rooms for ID: ${adminId.value} ---\n`
        const response = await api.get(`/api/chat/room/admin/${adminId.value}`)
        apiResults.value += `Status: ${response.status}\n`
        apiResults.value += `Data: ${JSON.stringify(response.data, null, 2)}\n`
      } catch (error) {
        apiResults.value += `Error: ${error.message}\n`
        if (error.response) {
          apiResults.value += `Status: ${error.response.status}\n`
          apiResults.value += `Data: ${JSON.stringify(error.response.data, null, 2)}\n`
        }
      }
    }

    const reloadAdminInfo = () => {
      apiResults.value = ''
      getAdminInfo()
    }

    onMounted(() => {
      getAdminInfo()
    })

    return {
      adminId,
      adminName,
      hasToken,
      apiResults,
      testGetAllRooms,
      testGetAdminRooms,
      reloadAdminInfo
    }
  }
}
</script>

<style scoped>
.admin-chat-test {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.debug-info {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 5px;
  margin-bottom: 20px;
}

.debug-actions {
  margin-bottom: 20px;
}

.debug-results {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 5px;
  max-height: 400px;
  overflow-y: auto;
}

.debug-results pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
  font-size: 12px;
}
</style> 