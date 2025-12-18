# FASE 5: Páginas Principales

## PASO 5.1: Crear página TÓTEM (/totem) con componentes

**Tareas:**
- Crear layout principal
- Crear página home
- Crear página TÓTEM con formulario

**Implementación:**

Archivo: src/routes/+layout.svelte
```svelte
<script lang="ts">
  import '../app.css';
  import Navigation from '$lib/components/layout/Navigation.svelte';
  import { notifications } from '$lib/stores/uiStore';
</script>

<div class="min-h-screen bg-gray-50">
  <Navigation />
  
  <main class="flex-1">
    <slot />
  </main>
  
  <!-- Notifications -->
  {#if $notifications.length > 0}
    <div class="fixed top-4 right-4 z-50 space-y-2">
      {#each $notifications as notification}
        <div class="max-w-sm w-full bg-white shadow-lg rounded-lg pointer-events-auto ring-1 ring-black ring-opacity-5 overflow-hidden">
          <div class="p-4">
            <div class="flex items-start">
              <div class="flex-shrink-0">
                {#if notification.type === 'success'}
                  <svg class="h-6 w-6 text-green-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                {:else if notification.type === 'error'}
                  <svg class="h-6 w-6 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                {:else}
                  <svg class="h-6 w-6 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                {/if}
              </div>
              <div class="ml-3 w-0 flex-1 pt-0.5">
                <p class="text-sm font-medium text-gray-900">{notification.message}</p>
              </div>
            </div>
          </div>
        </div>
      {/each}
    </div>
  {/if}
</div>
```

Archivo: src/routes/+page.svelte
```svelte
<script lang="ts">
  import { goto } from '$app/navigation';
  import Button from '$lib/components/shared/Button.svelte';
  
  function goToTotem() {
    goto('/totem');
  }
  
  function goToAdmin() {
    goto('/admin');
  }
</script>

<svelte:head>
  <title>Sistema Ticketero - Inicio</title>
</svelte:head>

<div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 to-primary-100">
  <div class="max-w-md w-full space-y-8 p-8">
    <div class="text-center">
      <h1 class="text-4xl font-bold text-gray-900 mb-2">Sistema Ticketero</h1>
      <p class="text-lg text-gray-600">Gestión de tickets con notificaciones en tiempo real</p>
    </div>
    
    <div class="space-y-4">
      <Button
        variant="primary"
        size="lg"
        fullWidth
        on:click={goToTotem}
      >
        <span class="text-2xl mr-3">🎫</span>
        TÓTEM - Crear Ticket
      </Button>
      
      <Button
        variant="secondary"
        size="lg"
        fullWidth
        on:click={goToAdmin}
      >
        <span class="text-2xl mr-3">📊</span>
        ADMIN - Dashboard
      </Button>
    </div>
    
    <div class="text-center text-sm text-gray-500">
      <p>Selecciona una opción para continuar</p>
    </div>
  </div>
</div>
```

Archivo: src/routes/totem/+page.svelte
```svelte
<script lang="ts">
  import Header from '$lib/components/layout/Header.svelte';
  import Button from '$lib/components/shared/Button.svelte';
  import Input from '$lib/components/shared/Input.svelte';
  import ErrorMessage from '$lib/components/shared/ErrorMessage.svelte';
  import Modal from '$lib/components/shared/Modal.svelte';
  
  import { ticketActions, ticketLoading, ticketError, currentTicket } from '$lib/stores/ticketStore';
  import { uiActions } from '$lib/stores/uiStore';
  import { QueueType } from '$lib/types';
  import { QUEUE_CONFIG, BRANCH_OFFICES } from '$lib/utils/constants';
  import { validateRUT, validatePhone, getQueueDisplayName, formatWaitTime } from '$lib/utils/helpers';
  
  // Form data
  let formData = {
    nationalId: '',
    telefono: '',
    branchOffice: BRANCH_OFFICES[0],
    queueType: QueueType.CAJA
  };
  
  // Form validation
  let errors = {
    nationalId: '',
    telefono: ''
  };
  
  let showSuccessModal = false;
  
  // Reactive validation
  $: {
    errors.nationalId = formData.nationalId && !validateRUT(formData.nationalId) 
      ? 'Formato de RUT inválido (ej: 12345678-9)' 
      : '';
    
    errors.telefono = formData.telefono && !validatePhone(formData.telefono)
      ? 'Formato de teléfono inválido'
      : '';
  }
  
  $: isFormValid = formData.nationalId && 
                   formData.branchOffice && 
                   !errors.nationalId && 
                   !errors.telefono;
  
  async function handleSubmit() {
    if (!isFormValid) return;
    
    try {
      await ticketActions.createTicket(formData);
      showSuccessModal = true;
      uiActions.showNotification('success', 'Ticket creado exitosamente');
      
      // Reset form
      formData = {
        nationalId: '',
        telefono: '',
        branchOffice: BRANCH_OFFICES[0],
        queueType: QueueType.CAJA
      };
    } catch (error) {
      uiActions.showNotification('error', 'Error al crear ticket');
    }
  }
  
  function closeSuccessModal() {
    showSuccessModal = false;
    ticketActions.clearCurrentTicket();
  }
</script>

<svelte:head>
  <title>TÓTEM - Crear Ticket</title>
</svelte:head>

<Header title="TÓTEM - Crear Ticket" showBackButton />

<div class="max-w-2xl mx-auto p-6">
  <div class="bg-white rounded-lg shadow-md p-8">
    <div class="text-center mb-8">
      <h2 class="text-2xl font-bold text-gray-900 mb-2">Crear Nuevo Ticket</h2>
      <p class="text-gray-600">Complete los datos para generar su ticket de atención</p>
    </div>
    
    {#if $ticketError.hasError}
      <div class="mb-6">
        <ErrorMessage message={$ticketError.message || 'Error desconocido'} />
      </div>
    {/if}
    
    <form on:submit|preventDefault={handleSubmit} class="space-y-6">
      <!-- RUT/ID -->
      <Input
        label="RUT o Cédula de Identidad"
        placeholder="12345678-9"
        bind:value={formData.nationalId}
        error={errors.nationalId}
        required
      />
      
      <!-- Teléfono -->
      <Input
        label="Teléfono (opcional)"
        type="tel"
        placeholder="+56912345678"
        bind:value={formData.telefono}
        error={errors.telefono}
      />
      
      <!-- Sucursal -->
      <div class="space-y-1">
        <label class="block text-sm font-medium text-gray-700">
          Sucursal <span class="text-red-500">*</span>
        </label>
        <select
          bind:value={formData.branchOffice}
          class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
          required
        >
          {#each BRANCH_OFFICES as office}
            <option value={office}>{office}</option>
          {/each}
        </select>
      </div>
      
      <!-- Tipo de Cola -->
      <div class="space-y-1">
        <label class="block text-sm font-medium text-gray-700">
          Tipo de Atención <span class="text-red-500">*</span>
        </label>
        <div class="grid grid-cols-2 gap-3">
          {#each Object.values(QueueType) as queueType}
            <label class="relative flex cursor-pointer rounded-lg border p-4 focus:outline-none {formData.queueType === queueType ? 'border-primary-600 ring-2 ring-primary-600' : 'border-gray-300'}">
              <input
                type="radio"
                bind:group={formData.queueType}
                value={queueType}
                class="sr-only"
              />
              <div class="flex flex-1 items-center">
                <div class="text-2xl mr-3">{QUEUE_CONFIG[queueType].icon}</div>
                <div class="flex flex-col">
                  <span class="block text-sm font-medium text-gray-900">
                    {getQueueDisplayName(queueType)}
                  </span>
                  <span class="block text-xs text-gray-500">
                    ~{QUEUE_CONFIG[queueType].avgTime} min
                  </span>
                </div>
              </div>
            </label>
          {/each}
        </div>
      </div>
      
      <!-- Submit Button -->
      <div class="pt-4">
        <Button
          type="submit"
          variant="primary"
          size="lg"
          fullWidth
          disabled={!isFormValid}
          loading={$ticketLoading.isLoading}
        >
          {#if $ticketLoading.isLoading}
            Creando Ticket...
          {:else}
            Crear Ticket
          {/if}
        </Button>
      </div>
    </form>
  </div>
</div>

<!-- Success Modal -->
<Modal bind:isOpen={showSuccessModal} title="¡Ticket Creado!" size="md">
  {#if $currentTicket}
    <div class="text-center space-y-4">
      <div class="text-6xl">{QUEUE_CONFIG[$currentTicket.queueType].icon}</div>
      
      <div>
        <h3 class="text-2xl font-bold text-gray-900">Ticket #{$currentTicket.numero}</h3>
        <p class="text-gray-600">{getQueueDisplayName($currentTicket.queueType)}</p>
      </div>
      
      <div class="bg-gray-50 rounded-lg p-4 space-y-2">
        <div class="flex justify-between">
          <span class="text-gray-600">Posición en cola:</span>
          <span class="font-semibold">#{$currentTicket.positionInQueue}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-gray-600">Tiempo estimado:</span>
          <span class="font-semibold">{formatWaitTime($currentTicket.estimatedWaitMinutes)}</span>
        </div>
      </div>
      
      <p class="text-sm text-gray-500">
        Recibirás notificaciones por Telegram cuando sea tu turno
      </p>
    </div>
  {/if}
  
  <svelte:fragment slot="footer">
    <Button variant="primary" fullWidth on:click={closeSuccessModal}>
      Entendido
    </Button>
  </svelte:fragment>
</Modal>
```

**Validaciones:**
```bash
# 1. Verificar TypeScript
npm run check

# 2. Verificar que páginas compilan
npm run build

# 3. Verificar dev server
npm run dev
```

---

## PASO 5.2: Crear página ADMIN (/admin) con dashboard

**Tareas:**
- Crear página ADMIN
- Crear componentes de dashboard
- Integrar WebSocket para updates en tiempo real

**Implementación:**

Archivo: src/routes/admin/+page.svelte
```svelte
<script lang="ts">
  import { onMount, onDestroy } from 'svelte';
  import Header from '$lib/components/layout/Header.svelte';
  import Loading from '$lib/components/shared/Loading.svelte';
  import ErrorMessage from '$lib/components/shared/ErrorMessage.svelte';
  import Button from '$lib/components/shared/Button.svelte';
  
  import { 
    dashboardActions, 
    dashboardMetrics, 
    advisors, 
    dashboardLoading, 
    dashboardError,
    availableAdvisors,
    busyAdvisors,
    offlineAdvisors
  } from '$lib/stores/dashboardStore';
  import { ticketActions, activeTickets } from '$lib/stores/ticketStore';
  import { websocketService } from '$lib/services/websocketService';
  import { REFRESH_INTERVAL } from '$lib/utils/constants';
  import { getStatusColor, formatWaitTime, formatDateTime } from '$lib/utils/helpers';
  
  let refreshInterval: NodeJS.Timeout;
  
  onMount(async () => {
    // Load initial data
    await loadData();
    
    // Setup auto-refresh
    refreshInterval = setInterval(loadData, REFRESH_INTERVAL);
    
    // Connect WebSocket
    websocketService.connect();
    websocketService.on('DASHBOARD_UPDATE', handleDashboardUpdate);
    websocketService.on('TICKET_UPDATED', handleTicketUpdate);
  });
  
  onDestroy(() => {
    if (refreshInterval) {
      clearInterval(refreshInterval);
    }
    websocketService.disconnect();
  });
  
  async function loadData() {
    try {
      await Promise.all([
        dashboardActions.loadDashboardData(),
        ticketActions.loadActiveTickets()
      ]);
    } catch (error) {
      console.error('Error loading dashboard data:', error);
    }
  }
  
  function handleDashboardUpdate(data: any) {
    // Handle real-time dashboard updates
    loadData();
  }
  
  function handleTicketUpdate(data: any) {
    // Handle real-time ticket updates
    ticketActions.loadActiveTickets();
  }
  
  async function updateAdvisorStatus(advisorId: number, newStatus: string) {
    try {
      await dashboardActions.updateAdvisorStatus(advisorId, newStatus);
    } catch (error) {
      console.error('Error updating advisor status:', error);
    }
  }
</script>

<svelte:head>
  <title>ADMIN - Dashboard</title>
</svelte:head>

<Header title="ADMIN - Dashboard" showBackButton />

<div class="max-w-7xl mx-auto p-6">
  {#if $dashboardLoading.isLoading}
    <Loading size="lg" message="Cargando dashboard..." />
  {:else if $dashboardError.hasError}
    <ErrorMessage message={$dashboardError.message || 'Error cargando dashboard'} />
  {:else}
    <!-- Metrics Cards -->
    {#if $dashboardMetrics}
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="text-3xl mr-4">🎫</div>
            <div>
              <p class="text-sm font-medium text-gray-600">Total Tickets</p>
              <p class="text-2xl font-bold text-gray-900">{$dashboardMetrics.totalTickets}</p>
            </div>
          </div>
        </div>
        
        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="text-3xl mr-4">⏳</div>
            <div>
              <p class="text-sm font-medium text-gray-600">Tickets Activos</p>
              <p class="text-2xl font-bold text-yellow-600">{$dashboardMetrics.activeTickets}</p>
            </div>
          </div>
        </div>
        
        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="text-3xl mr-4">✅</div>
            <div>
              <p class="text-sm font-medium text-gray-600">Completados</p>
              <p class="text-2xl font-bold text-green-600">{$dashboardMetrics.completedTickets}</p>
            </div>
          </div>
        </div>
        
        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="text-3xl mr-4">⏱️</div>
            <div>
              <p class="text-sm font-medium text-gray-600">Tiempo Promedio</p>
              <p class="text-2xl font-bold text-blue-600">{formatWaitTime($dashboardMetrics.averageWaitTime)}</p>
            </div>
          </div>
        </div>
      </div>
    {/if}
    
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
      <!-- Advisors Panel -->
      <div class="bg-white rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-medium text-gray-900">Asesores</h3>
        </div>
        <div class="p-6">
          <div class="space-y-4">
            {#each $advisors as advisor}
              <div class="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
                <div class="flex items-center">
                  <div class="text-2xl mr-3">
                    {#if advisor.status === 'AVAILABLE'}
                      ✅
                    {:else if advisor.status === 'BUSY'}
                      🔴
                    {:else}
                      ⚫
                    {/if}
                  </div>
                  <div>
                    <p class="font-medium text-gray-900">{advisor.name}</p>
                    <p class="text-sm text-gray-500">Módulo {advisor.moduleNumber}</p>
                    <p class="text-xs text-gray-400">Tickets asignados: {advisor.assignedTicketsCount}</p>
                  </div>
                </div>
                
                <div class="flex space-x-2">
                  <Button
                    size="sm"
                    variant={advisor.status === 'AVAILABLE' ? 'primary' : 'secondary'}
                    disabled={advisor.status === 'AVAILABLE'}
                    on:click={() => updateAdvisorStatus(advisor.id, 'AVAILABLE')}
                  >
                    Disponible
                  </Button>
                  <Button
                    size="sm"
                    variant={advisor.status === 'OFFLINE' ? 'danger' : 'secondary'}
                    disabled={advisor.status === 'OFFLINE'}
                    on:click={() => updateAdvisorStatus(advisor.id, 'OFFLINE')}
                  >
                    Offline
                  </Button>
                </div>
              </div>
            {/each}
          </div>
        </div>
      </div>
      
      <!-- Active Tickets Panel -->
      <div class="bg-white rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-medium text-gray-900">Tickets Activos</h3>
        </div>
        <div class="p-6">
          <div class="space-y-3 max-h-96 overflow-y-auto">
            {#each $activeTickets as ticket}
              <div class="flex items-center justify-between p-3 border border-gray-200 rounded-lg">
                <div class="flex items-center">
                  <div class="text-lg mr-3">{ticket.numero}</div>
                  <div>
                    <p class="font-medium text-gray-900">{ticket.nationalId}</p>
                    <p class="text-sm text-gray-500">Posición: #{ticket.positionInQueue}</p>
                  </div>
                </div>
                
                <div class="text-right">
                  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getStatusColor(ticket.status)}">
                    {ticket.status}
                  </span>
                  <p class="text-xs text-gray-500 mt-1">
                    {formatDateTime(ticket.createdAt)}
                  </p>
                </div>
              </div>
            {/each}
            
            {#if $activeTickets.length === 0}
              <div class="text-center py-8 text-gray-500">
                <div class="text-4xl mb-2">🎉</div>
                <p>No hay tickets activos</p>
              </div>
            {/if}
          </div>
        </div>
      </div>
    </div>
  {/if}
</div>
```

**Validaciones:**
```bash
# 1. Verificar TypeScript
npm run check

# 2. Verificar que página admin compila
npm run build

# 3. Verificar dev server
npm run dev

# 4. Verificar rutas funcionan
# - http://localhost:5173/ (Home)
# - http://localhost:5173/totem (TÓTEM)
# - http://localhost:5173/admin (ADMIN)
```

🔍 **PUNTO DE REVISIÓN 5.2:**
Después de completar el PASO 5.2, DETENTE y solicita revisión exhaustiva antes de continuar con los criterios finales.