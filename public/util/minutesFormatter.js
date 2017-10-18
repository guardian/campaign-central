export function formatToMinutes(seconds) {
  const minutesPart = Math.floor(seconds / 60);
  const secondsPart = Math.floor(seconds - minutesPart * 60);
  return `${minutesPart}.${secondsPart}`;
}
