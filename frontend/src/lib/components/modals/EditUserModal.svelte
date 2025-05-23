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
        <button class="btn-close btn-close-white" onclick={cancel}></button>
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
        <button class="btn btn-secondary" onclick={cancel}>Cancel</button>
        <button class="btn btn-primary" onclick={save}>Save</button>
      </div>
    </div>
  </div>
</div>

<style>
  .modal-backdrop {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.6);
    z-index: 999;
  }

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #95d4ee;
    margin-bottom: 1.5rem;
  }

  .modal-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-top: 1px solid #95d4ee;
    margin-bottom: 1.5rem;
  }

  label {
    display: block;
    font-weight: 600;
    margin-bottom: 0.3rem;
    color: #95d4ee;
  }

  .form-select {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 4px;
    background: rgba(255, 255, 255, 0.1);
    color: #fff;
  }

  .form-select:focus {
    background: rgba(255, 255, 255, 0.15);
    border-color: #95d4ee;
    color: #fff;
    box-shadow: none;
  }

  .form-select,
  .form-select option {
    background-color: #2a2e36 !important;
    color: #fff !important;
  }

  .form-select {
    appearance: none;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' fill='%2395d4ee' viewBox='0 0 16 16'%3E%3Cpath d='M7.247 11.14 2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z'/%3E%3C/svg%3E") !important;
    background-repeat: no-repeat !important;
    background-position: right 0.75rem center !important;
    background-size: 16px 12px !important;
    padding-right: 2.5rem !important;
  }

  .modal-content {
    padding: 2rem 2rem 1.5rem 2rem;
    border-radius: 1rem;
    background: #343c44;
  }

  .btn-primary {
    background: transparent !important;
    color: #95d4ee !important;
    border: none !important;
    font-weight: 600;
  }

  .btn-primary:hover {
    background: rgba(149, 212, 238, 0.1) !important;
    color: #fff !important;
  }

  .btn-secondary {
    background: transparent !important;
    color: #95d4ee !important;
    border: none !important;
    font-weight: 600;
  }

  .btn-secondary:hover {
    background: rgba(149, 212, 238, 0.1) !important;
    color: #fff !important;
  }

  .modal-body p {
    margin-bottom: 1rem;
  }

  .modal-body p strong {
    color: #fff;
    font-size: 1.1rem;
  }

  .modal-body p:not(:has(strong)) {
    color: #95d4ee;
    font-size: 0.9rem;
  }
</style>
