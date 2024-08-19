using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using Passknight.Core;
using Passknight.Models;
using Passknight.Services;
using Passknight.Services.Cryptography;
using Passknight.Services.Firebase;
using Passknight.Services.PKDB;

namespace Passknight.ViewModels
{
    /// <summary>
    /// Dependecies: <see cref="IDatabase"/>
    /// </summary>
    class NewVaultViewModel : Core.ViewModel
    {
        private NavigationService _navigationService;
        private IDatabase _firebase;
        private IDatabase _pkdb;

        public int VaultType { get; set; } = 0;

        public ErrorInputField Name { get; set; } = new ErrorInputField();
        public ErrorInputField Password { get; set; } = new ErrorInputField();
        public ErrorInputField Confirm { get; set; } = new ErrorInputField();

        public ICommand ConfirmCommand { get; }
        public ICommand BackCommand { get; }

        public NewVaultViewModel(NavigationService navigationService, IDatabase firebase, IDatabase pkdb)
        {
            _navigationService = navigationService;
            _firebase = firebase;
            _pkdb = pkdb;

            BackCommand = new RelayCommand((object? obj) => _navigationService.NavigateBack());
            ConfirmCommand = new RelayCommand(SubmitNewVault);
        }

        private async void SubmitNewVault(object? param)
        {
            if (Name.Input == "")
            {
                Name.SetError();
            }
            
            if (Password.Input.Length < 15)
            {
                Password.SetError();
            }

            if (Password.Input != Confirm.Input)
            {
                Confirm.SetError();
            }

            if(Name.Error || Password.Error || Confirm.Error)
            {
                return;
            }

            var (masterPasswordHash, protectedSymmetricKey) = Cryptoutils.Create($"{Name.Input}@passknight.vault", Password.Input);

            if(VaultType == 0)
            {
                var response = await _firebase.CreateNewVault(Name.Input, masterPasswordHash, protectedSymmetricKey);
                if (response)
                {
                    _navigationService.NavigateTo<VaultViewModel>(_firebase, Password.Input);
                }
            }
            else
            {
                var response = await _pkdb.CreateNewVault(Name.Input, masterPasswordHash, protectedSymmetricKey);
                if (response)
                {
                    _navigationService.NavigateTo<VaultViewModel>(_pkdb, Password.Input);
                }
            }

        }
    }
}
