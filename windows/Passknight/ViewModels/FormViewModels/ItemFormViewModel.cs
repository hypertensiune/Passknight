using Passknight.Core;
using Passknight.Models;
using Passknight.Models.Items;
using Passknight.Services;
using Passknight.Services.Firebase;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;

namespace Passknight.ViewModels.FormViewModels
{
    enum FormType
    {
        Add,
        Edit
    };

    internal class ItemFormViewModel<T> : ViewModel where T : ICryptable
    {
        public T Item { get; init; }

        protected T? _originalItem = default;
        
        private readonly List<T> _items;
        
        private readonly Firebase _firebase;
        private readonly Cryptography _cryptography;

        private readonly FormType _formType;
        
        public ICommand BackCommand { get; }
        public ICommand SubmitCommand { get; }
        
        protected ItemFormViewModel(Services.NavigationService navigationService, Firebase firebase, Cryptography cryptography, FormType type, List<T> items)
        {
            _firebase = firebase;
            _cryptography = cryptography;
            _formType = type;
            _items = items;
            
            BackCommand = new RelayCommand((object? obj) => navigationService.NavigateBack());
            SubmitCommand = new RelayCommand(SubmitCommandHandler);
        }
        
        private async void SubmitCommandHandler(object? obj)
        { 
            if(_formType == FormType.Add)
            {
                Item.Encrypt(_cryptography.Encrypt);
                _items.Add(Item);

                var res = await _firebase.UpdateFieldInVault(_items);
                if (!res)
                {
                    MessageBox.Show("Item couldn't be added", "Error", MessageBoxButton.OK, MessageBoxImage.Error);
                    _items.Remove(Item);

                    return;
                }
            }
            else if(_formType == FormType.Edit)
            {
                var index = _items.IndexOf(_originalItem!);
                if(index < 0)
                {
                    return;
                }

                Item.Encrypt(_cryptography.Encrypt);
                _items[index] = Item;

                var res = await _firebase.UpdateFieldInVault(_items);
                if (!res)
                {
                    MessageBox.Show("Item couldn't be edited", "Error", MessageBoxButton.OK, MessageBoxImage.Error);
                    _items[index] = _originalItem!;

                    return;
                }
            }

            BackCommand.Execute(null);
        }
    }
}
