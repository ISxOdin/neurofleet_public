<script>
  import axios from "axios";
  import { onMount } from "svelte";
  import { browser } from "$app/environment";
  import { jwt_token, isAuthenticated } from "../../store";
  import EditUserModal from "$lib/components/modals/EditUserModal.svelte";
  import { goto } from "$app/navigation";

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
  const pageSize = 5;
  let loading = false;

  onMount(async () => {
    if (browser) apiRoot = window.location.origin;
    await Promise.all([getUsers(), getCompanies()]);
  });

  async function getUsers() {
    try {
      loading = true;
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
    try {
      loading = true;
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

  async function saveEditedUser({ userId, selectedRole, selectedCompanyId }) {
    const id = encodeURIComponent(userId);

    const currentUser = users.find((u) => u.user_id === userId);
    const currentCompanyId = currentUser?.companyId;
    const currentRoles = [currentUser?.role].filter(Boolean); // falls null

    try {
      if (selectedRole && selectedRole !== currentRoles[0]) {
        await Promise.all(
          currentRoles.map((r) =>
            axios.delete(`${apiRoot}/api/auth0/users/${id}/roles/${r}`, {
              headers: { Authorization: `Bearer ${$jwt_token}` },
            })
          )
        );

        await axios.post(
          `${apiRoot}/api/auth0/users/${id}/roles/${selectedRole}`,
          {},
          { headers: { Authorization: `Bearer ${$jwt_token}` } }
        );
      }

      if (selectedCompanyId && selectedCompanyId !== currentCompanyId) {
        await axios.post(
          `${apiRoot}/api/companies/${selectedCompanyId}/users/${id}`,
          {},
          { headers: { Authorization: `Bearer ${$jwt_token}` } }
        );
      }

      alert("User updated");
      await getUsers();
      closeEditModal();
    } catch (e) {
      console.error("Failed to update user", e);
      alert("Failed to update user");
    }
  }

  function goToLogin() {
    goto("/");
  }
</script>

{#if $isAuthenticated}
  <div class="container-fluid px-4">
    <div class="users-header">
      <h1 class="text-center">Users</h1>
    </div>

    {#if loading}
      <div class="d-flex justify-content-center my-4">
        <div class="spinner-border text-light" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
      </div>
    {:else}
      <div class="table-responsive">
        <table class="users-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>User ID</th>
              <th>Role</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {#each users as user}
              <tr class="user-row">
                <td>{user.given_name} {user.family_name}</td>
                <td>{user.email}</td>
                <td>{user.user_id}</td>
                <td>
                  <span class="status-badge status-{user.role.toLowerCase()}">
                    <i class="bi bi-person-fill me-1"></i>
                    {user.role}
                  </span>
                </td>
                <td>
                  <button
                    class="btn btn-sm btn-outline-light"
                    on:click={() => openEditModal(user)}
                  >
                    <i class="bi bi-gear-fill"></i> Edit
                  </button>
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
  </div>

  {#if showEditModal && selectedUser}
    <EditUserModal
      user={selectedUser}
      roles={["admin", "owner", "fleetmanager"]}
      {companies}
      on:cancel={closeEditModal}
      on:save={(e) => saveEditedUser(e.detail)}
    />
  {/if}
{:else}
  <div class="container mt-5 text-center">
    <div class="not-authenticated">
      <i class="bi bi-lock-fill fa-3x mb-3"></i>
      <p>You are not logged in.</p>
      <button class="btn btn-primary mt-3" on:click={goToLogin}>
        Go to Login
      </button>
    </div>
  </div>
{/if}

<style>
  .container-fluid {
    padding-top: 2rem;
  }

  .users-header {
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

  .users-header h1 {
    color: white;
    font-size: 1.4rem;
    margin: 0;
  }

  .table {
    margin-top: 1rem;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 8px;
    overflow: hidden;
  }

  .users-table {
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

  .users-table th,
  .users-table td {
    padding: 1rem 0.8rem;
    text-align: left;
    vertical-align: middle;
  }

  .users-table th {
    color: #95d4ee;
    font-weight: 600;
    background: #343c44;
    border-bottom: 2px solid #343c44;
  }

  .user-row {
    transition: background 0.15s;
  }

  .user-row:nth-child(even) {
    background: #343c44;
  }

  .user-row:nth-child(odd) {
    background: #4f5a65;
  }

  .user-row:hover {
    background: rgba(149, 212, 238, 0.2);
  }

  .btn-outline-primary {
    color: #95d4ee;
    border-color: #95d4ee;
  }

  .btn-outline-primary:hover {
    background: rgba(149, 212, 238, 0.1);
    color: #fff;
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

  .status-admin {
    background-color: rgba(40, 167, 69, 0.5);
    color: #3dbe5b;
  }

  .status-owner {
    background-color: rgba(0, 123, 255, 0.5);
    color: #75b6fc;
  }

  .status-fleetmanager {
    background-color: rgba(255, 193, 7, 0.5);
    color: #ffc107;
  }

  .status-– {
    background-color: rgba(108, 117, 125, 0.5);
    color: #ced4da;
  }

  .not-authenticated {
    color: #95d4ee;
  }

  .not-authenticated i {
    font-size: 3rem;
    margin-bottom: 1rem;
  }
</style>
