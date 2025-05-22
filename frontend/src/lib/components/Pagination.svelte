<script>
  export let currentPage = 1;
  export let totalPages = 0;
  export let onPageChange = (page) => {};

  function handlePageChange(page) {
    if (page < 1 || page > totalPages) return;
    onPageChange(page);
  }
</script>

<nav>
  <ul class="pagination justify-content-center">
    <li class="page-item" class:disabled={currentPage === 1}>
      <button
        class="page-link"
        onclick={() => handlePageChange(currentPage - 1)}
      >
        &laquo;
      </button>
    </li>
    {#each Array(totalPages) as _, i}
      <li class="page-item" class:active={currentPage === i + 1}>
        <button class="page-link" onclick={() => handlePageChange(i + 1)}>
          {i + 1}
        </button>
      </li>
    {/each}
    <li class="page-item" class:disabled={currentPage === totalPages}>
      <button
        class="page-link"
        onclick={() => handlePageChange(currentPage + 1)}
      >
        &raquo;
      </button>
    </li>
  </ul>
</nav>

<style>
  .page-link {
    box-shadow: none;
    border-radius: 0.5rem;
  }
  .page-item {
    margin: 0 0.5rem;
  }
</style>
