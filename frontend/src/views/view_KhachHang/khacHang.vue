<script setup>

import { onMounted } from "vue";
import { jwtDecode } from "jwt-decode";
onMounted(()=>{
  const token= localStorage.getItem('authToken');
  if (token) {
        localStorage.setItem('authToken', token)
        const decodeToken = jwtDecode(token)
        const expiryTime = decodeToken.exp;
        const currentTime = Date.now() / 1000;
        if (expiryTime < currentTime) {
            localStorage.clear();
        }
    } else {
        console.error('Không tìm thấy token trong URL.')
    }
})
</script>
<template>
  <h1>Trang khách hàng test</h1>
</template>
