using Passknight.Core;
using Passknight.Services.Firebase;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace Passknight.ViewModels.FormViewModels
{
    class ItemFormViewModel<T> : ViewModel
    {
        public T Item { get; set; }

        public ICommand BackCommand { get; }
        public ICommand SubmitCommand { get; }

        public ItemFormViewModel(Services.NavigationService navigationService, Firebase firebase)
        {
            BackCommand = new RelayCommand((object? obj) => navigationService.NavigateBack());
            SubmitCommand = new RelayCommand(SubmitCommandHandler);
        }

        private void SubmitCommandHandler(object? obj)
        {
            
        }
    }
}
