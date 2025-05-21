<script>
  import "./styles.css";
  import { isAuthenticated, user, hasAnyRole, isAdmin } from "../store";
  import auth from "../auth.service";
  import { page } from "$app/stores";

  $: currentPath = $page.url.pathname;
</script>

<div class="container-fluid">
  <div class="row flex-nowrap">
    {#if $isAuthenticated}
      <!-- Sidebar -->
      <div class="col-auto px-0">
        <div
          id="sidebar"
          class="collapse collapse-horizontal show position-fixed"
          style="top: 54px; height: calc(100% - 56px); background-color: #343C44;"
        >
          <div
            id="sidebar-nav"
            class="list-group border-0 rounded-0 text-sm-start min-vh-100 text-white"
          >
            <ul class="nav nav-pills flex-column mb-auto">
              <li class="nav-item">
                <a
                  href="/"
                  class="nav-link p-3"
                  class:custom-active={currentPath === "/"}
                >
                  <i class="bi bi-house-door-fill me-2 accent-color"></i> Home
                </a>
              </li>
              {#if $isAuthenticated && $user.user_roles && hasAnyRole("admin", "owner", "fleetmanager")}
                <li></li>
                <li class="nav-item">
                  <a
                    href="/"
                    class="nav-link p-3"
                    class:custom-active={currentPath === "/dashboard"}
                  >
                    <i class="bi bi-speedometer2 me-2 accent-color"></i> Dashboard
                  </a>
                </li>
                <li>
                  <a
                    href="/vehicles"
                    class="nav-link p-3 text-white"
                    class:custom-active={currentPath === "/vehicles"}
                  >
                    <i class="bi bi-truck me-2 accent-color"></i> Fleet Management
                  </a>
                </li>
                <li>
                  <a
                    href="/jobs"
                    class="nav-link p-3 text-white"
                    class:custom-active={currentPath === "/jobs"}
                  >
                    <i class="bi bi-box me-2 accent-color"></i> Transport Jobs
                  </a>
                </li>
                <li>
                  <a
                    href="/routes"
                    class="nav-link p-3 text-white"
                    class:custom-active={currentPath === "/routes"}
                  >
                    <i class="bi bi-sign-turn-left me-2 accent-color"></i> Routes
                  </a>
                </li>

                {#if $isAuthenticated && $user.user_roles && hasAnyRole("admin", "owner")}
                  <li>
                    <a
                      class="nav-link text-white p-3 d-flex justify-content-between align-items-center"
                      data-bs-toggle="collapse"
                      href="#productsSubmenu"
                      role="button"
                      aria-expanded={currentPath.startsWith("/companies") ||
                      currentPath.startsWith("/locations")
                        ? "true"
                        : "false"}
                      aria-controls="productsSubmenu"
                    >
                      <span
                        ><i class="bi bi-grid-fill me-2 accent-color"></i> Admin</span
                      >
                      <i class="bi bi-caret-down-fill"></i>
                    </a>
                    <div
                      class="collapse {currentPath.startsWith('/companies') ||
                      currentPath.startsWith('/locations') ||
                      currentPath.startsWith('/users')
                        ? 'show'
                        : ''}"
                      id="productsSubmenu"
                    >
                      <ul
                        class="btn-toggle-nav list-unstyled fw-normal pb-1 small ms-4"
                      >
                        <li>
                          <a
                            href="/users"
                            class="nav-link p-3 text-white"
                            class:custom-active={currentPath === "/users"}
                          >
                            <i class="bi bi-people-fill me-2 accent-color"></i> Users
                          </a>
                        </li>
                        <li>
                          <a
                            href="/locations"
                            class="nav-link text-white py-3"
                            class:custom-active={currentPath === "/locations"}
                          >
                            <i class="bi bi-geo-alt me-2 accent-color"
                            ></i>Locations
                          </a>
                        </li>
                        {#if $isAuthenticated && $user.user_roles && isAdmin}
                          <li>
                            <a
                              href="/companies"
                              class="nav-link text-white py-3"
                              class:custom-active={currentPath === "/companies"}
                            >
                              <i class="bi bi-building me-2 accent-color"></i>
                              Companies
                            </a>
                          </li>
                        {/if}
                      </ul>
                    </div>
                  </li>
                {/if}
              {:else if $isAuthenticated && $user.user_roles && $user.user_roles.includes("driver")}
                <li class="nav-item">
                  <a
                    href="/driverjobs"
                    class="nav-link p-3"
                    class:custom-active={currentPath === "/driverjobs"}
                  >
                    <i class="bi bi-truck me-2 accent-color"></i> Your Jobs
                  </a>
                </li>
              {/if}
            </ul>
          </div>
        </div>
      </div>
    {/if}

    <!-- Main Content -->
    <div class="col p-0 offset-0" style="margin-left: 180px;">
      <!-- Navbar -->
      <nav
        class="navbar navbar-expand-lg navbar-light fixed-top"
        style="background-color: #343C44;"
      >
        <div
          class="container-fluid d-flex align-items-center justify-content-between"
        >
          <!-- Left Side: Menu Button + Logo -->
          <div class="d-flex align-items-center">
            {#if $isAuthenticated}
              <button
                class="btn btn-outline-light me-2"
                type="button"
                data-bs-toggle="collapse"
                data-bs-target="#sidebar"
                aria-controls="sidebar"
                aria-expanded="true"
                aria-label="Toggle navigation"
              >
                <i class="bi bi-list accent-color"></i>
              </button>
            {/if}

            <img src="/images/Writing.png" alt="Logo" class="navbar-logo" />
          </div>

          <!-- Right Side: User Dropdown -->
          {#if $isAuthenticated}
            <div class="dropdown">
              <a
                href="#"
                class="d-flex align-items-center text-white text-decoration-none dropdown-toggle"
                id="userDropdown"
                data-bs-toggle="dropdown"
                aria-expanded="false"
              >
                <img
                  src={$user.picture}
                  alt=""
                  width="32"
                  height="32"
                  class="rounded-circle me-2"
                />
                <strong>{$user.nickname}</strong>
              </a>
              <ul
                class="dropdown-menu dropdown-menu-dark dropdown-menu-end text-small shadow"
                aria-labelledby="userDropdown"
              >
                <li><a class="dropdown-item" href="#">Settings</a></li>
                <li><a class="dropdown-item" href="/profile">Profile</a></li>
                <li><hr class="dropdown-divider" /></li>
                <li>
                  <a class="dropdown-item" href="#" onclick={auth.logout}
                    >Sign out</a
                  >
                </li>
              </ul>
            </div>
          {/if}
        </div>
      </nav>

      <!-- Page Content -->
      <main class="pt-5 mt-3">
        <div class="container">
          <slot />
        </div>
      </main>

      <!-- Chatbot Button -->
      {#if $isAuthenticated}
        <button
          class="chatbot-button"
          onclick={() => window.dispatchEvent(new CustomEvent("toggleChatbot"))}
          aria-label="Toggle Chatbot"
        >
          <i class="bi bi-chat-dots-fill"></i>
        </button>
      {/if}
    </div>
  </div>
</div>

<style>
  /* Farbdefinitionen */
  :root {
    --base-color: #343c44;
    --accent-color: #95d4ee;
  }

  #sidebar-nav {
    width: 200px;
  }

  main {
    margin-top: 80px;
    padding: 1rem;
  }

  .dropdown-toggle {
    outline: 0;
  }

  .nav-link {
    display: flex;
    align-items: center;
    font-size: 1rem;
    color: white;
    transition:
      background-color 0.3s,
      color 0.3s;
  }

  .nav-link i {
    font-size: 1.2rem;
  }

  .custom-active {
    background-color: rgba(149, 212, 238, 0.2);
    font-weight: bold;
  }

  .nav-link:hover {
    background-color: rgba(149, 212, 238, 0.2);
    color: var(--accent-color);
  }

  .accent-color {
    color: var(--accent-color);
  }

  .navbar-brand {
    font-weight: bold;
  }

  .navbar-logo {
    height: 20px;
    width: auto;
  }

  .chatbot-button {
    position: fixed;
    bottom: 2rem;
    right: 2rem;
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background-color: var(--accent-color);
    border: none;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    z-index: 1000;
  }

  .chatbot-button i {
    font-size: 1.5rem;
    color: var(--base-color);
  }

  .chatbot-button:hover {
    transform: scale(1.1);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
  }

  .chatbot-button:active {
    transform: scale(0.95);
  }

  /* Add animation for the chat icon */
  @keyframes pulse {
    0% {
      box-shadow: 0 0 0 0 rgba(149, 212, 238, 0.4);
    }
    70% {
      box-shadow: 0 0 0 10px rgba(149, 212, 238, 0);
    }
    100% {
      box-shadow: 0 0 0 0 rgba(149, 212, 238, 0);
    }
  }

  .chatbot-button {
    animation: pulse 2s infinite;
  }
</style>
