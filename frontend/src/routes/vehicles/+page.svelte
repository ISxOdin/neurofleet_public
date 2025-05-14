<script>
  import { onMount } from "svelte";
  import axios from "axios";
  import {
    jwt_token,
    user,
    isAdmin,
    isOwner,
    isFleet,
    hasAnyRole,
  } from "../../store";
  import { page } from "$app/state";
  import CompanySelect from "$lib/components/forms/CompanySelect.svelte";
  import LocationSelect from "$lib/components/forms/LocationSelect.svelte";
  import EditVehicleModal from "$lib/components/modals/EditVehicleModal.svelte";

  const api_root = page.url.origin;

  let vehicle = {
    licensePlate: "",
    vin: "",
    vehicleType: "",
    locationId: "",
    companyId: "",
  };

  let currentPage = 1;
  let nrOfPages = 0;
  let defaultPageSize = 20;

  let myCompanyId = null;
  let myLocationId = null;

  let vehicles = [];
  let companies = [];
  let locations = [];
  let types = [];

  let loading = false;
  let showModal = false;
  let selectedVehicle = null;

  const sub = encodeURIComponent($user.sub);

  $: selectedTypeInfo = types.find((t) => t.name === vehicle.vehicleType);
  $: selectedEditTypeInfo = types.find(
    (t) => t.name === selectedVehicle?.vehicleType
  );
  $: vinValid = /^[A-HJ-NPR-Z0-9]{17}$/.test(vehicle.vin);

  onMount(async () => {
    await getCompanies();
    await getLocations();
    await getVehicles();
    await getTypes();

    if (isOwner) {
      vehicle.companyId = myCompanyId;
    } else if (isFleet) {
      vehicle.companyId = myCompanyId;
      vehicle.locationId = myLocationId;
    }
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

  function getVehicles() {
    loading = true;
    axios
      .get(api_root + "/api/vehicles", {
        headers: { Authorization: "Bearer " + $jwt_token },
      })
      .then((res) => {
        vehicles = res.data.content;
      })
      .catch(() => alert("Could not load vehicles"))
      .finally(() => (loading = false));
  }

  function getTypes() {
    axios
      .get(api_root + "/api/vehicles/types", {
        headers: { Authorization: "Bearer " + $jwt_token },
      })
      .then((res) => {
        types = res.data;
      })
      .catch(() => alert("Could not load vehicle types"));
  }

  function createVehicle() {
    const payload = {
      ...vehicle,
    };

    axios
      .post(api_root + "/api/vehicles", payload, {
        headers: {
          Authorization: "Bearer " + $jwt_token,
          "Content-Type": "application/json",
        },
      })
      .then(() => {
        alert("Vehicle created");
        getVehicles();
      })
      .catch(() => {
        alert("Vehicle creation failed");
      });
  }

  function deleteVehicle(id) {
    if (!confirm("Are you sure you want to delete this vehicle?")) return;

    axios
      .delete(api_root + "/api/vehicles/" + id, {
        headers: {
          Authorization: "Bearer " + $jwt_token,
        },
      })
      .then(() => {
        alert("Vehicle deleted");
        getVehicles();
      })
      .catch(() => {
        alert("Vehicle deletion failed");
      });
  }

  function openEdit(v) {
    selectedVehicle = { ...v };
    showModal = true;
  }

  function saveEdit(updatedVehicle) {
    axios
      .put(`${api_root}/api/vehicles/${updatedVehicle.id}`, updatedVehicle, {
        headers: {
          Authorization: "Bearer " + $jwt_token,
          "Content-Type": "application/json",
        },
      })
      .then(() => {
        alert("Vehicle updated");
        closeModal();
        getVehicles();
      })
      .catch(() => {
        alert("Update failed");
      });
  }

  function closeModal() {
    showModal = false;
    selectedVehicle = null;
  }

  function isValidVIN(vin) {
    const vinRegex = /^[A-HJ-NPR-Z0-9]{17}$/;
    return vinRegex.test(vin);
  }
</script>

<h1>Create Vehicle</h1>
<form onsubmit={createVehicle}>
  <div class="mb-3">
    <label class="form-label" for="licencePlate">License Plate</label>
    <input
      class="form-control"
      bind:value={vehicle.licensePlate}
      required
      placeholder="ZH 1234"
    />
  </div>
  <div class="mb-3">
    <label class="form-label" for="vin">VIN</label>
    <input
      class="form-control"
      bind:value={vehicle.vin}
      maxlength="17"
      required
      style="border-color: {vehicle.vin && !vinValid ? 'red' : ''}"
      placeholder="1HGCM82633A123456"
    />
    {#if vehicle.vin && !vinValid}
      <small style="color: red">
        VIN must be 17 characters, no I, O or Q
      </small>
    {/if}
  </div>

  <div class="mb-3">
    <label class="form-label" for="type">Type</label>
    <select class="form-select" bind:value={vehicle.vehicleType}>
      <option disabled selected value="">Select type</option>
      {#each types as type}
        <option value={type.name}>{type.label}</option>
      {/each}
    </select>
  </div>
  <div class="mb-3">
    <label class="form-label" for="capacity">Capacity (kg)</label>
    <input
      class="form-control"
      type="number"
      value={selectedTypeInfo?.capacityKg ?? ""}
      readonly
      disabled
    />
  </div>

  {#if isAdmin}
    <CompanySelect bind:bindValue={vehicle.companyId} {companies} />
  {/if}

  {#if hasAnyRole("admin", "owner")}
    <LocationSelect
      bind:bindValue={vehicle.locationId}
      locations={locations.filter((loc) => loc.companyId === vehicle.companyId)}
    />
    {#if vehicle.companyId && locations.filter((loc) => loc.companyId === vehicle.companyId).length === 0}
      <div class="text-muted mb-3">
        <p style="color: red">No locations available for selected company</p>
      </div>
    {/if}
  {/if}

  <button type="submit" class="btn btn-primary">Create</button>
</form>
<hr />
<h2>Vehicles</h2>

{#if loading}
  <div class="d-flex justify-content-center mt-5">
    <div class="spinner-border" role="status">
      <span class="visually-hidden">Loading...</span>
    </div>
  </div>
{:else}
  <table class="table mt-3">
    <thead>
      <tr>
        <th>License Plate</th>
        <th>VIN</th>
        <th>Type</th>
        <th>Capacity</th>
        <th>Status</th>
        <th>Company</th>
        <th>Location</th>
        <th>Actions</th>
      </tr>
    </thead>
    <tbody>
      {#each vehicles as v}
        <tr>
          <td>{v.licensePlate}</td>
          <td>{v.vin}</td>
          <td>{v.vehicleType}</td>
          <td>{v.capacity}</td>
          <td>{v.state}</td>
          <td>{companies.find((c) => c.id === v.companyId)?.name}</td>
          <td>{locations.find((l) => l.id === v.locationId)?.name}</td>
          <td>
            <div class="dropdown">
              <button
                class="btn btn-sm btn-outline-secondary"
                type="button"
                data-bs-toggle="dropdown"
              >
                <i class="bi bi-gear-fill"></i> Edit
              </button>
              <ul
                class="dropdown-menu dropdown-menu-dark dropdown-menu-end text-small shadow"
                aria-labelledby="userDropdown"
              >
                <li>
                  <a class="dropdown-item" onclick={() => openEdit(v)}>Edit</a>
                </li>
                <li>
                  <a
                    class="dropdown-item text-danger"
                    onclick={() => deleteVehicle(v.id)}>Delete</a
                  >
                </li>
              </ul>
            </div>
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
{/if}

{#if showModal && selectedVehicle}
  <EditVehicleModal
    vehicle={selectedVehicle}
    {companies}
    {locations}
    {types}
    on:cancel={closeModal}
    on:save={(e) => saveEdit(e.detail)}
  />
{/if}
