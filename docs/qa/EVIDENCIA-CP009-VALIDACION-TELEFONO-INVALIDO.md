# Evidencia CP-009: Validación Teléfono Inválido

**Caso de Prueba:** CP-009 - Validaciones de Entrada (Teléfono)  
**Requerimiento:** RF-001 - Crear Ticket Digital (Validaciones)  
**Fecha:** 15 Diciembre 2025  
**Estado:** ❌ FALLIDO

---

## Contexto de la Prueba

### Objetivo
- **Validar:** Sistema rechace teléfonos inválidos
- **Teléfono probado:** "123" (claramente inválido)
- **Expectativa:** Error 400 Bad Request con mensaje de validación

---

## Prueba Ejecutada

### Comando Ejecutado
```bash
curl -X POST http://localhost:8081/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "99999999-9",
    "phoneNumber": "123",
    "queueType": "CAJA",
    "branchOffice": "SUCURSAL_CENTRO"
  }'
```

### Respuesta Obtenida
```json
{
  "id": 5,
  "codigoReferencia": "b303c80c-4d2f-44c9-a22c-71aa08353502",
  "numero": "C70",
  "nationalId": "99999999-9",
  "telefono": null,
  "branchOffice": "SUCURSAL_CENTRO",
  "queueType": "CAJA",
  "status": "EN_ESPERA",
  "positionInQueue": 1,
  "estimatedWaitMinutes": 5,
  "assignedAdvisorName": null,
  "assignedModuleNumber": null,
  "createdAt": "2025-12-15T17:11:02.7715672"
}
```

---

## Análisis del Error

### ❌ Validación No Implementada
- **Status HTTP:** 200 OK (debería ser 400 Bad Request)
- **Comportamiento:** Sistema aceptó teléfono inválido "123"
- **Observación:** Campo `telefono` aparece como `null` (posible procesamiento)
- **Problema:** Sin validación de formato de phoneNumber

---

## Validaciones Realizadas

### ❌ Validación de Formato Teléfono
- [x] **Teléfono inválido rechazado:** ❌ NO (aceptó "123")
- [x] **Status HTTP correcto:** ❌ NO (200 en lugar de 400)
- [x] **Mensaje de error:** ❌ NO (sin mensaje)
- [x] **Ticket creado:** ✅ SÍ (incorrecto)

### ⚠️ Comportamiento del Campo Teléfono
- [x] **phoneNumber enviado:** "123"
- [x] **telefono en respuesta:** `null`
- [x] **Posible procesamiento:** Sistema podría estar validando internamente pero no rechazando

---

## Formatos Teléfono Esperados vs Actual

### ✅ Formatos Válidos Esperados
| Formato | Ejemplo | Descripción |
|---------|---------|-------------|
| Internacional | +56912345678 | Código país + 9-15 dígitos |
| Nacional | 912345678 | 9 dígitos (se agrega +56) |

### ❌ Formato Inválido Aceptado
| Formato | Ejemplo | Estado |
|---------|---------|--------|
| Muy corto | 123 | ❌ Aceptado pero telefono=null |

---

## Patrón de Comportamiento Identificado

### Observación Crítica
- **Entrada:** phoneNumber = "123"
- **Salida:** telefono = null
- **Hipótesis:** Sistema procesa pero no guarda teléfonos inválidos
- **Problema:** No rechaza la request, acepta datos parcialmente inválidos

---

## Impacto en el Sistema

### ❌ Problemas Derivados
1. **Datos incompletos:** Tickets sin teléfono de contacto
2. **Notificaciones fallidas:** No se pueden enviar mensajes Telegram
3. **Experiencia usuario:** Cliente no recibe notificaciones
4. **Validación silenciosa:** Usuario no sabe que su teléfono es inválido

### ❌ Validaciones Faltantes
- Sin validación de longitud mínima
- Sin validación de formato internacional
- Sin validación de código de país
- Sin rechazo explícito de formatos inválidos

---

## Corrección Requerida

### Implementación Necesaria en DTO
```java
public record CreateTicketRequest(
    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^(\\+56)?[0-9]{8,9}$",
        message = "Invalid phone number format"
    )
    String phoneNumber,
    // ... otros campos
) {}
```

### Validaciones Específicas
1. **Internacional:** `\+56[0-9]{9}` (ej: +56912345678)
2. **Nacional:** `[0-9]{9}` (ej: 912345678)
3. **Longitud:** 8-9 dígitos después del código país

### Respuesta Esperada (400 Bad Request)
```json
{
  "message": "Validation failed",
  "status": 400,
  "errors": ["phoneNumber: Invalid phone number format"],
  "timestamp": "2025-12-15T17:11:02Z"
}
```

---

## Casos de Prueba Adicionales

### Después de la Corrección
```bash
# Teléfono muy corto
curl -X POST ... -d '{"phoneNumber": "123", ...}'
# Esperado: 400 Bad Request

# Teléfono con letras
curl -X POST ... -d '{"phoneNumber": "abc123def", ...}'
# Esperado: 400 Bad Request

# Teléfono válido internacional
curl -X POST ... -d '{"phoneNumber": "+56912345678", ...}'
# Esperado: 201 Created

# Teléfono válido nacional
curl -X POST ... -d '{"phoneNumber": "912345678", ...}'
# Esperado: 201 Created, normalizado a +56912345678
```

---

## Investigación Adicional Requerida

### Verificar Comportamiento Actual
1. **¿Por qué telefono=null?** ¿Hay validación silenciosa?
2. **¿Se normalizan teléfonos válidos?** ¿912345678 → +56912345678?
3. **¿Funcionan las notificaciones?** ¿Qué pasa sin teléfono?

---

## Recomendaciones

### Prioridad ALTA
1. **Implementar validaciones** de formato phoneNumber
2. **Rechazar explícitamente** teléfonos inválidos (400 Bad Request)
3. **Normalizar teléfonos válidos** (agregar +56 si falta)
4. **Probar notificaciones** con teléfonos válidos

### Validaciones Adicionales Sugeridas
- Validar códigos de país permitidos
- Normalizar formato de salida
- Validar que el teléfono sea alcanzable (opcional)
- Considerar whitelist de operadores móviles

---

## Próximos Pasos

1. **ALTA PRIORIDAD:** Implementar validaciones phoneNumber
2. **Investigar** por qué telefono aparece como null
3. **Probar notificaciones** Telegram con datos válidos
4. **Re-ejecutar pruebas** una vez corregido

---

**Resultado:** ❌ **FALLIDO** - Validaciones de teléfono no implementadas, acepta datos inválidos

**Tester:** Sistema Automatizado  
**Timestamp:** 2025-12-15T17:11:02Z  
**Prioridad:** ALTA - Notificaciones comprometidas sin teléfonos válidos