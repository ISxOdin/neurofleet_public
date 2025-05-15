<script>
  import { createEventDispatcher } from "svelte";
  import CompanySelect from "$lib/components/forms/CompanySelect.svelte";
  import LocationSelect from "$lib/components/forms/LocationSelect.svelte";
  import { isAdmin, hasAnyRole } from "../../../store";

  export let vehicle;
  export let companies = [];
  export let locations = [];
  export let types = [];

  const dispatch = createEventDispatcher();

  $: selectedTypeInfo = types.find((t) => t.name === vehicle.vehicleType);

  function cancel() {
    dispatch("cancel");
  }

  function save() {
    dispatch("save", vehicle);
  }
</script>

<div class="modal-backdrop show"></div>
<div class="modal d-block" tabindex="-1" style="background: rgba(0, 0, 0, 0.5)">
  <div class="modal-dialog modal-lg modal-dialog-centered">
    <div class="modal-content bg-dark text-light">
      <div class="modal-header">
        <h5 class="modal-title">Edit Vehicle</h5>
        <button type="button" class="btn-close" onclick={cancel}></button>
      </div>
      <div class="modal-body">
        <form>
          <div class="row">
            <div class="col">
              <label class="form-label">License Plate</label>
              <input class="form-control" bind:value={vehicle.licensePlate} />
            </div>
            <div class="col">
              <label class="form-label">VIN</label>
              <input class="form-control" bind:value={vehicle.vin} />
            </div>
          </div>

          <div class="row mt-2">
            <div class="mb-3">
              <label class="form-label">Type</label>
              <select class="form-select" bind:value={vehicle.vehicleType}>
                <option disabled selected value={null}>Select type</option>
                {#each types as type}
                  <option value={type.name}>{type.label}</option>
                {/each}
              </select>
            </div>
            <div class="mb-3">
              <label class="form-label">Capacity (kg)</label>
              <input
                class="form-control"
                type="number"
                value={selectedTypeInfo?.capacityKg ?? ""}
                readonly
                disabled
              />
            </div>
          </div>

          <div class="mb-3 mt-2">
            <label class="form-label">Status</label>
            <select class="form-select" bind:value={vehicle.state}>
              <option value="AVAILABLE">Available</option>
              <option value="ON_ROUTE">On Route</option>
              <option value="DROPPING_OFF">Dropping Off</option>
              <option value="INACTIVE">Inactive</option>
              <option value="OUT_OF_SERVICE">Out of service</option>
            </select>
          </div>

          {#if $isAdmin}
            <CompanySelect bind:bindValue={vehicle.companyId} {companies} />
          {/if}

          {#if hasAnyRole("admin", "owner")}
            <LocationSelect
              bind:bindValue={vehicle.locationId}
              locations={locations.filter(
                (loc) => loc.companyId === vehicle.companyId
              )}
            />
            {#if vehicle.companyId && locations.filter((loc) => loc.companyId === vehicle.companyId).length === 0}
              <div class="text-muted mb-3">
                <p style="color: red">
                  No locations available for selected company
                </p>
              </div>
            {/if}
          {/if}
        </form>
      </div>
      <div class="modal-footer">
        <button class="btn btn-secondary" onclick={cancel}>Cancel</button>
        <button class="btn btn-primary" onclick={save}>Save</button>
      </div>
    </div>
  </div>
</div>
