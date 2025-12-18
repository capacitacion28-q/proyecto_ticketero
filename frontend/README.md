# Sistema Ticketero - Frontend

## Requisitos Previos

### Instalar Node.js

**Opción 1: Descarga Manual (Recomendado)**
1. Descargar Node.js LTS desde: https://nodejs.org/
2. Ejecutar el instalador
3. Verificar instalación: `node --version` y `npm --version`

**Opción 2: Chocolatey (Requiere permisos de administrador)**
```bash
# Abrir PowerShell como Administrador
choco install nodejs -y
```

## Instalación del Proyecto

```bash
# 1. Instalar dependencias
npm install

# 2. Verificar configuración
npm run check

# 3. Iniciar servidor de desarrollo
npm run dev

# 4. Compilar para producción
npm run build
```

## Scripts Disponibles

- `npm run dev` - Servidor de desarrollo (http://localhost:5173)
- `npm run build` - Compilar para producción
- `npm run preview` - Preview de build de producción
- `npm run check` - Verificar tipos TypeScript
- `npm run lint` - Verificar código con ESLint
- `npm run format` - Formatear código con Prettier

## Estructura del Proyecto

```
frontend/
├── src/
│   ├── routes/          # Páginas (file-based routing)
│   ├── lib/
│   │   ├── components/  # Componentes Svelte
│   │   ├── stores/      # Estado global (Svelte stores)
│   │   ├── services/    # Lógica de negocio
│   │   ├── types/       # TypeScript types
│   │   └── utils/       # Utilidades
│   ├── app.html         # Template HTML
│   └── app.css          # Estilos globales
├── static/              # Assets estáticos
└── tests/               # Tests
```

## Configuración

- **SvelteKit**: Framework principal
- **TypeScript**: Type safety
- **Tailwind CSS**: Styling
- **Vite**: Build tool
- **Vitest**: Testing

## Próximos Pasos

Una vez instalado Node.js, ejecutar:
```bash
npm install
npm run dev
```
