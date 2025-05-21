<script>
  import { fade, fly } from "svelte/transition";
  import ChatWindow from "./ChatWindow.svelte";
  import { isAuthenticated } from "../../../store";

  let isOpen = false;

  if (typeof window !== "undefined") {
    window.addEventListener("toggleChatbot", () => {
      isOpen = !isOpen;
    });
  }
</script>

{#if $isAuthenticated}
  <div
    class="chatbot-container"
    class:open={isOpen}
    transition:fly={{ y: 20, duration: 300 }}
  >
    <div class="chatbot-header">
      <h3>NeuroFleet Assistant</h3>
      <button
        class="close-button"
        on:click={() => (isOpen = false)}
        aria-label="Close chat"
      >
        <i class="bi bi-x-lg"></i>
      </button>
    </div>

    <ChatWindow />

    <div class="chatbot-footer" transition:fade>
      <p class="text-light small">Powered by NeuroFleet AI</p>
    </div>
  </div>
{/if}

<style>
  .chatbot-container {
    position: fixed;
    bottom: 5rem;
    right: 2rem;
    width: 350px;
    height: 500px;
    background: var(--base-color);
    border-radius: 1rem;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
    display: flex;
    flex-direction: column;
    z-index: 1000;
    border: 1px solid var(--accent-color);
    opacity: 0;
    pointer-events: none;
  }

  .chatbot-container.open {
    opacity: 1;
    pointer-events: all;
  }

  .chatbot-header {
    padding: 1rem;
    background: rgba(149, 212, 238, 0.1);
    border-bottom: 1px solid var(--accent-color);
    border-radius: 1rem 1rem 0 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .chatbot-header h3 {
    margin: 0;
    color: var(--accent-color);
    font-size: 1.1rem;
  }

  .close-button {
    background: none;
    border: none;
    color: var(--accent-color);
    cursor: pointer;
    padding: 0.25rem;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    transition: all 0.2s ease;
  }

  .close-button:hover {
    background: rgba(149, 212, 238, 0.1);
  }

  .chatbot-footer {
    padding: 0.75rem;
    text-align: center;
    border-top: 1px solid var(--accent-color);
    background: rgba(149, 212, 238, 0.05);
    border-radius: 0 0 1rem 1rem;
  }

  @media (max-width: 768px) {
    .chatbot-container {
      width: calc(100% - 2rem);
      height: calc(100% - 7rem);
      bottom: 5rem;
      right: 1rem;
    }
  }
</style>
