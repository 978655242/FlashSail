/**
 * useTheme Composable
 * 
 * Provides theme state management with dark/light mode support.
 * Persists theme preference in localStorage and applies theme by
 * adding/removing 'light' class on the html element.
 * 
 * Requirements: 1.3, 1.4, 1.5
 * - 1.3: Toggle between dark and light modes
 * - 1.4: Persist theme preference in localStorage
 * - 1.5: Restore previously selected theme on load
 */

import { ref, computed, watch, type Ref, type ComputedRef } from 'vue'

/** Theme mode type */
export type ThemeMode = 'dark' | 'light'

/** LocalStorage key for theme preference */
const THEME_STORAGE_KEY = 'flashsell-theme'

/** Default theme when no preference is stored */
const DEFAULT_THEME: ThemeMode = 'dark'

/** Return type for useTheme composable */
export interface UseThemeReturn {
  /** Current theme mode (reactive) */
  theme: Ref<ThemeMode>
  /** Computed boolean indicating if current theme is dark */
  isDark: ComputedRef<boolean>
  /** Toggle between dark and light themes */
  toggleTheme: () => void
  /** Set theme to a specific mode */
  setTheme: (mode: ThemeMode) => void
}

// Singleton state to ensure consistent theme across all component instances
let themeState: Ref<ThemeMode> | null = null
let isInitialized = false

/**
 * Apply theme to the document by adding/removing 'light' class on html element.
 * Dark mode is the default (no class), light mode adds 'light' class.
 */
function applyThemeToDocument(mode: ThemeMode): void {
  if (typeof document === 'undefined') return
  
  const htmlElement = document.documentElement
  if (mode === 'light') {
    htmlElement.classList.add('light')
  } else {
    htmlElement.classList.remove('light')
  }
}

/**
 * Get stored theme preference from localStorage.
 * Returns null if no preference is stored or if localStorage is unavailable.
 */
function getStoredTheme(): ThemeMode | null {
  if (typeof localStorage === 'undefined') return null
  
  try {
    const stored = localStorage.getItem(THEME_STORAGE_KEY)
    if (stored === 'dark' || stored === 'light') {
      return stored
    }
    return null
  } catch {
    // localStorage may be unavailable (e.g., private browsing)
    return null
  }
}

/**
 * Store theme preference in localStorage.
 */
function storeTheme(mode: ThemeMode): void {
  if (typeof localStorage === 'undefined') return
  
  try {
    localStorage.setItem(THEME_STORAGE_KEY, mode)
  } catch {
    // localStorage may be unavailable (e.g., private browsing, quota exceeded)
    console.warn('Failed to store theme preference in localStorage')
  }
}

/**
 * Initialize theme state from localStorage or use default.
 * This is called once when the composable is first used.
 */
function initializeTheme(): ThemeMode {
  const storedTheme = getStoredTheme()
  const initialTheme = storedTheme ?? DEFAULT_THEME
  
  // Apply theme to document immediately
  applyThemeToDocument(initialTheme)
  
  return initialTheme
}

/**
 * Theme management composable.
 * 
 * Provides reactive theme state with persistence and document class management.
 * Uses a singleton pattern to ensure consistent theme state across all components.
 * 
 * @example
 * ```typescript
 * const { theme, isDark, toggleTheme, setTheme } = useTheme()
 * 
 * // Toggle theme
 * toggleTheme()
 * 
 * // Set specific theme
 * setTheme('light')
 * 
 * // Check current theme
 * if (isDark.value) {
 *   console.log('Dark mode is active')
 * }
 * ```
 */
export function useTheme(): UseThemeReturn {
  // Initialize singleton state on first use
  if (!themeState) {
    themeState = ref<ThemeMode>(DEFAULT_THEME)
  }
  
  // Initialize from localStorage only once
  if (!isInitialized) {
    themeState.value = initializeTheme()
    isInitialized = true
    
    // Watch for theme changes and apply to document + persist
    watch(themeState, (newTheme) => {
      applyThemeToDocument(newTheme)
      storeTheme(newTheme)
    })
  }
  
  // Computed property for checking if dark mode is active
  const isDark = computed(() => themeState!.value === 'dark')
  
  /**
   * Toggle between dark and light themes.
   */
  function toggleTheme(): void {
    themeState!.value = themeState!.value === 'dark' ? 'light' : 'dark'
  }
  
  /**
   * Set theme to a specific mode.
   * @param mode - The theme mode to set ('dark' or 'light')
   */
  function setTheme(mode: ThemeMode): void {
    if (mode !== 'dark' && mode !== 'light') {
      console.warn(`Invalid theme mode: ${mode}. Using default: ${DEFAULT_THEME}`)
      themeState!.value = DEFAULT_THEME
      return
    }
    themeState!.value = mode
  }
  
  return {
    theme: themeState,
    isDark,
    toggleTheme,
    setTheme
  }
}

/**
 * Reset theme state (useful for testing).
 * This clears the singleton state and allows re-initialization.
 */
export function resetThemeState(): void {
  themeState = null
  isInitialized = false
}

/**
 * Export storage key for testing purposes.
 */
export const STORAGE_KEY = THEME_STORAGE_KEY
