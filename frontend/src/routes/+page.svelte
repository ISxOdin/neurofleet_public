<script>
  import { onMount, onDestroy } from "svelte";
  import { isAuthenticated, user } from "../store";
  import { goto } from "$app/navigation";
  import auth from "../auth.service";

  onMount(() => {
    document.body.classList.add("login-page");
  });

  onDestroy(() => {
    document.body.classList.remove("login-page");
  });

  let username = $state("");
  let password = $state("");
  let loginForm = $state();

  function loginWithUsernameAndPassword(event) {
    event.preventDefault();
    if (loginForm.checkValidity()) {
      auth.login(username, password);
    }
    loginForm.classList.add("was-validated");
  }

  function redirectBasedOnRole() {
    const roles = $user?.user_roles || [];
    if (roles.includes("driver")) {
      goto("/driverjobs");
    } else if (
      roles.includes("admin") ||
      roles.includes("owner") ||
      roles.includes("fleetmanager")
    ) {
      goto("/dashboard");
    } else {
      alert(
        "No dashboard available for your role. Please contact your administrator to assign you a role if you don't have one."
      );
    }
  }
</script>

{#if $isAuthenticated}
  <div class="container mt-5 text-center text-white">
    <h1>Welcome to Neurofleet!</h1>
    <p class="mt-3">
      Hello <strong>{$user.nickname}</strong>, your role is
      <strong>{$user.user_roles.join(", ")}</strong>.
    </p>
    <button class="btn btn-primary mt-4" onclick={redirectBasedOnRole}>
      Go to your Dashboard
    </button>
  </div>
{:else}
  <!-- Login Formular -->
  <div class="login-background">
    <div class="container mt-5">
      <div class="row justify-content-center">
        <div class="col-md-6">
          <div class="card">
            <div class="card-header">Login</div>
            <div class="card-body">
              <form
                onsubmit={loginWithUsernameAndPassword}
                bind:this={loginForm}
                class="needs-validation"
                novalidate
              >
                <div class="mb-3">
                  <label for="username" class="form-label">E-Mail</label>
                  <input
                    bind:value={username}
                    type="email"
                    class="form-control"
                    id="username"
                    name="username"
                    required
                  />
                  <div class="invalid-feedback">
                    Please provide your e-mail address.
                  </div>
                </div>

                <div class="mb-3">
                  <label for="password" class="form-label">Password</label>
                  <input
                    bind:value={password}
                    type="password"
                    class="form-control"
                    id="password"
                    name="password"
                    required
                  />
                  <div class="invalid-feedback">
                    Please provide your password.
                  </div>
                </div>

                <div class="row align-items-end">
                  <div class="col">
                    <button type="submit" class="btn btn-primary">Log in</button
                    >
                  </div>
                  <div class="col-auto">
                    <a href="/signup">Sign up</a>
                  </div>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
{/if}

<style>
  .login-background {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: url("/images/Background.png") no-repeat center center fixed;
    background-size: cover;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: -1;
  }

  .container.mt-5 {
    position: relative;
    z-index: 1;
  }

  img {
    max-width: 100%;
    height: auto;
  }

  .card {
    background-color: #3c4650;
    padding: 2rem;
    border-radius: 20px;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.4);
    color: white;
  }

  .card-header {
    font-size: 1.5rem;
    font-weight: bold;
    text-align: left;
    border-bottom: none;
    margin-bottom: 1.5rem;
  }

  .form-label {
    font-size: 0.9rem;
    margin-bottom: 0.25rem;
  }

  .form-control {
    padding: 0.75rem 1rem;
    border-radius: 10px;
    border: none;
    font-size: 1rem;
    margin-bottom: 0.5rem;
  }

  .form-control::placeholder {
    color: #aaa;
  }

  .invalid-feedback {
    font-size: 0.8rem;
    color: #ffc107;
  }

  .row.align-items-end {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 1rem;
  }

  a {
    color: #ccc;
    font-size: 0.85rem;
    text-decoration: none;
  }

  a:hover {
    text-decoration: underline;
  }
</style>
