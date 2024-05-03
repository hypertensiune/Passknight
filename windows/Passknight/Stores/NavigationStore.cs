using Passknight.ViewModels;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Stores
{
    public class NavigationStore
    {
        private Core.ViewModel? _currentViewModel;
        public Core.ViewModel? CurrentViewModel
        {
            get => _currentViewModel;
            set => _currentViewModel = value;
        }

        private Core.ViewModel? _previousViewModel;
        public Core.ViewModel? PreviousViewModel
        {
            get => _previousViewModel;
            set => _previousViewModel = value;
        }
    }
}
