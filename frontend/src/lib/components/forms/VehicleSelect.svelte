<script>
  export let vehicles = [];
  export let bindValue;
  export let label = "Vehicle";
  export let id = "vehicleId";
  export let disabled = false;
</script>

<div class="mb-3">
  <label class="form-label" for={id}>{label}</label>
  <select class="form-select" {id} bind:value={bindValue} {disabled}>
    <option disabled value="">Select vehicle</option>
    {#each vehicles as vehicle}
      <option value={vehicle.id}>
        {vehicle.licensePlate ?? vehicle.id} - {vehicle.vehicleType} - {vehicle.capacity}
      </option>
    {/each}
  </select>
</div>
