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
    phoneNumber: '',
    branchOffice: BRANCH_OFFICES[0],
    queueType: QueueType.CAJA
  };
  
  // Form validation
  let errors = {
    nationalId: '',
    phoneNumber: ''
  };
  
  let showSuccessModal = false;
  
  // Reactive validation
  $: {
    errors.nationalId = formData.nationalId && !validateRUT(formData.nationalId) 
      ? 'Formato de RUT inválido (ej: 12345678-9)' 
      : '';
    
    errors.phoneNumber = formData.phoneNumber && !validatePhone(formData.phoneNumber)
      ? 'Formato de teléfono inválido'
      : '';
  }
  
  $: isFormValid = formData.nationalId && 
                   formData.phoneNumber &&
                   formData.branchOffice && 
                   !errors.nationalId && 
                   !errors.phoneNumber;
  
  async function handleSubmit() {
    if (!isFormValid) return;
    
    try {
      await ticketActions.createTicket(formData);
      showSuccessModal = true;
      uiActions.showNotification('success', 'Ticket creado exitosamente');
      
      // Reset form
      formData = {
        nationalId: '',
        phoneNumber: '',
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
        label="Teléfono"
        type="tel"
        placeholder="+56912345678"
        bind:value={formData.phoneNumber}
        error={errors.phoneNumber}
        required
      />
      
      <!-- Sucursal -->
      <div class="space-y-1">
        <label for="branchOffice" class="block text-sm font-medium text-gray-700">
          Sucursal <span class="text-red-500">*</span>
        </label>
        <select
          id="branchOffice"
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
      <fieldset class="space-y-1">
        <legend class="block text-sm font-medium text-gray-700">
          Tipo de Atención <span class="text-red-500">*</span>
        </legend>
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
      </fieldset>
      
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