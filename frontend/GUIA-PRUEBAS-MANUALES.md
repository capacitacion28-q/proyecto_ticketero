# 🧪 Guía de Pruebas Manuales - Frontend Sistema Ticketero

## 📋 Información General

**Objetivo:** Validar manualmente el funcionamiento correcto del frontend integrado con el backend  
**Prerrequisitos:** Backend ejecutándose en `http://localhost:8081` y Frontend en `http://localhost:5173`  
**Tiempo estimado:** 30-45 minutos  
**Navegadores:** Chrome, Firefox, Safari (desktop y mobile)

---

## 🚀 Preparación del Entorno

### 1. Iniciar Backend
```bash
cd proyecto_ticketero
./start-local.bat
# Verificar: http://localhost:8081/api/health
```

### 2. Iniciar Frontend
```bash
cd frontend
npm install
npm run dev
# Acceder: http://localhost:5173
```

### 3. Verificar Conectividad
- ✅ Backend responde en puerto 8081
- ✅ Frontend carga en puerto 5173
- ✅ No hay errores de CORS en consola del navegador

---

## 🎯 CASO DE PRUEBA 1: Navegación Principal

### Objetivo
Verificar que la navegación entre pantallas funciona correctamente.

### Pasos
1. **Acceder a la página principal**
   - URL: `http://localhost:5173`
   - ✅ Se muestra título "Sistema Ticketero"
   - ✅ Se muestran 2 botones: "TÓTEM - Crear Ticket" y "ADMIN - Dashboard"
   - ✅ Diseño responsive se adapta al tamaño de pantalla

2. **Navegar a TÓTEM**
   - Hacer clic en "TÓTEM - Crear Ticket"
   - ✅ URL cambia a `/totem`
   - ✅ Se muestra formulario de creación de ticket
   - ✅ Botón "← Volver" funciona y regresa a home

3. **Navegar a ADMIN**
   - Desde home, hacer clic en "ADMIN - Dashboard"
   - ✅ URL cambia a `/admin`
   - ✅ Se muestra dashboard con métricas
   - ✅ Botón "← Volver" funciona y regresa a home

### Criterios de Éxito
- ✅ Navegación fluida sin errores
- ✅ URLs correctas en cada pantalla
- ✅ Botones de navegación funcionan
- ✅ No hay errores en consola del navegador

---

## 🎫 CASO DE PRUEBA 2: Crear Ticket (TÓTEM)

### Objetivo
Validar el flujo completo de creación de ticket desde la interfaz TÓTEM.

### Pasos

#### 2.1 Validaciones de Formulario
1. **Acceder a TÓTEM** (`/totem`)
2. **Probar validaciones de RUT/ID:**
   - Dejar campo vacío → ✅ Botón "Crear Ticket" deshabilitado
   - Ingresar "123" → ✅ Mensaje "Formato de RUT inválido"
   - Ingresar "12345678-9" → ✅ Validación pasa, sin mensaje de error
   - Ingresar "P12345678" → ✅ Validación pasa (formato extranjero)

3. **Probar validaciones de Teléfono:**
   - Dejar campo vacío → ✅ Botón deshabilitado
   - Ingresar "123" → ✅ Mensaje "Formato de teléfono inválido"
   - Ingresar "+56912345678" → ✅ Validación pasa
   - Ingresar "912345678" → ✅ Validación pasa

4. **Selección de Cola:**
   - ✅ CAJA seleccionado por defecto
   - ✅ Se pueden seleccionar otras opciones: PERSONAL_BANKER, EMPRESAS, GERENCIA
   - ✅ Cada opción muestra icono y tiempo estimado

#### 2.2 Creación Exitosa de Ticket
1. **Llenar formulario válido:**
   - RUT: `12345678-9`
   - Teléfono: `+56912345678`
   - Sucursal: `Sucursal Centro` (por defecto)
   - Cola: `CAJA` (por defecto)

2. **Enviar formulario:**
   - Hacer clic en "Crear Ticket"
   - ✅ Botón muestra "Creando Ticket..." con spinner
   - ✅ Se realiza llamada POST a `/api/tickets`

3. **Verificar respuesta exitosa:**
   - ✅ Modal "¡Ticket Creado!" aparece
   - ✅ Se muestra número de ticket (ej: "Ticket #C01")
   - ✅ Se muestra posición en cola (ej: "#1")
   - ✅ Se muestra tiempo estimado (ej: "5 min")
   - ✅ Mensaje sobre notificaciones Telegram

4. **Cerrar modal:**
   - Hacer clic en "Entendido"
   - ✅ Modal se cierra
   - ✅ Formulario se resetea (campos vacíos)

#### 2.3 Manejo de Errores
1. **Simular ticket duplicado:**
   - Usar mismo RUT que ticket anterior
   - ✅ Se muestra mensaje de error apropiado
   - ✅ Formulario permanece con datos ingresados

2. **Simular error de red:**
   - Detener backend temporalmente
   - Intentar crear ticket
   - ✅ Se muestra mensaje "Error al crear ticket"
   - ✅ Botón vuelve a estado normal

### Criterios de Éxito
- ✅ Validaciones funcionan en tiempo real
- ✅ Ticket se crea correctamente con backend
- ✅ Modal de éxito muestra información correcta
- ✅ Errores se manejan apropiadamente
- ✅ Formulario se resetea después del éxito

---

## 📊 CASO DE PRUEBA 3: Dashboard Admin

### Objetivo
Verificar que el dashboard admin muestra información correcta y se actualiza en tiempo real.

### Pasos

#### 3.1 Carga Inicial del Dashboard
1. **Acceder a ADMIN** (`/admin`)
2. **Verificar estado de carga:**
   - ✅ Se muestra "Cargando dashboard..." inicialmente
   - ✅ Spinner de carga visible
   - ✅ Llamadas API a `/api/admin/dashboard` y `/api/tickets/active`

3. **Verificar métricas principales:**
   - ✅ Card "Total Tickets" con número correcto
   - ✅ Card "Tickets Activos" con número correcto
   - ✅ Card "Completados" con número correcto
   - ✅ Card "Tiempo Promedio" con formato "X min"

#### 3.2 Panel de Asesores
1. **Verificar lista de asesores:**
   - ✅ Se muestran asesores con nombres
   - ✅ Estado visual correcto (✅ Disponible, 🔴 Ocupado, ⚫ Offline)
   - ✅ Número de módulo visible
   - ✅ Contador de tickets asignados

2. **Probar cambio de estado:**
   - Hacer clic en botón "Offline" de un asesor disponible
   - ✅ Se realiza llamada PUT a `/api/admin/advisors/{id}/status`
   - ✅ Estado se actualiza visualmente (si backend responde)

#### 3.3 Panel de Tickets Activos
1. **Verificar lista de tickets:**
   - ✅ Se muestran tickets con número (ej: C01, C02)
   - ✅ RUT/ID del cliente visible
   - ✅ Posición en cola mostrada
   - ✅ Estado con colores apropiados
   - ✅ Fecha/hora de creación

2. **Verificar estados vacíos:**
   - Si no hay tickets activos: ✅ Mensaje "No hay tickets activos" con emoji 🎉

#### 3.4 Actualización Automática
1. **Verificar auto-refresh:**
   - ✅ Dashboard se actualiza cada 5 segundos automáticamente
   - ✅ No hay parpadeo excesivo en la UI
   - ✅ Llamadas API periódicas visibles en Network tab

2. **Crear ticket desde TÓTEM:**
   - En otra pestaña, crear ticket en TÓTEM
   - Volver a dashboard ADMIN
   - ✅ Nuevo ticket aparece en "Tickets Activos" (máximo 5 segundos)
   - ✅ Métricas se actualizan correctamente

### Criterios de Éxito
- ✅ Dashboard carga sin errores
- ✅ Todas las métricas se muestran correctamente
- ✅ Asesores y tickets se listan apropiadamente
- ✅ Auto-refresh funciona sin problemas
- ✅ Cambios se reflejan en tiempo real

---

## 🔄 CASO DE PRUEBA 4: Flujo Integrado Completo

### Objetivo
Validar el ciclo completo de un ticket desde creación hasta finalización.

### Pasos

#### 4.1 Preparación
1. **Abrir 2 pestañas del navegador:**
   - Pestaña A: TÓTEM (`/totem`)
   - Pestaña B: ADMIN (`/admin`)

2. **Verificar estado inicial en ADMIN:**
   - ✅ Anotar métricas iniciales (Total, Activos, Completados)

#### 4.2 Crear Ticket
1. **En pestaña TÓTEM:**
   - Crear ticket con datos únicos:
     - RUT: `87654321-0`
     - Teléfono: `+56987654321`
     - Cola: `PERSONAL_BANKER`
   - ✅ Ticket creado exitosamente
   - ✅ Anotar número de ticket generado (ej: P01)

#### 4.3 Verificar en Dashboard
1. **En pestaña ADMIN:**
   - Esperar máximo 5 segundos para auto-refresh
   - ✅ "Total Tickets" incrementó en 1
   - ✅ "Tickets Activos" incrementó en 1
   - ✅ Nuevo ticket aparece en lista con estado "EN_ESPERA"
   - ✅ RUT correcto mostrado

#### 4.4 Simular Progreso del Ticket
1. **Usar Postman o curl para cambiar estado:**
   ```bash
   # Cambiar a ATENDIENDO
   curl -X PUT "http://localhost:8081/api/admin/tickets/{id}/status?status=ATENDIENDO"
   ```

2. **Verificar en ADMIN:**
   - ✅ Estado cambia a "ATENDIENDO" (máximo 5 segundos)
   - ✅ Color del badge se actualiza

#### 4.5 Completar Ticket
1. **Cambiar estado a COMPLETADO:**
   ```bash
   curl -X PUT "http://localhost:8081/api/admin/tickets/{id}/status?status=COMPLETADO"
   ```

2. **Verificar finalización:**
   - ✅ Ticket desaparece de "Tickets Activos"
   - ✅ "Tickets Activos" disminuye en 1
   - ✅ "Completados" incrementa en 1
   - ✅ "Tiempo Promedio" se actualiza

### Criterios de Éxito
- ✅ Flujo completo funciona sin interrupciones
- ✅ Estados se sincronizan entre frontend y backend
- ✅ Métricas se actualizan correctamente
- ✅ No hay errores en consola durante todo el proceso

---

## 📱 CASO DE PRUEBA 5: Responsive Design

### Objetivo
Verificar que la aplicación funciona correctamente en diferentes dispositivos.

### Pasos

#### 5.1 Desktop (1920x1080)
1. **Página principal:**
   - ✅ Botones centrados y bien espaciados
   - ✅ Texto legible y proporcional

2. **TÓTEM:**
   - ✅ Formulario centrado, máximo 2 columnas para tipos de cola
   - ✅ Campos de entrada con tamaño apropiado

3. **ADMIN:**
   - ✅ Dashboard en 2 columnas (Asesores | Tickets)
   - ✅ Métricas en 4 columnas horizontales
   - ✅ Tablas con scroll horizontal si es necesario

#### 5.2 Tablet (768x1024)
1. **Cambiar viewport del navegador** (F12 → Device Toolbar)
2. **Verificar adaptación:**
   - ✅ Botones se mantienen usables
   - ✅ Formularios se adaptan al ancho
   - ✅ Dashboard se reorganiza apropiadamente

#### 5.3 Mobile (375x667)
1. **Cambiar a viewport móvil**
2. **Página principal:**
   - ✅ Botones ocupan ancho completo
   - ✅ Texto se mantiene legible
   - ✅ Espaciado apropiado para touch

3. **TÓTEM:**
   - ✅ Formulario en una sola columna
   - ✅ Tipos de cola en 2x2 grid
   - ✅ Campos de entrada fáciles de tocar

4. **ADMIN:**
   - ✅ Métricas en 2x2 grid
   - ✅ Paneles se apilan verticalmente
   - ✅ Scroll vertical funciona correctamente

### Criterios de Éxito
- ✅ Interfaz usable en todos los tamaños de pantalla
- ✅ No hay elementos cortados o superpuestos
- ✅ Touch targets tienen tamaño mínimo 44px
- ✅ Scroll funciona apropiadamente

---

## 🚨 CASO DE PRUEBA 6: Manejo de Errores

### Objetivo
Verificar que la aplicación maneja errores de manera elegante.

### Pasos

#### 6.1 Error de Conectividad
1. **Detener el backend** (`Ctrl+C` en terminal del backend)
2. **Intentar crear ticket en TÓTEM:**
   - ✅ Se muestra mensaje de error apropiado
   - ✅ Botón vuelve a estado normal
   - ✅ Formulario mantiene datos ingresados

3. **Acceder a ADMIN:**
   - ✅ Se muestra "Error cargando dashboard"
   - ✅ No hay crash de la aplicación

#### 6.2 Respuestas de Error del Backend
1. **Reiniciar backend**
2. **Crear ticket duplicado:**
   - Crear ticket con RUT ya usado
   - ✅ Error 409 se maneja correctamente
   - ✅ Mensaje específico se muestra al usuario

#### 6.3 Validaciones de Frontend
1. **Datos inválidos:**
   - RUT malformado: ✅ Validación inmediata
   - Teléfono inválido: ✅ Validación inmediata
   - ✅ Botón submit permanece deshabilitado

### Criterios de Éxito
- ✅ Aplicación no se rompe con errores de red
- ✅ Mensajes de error son claros y útiles
- ✅ Usuario puede recuperarse de errores
- ✅ Validaciones previenen envío de datos inválidos

---

## 📊 REPORTE DE RESULTADOS

### Plantilla de Reporte

```
FECHA: ___________
NAVEGADOR: ___________
TESTER: ___________

CASO DE PRUEBA 1: Navegación Principal
□ PASÓ  □ FALLÓ  
Observaciones: ________________________________

CASO DE PRUEBA 2: Crear Ticket (TÓTEM)
□ PASÓ  □ FALLÓ  
Observaciones: ________________________________

CASO DE PRUEBA 3: Dashboard Admin
□ PASÓ  □ FALLÓ  
Observaciones: ________________________________

CASO DE PRUEBA 4: Flujo Integrado Completo
□ PASÓ  □ FALLÓ  
Observaciones: ________________________________

CASO DE PRUEBA 5: Responsive Design
□ PASÓ  □ FALLÓ  
Observaciones: ________________________________

CASO DE PRUEBA 6: Manejo de Errores
□ PASÓ  □ FALLÓ  
Observaciones: ________________________________

RESUMEN GENERAL:
□ TODAS LAS PRUEBAS PASARON
□ ALGUNAS PRUEBAS FALLARON (especificar)
□ PRUEBAS NO COMPLETADAS

BUGS ENCONTRADOS:
1. ________________________________
2. ________________________________
3. ________________________________

RECOMENDACIONES:
1. ________________________________
2. ________________________________
3. ________________________________
```

---

## 🎯 Criterios de Aceptación Final

### ✅ Funcionalidad Core
- [ ] Creación de tickets funciona correctamente
- [ ] Dashboard muestra información precisa
- [ ] Navegación entre pantallas es fluida
- [ ] Validaciones previenen datos incorrectos

### ✅ Experiencia de Usuario
- [ ] Interfaz es intuitiva y fácil de usar
- [ ] Mensajes de error son claros
- [ ] Loading states proporcionan feedback
- [ ] Responsive design funciona en todos los dispositivos

### ✅ Integración
- [ ] Frontend se comunica correctamente con backend
- [ ] Estados se sincronizan en tiempo real
- [ ] Auto-refresh funciona sin problemas
- [ ] Manejo de errores es robusto

### ✅ Performance
- [ ] Páginas cargan en menos de 2 segundos
- [ ] No hay memory leaks evidentes
- [ ] Actualizaciones son fluidas
- [ ] Bundle size es apropiado

---

**🎉 Si todos los criterios se cumplen, el frontend está listo para producción.**

---

**Versión:** 1.0  
**Fecha:** Diciembre 2025  
**Preparado por:** Equipo QA Frontend