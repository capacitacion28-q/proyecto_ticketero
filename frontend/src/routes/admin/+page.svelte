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
  import { websocketActions } from '$lib/stores/websocketStore';
  import { REFRESH_INTERVAL } from '$lib/utils/constants';
  import { getStatusColor, formatWaitTime, formatDateTime } from '$lib/utils/helpers';
  
  let refreshInterval: NodeJS.Timeout;
  
  onMount(async () => {
    // Load initial data
    await loadData();
    
    // Setup auto-refresh
    refreshInterval = setInterval(loadData, REFRESH_INTERVAL);
    
    // Connect WebSocket
    websocketActions.connect();
  });
  
  onDestroy(() => {
    if (refreshInterval) {
      clearInterval(refreshInterval);
    }
    websocketActions.disconnect();
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