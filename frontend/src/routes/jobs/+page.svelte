<script>
  import axios from "axios";
  import { page } from "$app/state";
  import { onMount } from "svelte";
  import { jwt_token } from "../../store";

  // get the origin of current page, e.g. http://localhost:8080
  const api_root = page.url.origin;

  let companies = $state([]);
  let jobs = $state([]);
  let locations = $state([]);
  let vehicles = $state([]);

  let job = $state({
    description: null,
    scheduledTime: null,
    originId: null,
    destinationId: null,
    vehicleId: null,
    companyId: null,
  });

  onMount(() => {
    getCompanies();
    getJobs();
  });

  function getCompanies() {
    var config = {
      method: "get",
      url: api_root + "/api/companies",
      headers: { Authorization: "Bearer " + $jwt_token },
    };

    axios(config)
      .then(function (response) {
        companies = response.data;
      })
      .catch(function (error) {
        alert("Could not get companies");
        console.log(error);
      });
  }

  function getJobs() {
    var config = {
      method: "get",
      url: api_root + "/api/jobs",
      headers: { Authorization: "Bearer " + $jwt_token },
    };

    axios(config)
      .then(function (response) {
        jobs = response.data;
      })
      .catch(function (error) {
        alert("Could not get jobs");
        console.log(error);
      });
  }

  function getLocations() {
  if (!job.companyId) return;

  var config = {
    method: "get",
    url: api_root + "/api/locations?companyId=" + job.companyId,
    headers: { Authorization: "Bearer " + $jwt_token },
  };

  axios(config)
    .then(function (response) {
      locations = response.data;
    })
    .catch(function (error) {
      alert("Could not get locations");
      console.log(error);
    });
}


  function getVehicles() {
  if (!job.companyId) return;

  var config = {
    method: "get",
    url: api_root + "/api/vehicles?companyId=" + job.companyId,
    headers: { Authorization: "Bearer " + $jwt_token },
  };

  axios(config)
    .then(function (response) {
      vehicles = response.data;
    })
    .catch(function (error) {
      alert("Could not get vehicles");
      console.log(error);
    });
}

  function createJob() {
    var config = {
      method: "post",
      url: api_root + "/api/jobs",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + $jwt_token,
      },
      data: job,
    };

    axios(config)
      .then(function (response) {
        alert("Job created");
        getJobs();
      })
      .catch(function (error) {
        alert("Could not create Job");
        console.log(error);
      });
  }
</script>

<h1 class="mt-3">Create Job</h1>
<form class="mb-5">
  <div class="mb-3">
    <label class="form-label" for="description">Description</label>
    <input
      bind:value={job.description}
      class="form-control"
      id="description"
      type="text"
    />
  </div>

  <div class="mb-3">
    <label class="form-label" for="scheduledTime">Scheduled Time</label>
    <input
      bind:value={job.scheduledTime}
      class="form-control"
      id="scheduledTime"
      type="datetime-local"
    />
  </div>

  <div class="row mb-3">
    <div class="col">
      <label class="form-label" for="originId">Origin</label>
      <input
        bind:value={job.originId}
        class="form-control"
        id="originId"
        type="text"
      />
    </div>
    <div class="col">
      <label class="form-label" for="destinationId">Destination</label>
      <input
        bind:value={job.destinationId}
        class="form-control"
        id="destinationId"
        type="text"
      />
    </div>
  </div>

  <div class="row mb-3">
    <div class="col">
      <label class="form-label" for="vehicleId">Vehicle ID</label>
      <input
        bind:value={job.vehicleId}
        class="form-control"
        id="vehicleId"
        type="text"
      />
    </div>
    <div class="col">
      <label class="form-label" for="companyId">Company</label>
      <select bind:value={job.companyId} class="form-select" id="companyId">
        {#each companies as company}
          <option value={company.id}>{company.name}</option>
        {/each}
      </select>
    </div>
  </div>

  <button type="button" class="btn btn-primary" onclick={createJob}
    >Submit</button
  >
</form>

<h1>All Jobs</h1>
<table class="table">
  <thead>
    <tr>
      <th scope="col">Description</th>
      <th scope="col">Scheduled Time</th>
      <th scope="col">Origin</th>
      <th scope="col">Destination</th>
      <th scope="col">Vehicle</th>
      <th scope="col">State</th>
    </tr>
  </thead>
  <tbody>
    {#each jobs as job}
      <tr>
        <td>{job.description}</td>
        <td>{job.scheduledtime}</td>
        <td>{job.origin}</td>
        <td>{job.destination}</td>
        <td>{job.vehicle}</td>
        <td>{job.state}</td>
      </tr>
    {/each}
  </tbody>
</table>
