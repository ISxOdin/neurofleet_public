<script>
  import { onMount } from "svelte";
  import axios from "axios";
  import { jwt_token, user } from "../../store";
  import { page } from "$app/state";

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

  const isAdmin = $user.user_roles.includes("admin");
  const isOwner = $user.user_roles.includes("owner");
  const isFleet = $user.user_roles.includes("fleetmanager");
  const sub = encodeURIComponent($user.sub);

  $: selectedTypeInfo = types.find((t) => t.name === vehicle.vehicleType);
  $: selectedEditTypeInfo = types.find((t) => t.name === selectedVehicle?.vehicleType);

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

  function saveEdit() {
    const payload = { ...selectedVehicle };

    axios
      .put(api_root + "/api/vehicles/" + selectedVehicle.id, payload, {
        headers: {
          Authorization: "Bearer " + $jwt_token,
          "Content-Type": "application/json",
        },
      })
      .then(() => {
        alert("Vehicle updated");
        showModal = false;
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
</script>

<h1>Create Vehicle</h1>
<form onsubmit={createVehicle}>
  <div class="mb-3">
    <label>License Plate</label><input
      class="form-control"
      bind:value={vehicle.licensePlate}
      required
    />
  </div>
  <div class="mb-3">
    <label>VIN</label><input
      class="form-control"
      bind:value={vehicle.vin}
      required
    />
  </div>
  <div class="mb-3">
    <label>Type</label>
    <select class="form-select" bind:value={vehicle.vehicleType}>
      <option disabled selected value={null}>Select type</option>
      {#each types as type}
        <option value={type.name}>{type.label}</option>
      {/each}
    </select>
  </div>
  <div class="mb-3">
    <label>Capacity (kg)</label>
    <input
      class="form-control"
      type="number"
      value={selectedTypeInfo?.capacityKg ?? ""}
      readonly
      disabled
    />
  </div>

  {#if isAdmin}
    <div class="mb-3">
      <label>Company</label>
      <select class="form-select" bind:value={vehicle.companyId}>
        <option disabled selected value={null}>Select company</option>
        {#each companies as company}
          <option value={company.id}>{company.name}</option>
        {/each}
      </select>
    </div>
  {/if}

  {#if isAdmin || isOwner}
    <div class="mb-3">
      <label>Location</label>
      <select class="form-select" bind:value={vehicle.locationId}>
        <option disabled selected value={null}>Select location</option>
        {#each locations as location}
          <option value={location.id}>{location.name}</option>
        {/each}
      </select>
    </div>
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

{#if showModal}
  <div
    class="modal fade show d-block"
    tabindex="-1"
    style="background: rgba(0, 0, 0, 0.5);"
  >
    <div class="modal-dialog modal-lg modal-dialog-centered">
      <div class="modal-content bg-dark text-light">
        <div class="modal-header">
          <h5 class="modal-title">Edit Vehicle</h5>
          <button type="button" class="btn-close" onclick={closeModal}></button>
        </div>
        <div class="modal-body">
          <form>
            <div class="row">
              <div class="col">
                <label>License Plate</label>
                <input
                  class="form-control"
                  bind:value={selectedVehicle.licensePlate}
                />
              </div>
              <div class="col">
                <label>VIN</label><input
                  class="form-control"
                  bind:value={selectedVehicle.vin}
                />
              </div>
            </div>
            <div class="row mt-2">
              <div class="mb-3">
                <label>Type</label>
                <select class="form-select" bind:value={selectedVehicle.vehicleType}>
                  <option disabled selected value={null}>Select type</option>
                  {#each types as type}
                    <option value={type.name}>{type.label}</option>
                  {/each}
                </select>
              </div>
              <div class="mb-3">
                <label>Capacity (kg)</label>
                <input
                  class="form-control"
                  type="number"
                  value={selectedEditTypeInfo?.capacityKg ?? ""}
                  readonly
                  disabled
                />
              </div>
            </div>
            <div class="row mt-2">
              <div class="col">
                <label>Status</label>
                <select class="form-select" bind:value={selectedVehicle.state}>
                  <option value="AVAILABLE">Available</option>
                  <option value="ON_ROUTE">On Route</option>
                  <option value="DROPPING_OFF">Dropping Off</option>
                  <option value="INACTIVE">Inactive</option>
                  <option value="OUT_OF_SERVICE">Out of service</option>
                </select>
              </div>
              {#if isAdmin}
                <div class="mb-3">
                  <label>Company</label>
                  <select
                    class="form-select"
                    bind:value={selectedVehicle.companyId}
                  >
                    <option disabled selected value="">Select company</option>
                    {#each companies as company}
                      <option value={company.id}>{company.name}</option>
                    {/each}
                  </select>
                </div>
              {/if}

              {#if isAdmin || isOwner}
                <div class="mb-3">
                  <label>Location</label>
                  <select
                    class="form-select"
                    bind:value={selectedVehicle.locationId}
                  >
                    <option disabled selected value="">Select location</option>
                    {#each locations as location}
                      <option value={location.id}>{location.name}</option>
                    {/each}
                  </select>
                </div>
              {/if}
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" onclick={closeModal}>Cancel</button>
          <button class="btn btn-primary" onclick={saveEdit}>Save</button>
        </div>
      </div>
    </div>
  </div>
{/if}
