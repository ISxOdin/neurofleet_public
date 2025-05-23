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
    isAuthenticated,
  } from "../../store";
  import { page } from "$app/state";
  import { goto } from "$app/navigation";
  import CompanySelect from "$lib/components/forms/CompanySelect.svelte";
  import LocationSelect from "$lib/components/forms/LocationSelect.svelte";
  import EditVehicleModal from "$lib/components/modals/EditVehicleModal.svelte";
  import Pagination from "$lib/components/Pagination.svelte";
  import CreateVehicleModal from "$lib/components/modals/CreateVehicleModal.svelte";
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
  let defaultPageSize = 5;
  let showCreateModal = false;

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

  function getVehicles(page = currentPage) {
    loading = true;
    axios
      .get(
        api_root +
          `/api/vehicles?pageNumber=${page}&pageSize=${defaultPageSize}`,
        {
          headers: { Authorization: "Bearer " + $jwt_token },
        }
      )
      .then((res) => {
        vehicles = res.data.content;
        nrOfPages = res.data.totalPages;
        currentPage = page;
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

  function createVehicle(vehicle) {
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

  function goToLogin() {
    goto("/");
  }
</script>

{#if $isAuthenticated}
  <div class="vehicles-header">
    <h1 class="text-center">All Vehicles</h1>
    <button class="btn-accent" onclick={() => (showCreateModal = true)}>
      <i class="bi bi-plus-lg"></i> Create Vehicle
    </button>
  </div>

  {#if showCreateModal}
    <CreateVehicleModal
      {types}
      {companies}
      {locations}
      {myCompanyId}
      {myLocationId}
      on:created={(e) => {
        showCreateModal = false;
        getVehicles();
        createVehicle(e.detail);
      }}
      on:cancel={() => (showCreateModal = false)}
    />
  {/if}
  {#if loading}
    <div class="d-flex justify-content-center mt-5">
      <div class="spinner-border" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>
  {:else}
    <table class="vehicles-table">
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
            <td>
              <span class="status-badge status-{v.state.toLowerCase()}">
                {#if v.state === "AVAILABLE"}
                  <i class="bi bi-check-circle-fill"></i>
                {:else if v.state === "ON_ROUTE"}
                  <i class="bi bi-truck"></i>
                {:else if v.state === "DROPPING_OFF"}
                  <i class="bi bi-box-seam"></i>
                {:else if v.state === "INACTIVE"}
                  <i class="bi bi-pause-circle-fill"></i>
                {:else if v.state === "OUT_OF_SERVICE"}
                  <i class="bi bi-x-circle-fill"></i>
                {/if}
                {v.state.replace("_", " ")}
              </span>
            </td>
            <td>{companies.find((c) => c.id === v.companyId)?.name}</td>
            <td>{locations.find((l) => l.id === v.locationId)?.name}</td>
            <td>
              <div class="dropdown">
                <button
                  class="btn btn-sm btn-outline-light"
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
                    <a class="dropdown-item" onclick={() => openEdit(v)}>Edit</a
                    >
                  </li>
                  <li>
                    <a
                      class="dropdown-item text-danger"
                      onclick={() => deleteVehicle(v.id)}>Delete</a
                    >
                  </li>
                  <li><hr class="dropdown-divider" /></li>
                </ul>
              </div>
            </td>
          </tr>
        {/each}
      </tbody>
    </table>

    <Pagination
      {currentPage}
      totalPages={nrOfPages}
      onPageChange={getVehicles}
    />
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
{:else}
  <div class="container mt-5 text-center" in:fade>
    <div class="not-authenticated">
      <i class="bi bi-lock-fill fa-3x mb-3"></i>
      <p>You are not logged in.</p>
      <button class="btn btn-primary mt-3" onclick={goToLogin}>
        Go to Login
      </button>
    </div>
  </div>
{/if}

<style>
  .vehicles-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;
    margin-top: 2rem;
    background-color: #343c44;
    padding: 1rem;
    border-radius: 0.5rem;
    border: 1px solid #95d4ee;
  }

  .vehicles-header h1 {
    color: white;
    font-size: 1.4rem;
    margin: 0;
  }

  .btn-accent {
    background: #95d4ee;
    color: #23272e;
    border: none;
    border-radius: 4px;
    padding: 0.6rem 1.2rem;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    cursor: pointer;
    transition: background 0.2s;
  }

  .btn-accent:hover {
    background: #7bc4e6;
  }

  .create-form {
    background: #343c44;
    padding: 1.5rem;
    border-radius: 0.5rem;
    border: 1px solid #95d4ee;
    margin-bottom: 2rem;
  }

  .vehicles-table {
    width: 100%;
    border-collapse: separate;
    border-spacing: 0;
    background: #4f5a65;
    color: #fff;
    border-radius: 8px;
    border: 1px solid #95d4ee;
    overflow: hidden;
    margin-bottom: 1.5rem;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }

  .vehicles-table th,
  .vehicles-table td {
    padding: 1rem 0.8rem;
    text-align: left;
    vertical-align: middle;
  }

  .vehicles-table th {
    color: #95d4ee;
    font-weight: 600;
    background: #343c44;
    border-bottom: 2px solid #343c44;
  }

  .vehicles-table tbody tr {
    transition: background 0.15s;
  }

  .vehicles-table tbody tr:nth-child(even) {
    background: #343c44;
  }

  .vehicles-table tbody tr:nth-child(odd) {
    background: #4f5a65;
  }

  .vehicles-table tbody tr:hover {
    background: rgba(149, 212, 238, 0.2);
  }

  .dropdown {
    position: relative;
    z-index: 1000;
  }

  .form-label {
    color: #fff;
    margin-bottom: 0.5rem;
  }

  .form-control,
  .form-select {
    background-color: #4f5a65;
    border: 1px solid #95d4ee;
    color: #fff;
  }

  .form-control:focus,
  .form-select:focus {
    background-color: #4f5a65;
    border-color: #95d4ee;
    color: #fff;
    box-shadow: 0 0 0 0.25rem rgba(149, 212, 238, 0.25);
  }

  .form-control::placeholder {
    color: #95d4ee;
    opacity: 0.7;
  }

  .form-control:disabled {
    background-color: #343c44;
    opacity: 0.7;
  }

  @media (max-width: 768px) {
    .vehicles-header {
      flex-direction: column;
      gap: 1rem;
      text-align: center;
    }

    .vehicles-table {
      display: block;
      overflow-x: auto;
    }
  }

  .status-badge {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.35rem 0.75rem;
    border-radius: 1rem;
    font-size: 0.875rem;
    font-weight: 500;
  }

  .status-badge i {
    font-size: 1rem;
  }

  .status-available {
    background-color: rgba(40, 167, 69, 0.5);
    color: #3dbe5b;
  }

  .status-on_route {
    background-color: rgba(0, 123, 255, 0.5);
    color: #75b6fc;
  }

  .status-dropping_off {
    background-color: rgba(111, 66, 193, 0.5);
    color: #af83ff;
  }

  .status-inactive {
    background-color: rgba(108, 117, 125, 0.5);
    color: #8d9194;
  }

  .status-out_of_service {
    background-color: rgba(220, 53, 69, 0.5);
    color: #dc3545;
  }
</style>
