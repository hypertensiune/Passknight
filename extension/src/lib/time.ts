export function getTimestamp(): string {
  return Date.now().toString();
}

export function getDateFromTimestamp(timestamp: string): string {
  const millis = Number.parseInt(timestamp);
  const date = new Date(millis).toLocaleString();
  console.log(date);
  return date;
}