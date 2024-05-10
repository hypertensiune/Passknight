using Passknight.Models;
using Passknight.Models.Items;
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
    class NoteFormViewModel : ItemFormViewModel<NoteItem>
    {
        public NoteFormViewModel(NavigationService navigationService, IDatabase database, Cryptography cryptography, FormType type, List<NoteItem> noteItems) : base(navigationService, database, cryptography, type, noteItems)
        {
            Item = new NoteItem();
        }

        public NoteFormViewModel(NavigationService navigationService, IDatabase database, Cryptography cryptography, FormType type, NoteItem item, List<NoteItem> noteItems) : base(navigationService, database, cryptography, type, noteItems)
        {
            Item = item.Clone();
            Item.Decrypt(cryptography.Decrypt);
            _originalItem = item;
        }
    }
}
