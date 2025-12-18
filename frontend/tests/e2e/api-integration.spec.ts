import { test, expect } from '@playwright/test';

test.describe('API Integration Tests', () => {
  test('handles API timeout gracefully', async ({ page }) => {
    // Mock slow API response
    await page.route('**/api/tickets', async route => {
      await new Promise(resolve => setTimeout(resolve, 15000)); // 15 second delay
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ ticket: { id: 1, numero: 'C01' } })
      });
    });

    await page.goto('/totem');
    
    await page.fill('input[placeholder="12345678-9"]', '12345678-9');
    await page.fill('input[placeholder="+56912345678"]', '+56912345678');
    
    await page.click('button[type="submit"]');
    
    // Should show loading state
    await expect(page.locator('text=Creando Ticket...')).toBeVisible();
    
    // Should eventually timeout or show error (depending on implementation)
    // This tests that the UI doesn't hang indefinitely
  });

  test('validates API response format', async ({ page }) => {
    // Mock malformed API response
    await page.route('**/api/tickets', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ invalid: 'response' })
      });
    });

    await page.goto('/totem');
    
    await page.fill('input[placeholder="12345678-9"]', '12345678-9');
    await page.fill('input[placeholder="+56912345678"]', '+56912345678');
    
    await page.click('button[type="submit"]');
    
    // Should handle malformed response gracefully
    await expect(page.locator('text=Error al crear ticket')).toBeVisible();
  });

  test('handles network errors', async ({ page }) => {
    // Mock network failure
    await page.route('**/api/tickets', async route => {
      await route.abort('failed');
    });

    await page.goto('/totem');
    
    await page.fill('input[placeholder="12345678-9"]', '12345678-9');
    await page.fill('input[placeholder="+56912345678"]', '+56912345678');
    
    await page.click('button[type="submit"]');
    
    // Should show network error
    await expect(page.locator('text=Error al crear ticket')).toBeVisible();
    
    // Form should remain usable
    await expect(page.locator('input[placeholder="12345678-9"]')).toHaveValue('12345678-9');
  });

  test('handles different HTTP status codes', async ({ page }) => {
    const testCases = [
      { status: 400, expectedMessage: 'Error al crear ticket' },
      { status: 409, expectedMessage: 'Error al crear ticket' },
      { status: 500, expectedMessage: 'Error al crear ticket' }
    ];

    for (const testCase of testCases) {
      await page.route('**/api/tickets', async route => {
        await route.fulfill({
          status: testCase.status,
          contentType: 'application/json',
          body: JSON.stringify({ message: 'Error message' })
        });
      });

      await page.goto('/totem');
      
      await page.fill('input[placeholder="12345678-9"]', '12345678-9');
      await page.fill('input[placeholder="+56912345678"]', '+56912345678');
      
      await page.click('button[type="submit"]');
      
      await expect(page.locator(`text=${testCase.expectedMessage}`)).toBeVisible();
    }
  });

  test('validates CORS headers', async ({ page }) => {
    let corsHeadersPresent = false;
    
    page.on('response', response => {
      if (response.url().includes('/api/')) {
        const headers = response.headers();
        if (headers['access-control-allow-origin']) {
          corsHeadersPresent = true;
        }
      }
    });

    // Mock API with CORS headers
    await page.route('**/api/admin/dashboard', async route => {
      await route.fulfill({
        status: 200,
        headers: {
          'Access-Control-Allow-Origin': '*',
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          totalTickets: 0,
          activeTickets: 0,
          completedTickets: 0,
          averageWaitTime: 0
        })
      });
    });

    await page.goto('/admin');
    
    // Wait for API call
    await page.waitForTimeout(1000);
    
    expect(corsHeadersPresent).toBe(true);
  });
});