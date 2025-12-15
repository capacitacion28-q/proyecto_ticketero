# Evidencia CP-005: Validación de Tickets Duplicados

**Caso de Prueba:** CP-005 - Validación de Reglas de Negocio  
**Requerimiento:** RN-001 - Unicidad de Ticket Activo  
**Fecha:** 15 Diciembre 2025  
**Estado:** ❌ FALLIDO

---

## Contexto de la Prueba

### Estado Previo del Sistema
- **Ticket existente:** C05 con nationalId "12345678-9" (EN_ESPERA)
- **Objetivo:** Validar que no se permita crear segundo ticket con mismo RUT
- **Regla esperada:** RN-001 - Un cliente solo puede tener 1 ticket activo

---

## Prueba Ejecutada

### Comando Ejecutado
```bash
curl -X POST http://localhost:8081/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678-9",
    "phoneNumber": "+56912345678",
    "queueType": "PERSONAL_BANKER",
    "branchOffice": "SUCURSAL_CENTRO"
  }'
```

### Respuesta Obtenida
```json
{
  "id": 3,
  "codigoReferencia": "18341cbe-12c0-41ba-adc9-dbcd56320199",
  "numero": "P73",
  "nationalId": "12345678-9",
  "telefono": null,
  "branchOffice": "SUCURSAL_CENTRO",
  "queueType": "PERSONAL_BANKER",
  "status": "EN_ESPERA",
  "positionInQueue": 1,
  "estimatedWaitMinutes": 15,
  "assignedAdvisorName": null,
  "assignedModuleNumber": null,
  "createdAt": "2025-12-15T17:04:26.1742774"
}
```

---

## Análisis del Error

### ❌ Violación de Regla de Negocio
- **RN-001 VIOLADA:** Sistema permitió crear segundo ticket activo
- **Status HTTP:** 200 OK (debería ser 409 Conflict)
- **Comportamiento esperado:** Error con mensaje "Cliente ya tiene un ticket activo"
- **Comportamiento actual:** Creación exitosa del segundo ticket

### ❌ Estado Actual del Sistema
- **Cliente 12345678-9 tiene:**
  - Ticket C05 (CAJA, EN_ESPERA)
  - Ticket P73 (PERSONAL_BANKER, EN_ESPERA)
- **Problema:** 2 tickets activos para el mismo cliente

---

## Validaciones Realizadas

### ❌ Validación de Unicidad
- [x] **Ticket creado:** Sí (incorrecto)
- [x] **Status HTTP:** 200 (debería ser 409)
- [x] **Mensaje de error:** Ninguno (debería existir)
- [x] **Validación RN-001:** ❌ NO IMPLEMENTADA

### ✅ Validaciones de Estructura (Ticket Creado)
- [x] **ID secuencial:** `3` (correcto)
- [x] **UUID único:** `18341cbe-12c0-41ba-adc9-dbcd56320199`
- [x] **Número:** `P73` (prefijo P correcto)
- [x] **Cola:** `PERSONAL_BANKER` (correcto)
- [x] **Tiempo estimado:** `15 minutos` (correcto)

---

## Impacto en el Sistema

### ❌ Reglas de Negocio Violadas
| Regla | Descripción | Estado |
|-------|-------------|--------|
| RN-001 | Un cliente solo puede tener 1 ticket activo | ❌ VIOLADA |

### ❌ Problemas Derivados
1. **Confusión del cliente:** ¿Cuál ticket es válido?
2. **Asignación múltiple:** Cliente podría ser llamado 2 veces
3. **Métricas incorrectas:** Conteos duplicados
4. **Experiencia de usuario:** Comportamiento inesperado

---

## Comparación de Tickets del Mismo Cliente

| Campo | Ticket C05 | Ticket P73 | Observación |
|-------|------------|------------|-------------|
| nationalId | 12345678-9 | 12345678-9 | ❌ Duplicado |
| queueType | CAJA | PERSONAL_BANKER | Diferentes colas |
| status | EN_ESPERA | EN_ESPERA | ❌ Ambos activos |
| positionInQueue | 1 | 1 | Posiciones independientes |

---

## Corrección Requerida

### Implementación Necesaria
```java
// En TicketService.create()
Optional<Ticket> existingTicket = ticketRepository
    .findByNationalIdAndStatusIn(request.nationalId(), ACTIVE_STATUSES);

if (existingTicket.isPresent()) {
    throw new ConflictException("Cliente ya tiene un ticket activo");
}
```

### Estados Activos a Validar
- EN_ESPERA
- PROXIMO  
- ATENDIENDO

### Respuesta Esperada (409 Conflict)
```json
{
  "message": "Cliente ya tiene un ticket activo",
  "status": 409,
  "timestamp": "2025-12-15T17:04:26Z"
}
```

---

## Pruebas Adicionales Requeridas

### Después de la Corrección
1. **Intentar crear ticket duplicado** → Debe fallar con 409
2. **Completar ticket existente** → Debe permitir crear nuevo
3. **Cancelar ticket existente** → Debe permitir crear nuevo
4. **Validar con diferentes estados** (EN_ESPERA, PROXIMO, ATENDIENDO)

---

## Recomendaciones

### Prioridad CRÍTICA
1. **Implementar validación RN-001** inmediatamente
2. **Agregar query repository** para buscar tickets activos
3. **Crear exception ConflictException** para error 409
4. **Agregar test unitario** para esta validación

### Validaciones Adicionales
- Verificar que la validación funcione con todos los estados activos
- Probar edge cases (tickets en diferentes sucursales)
- Validar performance de la query de búsqueda

---

## Próximos Pasos

1. **CRÍTICO:** Corregir validación de duplicados
2. **Re-ejecutar prueba** una vez corregido
3. **Limpiar datos de prueba** (eliminar tickets duplicados)
4. **Continuar con otras validaciones**

---

**Resultado:** ❌ **FALLIDO CRÍTICO** - RN-001 no implementada, permite tickets duplicados

**Tester:** Sistema Automatizado  
**Timestamp:** 2025-12-15T17:04:26Z  
**Prioridad:** CRÍTICA - Regla de negocio fundamental violada