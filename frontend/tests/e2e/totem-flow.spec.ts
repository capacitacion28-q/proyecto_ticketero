import { test, expect } from '@playwright/test';

test.describe('TÓTEM Flow', () => {
  test.beforeEach(async ({ page }) => {
    // Mock API responses
    await page.route('**/api/tickets', async route => {
      if (route.request().method() === 'POST') {
        await route.fulfill({
          status: 201,
          contentType: 'application/json',
          body: JSON.stringify({
            ticket: {
              id: 1,
              numero: 'C01',
              codigoReferencia: 'uuid-test-123',
              status: 'EN_ESPERA',
              positionInQueue: 1,
              estimatedWaitMinutes: 5,
              queueType: 'CAJA'
            },
            message: 'Ticket created successfully'
          })
        });
      }
    });
  });

  test('complete ticket creation flow', async ({ page }) => {
    // Navigate to home page
    await page.goto('/');
    
    // Click TÓTEM button
    await page.click('text=TÓTEM - Crear Ticket');
    
    // Verify we're on TÓTEM page
    await expect(page).toHaveURL('/totem');
    await expect(page.locator('h2')).toContainText('Crear Nuevo Ticket');
    
    // Fill form
    await page.fill('input[placeholder="12345678-9"]', '12345678-9');
    await page.fill('input[placeholder="+56912345678"]', '+56912345678');
    
    // Select queue type (CAJA should be selected by default)
    await expect(page.locator('input[value="CAJA"]')).toBeChecked();
    
    // Submit form
    await page.click('button[type="submit"]');
    
    // Wait for success modal
    await expect(page.locator('text=¡Ticket Creado!')).toBeVisible();
    await expect(page.locator('text=Ticket #C01')).toBeVisible();
    await expect(page.locator('text=Posición en cola:')).toBeVisible();
    await expect(page.locator('text=#1')).toBeVisible();
    
    // Close modal
    await page.click('text=Entendido');
    
    // Verify modal is closed and form is reset
    await expect(page.locator('text=¡Ticket Creado!')).not.toBeVisible();
    await expect(page.locator('input[placeholder="12345678-9"]')).toHaveValue('');
  });

  test('form validation works correctly', async ({ page }) => {
    await page.goto('/totem');
    
    // Try to submit empty form
    await page.click('button[type="submit"]');
    
    // Button should be disabled
    await expect(page.locator('button[type="submit"]')).toBeDisabled();
    
    // Fill invalid RUT
    await page.fill('input[placeholder="12345678-9"]', 'invalid-rut');
    await page.blur('input[placeholder="12345678-9"]');
    
    // Should show validation error
    await expect(page.locator('text=Formato de RUT inválido')).toBeVisible();
    
    // Fill invalid phone
    await page.fill('input[placeholder="+56912345678"]', '123');
    await page.blur('input[placeholder="+56912345678"]');
    
    // Should show validation error
    await expect(page.locator('text=Formato de teléfono inválido')).toBeVisible();
    
    // Button should still be disabled
    await expect(page.locator('button[type="submit"]')).toBeDisabled();
  });

  test('handles API errors gracefully', async ({ page }) => {
    // Mock API error
    await page.route('**/api/tickets', async route => {
      if (route.request().method() === 'POST') {
        await route.fulfill({
          status: 409,
          contentType: 'application/json',
          body: JSON.stringify({
            message: 'Ya tienes un ticket activo'
          })
        });
      }
    });

    await page.goto('/totem');
    
    // Fill valid form
    await page.fill('input[placeholder="12345678-9"]', '12345678-9');
    await page.fill('input[placeholder="+56912345678"]', '+56912345678');
    
    // Submit form
    await page.click('button[type="submit"]');
    
    // Should show error message
    await expect(page.locator('text=Error al crear ticket')).toBeVisible();
  });
});