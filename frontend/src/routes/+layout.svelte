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