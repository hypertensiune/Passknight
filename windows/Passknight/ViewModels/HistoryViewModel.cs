using Passknight.Core;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;

namespace Passknight.ViewModels
{
    class HistoryViewModel : Core.ViewModel
    {
        public List<string> History { get; }

        public ICommand BackCommand { get; }
        public ICommand CopyCommand { get; }

        public HistoryViewModel(Services.NavigationService navigationService, List<string> history)
        {
            History = history;

            BackCommand = new RelayCommand((object? param) => navigationService.NavigateBack());
            CopyCommand = new RelayCommand((object? param) => Clipboard.SetText(param as string));
        }
    }
}
