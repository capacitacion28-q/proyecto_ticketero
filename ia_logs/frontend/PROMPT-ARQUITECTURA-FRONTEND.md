# PROMPT ARQUITECTURA FRONTEND - Diseño de Alto Nivel del Sistema Ticketero

## Contexto
Eres un Arquitecto de Software Senior con 10+ años de experiencia en aplicaciones web frontend. Has recibido el documento de Requerimientos Funcionales aprobado y tu tarea es diseñar la arquitectura de alto nivel de la aplicación frontend del Sistema Ticketero.

## 🎯 OBJETIVO PRINCIPAL DEL FRONTEND

**El frontend debe ser una interfaz amigable y sencilla para probar los servicios del backend, con las siguientes características:**

✅ **Simplicidad Técnica:** 
- Arquitectura simple y directa
- Tecnologías estables y bien conocidas
- Mínima complejidad innecesaria

✅ **Intuitividad de Uso:**
- Interfaz clara y fácil de navegar
- Flujos de usuario simples
- Feedback visual inmediato

✅ **Dos Pantallas Principales:**
1. **Pantalla TÓTEM (Cliente):** Interfaz tipo kiosco donde los clientes pueden crear tickets de forma sencilla
2. **Pantalla ADMIN (Supervisor):** Dashboard completo para monitorear tickets, asesores, colas y métricas en tiempo real

✅ **Propósito:** 
- Demostrar y probar todos los servicios del backend
- Validar el flujo completo del sistema
- Interfaz funcional para presentaciones y testing

**IMPORTANTE:** Prioriza la simplicidad sobre la sofisticación. El objetivo es tener una herramienta funcional para probar el sistema, no una aplicación de producción compleja.

**IMPORTANTE:** Después de completar CADA paso, debes DETENERTE y solicitar una revisión exhaustiva antes de continuar con el siguiente paso.

## Documentos de Entrada
Lee estos archivos que YA están en tu proyecto:
- `docs\project-requirements.md` - Contexto de negocio
- `docs\requerimientos\REQUERIMIENTOS-FUNCIONALES-COMPLETO.md` - RF-001 a RF-008 con criterios de aceptación

## Metodología de Trabajo
**Principio Fundamental:**
"Diseñar → Validar → Confirmar → Continuar"

Después de CADA paso:
✅ Diseña el componente arquitectónico frontend
✅ Valida que es implementable/correcto
✅ Revisa alineación con requerimientos
⏸️ DETENTE y solicita revisión exhaustiva
✅ Espera confirmación antes de continuar

## Formato de Solicitud de Revisión:
```
✅ PASO X COMPLETADO

Componente diseñado:
- [Nombre del componente frontend]

Validaciones realizadas:
- [checklist de validaciones]

🔍 SOLICITO REVISIÓN EXHAUSTIVA:

Por favor, revisa:
1. ¿El diseño frontend es correcto y completo?
2. ¿Está alineado con los requerimientos?
3. ¿La justificación técnica es sólida?
4. ¿Hay algo que mejorar?
5. ¿Puedo continuar con el siguiente paso?

⏸️ ESPERANDO CONFIRMACIÓN PARA CONTINUAR...
```

## Tu Tarea
Crear un documento de Arquitectura Frontend profesional implementado en 6 pasos:

**PASO 1:** Stack Tecnológico Frontend con Justificaciones
**PASO 2:** Arquitectura de Componentes y Páginas
**PASO 3:** Flujos de Usuario (User Flows)
**PASO 4:** Arquitectura de Capas Frontend
**PASO 5:** Decisiones Arquitectónicas Frontend (ADRs)
**PASO 6:** Configuración de Build y Deploy

---

## PASO 1: Stack Tecnológico Frontend con Justificaciones

**Objetivo:** Seleccionar y justificar todas las tecnologías frontend del proyecto con análisis de alternativas.

**IMPORTANTE:** Tienes libertad completa para sugerir el stack tecnológico que consideres más apropiado para este proyecto.

**Tareas:**
- Seleccionar Framework Frontend (React, Vue, Angular, Svelte, etc.)
- Seleccionar Build Tool (Vite, Webpack, Parcel, etc.)
- Seleccionar Gestión de Estado (Redux, Zustand, Context API, Pinia, etc.)
- Seleccionar Styling Solution (CSS Modules, Styled Components, Tailwind, etc.)
- Seleccionar HTTP Client (Axios, Fetch API, SWR, React Query, etc.)
- Seleccionar Testing Framework (Jest, Vitest, Testing Library, etc.)
- Justificar cada selección con pros/contras vs alternativas
- **Considerar el contexto:** Interfaz simple para probar servicios, 2 pantallas (TÓTEM y ADMIN), priorizar simplicidad técnica

**Implementación:**
Crear tabla de decisiones tecnológicas con formato:

| Tecnología | Selección | Justificación | Alternativas Consideradas | Decisión |
|------------|-----------|---------------|---------------------------|----------|
| Framework | [Tu elección] | [Razones técnicas] | [Otras opciones] | ✅/❌ |

---

## PASO 2: Arquitectura de Componentes y Páginas

**Objetivo:** Crear diagrama de arquitectura frontend mostrando páginas, componentes y su organización.

**Tareas:**
- Crear diagrama de arquitectura de componentes (puede usar PlantUML, Mermaid o ASCII)
- Identificar las 2 pantallas principales:
  * **Pantalla TÓTEM:** Interfaz tipo kiosco para crear tickets (sencilla e intuitiva)
  * **Pantalla ADMIN:** Dashboard completo para monitorear todo el sistema
- Identificar componentes reutilizables (TicketForm, QueueStatus, etc.)
- Documentar jerarquía de componentes y comunicación entre ellos
- Guardar en archivo separado Y en documento
- Mapear componentes a requerimientos funcionales (RF-001 a RF-008)

**Implementación:**
Crear diagrama mostrando:
- Páginas principales (2 aplicaciones: Terminal Cliente, Dashboard Supervisor)
- Componentes reutilizables
- Comunicación entre componentes
- Estructura de carpetas

---

## PASO 3: Flujos de Usuario (User Flows)

**Objetivo:** Crear diagramas de flujos de usuario mostrando la navegación y experiencia end-to-end.

**Tareas:**
- Crear diagrama de flujo para **Pantalla TÓTEM** (interfaz tipo kiosco)
- Crear diagrama de flujo para **Pantalla ADMIN** (dashboard de monitoreo)
- Documentar 2 flujos principales **SIMPLES E INTUITIVOS:**
  * **Flujo TÓTEM:** Crear ticket → Ver confirmación → Consultar estado (máximo 3 pasos)
  * **Flujo ADMIN:** Dashboard → Monitorear datos → Gestionar sistema (vista única con todo visible)
- Incluir puntos de decisión, validaciones y casos de error
- Guardar en archivo separado Y en documento
- Mapear flujos a requerimientos funcionales

**Implementación:**
Crear diagramas de flujo (PlantUML, Mermaid o ASCII) mostrando:
- Navegación paso a paso
- Puntos de decisión
- Casos de error
- Validaciones de UI

---

## PASO 4: Arquitectura de Capas Frontend

**Objetivo:** Documentar la arquitectura en capas del frontend y los componentes principales.

**Tareas:**
- Crear diagrama ASCII de capas frontend
- Documentar responsabilidades por capa:
  * Capa de Presentación (Pages/Views)
  * Capa de Componentes (Reusable Components)
  * Capa de Lógica de Negocio (Services/Hooks)
  * Capa de Estado (Store/Context)
  * Capa de Datos (API Layer/HTTP Client)
- Documentar componentes principales (páginas, componentes reutilizables, servicios, hooks)
- Incluir ejemplos de código para cada componente
- Especificar dependencias entre componentes
- Mapear componentes a requerimientos funcionales

**Implementación:**
Crear diagrama de capas y documentar:
- Responsabilidades de cada capa
- Componentes principales por capa
- Ejemplos de código (interfaces, tipos)
- Flujo de datos entre capas

---

## PASO 5: Decisiones Arquitectónicas Frontend (ADRs)

**Objetivo:** Documentar las 5 decisiones arquitectónicas clave frontend con formato ADR.

**Tareas:**
- Crear ADR-001 (Framework Frontend elegido vs alternativas)
- Crear ADR-002 (Gestión de Estado elegida vs alternativas)
- Crear ADR-003 (Styling Approach elegido vs alternativas)
- Crear ADR-004 (Build Tool elegido vs alternativas)
- Crear ADR-005 (Testing Strategy elegida vs alternativas)
- Cada ADR con: Contexto, Decisión, Razones, Consecuencias, Futuro
- **Considerar el contexto:** Interfaz simple para probar servicios, 2 pantallas (TÓTEM y ADMIN), **priorizar simplicidad sobre sofisticación**

**Implementación:**
Formato ADR estándar:
```
## ADR-001: [Título de la Decisión]

**Contexto:** [Situación que requiere decisión]
**Decisión:** [Qué se decidió]
**Razones:** [Por qué se tomó esta decisión]
**Consecuencias:** [Pros y contras de la decisión]
**Futuro:** [Cuándo reevaluar]
```

---

## PASO 6: Configuración de Build y Deploy

**Objetivo:** Documentar configuración de build, deploy y integración con el backend existente.

**Tareas:**
- Configuración de Build Tool (Vite/Webpack config)
- Variables de entorno frontend (API_URL, etc.)
- Integración con Docker Compose existente (agregar servicio frontend)
- Configuración de Nginx para servir frontend
- Scripts de package.json
- Configuración de proxy para desarrollo
- Estructura del proyecto frontend
- Integración con servicios existentes (API backend, PostgreSQL)
- Configuración de CORS en backend para frontend
- Validación final de integración completa

**Implementación:**
Documentar:
- Configuración de build
- Variables de entorno
- Docker Compose actualizado
- Scripts de desarrollo y producción
- Configuración de proxy/CORS

---

## Criterios de Calidad

Tu documento DEBE cumplir:

**✅ Completitud:**
- [ ] 3 diagramas generados (Arquitectura de Componentes, User Flows, Capas Frontend)
- [ ] Stack tecnológico frontend completo (6+ tecnologías justificadas)
- [ ] Componentes principales documentados (páginas, componentes, servicios, hooks)
- [ ] 5 ADRs frontend con contexto/decisión/consecuencias
- [ ] Configuración completa (build config, env vars, docker integration)

**✅ Claridad:**
- [ ] Diagramas renderizables y comprensibles
- [ ] Justificaciones técnicas sólidas para decisiones frontend
- [ ] Pros/contras de alternativas consideradas

**✅ Profesionalismo:**
- [ ] Formato ADR estándar
- [ ] Arquitectura de componentes clara
- [ ] Responsabilidades claras por capa frontend

**✅ Alineación con Backend:**
- [ ] Componentes mapeados a endpoints del API
- [ ] Integración con Docker Compose existente
- [ ] Variables de entorno coordinadas con backend
- [ ] Flujos de usuario alineados con requerimientos funcionales (RF-001 a RF-008)

---

## Restricciones

**❌ NO incluir:**
- Implementación completa de componentes React/Vue/Angular
- Código CSS/styling detallado
- Configuración específica de bundlers (eso será en implementación)

**✅ SÍ incluir:**
- Nombres de componentes y páginas principales
- Estructura de carpetas frontend (src/components, src/pages, etc.)
- Decisiones técnicas justificadas
- Ejemplos de interfaces/tipos TypeScript
- Integración con API endpoints existentes

---

## Entregable

**Archivo:** `ARQUITECTURA-FRONTEND.md`
**Ubicación:** `docs/`
**Diagramas:** 
- `docs/diagrams/frontend/01-component-architecture.puml`
- `docs/diagrams/frontend/02-user-flows.puml` 
- `docs/diagrams/frontend/03-frontend-layers.puml`

**Longitud esperada:** 25-35 páginas (6,000-8,000 palabras)

Este documento será la entrada para:
- Implementación de componentes frontend
- Integración con backend existente
- Revisión técnica por equipo de desarrollo frontend

---

**IMPORTANTE:** Los diagramas deben ser renderizables. Prueba cada diagrama antes de finalizar el documento.

**Contexto del Sistema:**
- **2 pantallas principales:** TÓTEM (crear tickets) y ADMIN (monitoreo completo)
- **Objetivo:** Interfaz amigable para probar servicios del backend
- **Simplicidad:** Arquitectura técnica sencilla e intuitiva de usar
- Integración con API REST existente (Spring Boot)
- Debe integrarse con Docker Compose existente (API + PostgreSQL)
- 8 requerimientos funcionales a implementar (RF-001 a RF-008)
- **Prioridad:** Funcionalidad sobre complejidad técnica