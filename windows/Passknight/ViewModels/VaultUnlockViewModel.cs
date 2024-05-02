using Passknight.Core;
using Passknight.Services.Firebase;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Input;

namespace Passknight.ViewModels
{
    /// <summary>
    /// Dependencies: <see cref="Firebase"/> firebase, <see cref="string"/> vault
    /// </summary>
    class VaultUnlockViewModel : Core.ViewModel
    {
        private string _vault;
        public string Vault
        {
            get => "Unlock " + _vault;
            set => _vault = value;
        }

        public string Password { get; set; }

        public ICommand BackCommand { get; }
        public ICommand UnlockVaultCommand { get; }

        private Services.NavigationService _navigationService;
        private Firebase _firebase;

        public VaultUnlockViewModel(Services.NavigationService navigationService, Firebase firebase, string vault)
        {
            _navigationService = navigationService;
            _firebase = firebase;

            _vault = vault;

            Password = "";

            BackCommand = new RelayCommand(OnBackClick);
            UnlockVaultCommand = new RelayCommand(OnUnlockVaultCommand);
        }

        private async void OnUnlockVaultCommand(object? param)
        {
            var response = await _firebase.UnlockVault(_vault, Password);
            Password = string.Empty;
            if(response)
            {
                _navigationService.NavigateTo<VaultViewModel>(_firebase);
            }
            else
            {
                MessageBox.Show("Invalid master password", "Error", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private void OnBackClick(object? param)
        {
            _navigationService.NavigateBack();
        }
    }
}
