import { createApp } from 'vue'
import App from './App.vue'
import router from './router/user.js'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate' 
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import 'bootstrap-icons/font/bootstrap-icons.css'
import * as bootstrap from 'bootstrap/dist/js/bootstrap.bundle.min.js'
import Toast, { POSITION } from 'vue-toastification'
import 'vue-toastification/dist/index.css'
import "./assets/css/nucleo-icons.css";
import "./assets/css/nucleo-svg.css";
import '@fortawesome/fontawesome-free/css/all.css';

window.bootstrap = bootstrap

const app = createApp(App)

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
app.use(router)
app.use(pinia) 
app.use(Toast, {
  position: POSITION.TOP_RIGHT,
  timeout: 2500
})

app.mount('#app')
