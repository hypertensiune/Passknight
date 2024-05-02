using Passknight.Core;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace Passknight.Models
{
    class ItemForm<T>
    {
        public T Item { get; }

        public ICommand AddCommand { get; set; }

        public ItemForm()
        {
            AddCommand = new RelayCommand(AddCommandHandler);
        }

        private void AddCommandHandler(object? obj)
        {
            //
        }

    }
}
