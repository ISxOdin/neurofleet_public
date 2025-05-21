<script>
  import axios from "axios";
  import { onMount } from "svelte";
  import { browser } from "$app/environment";
  import { jwt_token, isAuthenticated, user } from "../../store";
  import { fade, fly } from "svelte/transition";

  let companies = [];
  let loading = false;
  let apiRoot = "";

  onMount(async () => {
    if (browser) {
      apiRoot = window.location.origin;
      await getCompanies();
    }
  });

  async function getCompanies() {
    loading = true;
    try {
      const { data } = await axios.get(`${apiRoot}/api/companies`, {
        headers: { Authorization: `Bearer ${$jwt_token}` },
      });
      companies = data.content || data;
    } catch (error) {
      console.error("Could not get companies", error);
      alert("Could not get companies");
    } finally {
      loading = false;
    }
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
          <i class="fas fa-envelope"></i>
          {$user.email}
        </p>
        <p class="profile-id">
          <i class="fas fa-fingerprint"></i>
          {$user.sub}
        </p>
      </div>

      <div class="profile-content">
        <div
          class="profile-section"
          in:fly={{ y: 20, duration: 400, delay: 200 }}
        >
          <h3 class="section-title">
            <i class="fas fa-user-circle"></i>
            Personal Information
          </h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="key-info"
                ><i class="fas fa-user-tag"></i> Nickname</span
              >
              <span class="value">{$user.nickname || "Not set"}</span>
            </div>
            <div class="info-item">
              <span class="key-info"
                ><i class="fas fa-user"></i> First Name</span
              >
              <span class="value">{$user.given_name || "Not set"}</span>
            </div>
            <div class="info-item">
              <span class="key-info"><i class="fas fa-user"></i> Last Name</span
              >
              <span class="value">{$user.family_name || "Not set"}</span>
            </div>
          </div>
        </div>

        <div
          class="profile-section"
          in:fly={{ y: 20, duration: 400, delay: 300 }}
        >
          <h3 class="section-title">
            <i class="fas fa-briefcase"></i>
            Professional Information
          </h3>
          {#if $user.user_roles?.length > 0}
            <div class="roles-section">
              <span class="key-info"
                ><i class="fas fa-user-shield"></i> Roles</span
              >
              <div class="roles-container">
                {#each $user.user_roles as role}
                  <span class="role-badge" in:fade>
                    <i class="fas fa-check-circle"></i>
                    {role}
                  </span>
                {/each}
              </div>
            </div>
          {/if}

          <div class="companies-section">
            {#each companies as company}
              {#if company.userIds?.includes($user.sub)}
                <div class="company-card" in:fade>
                  <i class="fas fa-building"></i>
                  <div class="company-info">
                    <span class="key-info">Company</span>
                    <span class="value">{company.name}</span>
                  </div>
                </div>
              {/if}
            {/each}
          </div>
        </div>
      </div>
    {/if}
  </div>
{:else}
  <div class="container mt-5 text-center" in:fade>
    <div class="not-authenticated">
      <i class="fas fa-lock fa-3x mb-3"></i>
      <p>You are not logged in.</p>
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
  }

  .key-info i {
    font-size: 0.9rem;
    opacity: 0.8;
  }

  .value {
    color: white;
    font-size: 1.1rem;
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

  .company-card {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 0.75rem;
    padding: 1rem;
    display: flex;
    align-items: center;
    gap: 1rem;
    transition: all 0.3s ease;
  }

  .company-card:hover {
    background: rgba(255, 255, 255, 0.08);
    transform: translateX(5px);
  }

  .company-card i {
    font-size: 1.5rem;
    color: #95d4ee;
  }

  .company-info {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
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
