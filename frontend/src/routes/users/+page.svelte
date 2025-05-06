<script>
  import axios from "axios";
  import { onMount } from "svelte";
  import { page } from "$app/state";
  import { jwt_token } from "../../store";

  const api_root = page.url.origin;

  let currentPage = $state(1);
  let nrOfPages = $state(1);
  let defaultPageSize = $state(20);

  let users = $state([]);
  let selectedUser = $state(null);
  let metadata = $state(null);
  let showEditModal = $state(false);

  onMount(() => {
    getUsers();
  });

  function getUsers() {
    axios
      .get(`${api_root}/api/auth0/users`, {
        headers: { Authorization: "Bearer " + $jwt_token },
      })
      .then((res) => {
        users = res.data;
        nrOfPages = 1; // keine Serverpagination bei Auth0
      })
      .catch((err) => {
        alert("Could not load users");
        console.error(err);
      });
  }

  function openEditModal(user) {
    selectedUser = user;
    showEditModal = true;
    getMetadata(user.user_id);
  }

  function closeEditModal() {
    showEditModal = false;
    selectedUser = null;
    metadata = null;
  }

  function getMetadata(userId) {
    axios
      .get(
        `${api_root}/api/auth0/users/${encodeURIComponent(userId)}/metadata`,
        {
          headers: { Authorization: "Bearer " + $jwt_token },
        }
      )
      .then((res) => {
        metadata = res.data;
      })
      .catch((err) => {
        alert("Could not load metadata");
        console.error(err);
      });
  }

  function submitEdit() {
    axios
      .patch(
        `${api_root}/api/admin/users/${encodeURIComponent(selectedUser.user_id)}/assign`,
        metadata,
        {
          headers: {
            Authorization: "Bearer " + $jwt_token,
            "Content-Type": "application/json",
          },
        }
      )
      .then(() => {
        alert("User metadata updated");
        closeEditModal();
      })
      .catch((err) => {
        alert("Could not update user");
        console.error(err);
      });
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
          <h5 class="modal-title">Edit User Metadata</h5>
          <button type="button" class="btn-close" onclick={closeEditModal}
          ></button>
        </div>
        <div class="modal-body">
          <p><strong>Email:</strong> {selectedUser.email}</p>
          <label>Company ID</label>
          <input class="form-control mb-2" bind:value={metadata.companyId} />
          <label>Role</label>
          <select class="form-select mb-2" bind:value={metadata.role}>
            <option value="ADMIN">ADMIN</option>
            <option value="OWNER">OWNER</option>
            <option value="FLEET_MANAGER">FLEET_MANAGER</option>
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

<h1 class="mt-3">All Auth0 Users</h1>
<table class="table">
  <thead>
    <tr>
      <th scope="col">Name</th>
      <th scope="col">Email</th>
      <th scope="col">ID</th>
      <th scope="col"></th>
    </tr>
  </thead>
  <tbody>
    {#each users as user}
      <tr>
        <td>{user.name}</td>
        <td>{user.email}</td>
        <td>{user.user_id}</td>
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
                  onclick={() => openEditModal(user)}>Edit</button
                >
              </li>
            </ul>
          </div>
        </td>
      </tr>
    {/each}
  </tbody>
</table>
