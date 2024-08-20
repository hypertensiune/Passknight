using Passknight.Models;
using Passknight.Models.Items;
using Passknight.Services;
using Passknight.Services.Cryptography;
using Passknight.Services.Firebase;
using Passknight.ViewModels.FormViewModels;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.ViewModels
{
    class PasswordFormViewModel : ItemFormViewModel<PasswordItem>
    {
        public PasswordFormViewModel(NavigationService navigationService, IDatabase database, Cryptography cryptography, FormType type, List<PasswordItem> passwordItems) : base(navigationService, database, cryptography, type, passwordItems)
        {
            Item = new PasswordItem();
        }

        public PasswordFormViewModel(NavigationService navigationService, IDatabase database, Cryptography cryptography, FormType type, PasswordItem item, List<PasswordItem> passwordItems) : base(navigationService, database, cryptography, type, passwordItems)
        {
            Item = item.Clone();
            Item.Decrypt(cryptography.Decrypt);
            _originalItem = item;
        }
    }
}
