﻿using Passknight.Core;
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
        public string Vault { get; set; }

        public ErrorInputField Password { get; set; } = new ErrorInputField();

        public ICommand BackCommand { get; }
        public ICommand UnlockVaultCommand { get; }

        private Services.NavigationService _navigationService;
        private IDatabase _database;

        public VaultUnlockViewModel(Services.NavigationService navigationService, IDatabase database, string vault)
        {
            _navigationService = navigationService;
            _database = database;

            Vault = vault;

            BackCommand = new RelayCommand(OnBackClick);
            UnlockVaultCommand = new RelayCommand(OnUnlockVaultCommand);
        }

        private async void OnUnlockVaultCommand(object? param)
        {
            var masterPasswordHash = Cryptoutils.GetMasterPasswordHash($"{Vault}@passknight.vault", Password.Input);

            var response = await _database.UnlockVault(Vault, masterPasswordHash);
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
