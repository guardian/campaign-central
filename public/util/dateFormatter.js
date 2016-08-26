export function formatMillisecondDate(msDate) {
  const date = new Date(msDate);

  return date.toDateString();
}
