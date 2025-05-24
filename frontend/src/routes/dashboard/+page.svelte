<script>
  import { onMount } from "svelte";
  import { user, hasAnyRole, jwt_token, isAuthenticated } from "../../store";
  import axios from "axios";
  import { page } from "$app/stores";
  import { goto } from "$app/navigation";
  import { findUserCompany } from "$lib/utils";
  const GOOGLE_MAPS_API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;

  const api_root = $page.url.origin;
  let dashboardData = {
    company: "",
    location: "",
    activeVehicles: 0,
    scheduledJobs: 0,
    inProgressJobs: 0,
    completedJobsToday: 0,
    alerts: [],
    optimizationSuggestions: [],
  };

  let loading = true;
  let error = null;
  let activeRoutes = [];
  let companyName = "";
  let jobs = [];
  let companies = [];
  let myCompanyId = null;

  onMount(async () => {
    try {
      // Get companies data first
      const companiesResponse = await axios.get(`${api_root}/api/companies`, {
        headers: {
          Authorization: `Bearer ${$jwt_token}`,
        },
      });
      companies = companiesResponse.data.content;
      myCompanyId = findUserCompany(companies, $jwt_token);
      console.log("My Company ID:", myCompanyId);

      if (!myCompanyId) {
        error = "Could not determine company ID";
        return;
      }

      // Then fetch all other data
      const [dashboardResponse, routesResponse, optimizationResponse] =
        await Promise.all([
          axios.get(`${api_root}/api/dashboard/${myCompanyId}`, {
            headers: {
              Authorization: `Bearer ${$jwt_token}`,
            },
          }),
          axios.get(`${api_root}/api/routes?pageSize=1000`, {
            headers: {
              Authorization: `Bearer ${$jwt_token}`,
            },
          }),
          axios.get(`${api_root}/api/optimization/summary`, {
            headers: {
              Authorization: `Bearer ${$jwt_token}`,
            },
          }),
        ]);

      dashboardData = dashboardResponse.data;

      // Filter routes to only show active ones and create alerts for problematic ones
      const allRoutes = routesResponse.data.content || [];
      activeRoutes = allRoutes.filter((route) =>
        ["SCHEDULED", "IN_PROGRESS"].includes(route.state)
      );

      // Add problematic routes to alerts
      const problematicRoutes = allRoutes.filter((route) =>
        ["FAILED", "CANCELLED", "ABORTED"].includes(route.state)
      );

      // Add route alerts to dashboardData.alerts
      dashboardData.alerts = [
        ...dashboardData.alerts,
        ...problematicRoutes.map((route) => ({
          id: `route-${route.id}`,
          type: route.state.toLowerCase(),
          title: `Route ${route.state.toLowerCase()}`,
          message: `Route "${route.description}" has been ${route.state.toLowerCase()}. ${route.alertReason || ""}`,
          timestamp: new Date().toISOString(),
          routeId: route.id,
        })),
      ];

      companyName = dashboardData.company;

      // Use the optimization suggestions from the API
      dashboardData.optimizationSuggestions =
        optimizationResponse.data.optimizationSuggestions;
    } catch (err) {
      error = "Failed to load dashboard data";
      console.error("Dashboard data error:", err);
    } finally {
      loading = false;
    }
  });

  function goToLogin() {
    goto("/");
  }

  function getImpactColor(impact) {
    switch (impact.toLowerCase()) {
      case "high":
        return "suggestion-high";
      case "medium":
        return "suggestion-medium";
      case "low":
        return "suggestion-low";
      default:
        return "";
    }
  }

  function getConfidencePercentage(confidence) {
    return Math.round(confidence * 100);
  }

  async function getJobs() {
    try {
      const response = await axios.get(`${api_root}/api/jobs?pageSize=1000`, {
        headers: { Authorization: `Bearer ${$jwt_token}` },
      });

      jobs = response.data.content;
      return jobs;
    } catch (error) {
      console.error("Failed to load jobs:", error);
      throw new Error("Could not load jobs. Please try again later.");
    }
  }
</script>

{#if $isAuthenticated}
  <div class="dashboard-container">
    {#if loading}
      <div class="loading-spinner">
        <div class="spinner-border text-light" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
      </div>
    {:else if error}
      <div class="alert alert-danger" role="alert">
        {error}
      </div>
    {:else}
      <!-- Company Header -->
      <div class="company-header">
        <h1>{companyName || "Dashboard"}</h1>
        {#if dashboardData.location}
          <div class="location-badge">
            <i class="bi bi-geo-alt-fill"></i>
            {dashboardData.location}
          </div>
        {/if}
      </div>

      <!-- Stats Grid -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">
            <i class="bi bi-truck"></i>
          </div>
          <div class="stat-content">
            <h4>Active Vehicles</h4>
            <p class="stat-number">{dashboardData.activeVehicles}</p>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">
            <i class="bi bi-calendar-check"></i>
          </div>
          <div class="stat-content">
            <h4>Scheduled Jobs</h4>
            <p class="stat-number">{dashboardData.scheduledJobs}</p>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">
            <i class="bi bi-arrow-repeat"></i>
          </div>
          <div class="stat-content">
            <h4>In Progress Jobs</h4>
            <p class="stat-number">{dashboardData.inProgressJobs}</p>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">
            <i class="bi bi-check-circle"></i>
          </div>
          <div class="stat-content">
            <h4>Completed Today</h4>
            <p class="stat-number">{dashboardData.completedJobsToday}</p>
          </div>
        </div>
      </div>

      <!-- Active Routes and AI Suggestions -->
      <div class="dashboard-grid">
        <!-- Active Routes -->
        <div class="active-routes-section">
          <h3>
            <i class="bi bi-truck"></i>
            Active Routes
          </h3>
          {#if activeRoutes.length === 0}
            <p class="no-routes">No active routes at the moment</p>
          {:else}
            <div class="routes-list">
              {#each activeRoutes as route}
                <div class="route-card">
                  <div class="route-header">
                    <h4>{route.description}</h4>
                    <span
                      class="status-badge status-{route.state.toLowerCase()}"
                    >
                      {#if route.state === "SCHEDULED"}
                        <i class="bi bi-calendar-event"></i>
                        Scheduled
                      {:else if route.state === "IN_PROGRESS"}
                        <i class="bi bi-truck"></i>
                        In Progress
                      {/if}
                    </span>
                  </div>
                  <div class="route-details">
                    <div class="detail-item">
                      <i class="bi bi-box-seam"></i>
                      <span>{route.jobIds.length} Jobs</span>
                    </div>
                    <div class="detail-item">
                      <i class="bi bi-weight"></i>
                      <span>{route.totalPayloadKg}kg Payload</span>
                    </div>
                    <div class="detail-item">
                      <i class="bi bi-calendar-event"></i>
                      <span
                        >{new Date(route.scheduledTime).toLocaleString()}</span
                      >
                    </div>
                  </div>
                </div>
              {/each}
            </div>
          {/if}
        </div>

        <!-- Replace Map Section with AI Suggestions -->
        <div class="optimization-section">
          <h3>
            <i class="bi bi-lightning-charge"></i>
            AI Optimization Suggestions
          </h3>
          {#if dashboardData.optimizationSuggestions && dashboardData.optimizationSuggestions.length > 0}
            <div class="suggestions-list">
              {#each dashboardData.optimizationSuggestions as suggestion}
                <div
                  class="suggestion-card {getImpactColor(suggestion.impact)}"
                >
                  <div class="suggestion-header">
                    <h4>{suggestion.title}</h4>
                    <span class="confidence-badge">
                      <i class="bi bi-graph-up"></i>
                      {getConfidencePercentage(suggestion.confidence)}%
                      confidence
                    </span>
                  </div>
                  <p class="suggestion-description">{suggestion.description}</p>
                  <div class="suggestion-details">
                    <div class="savings-badge">
                      <i class="bi bi-piggy-bank"></i>
                      Potential savings: {suggestion.potentialSavings}
                    </div>
                    <button class="btn btn-outline-light btn-sm">
                      <i class="bi bi-lightning"></i>
                      Apply Optimization
                    </button>
                  </div>
                </div>
              {/each}
            </div>
          {:else}
            <p class="no-suggestions">
              No optimization suggestions available at the moment
            </p>
          {/if}
        </div>
      </div>

      <!-- Alerts Section -->
      <div class="alerts-section">
        <h3>
          <i class="bi bi-exclamation-triangle"></i>
          Alerts
        </h3>
        {#if dashboardData.alerts.length === 0}
          <p class="no-alerts">No alerts at the moment</p>
        {:else}
          <div class="alerts-list">
            {#each dashboardData.alerts as alert}
              <div class="alert-item {alert.type}">
                <i
                  class="bi bi-{alert.type === 'cancelled'
                    ? 'x-circle'
                    : alert.type === 'failed'
                      ? 'exclamation-circle'
                      : alert.type === 'aborted'
                        ? 'stop-circle'
                        : 'exclamation-triangle'}"
                ></i>
                <div class="alert-content">
                  <h5>{alert.title}</h5>
                  <p>{alert.message}</p>
                  <small>{new Date(alert.timestamp).toLocaleString()}</small>
                </div>
              </div>
            {/each}
          </div>
        {/if}
      </div>
    {/if}
  </div>
{:else}
  <div class="container mt-5 text-center">
    <div class="not-authenticated">
      <i class="bi bi-lock-fill fa-3x mb-3"></i>
      <p>You are not logged in.</p>
      <button class="btn btn-primary mt-3" on:click={goToLogin}>
        Go to Login
      </button>
    </div>
  </div>
{/if}

<style>
  .dashboard-container {
    padding: 2rem;
    max-width: 1400px;
    margin: 0 auto;
    background: linear-gradient(
      to bottom,
      rgba(52, 60, 68, 0.4),
      rgba(52, 60, 68, 0.1)
    );
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  }

  .company-header {
    margin-bottom: 2.5rem;
    display: flex;
    align-items: center;
    gap: 1rem;
    padding-bottom: 1.5rem;
    border-bottom: 1px solid rgba(149, 212, 238, 0.1);
  }

  .company-header h1 {
    margin: 0;
    background: linear-gradient(135deg, #95d4ee, #5a9fc7);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
    font-size: 2.25rem;
    font-weight: 700;
    letter-spacing: -0.5px;
  }

  .location-badge {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.6rem 1.2rem;
    background: linear-gradient(
      135deg,
      rgba(149, 212, 238, 0.15),
      rgba(149, 212, 238, 0.05)
    );
    border: 1px solid rgba(149, 212, 238, 0.2);
    border-radius: 2rem;
    color: #95d4ee;
    font-size: 0.95rem;
    backdrop-filter: blur(10px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 1.5rem;
    margin-bottom: 2.5rem;
  }

  .stat-card {
    background: linear-gradient(
      135deg,
      rgba(255, 255, 255, 0.08),
      rgba(255, 255, 255, 0.03)
    );
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    padding: 1.5rem;
    transition:
      transform 0.2s,
      box-shadow 0.2s;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  }

  .stat-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  }

  .stat-icon {
    color: #95d4ee;
    font-size: 1.75rem;
    margin-bottom: 1rem;
  }

  .stat-content h4 {
    color: rgba(255, 255, 255, 0.8);
    font-size: 0.9rem;
    margin-bottom: 0.5rem;
    font-weight: 500;
  }

  .stat-number {
    color: #fff;
    font-size: 1.75rem;
    font-weight: 600;
    margin: 0;
  }

  .dashboard-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 2rem;
    margin: 2rem 0;
  }

  .active-routes-section,
  .optimization-section {
    background: linear-gradient(
      135deg,
      rgba(52, 60, 68, 0.95),
      rgba(52, 60, 68, 0.85)
    );
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    padding: 1.75rem;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
    backdrop-filter: blur(10px);
  }

  .active-routes-section h3,
  .optimization-section h3,
  .alerts-section h3 {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    color: #95d4ee;
    margin-bottom: 1.75rem;
    font-size: 1.35rem;
    font-weight: 600;
    letter-spacing: -0.3px;
  }

  .routes-list {
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
    max-height: 450px;
    overflow-y: auto;
    padding-right: 0.5rem;
  }

  .routes-list::-webkit-scrollbar {
    width: 6px;
  }

  .routes-list::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 3px;
  }

  .routes-list::-webkit-scrollbar-thumb {
    background: rgba(149, 212, 238, 0.3);
    border-radius: 3px;
  }

  .route-card {
    background: linear-gradient(
      135deg,
      rgba(255, 255, 255, 0.08),
      rgba(255, 255, 255, 0.03)
    );
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 10px;
    padding: 1.25rem;
    transition:
      transform 0.2s,
      box-shadow 0.2s;
  }

  .route-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  }

  .route-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.25rem;
  }

  .route-header h4 {
    margin: 0;
    color: #fff;
    font-size: 1.15rem;
    font-weight: 600;
  }

  .route-details {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 1.25rem;
  }

  .detail-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    color: rgba(149, 212, 238, 0.9);
    font-size: 0.95rem;
  }

  .optimization-section {
    background: linear-gradient(
      135deg,
      rgba(52, 60, 68, 0.95),
      rgba(52, 60, 68, 0.85)
    );
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    padding: 1.75rem;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
    backdrop-filter: blur(10px);
  }

  .suggestions-list {
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
  }

  .suggestion-card {
    background: linear-gradient(
      135deg,
      rgba(255, 255, 255, 0.08),
      rgba(255, 255, 255, 0.03)
    );
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 10px;
    padding: 1.5rem;
    transition:
      transform 0.2s,
      box-shadow 0.2s;
  }

  .suggestion-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  }

  .suggestion-card.suggestion-high {
    border-left: 4px solid #2ecc71;
  }

  .suggestion-card.suggestion-medium {
    border-left: 4px solid #f1c40f;
  }

  .suggestion-card.suggestion-low {
    border-left: 4px solid #95d4ee;
  }

  .suggestion-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
  }

  .suggestion-header h4 {
    margin: 0;
    color: #fff;
    font-size: 1.15rem;
    font-weight: 600;
  }

  .confidence-badge {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.4rem 0.8rem;
    background: linear-gradient(
      135deg,
      rgba(46, 204, 113, 0.15),
      rgba(46, 204, 113, 0.05)
    );
    border: 1px solid rgba(46, 204, 113, 0.2);
    border-radius: 2rem;
    color: #2ecc71;
    font-size: 0.85rem;
    font-weight: 500;
  }

  .suggestion-description {
    color: rgba(255, 255, 255, 0.8);
    font-size: 0.95rem;
    line-height: 1.5;
    margin: 0 0 1.25rem 0;
  }

  .suggestion-details {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 1rem;
  }

  .savings-badge {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.4rem 0.8rem;
    background: linear-gradient(
      135deg,
      rgba(149, 212, 238, 0.15),
      rgba(149, 212, 238, 0.05)
    );
    border: 1px solid rgba(149, 212, 238, 0.2);
    border-radius: 2rem;
    color: #95d4ee;
    font-size: 0.9rem;
  }

  .suggestion-details button {
    padding: 0.4rem 1rem;
    font-size: 0.9rem;
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    transition: all 0.2s;
  }

  .suggestion-details button:hover {
    background: #95d4ee;
    border-color: #95d4ee;
    color: #343c44;
    transform: translateY(-1px);
  }

  .no-routes,
  .no-suggestions,
  .no-alerts {
    text-align: center;
    color: rgba(149, 212, 238, 0.9);
    padding: 2.5rem;
    background: linear-gradient(
      135deg,
      rgba(255, 255, 255, 0.05),
      rgba(255, 255, 255, 0.02)
    );
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 10px;
    font-size: 1.1rem;
  }

  .status-badge {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.4rem 1rem;
    border-radius: 2rem;
    font-size: 0.9rem;
    font-weight: 500;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }

  .status-scheduled {
    background: linear-gradient(
      135deg,
      rgba(255, 193, 7, 0.3),
      rgba(255, 193, 7, 0.15)
    );
    border: 1px solid rgba(255, 193, 7, 0.3);
    color: #ffc107;
  }

  .status-in_progress {
    background: linear-gradient(
      135deg,
      rgba(0, 123, 255, 0.3),
      rgba(0, 123, 255, 0.15)
    );
    border: 1px solid rgba(117, 182, 252, 0.3);
    color: #75b6fc;
  }

  .alerts-section {
    background: linear-gradient(
      135deg,
      rgba(52, 60, 68, 0.95),
      rgba(52, 60, 68, 0.85)
    );
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    padding: 1.75rem;
    margin-top: 2rem;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  }

  .alerts-list {
    display: grid;
    gap: 1.25rem;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  }

  .alert-item {
    background: linear-gradient(
      135deg,
      rgba(255, 255, 255, 0.08),
      rgba(255, 255, 255, 0.03)
    );
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 10px;
    padding: 1.25rem;
    display: flex;
    gap: 1rem;
    align-items: flex-start;
    transition:
      transform 0.2s,
      box-shadow 0.2s;
  }

  .alert-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  }

  .alert-item i {
    font-size: 1.5rem;
    color: #95d4ee;
  }

  .alert-content h5 {
    color: #fff;
    margin: 0 0 0.5rem 0;
    font-size: 1.1rem;
    font-weight: 600;
  }

  .alert-content p {
    color: rgba(255, 255, 255, 0.8);
    margin: 0 0 0.5rem 0;
    font-size: 0.95rem;
    line-height: 1.5;
  }

  .alert-content small {
    color: rgba(149, 212, 238, 0.7);
    font-size: 0.85rem;
  }

  .loading-spinner {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 300px;
  }

  .not-authenticated {
    background: linear-gradient(
      135deg,
      rgba(52, 60, 68, 0.95),
      rgba(52, 60, 68, 0.85)
    );
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    padding: 3rem;
    text-align: center;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  }

  .not-authenticated i {
    font-size: 3rem;
    color: #95d4ee;
    margin-bottom: 1.5rem;
  }

  .not-authenticated p {
    color: rgba(255, 255, 255, 0.8);
    font-size: 1.2rem;
    margin-bottom: 1.5rem;
  }

  @media (max-width: 1200px) {
    .dashboard-grid {
      grid-template-columns: 1fr;
    }

    .stats-grid {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  @media (max-width: 768px) {
    .dashboard-container {
      padding: 1rem;
    }

    .stats-grid {
      grid-template-columns: 1fr;
    }

    .company-header {
      flex-direction: column;
      align-items: flex-start;
      text-align: left;
      gap: 1rem;
    }

    .alerts-list {
      grid-template-columns: 1fr;
    }
  }

  .alert-item.failed {
    border-left: 4px solid #dc3545;
  }

  .alert-item.cancelled {
    border-left: 4px solid #6c757d;
  }

  .alert-item.aborted {
    border-left: 4px solid #fd7e14;
  }
</style>
