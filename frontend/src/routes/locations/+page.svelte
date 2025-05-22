<script>
  import axios from "axios";
  import { page } from "$app/state";
  import { onMount } from "svelte";
  import { browser } from "$app/environment";
  import { jwt_token, user, isAuthenticated } from "../../store";
  import EditLocationModal from "$lib/components/modals/EditLocationModal.svelte";
  import CreateLocationModal from "$lib/components/modals/CreateLocationModal.svelte";
  import Pagination from "$lib/components/Pagination.svelte";

  let currentPage = 1;
  let defaultPageSize = 5;
  let nrOfPages = 0;
  let loading = false;
  let apiRoot = "";

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

      const myCo = companies.find((c) => c.userIds?.includes($user.sub));
      myCompanyId = myCo?.id || null;
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
        <th>Lon</th>
        <th>Lat</th>
        <th>ID</th>
        <th>Company</th>
        <th>Fleet Manager</th>
        <th>Actions</th>
      </tr>
    </thead>
    <tbody>
      {#each locations as loc}
        <tr>
          <td>{loc.name}</td>
          <td>{loc.address}</td>
          <td>{loc.longitude}</td>
          <td>{loc.latitude}</td>
          <td>{loc.id}</td>
          <td>{companies.find((c) => c.id === loc.companyId)?.name}</td>
          <td
            >{userMap[loc.fleetmanagerId]?.given_name}
            {userMap[loc.fleetmanagerId]?.family_name}</td
          >
          <td>
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
          </td>
        </tr>
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
  .page-link {
    box-shadow: none;
  }

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

  .modal-backdrop {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
  }

  .modal-content {
    background: #343c44;
    padding: 2rem;
    border-radius: 8px;
    border: 1px solid #95d4ee;
    width: 90%;
    max-width: 500px;
  }

  .modal-content h2 {
    color: white;
    margin-bottom: 1.5rem;
    font-size: 1.4rem;
  }

  .form-group {
    margin-bottom: 1rem;
  }

  .form-group label {
    display: block;
    color: #95d4ee;
    margin-bottom: 0.5rem;
  }

  .form-control {
    width: 100%;
    padding: 0.5rem;
    border: 1px solid #4f5a65;
    border-radius: 4px;
    background: #23272e;
    color: white;
  }

  .modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
    margin-top: 1.5rem;
  }
</style>
