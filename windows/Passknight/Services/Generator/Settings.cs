using Passknight.Core;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Services.Generator
{
    class Settings : ObservableObject
    {
        private bool lowercase;
        public bool Lowercase
        {
            get => lowercase;
            set
            {
                lowercase = value;
                OnPropertyChanged(nameof(Lowercase));
                OnSettingsChanged?.Invoke();
            }
        }

        private bool uppercase;
        public bool Uppercase
        {
            get => uppercase;
            set
            {
                uppercase = value;
                OnPropertyChanged(nameof(Uppercase));
                OnSettingsChanged?.Invoke();
            }
        }

        private bool numbers;
        public bool Numbers
        {
            get => numbers;
            set
            {
                numbers = value;
                OnPropertyChanged(nameof(Numbers));
                OnSettingsChanged?.Invoke();
            }
        }

        private bool symbols;
        public bool Symbols
        {
            get => symbols;
            set
            {
                symbols = value;
                OnPropertyChanged(nameof(Symbols));
                OnSettingsChanged?.Invoke();
            }
        }

        private int length;
        private int prevLength;
        public int Length
        {
            get => length;
            set
            {
                length = value;
                OnPropertyChanged(nameof(Length));

                if (length != prevLength)
                {
                    prevLength = length;
                    OnSettingsChanged?.Invoke();
                }
            }
        }


        public Settings()
        {
            uppercase = true;
            numbers = true;
            symbols = true;
            lowercase = true;
            length = 15;
        }

        public event Action? OnSettingsChanged;
    }
}
