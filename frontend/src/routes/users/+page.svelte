<script>
  import axios from "axios";
  import { onMount } from "svelte";
  import { browser } from "$app/environment";
  import { jwt_token } from "../../store";

  let users = [];
  let companies = [];

  let selectedUser = null;
  let showEditModal = false;
  let currentRoles = [];
  let newRole = "";
  let newCompanyId = "";
  let apiRoot = "";

  let currentPage = 1;
  let totalPages = 0;
  const pageSize = 20;
  let loading = false;

  onMount(async () => {
    if (browser) apiRoot = window.location.origin;
    await Promise.all([getUsers(), getCompanies()]);
  });

  async function getUsers() {
    loading = true;
    try {
      const { data } = await axios.get(`${apiRoot}/api/auth0/users`, {
        headers: { Authorization: `Bearer ${$jwt_token}` },
      });
      users = data.map((u) => ({ ...u, role: u.roles?.[0] || "–" }));
    } catch (e) {
      console.error("Could not load users", e);
      alert("Could not load users");
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

  function openEditModal(user) {
    selectedUser = user;
    currentRoles = user.roles || [];
    newRole = currentRoles[0] || "";
    newCompanyId = "";
    showEditModal = true;
  }

  function closeEditModal() {
    showEditModal = false;
    selectedUser = null;
    currentRoles = [];
    newRole = "";
    newCompanyId = "";
  }

  async function assignExclusiveRole() {
    if (!newRole || !selectedUser) return;
    const id = encodeURIComponent(selectedUser.user_id);
    try {
      // remove existing
      await Promise.all(
        currentRoles.map((r) =>
          axios.delete(`${apiRoot}/api/auth0/users/${id}/roles/${r}`, {
            headers: { Authorization: `Bearer ${$jwt_token}` },
          })
        )
      );
      // add new
      await axios.post(
        `${apiRoot}/api/auth0/users/${id}/roles/${newRole}`,
        {},
        { headers: { Authorization: `Bearer ${$jwt_token}` } }
      );
      alert("Role updated");
      await getUsers();
      closeEditModal();
    } catch (e) {
      console.error("Failed to assign role", e);
      alert("Failed to assign role");
    }
  }

  async function updateCompany() {
    if (!newCompanyId || !selectedUser) return;
    const userId = encodeURIComponent(selectedUser.user_id);
    try {
      await axios.post(
        `${apiRoot}/api/companies/${newCompanyId}/users/${userId}`,
        {},
        { headers: { Authorization: `Bearer ${$jwt_token}` } }
      );
      alert("Company assigned");
      await getUsers();
      closeEditModal();
    } catch (e) {
      console.error("Failed to assign company", e);
      alert("Failed to assign company");
    }
  }
</script>

{#if loading}
  <div class="d-flex justify-content-center my-4">
    <div class="spinner-border" role="status">
      <span class="visually-hidden">Loading...</span>
    </div>
  </div>
{:else}
  <table class="table table-striped">
    <thead
      ><tr
        ><th>Name</th><th>Email</th><th>User ID</th><th>Role</th><th>Actions</th
        ></tr
      ></thead
    >
    <tbody>
      {#each users as user}
        <tr>
          <td>{user.given_name} {user.family_name}</td>
          <td>{user.email}</td>
          <td>{user.user_id}</td>
          <td>{user.role}</td>
          <td>
            <button
              class="btn btn-sm btn-outline-secondary"
              onclick={() => openEditModal(user)}
            >
              <i class="bi bi-gear-fill"></i> Edit
            </button>
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
{/if}

{#if showEditModal}
  <div class="modal-backdrop show"></div>
  <div
    class="modal d-block"
    tabindex="-1"
    style="background-color: rgba(0,0,0,0.5)"
  >
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content bg-dark text-light">
        <div class="modal-header">
          <h5 class="modal-title">Edit User</h5>
          <button class="btn-close btn-close-white" onclick={closeEditModal}
          ></button>
        </div>
        <div class="modal-body">
          <p><strong>Email:</strong> {selectedUser.email}</p>
          <label>Role</label>
          <select class="form-select mb-3" bind:value={newRole}>
            <option disabled value="">-- Select role --</option>
            <option value="admin">admin</option>
            <option value="owner">owner</option>
            <option value="fleetmanager">fleetmanager</option>
            <option value="driver">driver</option>
            <option value="user">user</option>
          </select>
          <button
            class="btn btn-primary w-100 mb-3"
            onclick={assignExclusiveRole}
            disabled={!newRole}>Assign Role</button
          >
          <label>Company</label>
          <select class="form-select mb-3" bind:value={newCompanyId}>
            <option disabled value="">-- Select company --</option>
            {#each companies as c}
              <option value={c.id}>{c.name}</option>
            {/each}
          </select>
          <button
            class="btn btn-primary w-100"
            onclick={updateCompany}
            disabled={!newCompanyId}>Assign Company</button
          >
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" onclick={closeEditModal}
            >Close</button
          >
        </div>
      </div>
    </div>
  </div>
{/if}
