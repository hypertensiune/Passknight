using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using Passknight.Core;
using Passknight.Services;

namespace Passknight.ViewModels
{
    /// <summary>
    /// Dependencies: <see cref="IDatabase"/>
    /// </summary>
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

        public VaultListViewModel(Services.NavigationService navigationService, IDatabase firebase)
        {
            _navigationService = navigationService;
            _firebase = firebase;

            _pkdb = new Services.PKDB.Database();

            UnlockFirebaseVaultCommand = new RelayCommand(UnlockFirebaseVaultCommandHandler);
            UnlockPKDBVaultCommand = new RelayCommand(UnlockPKDBVaultCommandHandler);

            NewVaultCommand = new RelayCommand(NewVaultCommandHandler);

            GetVaults();
        }

        private async void GetVaults()
        {
            FirebaseVaults = await _firebase.GetVaultNames();
            OnPropertyChanged(nameof(FirebaseVaults));

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
            _navigationService.NavigateTo<NewVaultViewModel>(_firebase, _pkdb);
        }
    }
}
