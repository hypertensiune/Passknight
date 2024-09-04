using Passknight.Core;
using Passknight.Models;
using Passknight.Models.Items;
using Passknight.Services;
using Passknight.Services.Cryptography;
using Passknight.Services.Firebase;
using Passknight.Services.Generator;
using Passknight.ViewModels.FormViewModels;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace Passknight.ViewModels
{
    class PasswordFormViewModel : ItemFormViewModel<PasswordItem>
    {
        public ICommand FillGeneratedPasswordCommand { get; }

        public PasswordFormViewModel(NavigationService navigationService, IDatabase database, Cryptography cryptography, List<PasswordItem> passwordItems, Func<string> generate) : base(navigationService, database, cryptography, FormType.Add, passwordItems)
        {
            Item = new PasswordItem();

            FillGeneratedPasswordCommand = new RelayCommand((object? obj) =>
            {
                Item.Password = generate();
                OnPropertyChanged(nameof(Item));
            });
        }

        public PasswordFormViewModel(NavigationService navigationService, IDatabase database, Cryptography cryptography, PasswordItem item, List<PasswordItem> passwordItems, Func<string> generate) : base(navigationService, database, cryptography, FormType.Edit, passwordItems)
        {
            Item = item.Clone();
            Item.Decrypt(cryptography.Decrypt);
            _originalItem = item;

            Name.Input = Item.Name;

            FillGeneratedPasswordCommand = new RelayCommand((object? obj) =>
            {
                Item.Password = generate();
                OnPropertyChanged(nameof(Item));
            });
        }
    }
}
