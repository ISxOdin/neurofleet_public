<script>
  import { user } from "../../store";
</script>

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
