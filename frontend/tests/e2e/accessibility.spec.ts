import { test, expect } from '@playwright/test';

test.describe('Accessibility Tests', () => {
  test('home page has proper heading structure', async ({ page }) => {
    await page.goto('/');
    
    // Should have main heading
    const h1 = page.locator('h1');
    await expect(h1).toBeVisible();
    await expect(h1).toContainText('Sistema Ticketero');
    
    // Buttons should be accessible
    const totemButton = page.locator('text=TÓTEM - Crear Ticket');
    const adminButton = page.locator('text=ADMIN - Dashboard');
    
    await expect(totemButton).toBeVisible();
    await expect(adminButton).toBeVisible();
  });

  test('TÓTEM form has proper labels and ARIA attributes', async ({ page }) => {
    await page.goto('/totem');
    
    // Form should have proper labels
    const rutInput = page.locator('input[placeholder="12345678-9"]');
    const phoneInput = page.locator('input[placeholder="+56912345678"]');
    
    // Check that inputs are properly labeled
    await expect(page.locator('label:has-text("RUT o Cédula")')).toBeVisible();
    await expect(page.locator('label:has-text("Teléfono")')).toBeVisible();
    
    // Required fields should be marked
    await expect(page.locator('text=*')).toHaveCount(3); // RUT, Phone, Queue type
    
    // Submit button should be accessible
    const submitButton = page.locator('button[type="submit"]');
    await expect(submitButton).toBeVisible();
    await expect(submitButton).toContainText('Crear Ticket');
  });

  test('keyboard navigation works correctly', async ({ page }) => {
    await page.goto('/totem');
    
    // Tab through form elements
    await page.keyboard.press('Tab'); // RUT input
    await expect(page.locator('input[placeholder="12345678-9"]')).toBeFocused();
    
    await page.keyboard.press('Tab'); // Phone input
    await expect(page.locator('input[placeholder="+56912345678"]')).toBeFocused();
    
    await page.keyboard.press('Tab'); // Branch select
    await expect(page.locator('select')).toBeFocused();
    
    // Should be able to navigate with keyboard
    await page.keyboard.press('Tab'); // Queue type radio
    await page.keyboard.press('ArrowDown'); // Navigate queue options
  });

  test('error messages are announced properly', async ({ page }) => {
    await page.goto('/totem');
    
    // Fill invalid RUT
    await page.fill('input[placeholder="12345678-9"]', 'invalid');
    await page.blur('input[placeholder="12345678-9"]');
    
    // Error message should be visible and associated with input
    const errorMessage = page.locator('text=Formato de RUT inválido');
    await expect(errorMessage).toBeVisible();
    
    // Error should be near the input for screen readers
    const rutInput = page.locator('input[placeholder="12345678-9"]');
    await expect(rutInput).toHaveClass(/border-red/);
  });

  test('modal has proper focus management', async ({ page }) => {
    // Mock successful ticket creation
    await page.route('**/api/tickets', async route => {
      await route.fulfill({
        status: 201,
        contentType: 'application/json',
        body: JSON.stringify({
          ticket: {
            id: 1,
            numero: 'C01',
            codigoReferencia: 'uuid-123',
            status: 'EN_ESPERA',
            positionInQueue: 1,
            estimatedWaitMinutes: 5,
            queueType: 'CAJA'
          }
        })
      });
    });

    await page.goto('/totem');
    
    // Fill and submit form
    await page.fill('input[placeholder="12345678-9"]', '12345678-9');
    await page.fill('input[placeholder="+56912345678"]', '+56912345678');
    await page.click('button[type="submit"]');
    
    // Modal should appear and be focusable
    await expect(page.locator('text=¡Ticket Creado!')).toBeVisible();
    
    // Close button should be accessible
    const closeButton = page.locator('text=Entendido');
    await expect(closeButton).toBeVisible();
    await expect(closeButton).toBeFocused();
  });

  test('color contrast is sufficient', async ({ page }) => {
    await page.goto('/');
    
    // Check that text has sufficient contrast (basic check)
    const title = page.locator('h1');
    const titleColor = await title.evaluate(el => 
      window.getComputedStyle(el).color
    );
    
    // Should not be light gray on white (basic contrast check)
    expect(titleColor).not.toBe('rgb(128, 128, 128)');
    
    // Buttons should have proper contrast
    const primaryButton = page.locator('text=TÓTEM - Crear Ticket');
    const buttonBg = await primaryButton.evaluate(el => 
      window.getComputedStyle(el).backgroundColor
    );
    
    // Should have a defined background color
    expect(buttonBg).not.toBe('rgba(0, 0, 0, 0)');
  });
});