<script>
  import axios from "axios";
  import { onMount } from "svelte";
  import { browser } from "$app/environment";
  import { jwt_token } from "../../store";
  import EditCompanyModal from "$lib/components/modals/EditCompanyModal.svelte";

  let apiRoot = "";
  let users = [];
  let userMap = {};

  let companies = [];
  let currentPage = 1;
  let totalPages = 0;
  const pageSize = 20;
  let loading = false;

  let showEditModal = false;
  let editCompany = null;

  onMount(async () => {
    if (browser) apiRoot = window.location.origin;
    await getUsers();
    await getCompanies();
  });

  async function getUsers() {
    try {
      loading = true;
      const { data } = await axios.get(`${apiRoot}/api/auth0/users`, {
        headers: { Authorization: `Bearer ${$jwt_token}` },
      });
      users = data.map((u) => ({
        id: u.user_id,
        given_name: u.given_name || "",
        family_name: u.family_name || "",
        email: u.email,
      }));
      users.sort((a, b) => a.family_name.localeCompare(b.family_name));
      userMap = Object.fromEntries(users.map((u) => [u.id, u]));
    } catch (error) {
      console.error("Failed to load users", error);
      alert("Failed to load users");
    } finally {
      loading = false;
    }
  }

  // Paginated companies
  async function getCompanies(page = currentPage) {
    loading = true;
    try {
      const url = `${apiRoot}/api/companies?pageNumber=${page}&pageSize=${pageSize}`;
      const { data } = await axios.get(url, {
        headers: { Authorization: `Bearer ${$jwt_token}` },
      });
      companies = data.content;
      totalPages = data.totalPages;
      currentPage = page;
    } catch (error) {
      console.error("Could not get companies", error);
      alert("Could not get companies");
    } finally {
      loading = false;
    }
  }

  function changePage(page) {
    if (page < 1 || page > totalPages) return;
    getCompanies(page);
  }

  // Create new company
  let newCompany = { name: "", email: "", address: "" };
  async function createCompany() {
    try {
      await axios.post(`${apiRoot}/api/companies`, newCompany, {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${$jwt_token}`,
        },
      });
      alert("Company created");
      newCompany = { name: "", email: "", address: "" };
      getCompanies(1);
    } catch (error) {
      console.error("Could not create company", error);
      alert("Could not create company");
    }
  }

  // Edit company modal
  function openEditModal(company) {
    editCompany = { ...company };
    showEditModal = true;
  }

  function closeEditModal() {
    showEditModal = false;
    editCompany = null;
  }

  async function submitEdit(updated) {
    try {
      await axios.put(`${apiRoot}/api/companies/${updated.id}`, updated, {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${$jwt_token}`,
        },
      });
      alert("Company updated");
      closeEditModal();
      getCompanies(currentPage);
    } catch (error) {
      console.error("Could not update company", error);
      alert("Could not update company");
    }
  }

  async function deleteCompany(id) {
    if (!confirm("Delete this company?")) return;
    try {
      await axios.delete(`${apiRoot}/api/companies/${id}`, {
        headers: { Authorization: `Bearer ${$jwt_token}` },
      });
      alert("Company deleted");
      getCompanies(currentPage);
    } catch (error) {
      console.error("Could not delete company", error);
      alert("Could not delete company");
    }
  }
</script>

{#if showEditModal && editCompany}
  <EditCompanyModal
    company={editCompany}
    {users}
    on:cancel={closeEditModal}
    on:save={(e) => submitEdit(e.detail)}
  />
{/if}

<!-- Create Company Form -->
<h1 class="mt-3 text-center">Create Company</h1>
<form onsubmit={createCompany} class="mb-5">
  <div class="row g-3">
    <div class="col-md-4">
      <label class="form-label">Name</label>
      <input
        class="form-control"
        bind:value={newCompany.name}
        required
        placeholder="Neurofleet AG"
      />
    </div>
    <div class="col-md-4">
      <label class="form-label">Email</label>
      <input
        type="email"
        class="form-control"
        bind:value={newCompany.email}
        required
        placeholder="maxmustermann@neurofleet.com"
      />
    </div>
    <div class="col-md-4">
      <label class="form-label">Address</label>
      <input
        class="form-control"
        bind:value={newCompany.address}
        required
        placeholder="Bahnhofstrasse 1, 8001 Zürich, Switzerland"
      />
    </div>
  </div>
  <button type="submit" class="btn btn-primary mt-3">Submit</button>
</form>

<!-- Companies Table -->
<h1 class="text-center">All Companies</h1>
{#if loading}
  <div class="d-flex justify-content-center my-4">
    <div class="spinner-border" role="status">
      <span class="visually-hidden">Loading...</span>
    </div>
  </div>
{:else}
  <table class="table table-hover">
    <thead>
      <tr>
        <th>Name</th>
        <th>Address</th>
        <th>Latitude</th>
        <th>Longitude</th>
        <th>ID</th>
        <th>Owner</th>
        <th>Email</th>
        <th>Actions</th>
      </tr>
    </thead>
    <tbody>
      {#each companies as c}
        <tr>
          <td>{c.name}</td>
          <td>{c.address}</td>
          <td>{c.latitude}</td>
          <td>{c.longitude}</td>
          <td>{c.id}</td>
          <td
            >{userMap[c.owner]?.given_name}
            {userMap[c.owner]?.family_name || ""}</td
          >
          <td>{c.email}</td>
          <td>
            <a
              href="#"
              class="d-flex align-items-center text-white text-decoration-none dropdown-toggle"
              id="userDropdown"
              data-bs-toggle="dropdown"
              aria-expanded="false"
            >
              <button class="btn btn-sm btn-outline-secondary">
                <i class="bi bi-gear-fill"></i> Edit
              </button>
            </a>
            <ul
              class="dropdown-menu dropdown-menu-dark dropdown-menu-end text-small shadow"
              aria-labelledby="userDropdown"
            >
              <li>
                <a class="dropdown-item" onclick={() => openEditModal(c)}
                  >Edit</a
                >
              </li>
              <li>
                <a
                  class="dropdown-item text-danger"
                  onclick={() => deleteCompany(c.id)}>Delete</a
                >
              </li>
              <li><hr class="dropdown-divider" /></li>
            </ul>
          </td>
        </tr>
      {/each}
    </tbody>
  </table>

  <!-- Pagination -->
  <nav>
    <ul class="pagination justify-content-center">
      <li class="page-item" class:disabled={currentPage === 1}>
        <button class="page-link" onclick={() => changePage(currentPage - 1)}
          >&laquo;</button
        >
      </li>
      {#each Array(totalPages) as _, i}
        <li class="page-item" class:active={currentPage === i + 1}>
          <button class="page-link" onclick={() => changePage(i + 1)}
            >{i + 1}</button
          >
        </li>
      {/each}
      <li class="page-item" class:disabled={currentPage === totalPages}>
        <button class="page-link" onclick={() => changePage(currentPage + 1)}
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
