<script>
  import { createEventDispatcher } from "svelte";
  import { jwt_token } from "../../../store";
  import { browser } from "$app/environment";
  import axios from "axios";
  import { onMount } from "svelte";

  let apiRoot = "";
  let newCompany = { name: "", email: "", address: "" };

  onMount(async () => {
    if (browser) apiRoot = window.location.origin;
  });

  const dispatch = createEventDispatcher();

  function cancel() {
    dispatch("cancel");
  }

  function save() {
    dispatch("save", local);
  }

  function validateEmailAndCreateCompany() {
    var config = {
      method: "get",
      url: "https://disify.com/api/email/" + newCompany.email,
    };

    axios(config)
      .then(function (response) {
        console.log("Validated email " + newCompany.email);
        console.log(response.data);
        if (
          response.data.format &&
          !response.data.disposable &&
          response.data.dns
        ) {
          createCompany();
        } else {
          alert("Email " + newCompany.email + " is not valid.");
        }
      })
      .catch(function (error) {
        alert("Could not validate email");
        console.log(error);
      });
  }

  async function createCompany() {
    try {
      await axios.post(`${apiRoot}/api/companies`, newCompany, {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${$jwt_token}`,
        },
      });
      alert("Company created");
      newCompany = { name: "", email: "", address: "" };
      dispatch("created");
    } catch (error) {
      console.error("Could not create company", error);
      alert("Could not create company");
    }
  }
</script>

<div class="modal-backdrop show"></div>
<div class="modal d-block" tabindex="-1" style="background:rgba(0,0,0,0.5)">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content bg-dark text-light">
      <div class="modal-header">
        <h5 class="modal-title">Create Company</h5>
        <button class="btn-close btn-close-white" onclick={cancel}></button>
      </div>
      <form onsubmit={createCompany} class="mb-5">
        <div class="modal-form">
          <div class="form-group">
            <label class="form-label">Name</label>
            <input
              class="form-control"
              bind:value={newCompany.name}
              required
              placeholder="Neurofleet AG"
            />
          </div>
          <div class="form-group">
            <label class="form-label">Email</label>
            <input
              type="email"
              class="form-control"
              bind:value={newCompany.email}
              required
              placeholder="maxmustermann@neurofleet.com"
            />
          </div>
          <div class="form-group">
            <label class="form-label">Address</label>
            <input
              class="form-control"
              bind:value={newCompany.address}
              required
              placeholder="Bahnhofstrasse 1, 8001 Zürich, Switzerland"
            />
          </div>
        </div>
        <button
          type="button"
          class="btn btn-primary"
          onclick={validateEmailAndCreateCompany}>Submit</button
        >
      </form>
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

  .form-group {
    display: flex;
    flex-direction: column;
    gap: 0.3rem;
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

  .modal-form {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    margin-bottom: 1rem;
  }

  .form-label {
    color: #ccc;
    font-size: 0.95rem;
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

  .modal-content {
    padding: 2rem 2rem 1.5rem 2rem;
    border-radius: 1rem;
    background: #343c44;
  }
</style>
