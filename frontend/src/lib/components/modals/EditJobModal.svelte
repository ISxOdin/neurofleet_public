<script>
  import LocationSelect from "$lib/components/forms/LocationSelect.svelte";
  import JobStatusSelect from "$lib/components/forms/JobStatusSelect.svelte";
  import flatpickr from "flatpickr";
  import { createEventDispatcher, onMount } from "svelte";

  export let selectedJob;
  export let locations = [];
  const dispatch = createEventDispatcher();

  let flatpickrInstance;

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

          <LocationSelect
            bind:bindValue={selectedJob.originId}
            {locations}
            label="Origin"
            id="editOriginId"
          />

          <LocationSelect
            bind:bindValue={selectedJob.destinationId}
            locations={locations.filter(
              (loc) => loc.id !== selectedJob.originId
            )}
            label="Destination"
            id="editDestinationId"
          />

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
