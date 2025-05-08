<script>
  import axios from "axios";
  import { onMount } from "svelte";
  import { browser } from "$app/environment";
  import { jwt_token, isAuthenticated, user } from "../../store";

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
  <h1>Account Details</h1>

  {#if loading}
    <div class="d-flex justify-content-center my-4">
      <div class="spinner-border" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>
  {:else}
    <div class="card" style="width: 18rem;">
      <img
        src={$user.picture}
        class="card-img-top"
        alt="{$user.name}'s profile picture"
      />
      <div class="card-body">
        <h5 class="card-title"><b>{$user.name}</b></h5>
        <p><b>Nickname:</b> {$user.nickname}</p>
        <p><b>First Name:</b> {$user.given_name}</p>
        <p><b>Last Name:</b> {$user.family_name}</p>
        <p><b>Email:</b> {$user.email}</p>
        {#if $user.user_roles?.length > 0}
          <p><b>Roles:</b> {$user.user_roles.join(", ")}</p>
        {/if}
        <p><b>ID:</b> {$user.sub}</p>
        {#each companies as company}
          {#if company.userIds?.includes($user.sub)}
            <p><b>Company:</b> {company.name}</p>
          {/if}
        {/each}
      </div>
    </div>
  {/if}
{:else}
  <p>Not logged in</p>
{/if}
