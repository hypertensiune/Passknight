using System;
using System.Collections.Generic;
using System.Configuration;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using Newtonsoft.Json.Linq;
using Passknight.Core;
using Passknight.Models;
using Passknight.Services.Firebase;

namespace Passknight.ViewModels
{
    partial class VaultListViewModel : Core.ViewModel
    {
        public List<string>? FirebaseVaults { get; set; }
        public List<string>? PKDBVaults { get; set; }

        private readonly Services.NavigationService _navigationService;
        private readonly IDatabase _firebase;
        private readonly IDatabase _pkdb;
        public ICommand UnlockFirebaseVaultCommand { get; }
        public ICommand UnlockPKDBVaultCommand { get; }
        public ICommand NewVaultCommand { get; }

        public VaultListViewModel(Services.NavigationService navigationService)
        {
            _navigationService = navigationService;

            _pkdb = new Services.PKDB.Database();

            // If the firebase configuration file exists read the API KEY from it and create a new firebase instance.
            if(File.Exists("firebase"))
            {
                var key = File.ReadAllText("firebase");
                _firebase = new Firebase(key);
            }

            UnlockFirebaseVaultCommand = new RelayCommand(UnlockFirebaseVaultCommandHandler);
            UnlockPKDBVaultCommand = new RelayCommand(UnlockPKDBVaultCommandHandler);

            NewVaultCommand = new RelayCommand(NewVaultCommandHandler);

            GetVaults();
        }

        private async void GetVaults()
        {
            if(_firebase != null)
            {
                FirebaseVaults = await _firebase.GetVaultNames();
                OnPropertyChanged(nameof(FirebaseVaults));
            }

            PKDBVaults = await _pkdb.GetVaultNames();
            OnPropertyChanged(nameof(PKDBVaults));
        }

        private void UnlockFirebaseVaultCommandHandler(object? param)
        {
            _navigationService.NavigateTo<VaultUnlockViewModel>(_firebase, param!);
        }

        private void UnlockPKDBVaultCommandHandler(object? param)
        {
            _navigationService.NavigateTo<VaultUnlockViewModel>(_pkdb, param!);
        }

        private void NewVaultCommandHandler(object? param)
        {
            _navigationService.NavigateTo<NewVaultViewModel>(_firebase, _pkdb, FirebaseVaults, PKDBVaults);
        }
    }
}
