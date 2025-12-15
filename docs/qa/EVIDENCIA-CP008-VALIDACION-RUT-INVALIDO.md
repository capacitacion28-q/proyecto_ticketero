# Evidencia CP-008: Validación RUT Inválido

**Caso de Prueba:** CP-008 - Validaciones de Entrada  
**Requerimiento:** RF-001 - Crear Ticket Digital (Validaciones)  
**Fecha:** 15 Diciembre 2025  
**Estado:** ❌ FALLIDO

---

## Contexto de la Prueba

### Objetivo
- **Validar:** Sistema rechace RUT/ID inválidos
- **RUT probado:** "123" (claramente inválido)
- **Expectativa:** Error 400 Bad Request con mensaje de validación

---

## Prueba Ejecutada

### Comando Ejecutado
```bash
curl -X POST http://localhost:8081/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "123",
    "phoneNumber": "+56912345678",
    "queueType": "CAJA",
    "branchOffice": "SUCURSAL_CENTRO"
  }'
```

### Respuesta Obtenida
```json
{
  "id": 4,
  "codigoReferencia": "7298752b-dedf-4648-94d7-11ed7d9dbc59",
  "numero": "C63",
  "nationalId": "123",
  "telefono": null,
  "branchOffice": "SUCURSAL_CENTRO",
  "queueType": "CAJA",
  "status": "EN_ESPERA",
  "positionInQueue": 1,
  "estimatedWaitMinutes": 5,
  "assignedAdvisorName": null,
  "assignedModuleNumber": null,
  "createdAt": "2025-12-15T17:09:38.0649887"
}
```

---

## Análisis del Error

### ❌ Validación No Implementada
- **Status HTTP:** 200 OK (debería ser 400 Bad Request)
- **Comportamiento:** Sistema aceptó RUT inválido "123"
- **Problema:** Sin validación de formato de nationalId
- **Impacto:** Datos inconsistentes en el sistema

---

## Validaciones Realizadas

### ❌ Validación de Formato RUT
- [x] **RUT inválido rechazado:** ❌ NO (aceptó "123")
- [x] **Status HTTP correcto:** ❌ NO (200 en lugar de 400)
- [x] **Mensaje de error:** ❌ NO (sin mensaje)
- [x] **Ticket creado:** ✅ SÍ (incorrecto)

### ✅ Funcionalidad Básica (Ticket Creado)
- [x] **ID generado:** `4` (secuencial)
- [x] **UUID generado:** `7298752b-dedf-4648-94d7-11ed7d9dbc59`
- [x] **Número:** `C63` (formato correcto)
- [x] **nationalId preservado:** `123` (valor inválido guardado)

---

## Formatos RUT Esperados vs Actual

### ✅ Formatos Válidos Esperados
| Formato | Ejemplo | Descripción |
|---------|---------|-------------|
| RUT Chileno | 12345678-9 | Con dígito verificador |
| RUT sin guión | 123456789 | 8-9 dígitos numéricos |
| ID extranjero | P12345678 | Alfanumérico 8-12 chars |

### ❌ Formato Inválido Aceptado
| Formato | Ejemplo | Estado |
|---------|---------|--------|
| Muy corto | 123 | ❌ Aceptado (incorrecto) |

---

## Impacto en el Sistema

### ❌ Problemas Derivados
1. **Datos inconsistentes:** RUTs inválidos en base de datos
2. **Integración futura:** Problemas con sistemas externos
3. **Experiencia usuario:** Confusión con identificaciones inválidas
4. **Auditoría:** Trazabilidad comprometida con IDs falsos

### ❌ Validaciones Faltantes
- Sin validación de longitud mínima
- Sin validación de formato RUT chileno
- Sin validación de dígito verificador
- Sin validación de caracteres permitidos

---

## Corrección Requerida

### Implementación Necesaria en DTO
```java
public record CreateTicketRequest(
    @NotBlank(message = "National ID is required")
    @Pattern(
        regexp = "^(\\d{7,8}-[\\dkK]|\\d{8,9}|[A-Z]\\d{7,11})$",
        message = "Invalid national ID format"
    )
    String nationalId,
    // ... otros campos
) {}
```

### Validaciones Específicas
1. **RUT Chileno:** `\d{7,8}-[\dkK]` (ej: 12345678-9)
2. **RUT sin guión:** `\d{8,9}` (ej: 123456789)
3. **ID extranjero:** `[A-Z]\d{7,11}` (ej: P12345678)

### Respuesta Esperada (400 Bad Request)
```json
{
  "message": "Validation failed",
  "status": 400,
  "errors": ["nationalId: Invalid national ID format"],
  "timestamp": "2025-12-15T17:09:38Z"
}
```

---

## Casos de Prueba Adicionales

### Después de la Corrección
```bash
# RUT muy corto
curl -X POST ... -d '{"nationalId": "123", ...}'
# Esperado: 400 Bad Request

# RUT con formato incorrecto
curl -X POST ... -d '{"nationalId": "12345678", ...}'
# Esperado: 400 Bad Request

# RUT válido con guión
curl -X POST ... -d '{"nationalId": "12345678-9", ...}'
# Esperado: 201 Created

# ID extranjero válido
curl -X POST ... -d '{"nationalId": "P12345678", ...}'
# Esperado: 201 Created
```

---

## Recomendaciones

### Prioridad ALTA
1. **Implementar validaciones** de formato nationalId
2. **Agregar @Pattern** en CreateTicketRequest
3. **Crear tests unitarios** para validaciones
4. **Limpiar datos inválidos** existentes

### Validaciones Adicionales Sugeridas
- Validar dígito verificador RUT chileno
- Normalizar formatos (agregar guión si falta)
- Validar que ID extranjero tenga prefijo válido
- Considerar whitelist de países para IDs extranjeros

---

## Próximos Pasos

1. **ALTA PRIORIDAD:** Implementar validaciones nationalId
2. **Probar validación** de teléfono
3. **Probar otros campos** requeridos
4. **Re-ejecutar pruebas** una vez corregido

---

**Resultado:** ❌ **FALLIDO** - Validaciones de entrada no implementadas, acepta datos inválidos

**Tester:** Sistema Automatizado  
**Timestamp:** 2025-12-15T17:09:38Z  
**Prioridad:** ALTA - Integridad de datos comprometida