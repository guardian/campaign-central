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

export function isoFormatMillisecondDate(msDate) {
  const date = new Date(msDate);

  return date.toISOString();
}

// 'dd/mm' eg '02/11'
export function ddmmFormatDate(date) {
  return new Date(date).toLocaleDateString('en-GB', {day: '2-digit', month: '2-digit'})
}

// 'ddd, dd mmm' eg 'Wed, 02 Nov'
export function dddddmmmFormatDate(date) {
  return new Date(date).toLocaleDateString('en-GB', {weekday: 'short', day: '2-digit', month: 'short'})
}
