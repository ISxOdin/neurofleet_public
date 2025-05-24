<script>
  import axios from "axios";
  import { onMount } from "svelte";
  import { browser } from "$app/environment";
  import { jwt_token, isAuthenticated, user, isOwner } from "../../store";
  import { fade, fly } from "svelte/transition";
  import { findUserCompany } from "$lib/utils";
  import { goto } from "$app/navigation";

  let companies = [];
  let locations = [];
  let loading = false;
  let apiRoot = "";
  let myCompanyId = null;

  let currentPage = 1;
  let defaultPageSize = 5;
  let nrOfPages = 0;
  let sub = encodeURIComponent($user.sub);

  onMount(async () => {
    if (browser) {
      apiRoot = window.location.origin;
      await getMyCompanies();
      if (myCompanyId) {
        await getMyLocations();
      }
    }
  });

  async function getMyCompanies() {
    try {
      const response = await axios.get(`${apiRoot}/api/companies/user/${sub}`, {
        headers: { Authorization: `Bearer ${$jwt_token}` },
      });
      companies = response.data;
      if (isOwner) {
        myCompanyId = findUserCompany(companies, $user.sub);
      }
    } catch (err) {
      console.error("Could not load companies", err);
    }
  }

  async function getMyLocations() {
    try {
      let response;
      if (isOwner && myCompanyId) {
        // For owners, get all locations of their company
        response = await axios.get(
          `${apiRoot}/api/locations?pageNumber=1&pageSize=100&companyId=${myCompanyId}`,
          { headers: { Authorization: `Bearer ${$jwt_token}` } }
        );
        locations = response.data.content;
      } else {
        // For other roles, get locations where they are a member or fleet manager
        response = await axios.get(`${apiRoot}/api/locations/user/${sub}`, {
          headers: { Authorization: `Bearer ${$jwt_token}` },
        });
        locations = response.data;
      }
    } catch (err) {
      console.error("Could not load locations", err);
    }
  }

  function goToLogin() {
    goto("/");
  }
</script>

{#if $isAuthenticated}
  <div class="profile-container" in:fade={{ duration: 300 }}>
    {#if loading}
      <div class="text-center my-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
      </div>
    {:else}
      <div class="profile-header" in:fly={{ y: 20, duration: 400 }}>
        <div class="profile-avatar-wrapper">
          <img src={$user.picture} alt={$user.name} class="profile-avatar" />
          <div class="profile-status"></div>
        </div>
        <h2 class="profile-name">{$user.name}</h2>
        <p class="profile-email">
          <i class="bi bi-envelope"></i>
          {$user.email}
        </p>
        <p class="profile-id">
          <i class="bi bi-fingerprint"></i>
          {$user.sub}
        </p>
      </div>

      <div class="profile-content">
        <div
          class="profile-section"
          in:fly={{ y: 20, duration: 400, delay: 200 }}
        >
          <h3 class="section-title">
            <i class="bi bi-person-circle"></i>
            Personal Information
          </h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="key-info"> Nickname</span>
              <span class="value">{$user.nickname || "Not set"}</span>
            </div>
            <div class="info-item">
              <span class="key-info">First Name</span>
              <span class="value">{$user.given_name || "Not set"}</span>
            </div>
            <div class="info-item">
              <span class="key-info"> Last Name</span>
              <span class="value">{$user.family_name || "Not set"}</span>
            </div>
          </div>
        </div>

        <div
          class="profile-section"
          in:fly={{ y: 20, duration: 400, delay: 300 }}
        >
          <h3 class="section-title">
            <i class="bi bi-briefcase"></i>
            Professional Information
          </h3>
          {#if $user.user_roles?.length > 0}
            <div class="roles-section">
              <h4 class="subsection-title">
                <i class="bi bi-shield-check"></i> Roles
              </h4>
              <div class="roles-container">
                {#each $user.user_roles as role}
                  <span class="role-badge" in:fade>
                    {role}
                  </span>
                {/each}
              </div>
            </div>
          {/if}

          <div class="companies-section">
            {#if companies.length > 0}
              <h4 class="subsection-title">
                <i class="bi bi-building"></i> Companies
              </h4>
              {#each companies as company}
                <div class="card" in:fade>
                  <div class="company-info">
                    <span class="key-info">Company</span>
                    <span class="value">{company.name}</span>
                    <span class="id"
                      ><i class="bi bi-database"></i>{company.id}</span
                    >
                    {#if company.address}
                      <span class="address"
                        ><i class="bi bi-geo-alt"></i>
                        {company.address}</span
                      >
                    {/if}
                    {#if company.latitude && company.longitude}
                      <span class="address"
                        ><i class="bi bi-geo-alt-fill"></i>
                        {company.latitude}, {company.longitude}</span
                      >
                    {/if}
                  </div>
                </div>
              {/each}
            {/if}

            {#if locations.length > 0}
              <h4 class="subsection-title">
                <i class="bi bi-geo"></i> Locations
              </h4>
              {#each locations as location}
                <div class="card" in:fade>
                  <div class="company-info">
                    <span class="key-info">Location</span>
                    <span class="value">{location.name}</span>
                    <span class="id"
                      ><i class="bi bi-database"></i>{location.id}</span
                    >
                    {#if location.address}
                      <span class="address"
                        ><i class="bi bi-geo-alt"></i>
                        {location.address}</span
                      >
                    {/if}
                    {#if location.latitude && location.longitude}
                      <span class="address"
                        ><i class="bi bi-geo-alt-fill"></i>
                        {location.latitude}, {location.longitude}</span
                      >
                    {/if}
                  </div>
                </div>
              {/each}
            {/if}
          </div>
        </div>
      </div>
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
  .profile-container {
    max-width: 800px;
    margin: 4rem auto;
    background: #343c44;
    border-radius: 1.5rem;
    box-shadow: 0 8px 32px rgba(0, 68, 102, 0.15);
    padding: 2.5rem;
    border-left: 8px solid #95d4ee;
  }

  .profile-header {
    text-align: center;
    margin-bottom: 2.5rem;
  }

  .profile-avatar-wrapper {
    position: relative;
    display: inline-block;
    margin-bottom: 1.5rem;
  }

  .profile-avatar {
    width: 140px;
    height: 140px;
    border-radius: 9999px;
    object-fit: cover;
    border: 4px solid #95d4ee;
    box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.1);
    transition: transform 0.3s ease;
  }

  .profile-avatar:hover {
    transform: scale(1.05);
  }

  .profile-status {
    position: absolute;
    bottom: 10px;
    right: 10px;
    width: 16px;
    height: 16px;
    background-color: #4caf50;
    border: 2px solid #343c44;
    border-radius: 50%;
  }

  .profile-name {
    color: white;
    font-size: 2rem;
    font-weight: 700;
    margin-bottom: 0.5rem;
  }

  .profile-email,
  .profile-id {
    color: #95d4ee;
    font-size: 0.9rem;
    margin: 0.25rem 0;
  }

  .profile-email i,
  .profile-id i {
    margin-right: 0.5rem;
    opacity: 0.8;
  }

  .profile-content {
    display: grid;
    gap: 2rem;
  }

  .profile-section {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 1rem;
    padding: 1.5rem;
  }

  .section-title {
    font-size: 1.25rem;
    color: white;
    margin-bottom: 1.5rem;
    padding-bottom: 0.75rem;
    border-bottom: 2px solid #95d4ee;
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .section-title i {
    color: #95d4ee;
  }

  .info-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1.5rem;
  }

  .info-item {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    background: rgba(255, 255, 255, 0.03);
    padding: 1rem;
    border-radius: 0.75rem;
    transition: all 0.3s ease;
  }

  .info-item:hover {
    background: rgba(255, 255, 255, 0.05);
  }

  .key-info {
    font-weight: 600;
    color: #95d4ee;
    display: flex;
    align-items: center;
    font-size: 0.9rem;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    text-align: left;
  }

  .key-info i {
    font-size: 0.9rem;
    opacity: 0.8;
  }

  .value {
    color: white;
    font-size: 1.1rem;
    text-align: left;
  }

  .roles-section {
    margin-bottom: 1.5rem;
  }

  .roles-section .key-info {
    padding-left: 0.5rem;
  }

  .roles-container {
    display: flex;
    flex-wrap: wrap;
    gap: 0.75rem;
    margin-top: 0.75rem;
  }

  .role-badge {
    background-color: rgba(149, 212, 238, 0.1);
    color: #95d4ee;
    border: 1px solid #95d4ee;
    border-radius: 999px;
    padding: 0.5rem 1rem;
    font-size: 0.9rem;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    transition: all 0.3s ease;
    min-width: 100px;
    text-align: center;
  }

  .role-badge:hover {
    background-color: rgba(149, 212, 238, 0.2);
    transform: translateY(-2px);
  }

  .role-badge i {
    font-size: 0.8rem;
    flex-shrink: 0;
  }

  .companies-section {
    display: grid;
    gap: 1rem;
  }

  .subsection-title {
    color: #95d4ee;
    font-size: 1.1rem;
    margin: 1.5rem 0 1rem;
    padding-left: 0.5rem;
    border-left: 3px solid #95d4ee;
  }

  .card {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 0.75rem;
    padding: 1rem;
    display: flex;
    align-items: flex-start;
    gap: 1rem;
    transition: all 0.3s ease;
    border-left: 4px solid #95d4ee;
    text-align: left;
  }

  .card:hover {
    background: rgba(255, 255, 255, 0.08);
    transform: translateX(5px);
  }

  .card i {
    font-size: 1.5rem;
  }

  .card i {
    color: #95d4ee;
  }

  .company-info {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
    text-align: left;
    flex: 1;
  }

  .address {
    color: #95d4ee;
    font-size: 0.9rem;
    margin-top: 0.25rem;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    text-align: left;
  }

  .address i {
    font-size: 0.8rem;
    opacity: 0.8;
  }
  .id {
    color: white;
    font-size: 0.9rem;
    margin-top: 0.25rem;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    text-align: left;
  }

  .id i {
    font-size: 0.8rem;
    opacity: 0.8;
  }

  .not-authenticated {
    color: #95d4ee;
    padding: 3rem;
    background: #343c44;
    border-radius: 1rem;
    box-shadow: 0 4px 16px rgba(0, 68, 102, 0.1);
  }

  @media (max-width: 768px) {
    .profile-container {
      margin: 2rem 1rem;
      padding: 1.5rem;
    }

    .info-grid {
      grid-template-columns: 1fr;
    }

    .profile-avatar {
      width: 120px;
      height: 120px;
    }
  }
</style>
