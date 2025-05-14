<script>
  import { createEventDispatcher } from "svelte";

  export let location;
  export let users = [];
  export let companyId;

  const dispatch = createEventDispatcher();
  let local = { ...location };

  function save() {
    dispatch("save", local);
  }

  function cancel() {
    dispatch("cancel");
  }
</script>

<div class="modal-backdrop show"></div>
<div class="modal d-block" tabindex="-1" style="background: rgba(0,0,0,0.5)">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content bg-dark text-light">
      <div class="modal-header">
        <h5 class="modal-title">Edit Location</h5>
        <button class="btn-close btn-close-white" onclick={cancel}></button>
      </div>
      <div class="modal-body">
        <label>Name</label>
        <input class="form-control mb-2" bind:value={local.name} />

        <label>Address</label>
        <input class="form-control mb-2" bind:value={local.address} />

        <label>Fleet Manager</label>
        <select class="form-select mb-2" bind:value={local.fleetmanagerId}>
          <option disabled value="">-- Select Fleet Manager --</option>
          {#each users.filter((u) => u.role === "fleetmanager" && u.companyId === companyId) as u}
            <option value={u.user_id}>
              {u.given_name}
              {u.family_name} ({u.email})
            </option>
          {/each}
        </select>
      </div>
      <div class="modal-footer">
        <button class="btn btn-secondary" onclick={cancel}>Cancel</button>
        <button class="btn btn-primary" onclick={save}>Save</button>
      </div>
    </div>
  </div>
</div>
