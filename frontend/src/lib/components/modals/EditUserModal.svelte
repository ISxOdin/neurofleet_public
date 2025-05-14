<script>
  import { createEventDispatcher } from "svelte";

  export let user;
  export let roles = [];
  export let companies = [];

  const dispatch = createEventDispatcher();

  let selectedRole = user.role;
  let selectedCompanyId = user.companyId;

  function cancel() {
    dispatch("cancel");
  }

  function save() {
    dispatch("save", {
      userId: user.user_id,
      selectedRole,
      selectedCompanyId,
    });
  }
</script>

<div class="modal-backdrop show"></div>
<div class="modal d-block" tabindex="-1" style="background: rgba(0,0,0,0.5)">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content bg-dark text-light">
      <div class="modal-header">
        <h5 class="modal-title">Edit User</h5>
        <button class="btn-close btn-close-white" on:click={cancel}></button>
      </div>
      <div class="modal-body">
        <p><strong>{user.given_name} {user.family_name}</strong></p>
        <p>{user.email}</p>

        <label>Role</label>
        <select class="form-select mb-2" bind:value={selectedRole}>
          <option value="">–</option>
          {#each roles as r}
            <option value={r}>{r}</option>
          {/each}
        </select>

        <label>Company</label>
        <select class="form-select mb-2" bind:value={selectedCompanyId}>
          <option value="">–</option>
          {#each companies as c}
            <option value={c.id}>{c.name}</option>
          {/each}
        </select>
      </div>
      <div class="modal-footer">
        <button class="btn btn-secondary" on:click={cancel}>Cancel</button>
        <button class="btn btn-primary" on:click={save}>Save</button>
      </div>
    </div>
  </div>
</div>
