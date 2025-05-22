<script>
  import { user } from "../../store";
  import { isAuthenticated } from "../../store";
  import { goto } from "$app/navigation";

  function goToLogin() {
    goto("/");
  }
</script>

{#if $isAuthenticated}
  <div class="page">
    <h1>Driver Dashboard</h1>
    <p>Welcome, {$user.nickname}!</p>

    <div class="card mt-4">
      <h2>Your Assigned Jobs</h2>
      <p>
        This is a placeholder. Here you'll later see a list of transport jobs
        assigned to you.
      </p>
    </div>
  </div>
{:else}
  <div class="container mt-5 text-center" in:fade>
    <div class="not-authenticated">
      <i class="bi bi-lock-fill fa-3x mb-3"></i>
      <p>You are not logged in.</p>
      <button class="btn btn-primary mt-3" onclick={goToLogin}>
        Go to Login
      </button>
    </div>
  </div>
{/if}

<style>
  .page {
    padding: 2rem;
    color: white;
    background-color: #2c3036;
    min-height: 100vh;
  }

  .card {
    background: #3a3f47;
    padding: 1.5rem;
    border-radius: 1rem;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
  }
</style>
