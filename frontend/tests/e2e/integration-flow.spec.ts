import { test, expect } from '@playwright/test';

test.describe('Integration Flow - Complete User Journey', () => {
  test('complete ticket lifecycle from creation to completion', async ({ page }) => {
    let ticketNumber = '';
    let ticketId = 0;

    // Step 1: Create ticket in TÓTEM
    await page.route('**/api/tickets', async route => {
      if (route.request().method() === 'POST') {
        ticketId = Math.floor(Math.random() * 1000);
        ticketNumber = `C${String(ticketId).padStart(2, '0')}`;
        
        await route.fulfill({
          status: 201,
          contentType: 'application/json',
          body: JSON.stringify({
            ticket: {
              id: ticketId,
              numero: ticketNumber,
              codigoReferencia: `uuid-${ticketId}`,
              status: 'EN_ESPERA',
              positionInQueue: 1,
              estimatedWaitMinutes: 5,
              queueType: 'CAJA',
              nationalId: '12345678-9',
              createdAt: new Date().toISOString()
            },
            message: 'Ticket created successfully'
          })
        });
      }
    });

    // Navigate to TÓTEM and create ticket
    await page.goto('/');
    await page.click('text=TÓTEM - Crear Ticket');
    
    await page.fill('input[placeholder="12345678-9"]', '12345678-9');
    await page.fill('input[placeholder="+56912345678"]', '+56912345678');
    await page.click('button[type="submit"]');
    
    // Verify ticket creation
    await expect(page.locator('text=¡Ticket Creado!')).toBeVisible();
    await expect(page.locator(`text=Ticket #${ticketNumber}`)).toBeVisible();
    
    await page.click('text=Entendido');

    // Step 2: Check ticket in ADMIN dashboard
    await page.route('**/api/admin/dashboard', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          totalTickets: 1,
          activeTickets: 1,
          completedTickets: 0,
          averageWaitTime: 5,
          queueStats: [
            {
              queueType: 'CAJA',
              totalTickets: 1,
              activeTickets: 1,
              averageWaitTime: 5
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
              completedToday: 0
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
            id: ticketId,
            numero: ticketNumber,
            nationalId: '12345678-9',
            status: 'EN_ESPERA',
            positionInQueue: 1,
            createdAt: new Date().toISOString(),
            queueType: 'CAJA'
          }
        ])
      });
    });

    // Navigate to ADMIN
    await page.goto('/');
    await page.click('text=ADMIN - Dashboard');
    
    // Verify ticket appears in dashboard
    await expect(page.locator('text=Total Tickets')).toBeVisible();
    await expect(page.locator('text=1').first()).toBeVisible();
    await expect(page.locator('text=Tickets Activos')).toBeVisible();
    await expect(page.locator(`text=${ticketNumber}`)).toBeVisible();
    await expect(page.locator('text=12345678-9')).toBeVisible();

    // Step 3: Simulate ticket status change to ATENDIENDO
    await page.route('**/api/tickets/active', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            id: ticketId,
            numero: ticketNumber,
            nationalId: '12345678-9',
            status: 'ATENDIENDO',
            positionInQueue: 1,
            createdAt: new Date().toISOString(),
            queueType: 'CAJA'
          }
        ])
      });
    });

    // Refresh page to see updated status
    await page.reload();
    await expect(page.locator('text=ATENDIENDO')).toBeVisible();

    // Step 4: Simulate ticket completion
    await page.route('**/api/admin/dashboard', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          totalTickets: 1,
          activeTickets: 0,
          completedTickets: 1,
          averageWaitTime: 0,
          queueStats: [],
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
              completedToday: 1
            }
          ]
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

    // Refresh to see completion
    await page.reload();
    
    // Verify completion metrics
    await expect(page.locator('text=Completados')).toBeVisible();
    await expect(page.locator('text=1').nth(2)).toBeVisible(); // Third "1" should be completed tickets
    await expect(page.locator('text=No hay tickets activos')).toBeVisible();
  });

  test('navigation between TÓTEM and ADMIN works correctly', async ({ page }) => {
    // Start at home
    await page.goto('/');
    await expect(page.locator('h1')).toContainText('Sistema Ticketero');
    
    // Go to TÓTEM
    await page.click('text=TÓTEM - Crear Ticket');
    await expect(page).toHaveURL('/totem');
    await expect(page.locator('h2')).toContainText('Crear Nuevo Ticket');
    
    // Go back to home
    await page.click('text=← Volver');
    await expect(page).toHaveURL('/');
    
    // Go to ADMIN
    await page.click('text=ADMIN - Dashboard');
    await expect(page).toHaveURL('/admin');
    await expect(page.locator('text=ADMIN - Dashboard')).toBeVisible();
    
    // Go back to home
    await page.click('text=← Volver');
    await expect(page).toHaveURL('/');
  });

  test('responsive design works on mobile viewport', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    
    await page.goto('/');
    
    // Check home page is responsive
    await expect(page.locator('h1')).toBeVisible();
    await expect(page.locator('text=TÓTEM - Crear Ticket')).toBeVisible();
    await expect(page.locator('text=ADMIN - Dashboard')).toBeVisible();
    
    // Check TÓTEM page is responsive
    await page.click('text=TÓTEM - Crear Ticket');
    await expect(page.locator('h2')).toContainText('Crear Nuevo Ticket');
    
    // Form should be usable on mobile
    await expect(page.locator('input[placeholder="12345678-9"]')).toBeVisible();
    await expect(page.locator('input[placeholder="+56912345678"]')).toBeVisible();
    
    // Queue type selection should work on mobile
    await expect(page.locator('input[value="CAJA"]')).toBeVisible();
  });
});