<template>
    <div>
      <p>Đang xử lý đăng nhập...</p>
    </div>
  </template>
  
  <script setup>
  import { useRoute, useRouter } from 'vue-router'
  
  const route = useRoute()
  const router = useRouter()
  
  const token = route.query.token
  
  if (token) {
    localStorage.setItem('token', token)
    router.replace('/') // chuyển về trang chính
  } else {
    alert("Đăng nhập thất bại. Không nhận được token.")
    router.replace('/login')
  }
  </script>
  