<script>
  import { createEventDispatcher } from "svelte";
  import { isAuthenticated, hasAnyRole, isAdmin } from "../../../store";
  const dispatch = createEventDispatcher();

  export let types = [];
  export let companies = [];
  export let locations = [];
  export let myCompanyId = null;
  export let myLocationId = null;

  let vehicle = {
    licensePlate: "",
    vin: "",
    vehicleType: "",
    locationId: "",
    companyId: "",
  };

  $: selectedTypeInfo = types.find((t) => t.name === vehicle.vehicleType);
  $: vinValid = /^[A-HJ-NPR-Z0-9]{17}$/.test(vehicle.vin);

  // Set default company and location based on user role
  if ($isAdmin) {
    // Admin can select any company/location
  } else if (hasAnyRole("owner")) {
    vehicle.companyId = myCompanyId;
  } else if (hasAnyRole("fleetmanager")) {
    vehicle.companyId = companies.find(
      (c) => c.id === locations.find((l) => l.id === myLocationId)?.companyId
    )?.id;
    vehicle.locationId = myLocationId;
  }

  function handleSubmit() {
    if (!vinValid) return;
    dispatch("created", vehicle);
    vehicle = {
      licensePlate: "",
      vin: "",
      vehicleType: "",
      locationId: "",
      companyId: "",
    };
  }

  function handleCancel() {
    dispatch("cancel");
    vehicle = {
      licensePlate: "",
      vin: "",
      vehicleType: "",
      locationId: "",
      companyId: "",
    };
  }
</script>

<div class="modal-backdrop show"></div>
<div class="modal d-block" tabindex="-1" style="background:rgba(0,0,0,0.5)">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content bg-dark text-light">
      <div class="modal-header">
        <h5 class="modal-title">Create Vehicle</h5>
        <button class="btn-close btn-close-white" onclick={handleCancel}
        ></button>
      </div>
      <div class="modal-body">
        <label>License Plate</label>
        <input
          class="form-control mb-3"
          bind:value={vehicle.licensePlate}
          placeholder="ZH 1234"
          required
        />

        <label>VIN</label>
        <input
          class="form-control mb-2"
          bind:value={vehicle.vin}
          maxlength="17"
          required
          style="border-color: {vehicle.vin && !vinValid ? 'red' : ''}"
          placeholder="1HGCM82633A123456"
        />
        {#if vehicle.vin && !vinValid}
          <small style="color: red" class="mb-3 d-block">
            VIN must be 17 characters, no I, O or Q
          </small>
        {/if}

        <label>Type</label>
        <select
          class="form-select mb-3"
          bind:value={vehicle.vehicleType}
          style="background-color: rgba(255, 255, 255, 0.1); color: #fff; border: 1px solid rgba(255, 255, 255, 0.2);"
        >
          <option disabled selected value="">Select type</option>
          {#each types as type}
            <option value={type.name}>{type.label}</option>
          {/each}
        </select>

        <label>Capacity (kg)</label>
        <input
          class="form-control mb-3"
          type="number"
          value={selectedTypeInfo?.capacityKg ?? ""}
          readonly
          disabled
        />

        {#if isAdmin}
          <label>Company</label>
          <select
            class="form-select mb-3"
            bind:value={vehicle.companyId}
            style="background-color: rgba(255, 255, 255, 0.1); color: #fff; border: 1px solid rgba(255, 255, 255, 0.2);"
          >
            <option disabled selected value="">Select company</option>
            {#each companies as company}
              <option value={company.id}>{company.name}</option>
            {/each}
          </select>
        {/if}

        {#if hasAnyRole("admin", "owner")}
          <label>Location</label>
          <select
            class="form-select mb-3"
            bind:value={vehicle.locationId}
            style="background-color: rgba(255, 255, 255, 0.1); color: #fff; border: 1px solid rgba(255, 255, 255, 0.2);"
            disabled={!vehicle.companyId}
          >
            <option disabled selected value="">Select location</option>
            {#each locations.filter((loc) => loc.companyId === vehicle.companyId) as location}
              <option value={location.id}>{location.name}</option>
            {/each}
          </select>
          {#if vehicle.companyId && locations.filter((loc) => loc.companyId === vehicle.companyId).length === 0}
            <small style="color: red" class="mb-3 d-block">
              No locations available for selected company
            </small>
          {/if}
        {/if}
      </div>
      <div class="modal-footer">
        <button class="btn btn-secondary" onclick={handleCancel}>Cancel</button>
        <button class="btn btn-primary" onclick={handleSubmit}>Create</button>
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
  }

  .form-select:focus {
    background: rgba(255, 255, 255, 0.15);
    border-color: #95d4ee;
    color: #fff;
    box-shadow: none;
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

  .form-select {
    width: 100%;
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
    background-color: rgba(255, 255, 255, 0.15);
    border-color: #95d4ee;
    color: #fff;
    box-shadow: none;
  }

  .form-select:disabled {
    background-color: rgba(255, 255, 255, 0.05);
    opacity: 0.7;
    cursor: not-allowed;
  }

  .form-select option {
    background-color: #343c44;
    color: #fff;
  }
</style>
