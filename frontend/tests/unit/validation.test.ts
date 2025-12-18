import { describe, it, expect } from 'vitest';
import { validateRUT, validatePhone } from '$lib/utils/helpers';

describe('Form Validations', () => {
  describe('validateRUT', () => {
    it('validates correct RUT formats', () => {
      expect(validateRUT('12345678-9')).toBe(true);
      expect(validateRUT('123456789')).toBe(true);
      expect(validateRUT('1234567-8')).toBe(true);
    });

    it('validates foreign ID formats', () => {
      expect(validateRUT('P12345678')).toBe(true);
      expect(validateRUT('A1234567890')).toBe(true);
    });

    it('rejects invalid formats', () => {
      expect(validateRUT('invalid')).toBe(false);
      expect(validateRUT('123')).toBe(false);
      expect(validateRUT('12345678-')).toBe(false);
      expect(validateRUT('')).toBe(false);
    });
  });

  describe('validatePhone', () => {
    it('validates international format', () => {
      expect(validatePhone('+56912345678')).toBe(true);
      expect(validatePhone('+56987654321')).toBe(true);
    });

    it('validates national format', () => {
      expect(validatePhone('912345678')).toBe(true);
      expect(validatePhone('987654321')).toBe(true);
    });

    it('rejects invalid formats', () => {
      expect(validatePhone('invalid')).toBe(false);
      expect(validatePhone('123')).toBe(false);
      expect(validatePhone('+5691234')).toBe(false);
      expect(validatePhone('')).toBe(false);
    });
  });
});