<script>
  import axios from "axios";
  import { page } from "$app/state";
  import { onMount } from "svelte";
  import {
    jwt_token,
    user,
    isAdmin,
    isOwner,
    isFleet,
    hasAnyRole,
    isAuthenticated,
  } from "../../store";
  import { goto } from "$app/navigation";
  import CreateRouteModal from "$lib/components/modals/CreateRouteModal.svelte";

  const api_root = page.url.origin;
  const GOOGLE_MAPS_API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;

  let routes = [];
  let vehicles = [];
  let jobs = [];
  let companies = [];
  let loading = false;
  let showCreateModal = false;
  let expandedRouteId = null;

  let myCompanyId = null;

  onMount(async () => {
    await Promise.all([getRoutes(), getVehicles(), getJobs(), getCompanies()]);
  });

  async function getRoutes() {
    try {
      const res = await axios.get(`${api_root}/api/routes`, {
        headers: { Authorization: "Bearer " + $jwt_token },
      });
      routes = res.data.content;
    } catch (error) {
      console.error("Could not load routes", error);
      alert("Could not load routes");
    }
  }

  async function getVehicles() {
    try {
      const res = await axios.get(`${api_root}/api/vehicles`, {
        headers: { Authorization: "Bearer " + $jwt_token },
      });
      vehicles = res.data.content;
    } catch (error) {
      console.error("Could not load vehicles", error);
      alert("Could not load vehicles");
    }
  }

  async function getJobs() {
    try {
      const res = await axios.get(`${api_root}/api/jobs`, {
        headers: { Authorization: "Bearer " + $jwt_token },
      });
      jobs = res.data.content;
    } catch (error) {
      console.error("Could not load jobs", error);
      alert("Could not load jobs");
    }
  }

  async function getCompanies() {
    try {
      const res = await axios.get(`${api_root}/api/companies`, {
        headers: { Authorization: "Bearer " + $jwt_token },
      });
      companies = res.data.content;
      const myCo =
        companies.find((c) => c.userIds?.includes($user.sub)) ||
        companies.find((c) => c.owner === $user.sub);
      myCompanyId = myCo?.id || null;
    } catch (error) {
      console.error("Could not load companies", error);
      alert("Could not load companies");
    }
  }

  function toggleRouteExpand(routeId) {
    expandedRouteId = expandedRouteId === routeId ? null : routeId;
  }

  function openCreateModal() {
    showCreateModal = true;
  }

  function closeCreateModal() {
    showCreateModal = false;
  }

  function handleRouteCreated(event) {
    const routeData = event.detail;
    axios
      .post(`${api_root}/api/routes`, routeData, {
        headers: {
          Authorization: `Bearer ${$jwt_token}`,
          "Content-Type": "application/json",
        },
      })
      .then(() => {
        alert("Route created");
        closeCreateModal();
        getRoutes();
        getJobs(); // Refresh jobs to update their route assignments
        getVehicles(); // Refresh vehicles to update their route assignments
      })
      .catch((error) => {
        console.error("Could not create route", error);
        alert("Could not create route");
      });
  }

  function goToLogin() {
    goto("/");
  }
</script>

{#if $isAuthenticated}
  <div class="routes-container">
    <div class="routes-header">
      <h1 class="text-center">Routes</h1>
      <button class="btn-accent" onclick={openCreateModal}>
        <i class="bi bi-plus-lg"></i> Create Route
      </button>
    </div>

    <table class="routes-table">
      <thead>
        <tr>
          <th>Name</th>
          <th>Vehicle</th>
          <th>Jobs</th>
          <th>Total Payload</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {#each routes as route}
          <tr class="route-row" class:expanded={expandedRouteId === route.id}>
            <td>{route.name}</td>
            <td>
              {#if vehicles.find((v) => v.id === route.vehicleId)}
                {vehicles.find((v) => v.id === route.vehicleId).licensePlate}
                ({vehicles.find((v) => v.id === route.vehicleId).vehicleType})
              {/if}
            </td>
            <td>
              {route.jobIds?.length || 0} jobs
              <button
                class="btn btn-sm btn-outline-light ms-2"
                onclick={() => toggleRouteExpand(route.id)}
              >
                <i class="bi bi-list"></i> View
              </button>
            </td>
            <td>
              {#if route.jobIds}
                {route.jobIds.reduce((sum, jobId) => {
                  const job = jobs.find((j) => j.id === jobId);
                  return sum + (job?.payloadKg || 0);
                }, 0)}kg
              {/if}
            </td>
            <td>
              <span
                class="status-badge status-{route.routestate.toLowerCase()}"
              >
                {route.routestate.replace("_", " ")}
              </span>
            </td>
            <td>
              <div class="dropdown">
                <button
                  class="btn btn-sm btn-outline-light"
                  type="button"
                  data-bs-toggle="dropdown"
                >
                  <i class="bi bi-gear-fill"></i> Actions
                </button>
                <ul class="dropdown-menu dropdown-menu-dark dropdown-menu-end">
                  <li>
                    <a class="dropdown-item" href="#edit">Edit</a>
                  </li>
                  <li>
                    <a class="dropdown-item text-danger" href="#delete"
                      >Delete</a
                    >
                  </li>
                </ul>
              </div>
            </td>
          </tr>
          {#if expandedRouteId === route.id}
            <tr class="jobs-detail-row">
              <td colspan="6">
                <div class="jobs-list">
                  <h6 class="mb-3">Assigned Jobs</h6>
                  {#if route.jobIds && route.jobIds.length > 0}
                    {#each route.jobIds as jobId}
                      {#if jobs.find((j) => j.id === jobId)}
                        {@const job = jobs.find((j) => j.id === jobId)}
                        <div class="job-item">
                          <div>
                            <strong>{job.description}</strong>
                            <small class="text-muted d-block">
                              Payload: {job.payloadKg}kg
                            </small>
                          </div>
                          <button class="btn btn-sm btn-outline-danger">
                            <i class="bi bi-x"></i> Remove
                          </button>
                        </div>
                      {/if}
                    {/each}
                  {:else}
                    <p class="text-muted">No jobs assigned to this route</p>
                  {/if}
                </div>
              </td>
            </tr>
          {/if}
        {/each}
      </tbody>
    </table>

    {#if showCreateModal}
      <CreateRouteModal
        {vehicles}
        {jobs}
        {companies}
        {myCompanyId}
        on:cancel={closeCreateModal}
        on:created={handleRouteCreated}
      />
    {/if}
  </div>
{:else}
  <div class="container mt-5 text-center">
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
  .routes-container {
    padding: 1rem;
  }

  .routes-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;
    margin-top: 2rem;
    background-color: #343c44;
    padding: 1rem;
    border-radius: 0.5rem;
    border: 1px solid #95d4ee;
  }

  .routes-header h1 {
    color: white;
    font-size: 1.4rem;
    margin: 0;
  }

  .btn-accent {
    background: #95d4ee;
    color: #23272e;
    border: none;
    border-radius: 4px;
    padding: 0.6rem 1.2rem;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    cursor: pointer;
    transition: background 0.2s;
  }

  .btn-accent:hover {
    background: #7bc4e6;
  }

  .routes-table {
    width: 100%;
    border-collapse: separate;
    border-spacing: 0;
    background: #4f5a65;
    color: #fff;
    border-radius: 8px;
    border: 1px solid #95d4ee;
    overflow: hidden;
    margin-bottom: 1.5rem;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }

  .routes-table th,
  .routes-table td {
    padding: 1rem 0.8rem;
    text-align: left;
    vertical-align: middle;
  }

  .routes-table th {
    color: #95d4ee;
    font-weight: 600;
    background: #343c44;
    border-bottom: 2px solid #343c44;
  }

  .route-row {
    transition: background 0.15s;
  }

  .route-row:nth-child(even) {
    background: #343c44;
  }

  .route-row:nth-child(odd) {
    background: #4f5a65;
  }

  .route-row:hover {
    background: rgba(149, 212, 238, 0.2);
  }

  .route-row.expanded {
    background: rgba(149, 212, 238, 0.15);
  }

  .jobs-detail-row td {
    background: #2a2e36;
    padding: 1.5rem !important;
  }

  .jobs-list {
    padding: 0.5rem;
  }

  .job-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.75rem;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 4px;
    margin-bottom: 0.5rem;
  }

  .status-badge {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.35rem 0.75rem;
    border-radius: 1rem;
    font-size: 0.875rem;
    font-weight: 500;
  }

  .status-new {
    background-color: rgba(108, 117, 125, 0.5);
    color: #ced4da;
  }

  .status-in_progress {
    background-color: rgba(0, 123, 255, 0.5);
    color: #75b6fc;
  }

  .status-completed {
    background-color: rgba(40, 167, 69, 0.5);
    color: #3dbe5b;
  }

  .dropdown {
    position: relative;
    z-index: 1000;
  }
</style>
