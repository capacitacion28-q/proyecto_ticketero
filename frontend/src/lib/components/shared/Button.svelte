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