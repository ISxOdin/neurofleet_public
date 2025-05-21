<script>
  import axios from "axios";
  import { page } from "$app/state";
  import { onMount } from "svelte";
  import { browser } from "$app/environment";
  import { jwt_token, user, isAuthenticated } from "../../store";
  import EditLocationModal from "$lib/components/modals/EditLocationModal.svelte";
  import Pagination from "$lib/components/Pagination.svelte";

  let currentPage = 1;
  let defaultPageSize = 20;
  let nrOfPages = 0;
  let loading = false;
  let apiRoot = "";

  let myCompanyId;

  let showEditModal = false;
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

{#if showEditModal && editLocation}
  <EditLocationModal
    location={editLocation}
    {users}
    companyId={myCompanyId}
    on:cancel={closeEditModal}
    on:save={(e) => submitEdit(e.detail)}
  />
{/if}

<!-- Create Form -->
<h1 class="mt-3 text-center">Create Location</h1>
<form
  onsubmit={() =>
    createLocation({ name: location.name, address: location.address })}
  class="mb-5"
>
  <div class="row mb-3">
    <div class="col">
      <label>Name</label><input
        class="form-control"
        bind:value={location.name}
        placeholder="Zurich HQ"
      />
    </div>
  </div>
  <div class="row mb-3">
    <div class="col">
      <label>Address</label><input
        class="form-control"
        bind:value={location.address}
        placeholder="Bahnhofstrasse 1, 8001 Zürich, Switzerland"
      />
    </div>
  </div>
  <button type="submit" class="btn btn-primary">Submit</button>
</form>

<!-- Locations Table -->
<h1 class="text-center">All Locations</h1>
{#if loading}
  <div class="d-flex justify-content-center my-4">
    <div class="spinner-border" role="status">
      <span class="visually-hidden">Loading...</span>
    </div>
  </div>
{:else}
  <table class="table table-hover">
    <thead>
      <tr
        ><th>Name</th><th>Address</th><th>Lon</th><th>Lat</th><th>ID</th><th
          >Company</th
        ><th>Fleet Manager</th><th></th></tr
      >
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
                <a
                  class="dropdown-item"
                  onclick={() => openEditModal(loc, loc.id)}>Edit</a
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
</style>
