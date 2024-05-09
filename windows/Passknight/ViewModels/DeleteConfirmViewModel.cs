using Passknight.Core;
using Passknight.Services;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace Passknight.ViewModels
{
    /// <summary>
    /// Dependencies: <see cref="Action"/> to execute if confirmed
    /// </summary>
    internal class DeleteConfirmViewModel : Core.ViewModel
    {
        public ICommand YesCommand { get; }
        public ICommand NoCommand { get; }

        public DeleteConfirmViewModel(NavigationService navigationService, Action action)
        {
            NoCommand = new RelayCommand((object? param) => navigationService.NavigateBack());
            YesCommand = new RelayCommand((object? param) => action());
        }
    }
}
