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
    isAuthenticated,
  } from "../../store";
  import flatpickr from "flatpickr";
  import "flatpickr/dist/flatpickr.min.css";
  import CompanySelect from "$lib/components/forms/CompanySelect.svelte";
  import LocationSelect from "$lib/components/forms/LocationSelect.svelte";
  import VehicleSelect from "$lib/components/forms/VehicleSelect.svelte";
  import EditJobModal from "$lib/components/modals/EditJobModal.svelte";
  import { goto } from "$app/navigation";

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
  let lastOriginId = "";
  let lastCompanyId = "";
  let selectedJob = null;

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

  $: if (job.companyId !== lastCompanyId) {
    lastCompanyId = job.companyId;
    job.originId = "";
    job.destinationId = "";
    job.vehicleId = "";
  }

  $: if (job.originId !== lastOriginId) {
    lastOriginId = job.originId;
    job.destinationId = "";
    job.vehicleId = "";
  }

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

  async function getVehicles() {
    loading = true;
    try {
      const res = await axios.get(api_root + "/api/vehicles", {
        headers: { Authorization: "Bearer " + $jwt_token },
      });
      vehicles = res.data.content;
    } catch {
      alert("Could not load vehicles");
    } finally {
      loading = false;
    }
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
    axios
      .get(api_root + "/api/jobs", {
        headers: { Authorization: "Bearer " + $jwt_token },
      })
      .then((res) => {
        jobs = res.data.content;
      })
      .catch((error) => {
        alert("Could not load jobs");
        console.error(error);
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

  function openEditJob(jobData) {
    selectedJob = { ...jobData };
    showModal = true;
  }

  function closeEditJobModal() {
    showModal = false;
    selectedJob = null;
  }

  function deleteJob(jobId) {
    if (!confirm("Are you sure you want to delete this job?")) return;

    axios
      .delete(`${api_root}/api/jobs/${jobId}`, {
        headers: {
          Authorization: `Bearer ${$jwt_token}`,
        },
      })
      .then(() => {
        alert("Job deleted");
        getJobs();
      })
      .catch(() => {
        alert("Deletion failed");
      });
  }

  function saveEditedJob(jobData) {
    axios
      .put(`${api_root}/api/jobs/${jobData.id}`, jobData, {
        headers: {
          Authorization: `Bearer ${$jwt_token}`,
          "Content-Type": "application/json",
        },
      })
      .then(() => {
        alert("Job updated");
        closeEditJobModal();
        getJobs();
      })
      .catch(() => {
        alert("Update failed");
      });
  }

  function goToLogin() {
    goto("/");
  }
</script>

{#if $isAuthenticated}
  <div class="jobs-container">
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
      <div
        class="input-group mb-3 datepicker-wrapper"
        style="max-width: 220px;"
      >
        <span class="input-group-text"><i class="bi bi-calendar-date"></i></span
        >
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
        {#if job.companyId && locations.filter((loc) => loc.companyId === job.companyId).length === 0}
          <div class="text-muted mb-3">
            <p style="color: red">
              No locations available for selected company
            </p>
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
        vehicles={vehicles.filter(
          (vehicle) => vehicle.locationId === job.originId
        )}
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
          <th scope="col">Company</th>
          <th scope="col">Origin</th>
          <th scope="col">Destination</th>
          <th scope="col">Vehicle</th>
          <th scope="col">State</th>
          <th scope="col">Actions</th>
        </tr>
      </thead>
      <tbody>
        {#each jobs as j}
          <tr>
            <td>{j.description}</td>
            <td>{j.scheduledTime}</td>
            <td>{companies.find((c) => c.id === j.companyId)?.name}</td>
            <td>{locations.find((l) => l.id === j.originId)?.name}</td>
            <td>{locations.find((l) => l.id === j.destinationId)?.name}</td>
            <td>{vehicles.find((v) => v.id === j.vehicleId)?.licensePlate}</td>
            <td>{j.jobState}</td>
            <td>
              <div class="dropdown">
                <button
                  class="btn btn-sm btn-outline-secondary"
                  type="button"
                  data-bs-toggle="dropdown"
                >
                  <i class="bi bi-gear-fill"></i> Edit
                </button>
                <ul
                  class="dropdown-menu dropdown-menu-dark dropdown-menu-end text-small shadow"
                >
                  <li>
                    <a class="dropdown-item" onclick={() => openEditJob(j)}
                      >Edit</a
                    >
                  </li>
                  <li>
                    <a
                      class="dropdown-item text-danger"
                      onclick={() => deleteJob(j.id)}>Delete</a
                    >
                  </li>
                </ul>
              </div>
            </td>
          </tr>
        {/each}
      </tbody>
    </table>

    {#if showModal}
      <EditJobModal
        {selectedJob}
        {locations}
        {vehicles}
        on:close={closeEditJobModal}
        on:save={(e) => saveEditedJob(e.detail)}
      />
    {/if}
  </div>
{:else}
  <div class="container mt-5 text-center" in:fade>
    <div class="not-authenticated">
      <i class="bi bi-lock-fill fa-3x mb-3"></i>
      <p>You are not logged in.</p>
      <button class="btn btn-primary mt-3" onclick={goToLogin}>
        Go to Login
      </button>
    </div>
  </div>
{/if}

<style>
</style>
