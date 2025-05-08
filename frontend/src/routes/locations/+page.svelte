<script>
  import axios from "axios";
  import { page } from "$app/state";
  import { onMount } from "svelte";
  import { browser } from "$app/environment";
  import { jwt_token, user, isAuthenticated } from "../../store";

  let currentPage = 1;
  let defaultPageSize = 20;
  let nrOfPages = 0;
  let loading = false;
  let apiRoot = "";

  let mySub;
  let myRole;
  let myCompanyId;

  let showEditModal = false;
  let editLocation = null;
  let users = [];
  let userMap = {};
  let locations = [];
  let companies = [];

  onMount(async () => {
    if (browser) {
      apiRoot = window.location.origin;
    }
    const u = $user;
    mySub = u.sub;
    myRole = u.user_roles?.[0] || null;

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

      const myCo = companies.find((c) => c.userIds?.includes(mySub));
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

  async function createLocation(data) {
    if (!myCompanyId) {
      return alert("You don't belong to any company");
    }
    const payload = {
      ...data,
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

  async function submitEdit() {
    try {
      await axios.put(
        `${apiRoot}/api/locations/${editLocation.id}`,
        editLocation,
        { headers: { Authorization: `Bearer ${$jwt_token}` } }
      );
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

{#if showEditModal}
  <div class="modal-backdrop show"></div>
  <div class="modal d-block" tabindex="-1" style="background: rgba(0,0,0,0.5)">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content bg-dark text-light">
        <div class="modal-header">
          <h5 class="modal-title">Edit Location</h5>
          <button class="btn-close btn-close-white" onclick={closeEditModal}
          ></button>
        </div>
        <div class="modal-body">
          <label>Name</label>
          <input class="form-control mb-2" bind:value={editLocation.name} />
          <label>Address</label>
          <input class="form-control mb-2" bind:value={editLocation.address} />
          <label>Fleet Manager</label>
          <select
            class="form-select mb-2"
            bind:value={editLocation.fleetmanagerId}
          >
            <option disabled value="">-- Select Fleet Manager --</option>
            {#each users.filter((u) => u.role === "fleetmanager" && u.companyId === myCompanyId) as fm}
              <option value={fm.user_id}
                >{fm.given_name} {fm.family_name} ({fm.email})</option
              >
            {/each}
          </select>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" onclick={closeEditModal}
            >Cancel</button
          >
          <button class="btn btn-primary" onclick={submitEdit}>Save</button>
        </div>
      </div>
    </div>
  </div>
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
      />
    </div>
  </div>
  <div class="row mb-3">
    <div class="col">
      <label>Address</label><input
        class="form-control"
        bind:value={location.address}
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
  <table class="table table-striped">
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
              onclick={() => openEditModal(loc)}>Edit</button
            >
            <button
              class="btn btn-sm btn-outline-danger"
              onclick={() => deleteLocation(loc.id)}>Delete</button
            >
          </td>
        </tr>
      {/each}
    </tbody>
  </table>

  <nav>
    <ul class="pagination justify-content-center">
      <li class="page-item" class:disabled={currentPage === 1}>
        <button class="page-link" onclick={() => getLocations(currentPage - 1)}
          >&laquo;</button
        >
      </li>
      {#each Array(nrOfPages) as _, i}
        <li class="page-item" class:active={currentPage === i + 1}>
          <button class="page-link" onclick={() => getLocations(i + 1)}
            >{i + 1}</button
          >
        </li>
      {/each}
      <li class="page-item" class:disabled={currentPage === nrOfPages}>
        <button class="page-link" onclick={() => getLocations(currentPage + 1)}
          >&raquo;</button
        >
      </li>
    </ul>
  </nav>
{/if}

<style>
  .page-link {
    box-shadow: none;
  }
</style>
