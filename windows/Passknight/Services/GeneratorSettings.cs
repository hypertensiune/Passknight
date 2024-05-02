using Passknight.Core;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Services
{
    class GeneratorSettings : ObservableObject
    {
        private bool lowercase;
        public bool Lowercase
        {
            get => lowercase;
            set
            {
                lowercase = value;
                OnPropertyChanged(nameof(Lowercase));
                OnSettingChanged?.Invoke();
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
                OnSettingChanged?.Invoke();
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
                OnSettingChanged?.Invoke();
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
                OnSettingChanged?.Invoke();
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

                if(length != prevLength)
                {
                    prevLength = length;
                    OnSettingChanged?.Invoke();
                }
            }
        }


        public GeneratorSettings()
        {
            uppercase = true;
            numbers = true;
            symbols = true;
            lowercase = true;
            length = 5;
        }

        public event Action? OnSettingChanged;
    }
}
