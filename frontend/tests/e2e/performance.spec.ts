import { test, expect } from '@playwright/test';

test.describe('Performance Tests', () => {
  test('home page loads within performance budget', async ({ page }) => {
    const startTime = Date.now();
    
    await page.goto('/');
    
    // Wait for page to be fully loaded
    await page.waitForLoadState('networkidle');
    
    const loadTime = Date.now() - startTime;
    
    // Should load within 2 seconds
    expect(loadTime).toBeLessThan(2000);
    
    // Check that main elements are visible
    await expect(page.locator('h1')).toBeVisible();
    await expect(page.locator('text=TÓTEM - Crear Ticket')).toBeVisible();
    await expect(page.locator('text=ADMIN - Dashboard')).toBeVisible();
  });

  test('TÓTEM page renders quickly', async ({ page }) => {
    await page.goto('/totem');
    
    const startTime = Date.now();
    
    // Wait for form to be interactive
    await page.waitForSelector('input[placeholder="12345678-9"]');
    await page.waitForSelector('button[type="submit"]');
    
    const renderTime = Date.now() - startTime;
    
    // Form should be interactive within 1 second
    expect(renderTime).toBeLessThan(1000);
  });

  test('ADMIN dashboard loads data efficiently', async ({ page }) => {
    // Mock API responses
    await page.route('**/api/admin/dashboard', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          totalTickets: 10,
          activeTickets: 3,
          completedTickets: 7,
          averageWaitTime: 12
        })
      });
    });

    await page.route('**/api/tickets/active', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([])
      });
    });

    const startTime = Date.now();
    
    await page.goto('/admin');
    
    // Wait for dashboard to load
    await expect(page.locator('text=Total Tickets')).toBeVisible();
    await expect(page.locator('text=10').first()).toBeVisible();
    
    const loadTime = Date.now() - startTime;
    
    // Dashboard should load within 1.5 seconds
    expect(loadTime).toBeLessThan(1500);
  });

  test('bundle size is reasonable', async ({ page }) => {
    // Navigate to page and check network requests
    const responses = [];
    
    page.on('response', response => {
      if (response.url().includes('.js') || response.url().includes('.css')) {
        responses.push({
          url: response.url(),
          size: response.headers()['content-length']
        });
      }
    });
    
    await page.goto('/');
    await page.waitForLoadState('networkidle');
    
    // Check that we don't have excessively large bundles
    const jsFiles = responses.filter(r => r.url.includes('.js'));
    const totalJSSize = jsFiles.reduce((sum, file) => sum + (parseInt(file.size) || 0), 0);
    
    // Total JS should be less than 500KB (reasonable for SvelteKit app)
    expect(totalJSSize).toBeLessThan(500 * 1024);
  });
});