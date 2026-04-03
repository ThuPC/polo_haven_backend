```vue
<template>
  <div v-if="show" class="modal-overlay">
    <div class="modal-box">
      <div class="modal-header">
        <h5 class="modal-title">{{ title }}</h5>
        <button type="button" class="btn-close" @click="closeModal">×</button>
      </div>
      <div class="modal-body">
        <p>{{ message }}</p>
        <div v-if="showInput" class="mb-3">
          <label for="modal-input" class="form-label">{{ inputLabel }}</label>
          <input
            id="modal-input"
            v-model="inputValue"
            type="text"
            class="form-control"
            :placeholder="inputLabel"
          />
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-secondary" @click="closeModal">Hủy</button>
        <button class="btn btn-primary" @click="confirmAction">Xác nhận</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  show: Boolean,
  title: String,
  message: String,
  showInput: Boolean,
  inputLabel: String,
});

const emit = defineEmits(['confirm', 'close']);

const inputValue = ref('');

const confirmAction = () => {
  emit('confirm', inputValue.value);
  inputValue.value = ''; // Reset input
};

const closeModal = () => {
  emit('close');
  inputValue.value = ''; // Reset input
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
}

.modal-box {
  background: #fff;
  border-radius: 8px;
  width: 500px;
  max-width: 90%;
  padding: 1rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.modal-header,
.modal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-body {
  padding: 1rem 0;
}

.btn-close {
  border: none;
  background: transparent;
  font-size: 1.5rem;
  cursor: pointer;
}

.form-control {
  width: 100%;
  padding: 8px;
  border: 1px solid #dfe1e6;
  border-radius: 4px;
}
</style>
```