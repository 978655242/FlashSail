/**
 * i18n Store
 * 
 * Manages internationalization state including locale and translation messages.
 * Persists language preference in localStorage.
 * 
 * Requirements: 13.1, 13.2, 13.4
 * - 13.1: Support Chinese (zh-CN) as the default language
 * - 13.2: Support English (en) as an alternative language
 * - 13.4: Persist language preference in localStorage
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/** Supported locale types */
export type Locale = 'zh' | 'en'

/** LocalStorage key for language preference */
export const LOCALE_STORAGE_KEY = 'flashsell-locale'

/** Default locale when no preference is stored */
export const DEFAULT_LOCALE: Locale = 'zh'

/** Translation message value - can be string or nested object */
export type MessageValue = string | { [key: string]: MessageValue }

/** Translation messages type - nested key-value structure supporting arbitrary depth */
export type Messages = Record<string, Record<string, MessageValue>>

/** i18n state interface */
export interface I18nState {
  locale: Locale
  messages: Record<Locale, Messages>
}

/**
 * Get stored locale preference from localStorage.
 * Returns null if no preference is stored or if localStorage is unavailable.
 */
function getStoredLocale(): Locale | null {
  if (typeof localStorage === 'undefined') return null
  
  try {
    const stored = localStorage.getItem(LOCALE_STORAGE_KEY)
    if (stored === 'zh' || stored === 'en') {
      return stored
    }
    return null
  } catch {
    // localStorage may be unavailable (e.g., private browsing)
    return null
  }
}

/**
 * Store locale preference in localStorage.
 */
function storeLocale(locale: Locale): void {
  if (typeof localStorage === 'undefined') return
  
  try {
    localStorage.setItem(LOCALE_STORAGE_KEY, locale)
  } catch {
    // localStorage may be unavailable (e.g., private browsing, quota exceeded)
    console.warn('Failed to store locale preference in localStorage')
  }
}

/**
 * Validate if a string is a valid locale.
 */
export function isValidLocale(value: string): value is Locale {
  return value === 'zh' || value === 'en'
}

export const useI18nStore = defineStore('i18n', () => {
  // Initialize locale from localStorage or use default
  const storedLocale = getStoredLocale()
  const locale = ref<Locale>(storedLocale ?? DEFAULT_LOCALE)
  
  // Translation messages storage
  const messages = ref<Record<Locale, Messages>>({
    zh: {},
    en: {}
  })

  // Computed properties
  const currentLocale = computed(() => locale.value)
  const currentMessages = computed(() => messages.value[locale.value])
  const isZh = computed(() => locale.value === 'zh')
  const isEn = computed(() => locale.value === 'en')

  /**
   * Set the current locale and persist to localStorage.
   * @param newLocale - The locale to set ('zh' or 'en')
   */
  function setLocale(newLocale: Locale): void {
    if (!isValidLocale(newLocale)) {
      console.warn(`Invalid locale: ${newLocale}. Using default: ${DEFAULT_LOCALE}`)
      locale.value = DEFAULT_LOCALE
      storeLocale(DEFAULT_LOCALE)
      return
    }
    locale.value = newLocale
    storeLocale(newLocale)
  }

  /**
   * Toggle between Chinese and English locales.
   */
  function toggleLocale(): void {
    const newLocale: Locale = locale.value === 'zh' ? 'en' : 'zh'
    setLocale(newLocale)
  }

  /**
   * Set translation messages for a specific locale.
   * @param targetLocale - The locale to set messages for
   * @param newMessages - The translation messages
   */
  function setMessages(targetLocale: Locale, newMessages: Messages): void {
    if (!isValidLocale(targetLocale)) {
      console.warn(`Invalid locale for messages: ${targetLocale}`)
      return
    }
    messages.value[targetLocale] = newMessages
  }

  /**
   * Merge translation messages for a specific locale.
   * Useful for lazy-loading translations.
   * @param targetLocale - The locale to merge messages for
   * @param newMessages - The translation messages to merge
   */
  function mergeMessages(targetLocale: Locale, newMessages: Messages): void {
    if (!isValidLocale(targetLocale)) {
      console.warn(`Invalid locale for messages: ${targetLocale}`)
      return
    }
    messages.value[targetLocale] = {
      ...messages.value[targetLocale],
      ...newMessages
    }
  }

  /**
   * Get a translation by key path.
   * Supports nested keys using dot notation (e.g., 'nav.dashboard').
   * @param key - The translation key (supports dot notation for nested keys)
   * @param fallback - Optional fallback value if key is not found
   * @returns The translated string or the key/fallback if not found
   */
  function t(key: string, fallback?: string): string {
    const currentMsgs = messages.value[locale.value]
    
    // Handle nested keys (e.g., 'nav.dashboard')
    const keys = key.split('.')
    let result: unknown = currentMsgs
    
    for (const k of keys) {
      if (result && typeof result === 'object' && k in result) {
        result = (result as Record<string, unknown>)[k]
      } else {
        // Key not found, return fallback or key
        return fallback ?? key
      }
    }
    
    // Return the result if it's a string, otherwise return fallback or key
    if (typeof result === 'string') {
      return result
    }
    
    return fallback ?? key
  }

  /**
   * Initialize the store from localStorage.
   * Called automatically on store creation, but can be called manually if needed.
   */
  function initFromStorage(): void {
    const stored = getStoredLocale()
    if (stored) {
      locale.value = stored
    }
  }

  /**
   * Reset the store to default state.
   * Useful for testing.
   */
  function $reset(): void {
    locale.value = DEFAULT_LOCALE
    messages.value = { zh: {}, en: {} }
    storeLocale(DEFAULT_LOCALE)
  }

  return {
    // State
    locale,
    messages,
    // Computed
    currentLocale,
    currentMessages,
    isZh,
    isEn,
    // Actions
    setLocale,
    toggleLocale,
    setMessages,
    mergeMessages,
    t,
    initFromStorage,
    $reset
  }
})
