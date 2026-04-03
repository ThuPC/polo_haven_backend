<template>
    <div>Đang đăng nhập...</div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { jwtDecode } from 'jwt-decode'


const router = useRouter()
onMounted(() => {
    const params = new URLSearchParams(window.location.search)
    const token = params.get('token')
    console.log("token: "+token)
    // const selectedRole = localStorage.getItem('selected_role')

    if (token) {
        localStorage.setItem('authToken', token)
        window.dispatchEvent(new CustomEvent('auth-change'));
        const decodeToken = jwtDecode(token)
        const roleByUser = decodeToken.scope;
        const expiryTime = decodeToken.exp;
        const currentTime = Date.now() / 1000;
        if (expiryTime < currentTime) {
            localStorage.clear();
              router.push('/401unauthenticated');
            // token hết hạn mẹ nó rồi
        } else {
            if (roleByUser === 'ROLE_QUANLY') {
                router.push('/thong-ke')
            }
            if(roleByUser==='ROLE_NHANVIEN'){
                router.push('/banHangTaiQuay')
            }
        }
    } else {
        // Không có token trong URL
        console.error('Không tìm thấy token trong URL.');
        router.push('/403unauthorize');
    }
})
</script>