# PASO 3: RF-002 - Enviar Notificaciones Automáticas vía Telegram

## Contenido de este paso:
- ✅ Descripción completa del requerimiento
- ✅ Modelo de datos Mensaje con 8 campos
- ✅ 3 Plantillas de mensajes con formato HTML y emojis
- ✅ 4 Reglas de negocio aplicables
- ✅ 7 Escenarios Gherkin (éxito, fallos, reintentos, backoff)

---

## RF-002: Enviar Notificaciones Automáticas vía Telegram

**Descripción:**  
El sistema debe enviar automáticamente tres mensajes vía Telegram al cliente durante el ciclo de vida del ticket: (1) confirmación inmediata al crear el ticket con número, posición y tiempo estimado; (2) pre-aviso cuando quedan 3 personas adelante solicitando acercarse a sucursal; (3) notificación de turno activo indicando módulo y nombre del asesor asignado. El sistema debe manejar fallos de red con reintentos automáticos y backoff exponencial.

**Prioridad:** Alta

**Actor Principal:** Sistema (automatizado)

**Precondiciones:**
- Ticket creado con teléfono válido
- Telegram Bot configurado y activo
- Cliente tiene cuenta de Telegram vinculada al teléfono

---

### Modelo de Datos (Entidad Mensaje)

| Campo | Tipo | Descripción | Ejemplo |
|-------|------|-------------|---------|
| id | BIGSERIAL | Primary key | 1 |
| ticket_id | BIGINT | Foreign key a ticket | 123 |
| plantilla | String | Nombre de la plantilla | "totem_ticket_creado" |
| estadoEnvio | Enum | Estado del envío | PENDIENTE, ENVIADO, FALLIDO |
| fechaProgramada | Timestamp | Cuándo debe enviarse | "2025-12-15T10:30:00Z" |
| fechaEnvio | Timestamp | Cuándo se envió (nullable) | "2025-12-15T10:30:05Z" |
| telegramMessageId | String | ID retornado por Telegram (nullable) | "12345" |
| intentos | Integer | Contador de reintentos | 0, 1, 2, 3, 4 |

**Total de campos:** 8

---

### Plantillas de Mensajes

#### 1. totem_ticket_creado (Mensaje 1 - Confirmación)

**Momento de envío:** Inmediato al crear ticket

**Contenido:**
```
✅ <b>Ticket Creado</b>

Tu número de turno: <b>{numero}</b>
Posición en cola: <b>#{posicion}</b>
Tiempo estimado: <b>{tiempo} minutos</b>

Te notificaremos cuando estés próximo.
```

**Variables:**
- `{numero}`: Número del ticket (ej: "C01")
- `{posicion}`: Posición en cola (ej: 5)
- `{tiempo}`: Tiempo estimado en minutos (ej: 25)

**Ejemplo renderizado:**
```
✅ Ticket Creado

Tu número de turno: C01
Posición en cola: #5
Tiempo estimado: 25 minutos

Te notificaremos cuando estés próximo.
```

---

#### 2. totem_proximo_turno (Mensaje 2 - Pre-aviso)

**Momento de envío:** Cuando posición ≤ 3

**Contenido:**
```
⏰ <b>¡Pronto será tu turno!</b>

Turno: <b>{numero}</b>
Faltan aproximadamente 3 turnos.

Por favor, acércate a la sucursal.
```

**Variables:**
- `{numero}`: Número del ticket (ej: "P05")

**Ejemplo renderizado:**
```
⏰ ¡Pronto será tu turno!

Turno: P05
Faltan aproximadamente 3 turnos.

Por favor, acércate a la sucursal.
```

---

#### 3. totem_es_tu_turno (Mensaje 3 - Turno Activo)

**Momento de envío:** Al asignar a asesor

**Contenido:**
```
🔔 <b>¡ES TU TURNO {numero}!</b>

Dirígete al módulo: <b>{modulo}</b>
Asesor: <b>{nombreAsesor}</b>
```

**Variables:**
- `{numero}`: Número del ticket (ej: "E03")
- `{modulo}`: Número de módulo (ej: 3)
- `{nombreAsesor}`: Nombre del asesor (ej: "Juan Pérez")

**Ejemplo renderizado:**
```
🔔 ¡ES TU TURNO E03!

Dirígete al módulo: 3
Asesor: Juan Pérez
```

---

### Reglas de Negocio Aplicables

| Regla | Descripción | Aplicación en RF-002 |
|-------|-------------|----------------------|
| RN-007 | 3 reintentos automáticos | Si falla envío, reintentar hasta 3 veces |
| RN-008 | Backoff exponencial: 30s, 60s, 120s | Tiempos de espera entre reintentos |
| RN-011 | Auditoría obligatoria | Registrar cada envío exitoso o fallido |
| RN-012 | Mensaje 2 cuando posición ≤ 3 | Trigger para enviar pre-aviso |

---

### Criterios de Aceptación (Gherkin)

#### Escenario 1: Envío exitoso del Mensaje 1 (confirmación)

```gherkin
Given un ticket fue creado con:
  | numero   | C05          |
  | telefono | +56912345678 |
  | posicion | 5            |
  | tiempo   | 25           |
When el sistema programa el Mensaje 1
And el sistema ejecuta el envío
Then el sistema invoca Telegram API con plantilla "totem_ticket_creado"
And Telegram API retorna telegramMessageId "12345"
And el sistema actualiza el mensaje con:
  | estadoEnvio       | ENVIADO          |
  | telegramMessageId | 12345            |
  | fechaEnvio        | timestamp actual |
  | intentos          | 1                |
And el sistema registra auditoría "MENSAJE_ENVIADO"
```

**Validación:** Aplica RN-011 (Auditoría obligatoria)

---

#### Escenario 2: Envío exitoso del Mensaje 2 (pre-aviso)

```gherkin
Given un ticket tiene positionInQueue = 3
And el ticket tiene status EN_ESPERA
When el sistema detecta que posición ≤ 3
Then el sistema programa Mensaje 2 con plantilla "totem_proximo_turno"
And el sistema envía el mensaje inmediatamente
And el mensaje contiene "Faltan aproximadamente 3 turnos"
And estadoEnvio = ENVIADO
```

**Validación:** Aplica RN-012 (Umbral de pre-aviso)

---

#### Escenario 3: Envío exitoso del Mensaje 3 (turno activo)

```gherkin
Given un ticket fue asignado a:
  | asesor | Juan Pérez |
  | modulo | 3          |
When el sistema programa el Mensaje 3
And el sistema ejecuta el envío
Then el mensaje contiene:
  | campo        | valor                |
  | plantilla    | totem_es_tu_turno    |
  | modulo       | 3                    |
  | nombreAsesor | Juan Pérez           |
And estadoEnvio = ENVIADO
```

---

#### Escenario 4: Fallo de red en primer intento, éxito en segundo

```gherkin
Given un mensaje está en estado PENDIENTE
When el sistema intenta enviar (intento 1)
And Telegram API retorna error de red (timeout)
Then el sistema marca intentos = 1
And estadoEnvio permanece PENDIENTE
And el sistema espera 30 segundos (backoff)
When el sistema reintenta envío (intento 2)
And Telegram API retorna éxito con messageId "67890"
Then estadoEnvio = ENVIADO
And intentos = 2
And telegramMessageId = "67890"
```

**Validación:** Aplica RN-007 (Reintentos) y RN-008 (Backoff 30s)

---

#### Escenario 5: 3 reintentos fallidos → estado FALLIDO

```gherkin
Given un mensaje está en estado PENDIENTE
When el sistema intenta enviar (intento 1) y falla
And espera 30 segundos y reintenta (intento 2) y falla
And espera 60 segundos y reintenta (intento 3) y falla
And espera 120 segundos y reintenta (intento 4) y falla
Then estadoEnvio = FALLIDO
And intentos = 4
And el sistema registra auditoría "MENSAJE_FALLIDO"
And el sistema NO reintenta más
```

**Validación:** Aplica RN-007 (Máximo 3 reintentos = 4 intentos totales)

---

#### Escenario 6: Backoff exponencial entre reintentos

```gherkin
Given un mensaje falló en intento 1
When el sistema programa reintento 2
Then el delay es 30 segundos

Given el mensaje falló en intento 2
When el sistema programa reintento 3
Then el delay es 60 segundos

Given el mensaje falló en intento 3
When el sistema programa reintento 4
Then el delay es 120 segundos
```

**Validación:** Aplica RN-008 (Backoff exponencial)

**Tabla de tiempos:**
| Intento | Delay antes del intento |
|---------|-------------------------|
| 1       | 0s (inmediato)          |
| 2       | 30s                     |
| 3       | 60s                     |
| 4       | 120s                    |

---

#### Escenario 7: Cliente sin teléfono, no se programan mensajes

```gherkin
Given un ticket fue creado sin campo telefono
When el sistema evalúa programación de mensajes
Then el sistema NO crea registros en tabla Mensaje
And el ticket se procesa normalmente
```

**Caso edge:** Cliente sin notificaciones

---

### Postcondiciones

1. Mensaje insertado en BD con estado según resultado (ENVIADO o FALLIDO)
2. `telegram_message_id` almacenado si envío exitoso
3. Campo `intentos` incrementado en cada reintento
4. Auditoría registrada para cada envío (éxito o fallo)

---

### Endpoints HTTP

**Ninguno** - Este es un proceso interno automatizado por scheduler/worker.

El sistema monitorea la tabla Mensaje y procesa registros en estado PENDIENTE.

---

## Resumen del PASO 3

**Elementos documentados:**
- ✅ 1 Requerimiento Funcional (RF-002)
- ✅ 8 campos del modelo Mensaje
- ✅ 3 Plantillas con formato HTML y emojis (✅, ⏰, 🔔)
- ✅ 4 Reglas de negocio aplicadas (RN-007, RN-008, RN-011, RN-012)
- ✅ 7 Escenarios Gherkin
- ✅ 0 Endpoints HTTP (proceso interno)

**Cobertura de escenarios:**
- Happy path: Escenarios 1, 2, 3 (3 mensajes)
- Reintentos exitosos: Escenario 4
- Reintentos fallidos: Escenario 5
- Validación backoff: Escenario 6
- Edge cases: Escenario 7

**Plantillas documentadas:**
1. ✅ totem_ticket_creado (con emoji ✅)
2. ✅ totem_proximo_turno (con emoji ⏰)
3. ✅ totem_es_tu_turno (con emoji 🔔)

