<script>
  import axios from "axios";
  import { onMount } from "svelte";
  import { browser } from "$app/environment";
  import { jwt_token } from "../../store";

  let users = [];
  let selectedUser = null;
  let showEditModal = false;
  let currentRoles = [];
  let newRole = "";
  let loading = false;
  let metadata = { companyId: "" };
  let apiRoot = "";

  onMount(() => {
    if (browser) {
      apiRoot = window.location.origin;
    }
    getUsers();
  });

  async function getUsers() {
    loading = true;
    try {
      const { data: rawUsers } = await axios.get(`${apiRoot}/api/auth0/users`, {
        headers: { Authorization: `Bearer ${$jwt_token}` },
      });

      users = rawUsers.map((user) => ({
        ...user,
        role: user.roles?.[0] || "–",
      }));
    } catch (error) {
      console.error("Could not load users", error);
      alert("Could not load users");
    } finally {
      loading = false;
    }
  }

  function openEditModal(user) {
    selectedUser = user;
    showEditModal = true;
    currentRoles = user.roles || [];
    newRole = currentRoles[0] || "";
    loadMetadata(user.user_id);
  }

  async function loadMetadata(userId) {
    try {
      const { data } = await axios.get(
        `${apiRoot}/api/auth0/users/${encodeURIComponent(userId)}/metadata`,
        { headers: { Authorization: `Bearer ${$jwt_token}` } }
      );
      metadata = data;
    } catch (error) {
      console.error("Could not load metadata", error);
      metadata = { companyId: "" };
    }
  }

  function closeEditModal() {
    showEditModal = false;
    selectedUser = null;
    currentRoles = [];
    newRole = "";
    metadata = { companyId: "" };
  }

  async function assignExclusiveRole() {
    if (!newRole || !selectedUser) return;
    const userId = selectedUser.user_id;
    try {
      await Promise.all(
        currentRoles.map((role) =>
          axios.delete(
            `${apiRoot}/api/auth0/users/${encodeURIComponent(userId)}/roles/${role}`,
            { headers: { Authorization: `Bearer ${$jwt_token}` } }
          )
        )
      );

      await axios.post(
        `${apiRoot}/api/auth0/users/${encodeURIComponent(userId)}/roles/${newRole}`,
        {},
        { headers: { Authorization: `Bearer ${$jwt_token}` } }
      );

      alert("Role updated");
      await getUsers();
      closeEditModal();
    } catch (error) {
      console.error("Failed to assign role", error);
      alert("Failed to assign role");
    }
  }

  async function updateMetadata() {
    if (!selectedUser) return;
    try {
      await axios.patch(
        `${apiRoot}/api/auth0/users/${encodeURIComponent(selectedUser.user_id)}/metadata`,
        metadata,
        {
          headers: {
            Authorization: `Bearer ${$jwt_token}`,
            "Content-Type": "application/json",
          },
        }
      );
      alert("Company ID updated");
    } catch (error) {
      console.error("Failed to update companyId", error);
      alert("Failed to update company ID");
    }
  }
</script>

<!-- Edit User Modal -->
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
          <button class="btn-close btn-close-white" on:click={closeEditModal}
          ></button>
        </div>
        <div class="modal-body">
          <p><strong>Email:</strong> {selectedUser.email}</p>
          <label>Assigned Role</label>
          <select class="form-select mb-3" bind:value={newRole}>
            <option value="" disabled>-- Select role --</option>
            <option value="admin">admin</option>
            <option value="owner">owner</option>
            <option value="fleetmanager">fleetmanager</option>
            <option value="driver">driver</option>
            <option value="user">user</option>
          </select>
          <button
            class="btn btn-primary w-100"
            on:click={assignExclusiveRole}
            disabled={!newRole}
          >
            Assign Role
          </button>
          <hr class="my-3" />
          <label>Company ID</label>
          <input
            class="form-control mb-3"
            bind:value={metadata.companyId}
            placeholder="Enter Company ID"
          />
          <button class="btn btn-outline-light w-100" on:click={updateMetadata}>
            Update Company ID
          </button>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" on:click={closeEditModal}
            >Close</button
          >
        </div>
      </div>
    </div>
  </div>
{/if}

<!-- User Table -->
<h1 class="mt-3">All Auth0 Users</h1>

{#if loading}
  <div class="d-flex justify-content-center my-4">
    <div class="spinner-border" role="status">
      <span class="visually-hidden">Loading...</span>
    </div>
  </div>
{:else}
  <table class="table table-striped">
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
        <tr>
          <td>{user.given_name} {user.family_name}</td>
          <td>{user.email}</td>
          <td>{user.user_id}</td>
          <td>{user.role}</td>
          <td>
            <button
              class="btn btn-sm btn-outline-secondary"
              on:click={() => openEditModal(user)}
            >
              <i class="bi bi-gear-fill"></i> Edit</button
            >
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
{/if}
