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
    role="button"
    tabindex="-1"
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