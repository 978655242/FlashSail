import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './style.css'

// Import i18n initialization
import { initI18n } from './locales'
import { useI18nStore } from './stores/i18n'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// Initialize i18n with translation messages
const i18nStore = useI18nStore()
initI18n(i18nStore)

app.mount('#app')
