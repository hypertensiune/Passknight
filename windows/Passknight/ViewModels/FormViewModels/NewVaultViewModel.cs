using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using Passknight.Core;
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

        public string Name { get; set; }
        public string MasterPassword { get; set; }
        public string ConfirmMasterPassword { get; set; }

        public ICommand ConfirmCommand { get; }
        public ICommand BackCommand { get; }

        public NewVaultViewModel(NavigationService navigationService, Firebase firebase)
        {
            _navigationService = navigationService;
            _firebase = firebase;

            MasterPassword = "";
            ConfirmMasterPassword = "";

            BackCommand = new RelayCommand((object? obj) => _navigationService.NavigateBack());
            ConfirmCommand = new RelayCommand(SubmitNewVault);
        }

        private async void SubmitNewVault(object? param)
        {
            var response = await _firebase.CreateNewVault(Name, MasterPassword);
            if(response)
            {
                _navigationService.NavigateTo<VaultViewModel>(_firebase);
            }
        }
    }
}
