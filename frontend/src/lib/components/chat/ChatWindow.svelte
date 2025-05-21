<script>
  import { fade, fly } from "svelte/transition";
  import ChatMessage from "./ChatMessage.svelte";
  import ChatInput from "./ChatInput.svelte";
  import { onMount } from "svelte";
  import axios from "axios";
  import { page } from "$app/stores";
  import { jwt_token } from "../../../store";

  let messages = [];
  let chatWindow;
  const api_root = $page.url.origin;

  onMount(() => {
    messages = [
      {
        type: "bot",
        content: "Hello there! How can I help you?",
        timestamp: new Date(),
      },
    ];
  });

  async function addMessage(content, type = "user") {
    messages = [
      ...messages,
      {
        type,
        content,
        timestamp: new Date(),
      },
    ];

    // Scroll to end of messages
    setTimeout(() => {
      if (chatWindow) {
        chatWindow.scrollTop = chatWindow.scrollHeight;
      }
    }, 100);

    // If it's a user message, make API call
    if (type === "user") {
      try {
        const query = "?message=" + encodeURIComponent(content);
        const config = {
          method: "get",
          url: api_root + "/api/chat" + query,
          headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer " + $jwt_token,
          },
        };

        const response = await axios(config);

        // Add bot response
        messages = [
          ...messages,
          {
            type: "bot",
            content: response.data,
            timestamp: new Date(),
          },
        ];

        // Scroll again for bot response
        setTimeout(() => {
          if (chatWindow) {
            chatWindow.scrollTop = chatWindow.scrollHeight;
          }
        }, 100);
      } catch (error) {
        console.error("Error while chatting:", error);

        // Specific error message based on backend response
        let errorMessage = "Sorry, there was an error processing your request.";

        if (error.response) {
          // Backend returned a specific error message
          if (error.response.status === 403) {
            errorMessage =
              error.response.data.message ||
              "You don't have the required permissions for this request.";
          } else if (error.response.data && error.response.data.message) {
            errorMessage = error.response.data.message;
          }
        }
        messages = [
          ...messages,
          {
            type: "bot",
            content: errorMessage,
            timestamp: new Date(),
          },
        ];
      }
    }
  }
</script>

<div class="chat-window" bind:this={chatWindow}>
  <div class="messages-container">
    {#each messages as message (message.timestamp)}
      <div
        transition:fly={{ y: 20, duration: 300 }}
        class="message-wrapper {message.type}"
      >
        <ChatMessage {message} />
      </div>
    {/each}
  </div>
</div>

<ChatInput on:send={({ detail }) => addMessage(detail)} />

<style>
  .chat-window {
    flex: 1;
    overflow-y: auto;
    padding: 1rem;
    display: flex;
    flex-direction: column;
    gap: 1rem;
    background: rgba(255, 255, 255, 0.02);
  }

  .messages-container {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }

  .message-wrapper {
    display: flex;
    width: 100%;
  }

  .message-wrapper.user {
    justify-content: flex-end;
  }

  .message-wrapper.bot {
    justify-content: flex-start;
  }

  /* Scrollbar Styling */
  .chat-window::-webkit-scrollbar {
    width: 6px;
  }

  .chat-window::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 3px;
  }

  .chat-window::-webkit-scrollbar-thumb {
    background: var(--accent-color);
    border-radius: 3px;
  }

  .chat-window::-webkit-scrollbar-thumb:hover {
    background: rgba(149, 212, 238, 0.8);
  }
</style>
