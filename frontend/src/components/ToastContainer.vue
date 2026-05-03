<script setup>
import { useToast } from '@/composables/useToast'

const { toasts, dismiss } = useToast()
</script>

<template>
  <Teleport to="body">
    <div class="toast-container">
      <div
        v-for="t in toasts"
        :key="t.id"
        class="toast-item"
        :class="[`toast-${t.type}`, { 'toast-leaving': t.leaving }]"
        @click="dismiss(t.id)"
      >
        <span class="toast-icon">
          <template v-if="t.type === 'success'">✓</template>
          <template v-else-if="t.type === 'error'">✕</template>
          <template v-else-if="t.type === 'warn'">⚠</template>
          <template v-else>ℹ</template>
        </span>
        <span class="toast-msg">{{ t.message }}</span>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-container {
  position: fixed;
  top: env(safe-area-inset-top, 16px);
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding-top: 16px;
  pointer-events: none;
  width: 92vw;
  max-width: 380px;
}
.toast-item {
  pointer-events: auto;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.7rem 1rem;
  border-radius: 12px;
  font-size: 0.92rem;
  font-weight: 500;
  box-shadow: 0 8px 28px -6px rgba(0, 0, 0, 0.18);
  cursor: pointer;
  animation: toast-in 0.3s ease;
  width: 100%;
}
.toast-leaving {
  animation: toast-out 0.3s ease forwards;
}
.toast-icon {
  flex-shrink: 0;
  width: 1.3rem;
  text-align: center;
  font-weight: 700;
}
.toast-msg {
  flex: 1;
  line-height: 1.4;
}
.toast-success {
  background: #ecfdf5;
  color: #065f46;
  border: 1px solid #a7f3d0;
}
.toast-error {
  background: #fef2f2;
  color: #991b1b;
  border: 1px solid #fecaca;
}
.toast-warn {
  background: #fffbeb;
  color: #92400e;
  border: 1px solid #fde68a;
}
.toast-info {
  background: #eff6ff;
  color: #1e40af;
  border: 1px solid #bfdbfe;
}
@keyframes toast-in {
  from {
    opacity: 0;
    transform: translateY(-12px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
@keyframes toast-out {
  from {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
  to {
    opacity: 0;
    transform: translateY(-12px) scale(0.96);
  }
}
</style>
