using Passknight.Core;
using Passknight.Services;
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
        public ICommand ClearHistoryCommand { get; }

        public HistoryViewModel(Services.NavigationService navigationService, List<string> history, Action clear)
        {
            History = history;

            BackCommand = new RelayCommand((object? param) => navigationService.NavigateBack());
            CopyCommand = new RelayCommand((object? param) => Clipboard.SetText(param as string));
            ClearHistoryCommand = new RelayCommand(async (object? param) =>
            {
                var response = await Msgbox.ShowYesNo("Clear history", "Are you sure you want to clear\nthe generated passwords history?");
                if(response == Wpf.Ui.Controls.MessageBoxResult.Primary)
                {
                    clear();
                    navigationService.NavigateBack();
                }
            });
        }
    }
}
