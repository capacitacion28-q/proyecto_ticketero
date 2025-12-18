import { test, expect } from '@playwright/test';

test.describe('ADMIN Dashboard Flow', () => {
  test.beforeEach(async ({ page }) => {
    // Mock dashboard API responses
    await page.route('**/api/admin/dashboard', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          totalTickets: 5,
          activeTickets: 2,
          completedTickets: 3,
          averageWaitTime: 15,
          queueStats: [
            {
              queueType: 'CAJA',
              totalTickets: 3,
              activeTickets: 1,
              averageWaitTime: 10
            }
          ],
          advisorStats: [
            {
              advisor: {
                id: 1,
                name: 'Juan Pérez',
                status: 'AVAILABLE',
                moduleNumber: 1,
                assignedTicketsCount: 0
              },
              currentTickets: 0,
              completedToday: 2
            }
          ]
        })
      });
    });

    await page.route('**/api/tickets/active', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            id: 1,
            numero: 'C01',
            nationalId: '12345678-9',
            status: 'EN_ESPERA',
            positionInQueue: 1,
            createdAt: new Date().toISOString(),
            queueType: 'CAJA'
          },
          {
            id: 2,
            numero: 'C02',
            nationalId: '87654321-0',
            status: 'ATENDIENDO',
            positionInQueue: 1,
            createdAt: new Date().toISOString(),
            queueType: 'CAJA'
          }
        ])
      });
    });

    await page.route('**/api/admin/advisors/*/status', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ success: true })
      });
    });
  });

  test('displays dashboard metrics correctly', async ({ page }) => {
    await page.goto('/admin');
    
    // Wait for data to load
    await expect(page.locator('text=Cargando dashboard...')).not.toBeVisible();
    
    // Check metrics cards
    await expect(page.locator('text=Total Tickets')).toBeVisible();
    await expect(page.locator('text=5').first()).toBeVisible();
    
    await expect(page.locator('text=Tickets Activos')).toBeVisible();
    await expect(page.locator('text=2').first()).toBeVisible();
    
    await expect(page.locator('text=Completados')).toBeVisible();
    await expect(page.locator('text=3').first()).toBeVisible();
    
    await expect(page.locator('text=Tiempo Promedio')).toBeVisible();
    await expect(page.locator('text=15 min')).toBeVisible();
  });

  test('displays advisors panel correctly', async ({ page }) => {
    await page.goto('/admin');
    
    // Wait for data to load
    await expect(page.locator('text=Cargando dashboard...')).not.toBeVisible();
    
    // Check advisors section
    await expect(page.locator('text=Asesores')).toBeVisible();
    await expect(page.locator('text=Juan Pérez')).toBeVisible();
    await expect(page.locator('text=Módulo 1')).toBeVisible();
    await expect(page.locator('text=Tickets asignados: 0')).toBeVisible();
    
    // Check advisor status buttons
    await expect(page.locator('button:has-text("Disponible")')).toBeVisible();
    await expect(page.locator('button:has-text("Offline")')).toBeVisible();
  });

  test('displays active tickets correctly', async ({ page }) => {
    await page.goto('/admin');
    
    // Wait for data to load
    await expect(page.locator('text=Cargando dashboard...')).not.toBeVisible();
    
    // Check active tickets section
    await expect(page.locator('text=Tickets Activos')).toBeVisible();
    await expect(page.locator('text=C01')).toBeVisible();
    await expect(page.locator('text=12345678-9')).toBeVisible();
    await expect(page.locator('text=Posición: #1')).toBeVisible();
    
    await expect(page.locator('text=C02')).toBeVisible();
    await expect(page.locator('text=87654321-0')).toBeVisible();
    
    // Check status badges
    await expect(page.locator('text=EN_ESPERA')).toBeVisible();
    await expect(page.locator('text=ATENDIENDO')).toBeVisible();
  });

  test('can update advisor status', async ({ page }) => {
    await page.goto('/admin');
    
    // Wait for data to load
    await expect(page.locator('text=Cargando dashboard...')).not.toBeVisible();
    
    // Click offline button for advisor
    await page.click('button:has-text("Offline")');
    
    // Should make API call (mocked to return success)
    // In a real test, we would verify the UI updates accordingly
    await page.waitForTimeout(100); // Small delay for API call
  });

  test('handles loading states', async ({ page }) => {
    // Delay the API response to test loading state
    await page.route('**/api/admin/dashboard', async route => {
      await new Promise(resolve => setTimeout(resolve, 1000));
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          totalTickets: 0,
          activeTickets: 0,
          completedTickets: 0,
          averageWaitTime: 0,
          queueStats: [],
          advisorStats: []
        })
      });
    });

    await page.goto('/admin');
    
    // Should show loading state
    await expect(page.locator('text=Cargando dashboard...')).toBeVisible();
    
    // Wait for loading to complete
    await expect(page.locator('text=Cargando dashboard...')).not.toBeVisible();
  });

  test('handles API errors', async ({ page }) => {
    // Mock API error
    await page.route('**/api/admin/dashboard', async route => {
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({
          message: 'Internal server error'
        })
      });
    });

    await page.goto('/admin');
    
    // Should show error message
    await expect(page.locator('text=Error cargando dashboard')).toBeVisible();
  });
});