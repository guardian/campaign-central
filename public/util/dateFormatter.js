// format with words etc
export function formatMillisecondDate(msDate) {
  const date = new Date(msDate);

  return date.toDateString();
}

// just numbers and /s
export function shortFormatMillisecondDate(msDate) {
  const date = new Date(msDate);

  return date.toLocaleDateString();
}
