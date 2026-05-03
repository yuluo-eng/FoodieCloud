import { ref } from 'vue'

const toasts = ref([])
let nextId = 0

function show(message, type = 'info', duration = 3000) {
  const id = nextId++
  toasts.value.push({ id, message, type, leaving: false })
  setTimeout(() => dismiss(id), duration)
}

function dismiss(id) {
  const t = toasts.value.find((t) => t.id === id)
  if (t) t.leaving = true
  setTimeout(() => {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }, 300)
}

export function useToast() {
  return {
    toasts,
    success: (msg, ms) => show(msg, 'success', ms),
    error: (msg, ms) => show(msg, 'error', ms ?? 4000),
    info: (msg, ms) => show(msg, 'info', ms),
    warn: (msg, ms) => show(msg, 'warn', ms),
    dismiss,
  }
}
