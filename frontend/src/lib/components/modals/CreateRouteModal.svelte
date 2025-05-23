<script>
  import { createEventDispatcher } from "svelte";
  import { hasAnyRole, isAdmin } from "../../../store";
  import VehicleSelect from "$lib/components/forms/VehicleSelect.svelte";
  import { onMount } from "svelte";

  export let vehicles = [];
  export let jobs = [];
  export let companies = [];
  export let myCompanyId = null;

  const dispatch = createEventDispatcher();

  let route = {
    name: "",
    vehicleId: "",
    jobIds: [],
    companyId: "",
    waypoints: [],
  };

  let availableJobs = [];
  let selectedVehicle = null;
  let totalPayload = 0;

  $: {
    if (route.vehicleId) {
      selectedVehicle = vehicles.find((v) => v.id === route.vehicleId);
    }
  }

  $: {
    if (route.companyId) {
      availableJobs = jobs.filter(
        (job) =>
          job.companyId === route.companyId &&
          !job.routeId &&
          job.jobState === "NEW"
      );
    }
  }

  $: {
    totalPayload = route.jobIds.reduce((sum, jobId) => {
      const job = jobs.find((j) => j.id === jobId);
      return sum + (job?.payloadKg || 0);
    }, 0);
  }

  function addJob(jobId) {
    const job = jobs.find((j) => j.id === jobId);
    if (!job) return;

    // Check if adding this job would exceed vehicle capacity
    const newTotalPayload = totalPayload + job.payloadKg;
    if (selectedVehicle && newTotalPayload > selectedVehicle.capacity) {
      alert(
        `Cannot add job: Would exceed vehicle capacity of ${selectedVehicle.capacity}kg`
      );
      return;
    }

    route.jobIds = [...route.jobIds, jobId];

    // Update waypoints
    const jobLocation = job.originId;
    if (!route.waypoints.includes(jobLocation)) {
      route.waypoints = [...route.waypoints, jobLocation];
    }
    if (!route.waypoints.includes(job.destinationId)) {
      route.waypoints = [...route.waypoints, job.destinationId];
    }
  }

  function removeJob(jobId) {
    route.jobIds = route.jobIds.filter((id) => id !== jobId);

    // Recalculate waypoints
    const remainingJobs = jobs.filter((job) => route.jobIds.includes(job.id));
    const newWaypoints = new Set();
    remainingJobs.forEach((job) => {
      newWaypoints.add(job.originId);
      newWaypoints.add(job.destinationId);
    });
    route.waypoints = Array.from(newWaypoints);
  }

  function cancel() {
    dispatch("cancel");
  }

  function save() {
    if (!route.name) {
      alert("Please enter a route name");
      return;
    }
    if (!route.vehicleId) {
      alert("Please select a vehicle");
      return;
    }
    if (route.jobIds.length === 0) {
      alert("Please add at least one job to the route");
      return;
    }
    dispatch("created", route);
  }

  onMount(() => {
    if (!isAdmin && myCompanyId) {
      route.companyId = myCompanyId;
    }
  });
</script>

<div class="modal-backdrop show"></div>
<div class="modal d-block" tabindex="-1" style="background: rgba(0, 0, 0, 0.5)">
  <div class="modal-dialog modal-dialog-centered modal-lg">
    <div class="modal-content bg-dark text-light">
      <div class="modal-header">
        <h5 class="modal-title">Create Route</h5>
        <button class="btn-close btn-close-white" onclick={cancel}></button>
      </div>
      <div class="modal-body">
        <div class="row">
          <div class="col-md-6">
            <label>Route Name</label>
            <input
              class="form-control mb-3"
              bind:value={route.name}
              placeholder="Enter route name"
            />

            {#if $isAdmin}
              <label>Company</label>
              <select class="form-select mb-3" bind:value={route.companyId}>
                <option disabled selected value="">Select company</option>
                {#each companies as company}
                  <option value={company.id}>{company.name}</option>
                {/each}
              </select>
            {/if}

            <VehicleSelect
              bind:bindValue={route.vehicleId}
              vehicles={vehicles.filter(
                (v) =>
                  !v.routeId &&
                  (!route.companyId || v.companyId === route.companyId)
              )}
              label="Assign Vehicle"
            />

            {#if selectedVehicle}
              <div class="capacity-info mb-3">
                <div class="progress">
                  <div
                    class="progress-bar"
                    role="progressbar"
                    style="width: {(totalPayload / selectedVehicle.capacity) *
                      100}%"
                    class:bg-danger={totalPayload > selectedVehicle.capacity}
                    class:bg-warning={totalPayload >
                      selectedVehicle.capacity * 0.8 &&
                      totalPayload <= selectedVehicle.capacity}
                    class:bg-success={totalPayload <=
                      selectedVehicle.capacity * 0.8}
                  >
                    {totalPayload}kg / {selectedVehicle.capacity}kg
                  </div>
                </div>
              </div>
            {/if}
          </div>

          <div class="col-md-6">
            <label>Available Jobs</label>
            <div class="jobs-container">
              {#if availableJobs.length > 0}
                {#each availableJobs.filter((job) => !route.jobIds.includes(job.id)) as job}
                  <div class="job-item">
                    <div>
                      <strong>{job.description}</strong>
                      <small class="text-muted d-block">
                        Payload: {job.payloadKg}kg
                      </small>
                    </div>
                    <button
                      class="btn btn-sm btn-outline-success"
                      onclick={() => addJob(job.id)}
                    >
                      <i class="bi bi-plus"></i> Add
                    </button>
                  </div>
                {/each}
              {:else}
                <p class="text-muted">No available jobs</p>
              {/if}
            </div>

            {#if route.jobIds.length > 0}
              <label class="mt-3">Selected Jobs</label>
              <div class="jobs-container">
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
                      <button
                        class="btn btn-sm btn-outline-danger"
                        onclick={() => removeJob(job.id)}
                      >
                        <i class="bi bi-x"></i> Remove
                      </button>
                    </div>
                  {/if}
                {/each}
              </div>
            {/if}
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-secondary" onclick={cancel}>Cancel</button>
        <button class="btn btn-primary" onclick={save}>Create Route</button>
      </div>
    </div>
  </div>
</div>

<style>
  .modal-backdrop {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.6);
    z-index: 999;
  }

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #95d4ee;
    margin-bottom: 1.5rem;
  }

  .modal-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-top: 1px solid #95d4ee;
    margin-bottom: 1.5rem;
  }

  label {
    display: block;
    font-weight: 600;
    margin-bottom: 0.3rem;
    color: #95d4ee;
  }

  .form-control {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 4px;
    background: rgba(255, 255, 255, 0.1);
    color: #fff;
  }

  .form-control:focus {
    background: rgba(255, 255, 255, 0.15);
    border-color: #95d4ee;
    color: #fff;
    box-shadow: none;
  }

  .form-select {
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: #fff;
    padding: 0.75rem;
    border-radius: 4px;
  }

  .jobs-container {
    max-height: 300px;
    overflow-y: auto;
    padding: 0.5rem;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 4px;
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

  .capacity-info {
    padding: 1rem;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 4px;
  }

  .progress {
    height: 1.5rem;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 0.75rem;
    overflow: hidden;
  }

  .progress-bar {
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 0.875rem;
    transition: width 0.3s ease;
  }

  .modal-content {
    padding: 2rem 2rem 1.5rem 2rem;
    border-radius: 1rem;
    background: #343c44;
  }

  .btn-primary {
    background: transparent !important;
    color: #95d4ee !important;
    border: none !important;
    font-weight: 600;
  }

  .btn-primary:hover {
    background: rgba(149, 212, 238, 0.1) !important;
    color: #fff !important;
  }

  .btn-secondary {
    background: transparent !important;
    color: #95d4ee !important;
    border: none !important;
    font-weight: 600;
  }

  .btn-secondary:hover {
    background: rgba(149, 212, 238, 0.1) !important;
    color: #fff !important;
  }
</style>
