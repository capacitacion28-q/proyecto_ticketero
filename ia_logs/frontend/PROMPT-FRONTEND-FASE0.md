# FASE 0: Setup del Proyecto Frontend

## PASO 0.1: Crear Estructura Base de SvelteKit

**Tareas:**
- Crear package.json con todas las dependencias
- Verificar que npm instala dependencias correctamente
- Compilar proyecto vacío

**Implementación:**
Crear el archivo package.json en la raíz del proyecto:

```json
{
  "name": "ticketero-frontend",
  "version": "1.0.0",
  "description": "Sistema de Gestión de Tickets - Frontend",
  "type": "module",
  "scripts": {
    "dev": "vite dev",
    "build": "vite build",
    "preview": "vite preview",
    "check": "svelte-kit sync && svelte-check --tsconfig ./tsconfig.json",
    "check:watch": "svelte-kit sync && svelte-check --tsconfig ./tsconfig.json --watch",
    "lint": "prettier --plugin-search-dir . --check . && eslint .",
    "format": "prettier --plugin-search-dir . --write ."
  },
  "devDependencies": {
    "@sveltejs/adapter-auto": "^2.0.0",
    "@sveltejs/kit": "^1.20.4",
    "@typescript-eslint/eslint-plugin": "^6.0.0",
    "@typescript-eslint/parser": "^6.0.0",
    "autoprefixer": "^10.4.14",
    "eslint": "^8.28.0",
    "eslint-config-prettier": "^8.5.0",
    "eslint-plugin-svelte": "^2.30.0",
    "postcss": "^8.4.24",
    "prettier": "^2.8.0",
    "prettier-plugin-svelte": "^2.10.1",
    "svelte": "^4.0.5",
    "svelte-check": "^3.4.3",
    "tailwindcss": "^3.3.0",
    "tslib": "^2.4.1",
    "typescript": "^5.0.0",
    "vite": "^4.4.2",
    "@vitest/ui": "^0.34.0",
    "vitest": "^0.34.0"
  },
  "dependencies": {
    "axios": "^1.5.0",
    "date-fns": "^2.30.0"
  }
}
```

**Validaciones:**
```bash
# 1. Verificar que npm puede leer el package.json
npm install

# 2. Verificar estructura básica
npm run check

# 3. Compilar (debe fallar porque no hay código aún, pero sin errores de dependencias)
npm run build
```

---

## PASO 0.2: Configuración de TypeScript y Tailwind

**Tareas:**
- Crear svelte.config.js
- Crear tsconfig.json
- Crear tailwind.config.js
- Crear vite.config.js

**Implementación:**

Archivo 1: svelte.config.js
```javascript
import adapter from '@sveltejs/adapter-auto';
import { vitePreprocess } from '@sveltejs/kit/vite';

/** @type {import('@sveltejs/kit').Config} */
const config = {
	preprocess: vitePreprocess(),
	kit: {
		adapter: adapter()
	}
};

export default config;
```

Archivo 2: tsconfig.json
```json
{
	"extends": "./.svelte-kit/tsconfig.json",
	"compilerOptions": {
		"allowJs": true,
		"checkJs": true,
		"esModuleInterop": true,
		"forceConsistentCasingInFileNames": true,
		"resolveJsonModule": true,
		"skipLibCheck": true,
		"sourceMap": true,
		"strict": true
	}
}
```

Archivo 3: tailwind.config.js
```javascript
/** @type {import('tailwindcss').Config} */
export default {
  content: ['./src/**/*.{html,js,svelte,ts}'],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#eff6ff',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8'
        }
      }
    },
  },
  plugins: [],
}
```

Archivo 4: vite.config.js
```javascript
import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

export default defineConfig({
	plugins: [sveltekit()],
	test: {
		include: ['src/**/*.{test,spec}.{js,ts}']
	}
});
```

**Validaciones:**
```bash
# 1. Instalar dependencias
npm install

# 2. Verificar TypeScript
npm run check

# 3. Verificar que Tailwind está configurado
npm run dev
```

---

## PASO 0.3: Configuración de Build y Deploy

**Tareas:**
- Crear estructura de carpetas src/
- Crear app.html
- Crear archivos de configuración adicionales

**Implementación:**

Archivo 1: src/app.html
```html
<!DOCTYPE html>
<html lang="es">
	<head>
		<meta charset="utf-8" />
		<link rel="icon" href="%sveltekit.assets%/favicon.png" />
		<meta name="viewport" content="width=device-width" />
		<title>Sistema Ticketero</title>
		%sveltekit.head%
	</head>
	<body data-sveltekit-preload-data="hover" class="bg-gray-50">
		<div style="display: contents">%sveltekit.body%</div>
	</body>
</html>
```

Archivo 2: src/app.css
```css
@tailwind base;
@tailwind components;
@tailwind utilities;

@layer base {
  html {
    font-family: system-ui, sans-serif;
  }
}

@layer components {
  .btn-primary {
    @apply bg-primary-600 hover:bg-primary-700 text-white font-medium py-2 px-4 rounded-lg transition-colors;
  }
  
  .btn-secondary {
    @apply bg-gray-200 hover:bg-gray-300 text-gray-800 font-medium py-2 px-4 rounded-lg transition-colors;
  }
}
```

**Validaciones:**
```bash
# 1. Crear estructura de carpetas
mkdir -p src/routes src/lib/components src/lib/stores src/lib/services src/lib/types src/lib/utils

# 2. Verificar build
npm run build

# 3. Verificar dev server
npm run dev
```

🔍 **PUNTO DE REVISIÓN 0.3:**
Después de completar el PASO 0.3, DETENTE y solicita revisión exhaustiva antes de continuar con FASE 1.