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
        public List<string>? Vaults { get; set; }

        private Services.NavigationService _navigationService;
        private IDatabase _database;
        public ICommand UnlockCommand { get; }
        public ICommand NewVaultCommand { get; }

        public VaultListViewModel(Services.NavigationService navigationService, IDatabase database)
        {
            _navigationService = navigationService;
            _database = database;

            UnlockCommand = new RelayCommand(UnlockCommandHandler);
            NewVaultCommand = new RelayCommand(NewVaultCommandHandler);

            GetVaults();
        }

        private async void GetVaults()
        {
            Vaults = await _database.GetVaultNames();
            OnPropertyChanged(nameof(Vaults));
        }

        private void UnlockCommandHandler(object? param)
        {
            _navigationService.NavigateTo<VaultUnlockViewModel>(_database, param!);
        }

        private void NewVaultCommandHandler(object? param)
        {
            _navigationService.NavigateTo<NewVaultViewModel>(_database);
        }
    }
}
