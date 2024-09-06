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

// https://github.com/bitwarden/clients/blob/main/libs/common/src/tools/generator/password/password-generation.service.ts
export function generatePassword(options: GeneratorOptions): string {
  let output = "";

  const lowercase = "abcdefghijklmnopqrstuvwxyz";
  const uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  const numbers = "01234567890";
  const symbols = "!@#$%^&*";

  let positions = "";
  let chars = "";

  // Make sure at least one of every type of characters is used if selected.
  if(options.lowercase)
  {
    chars += lowercase;
    positions += 'l';
  }
  if(options.uppercase)
  {
    chars += uppercase;
    positions += 'u';
  }
  if(options.numbers)
  {
    chars += numbers;
    positions += 'n';
  }
  if(options.symbols)
  {
    chars += symbols;
    positions += 's';
  }

  while(positions.length < options.length)
    positions += 'a';

  const positionsShuffled = shuffle(positions.split("") as []).join("");

  if (chars.length == 0)
    return "";

  for (let i = 0; i < options.length; i++) {
    let tempChars = chars;
    switch(positionsShuffled[i])
    {
      case 'l':
        tempChars = lowercase; break;
      case 'u':
        tempChars = uppercase; break;
      case 'n':
        tempChars = numbers; break;
      case 's':
        tempChars = symbols; break;
    }
    const rand = getRandomNumber(0, tempChars.length - 1);
    output += tempChars[rand];
  }

  return output;
}