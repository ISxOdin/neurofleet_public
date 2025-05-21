<script>
  import axios from "axios";
  import { onMount } from "svelte";
  import { browser } from "$app/environment";
  import { jwt_token } from "../../store";
  import EditCompanyModal from "$lib/components/modals/EditCompanyModal.svelte";
  import CreateCompanyModal from "$lib/components/modals/CreateCompanyModal.svelte";
  import Pagination from "$lib/components/Pagination.svelte";

  let apiRoot = "";
  let users = [];
  let userMap = {};

  let companies = [];
  let currentPage = 1;
  let totalPages = 0;
  const pageSize = 5;
  let loading = false;

  let showEditModal = false;
  let showCreateModal = false;
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

  function openEditModal(company) {
    editCompany = { ...company };
    showEditModal = true;
  }

  function closeEditModal() {
    showEditModal = false;
    editCompany = null;
  }

  function openCreateModal() {
    showCreateModal = true;
  }

  function closeCreateModal() {
    showCreateModal = false;
    newCompany = { name: "", email: "", address: "" };
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

  function validateEmailAndCreateCompany() {
    var config = {
      method: "get",
      url: "https://disify.com/api/email/" + newCompany.email,
    };

    axios(config)
      .then(function (response) {
        console.log("Validated email " + newCompany.email);
        console.log(response.data);
        if (
          response.data.format &&
          !response.data.disposable &&
          response.data.dns
        ) {
          createCompany();
        } else {
          alert("Email " + newCompany.email + " is not valid.");
        }
      })
      .catch(function (error) {
        alert("Could not validate email");
        console.log(error);
      });
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

<!-- Companies Table -->

<div class="companies-header">
  <h1 class="text-center">All Companies</h1>
  <button class="btn-accent" onclick={() => (showCreateModal = true)}>
    <i class="bi bi-plus-lg"></i> Create Company
  </button>
  {#if showCreateModal}
    <CreateCompanyModal
      on:created={() => {
        showCreateModal = false;
        getCompanies();
      }}
      on:cancel={closeCreateModal}
    />
  {/if}
</div>
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
              <button class="btn btn-sm btn-outline-light">
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

  <Pagination {currentPage} {totalPages} onPageChange={getCompanies} />
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
</style>
