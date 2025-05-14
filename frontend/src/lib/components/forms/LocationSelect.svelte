<script>
  export let locations = [];
  export let selected = "";
  export let label = "Location";
  export let bindValue;
  export let id = "locationId";
</script>

<div class="mb-3">
  <label class="form-label" for={id}>{label}</label>
  <select class="form-select" bind:value={bindValue} {id}>
    <option disabled value="">Select location</option>
    {#each locations as location}
      <option value={location.id}>{location.name}</option>
    {/each}
  </select>
</div>
