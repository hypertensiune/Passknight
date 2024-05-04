using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using Passknight.Core;
using Passknight.Models;
using Passknight.Services;
using Passknight.Services.Firebase;

namespace Passknight.ViewModels
{
    /// <summary>
    /// Dependecies: <see cref="Firebase"/>
    /// </summary>
    class NewVaultViewModel : Core.ViewModel
    {
        private NavigationService _navigationService;
        private Firebase _firebase;

        public ErrorInputField Name { get; set; } = new ErrorInputField();
        public ErrorInputField Password { get; set; } = new ErrorInputField();
        public ErrorInputField Confirm { get; set; } = new ErrorInputField();

        public ICommand ConfirmCommand { get; }
        public ICommand BackCommand { get; }

        public NewVaultViewModel(NavigationService navigationService, Firebase firebase)
        {
            _navigationService = navigationService;
            _firebase = firebase;

            //MasterPassword = "";
            //ConfirmMasterPassword = "";

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

            var response = await _firebase.CreateNewVault(Name.Input, Password.Input);
            if (response)
            {
                _navigationService.NavigateTo<VaultViewModel>(_firebase);
            }
        }
    }
}
