using Passknight.Core;
using Passknight.Models;
using Passknight.Models.Items;
using Passknight.Services;
using Passknight.Services.Firebase;
using Passknight.ViewModels.FormViewModels;
using Passknight.Extensions;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media.Imaging;
using Passknight.Services.Generator;
using System.IO;
using Newtonsoft.Json;
using System.Reflection.PortableExecutable;
using Passknight.Services.Cryptography;

namespace Passknight.ViewModels
{
    /// <summary>
    /// Dependencies: <see cref="Firebase"/>
    /// </summary>
    class VaultViewModel : Core.ViewModel
    {
        private readonly NavigationService _navigationService;
        private readonly IDatabase _database;

        private Cryptography _cryptography;

        public Vault Vault { get; private set; }

        public ICommand LockVaultCommand { get; }
        public ICommand OpenPasswordItemAddFormCommand { get; }
        public ICommand OpenPasswordItemEditFormCommand { get; }
        public ICommand OpenNoteItemAddFormCommand { get; }
        public ICommand OpenNoteItemEditFormCommand { get; }

        public ICommand CopyUsernameCommand { get; }
        public ICommand CopyPasswordCommand { get; }

        public ICommand DeleteVaultCommand { get; }

        public ICommand RegeneratePasswordCommand { get; }
        public ICommand CopyGeneratedPasswordCommand { get; }
        public ICommand OpenHistoryCommand { get; }

        public string GeneratedPassword { get; set; }
        public Settings GeneratorSettings { get; } = new Settings();
        private Generator generator = new Generator();

        private readonly object _valueLock = new object();

        public VaultViewModel(Services.NavigationService navigationService, IDatabase database, string password)
        {
            _navigationService = navigationService;
            _database = database;

            _ = GetVaultAsync().Then(() => _cryptography = new Cryptography($"{Vault.Name}@passknight.vault", password, Vault.Psk));

            LockVaultCommand = new RelayCommand(Lock);
            DeleteVaultCommand = new RelayCommand(DeleteVaultCommandHandler);

            OpenPasswordItemAddFormCommand = new RelayCommand(OpenPasswordItemAddFormCommandHanlder);
            OpenPasswordItemEditFormCommand = new RelayCommand(OpenPasswordItemEditFormCommandHandler);

            OpenNoteItemAddFormCommand = new RelayCommand(OpenNoteItemAddFormCommandHanlder);
            OpenNoteItemEditFormCommand = new RelayCommand(OpenNoteItemEditFormCommandHandler);

            CopyUsernameCommand = new RelayCommand((object? param) => Clipboard.SetText((string)param!));
            CopyPasswordCommand = new RelayCommand((object? param) => Clipboard.SetText((_cryptography.Decrypt((string)param!))));

            GeneratorSettings.OnSettingsChanged += RegeneratePassword;

            GeneratedPassword = generator.GeneratePassword(GeneratorSettings);

            RegeneratePasswordCommand = new RelayCommand((object? param) => RegeneratePassword());
            CopyGeneratedPasswordCommand = new RelayCommand((object? param) => Clipboard.SetText((string)param!));

            OpenHistoryCommand = new RelayCommand((object? param) => navigationService.NavigateTo<HistoryViewModel>(Vault.GeneratorHistory));
        }

        private async Task GetVaultAsync()
        {
            Vault = await _database.GetVault();
            OnPropertyChanged(nameof(Vault));
        }

        private void Lock(object? param)
        {
            _database.LockVault();
            _navigationService.NavigateTo<VaultListViewModel>();
            _navigationService.InvalidateNavigateBack();
        }

        private void DeleteVaultCommandHandler(object? param)
        {
            _navigationService.NavigateTo<DeleteConfirmViewModel>(DeleteVault);
        }

        private void DeleteVault()
        {
            _database.DeleteVault();
            _navigationService.NavigateTo<VaultListViewModel>();
            _navigationService.InvalidateNavigateBack();
        }

        private void OpenPasswordItemAddFormCommandHanlder(object? param)
        {
            _navigationService.NavigateTo<PasswordFormViewModel>(_database, _cryptography, FormType.Add, Vault.PasswordItems);
        }

        private void OpenPasswordItemEditFormCommandHandler(object? param)
        {
            _navigationService.NavigateTo<PasswordFormViewModel>(_database, _cryptography, FormType.Edit, (PasswordItem)param!, Vault.PasswordItems);
        }

        private void OpenNoteItemAddFormCommandHanlder(object? param)
        {
            _navigationService.NavigateTo<NoteFormViewModel>(_database, _cryptography, FormType.Add, Vault.NoteItems);
        }

        private void OpenNoteItemEditFormCommandHandler(object? param)
        {
            _navigationService.NavigateTo<NoteFormViewModel>(_database, _cryptography, FormType.Edit, (NoteItem)param!, Vault.NoteItems);
        }

        private void RegeneratePassword()
        {
            GeneratedPassword = generator.GeneratePassword(GeneratorSettings);
            OnPropertyChanged(nameof(GeneratedPassword));

            // Save the newly generated password in the vault.
            // To avoid saving too many passwords if continously dragging the slider add a 1000ms delay after the slider is last moved.
            // If the slider is moved again re-add the delay.
            // This is the C# implementation of the timeout method used in TypeScript
            // https://github.com/hypertensiune/PassKnight/blob/master/extension/src/pages/Vault/Tabs/GeneratorTab/index.tsx
            //
            // https://stackoverflow.com/questions/723502/wpf-slider-with-an-event-that-triggers-after-a-user-drags
            lock (_valueLock)
            Monitor.PulseAll(_valueLock);
            Task.Run(() =>
            {
                lock (_valueLock)
                if(!Monitor.Wait(_valueLock, 1000))
                {
                    Vault.GeneratorHistory.Add(GeneratedPassword);
                    if(Vault.GeneratorHistory.Count > 15) 
                    {
                        Vault.GeneratorHistory.RemoveAt(0);    
                    }
                    _database.SetGeneratorHistory(Vault.GeneratorHistory);
                }
            });
        }
    }
}
