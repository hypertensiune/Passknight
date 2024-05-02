using Passknight.Models;
using Passknight.Services;
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
        public PasswordFormViewModel(NavigationService navigationService, Firebase firebase) : base(navigationService, firebase)
        {
            Item = new PasswordItem();
        }
    }
}
