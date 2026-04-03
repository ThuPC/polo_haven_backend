<template>
  <div class="container mt-4">
    <h3>Danh sách yêu cầu trả hàng</h3>
    <div v-if="loading">Đang tải...</div>
    <div v-else>
      <div v-if="requests.length === 0">Chưa có yêu cầu trả hàng</div>
      <div v-for="r in requests" :key="r.id" class="card mb-3">
        <div class="card-body">
          <div class="d-flex justify-content-between">
            <div>
              <h5>Hóa đơn: {{ r.hoaDon?.id || '---' }}</h5>
              <p><strong>Khách:</strong> {{ r.khachHangId }}</p>
              <p><strong>Lý do:</strong> {{ r.lyDo }}</p>
              <p><strong>Trạng thái:</strong> {{ statusText(r.status) }}</p>
            </div>
            <div class="text-end">
              <small>Yêu cầu: {{ formatDate(r.requestedAt) }}</small>
              <div class="mt-2">
                <button class="btn btn-success btn-sm me-1" @click="review(r.id, 1)">Nguyên vẹn</button>
                <button class="btn btn-danger btn-sm" @click="review(r.id, 2)">Lỗi</button>
              </div>
            </div>
          </div>

          <div class="mt-3">
            <div v-if="r.mediaUrls && r.mediaUrls.length > 0" class="d-flex flex-wrap">
              <div v-for="(m, idx) in r.mediaUrls" :key="idx" class="me-2 mb-2">
                <img v-if="isImage(m)" :src="absoluteUrl(m)" style="width:150px; height:150px; object-fit:cover;" 
                     @error="$event.target.src = '/default-avatar.png'" />
                <video v-else controls :src="absoluteUrl(m)" style="width:240px; height:150px;" />
              </div>
            </div>
            <div v-else class="text-muted">Không có ảnh/video đính kèm</div>
          </div>

          <div v-if="r.adminNote" class="mt-2">
            <strong>Ghi chú admin:</strong> {{ r.adminNote }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/utils/api'

const requests = ref([])
const loading = ref(false)

function absoluteUrl(path) {
  if (!path) return ''
  if (path.startsWith('http')) return path
  // Sử dụng backend URL thay vì frontend URL
  return `http://localhost:8080/uploads/${path}`
}

function isImage(url) {
  return url.match(/\.(jpeg|jpg|gif|png|webp)$/i)
}

function statusText(s) {
  if (s === null || s === undefined) return 'Chưa xử lý'
  if (s === 0) return 'Mới'
  if (s === 1) return 'Nguyên vẹn'
  if (s === 2) return 'Lỗi'
  return '' + s
}

function formatDate(iso) {
  if (!iso) return '-'
  const d = new Date(iso)
  return d.toLocaleString()
}

async function load() {
  loading.value = true
  try {
    const res = await api.get('/admin/tra-hang/requests')
    // backend stores mediaUrls as JSON string in mediaUrlsJson; the controller returns entity with mediaUrlsJson => need to parse if present
    requests.value = res.data.map(item => {
      const parsed = { ...item }
      if (parsed.mediaUrlsJson && typeof parsed.mediaUrlsJson === 'string') {
        try { parsed.mediaUrls = JSON.parse(parsed.mediaUrlsJson) } catch(e) { parsed.mediaUrls = [] }
      } else {
        parsed.mediaUrls = parsed.mediaUrls || []
      }
      return parsed
    })
  } catch (e) {
    console.error('Load requests failed', e)
    requests.value = []
  } finally {
    loading.value = false
  }
}

async function review(id, newStatus) {
  const adminName = prompt('Nhập tên admin (hiển thị trong lịch sử):', 'Admin') || 'Admin'
  const adminNote = prompt('Ghi chú (tuỳ chọn):', '') || ''
  try {
    await api.post(`/admin/tra-hang/review/${id}?newStatus=${newStatus}&adminName=${encodeURIComponent(adminName)}&adminNote=${encodeURIComponent(adminNote)}`)
    alert('Cập nhật thành công')
    await load()
  } catch (e) {
    console.error(e)
    alert('Cập nhật thất bại')
  }
}

onMounted(load)
</script>

<style scoped>
.card { border-radius: 8px }
</style>
