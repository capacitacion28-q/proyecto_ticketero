# FASE 4: Componentes Compartidos

## PASO 4.1: Crear componentes base (Button, Input, Loading, etc.)

**Tareas:**
- Crear Button.svelte
- Crear Input.svelte
- Crear Loading.svelte
- Crear ErrorMessage.svelte

**Implementación:**

Archivo: src/lib/components/shared/Button.svelte
```svelte
<script lang="ts">
  export let variant: 'primary' | 'secondary' | 'danger' = 'primary';
  export let size: 'sm' | 'md' | 'lg' = 'md';
  export let disabled = false;
  export let loading = false;
  export let type: 'button' | 'submit' | 'reset' = 'button';
  export let fullWidth = false;

  $: classes = [
    'inline-flex items-center justify-center font-medium rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2',
    // Variants
    variant === 'primary' && 'bg-primary-600 hover:bg-primary-700 text-white focus:ring-primary-500',
    variant === 'secondary' && 'bg-gray-200 hover:bg-gray-300 text-gray-800 focus:ring-gray-500',
    variant === 'danger' && 'bg-red-600 hover:bg-red-700 text-white focus:ring-red-500',
    // Sizes
    size === 'sm' && 'px-3 py-1.5 text-sm',
    size === 'md' && 'px-4 py-2 text-base',
    size === 'lg' && 'px-6 py-3 text-lg',
    // States
    disabled && 'opacity-50 cursor-not-allowed',
    fullWidth && 'w-full'
  ].filter(Boolean).join(' ');
</script>

<button
  {type}
  class={classes}
  {disabled}
  on:click
  on:focus
  on:blur
>
  {#if loading}
    <svg class="animate-spin -ml-1 mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24">
      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
    </svg>
  {/if}
  <slot />
</button>
```

Archivo: src/lib/components/shared/Input.svelte
```svelte
<script lang="ts">
  export let label = '';
  export let placeholder = '';
  export let value = '';
  export let type: 'text' | 'email' | 'tel' | 'password' = 'text';
  export let required = false;
  export let disabled = false;
  export let error = '';
  export let maxlength: number | undefined = undefined;

  let inputElement: HTMLInputElement;

  export function focus() {
    inputElement?.focus();
  }

  $: hasError = !!error;
  $: inputClasses = [
    'block w-full px-3 py-2 border rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-1 sm:text-sm',
    hasError 
      ? 'border-red-300 text-red-900 focus:ring-red-500 focus:border-red-500' 
      : 'border-gray-300 focus:ring-primary-500 focus:border-primary-500',
    disabled && 'bg-gray-50 cursor-not-allowed'
  ].filter(Boolean).join(' ');
</script>

<div class="space-y-1">
  {#if label}
    <label class="block text-sm font-medium text-gray-700">
      {label}
      {#if required}
        <span class="text-red-500">*</span>
      {/if}
    </label>
  {/if}
  
  <input
    bind:this={inputElement}
    bind:value
    {type}
    {placeholder}
    {required}
    {disabled}
    {maxlength}
    class={inputClasses}
    on:input
    on:change
    on:focus
    on:blur
  />
  
  {#if hasError}
    <p class="text-sm text-red-600">{error}</p>
  {/if}
</div>
```

Archivo: src/lib/components/shared/Loading.svelte
```svelte
<script lang="ts">
  export let size: 'sm' | 'md' | 'lg' = 'md';
  export let message = '';
  export let fullScreen = false;

  $: spinnerSize = {
    sm: 'h-4 w-4',
    md: 'h-8 w-8',
    lg: 'h-12 w-12'
  }[size];
</script>

<div class="flex flex-col items-center justify-center {fullScreen ? 'fixed inset-0 bg-white bg-opacity-75 z-50' : 'p-4'}">
  <svg class="animate-spin {spinnerSize} text-primary-600" fill="none" viewBox="0 0 24 24">
    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
  </svg>
  
  {#if message}
    <p class="mt-2 text-sm text-gray-600">{message}</p>
  {/if}
</div>
```

Archivo: src/lib/components/shared/ErrorMessage.svelte
```svelte
<script lang="ts">
  export let message: string;
  export let title = 'Error';
  export let dismissible = false;

  function dismiss() {
    if (dismissible) {
      // Dispatch custom event for parent to handle
      const event = new CustomEvent('dismiss');
      document.dispatchEvent(event);
    }
  }
</script>

<div class="rounded-md bg-red-50 p-4 border border-red-200">
  <div class="flex">
    <div class="flex-shrink-0">
      <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
      </svg>
    </div>
    <div class="ml-3 flex-1">
      <h3 class="text-sm font-medium text-red-800">{title}</h3>
      <p class="mt-1 text-sm text-red-700">{message}</p>
    </div>
    {#if dismissible}
      <div class="ml-auto pl-3">
        <button
          type="button"
          class="inline-flex rounded-md bg-red-50 p-1.5 text-red-500 hover:bg-red-100 focus:outline-none focus:ring-2 focus:ring-red-600 focus:ring-offset-2 focus:ring-offset-red-50"
          on:click={dismiss}
        >
          <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
            <path d="M6.28 5.22a.75.75 0 00-1.06 1.06L8.94 10l-3.72 3.72a.75.75 0 101.06 1.06L10 11.06l3.72 3.72a.75.75 0 101.06-1.06L11.06 10l3.72-3.72a.75.75 0 00-1.06-1.06L10 8.94 6.28 5.22z" />
          </svg>
        </button>
      </div>
    {/if}
  </div>
</div>
```

**Validaciones:**
```bash
# 1. Verificar TypeScript
npm run check

# 2. Verificar que componentes compilan
npm run build
```

---

## PASO 4.2: Crear componentes de layout (Header, Navigation)

**Tareas:**
- Crear Header.svelte
- Crear Navigation.svelte
- Crear Modal.svelte

**Implementación:**

Archivo: src/lib/components/layout/Header.svelte
```svelte
<script lang="ts">
  export let title: string;
  export let showBackButton = false;
  
  import { goto } from '$app/navigation';
  
  function goBack() {
    goto('/');
  }
</script>

<header class="bg-white shadow-sm border-b border-gray-200">
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
    <div class="flex justify-between items-center h-16">
      <div class="flex items-center">
        {#if showBackButton}
          <button
            on:click={goBack}
            class="mr-4 p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-primary-500"
          >
            <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
        {/if}
        
        <h1 class="text-xl font-semibold text-gray-900">{title}</h1>
      </div>
      
      <div class="flex items-center space-x-4">
        <div class="text-sm text-gray-500">
          Sistema Ticketero v1.0
        </div>
      </div>
    </div>
  </div>
</header>
```

Archivo: src/lib/components/layout/Navigation.svelte
```svelte
<script lang="ts">
  import { page } from '$app/stores';
  
  $: currentPath = $page.url.pathname;
  
  const navItems = [
    { path: '/', label: 'Inicio', icon: '🏠' },
    { path: '/totem', label: 'TÓTEM', icon: '🎫' },
    { path: '/admin', label: 'ADMIN', icon: '📊' }
  ];
  
  function isActive(path: string): boolean {
    if (path === '/') {
      return currentPath === '/';
    }
    return currentPath.startsWith(path);
  }
</script>

<nav class="bg-primary-600 shadow-lg">
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
    <div class="flex justify-between h-16">
      <div class="flex items-center space-x-8">
        <div class="flex-shrink-0 flex items-center">
          <span class="text-white text-lg font-bold">Ticketero</span>
        </div>
        
        <div class="hidden md:flex space-x-4">
          {#each navItems as item}
            <a
              href={item.path}
              class="px-3 py-2 rounded-md text-sm font-medium transition-colors {
                isActive(item.path)
                  ? 'bg-primary-700 text-white'
                  : 'text-primary-100 hover:bg-primary-500 hover:text-white'
              }"
            >
              <span class="mr-2">{item.icon}</span>
              {item.label}
            </a>
          {/each}
        </div>
      </div>
    </div>
  </div>
</nav>
```

Archivo: src/lib/components/shared/Modal.svelte
```svelte
<script lang="ts">
  export let isOpen = false;
  export let title = '';
  export let size: 'sm' | 'md' | 'lg' | 'xl' = 'md';
  export let closable = true;
  
  function closeModal() {
    if (closable) {
      isOpen = false;
    }
  }
  
  function handleKeydown(event: KeyboardEvent) {
    if (event.key === 'Escape' && closable) {
      closeModal();
    }
  }
  
  $: sizeClasses = {
    sm: 'max-w-md',
    md: 'max-w-lg',
    lg: 'max-w-2xl',
    xl: 'max-w-4xl'
  }[size];
</script>

<svelte:window on:keydown={handleKeydown} />

{#if isOpen}
  <!-- Backdrop -->
  <div
    class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity z-40"
    on:click={closeModal}
    on:keydown
  ></div>
  
  <!-- Modal -->
  <div class="fixed inset-0 z-50 overflow-y-auto">
    <div class="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
      <div class="relative transform overflow-hidden rounded-lg bg-white text-left shadow-xl transition-all sm:my-8 sm:w-full {sizeClasses}">
        <!-- Header -->
        {#if title || closable}
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-200">
            {#if title}
              <h3 class="text-lg font-medium text-gray-900">{title}</h3>
            {/if}
            
            {#if closable}
              <button
                type="button"
                class="rounded-md bg-white text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
                on:click={closeModal}
              >
                <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            {/if}
          </div>
        {/if}
        
        <!-- Content -->
        <div class="px-6 py-4">
          <slot />
        </div>
        
        <!-- Footer -->
        <div class="px-6 py-4 bg-gray-50 border-t border-gray-200">
          <slot name="footer" />
        </div>
      </div>
    </div>
  </div>
{/if}
```

**Validaciones:**
```bash
# 1. Verificar TypeScript
npm run check

# 2. Verificar que componentes compilan
npm run build
```

🔍 **PUNTO DE REVISIÓN 4.2:**
Después de completar el PASO 4.2, DETENTE y solicita revisión exhaustiva antes de continuar con FASE 5.