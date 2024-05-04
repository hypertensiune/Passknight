using Passknight.Core;
using Passknight.Models;
using Passknight.Services;
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

        public ErrorInputField Password { get; set; } = new ErrorInputField();

        public ICommand BackCommand { get; }
        public ICommand UnlockVaultCommand { get; }

        private Services.NavigationService _navigationService;
        private Firebase _firebase;

        public VaultUnlockViewModel(Services.NavigationService navigationService, Firebase firebase, string vault)
        {
            _navigationService = navigationService;
            _firebase = firebase;

            _vault = vault;

            BackCommand = new RelayCommand(OnBackClick);
            UnlockVaultCommand = new RelayCommand(OnUnlockVaultCommand);
        }

        private async void OnUnlockVaultCommand(object? param)
        {
            var response = await _firebase.UnlockVault(_vault, Password.Input);
            Password.ClearField();
            if (response)
            {
                _navigationService.NavigateTo<VaultViewModel>(_firebase, Password.Input);
            }
            else
            {
                Password.SetError();
            }  
        }

        private void OnBackClick(object? param)
        {
            _navigationService.NavigateBack();
        }
    }
}
