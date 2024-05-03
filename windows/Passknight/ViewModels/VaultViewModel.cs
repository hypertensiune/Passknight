using Passknight.Core;
using Passknight.Models;
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

namespace Passknight.ViewModels
{
    /// <summary>
    /// Dependencies: <see cref="Firebase"/>
    /// </summary>
    class VaultViewModel : Core.ViewModel
    {
        private Firebase _firebase;

        public Vault Vault { get; private set; }

        private readonly Cryptography _cryptography = new Cryptography();

        public ICommand OpenPasswordItemAddFormCommand { get; }
        public ICommand OpenPasswordItemEditFormCommand { get; }
        public ICommand BackCommand { get; }

        private readonly NavigationService _navigationService;

        #region PasswordsTab

        public ICommand CopyUsernameCommand { get; }
        public ICommand CopyPasswordCommand { get; }

        #endregion

        #region GeneratorTab

        private Generator generator = new Generator();

        private string generatedPassword;
        public List<string> GeneratedPassword
        {
            get
            {
                var split = Regex.Matches(generatedPassword, @"(\d+)|([a-zA-Z]+)|([!@#$%^&*]+)");
                return split.Select(gr => gr.Value).ToList();
            }
        }

        public GeneratorSettings GeneratorSettings { get; }

        public ICommand RegeneratePasswordCommand { get; }
        public ICommand CopyGeneratedPasswordCommand { get; }

        #endregion
        
        public VaultViewModel(Services.NavigationService navigationService, Firebase firebase, string masterPassword)
        {
            _firebase = firebase;
            _navigationService = navigationService;

            GetVaultAsync().Then(() => _cryptography.Initialize(masterPassword, Vault.Salt));

            OpenPasswordItemAddFormCommand = new RelayCommand(OpenPasswordItemAddFormCommandHanlder);
            OpenPasswordItemEditFormCommand = new RelayCommand(OpenPasswordItemEditFormCommandHandler);

            CopyUsernameCommand = new RelayCommand((object? param) => Clipboard.SetText((string)param!));
            CopyPasswordCommand = new RelayCommand((object? param) => Clipboard.SetText((_cryptography.Decrypt((string)param!))));

            GeneratorSettings = new GeneratorSettings();
            GeneratorSettings.OnSettingChanged += OnGeneratorSettingsChanged;

            generatedPassword = generator.GeneratePassword(GeneratorSettings);

            RegeneratePasswordCommand = new RelayCommand((object? param) => { });
            CopyGeneratedPasswordCommand = new RelayCommand((object? param) => { });
        }

        private void OpenPasswordItemAddFormCommandHanlder(object? param)
        {
            _navigationService.NavigateTo<PasswordFormViewModel>(_firebase, _cryptography, FormType.Add, Vault.PasswordItems);
        }

        private void OpenPasswordItemEditFormCommandHandler(object? param)
        {
            _navigationService.NavigateTo<PasswordFormViewModel>(_firebase, _cryptography, FormType.Edit, (PasswordItem)param!, Vault.PasswordItems);
        }

        private void OnGeneratorSettingsChanged()
        {
            generatedPassword = generator.GeneratePassword(GeneratorSettings);
            OnPropertyChanged(nameof(GeneratedPassword));
        }

        private async Task GetVaultAsync()
        {
            Vault = await _firebase.GetVault();
            OnPropertyChanged(nameof(Vault));
        }
    }
}
