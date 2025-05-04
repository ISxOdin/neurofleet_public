<script>
  import { isAuthenticated, user } from "../store";
  import { goto } from "$app/navigation";
  import auth from "../auth.service";

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
      alert("No dashboard available for your role. Please contact your administrator to assign you a role if you don't have one.");
    }
  }
</script>

{#if $isAuthenticated}
  <div class="container mt-5 text-center text-white">
    <h1>Welcome to Neurofleet!</h1>
    <p class="mt-3">Hello <strong>{$user.nickname}</strong>, your role is <strong>{$user.user_roles.join(", ")}</strong>.</p>
    <button class="btn btn-primary mt-4" onclick={redirectBasedOnRole}>
      Go to your Dashboard
    </button>
  </div>
{:else}
  <!-- Login Formular -->
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
                  <button type="submit" class="btn btn-primary">Log in</button>
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
{/if}

<style>
  body {
    color: #fff;
    background: #343c44;
  }

  img {
    max-width: 100%;
    height: auto;
  }
</style>
