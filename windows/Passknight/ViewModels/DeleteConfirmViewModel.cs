using Passknight.Core;
using Passknight.Models;
using Passknight.Services;
using Passknight.Services.Cryptography;
using Passknight.Services.Firebase;
using Passknight.Services.PKDB;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace Passknight.ViewModels
{
    /// <summary>
    /// Dependencies: <br/> 
    /// <see cref="string"/> the name of the vault to delete <br/>
    /// <see cref="IDatabase"/> <br/>
    /// <see cref="Action"/> to execute if confirmed
    /// </summary>
    internal class DeleteConfirmViewModel : Core.ViewModel
    {
        public ICommand ConfirmDeleteCommand { get; }
        public ICommand BackCommand { get; }

        public ErrorInputField Password { get; set; } = new ErrorInputField();

        public DeleteConfirmViewModel(NavigationService navigationService, string vault, IDatabase database, Action action)
        {
            BackCommand = new RelayCommand((object? param) => navigationService.NavigateBack());
            ConfirmDeleteCommand = new RelayCommand(async (object? param) =>
            {
                var masterPasswordHash = Cryptoutils.GetMasterPasswordHash($"{vault}@passknight.vault", Password.Input);
                var response = await database.UnlockVault(vault, masterPasswordHash);
                if(response)
                {
                    action();
                    Password.ClearField();
                } 
                else
                {
                    Password.ClearField();
                    Password.SetError();
                }
            });
        }
    }
}
