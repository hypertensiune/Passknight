using Passknight.Core;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Models
{
    class ErrorInputField : ObservableObject
    {
        private string _input;
        public bool Error { get; set; } = false;
        public string Input
        {
            get => _input;
            set
            {
                _input = value;
                ClearError();
                OnPropertyChanged(nameof(Input));
            }
        }

        public ErrorInputField()
        {
            Input = "";
        }

        public void ClearField()
        {
            Input = string.Empty;
        }

        public void ClearError()
        {
            Error = false;
            OnPropertyChanged(nameof(Error));
        }

        public void SetError()
        {
            Error = true;
            OnPropertyChanged(nameof(Error));
        }
    }
}
