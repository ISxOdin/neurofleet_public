<script>
  import { createEventDispatcher } from "svelte";

  const dispatch = createEventDispatcher();
  let message = "";

  function handleSubmit() {
    if (message.trim()) {
      dispatch("send", message.trim());
      message = "";
    }
  }

  function handleKeydown(event) {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      handleSubmit();
    }
  }
</script>

<div class="chat-input">
  <textarea
    bind:value={message}
    onkeydown={handleKeydown}
    placeholder="Write a message..."
    rows="1"
    class="input-field"
  ></textarea>
  <button
    class="send-button"
    onclick={handleSubmit}
    disabled={!message.trim()}
    aria-label="Send message"
  >
    <i class="bi bi-send-fill"></i>
  </button>
</div>

<style>
  .chat-input {
    padding: 1rem;
    background: rgba(255, 255, 255, 0.02);
    border-top: 1px solid var(--accent-color);
    display: flex;
    gap: 0.75rem;
    align-items: flex-end;
  }

  .input-field {
    flex: 1;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid var(--accent-color);
    border-radius: 1rem;
    padding: 0.75rem 1rem;
    color: white;
    font-size: 0.95rem;
    resize: none;
    min-height: 24px;
    max-height: 120px;
    transition: all 0.2s ease;
  }

  .input-field:focus {
    outline: none;
    background: rgba(255, 255, 255, 0.08);
    border-color: rgba(149, 212, 238, 0.8);
  }

  .input-field::placeholder {
    color: rgba(255, 255, 255, 0.5);
  }

  .send-button {
    background: var(--accent-color);
    border: none;
    border-radius: 50%;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s ease;
    flex-shrink: 0;
  }

  .send-button:not(:disabled):hover {
    transform: scale(1.05);
    background: rgba(149, 212, 238, 0.9);
  }

  .send-button:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  .send-button i {
    color: var(--base-color);
    font-size: 1.1rem;
  }
</style>
