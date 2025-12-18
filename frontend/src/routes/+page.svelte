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