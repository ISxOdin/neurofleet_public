<script>
  import { createEventDispatcher } from "svelte";
  const dispatch = createEventDispatcher();

  let location = {
    name: "",
    address: "",
  };

  function handleSubmit() {
    dispatch("created", location);
    location = { name: "", address: "" };
  }

  function handleCancel() {
    dispatch("cancel");
    location = { name: "", address: "" };
  }
</script>

<div class="modal-backdrop">
  <div class="modal-content">
    <h2>Create Location</h2>
    <form onsubmit={handleSubmit}>
      <div class="form-group">
        <label>Name</label>
        <input
          class="form-control"
          bind:value={location.name}
          placeholder="Zurich HQ"
          required
        />
      </div>
      <div class="form-group">
        <label>Address</label>
        <input
          class="form-control"
          bind:value={location.address}
          placeholder="Bahnhofstrasse 1, 8001 Zürich, Switzerland"
          required
        />
      </div>
      <div class="modal-actions">
        <button type="button" class="btn-secondary" onclick={handleCancel}>
          Cancel
        </button>
        <button type="submit" class="btn-accent">Create Location</button>
      </div>
    </form>
  </div>
</div>

<style>
  .modal-backdrop {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
  }

  .modal-content {
    background: #343c44;
    padding: 2rem;
    border-radius: 8px;
    border: 1px solid #95d4ee;
    width: 90%;
    max-width: 500px;
  }

  .modal-content h2 {
    color: white;
    margin-bottom: 1.5rem;
    font-size: 1.4rem;
  }

  .form-group {
    margin-bottom: 1rem;
  }

  .form-group label {
    display: block;
    color: #95d4ee;
    margin-bottom: 0.5rem;
  }

  .form-control {
    width: 100%;
    padding: 0.5rem;
    border: 1px solid #4f5a65;
    border-radius: 4px;
    background: #23272e;
    color: white;
  }

  .modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
    margin-top: 1.5rem;
  }

  .btn-accent {
    background: #95d4ee;
    color: #23272e;
    border: none;
    border-radius: 4px;
    padding: 0.6rem 1.2rem;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    cursor: pointer;
    transition: background 0.2s;
  }

  .btn-accent:hover {
    background: #7bc4e6;
  }

  .btn-secondary {
    background: #4f5a65;
    color: #fff;
    border: none;
    border-radius: 4px;
    padding: 0.6rem 1.2rem;
    font-weight: 600;
    cursor: pointer;
    transition: background 0.2s;
  }

  .btn-secondary:hover {
    background: #343c44;
  }
</style>
