# Evidencia CP-010: Dashboard con Múltiples Tickets

**Caso de Prueba:** CP-010 - Dashboard Actualizado  
**Requerimiento:** RF-007 - Panel de Monitoreo para Supervisor  
**Fecha:** 15 Diciembre 2025  
**Estado:** ⚠️ PARCIAL

---

## Contexto de la Prueba

### Estado del Sistema
- **Tickets creados:** 5 total
  - C05 (CAJA, nationalId: 12345678-9)
  - G95 (GERENCIA, nationalId: 22222222-2)  
  - P73 (PERSONAL_BANKER, nationalId: 12345678-9) ← Duplicado
  - C63 (CAJA, nationalId: 123) ← RUT inválido
  - C70 (CAJA, nationalId: 99999999-9, teléfono inválido)

---

## Prueba Ejecutada

### Comando Ejecutado
```bash
curl http://localhost:8081/api/admin/dashboard
```

### Respuesta Obtenida
```json
{
  "summary": {
    "totalTicketsToday": 5,
    "ticketsInQueue": 0,
    "ticketsBeingServed": 0,
    "ticketsCompleted": 0,
    "availableAdvisors": 5,
    "avgWaitTime": 15.0
  },
  "queueStats": [
    {
      "queueType": "CAJA",
      "ticketsWaiting": 0,
      "avgWaitMinutes": 5,
      "longestWaitMinutes": 10
    },
    {
      "queueType": "PERSONAL_BANKER",
      "ticketsWaiting": 0,
      "avgWaitMinutes": 15,
      "longestWaitMinutes": 30
    },
    {
      "queueType": "EMPRESAS",
      "ticketsWaiting": 0,
      "avgWaitMinutes": 20,
      "longestWaitMinutes": 40
    },
    {
      "queueType": "GERENCIA",
      "ticketsWaiting": 0,
      "avgWaitMinutes": 30,
      "longestWaitMinutes": 60
    }
  ],
  "advisorStats": [],
  "lastUpdated": "2025-12-15T17:12:00.076252"
}
```

---

## Validaciones Realizadas

### ✅ Conteo Total Correcto
- [x] **totalTicketsToday:** `5` ✅ (correcto, 5 tickets creados)
- [x] **lastUpdated:** Timestamp actualizado ✅

### ❌ Inconsistencias Críticas
- [x] **ticketsInQueue:** `0` ❌ (debería ser 5, todos EN_ESPERA)
- [x] **ticketsBeingServed:** `0` ✅ (correcto, ninguno asignado)
- [x] **ticketsCompleted:** `0` ✅ (correcto, ninguno completado)

### ❌ Queue Stats Inconsistentes
- [x] **CAJA ticketsWaiting:** `0` ❌ (debería ser 3: C05, C63, C70)
- [x] **GERENCIA ticketsWaiting:** `0` ❌ (debería ser 1: G95)
- [x] **PERSONAL_BANKER ticketsWaiting:** `0` ❌ (debería ser 1: P73)
- [x] **EMPRESAS ticketsWaiting:** `0` ✅ (correcto, ninguno)

### ❌ Advisor Stats Vacío
- [x] **advisorStats:** `[]` ❌ (debería mostrar 5 asesores)

---

## Análisis de Inconsistencias

### Problema Principal: Contadores en Cero
- **totalTicketsToday = 5** ✅ (sistema cuenta tickets creados)
- **ticketsInQueue = 0** ❌ (no cuenta tickets EN_ESPERA)
- **ticketsWaiting = 0** ❌ (en todas las colas)

### Posibles Causas
1. **Query incorrecta:** No filtra por status EN_ESPERA
2. **Estados inconsistentes:** Tickets no están realmente EN_ESPERA
3. **Lógica de conteo:** Error en el cálculo de métricas
4. **Cache desactualizado:** Dashboard no refleja estado real

---

## Comparación Esperado vs Actual

### Estado Esperado
```json
{
  "summary": {
    "totalTicketsToday": 5,
    "ticketsInQueue": 5,        // ← Debería ser 5
    "ticketsBeingServed": 0,
    "ticketsCompleted": 0,
    "availableAdvisors": 5,
    "avgWaitTime": 15.0
  },
  "queueStats": [
    {
      "queueType": "CAJA",
      "ticketsWaiting": 3,      // ← C05, C63, C70
      "avgWaitMinutes": 5,
      "longestWaitMinutes": 10
    },
    {
      "queueType": "GERENCIA",
      "ticketsWaiting": 1,      // ← G95
      "avgWaitMinutes": 30,
      "longestWaitMinutes": 60
    },
    {
      "queueType": "PERSONAL_BANKER",
      "ticketsWaiting": 1,      // ← P73
      "avgWaitMinutes": 15,
      "longestWaitMinutes": 30
    }
  ]
}
```

---

## Impacto en Supervisión

### ❌ Información Incorrecta para Supervisor
- **No ve tickets en espera:** Cree que no hay trabajo pendiente
- **No puede gestionar carga:** Sin visibilidad de colas reales
- **Decisiones erróneas:** Basadas en datos incorrectos
- **Sin gestión de asesores:** advisorStats vacío

### ❌ Funcionalidad RF-007 Comprometida
- Dashboard existe pero datos incorrectos
- Supervisor no puede supervisar efectivamente
- Métricas no confiables para toma de decisiones

---

## Investigación Requerida

### Verificar en Base de Datos
```sql
-- Verificar estado real de tickets
SELECT numero, status, queue_type, created_at 
FROM ticket 
WHERE DATE(created_at) = CURRENT_DATE
ORDER BY created_at;

-- Contar por estado
SELECT status, COUNT(*) 
FROM ticket 
WHERE DATE(created_at) = CURRENT_DATE
GROUP BY status;

-- Verificar asesores
SELECT id, name, status, assigned_tickets_count 
FROM advisor;
```

---

## Recomendaciones

### Corrección Inmediata Requerida
1. **Revisar queries** en AdminService para conteo de tickets
2. **Verificar filtros** por status EN_ESPERA
3. **Corregir lógica** de ticketsInQueue y ticketsWaiting
4. **Poblar advisorStats** con datos de asesores

### Validaciones Adicionales
- Verificar que tickets se crean con status correcto
- Probar dashboard después de asignaciones automáticas
- Validar actualización en tiempo real

---

## Próximos Pasos

1. **CRÍTICO:** Investigar por qué contadores están en 0
2. **Verificar** estado real de tickets en BD
3. **Probar** asignación automática con scheduler
4. **Corregir** lógica de dashboard antes de producción

---

**Resultado:** ⚠️ **PARCIAL** - Dashboard funciona pero datos incorrectos, supervisión comprometida

**Tester:** Sistema Automatizado  
**Timestamp:** 2025-12-15T17:12:00Z  
**Prioridad:** ALTA - Datos incorrectos afectan supervisión operacional