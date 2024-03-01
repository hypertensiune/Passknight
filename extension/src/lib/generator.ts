function getRandomNumber(min: number, max: number): number {
  const arr = new Uint8Array(1);
  crypto.getRandomValues(arr);

  return arr[0] % (max - min + 1) + min;
}

function shuffle(array: []): [] {
  for (let i = 0; i < array.length; i++) {
    const j = getRandomNumber(0, i);
    [array[i], array[j]] = [array[j], array[i]];
  }

  return array;
}

export function generatePassword(length: number, options: { lowercase: boolean, uppercase: boolean, numbers: boolean, symbols: boolean }): string {
  let output = "";

  const lowercase = "abcdefghijklmnopqrstuvwxyz";
  const uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  const numbers = "01234567890";
  const symbols = "!@#$%^&*";

  const chars = (options.lowercase ? lowercase : "") + (options.uppercase ? uppercase : "") + (options.numbers ? numbers : "") + (options.symbols ? symbols : "");
  const charsShuffled = shuffle(chars.split("") as []).join("");

  if (chars.length == 0)
    return "";

  for (let i = 0; i < length; i++) {
    const randomIndex = getRandomNumber(0, chars.length - 1);
    output += charsShuffled[randomIndex];
  }

  return output;
}