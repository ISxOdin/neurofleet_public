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
  let editCompany = $state(null);
  let users = $state([]);
  let userMap = $state({});

  let companies = $state([]);
  let company = $state({
    id: null,
    name: null,
    email: null,
    address: null,
    latitude: null,
    longitude: null,
    owner: null,
  });

  onMount(() => {
    getCompanies();
    getUsers();
  });

  function changePage(page) {
    currentPage = page;
    getCompanies();
  }

  function getCompanies() {
    let query = "?pageSize=" + defaultPageSize + "&pageNumber=" + currentPage;

    var config = {
      method: "get",
      url: api_root + "/api/companies" + query,
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

  function createCompany() {
    var config = {
      method: "post",
      url: api_root + "/api/companies",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + $jwt_token,
      },
      data: company,
    };

    axios(config)
      .then(function (response) {
        alert("Company created");
        getCompanies();
      })
      .catch(function (error) {
        alert("Could not create Company");
        console.log(error);
      });
  }
  function openEditModal(company) {
    editCompany = { ...company };
    showEditModal = true;
    getUsers();
  }
  function closeEditModal() {
    showEditModal = false;
    editCompany = null;
  }

  function submitEdit() {
    axios
      .put(
        `${api_root}/api/companies/${editCompany.id}`,
        {
          name: editCompany.name,
          email: editCompany.email,
          address: editCompany.address,
          owner: editCompany.owner,
        },
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer " + $jwt_token,
          },
        }
      )
      .then(() => {
        alert("Company updated successfully");
        closeEditModal();
        getCompanies(); // refresh the list
      })
      .catch((err) => {
        alert("Could not update company");
        console.error(err);
      });
  }

  function deleteCompany(companyId) {
    if (confirm("Are you sure you want to delete this company?")) {
      axios
        .delete(`${api_root}/api/companies/${companyId}`, {
          headers: { Authorization: "Bearer " + $jwt_token },
        })
        .then(() => {
          alert("Company deleted");
          getCompanies();
        })
        .catch((err) => {
          alert("Could not delete company");
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

        // Build lookup map by ID
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
          <h5 class="modal-title">Edit Company</h5>
          <button type="button" class="btn-close" onclick={closeEditModal}
          ></button>
        </div>
        <div class="modal-body">
          <label>Name</label>
          <input class="form-control mb-2" bind:value={editCompany.name} />

          <label>Email</label>
          <input class="form-control mb-2" bind:value={editCompany.email} />

          <label>Address</label>
          <input class="form-control mb-2" bind:value={editCompany.address} />
          <label>Owner</label>
          <select class="form-select mb-2" bind:value={editCompany.owner}>
            <option disabled selected value="">-- Select owner --</option>
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

<h1 class="mt-3">Create Company</h1>
<form class="mb-5">
  <div class="row mb-3">
    <div class="col">
      <label class="form-label" for="description">Name</label>
      <input
        bind:value={company.name}
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
        bind:value={company.email}
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
        bind:value={company.address}
        class="form-control"
        id="description"
        type="text"
      />
    </div>
  </div>
  <button type="button" class="btn btn-primary" onclick={createCompany}
    >Submit</button
  >
</form>

<h1>All Companies</h1>
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
    {#each companies as company}
      <tr>
        <td>{company.name}</td>
        <td>{company.address}</td>
        <td>{company.latitude}</td>
        <td>{company.longitude}</td>
        <td>{company.id}</td>
        <td>{userMap[company.owner]?.name || userMap[company.owner]?.email || '–'}</td>
        <td>{company.email}</td>
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
                  onclick={() => openEditModal(company)}>Edit</button
                >
              </li>
              <li>
                <button
                  class="dropdown-item text-danger"
                  onclick={() => deleteCompany(company.id)}>Delete</button
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
