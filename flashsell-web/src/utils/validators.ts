/**
 * validators.ts - Input Validation Utilities
 * 
 * Provides reusable validation functions for form inputs.
 * 
 * **Validates: Requirements 4.4**
 * - 4.4: Validate phone number format (Chinese mobile: 1[3-9]XXXXXXXXX)
 */

/**
 * Validation error interface
 * Contains the field name and error message for display
 */
export interface ValidationError {
  field: string
  message: string
}

/**
 * Chinese mobile phone number pattern
 * Format: 1[3-9]XXXXXXXXX (11 digits starting with 1, second digit 3-9)
 * 
 * Valid examples: 13812345678, 15912345678, 18812345678
 * Invalid examples: 12345678901 (starts with 12), 1381234567 (too short)
 */
const CHINESE_MOBILE_PATTERN = /^1[3-9]\d{9}$/

/**
 * Validates a Chinese mobile phone number
 * 
 * @param phone - The phone number string to validate
 * @returns ValidationError if invalid, null if valid
 * 
 * **Validates: Requirements 4.4**
 * 
 * @example
 * ```ts
 * validatePhone('13812345678') // returns null (valid)
 * validatePhone('12345678901') // returns { field: 'phone', message: '...' } (invalid)
 * validatePhone('') // returns { field: 'phone', message: '...' } (empty)
 * ```
 */
export function validatePhone(phone: string): ValidationError | null {
  // Check for empty input
  if (!phone || phone.trim() === '') {
    return {
      field: 'phone',
      message: '请输入手机号'
    }
  }

  // Validate against Chinese mobile pattern
  if (!CHINESE_MOBILE_PATTERN.test(phone)) {
    return {
      field: 'phone',
      message: '请输入正确的手机号'
    }
  }

  // Valid phone number
  return null
}

/**
 * Validates a verification code
 * 
 * @param code - The verification code string to validate
 * @returns ValidationError if invalid, null if valid
 * 
 * @example
 * ```ts
 * validateVerifyCode('123456') // returns null (valid)
 * validateVerifyCode('12345') // returns { field: 'code', message: '...' } (too short)
 * validateVerifyCode('') // returns { field: 'code', message: '...' } (empty)
 * ```
 */
export function validateVerifyCode(code: string): ValidationError | null {
  // Check for empty input
  if (!code || code.trim() === '') {
    return {
      field: 'code',
      message: '请输入验证码'
    }
  }

  // Validate 6-digit code
  if (!/^\d{6}$/.test(code)) {
    return {
      field: 'code',
      message: '请输入6位验证码'
    }
  }

  // Valid verification code
  return null
}

/**
 * Checks if a phone number is valid (convenience function)
 * 
 * @param phone - The phone number string to check
 * @returns true if valid, false otherwise
 */
export function isValidPhone(phone: string): boolean {
  return validatePhone(phone) === null
}

/**
 * Checks if a verification code is valid (convenience function)
 * 
 * @param code - The verification code string to check
 * @returns true if valid, false otherwise
 */
export function isValidVerifyCode(code: string): boolean {
  return validateVerifyCode(code) === null
}
