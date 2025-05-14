<script>
  import LocationSelect from "$lib/components/forms/LocationSelect.svelte";
  import VehicleSelect from "$lib/components/forms/VehicleSelect.svelte";
  import flatpickr from "flatpickr";
  import { createEventDispatcher, onMount } from "svelte";

  export let selectedJob;
  export let locations = [];
  export let vehicles = [];
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
  <div
    class="modal fade show d-block"
    tabindex="-1"
    style="background: rgba(0, 0, 0, 0.5);"
  >
    <div class="modal-dialog modal-lg modal-dialog-centered">
      <div class="modal-content bg-dark text-light">
        <div class="modal-header">
          <h5 class="modal-title">Edit Job</h5>
          <button
            type="button"
            class="btn-close"
            onclick={() => dispatch("close")}
          ></button>
        </div>
        <div class="modal-body">
          <form>
            <div class="mb-3">
              <label>Description</label>
              <input
                class="form-control"
                bind:value={selectedJob.description}
              />
            </div>

            <div class="mb-3">
              <label>Scheduled Time</label>
              <input
                id="editScheduledTime"
                class="form-control"
                bind:value={selectedJob.scheduledTime}
              />
            </div>

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

            <VehicleSelect
              bind:bindValue={selectedJob.vehicleId}
              vehicles={vehicles.filter(
                (v) => v.locationId === selectedJob.originId
              )}
            />
          </form>
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
