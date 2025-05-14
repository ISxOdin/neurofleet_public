<script>
  export let companies = [];
  export let selected = "";
  export let disabled = false;

  export let bindValue;
</script>

<div class="mb-3">
  <label class="form-label" for="companyId">Company</label>
  <select class="form-select" id="companyId" bind:value={bindValue} {disabled}>
    <option disabled selected value="">Select company</option>
    {#each companies as company}
      <option value={company.id}>{company.name}</option>
    {/each}
  </select>
</div>
