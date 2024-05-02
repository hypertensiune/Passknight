using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;

using Passknight.Core;
using Passknight.Services.Firebase;

namespace Passknight.ViewModels
{
    /// <summary>
    /// Dependencies: <see cref="Firebase"/>
    /// </summary>
    partial class VaultListViewModel : Core.ViewModel
    {
        public List<string>? Vaults { get; set; }

        private Services.NavigationService _navigationService;
        private Firebase _firebase;
        public ICommand UnlockCommand { get; }
        public ICommand NewVaultCommand { get; }

        public VaultListViewModel(Services.NavigationService navigationService, Firebase firebase)
        {
            _navigationService = navigationService;
            _firebase = firebase;

            UnlockCommand = new RelayCommand(UnlockCommandHandler);
            NewVaultCommand = new RelayCommand(NewVaultCommandHandler);

            GetVaults();
        }

        private async void GetVaults()
        {
            Vaults = await _firebase.GetVaultNames();
            OnPropertyChanged(nameof(Vaults));
        }

        private void UnlockCommandHandler(object? param)
        {
            _navigationService.NavigateTo<VaultUnlockViewModel>(_firebase, param!);
        }

        private void NewVaultCommandHandler(object? param)
        {
            _navigationService.NavigateTo<NewVaultViewModel>(_firebase);
        }
    }
}
