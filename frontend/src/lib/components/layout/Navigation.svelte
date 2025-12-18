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