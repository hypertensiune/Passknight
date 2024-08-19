using Passknight.Core;
using Passknight.Models;
using Passknight.Services.Cryptography;
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
    /// Dependencies: <see cref="IDatabase"/> database, <see cref="string"/> vault
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
        private IDatabase _database;

        public VaultUnlockViewModel(Services.NavigationService navigationService, IDatabase database, string vault)
        {
            _navigationService = navigationService;
            _database = database;

            _vault = vault;

            BackCommand = new RelayCommand(OnBackClick);
            UnlockVaultCommand = new RelayCommand(OnUnlockVaultCommand);
        }

        private async void OnUnlockVaultCommand(object? param)
        {
            var masterPasswordHash = Cryptoutils.GetMasterPasswordHash($"{_vault}@passknight.vault", Password.Input);

            var response = await _database.UnlockVault(_vault, masterPasswordHash);
            if (response)
            {
                _navigationService.NavigateTo<VaultViewModel>(_database, Password.Input);
                Password.ClearField();
            }
            else
            {
                Password.ClearField();
                Password.SetError();
            }  
        }

        private void OnBackClick(object? param)
        {
            _navigationService.NavigateBack();
        }
    }
}
