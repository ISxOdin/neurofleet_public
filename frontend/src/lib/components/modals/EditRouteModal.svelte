<script>
  import { createEventDispatcher } from "svelte";
  import { hasAnyRole, isAdmin } from "../../../store";
  import { onMount } from "svelte";
  import flatpickr from "flatpickr";
  import "flatpickr/dist/flatpickr.min.css";

  export let route;
  export let vehicles = [];
  export let jobs = [];
  export let companies = [];
  export let myCompanyId = null;

  const dispatch = createEventDispatcher();

  let selectedVehicle = null;
  let totalPayload = 0;

  $: {
    if (route.vehicleId) {
      selectedVehicle = vehicles.find((v) => v.id === route.vehicleId);
    }
  }

  $: {
    totalPayload = route.jobIds.reduce((sum, jobId) => {
      const job = jobs.find((j) => j.id === jobId);
      return sum + (job?.payloadKg || 0);
    }, 0);
    route.totalPayloadKg = totalPayload;
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
  }

  function removeJob(jobId) {
    route.jobIds = route.jobIds.filter((id) => id !== jobId);
  }

  function cancel() {
    dispatch("cancel");
  }

  function save() {
    if (!route.description) {
      alert("Please enter a route description");
      return;
    }
    if (!route.vehicleId) {
      alert("Please select a vehicle");
      return;
    }
    if (!route.scheduledTime) {
      alert("Please select a scheduled time");
      return;
    }
    if (route.jobIds.length === 0) {
      alert("Please add at least one job to the route");
      return;
    }
    dispatch("updated", route);
  }

  onMount(() => {
    flatpickr("#scheduledTime", {
      enableTime: true,
      dateFormat: "Y-m-d\\TH:i",
      time_24hr: true,
      defaultDate: route.scheduledTime,
    });

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
        <h5 class="modal-title">Edit Route</h5>
        <button class="btn-close btn-close-white" onclick={cancel}></button>
      </div>
      <div class="modal-body">
        <div class="row">
          <div class="col-md-6">
            <label>Description</label>
            <input
              class="form-control mb-3"
              bind:value={route.description}
              placeholder="Enter route description"
            />

            <label>Scheduled Time</label>
            <div class="input-group mb-3 datepicker-wrapper">
              <span class="input-group-text"
                ><i class="bi bi-calendar-date"></i></span
              >
              <input
                id="scheduledTime"
                bind:value={route.scheduledTime}
                type="text"
                class="form-control"
                placeholder="YYYY-MM-DD HH:mm"
              />
            </div>

            {#if $isAdmin}
              <label>Company</label>
              <select class="form-select mb-3" bind:value={route.companyId}>
                <option disabled selected value="">Select company</option>
                {#each companies as company}
                  <option value={company.id}>{company.name}</option>
                {/each}
              </select>
            {/if}

            <label>Vehicle</label>
            <select class="form-select mb-3" bind:value={route.vehicleId}>
              <option disabled selected value="">Select vehicle</option>
              {#each vehicles.filter((v) => !v.routeId || v.routeId === route.id) as vehicle}
                <option value={vehicle.id}>
                  {vehicle.licensePlate} ({vehicle.vehicleType} - Capacity:
                  {vehicle.capacity}kg)
                </option>
              {/each}
            </select>

            {#if selectedVehicle}
              <div class="capacity-info mb-3">
                <div
                  class="progress"
                  data-text="{totalPayload}kg / {selectedVehicle.capacity}kg"
                >
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
                  ></div>
                </div>
              </div>
            {/if}
          </div>

          <div class="col-md-6">
            <label>Available Jobs</label>
            <div class="jobs-container">
              {#if jobs.filter((job) => (!job.routeId || job.routeId === route.id) && (!route.companyId || job.companyId === route.companyId)).length > 0}
                {#each jobs.filter((job) => (!job.routeId || job.routeId === route.id) && (!route.companyId || job.companyId === route.companyId) && !route.jobIds.includes(job.id)) as job}
                  <div class="job-item">
                    <div>
                      <strong>{job.description}</strong>
                      <small class="text-white d-block">
                        Payload: {job.payloadKg}kg
                        <br />
                        Scheduled: {new Date(
                          job.scheduledTime
                        ).toLocaleString()}
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
                        <small class="text-white d-block">
                          Payload: {job.payloadKg}kg
                          <br />
                          Scheduled: {new Date(
                            job.scheduledTime
                          ).toLocaleString()}
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
        <button class="btn btn-primary" onclick={save}>Update Route</button>
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

  .form-select:focus {
    background: rgba(255, 255, 255, 0.15);
    border-color: #95d4ee;
    color: #fff;
    box-shadow: none;
  }

  .form-select,
  .form-select option {
    background-color: #2a2e36 !important;
    color: #fff !important;
  }

  .form-select {
    appearance: none;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' fill='%2395d4ee' viewBox='0 0 16 16'%3E%3Cpath d='M7.247 11.14 2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z'/%3E%3C/svg%3E") !important;
    background-repeat: no-repeat !important;
    background-position: right 0.75rem center !important;
    background-size: 16px 12px !important;
    padding-right: 2.5rem !important;
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
    position: relative;
  }

  .progress::after {
    content: attr(data-text);
    position: absolute;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
    white-space: nowrap;
    color: white;
    text-shadow: 0 0 2px rgba(0, 0, 0, 0.5);
    z-index: 1;
  }

  .progress-bar {
    height: 100%;
    transition: width 0.3s ease;
  }

  .bg-success {
    background-color: #28a745 !important;
  }

  .bg-warning {
    background-color: #ffc107 !important;
  }

  .bg-danger {
    background-color: #dc3545 !important;
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

  .input-group {
    display: flex;
    align-items: stretch;
    width: 100%;
  }

  .input-group-text {
    display: flex;
    align-items: center;
    padding: 0.75rem;
    font-size: 1rem;
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-right: none;
    border-radius: 4px 0 0 4px;
    color: #95d4ee;
    margin: 0;
  }

  .input-group .form-control {
    flex: 1 1 auto;
    width: 1%;
    min-width: 0;
    border-radius: 0 4px 4px 0;
    margin: 0;
  }
  .datepicker-wrapper {
    position: relative;
    width: 100%;
  }
</style>
