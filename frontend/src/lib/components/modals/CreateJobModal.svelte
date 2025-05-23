<script>
  import { createEventDispatcher } from "svelte";
  import { hasAnyRole, isAdmin, isOwner, isFleet } from "../../../store";
  import flatpickr from "flatpickr";
  import "flatpickr/dist/flatpickr.min.css";
  import { onMount } from "svelte";

  export let companies = [];
  export let locations = [];
  export let myCompanyId = null;
  export let myLocationId = null;

  const dispatch = createEventDispatcher();

  let job = {
    description: "",
    scheduledTime: "",
    originId: "",
    destinationId: "",
    companyId: "",
    locationId: "",
    payloadKg: 0,
  };

  let lastOriginId = "";
  let lastCompanyId = "";

  $: if (job.companyId !== lastCompanyId) {
    lastCompanyId = job.companyId;
    job.originId = "";
    job.destinationId = "";
  }

  $: if (job.originId !== lastOriginId) {
    lastOriginId = job.originId;
    job.destinationId = "";
  }

  // Automatically set company and location based on user role
  $: if ($isOwner && myCompanyId) {
    job.companyId = myCompanyId;
  }

  $: if ($isFleet && myLocationId) {
    const location = locations.find((l) => l.id === myLocationId);
    if (location) {
      job.companyId = location.companyId;
      job.originId = myLocationId;
    }
  }

  onMount(() => {
    flatpickr("#scheduledTime", {
      enableTime: true,
      dateFormat: "Y-m-d\\TH:i",
      time_24hr: true,
    });
  });

  function cancel() {
    dispatch("cancel");
  }

  function save() {
    if (!job.payloadKg || job.payloadKg <= 0) {
      alert("Please enter a valid payload weight (greater than 0 kg)");
      return;
    }
    if ($isFleet && job.originId !== myLocationId) {
      alert(
        "As a fleet manager, you can only create jobs starting from your location"
      );
      return;
    }
    dispatch("created", job);
  }
</script>

<div class="modal-backdrop show"></div>
<div class="modal d-block" tabindex="-1" style="background: rgba(0, 0, 0, 0.5)">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content bg-dark text-light">
      <div class="modal-header">
        <h5 class="modal-title">Create Job</h5>
        <button class="btn-close btn-close-white" onclick={cancel}></button>
      </div>
      <div class="modal-body">
        <label>Description</label>
        <input
          class="form-control mb-3"
          bind:value={job.description}
          placeholder="Enter job description"
        />

        <label>Scheduled Time</label>
        <div class="input-group mb-3 datepicker-wrapper">
          <span class="input-group-text"
            ><i class="bi bi-calendar-date"></i></span
          ><input
            id="scheduledTime"
            bind:value={job.scheduledTime}
            type="text"
            class="form-control"
            placeholder="YYYY-MM-DD HH:mm"
          />
        </div>

        {#if $isAdmin}
          <label>Company</label>
          <select class="form-select mb-3" bind:value={job.companyId}>
            <option disabled selected value="">Select company</option>
            {#each companies as company}
              <option value={company.id}>{company.name}</option>
            {/each}
          </select>
        {:else if $isOwner}
          <input type="hidden" bind:value={job.companyId} />
        {/if}

        {#if hasAnyRole("admin", "owner")}
          <label>Origin</label>
          <select class="form-select mb-3" bind:value={job.originId}>
            <option disabled selected value="">Select origin</option>
            {#each locations.filter((loc) => loc.companyId === job.companyId) as location}
              <option value={location.id}>{location.name}</option>
            {/each}
          </select>
          {#if job.companyId && locations.filter((loc) => loc.companyId === job.companyId).length === 0}
            <small style="color: red" class="mb-3 d-block">
              No locations available for selected company
            </small>
          {/if}
        {:else if $isFleet}
          <label>Origin</label>
          <div class="form-control mb-3 disabled">
            {locations.find((l) => l.id === myLocationId)?.name || "Loading..."}
          </div>
          <input type="hidden" bind:value={job.originId} />
        {/if}

        <label>Destination</label>
        <select
          class="form-select mb-3"
          bind:value={job.destinationId}
          disabled={!job.originId}
        >
          <option disabled selected value="">Select destination</option>
          {#each locations.filter((loc) => loc.companyId === job.companyId && loc.id !== job.originId) as location}
            <option value={location.id}>{location.name}</option>
          {/each}
        </select>

        <label>Payload (kg)</label>
        <input
          type="number"
          class="form-control mb-3"
          bind:value={job.payloadKg}
          min="1"
          placeholder="Enter payload weight in kg"
        />
      </div>
      <div class="modal-footer">
        <button class="btn btn-secondary" onclick={cancel}>Cancel</button>
        <button class="btn btn-primary" onclick={save}>Create</button>
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

  .form-control::placeholder {
    color: rgba(255, 255, 255, 0.5);
  }

  .form-control:disabled {
    background: rgba(255, 255, 255, 0.05);
    opacity: 0.7;
  }

  .modal-content {
    padding: 2rem 2rem 1.5rem 2rem;
    border-radius: 1rem;
    background: rgba(255, 255, 255, 0.05);
  }

  .form-select,
  .form-select option {
    background: #2a2e36 !important;
    color: #fff !important;
  }

  .form-select {
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: #fff;
    padding: 0.75rem;
    border-radius: 4px;
    appearance: none;
    background-repeat: no-repeat;
    background-position: right 0.75rem center;
    background-size: 16px 12px;
    padding-right: 2.5rem;
  }

  .form-select:focus {
    background: rgba(255, 255, 255, 0.15);
    border-color: #95d4ee;
    color: #fff;
    box-shadow: none;
  }

  .form-select:disabled {
    background: rgba(255, 255, 255, 0.05);
    opacity: 0.7;
    cursor: not-allowed;
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
    padding: 0.75rem;
    font-size: 1rem;
    font-weight: bold;
    border-radius: 10px;
    cursor: pointer;
    margin-top: 1rem;
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
