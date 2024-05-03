using Passknight.Stores;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;

namespace Passknight.ViewModels
{
    internal class MainViewModel : Core.ViewModel
    {
        private Services.NavigationService NavigationService { get; set; }
        public Core.ViewModel CurrentViewModel => NavigationService.CurrentViewModel;
        
        public MainViewModel(Services.NavigationService navigationService)
        {
            NavigationService = navigationService;
            NavigationService.OnNavigate += OnNavigate;
        }

        private void OnNavigate()
        {
            OnPropertyChanged(nameof(CurrentViewModel));
        }
    }
}
