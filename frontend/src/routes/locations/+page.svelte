<script>
  import axios from "axios";
  import { page } from "$app/state";
  import { onMount } from "svelte";
  import { jwt_token } from "../../store";

  // get the origin of current page, e.g. http://localhost:8080
  const api_root = page.url.origin;

  let currentPage = $state(1);
  let nrOfPages = $state(0);
  let defaultPageSize = $state(20);

  let showEditModal = $state(false);
  let editLocation = $state(null);
  let users = $state([]);
  let userMap = $state({});

  let locations = $state([]);
  let location = $state({
    id: null,
    name: null,
    email: null,
    address: null,
    latitude: null,
    longitude: null,
    owner: null,
  });

  onMount(() => {
    getLocations();
    getUsers();
  });

  function changePage(page) {
    currentPage = page;
    getLocations();
  }

  function getLocations() {
    let query = "?pageSize=" + defaultPageSize + "&pageNumber=" + currentPage;

    var config = {
      method: "get",
      url: api_root + "/api/locations" + query,
      headers: { Authorization: "Bearer " + $jwt_token },
    };

    axios(config)
      .then(function (response) {
        companies = response.data.content;
        nrOfPages = response.data.totalPages;
      })
      .catch(function (error) {
        alert("Could not get companies");
        console.log(error);
      });
  }

  function createLocation() {
    var config = {
      method: "post",
      url: api_root + "/api/locations",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + $jwt_token,
      },
      data: location,
    };

    axios(config)
      .then(function (response) {
        alert("Location created");
        getCompanies();
      })
      .catch(function (error) {
        alert("Could not create Location");
        console.log(error);
      });
  }
  function openEditModal(location) {
    editLocation = { ...location };
    showEditModal = true;
    getUsers();
  }
  function closeEditModal() {
    showEditModal = false;
    editLocation = null;
  }

  function submitEdit() {
    axios
      .put(
        `${api_root}/api/locations/${editLocation.id}`,
        {
          name: editLocation.name,
          email: editLocation.email,
          address: editLocation.address,
          owner: editLocation.owner,
        },
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer " + $jwt_token,
          },
        }
      )
      .then(() => {
        alert("Location updated successfully");
        closeEditModal();
        getCompanies(); // refresh the list
      })
      .catch((err) => {
        alert("Could not update Location");
        console.error(err);
      });
  }

  function deleteLocation(locationId) {
    if (confirm("Are you sure you want to delete this location?")) {
      axios
        .delete(`${api_root}/api/locations/${locationId}`, {
          headers: { Authorization: "Bearer " + $jwt_token },
        })
        .then(() => {
          alert("Location deleted");
          getCompanies();
        })
        .catch((err) => {
          alert("Could not delete location");
          console.log(err);
        });
    }
  }

  function getUsers() {
    axios
      .get(api_root + "/api/auth0/users", {
        headers: { Authorization: "Bearer " + $jwt_token },
      })
      .then((res) => {
        users = res.data.map((user) => ({
          id: user.user_id,
          label: user.email || user.name,
          name: user.name,
          email: user.email,
        }));

        userMap = users.reduce((acc, user) => {
          acc[user.id] = user;
          return acc;
        }, {});
      })
      .catch((err) => {
        alert("Failed to load Auth0 users");
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
          <h5 class="modal-title">Edit Location</h5>
          <button type="button" class="btn-close" onclick={closeEditModal}
          ></button>
        </div>
        <div class="modal-body">
          <label>Name</label>
          <input class="form-control mb-2" bind:value={editLocation.name} />

          <label>Email</label>
          <input class="form-control mb-2" bind:value={editLocation.email} />

          <label>Address</label>
          <input class="form-control mb-2" bind:value={editLocation.address} />
          <label>Fleet Manager</label>
          <select class="form-select mb-2" bind:value={editLocation.owner}>
            <option disabled selected value=""
              >-- Select Fleet Manager --</option
            >
            {#each users as user}
              <option value={user.id}>{user.label}</option>
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

<h1 class="mt-3">Create Location</h1>
<form class="mb-5">
  <div class="row mb-3">
    <div class="col">
      <label class="form-label" for="description">Name</label>
      <input
        bind:value={location.name}
        class="form-control"
        id="description"
        type="text"
      />
    </div>
  </div>
  <div class="row mb-3">
    <div class="col">
      <label class="form-label" for="description">E-Mail</label>
      <input
        bind:value={location.email}
        class="form-control"
        id="description"
        type="text"
      />
    </div>
  </div>
  <div class="row mb-3">
    <div class="col">
      <label class="form-label" for="description">Address</label>
      <input
        bind:value={location.address}
        class="form-control"
        id="description"
        type="text"
      />
    </div>
  </div>
  <button type="button" class="btn btn-primary" onclick={createLocation}
    >Submit</button
  >
</form>

<h1>All Locations</h1>
<table class="table">
  <thead>
    <tr>
      <th scope="col">Name</th>
      <th scope="col">Address</th>
      <th scope="col">Longitude</th>
      <th scope="col">Latitude</th>
      <th scope="col">ID</th>
      <th scope="col">Owner</th>
      <th scope="col">E-Mail</th>
      <th scope="col"></th>
    </tr>
  </thead>
  <tbody>
    {#each companies as location}
      <tr>
        <td>{location.name}</td>
        <td>{location.address}</td>
        <td>{location.latitude}</td>
        <td>{location.longitude}</td>
        <td>{location.id}</td>
        <td
          >{userMap[location.owner]?.name ||
            userMap[location.owner]?.email ||
            "–"}</td
        >
        <td>{location.email}</td>
        <td>
          <div class="dropdown">
            <button
              class="btn btn-link text-dark p-0"
              type="button"
              data-bs-toggle="dropdown"
              aria-expanded="false"
            >
              <i class="bi bi-gear-fill"></i>
            </button>
            <ul class="dropdown-menu">
              <li>
                <button
                  class="dropdown-item"
                  onclick={() => openEditModal(location)}>Edit</button
                >
              </li>
              <li>
                <button
                  class="dropdown-item text-danger"
                  onclick={() => deleteLocation(location.id)}>Delete</button
                >
              </li>
            </ul>
          </div>
        </td>
      </tr>
    {/each}
  </tbody>
</table>

<nav>
  <ul class="pagination justify-content-center">
    <li class="page-item">
      <a class="page-link" href="#" aria-label="Previous">
        <span aria-hidden="true">&laquo;</span>
      </a>
    </li>
    {#each Array(nrOfPages) as _, i}
      <li class="page-item">
        <button
          class="page-link"
          class:active={currentPage == i + 1}
          onclick={() => {
            changePage(i + 1);
          }}
          >{i + 1}
        </button>
      </li>
    {/each}
    <li class="page-item">
      <a class="page-link" href="#" aria-label="Next">
        <span aria-hidden="true">&raquo;</span>
      </a>
    </li>
  </ul>
</nav>

<style>
  .page-link {
    box-shadow: none;
  }
</style>
