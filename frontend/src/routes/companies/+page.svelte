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
  </script>
  
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
          <td>{company.owner}</td>
          <td>{company.email}</td>
        </tr>
      {/each}
    </tbody>
  </table>
  
  <nav>
    <ul class="pagination">
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
    </ul>
  </nav>
  
  <style>
    .page-link {
      box-shadow: none;
    }
  </style>
  