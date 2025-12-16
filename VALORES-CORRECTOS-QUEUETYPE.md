# ✅ Valores Correctos para QueueType

## 🚨 Error Corregido
**Error anterior:** `"GENERAL"` no es un valor válido

## 📋 Valores Válidos de QueueType

| Valor | Descripción | Prefijo | Tiempo Promedio |
|-------|-------------|---------|-----------------|
| `CAJA` | Caja | C | 5 minutos |
| `PERSONAL_BANKER` | Personal Banker | P | 15 minutos |
| `EMPRESAS` | Empresas | E | 20 minutos |
| `GERENCIA` | Gerencia | G | 30 minutos |

## 🎯 Ejemplos de Uso Correcto

### Crear Ticket - CAJA
```json
{
  "nationalId": "12345678",
  "phoneNumber": "987654321",
  "branchOffice": "Sucursal Centro",
  "queueType": "CAJA"
}
```

### Crear Ticket - GERENCIA
```json
{
  "nationalId": "87654321",
  "phoneNumber": "912345678",
  "branchOffice": "Sucursal Norte",
  "queueType": "GERENCIA"
}
```

### Consultar Cola
```bash
GET /api/admin/queue/CAJA
GET /api/admin/queue/PERSONAL_BANKER
GET /api/admin/queue/EMPRESAS
GET /api/admin/queue/GERENCIA
```

## 🔧 Archivos Actualizados
- ✅ `postman/Ticketero-API.postman_collection.json`
- ✅ `postman/Environment-Local.postman_environment.json`

## 🚀 Listo para Usar
La colección de Postman ahora tiene los valores correctos y funcionará sin errores.