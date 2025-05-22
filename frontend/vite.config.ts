import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

export default defineConfig({
	plugins: [sveltekit()],
	build: {
		sourcemap: true, // Enables debugging the frontend code in the browser (with developer tools)
	},
	// Ensure environment variables are properly exposed to the client
	envPrefix: ['VITE_']
});
