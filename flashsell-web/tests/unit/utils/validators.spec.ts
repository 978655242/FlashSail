/**
 * Unit Tests for Validators Utility
 * 
 * Tests the phone number and verification code validation functions.
 * 
 * **Validates: Requirements 4.4**
 * - 4.4: Validate phone number format (Chinese mobile: 1[3-9]XXXXXXXXX)
 */

import { describe, it, expect } from 'vitest'
import {
  validatePhone,
  validateVerifyCode,
  isValidPhone,
  isValidVerifyCode,
  type ValidationError
} from '@/utils/validators'

describe('validatePhone', () => {
  describe('valid Chinese mobile numbers', () => {
    it('should accept valid numbers starting with 13', () => {
      expect(validatePhone('13812345678')).toBeNull()
      expect(validatePhone('13012345678')).toBeNull()
      expect(validatePhone('13912345678')).toBeNull()
    })

    it('should accept valid numbers starting with 14', () => {
      expect(validatePhone('14512345678')).toBeNull()
      expect(validatePhone('14712345678')).toBeNull()
    })

    it('should accept valid numbers starting with 15', () => {
      expect(validatePhone('15012345678')).toBeNull()
      expect(validatePhone('15912345678')).toBeNull()
    })

    it('should accept valid numbers starting with 16', () => {
      expect(validatePhone('16612345678')).toBeNull()
    })

    it('should accept valid numbers starting with 17', () => {
      expect(validatePhone('17012345678')).toBeNull()
      expect(validatePhone('17812345678')).toBeNull()
    })

    it('should accept valid numbers starting with 18', () => {
      expect(validatePhone('18012345678')).toBeNull()
      expect(validatePhone('18812345678')).toBeNull()
    })

    it('should accept valid numbers starting with 19', () => {
      expect(validatePhone('19812345678')).toBeNull()
      expect(validatePhone('19912345678')).toBeNull()
    })
  })

  describe('invalid phone numbers', () => {
    it('should reject empty input', () => {
      const result = validatePhone('')
      expect(result).not.toBeNull()
      expect(result?.field).toBe('phone')
      expect(result?.message).toBe('请输入手机号')
    })

    it('should reject whitespace-only input', () => {
      const result = validatePhone('   ')
      expect(result).not.toBeNull()
      expect(result?.field).toBe('phone')
      expect(result?.message).toBe('请输入手机号')
    })

    it('should reject numbers starting with 10', () => {
      const result = validatePhone('10812345678')
      expect(result).not.toBeNull()
      expect(result?.message).toBe('请输入正确的手机号')
    })

    it('should reject numbers starting with 11', () => {
      const result = validatePhone('11812345678')
      expect(result).not.toBeNull()
      expect(result?.message).toBe('请输入正确的手机号')
    })

    it('should reject numbers starting with 12', () => {
      const result = validatePhone('12812345678')
      expect(result).not.toBeNull()
      expect(result?.message).toBe('请输入正确的手机号')
    })

    it('should reject numbers that are too short', () => {
      const result = validatePhone('1381234567')
      expect(result).not.toBeNull()
      expect(result?.message).toBe('请输入正确的手机号')
    })

    it('should reject numbers that are too long', () => {
      const result = validatePhone('138123456789')
      expect(result).not.toBeNull()
      expect(result?.message).toBe('请输入正确的手机号')
    })

    it('should reject numbers with non-digit characters', () => {
      expect(validatePhone('1381234567a')).not.toBeNull()
      expect(validatePhone('138-1234-5678')).not.toBeNull()
      expect(validatePhone('138 1234 5678')).not.toBeNull()
      expect(validatePhone('+8613812345678')).not.toBeNull()
    })

    it('should reject numbers not starting with 1', () => {
      expect(validatePhone('23812345678')).not.toBeNull()
      expect(validatePhone('03812345678')).not.toBeNull()
    })
  })
})

describe('validateVerifyCode', () => {
  describe('valid verification codes', () => {
    it('should accept 6-digit codes', () => {
      expect(validateVerifyCode('123456')).toBeNull()
      expect(validateVerifyCode('000000')).toBeNull()
      expect(validateVerifyCode('999999')).toBeNull()
    })
  })

  describe('invalid verification codes', () => {
    it('should reject empty input', () => {
      const result = validateVerifyCode('')
      expect(result).not.toBeNull()
      expect(result?.field).toBe('code')
      expect(result?.message).toBe('请输入验证码')
    })

    it('should reject whitespace-only input', () => {
      const result = validateVerifyCode('   ')
      expect(result).not.toBeNull()
      expect(result?.field).toBe('code')
      expect(result?.message).toBe('请输入验证码')
    })

    it('should reject codes that are too short', () => {
      const result = validateVerifyCode('12345')
      expect(result).not.toBeNull()
      expect(result?.message).toBe('请输入6位验证码')
    })

    it('should reject codes that are too long', () => {
      const result = validateVerifyCode('1234567')
      expect(result).not.toBeNull()
      expect(result?.message).toBe('请输入6位验证码')
    })

    it('should reject codes with non-digit characters', () => {
      expect(validateVerifyCode('12345a')).not.toBeNull()
      expect(validateVerifyCode('abcdef')).not.toBeNull()
      expect(validateVerifyCode('123 456')).not.toBeNull()
    })
  })
})

describe('isValidPhone', () => {
  it('should return true for valid phone numbers', () => {
    expect(isValidPhone('13812345678')).toBe(true)
    expect(isValidPhone('15912345678')).toBe(true)
    expect(isValidPhone('18812345678')).toBe(true)
  })

  it('should return false for invalid phone numbers', () => {
    expect(isValidPhone('')).toBe(false)
    expect(isValidPhone('12345678901')).toBe(false)
    expect(isValidPhone('1381234567')).toBe(false)
  })
})

describe('isValidVerifyCode', () => {
  it('should return true for valid codes', () => {
    expect(isValidVerifyCode('123456')).toBe(true)
    expect(isValidVerifyCode('000000')).toBe(true)
  })

  it('should return false for invalid codes', () => {
    expect(isValidVerifyCode('')).toBe(false)
    expect(isValidVerifyCode('12345')).toBe(false)
    expect(isValidVerifyCode('1234567')).toBe(false)
  })
})
