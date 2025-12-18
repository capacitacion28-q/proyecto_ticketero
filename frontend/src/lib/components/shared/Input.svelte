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
  const inputId = `input-${Math.random().toString(36).substr(2, 9)}`;

  export function focus() {
    inputElement?.focus();
  }

  function handleInput(event: Event) {
    const target = event.target as HTMLInputElement;
    value = target.value;
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
    <label for={inputId} class="block text-sm font-medium text-gray-700">
      {label}
      {#if required}
        <span class="text-red-500">*</span>
      {/if}
    </label>
  {/if}
  
  <input
    bind:this={inputElement}
    id={inputId}
    {value}
    type={type}
    {placeholder}
    {required}
    {disabled}
    {maxlength}
    class={inputClasses}
    on:input={handleInput}
    on:change
    on:focus
    on:blur
  />
  
  {#if hasError}
    <p class="text-sm text-red-600">{error}</p>
  {/if}
</div>