using Passknight.Core;
using Passknight.Models;
using Passknight.Models.Items;
using Passknight.Services;
using Passknight.Services.Cryptography;
using Passknight.Services.Firebase;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
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

    internal class ItemFormViewModel<T> : ViewModel where T : Item, ICryptable
    {
        public T Item { get; init; }

        protected T? _originalItem = default;
        
        private readonly List<T> _items;
        
        private readonly IDatabase _database;
        private readonly NavigationService _navigationService;
        private readonly Cryptography _cryptography;

        private readonly FormType _formType;
        
        public ICommand BackCommand { get; }
        public ICommand SubmitCommand { get; }
        public ICommand DeleteCommand { get; }
        
        protected ItemFormViewModel(Services.NavigationService navigationService, IDatabase database, Cryptography cryptography, FormType type, List<T> items)
        {
            _navigationService = navigationService;
            _database = database;
            _cryptography = cryptography;
            _formType = type;
            _items = items;
            
            BackCommand = new RelayCommand((object? obj) => navigationService.NavigateBack());
            SubmitCommand = new RelayCommand(SubmitCommandHandler);
            DeleteCommand = new RelayCommand(DeleteCommandHandler);
        }
        
        private async void SubmitCommandHandler(object? obj)
        { 
            if(_formType == FormType.Add)
            {
                Item.Encrypt(_cryptography.Encrypt);
                _items.Add(Item);

                Item.Created = DateTimeOffset.Now.ToUnixTimeMilliseconds().ToString();
                Item.Updated = DateTimeOffset.Now.ToUnixTimeMilliseconds().ToString();

                var res = await _database.AddItemInVault(Item);
                if (!res)
                {
                    Msgbox.Show("Error", "Item couldn't be added");
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

                Item.Updated = DateTimeOffset.Now.ToUnixTimeMilliseconds().ToString();

                Item.Encrypt(_cryptography.Encrypt);
                _items[index] = Item;

                var res = await _database.EditItemInVault(_originalItem!, Item);
                if (!res)
                {
                    Msgbox.Show("Error", "Item couldn't be edited");
                    _items[index] = _originalItem!;

                    return;
                }
            }

            _navigationService.NavigateBack();
        }

        private void DeleteCommandHandler(object? param)
        {
            _navigationService.NavigateTo<DeleteConfirmViewModel>(Delete);
        }

        private async void Delete()
        {
            _items.Remove(_originalItem!);
            var res = await _database.DeleteItemFromVault<T>(_originalItem!);
            if (!res)
            {
                Msgbox.Show("Error", "Item couldn't be deleted");
                _items.Add(_originalItem!);

                return;
            }

            _navigationService.NavigateBack();
            _navigationService.NavigateBack();
        }
    }
}
