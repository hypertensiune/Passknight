using Passknight.Core;
using Passknight.Models;
using Passknight.Services;
using Passknight.Services.Firebase;
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
        private Firebase firebase;

        public Vault Vault { get; private set; }
        public List<string> GeneratorHistory { get; }

        public ICommand OpenPasswordItemForm { get; }
        public ICommand BackCommand { get; }

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
        
        public VaultViewModel(Services.NavigationService navigationService, Firebase firebase)
        {
            this.firebase = firebase;

            OpenPasswordItemForm = new RelayCommand((object? obj) => navigationService.NavigateTo<PasswordFormViewModel>(firebase));

            CopyUsernameCommand = new RelayCommand((object? param) => Clipboard.SetText(param as string));
            CopyPasswordCommand = new RelayCommand((object? param) => Clipboard.SetText(param as string));

            GeneratorSettings = new GeneratorSettings();
            GeneratorSettings.OnSettingChanged += OnGeneratorSettingChanged;

            generatedPassword = generator.GeneratePassword(GeneratorSettings);

            RegeneratePasswordCommand = new RelayCommand((object? param) => { });
            CopyGeneratedPasswordCommand = new RelayCommand((object? param) => { });

            GetVault();
        }

        private void OnGeneratorSettingChanged()
        {
            generatedPassword = generator.GeneratePassword(GeneratorSettings);
            OnPropertyChanged(nameof(GeneratedPassword));
        }

        private async void GetVault()
        {
            Vault = await firebase.GetVault();
            OnPropertyChanged(nameof(Vault));
        }
    }
}
