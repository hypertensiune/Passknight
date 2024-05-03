﻿using Passknight.Models.Items;
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
        public NoteFormViewModel(NavigationService navigationService, Firebase firebase, Cryptography cryptography, FormType type, List<NoteItem> noteItems) : base(navigationService, firebase, cryptography, type, noteItems)
        {
            Item = new NoteItem();
        }
    }
}
