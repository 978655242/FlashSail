/**
 * Locales Index
 * 
 * Exports all translation files and provides utility functions
 * for initializing the i18n system.
 * 
 * Requirements: 13.1, 13.2
 * - 13.1: Support Chinese (zh-CN) as the default language
 * - 13.2: Support English (en) as an alternative language
 */

import { zhMessages } from './zh'
import { enMessages } from './en'
import type { Messages, Locale } from '@/stores/i18n'

/**
 * All available translation messages indexed by locale.
 */
export const messages: Record<Locale, Messages> = {
  zh: zhMessages,
  en: enMessages
}

/**
 * Get translation messages for a specific locale.
 * @param locale - The locale to get messages for
 * @returns The translation messages for the specified locale
 */
export function getMessages(locale: Locale): Messages {
  return messages[locale] ?? messages.zh
}

/**
 * Initialize the i18n store with all translation messages.
 * Call this function during app initialization.
 * 
 * @example
 * ```typescript
 * import { initI18n } from '@/locales'
 * import { useI18nStore } from '@/stores/i18n'
 * 
 * const i18nStore = useI18nStore()
 * initI18n(i18nStore)
 * ```
 */
export function initI18n(store: {
  setMessages: (locale: Locale, messages: Messages) => void
}): void {
  store.setMessages('zh', zhMessages)
  store.setMessages('en', enMessages)
}

// Re-export individual message objects for direct access
export { zhMessages, enMessages }

// Re-export types
export type { Messages, Locale }
