<script>
  import { createEventDispatcher } from "svelte";
  import { isAdmin, isOwner } from "../../../store";
  import CompanySelect from "../forms/CompanySelect.svelte";
  const dispatch = createEventDispatcher();

  let location = {
    name: "",
    address: "",
    companyId: "",
  };
  export let companies = [];

  function handleSubmit() {
    dispatch("created", location);
    location = { name: "", address: "", companyId: "" };
  }

  function handleCancel() {
    dispatch("cancel");
    location = { name: "", address: "", companyId: "" };
  }
</script>

<div class="modal-backdrop show"></div>
<div class="modal d-block" tabindex="-1" style="background:rgba(0,0,0,0.5)">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content bg-dark text-light">
      <div class="modal-header">
        <h5 class="modal-title">Create Location</h5>
        <button class="btn-close btn-close-white" onclick={handleCancel}
        ></button>
      </div>
      <div class="modal-body">
        <label>Name</label>
        <input
          class="form-control mb-2"
          bind:value={location.name}
          placeholder="Zurich HQ"
          required
        />
        <label>Address</label>
        <input
          class="form-control mb-2"
          bind:value={location.address}
          placeholder="Bahnhofstrasse 1, Zürich"
          required
        />
        {#if $isAdmin}
          <label>Company</label>
          <select class="form-select mb-3" bind:value={location.companyId}>
            <option disabled selected value="">Select company</option>
            {#each companies as company}
              <option value={company.id}>{company.name}</option>
            {/each}
          </select>
        {:else if $isOwner}
          <input type="hidden" bind:value={location.companyId} />
        {/if}
      </div>
      <div class="modal-footer">
        <button class="btn btn-secondary" onclick={handleCancel}>Cancel</button>
        <button class="btn btn-primary" onclick={handleSubmit}>Create</button>
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

  input.form-control {
    width: 100%;
    padding: 0.6rem;
    border: none;
    border-radius: 0.5rem;
    background: #2a2e36;
    color: white;
  }

  .form-control {
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: #fff;
    padding: 0.75rem;
    border-radius: 4px;
  }

  .form-control:focus {
    background: rgba(255, 255, 255, 0.15);
    border-color: #95d4ee;
    color: #fff;
    box-shadow: none;
  }

  .form-control::placeholder {
    color: rgba(255, 255, 255, 0.5);
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
    padding: 0.75rem;
    font-size: 1rem;
    font-weight: bold;
    border-radius: 10px;
    cursor: pointer;
    margin-top: 1rem;
  }
  .btn-secondary:hover {
    background: rgba(149, 212, 238, 0.1) !important;
    color: #fff !important;
  }
  .form-select,
  .form-select option {
    background: #2a2e36 !important;
    color: #fff !important;
  }

  .form-select {
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: #fff;
    padding: 0.75rem;
    border-radius: 4px;
  }

  .form-select:focus {
    background: rgba(255, 255, 255, 0.15);
    border-color: #95d4ee;
    color: #fff;
    box-shadow: none;
  }
</style>
