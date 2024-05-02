using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Services
{
    /// <summary>
    /// Generate secure random passwords.
    /// <a href="https://github.com/bitwarden/clients/blob/main/libs/common/src/tools/generator/password/password-generation.service.ts"/>
    /// </summary>
    class Generator
    {
        private string lowercase = "abcdefghijklmnopqrstuvwxyz";
        private string uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        private string numbers = "0123456789";
        private string symbols = "!@#$%^&*";

        private Random random = new Random();

        private string ShufflePositions(string positions)
        {
            var shuffled = positions.ToCharArray();
            for(int i = 0; i < shuffled.Length; i++)
            {
                int j = random.Next(0, shuffled.Length - 1);
                char tmp = shuffled[i];
                shuffled[i] = shuffled[j];
                shuffled[j] = tmp;
            }

            return new string(shuffled);
        }

        public string GeneratePassword(GeneratorSettings settings)
        {
            string positions = "";

            string characters = string.Empty;
            if (settings.Lowercase)
            {
                characters += lowercase;
                positions += 'l';
            }
            if(settings.Uppercase)
            {
                characters += uppercase;
                positions += 'u';
            }
            if (settings.Numbers)
            {
                characters += numbers;
                positions += 'n';
            }
            if (settings.Symbols)
            {
                characters += symbols;
                positions += 's';
            }

            while (positions.Length < settings.Length)
                positions += 'a';

            var shuffledPositions = ShufflePositions(positions);

            string password = "";
            for(int i = 0; i < settings.Length; i++)
            {
                string chars = characters;
                switch(shuffledPositions[i])
                {
                    case 'l':
                        chars = lowercase; break;
                    case 'u':
                        chars = uppercase; break;
                    case 'n':
                        chars = numbers; break;
                    case 's':
                        chars = symbols; break;
                }

                int rand = random.Next(0, chars.Length - 1);
                password += chars[rand];
            }

            return password;
        }
    }
}
