<script>
  import { createEventDispatcher } from "svelte";

  export let company;
  export let users = [];

  const dispatch = createEventDispatcher();

  let local = { ...company };

  function cancel() {
    dispatch("cancel");
  }

  function save() {
    dispatch("save", local);
  }
</script>

<div class="modal-backdrop show"></div>
<div class="modal d-block" tabindex="-1" style="background:rgba(0,0,0,0.5)">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content bg-dark text-light">
      <div class="modal-header">
        <h5 class="modal-title">Edit Company</h5>
        <button class="btn-close btn-close-white" onclick={cancel}></button>
      </div>
      <div class="modal-body">
        <label>Name</label>
        <input
          class="form-control mb-2"
          bind:value={local.name}
          placeholder="Neurofleet AG"
        />
        <label>Email</label>
        <input
          class="form-control mb-2"
          type="email"
          bind:value={local.email}
          placeholder="max@neurofleet.com"
        />
        <label>Address</label>
        <input
          class="form-control mb-2"
          bind:value={local.address}
          placeholder="Bahnhofstrasse 1, Zürich"
        />
        <label>Owner</label>
        <select class="form-select mb-2" bind:value={local.owner}>
          <option value="">-- Select owner --</option>
          {#each users as u}
            <option value={u.id}
              >{u.given_name} {u.family_name} ({u.email})</option
            >
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
