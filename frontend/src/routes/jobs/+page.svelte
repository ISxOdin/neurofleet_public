<script>
  import axios from "axios";
  import { page } from "$app/state";
  import { onMount } from "svelte";
  import {
    jwt_token,
    user,
    isAdmin,
    isOwner,
    isFleet,
    hasAnyRole,
  } from "../../store";
  import flatpickr from "flatpickr";
  import "flatpickr/dist/flatpickr.min.css";
  import CompanySelect from "$lib/components/forms/CompanySelect.svelte";
  import LocationSelect from "$lib/components/forms/LocationSelect.svelte";
  import VehicleSelect from "$lib/components/forms/VehicleSelect.svelte";

  const api_root = page.url.origin;

  let currentPage = 1;
  let nrOfPages = 0;
  let defaultPageSize = 20;

  let myCompanyId = null;
  let myLocationId = null;

  let vehicles = [];
  let companies = [];
  let locations = [];
  let jobs = [];
  let types = [];

  let loading = false;
  let showModal = false;
  let scheduledTime = "";

  let job = {
    description: "",
    scheduledTime: "",
    originId: "",
    destinationId: "",
    vehicleId: "",
    companyId: "",
    locationId: "",
  };

  const sub = encodeURIComponent($user.sub);

  onMount(async () => {
    await getCompanies();
    await getLocations();
    await getVehicles();
    getJobs();

    flatpickr("#scheduledTime", {
      enableTime: true,
      dateFormat: "Y-m-d\\TH:i",
      time_24hr: true,
    });

    if (!isAdmin && myCompanyId) {
      job.companyId = myCompanyId;
    }

    if (isFleet && myLocationId) {
      job.locationId = myLocationId;
    }
  });

  async function getCompanies() {
    try {
      const res = await axios.get(api_root + "/api/companies", {
        headers: { Authorization: "Bearer " + $jwt_token },
      });
      companies = res.data.content;
      const myCo =
        companies.find((c) => c.userIds?.includes($user.sub)) ||
        companies.find((c) => c.owner === $user.sub);
      myCompanyId = myCo?.id || null;
    } catch (e) {
      alert("Could not load companies");
    }
  }

  async function getLocations() {
    try {
      const res = await axios.get(api_root + "/api/locations", {
        headers: { Authorization: "Bearer " + $jwt_token },
      });

      locations = res.data.content;

      const myLo =
        locations.find((l) => l.userIds?.includes($user.sub)) ||
        locations.find((l) => l.fleetmanagerId === $user.sub);

      myLocationId = myLo?.id || null;
    } catch (e) {
      alert("Could not load locations");
    }
  }

  function getVehicles() {
    loading = true;
    axios
      .get(api_root + "/api/vehicles", {
        headers: { Authorization: "Bearer " + $jwt_token },
      })
      .then((res) => {
        vehicles = res.data.content;
      })
      .catch(() => alert("Could not load vehicles"))
      .finally(() => (loading = false));
  }

  function getTypes() {
    axios
      .get(api_root + "/api/vehicles/types", {
        headers: { Authorization: "Bearer " + $jwt_token },
      })
      .then((res) => {
        types = res.data;
      })
      .catch(() => alert("Could not load vehicle types"));
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
      placeholder="Description"
    />
  </div>

  <label class="form-label" for="scheduledTime">Scheduled Time</label>
  <div class="input-group mb-3 datepicker-wrapper" style="max-width: 220px;">
    <span class="input-group-text"><i class="bi bi-calendar-date"></i></span>
    <input
      id="scheduledTime"
      bind:value={job.scheduledTime}
      type="text"
      class="form-control"
      placeholder="YYYY-MM-DD"
    />
  </div>

  {#if isAdmin}
    <CompanySelect bind:bindValue={job.companyId} {companies} />
  {/if}

  {#if hasAnyRole("admin", "owner")}
    <LocationSelect
      bind:bindValue={job.originId}
      locations={locations.filter((loc) => loc.companyId === job.companyId)}
      label="Origin"
      id="originId"
    />
    {#if locations.filter((loc) => loc.companyId === job.companyId).length === 0}
      <div class="text-muted mb-3">
        <p style="color: red">No locations available for selected company</p>
      </div>
    {/if}
    <LocationSelect
      bind:bindValue={job.destinationId}
      locations={locations.filter(
        (loc) => loc.companyId === job.companyId && loc.id !== job.originId
      )}
      label="Destination"
      id="destinationId"
    />
  {/if}

  <VehicleSelect
    bind:bindValue={job.vehicleId}
    vehicles={vehicles.filter((vehicle) => vehicle.locationId === job.originId)}
  />

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
