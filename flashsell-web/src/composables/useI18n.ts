/**
 * useI18n Composable
 * 
 * Provides internationalization functionality with a t() function for translations.
 * Wraps the i18n store to provide a convenient API for components.
 * 
 * Requirements: 13.1, 13.2, 13.4
 * - 13.1: Support Chinese (zh-CN) as the default language
 * - 13.2: Support English (en) as an alternative language
 * - 13.4: Persist language preference in localStorage
 */

import { type ComputedRef, type Ref } from 'vue'
import { useI18nStore, type Locale, type Messages, LOCALE_STORAGE_KEY, DEFAULT_LOCALE } from '@/stores/i18n'
import { storeToRefs } from 'pinia'

/** Return type for useI18n composable */
export interface UseI18nReturn {
  /** Current locale (reactive) */
  locale: Ref<Locale>
  /** Computed boolean indicating if current locale is Chinese */
  isZh: ComputedRef<boolean>
  /** Computed boolean indicating if current locale is English */
  isEn: ComputedRef<boolean>
  /** 
   * Translate a key to the current locale.
   * Supports nested keys using dot notation (e.g., 'nav.dashboard').
   * @param key - The translation key
   * @param fallback - Optional fallback value if key is not found
   * @returns The translated string or the key/fallback if not found
   */
  t: (key: string, fallback?: string) => string
  /** Set the current locale */
  setLocale: (locale: Locale) => void
  /** Toggle between Chinese and English */
  toggleLocale: () => void
  /** Set translation messages for a locale */
  setMessages: (locale: Locale, messages: Messages) => void
  /** Merge translation messages for a locale */
  mergeMessages: (locale: Locale, messages: Messages) => void
}

/**
 * Internationalization composable.
 * 
 * Provides reactive locale state and translation functions.
 * Uses the i18n store internally for state management.
 * 
 * @example
 * ```typescript
 * const { t, locale, toggleLocale, setLocale } = useI18n()
 * 
 * // Translate a key
 * const title = t('nav.dashboard')
 * 
 * // Translate with fallback
 * const label = t('some.missing.key', 'Default Label')
 * 
 * // Toggle language
 * toggleLocale()
 * 
 * // Set specific locale
 * setLocale('en')
 * 
 * // Check current locale
 * if (locale.value === 'zh') {
 *   console.log('Chinese is active')
 * }
 * ```
 */
export function useI18n(): UseI18nReturn {
  const store = useI18nStore()
  const { locale, isZh, isEn } = storeToRefs(store)

  /**
   * Translate a key to the current locale.
   * This is a wrapper around the store's t() function that provides
   * a more convenient API for components.
   * 
   * @param key - The translation key (supports dot notation for nested keys)
   * @param fallback - Optional fallback value if key is not found
   * @returns The translated string or the key/fallback if not found
   */
  function t(key: string, fallback?: string): string {
    return store.t(key, fallback)
  }

  /**
   * Set the current locale.
   * @param newLocale - The locale to set ('zh' or 'en')
   */
  function setLocale(newLocale: Locale): void {
    store.setLocale(newLocale)
  }

  /**
   * Toggle between Chinese and English locales.
   */
  function toggleLocale(): void {
    store.toggleLocale()
  }

  /**
   * Set translation messages for a specific locale.
   * @param targetLocale - The locale to set messages for
   * @param messages - The translation messages
   */
  function setMessages(targetLocale: Locale, messages: Messages): void {
    store.setMessages(targetLocale, messages)
  }

  /**
   * Merge translation messages for a specific locale.
   * Useful for lazy-loading translations.
   * @param targetLocale - The locale to merge messages for
   * @param messages - The translation messages to merge
   */
  function mergeMessages(targetLocale: Locale, messages: Messages): void {
    store.mergeMessages(targetLocale, messages)
  }

  return {
    locale,
    isZh,
    isEn,
    t,
    setLocale,
    toggleLocale,
    setMessages,
    mergeMessages
  }
}

/**
 * Export constants and types for external use.
 */
export { LOCALE_STORAGE_KEY, DEFAULT_LOCALE }
export type { Locale, Messages }

/**
 * Reset i18n state (useful for testing).
 * This clears the store state and resets to defaults.
 */
export function resetI18nState(): void {
  const store = useI18nStore()
  store.$reset()
}
