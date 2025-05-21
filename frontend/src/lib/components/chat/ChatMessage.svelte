<script>
  export let message;

  function formatTime(date) {
    return new Date(date).toLocaleTimeString([], {
      hour: "2-digit",
      minute: "2-digit",
    });
  }
</script>

<div class="message {message.type}">
  {#if message.type === "bot"}
    <div class="avatar">
      <i class="bi bi-robot"></i>
    </div>
  {/if}

  <div class="content">
    <div class="text">{message.content}</div>
    <div class="timestamp">{formatTime(message.timestamp)}</div>
  </div>

  {#if message.type === "user"}
    <div class="avatar">
      <i class="bi bi-person-fill"></i>
    </div>
  {/if}
</div>

<style>
  .message {
    display: flex;
    align-items: flex-start;
    gap: 0.75rem;
    max-width: 80%;
  }

  .message.bot {
    flex-direction: row;
  }

  .message.user {
    flex-direction: row-reverse;
  }

  .avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: var(--accent-color);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .avatar i {
    color: var(--base-color);
    font-size: 1rem;
  }

  .content {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }

  .text {
    padding: 0.75rem 1rem;
    border-radius: 1rem;
    font-size: 0.95rem;
    line-height: 1.4;
  }

  .message.bot .text {
    background: rgba(149, 212, 238, 0.1);
    color: white;
    border-top-left-radius: 0;
  }

  .message.user .text {
    background: var(--accent-color);
    color: var(--base-color);
    border-top-right-radius: 0;
  }

  .timestamp {
    font-size: 0.75rem;
    color: rgba(255, 255, 255, 0.5);
    padding: 0 0.5rem;
  }

  .message.user .timestamp {
    text-align: right;
  }
</style>
