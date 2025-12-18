# 🧪 Testing - Sistema Ticketero Frontend

## 📋 Resumen

Este documento describe la estrategia de testing implementada para el frontend del Sistema Ticketero, incluyendo pruebas unitarias, de integración y end-to-end (E2E).

## 🏗️ Arquitectura de Testing

```
tests/
├── unit/                    # Tests unitarios (Vitest + Testing Library)
│   ├── Button.test.ts      # Componentes UI
│   ├── validation.test.ts  # Lógica de validación
│   └── ticketService.test.ts # Services
├── e2e/                    # Tests E2E (Playwright)
│   ├── totem-flow.spec.ts  # Flujo TÓTEM completo
│   ├── admin-flow.spec.ts  # Flujo ADMIN completo
│   └── integration-flow.spec.ts # Integración completa
└── setup.ts               # Configuración global
```

## 🚀 Configuración Inicial

### 1. Instalar Dependencias
```bash
# Opción 1: Script automático
scripts\setup-testing.bat

# Opción 2: Manual
npm install @testing-library/svelte @testing-library/jest-dom @testing-library/user-event @playwright/test jsdom --save-dev
npx playwright install
```

### 2. Verificar Configuración
```bash
npm run check
npm run test:unit -- --run
```

## 🎯 Tipos de Pruebas

### Unit Tests (Vitest + Testing Library)

**Objetivo:** Probar componentes y funciones de forma aislada

**Cobertura:**
- ✅ Componentes UI (Button, Input, Modal)
- ✅ Validaciones de formulario (RUT, teléfono)
- ✅ Services (TicketService, DashboardService)
- ✅ Stores (estado global)
- ✅ Utilidades (helpers, formatters)

**Comandos:**
```bash
npm run test:unit           # Ejecutar una vez
npm run test:unit:watch     # Modo watch
```

### E2E Tests (Playwright)

**Objetivo:** Probar flujos completos de usuario

**Cobertura:**
- ✅ Flujo TÓTEM: Crear ticket completo
- ✅ Flujo ADMIN: Dashboard y gestión
- ✅ Integración: Ciclo completo de ticket
- ✅ Navegación entre pantallas
- ✅ Responsive design
- ✅ Manejo de errores

**Comandos:**
```bash
npm run test:e2e           # Ejecutar E2E
npm run test:e2e:ui        # Modo UI interactivo
```

## 📊 Ejecutar Pruebas

### Suite Completa
```bash
# Opción 1: Script automático (recomendado)
scripts\run-tests.bat

# Opción 2: Manual
npm run test:all
```

### Pruebas Individuales
```bash
# Solo unit tests
npm run test:unit

# Solo E2E tests
npm run test:e2e

# Test específico
npm run test:unit -- Button.test.ts
npx playwright test totem-flow.spec.ts
```

### Modo Desarrollo
```bash
# Unit tests en modo watch
npm run test:unit:watch

# E2E con UI interactiva
npm run test:e2e:ui
```

## 🎭 Mocking y Fixtures

### API Mocking (E2E)
```typescript
// Mock respuesta exitosa
await page.route('**/api/tickets', async route => {
  await route.fulfill({
    status: 201,
    contentType: 'application/json',
    body: JSON.stringify({
      ticket: { /* datos del ticket */ }
    })
  });
});

// Mock error
await page.route('**/api/tickets', async route => {
  await route.fulfill({
    status: 409,
    body: JSON.stringify({ message: 'Ticket duplicado' })
  });
});
```

### Service Mocking (Unit)
```typescript
// Mock service
vi.mock('$lib/services/api', () => ({
  api: {
    post: vi.fn(),
    get: vi.fn()
  }
}));

// Configurar mock
vi.mocked(api.post).mockResolvedValue(mockResponse);
```

## 📈 Reportes y Cobertura

### Unit Tests
- **Reporte:** `coverage/index.html`
- **Formato:** HTML interactivo con líneas cubiertas
- **Objetivo:** >80% cobertura en componentes críticos

### E2E Tests
- **Reporte:** `playwright-report/index.html`
- **Incluye:** Screenshots, videos, traces
- **Formato:** HTML con detalles de cada test

### Ver Reportes
```bash
# Abrir reporte de cobertura
start coverage/index.html

# Abrir reporte E2E
start playwright-report/index.html
```

## 🐛 Debugging

### Unit Tests
```bash
# Debug con breakpoints
npm run test:unit -- --inspect-brk

# Logs detallados
npm run test:unit -- --reporter=verbose
```

### E2E Tests
```bash
# Modo debug (pausa en cada paso)
npx playwright test --debug

# Con UI interactiva
npm run test:e2e:ui

# Solo en un navegador
npx playwright test --project=chromium
```

### Logs y Screenshots
```typescript
// En tests E2E
await page.screenshot({ path: 'debug-screenshot.png' });
console.log('Current URL:', page.url());
```

## 🔧 Configuración Avanzada

### Vitest Config (`vitest.config.ts`)
```typescript
export default defineConfig({
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    coverage: {
      reporter: ['text', 'html'],
      exclude: ['node_modules/', 'src/test/']
    }
  }
});
```

### Playwright Config (`playwright.config.ts`)
```typescript
export default defineConfig({
  testDir: './tests/e2e',
  use: {
    baseURL: 'http://localhost:4173',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure'
  },
  projects: [
    { name: 'chromium' },
    { name: 'firefox' },
    { name: 'webkit' }
  ]
});
```

## 🎯 Mejores Prácticas

### Unit Tests
- ✅ Probar comportamiento, no implementación
- ✅ Un concepto por test
- ✅ Nombres descriptivos
- ✅ Arrange-Act-Assert pattern
- ✅ Mock dependencias externas

### E2E Tests
- ✅ Probar flujos de usuario reales
- ✅ Usar data-testid para selectores estables
- ✅ Mock APIs para tests determinísticos
- ✅ Verificar estados visuales importantes
- ✅ Manejar timing con waitFor

### General
- ✅ Tests independientes (no dependen entre sí)
- ✅ Datos de prueba únicos
- ✅ Cleanup después de cada test
- ✅ Documentar casos edge
- ✅ Mantener tests actualizados

## 🚨 Troubleshooting

### Problemas Comunes

**1. Tests unitarios fallan con imports de SvelteKit**
```bash
# Verificar setup.ts tiene mocks correctos
# Verificar vitest.config.ts incluye setupFiles
```

**2. E2E tests fallan por timeout**
```bash
# Aumentar timeout en playwright.config.ts
# Verificar que backend esté ejecutándose
# Usar waitFor para elementos dinámicos
```

**3. Playwright no encuentra navegadores**
```bash
npx playwright install
```

**4. Coverage no se genera**
```bash
# Verificar vitest.config.ts tiene coverage configurado
npm run test:unit -- --coverage
```

### Logs de Debug
```bash
# Habilitar logs detallados
DEBUG=pw:api npm run test:e2e
VITEST_LOG_LEVEL=debug npm run test:unit
```

## 📋 Checklist de Testing

### Antes de Commit
- [ ] Todos los unit tests pasan
- [ ] Cobertura >80% en archivos modificados
- [ ] E2E tests críticos pasan
- [ ] No hay warnings en consola

### Antes de Deploy
- [ ] Suite completa de tests pasa
- [ ] Tests en múltiples navegadores
- [ ] Performance tests aceptables
- [ ] Reportes generados y revisados

### Mantenimiento
- [ ] Tests actualizados con nuevas features
- [ ] Mocks actualizados con cambios de API
- [ ] Documentación de testing actualizada
- [ ] Cleanup de tests obsoletos

## 🎉 Integración Continua

### GitHub Actions (ejemplo)
```yaml
name: Frontend Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: 18
      - run: npm ci
      - run: npm run test:unit
      - run: npm run test:e2e
      - uses: actions/upload-artifact@v3
        with:
          name: test-reports
          path: |
            coverage/
            playwright-report/
```

---

## 📞 Soporte

**Documentación adicional:**
- [Vitest Docs](https://vitest.dev/)
- [Testing Library](https://testing-library.com/docs/svelte-testing-library/intro)
- [Playwright Docs](https://playwright.dev/)

**Comandos de ayuda:**
```bash
npm run test:unit -- --help
npx playwright test --help
```

---

**Versión:** 1.0  
**Última actualización:** Diciembre 2025  
**Mantenido por:** Equipo Frontend