<script>
  import axios from "axios";
  import { onMount } from "svelte";
  import { browser } from "$app/environment";
  import { jwt_token } from "../../store";
  import EditUserModal from "$lib/components/modals/EditUserModal.svelte";

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
</script>

<h1 class="text-center">All Users</h1>
{#if loading}
  <div class="d-flex justify-content-center my-4">
    <div class="spinner-border" role="status">
      <span class="visually-hidden">Loading...</span>
    </div>
  </div>
{:else}
  <table class="table table-hover">
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

{#if showEditModal && selectedUser}
  <EditUserModal
    user={selectedUser}
    roles={["admin", "owner", "fleetmanager"]}
    {companies}
    on:cancel={closeEditModal}
    on:save={(e) => saveEditedUser(e.detail)}
  />
{/if}
