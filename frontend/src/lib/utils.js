export function findUserCompany(companies, userId) {
  return companies.find((c) => c.userIds?.includes(userId))?.id || null;
}

export function findUserLocation(locations, userId) {
  return (
    locations.find(
      (l) => l.userIds?.includes(userId) || l.fleetmanagerId === userId
    )?.id || null
  );
}
