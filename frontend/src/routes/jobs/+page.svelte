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
  import EditJobModal from "$lib/components/modals/EditJobModal.svelte";
  import CreateJobModal from "$lib/components/modals/CreateJobModal.svelte";
  import { goto } from "$app/navigation";

  const api_root = page.url.origin;
  const GOOGLE_MAPS_API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;

  let vehicles = [];
  let companies = [];
  let locations = [];
  let jobs = [];
  let types = [];

  let loading = false;
  let showEditModal = false;
  let showCreateModal = false;
  let selectedJob = null;
  let expandedRowId = null;

  let myCompanyId = null;
  let myLocationId = null;

  onMount(async () => {
    await getCompanies();
    await getLocations();
    await getVehicles();
    getJobs();
  });

  async function getCompanies() {
    try {
      const res = await axios.get(api_root + "/api/companies", {
        headers: { Authorization: "Bearer " + $jwt_token },
      });
      companies = res.data.content;
      const myCo =
        companies.find((c) => c.userIds?.includes($user.sub)) ||
        companies.find((c) => c.owner === $user.sub);
      myCompanyId = myCo?.id || null;
    } catch (e) {
      alert("Could not load companies");
    }
  }

  async function getLocations() {
    try {
      const res = await axios.get(api_root + "/api/locations", {
        headers: { Authorization: "Bearer " + $jwt_token },
      });

      locations = res.data.content;

      const myLo =
        locations.find((l) => l.userIds?.includes($user.sub)) ||
        locations.find((l) => l.fleetmanagerId === $user.sub);

      myLocationId = myLo?.id || null;
    } catch (e) {
      alert("Could not load locations");
    }
  }

  async function getVehicles() {
    loading = true;
    try {
      const res = await axios.get(api_root + "/api/vehicles", {
        headers: { Authorization: "Bearer " + $jwt_token },
      });
      vehicles = res.data.content;
    } catch {
      alert("Could not load vehicles");
    } finally {
      loading = false;
    }
  }

  function getJobs() {
    axios
      .get(api_root + "/api/jobs", {
        headers: { Authorization: "Bearer " + $jwt_token },
      })
      .then((res) => {
        jobs = res.data.content;
      })
      .catch((error) => {
        alert("Could not load jobs");
        console.error(error);
      });
  }

  function openEditJob(jobData) {
    selectedJob = { ...jobData };
    showEditModal = true;
  }

  function closeEditJobModal() {
    showEditModal = false;
    selectedJob = null;
  }

  function closeCreateJobModal() {
    showCreateModal = false;
  }

  function deleteJob(jobId) {
    if (!confirm("Are you sure you want to delete this job?")) return;

    axios
      .delete(`${api_root}/api/jobs/${jobId}`, {
        headers: {
          Authorization: `Bearer ${$jwt_token}`,
        },
      })
      .then(() => {
        alert("Job deleted");
        getJobs();
      })
      .catch(() => {
        alert("Deletion failed");
      });
  }

  function saveEditedJob(jobData) {
    axios
      .put(`${api_root}/api/jobs/${jobData.id}`, jobData, {
        headers: {
          Authorization: `Bearer ${$jwt_token}`,
          "Content-Type": "application/json",
        },
      })
      .then(() => {
        alert("Job updated");
        closeEditJobModal();
        getJobs();
      })
      .catch(() => {
        alert("Update failed");
      });
  }

  function handleJobCreated(event) {
    const jobData = event.detail;
    axios
      .post(`${api_root}/api/jobs`, jobData, {
        headers: {
          Authorization: `Bearer ${$jwt_token}`,
          "Content-Type": "application/json",
        },
      })
      .then(() => {
        alert("Job created");
        closeCreateJobModal();
        getJobs();
      })
      .catch((error) => {
        alert("Could not create job");
        console.error(error);
      });
  }

  function toggleRow(companyId, event) {
    // Don't toggle if clicking on the dropdown
    if (event.target.closest(".dropdown")) return;
    expandedRowId = expandedRowId === companyId ? null : companyId;
  }

  function goToLogin() {
    goto("/");
  }
</script>

{#if $isAuthenticated}
  <div class="jobs-container">
    <div class="jobs-header">
      <h1 class="text-center">All Jobs</h1>
      <button class="btn-accent" onclick={() => (showCreateModal = true)}>
        <i class="bi bi-plus-lg"></i> Create Job
      </button>
    </div>

    <table class="jobs-table">
      <thead>
        <tr>
          <th scope="col">Description</th>
          <th scope="col">Scheduled Time</th>
          <th scope="col">Company</th>
          <th scope="col">Origin</th>
          <th scope="col">Destination</th>
          <th scope="col">Vehicle</th>
          <th scope="col">State</th>
          <th scope="col">Actions</th>
        </tr>
      </thead>
      <tbody>
        {#each jobs as j}
          <tr
            class="job-row"
            class:expanded={expandedRowId === j.id}
            onclick={(e) => toggleRow(j.id, e)}
          >
            <td>{j.description}</td>
            <td>{j.scheduledTime}</td>
            <td>{companies.find((c) => c.id === j.companyId)?.name}</td>
            <td>{locations.find((l) => l.id === j.originId)?.name}</td>
            <td>{locations.find((l) => l.id === j.destinationId)?.name}</td>
            <td>{vehicles.find((v) => v.id === j.vehicleId)?.licensePlate}</td>
            <td>
              <span class="status-badge status-{j.jobState.toLowerCase()}">
                {#if j.jobState === "NEW"}
                  <i class="bi bi-plus-circle"></i>
                {:else if j.jobState === "SCHEDULED"}
                  <i class="bi bi-calendar-event"></i>
                {:else if j.jobState === "IN_PROGRESS"}
                  <i class="bi bi-truck"></i>
                {:else if j.jobState === "COMPLETED"}
                  <i class="bi bi-check-circle-fill"></i>
                {:else if j.jobState === "FAILED"}
                  <i class="bi bi-exclamation-circle-fill"></i>
                {:else if j.jobState === "CANCELLED"}
                  <i class="bi bi-x-circle-fill"></i>
                {:else if j.jobState === "ABORTED"}
                  <i class="bi bi-stop-circle-fill"></i>
                {/if}
                {j.jobState.replace("_", " ")}
              </span>
            </td>
            <td>
              <div class="dropdown">
                <button
                  class="btn btn-sm btn-outline-light"
                  type="button"
                  data-bs-toggle="dropdown"
                >
                  <i class="bi bi-gear-fill"></i> Edit
                </button>
                <ul
                  class="dropdown-menu dropdown-menu-dark dropdown-menu-end text-small shadow"
                >
                  <li>
                    <a class="dropdown-item" onclick={() => openEditJob(j)}
                      >Edit</a
                    >
                  </li>
                  <li>
                    <a
                      class="dropdown-item text-danger"
                      onclick={() => deleteJob(j.id)}>Delete</a
                    >
                  </li>
                </ul>
              </div>
            </td>
          </tr>
          {#if expandedRowId === j.id}
            <tr class="map-row">
              <td colspan="8">
                {#if j.originId}
                  <iframe
                    width="100%"
                    height="450"
                    style="border:0"
                    loading="lazy"
                    allowfullscreen
                    src="https://www.google.com/maps/embed/v1/directions?key={GOOGLE_MAPS_API_KEY}&origin={encodeURIComponent(
                      locations.find((l) => l.id === j.originId)?.address
                    )}&destination={encodeURIComponent(
                      locations.find((l) => l.id === j.destinationId)?.address
                    )}&mode=driving"
                  ></iframe>
                {:else}
                  <div class="text-center p-3">
                    <i class="bi bi-map"></i> No address available for this job
                  </div>
                {/if}
              </td>
            </tr>
          {/if}
        {/each}
      </tbody>
    </table>

    {#if showEditModal}
      <EditJobModal
        {selectedJob}
        {locations}
        {vehicles}
        on:close={closeEditJobModal}
        on:save={(e) => saveEditedJob(e.detail)}
      />
    {/if}

    {#if showCreateModal}
      <CreateJobModal
        {companies}
        {locations}
        {vehicles}
        {myCompanyId}
        {myLocationId}
        on:cancel={closeCreateJobModal}
        on:created={handleJobCreated}
      />
    {/if}
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
  .jobs-container {
    padding: 1rem;
  }

  .jobs-header {
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

  .jobs-header h1 {
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

  .jobs-table {
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

  .jobs-table th,
  .jobs-table td {
    padding: 1rem 0.8rem;
    text-align: left;
    vertical-align: middle;
  }

  .jobs-table th {
    color: #95d4ee;
    font-weight: 600;
    background: #343c44;
    border-bottom: 2px solid #343c44;
  }

  .jobs-table tbody tr {
    transition: background 0.15s;
  }

  .jobs-table tbody tr:nth-child(even) {
    background: #343c44;
  }

  .jobs-table tbody tr:nth-child(odd) {
    background: #4f5a65;
  }

  .jobs-table tbody tr:hover {
    background: rgba(149, 212, 238, 0.2);
  }

  .dropdown {
    position: relative;
    z-index: 1000;
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

  .status-badge i {
    font-size: 1rem;
  }

  .status-new {
    background-color: rgba(108, 117, 125, 0.5);
    color: #ced4da;
  }

  .status-scheduled {
    background-color: rgba(255, 193, 7, 0.5);
    color: #ffc107;
  }

  .status-in_progress {
    background-color: rgba(0, 123, 255, 0.5);
    color: #75b6fc;
  }

  .status-completed {
    background-color: rgba(40, 167, 69, 0.5);
    color: #3dbe5b;
  }

  .status-failed {
    background-color: rgba(220, 53, 69, 0.5);
    color: #ff6b6b;
  }

  .status-cancelled {
    background-color: rgba(108, 117, 125, 0.5);
    color: #adb5bd;
  }

  .status-aborted {
    background-color: rgba(153, 51, 255, 0.5);
    color: #b266ff;
  }

  @media (max-width: 768px) {
    .jobs-header {
      flex-direction: column;
      gap: 1rem;
      text-align: center;
    }

    .jobs-table {
      display: block;
      overflow-x: auto;
    }
  }
  .job-row {
    cursor: pointer;
    transition: background-color 0.2s;
  }

  .job-row:hover {
    background-color: rgba(149, 212, 238, 0.1);
  }

  .job-row.expanded {
    background-color: rgba(149, 212, 238, 0.15);
  }

  .map-row td {
    padding: 0 !important;
    background-color: #343c44;
  }

  .map-row iframe {
    border-radius: 0 0 8px 8px;
  }
</style>
