<script>
  import LocationSelect from "$lib/components/forms/LocationSelect.svelte";
  import JobStatusSelect from "$lib/components/forms/JobStatusSelect.svelte";
  import flatpickr from "flatpickr";
  import { createEventDispatcher, onMount } from "svelte";
  import { isAdmin, isOwner, isFleet, hasAnyRole } from "../../../store";

  export let selectedJob;
  export let locations = [];
  export let companies = [];
  export let myCompanyId = null;
  export let myLocationId = null;

  const dispatch = createEventDispatcher();

  let flatpickrInstance;

  // Automatically set company and location based on user role
  $: if ($isOwner && myCompanyId) {
    selectedJob.companyId = myCompanyId;
  }

  $: if ($isFleet && myLocationId) {
    const location = locations.find((l) => l.id === myLocationId);
    if (location) {
      selectedJob.companyId = location.companyId;
      selectedJob.locationId = myLocationId;
    }
  }

  onMount(() => {
    flatpickrInstance = flatpickr("#editScheduledTime", {
      enableTime: true,
      dateFormat: "Y-m-d\\TH:i",
      time_24hr: true,
      defaultDate: selectedJob?.scheduledTime,
    });
  });
</script>

{#if selectedJob}
  <div class="modal-backdrop show"></div>
  <div
    class="modal d-block"
    tabindex="-1"
    style="background: rgba(0, 0, 0, 0.5)"
  >
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content bg-dark text-light">
        <div class="modal-header">
          <h5 class="modal-title">Edit Job</h5>
          <button
            class="btn-close btn-close-white"
            onclick={() => dispatch("close")}
          ></button>
        </div>
        <div class="modal-body">
          <label>Description</label>
          <input
            class="form-control mb-3"
            bind:value={selectedJob.description}
          />

          <label>Scheduled Time</label>
          <div class="input-group mb-3 datepicker-wrapper">
            <span class="input-group-text"
              ><i class="bi bi-calendar-date"></i></span
            >
            <input
              id="editScheduledTime"
              class="form-control"
              bind:value={selectedJob.scheduledTime}
              placeholder="YYYY-MM-DD HH:mm"
            />
          </div>

          <JobStatusSelect bind:bindValue={selectedJob.jobState} />

          {#if $isAdmin}
            <label>Company</label>
            <select class="form-select mb-3" bind:value={selectedJob.companyId}>
              <option disabled selected value="">Select company</option>
              {#each companies as company}
                <option value={company.id}>{company.name}</option>
              {/each}
            </select>
          {:else if $isOwner}
            <input type="hidden" bind:value={selectedJob.companyId} />
          {/if}

          {#if hasAnyRole("admin", "owner")}
            <LocationSelect
              bind:bindValue={selectedJob.originId}
              locations={locations.filter(
                (loc) => loc.companyId === selectedJob.companyId
              )}
              label="Origin"
              id="editOriginId"
            />

            <LocationSelect
              bind:bindValue={selectedJob.destinationId}
              locations={locations.filter(
                (loc) =>
                  loc.companyId === selectedJob.companyId &&
                  loc.id !== selectedJob.originId
              )}
              label="Destination"
              id="editDestinationId"
            />
          {:else if $isFleet}
            <input type="hidden" bind:value={selectedJob.locationId} />
          {/if}

          <label>Payload (kg)</label>
          <input
            type="number"
            class="form-control mb-3"
            bind:value={selectedJob.payloadKg}
            min="1"
            placeholder="Enter payload weight in kg"
          />
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" onclick={() => dispatch("close")}
            >Cancel</button
          >
          <button
            class="btn btn-primary"
            onclick={() => dispatch("save", selectedJob)}>Save</button
          >
        </div>
      </div>
    </div>
  </div>
{/if}

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

  .modal-content {
    padding: 2rem 2rem 1.5rem 2rem;
    border-radius: 1rem;
    background: #343c44;
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

  .form-select {
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: #fff;
    padding: 0.75rem;
    border-radius: 4px;
    appearance: none;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' fill='%2395d4ee' viewBox='0 0 16 16'%3E%3Cpath d='M7.247 11.14 2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z'/%3E%3C/svg%3E");
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

  .form-select,
  .form-select option {
    background-color: #2a2e36 !important;
    color: #fff !important;
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
