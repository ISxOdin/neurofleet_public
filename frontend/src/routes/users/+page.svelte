<script>
  import axios from "axios";
  import { onMount } from "svelte";
  import { page } from "$app/state";
  import { jwt_token } from "../../store";

  const api_root = page.url.origin;

  let users = $state([]);
  let selectedUser = $state(null);
  let showEditModal = $state(false);
  let currentRoles = $state([]);
  let newRole = $state("");
  let loading = $state(false);

  onMount(() => {
    getUsers();
  });

  async function getUsers() {
    loading = true;
    try {
      const res = await axios.get(`${api_root}/api/auth0/users`, {
        headers: { Authorization: "Bearer " + $jwt_token },
      });

      const rawUsers = res.data;

      const usersWithRoles = await Promise.all(
        rawUsers.map(async (user) => {
          try {
            const roleRes = await axios.get(
              `${api_root}/api/auth0/users/${encodeURIComponent(user.user_id)}/roles`,
              {
                headers: { Authorization: "Bearer " + $jwt_token },
              }
            );
            return { ...user, role: roleRes.data[0] || "–" };
          } catch {
            return { ...user, role: "–" };
          }
        })
      );

      users = usersWithRoles;
    } catch (err) {
      alert("Could not load users");
      console.error(err);
    } finally {
      loading = false;
    }
  }

  async function openEditModal(user) {
    selectedUser = user;
    showEditModal = true;

    try {
      const res = await axios.get(
        `${api_root}/api/auth0/users/${encodeURIComponent(user.user_id)}/roles`,
        {
          headers: { Authorization: "Bearer " + $jwt_token },
        }
      );
      currentRoles = res.data;
      newRole = currentRoles[0] || "";
    } catch (err) {
      alert("Could not load roles");
      console.error(err);
    }
  }

  function closeEditModal() {
    showEditModal = false;
    selectedUser = null;
    currentRoles = [];
    newRole = "";
  }

  async function assignExclusiveRole() {
    if (!newRole) return;

    const userId = selectedUser.user_id;

    try {
      for (const role of currentRoles) {
        await axios.delete(
          `${api_root}/api/auth0/users/${encodeURIComponent(userId)}/roles/${role}`,
          {
            headers: { Authorization: "Bearer " + $jwt_token },
          }
        );
      }

      await axios.post(
        `${api_root}/api/auth0/users/${encodeURIComponent(userId)}/roles/${newRole}`,
        {},
        { headers: { Authorization: "Bearer " + $jwt_token } }
      );

      alert("Role updated");
      await openEditModal(selectedUser);
      closeEditModal();
      getUsers();
    } catch (err) {
      alert("Failed to assign role");
      console.error(err);
    }
  }
</script>

{#if showEditModal}
  <div class="modal-backdrop show"></div>
  <div
    class="modal d-block"
    tabindex="-1"
    style="background-color: rgba(0,0,0,0.5)"
  >
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content" style="background-color: #4F5A65">
        <div class="modal-header">
          <h5 class="modal-title">Edit User Role</h5>
          <button type="button" class="btn-close" onclick={closeEditModal}
          ></button>
        </div>
        <div class="modal-body">
          <p><strong>Email:</strong> {selectedUser.email}</p>

          <label>Assigned Role</label>
          <select class="form-select mb-3" bind:value={newRole}>
            <option value="" disabled selected>-- Select role --</option>
            <option value="admin">admin</option>
            <option value="owner">owner</option>
            <option value="fleetmanager">fleetmanager</option>
          </select>

          <button
            class="btn btn-primary w-100"
            onclick={assignExclusiveRole}
            disabled={!newRole}
          >
            Assign Role
          </button>
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

<h1 class="mt-3">All Auth0 Users</h1>

{#if loading}
  <div class="d-flex justify-content-center my-4">
    <div class="spinner-border" role="status">
      <span class="visually-hidden">Loading...</span>
    </div>
  </div>
{:else}
  <table class="table">
    <thead>
      <tr>
        <th>Name</th>
        <th>Email</th>
        <th>User ID</th>
        <th>Role</th>
        <th></th>
      </tr>
    </thead>
    <tbody>
      {#each users as user}
        <tr>
          <td>{user.name}</td>
          <td>{user.email}</td>
          <td>{user.user_id}</td>
          <td>{user.role}</td>
          <td>
            <div class="dropdown">
              <button
                class="btn btn-link text-dark p-0"
                type="button"
                data-bs-toggle="dropdown"
              >
                <i class="bi bi-gear-fill"></i>
              </button>
              <ul class="dropdown-menu">
                <li>
                  <button
                    class="dropdown-item"
                    onclick={() => openEditModal(user)}>Edit Role</button
                  >
                </li>
              </ul>
            </div>
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
{/if}
