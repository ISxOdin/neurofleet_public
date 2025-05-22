<script>
  import { onMount } from "svelte";
  import { user, hasAnyRole, jwt_token } from "../../store";
  import axios from "axios";
  import { page } from "$app/stores";

  const api_root = $page.url.origin;
  let dashboardData = {
    company: "",
    location: "",
    activeVehicles: 0,
    scheduledJobs: 0,
    inProgressJobs: 0,
    completedJobsToday: 0,
    alerts: [],
  };

  let loading = true;
  let error = null;

  onMount(async () => {
    try {
      const response = await axios.get(`${api_root}/api/dashboard`, {
        headers: {
          Authorization: `Bearer ${$jwt_token}`,
        },
      });
      dashboardData = response.data;
    } catch (err) {
      error = "Failed to load dashboard data";
      console.error("Dashboard data error:", err);
    } finally {
      loading = false;
    }
  });
</script>

<div class="dashboard-container">
  {#if loading}
    <div class="loading-spinner">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>
  {:else if error}
    <div class="alert alert-danger" role="alert">
      {error}
    </div>
  {:else}
    <!-- Role-specific Information -->
    <div class="role-info-section">
      {#if hasAnyRole(["owner"])}
        <div class="info-card">
          <div class="card-icon">
            <i class="fas fa-building"></i>
          </div>
          <div class="card-content">
            <h3>My Company</h3>
            <p>{dashboardData.company}</p>
          </div>
        </div>
      {/if}

      {#if hasAnyRole(["fleetmanager"])}
        <div class="info-card">
          <div class="card-icon">
            <i class="fas fa-map-marker-alt"></i>
          </div>
          <div class="card-content">
            <h3>Location</h3>
            <p>{dashboardData.location}</p>
          </div>
        </div>
      {/if}
    </div>

    <!-- Job Statistics -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">
          <i class="fas fa-truck"></i>
        </div>
        <div class="stat-content">
          <h4>Active Vehicles</h4>
          <p class="stat-number">{dashboardData.activeVehicles}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">
          <i class="fas fa-calendar-check"></i>
        </div>
        <div class="stat-content">
          <h4>Scheduled Jobs</h4>
          <p class="stat-number">{dashboardData.scheduledJobs}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">
          <i class="fas fa-spinner"></i>
        </div>
        <div class="stat-content">
          <h4>In Progress Jobs</h4>
          <p class="stat-number">{dashboardData.inProgressJobs}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">
          <i class="fas fa-check-circle"></i>
        </div>
        <div class="stat-content">
          <h4>Completed Today</h4>
          <p class="stat-number">{dashboardData.completedJobsToday}</p>
        </div>
      </div>
    </div>

    <!-- Alerts Section -->
    <div class="alerts-section">
      <h3>Alerts</h3>
      {#if dashboardData.alerts.length === 0}
        <p class="no-alerts">No alerts at the moment</p>
      {:else}
        <div class="alerts-list">
          {#each dashboardData.alerts as alert}
            <div class="alert-item {alert.type}">
              <i
                class="fas {alert.type === 'cancelled'
                  ? 'fa-ban'
                  : 'fa-exclamation-triangle'}"
              ></i>
              <div class="alert-content">
                <h5>{alert.title}</h5>
                <p>{alert.message}</p>
                <small>{alert.timestamp}</small>
              </div>
            </div>
          {/each}
        </div>
      {/if}
    </div>
  {/if}
</div>

<style>
  .dashboard-container {
    padding: 2rem;
    max-width: 1200px;
    margin: 0 auto;
  }

  .loading-spinner {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 200px;
  }

  .role-info-section {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1.5rem;
    margin-bottom: 2rem;
  }

  .info-card {
    background: var(--background-color, #343c44);
    border-radius: 8px;
    padding: 1.5rem;
    display: flex;
    align-items: center;
    gap: 1rem;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .card-icon {
    font-size: 1.5rem;
    color: var(--accent-color, #95d4ee);
  }

  .card-content h3 {
    margin: 0;
    font-size: 1.1rem;
    color: #fff;
  }

  .card-content p {
    margin: 0.5rem 0 0;
    color: #ccc;
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1.5rem;
    margin-bottom: 2rem;
  }

  .stat-card {
    background: var(--background-color, #343c44);
    border-radius: 8px;
    padding: 1.5rem;
    display: flex;
    align-items: center;
    gap: 1rem;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .stat-icon {
    font-size: 1.5rem;
    color: var(--accent-color, #95d4ee);
  }

  .stat-content h4 {
    margin: 0;
    font-size: 0.9rem;
    color: #ccc;
  }

  .stat-number {
    margin: 0.5rem 0 0;
    font-size: 1.5rem;
    font-weight: bold;
    color: #fff;
  }

  .alerts-section {
    background: var(--background-color, #343c44);
    border-radius: 8px;
    padding: 1.5rem;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .alerts-section h3 {
    margin: 0 0 1rem;
    color: #fff;
  }

  .no-alerts {
    color: #ccc;
    text-align: center;
    padding: 1rem;
  }

  .alerts-list {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .alert-item {
    display: flex;
    align-items: flex-start;
    gap: 1rem;
    padding: 1rem;
    border-radius: 6px;
    background: rgba(255, 255, 255, 0.05);
  }

  .alert-item.cancelled {
    border-left: 4px solid #dc3545;
  }

  .alert-item.aborted {
    border-left: 4px solid #ffc107;
  }

  .alert-item i {
    color: var(--accent-color, #95d4ee);
    font-size: 1.2rem;
  }

  .alert-content h5 {
    margin: 0;
    color: #fff;
  }

  .alert-content p {
    margin: 0.25rem 0;
    color: #ccc;
  }

  .alert-content small {
    color: #888;
  }

  @media (max-width: 768px) {
    .dashboard-container {
      padding: 1rem;
    }

    .stats-grid {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  @media (max-width: 480px) {
    .stats-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
