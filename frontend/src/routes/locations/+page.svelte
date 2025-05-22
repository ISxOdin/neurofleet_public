<script>
  import axios from "axios";
  import { page } from "$app/state";
  import { onMount } from "svelte";
  import { browser } from "$app/environment";
  import { jwt_token, user, isAuthenticated } from "../../store";
  import EditLocationModal from "$lib/components/modals/EditLocationModal.svelte";
  import CreateLocationModal from "$lib/components/modals/CreateLocationModal.svelte";
  import Pagination from "$lib/components/Pagination.svelte";
  import { findUserCompany } from "$lib/utils";
  import { VITE_GOOGLE_MAPS_API_KEY } from "$env/static/public";

  let currentPage = 1;
  let defaultPageSize = 5;
  let nrOfPages = 0;
  let loading = false;
  let apiRoot = "";
  let expandedRowId = null;

  let myCompanyId;

  let showEditModal = false;
  let showCreateModal = false;
  let editLocation = null;
  let users = [];
  let userMap = {};
  let locations = [];
  let companies = [];
  let location = {
    name: "",
    address: "",
  };

  onMount(async () => {
    if (browser) {
      apiRoot = window.location.origin;
    }
    await getCompanies();
    await getUsers();
    await getLocations();
  });

  async function getCompanies() {
    loading = true;
    try {
      const response = await axios.get(`${apiRoot}/api/companies`, {
        headers: { Authorization: `Bearer ${$jwt_token}` },
      });
      companies = response.data.content || response.data;
      myCompanyId = findUserCompany(companies, $user.sub);
    } catch (err) {
      console.error("Could not load companies", err);
      alert("Could not load companies");
    } finally {
      loading = false;
    }
  }

  async function getUsers() {
    loading = true;
    try {
      const response = await axios.get(`${apiRoot}/api/auth0/users`, {
        headers: { Authorization: `Bearer ${$jwt_token}` },
      });
      users = response.data.map((u) => ({
        ...u,
        role: u.roles?.[0] || "–",

        companyId:
          companies.find((c) => c.userIds?.includes(u.user_id))?.id || null,
      }));
      userMap = users.reduce((acc, u) => ({ ...acc, [u.user_id]: u }), {});
    } catch (err) {
      console.error("Could not load users", err);
      alert("Could not load users");
    } finally {
      loading = false;
    }
  }

  async function getLocations(page = currentPage) {
    loading = true;
    try {
      const response = await axios.get(
        `${apiRoot}/api/locations?pageNumber=${page}&pageSize=${defaultPageSize}`,
        { headers: { Authorization: `Bearer ${$jwt_token}` } }
      );
      locations = response.data.content;
      nrOfPages = response.data.totalPages;
      currentPage = page;
    } catch (err) {
      console.error("Could not get locations", err);
      alert("Could not get locations");
    } finally {
      loading = false;
    }
  }

  async function createLocation(location) {
    if (!myCompanyId) {
      return alert("You don't belong to any company");
    }
    const payload = {
      ...location,
      companyId: myCompanyId,
    };
    try {
      await axios.post(`${apiRoot}/api/locations`, payload, {
        headers: { Authorization: `Bearer ${$jwt_token}` },
      });
      alert("Location created");
      showCreateModal = false;
      await getLocations();
    } catch (err) {
      console.error("Could not create location", err);
      alert("Could not create location");
    }
  }

  function openEditModal(loc) {
    editLocation = { ...loc };
    showEditModal = true;
  }

  function closeEditModal() {
    showEditModal = false;
    editLocation = null;
  }

  async function submitEdit(updated) {
    try {
      await axios.put(`${apiRoot}/api/locations/${updated.id}`, updated, {
        headers: { Authorization: `Bearer ${$jwt_token}` },
      });
      alert("Location updated successfully");
      closeEditModal();
      await getLocations();
    } catch (err) {
      console.error("Could not update location", err);
      alert("Could not update location");
    }
  }

  async function deleteLocation(id) {
    if (!confirm("Delete this location?")) return;
    try {
      await axios.delete(`${apiRoot}/api/locations/${id}`, {
        headers: { Authorization: `Bearer ${$jwt_token}` },
      });
      alert("Location deleted");
      await getLocations();
    } catch (err) {
      console.error("Could not delete location", err);
      alert("Could not delete location");
    }
  }

  function toggleRow(locationId, event) {
    // Don't toggle if clicking on the dropdown
    if (event.target.closest(".dropdown")) return;
    expandedRowId = expandedRowId === locationId ? null : locationId;
  }
</script>

<!-- Create Form -->
<div class="companies-header">
  <h1 class="text-center">All Locations</h1>
  <button class="btn-accent" onclick={() => (showCreateModal = true)}>
    <i class="bi bi-plus-lg"></i> Create Location
  </button>
</div>

{#if showCreateModal}
  <CreateLocationModal
    on:created={(e) => createLocation(e.detail)}
    on:cancel={() => (showCreateModal = false)}
  />
{/if}

{#if showEditModal && editLocation}
  <EditLocationModal
    location={editLocation}
    {users}
    companyId={myCompanyId}
    on:cancel={closeEditModal}
    on:save={(e) => submitEdit(e.detail)}
  />
{/if}

{#if loading}
  <div class="d-flex justify-content-center my-4">
    <div class="spinner-border" role="status">
      <span class="visually-hidden">Loading...</span>
    </div>
  </div>
{:else}
  <table class="companies-table">
    <thead>
      <tr>
        <th>Name</th>
        <th>Address</th>
        <th>ID</th>
        <th>Company</th>
        <th>Fleet Manager</th>
        <th>Actions</th>
      </tr>
    </thead>
    <tbody>
      {#each locations as loc}
        <tr
          class="location-row"
          class:expanded={expandedRowId === loc.id}
          onclick={(e) => toggleRow(loc.id, e)}
        >
          <td>
            <div class="location-name">{loc.name}</div>
            {#if loc.latitude && loc.longitude}
              <div class="coordinates">
                <i class="bi bi-geo-alt"></i>
                {loc.latitude}, {loc.longitude}
              </div>
            {/if}
          </td>
          <td>{loc.address}</td>
          <td>{loc.id}</td>
          <td>{companies.find((c) => c.id === loc.companyId)?.name}</td>
          <td
            >{userMap[loc.fleetmanagerId]?.given_name}
            {userMap[loc.fleetmanagerId]?.family_name}</td
          >
          <td>
            <div class="dropdown">
              <a
                href="#"
                class="d-flex align-items-center text-white text-decoration-none dropdown-toggle"
                id="userDropdown"
                data-bs-toggle="dropdown"
                aria-expanded="false"
              >
                <button class="btn btn-sm btn-outline-light">
                  <i class="bi bi-gear-fill"></i> Edit
                </button>
              </a>
              <ul
                class="dropdown-menu dropdown-menu-dark dropdown-menu-end text-small shadow"
                aria-labelledby="userDropdown"
              >
                <li>
                  <a class="dropdown-item" onclick={() => openEditModal(loc)}
                    >Edit</a
                  >
                </li>
                <li>
                  <a
                    class="dropdown-item text-danger"
                    onclick={() => deleteLocation(loc.id)}>Delete</a
                  >
                </li>
                <li><hr class="dropdown-divider" /></li>
              </ul>
            </div>
          </td>
        </tr>
        {#if expandedRowId === loc.id}
          <tr class="map-row">
            <td colspan="8">
              {#if loc.address}
                <iframe
                  width="100%"
                  height="450"
                  style="border:0"
                  loading="lazy"
                  allowfullscreen
                  src="https://www.google.com/maps/embed/v1/search?q={encodeURIComponent(
                    loc.address
                  )}&key={VITE_GOOGLE_MAPS_API_KEY}"
                ></iframe>
              {:else}
                <div class="text-center p-3">
                  <i class="bi bi-map"></i> No address available for this location
                </div>
              {/if}
            </td>
          </tr>
        {/if}
      {/each}
    </tbody>
  </table>

  <Pagination
    {currentPage}
    totalPages={nrOfPages}
    onPageChange={getLocations}
  />
{/if}

<style>
  .companies-header {
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

  .companies-header h1 {
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

  .btn-secondary {
    background: #4f5a65;
    color: #fff;
    border: none;
    border-radius: 4px;
    padding: 0.6rem 1.2rem;
    font-weight: 600;
    cursor: pointer;
    transition: background 0.2s;
  }

  .btn-secondary:hover {
    background: #343c44;
  }

  .companies-table {
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

  .companies-table th,
  .companies-table td {
    padding: 1rem 0.8rem;
    text-align: left;
    vertical-align: middle;
  }

  .companies-table th {
    color: #95d4ee;
    font-weight: 600;
    background: #343c44;
    border-bottom: 2px solid #343c44;
  }

  .companies-table tbody tr {
    transition: background 0.15s;
  }

  .companies-table tbody tr:nth-child(even) {
    background: #343c44;
  }

  .companies-table tbody tr:nth-child(odd) {
    background: #4f5a65;
  }

  .companies-table tbody tr:hover {
    background: rgba(149, 212, 238, 0.2);
  }

  .location-row {
    cursor: pointer;
    transition: background-color 0.2s;
  }

  .location-row:hover {
    background-color: rgba(149, 212, 238, 0.1);
  }

  .location-row.expanded {
    background-color: rgba(149, 212, 238, 0.15);
  }

  .map-row td {
    padding: 0 !important;
    background-color: #343c44;
  }

  .map-row iframe {
    border-radius: 0 0 8px 8px;
  }

  .dropdown {
    position: relative;
    z-index: 1000;
  }

  .location-name {
    font-weight: 500;
    margin-bottom: 0.25rem;
  }

  .coordinates {
    font-size: 0.85rem;
    color: #95d4ee;
    display: flex;
    align-items: center;
    gap: 0.25rem;
  }

  .coordinates i {
    font-size: 0.8rem;
    opacity: 0.8;
  }
</style>
