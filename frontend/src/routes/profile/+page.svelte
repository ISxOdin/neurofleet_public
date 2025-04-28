<script>
  import axios from "axios";
  import { page } from "$app/state";
  import { onMount } from "svelte";
  import { jwt_token } from "../../store";
  import { isAuthenticated, user } from "../../store";

  // get the origin of current page, e.g. http://localhost:8080
  const api_root = page.url.origin;
</script>

{#if $isAuthenticated}
  <h1>Account Details</h1>
  <div class="card" style="width: 18rem;">
    <img
      src={$user.picture}
      class="card-img-top"
      alt="{$user.name}'s profile picture"
    />
    <div class="card-body">
      <h5 class="card-title"><b>{$user.name}</b></h5>
      <p class="card-text"></p>
      <p><b>Nickname:</b> {$user.nickname}</p>
      <p><b>First Name:</b> {$user.given_name}</p>
      <p><b>Last Name:</b> {$user.family_name}</p>
      <p><b>Email:</b> {$user.email}</p>
      {#if $user.user_roles && $user.user_roles.length > 0}
        <p><b>Roles:</b> {$user.user_roles}</p>
      {/if}
    </div>
  </div>
{:else}
  <p>Not logged in</p>
{/if}
