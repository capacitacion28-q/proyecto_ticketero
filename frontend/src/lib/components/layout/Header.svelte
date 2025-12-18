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