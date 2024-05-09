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

namespace Passknight.ViewModels
{
    /// <summary>
    /// Dependencies: <see cref="Firebase"/>
    /// </summary>
    class VaultViewModel : Core.ViewModel
    {
        private readonly NavigationService _navigationService;
        private readonly IDatabase _database;

        private readonly Cryptography _cryptography = new Cryptography();

        public Vault Vault { get; private set; }

        public ICommand LockVaultCommand { get; }
        public ICommand OpenPasswordItemAddFormCommand { get; }
        public ICommand OpenPasswordItemEditFormCommand { get; }
        public ICommand OpenNoteItemAddFormCommand { get; }
        public ICommand OpenNoteItemEditFormCommand { get; }

        public ICommand CopyUsernameCommand { get; }
        public ICommand CopyPasswordCommand { get; }

        public ICommand RegeneratePasswordCommand { get; }
        public ICommand CopyGeneratedPasswordCommand { get; }

        public ICommand DeleteVaultCommand { get; }

        public Settings GeneratorSettings { get; } = new Settings();
        private Generator generator = new Generator();

        private string _generatedPassword;
        public List<string> GeneratedPassword
        {
            get
            {
                var split = Regex.Matches(_generatedPassword, @"(\d+)|([a-zA-Z]+)|([!@#$%^&*]+)");
                return split.Select(gr => gr.Value).ToList();
            }
        }

        public VaultViewModel(Services.NavigationService navigationService, IDatabase database, string masterPassword)
        {
            _navigationService = navigationService;
            _database = database;

            GetVaultAsync().Then(() => _cryptography.Initialize(masterPassword, Vault.Salt));

            LockVaultCommand = new RelayCommand(Lock);
            DeleteVaultCommand = new RelayCommand(DeleteVaultCommandHandler);

            OpenPasswordItemAddFormCommand = new RelayCommand(OpenPasswordItemAddFormCommandHanlder);
            OpenPasswordItemEditFormCommand = new RelayCommand(OpenPasswordItemEditFormCommandHandler);

            OpenNoteItemAddFormCommand = new RelayCommand(OpenNoteItemAddFormCommandHanlder);
            OpenNoteItemEditFormCommand = new RelayCommand(OpenNoteItemEditFormCommandHandler);

            CopyUsernameCommand = new RelayCommand((object? param) => Clipboard.SetText((string)param!));
            CopyPasswordCommand = new RelayCommand((object? param) => Clipboard.SetText((_cryptography.Decrypt((string)param!))));

            GeneratorSettings.OnSettingsChanged += OnGeneratorSettingsChanged;

            _generatedPassword = generator.GeneratePassword(GeneratorSettings);

            RegeneratePasswordCommand = new RelayCommand((object? param) => { });
            CopyGeneratedPasswordCommand = new RelayCommand((object? param) => { });
        }

        private async Task GetVaultAsync()
        {
            Vault = await _database.GetVault();

            //var stream = new FileStream($"pkdb/local.pkvault", FileMode.OpenOrCreate, FileAccess.Write);
            //var writer = new BinaryWriter(stream);

            //writer.Write(Vault.Salt);
            //writer.Write(Vault.PasswordItems.Count);
            //foreach (var item in Vault.PasswordItems)
            //{
            //    writer.Write(item.Name);
            //    writer.Write(item.Website);
            //    writer.Write(item.Username);
            //    writer.Write(item.Password);
            //}

            //writer.Write(Vault.NoteItems.Count);
            //foreach (var item in Vault.NoteItems)
            //{
            //    writer.Write(item.Name);
            //    writer.Write(item.Content);
            //}

            //writer.Write(Vault.GeneratorHistory.Count);
            //foreach (var item in Vault.GeneratorHistory)
            //{
            //    writer.Write(item);
            //}

            //writer.Flush();

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

        private void OnGeneratorSettingsChanged()
        {
            _generatedPassword = generator.GeneratePassword(GeneratorSettings);
            OnPropertyChanged(nameof(GeneratedPassword));
        }
    }
}
