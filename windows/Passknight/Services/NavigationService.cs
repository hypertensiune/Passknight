using Passknight.Core;
using Passknight.ViewModels;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Services
{
    public class NavigationService
    {
        private Stores.NavigationStore _navigationStore;

        public Core.ViewModel CurrentViewModel => _navigationStore.CurrentViewModel;
        public Core.ViewModel PreviousViewModel => _navigationStore.PreviousViewModel;

        public NavigationService()
        {
            _navigationStore = new Stores.NavigationStore();
        }

        public void SetDefaultViewModel(Core.ViewModel viewModel)
        {
            _navigationStore.CurrentViewModel = viewModel;
        }

        public void NavigateBack()
        {
            if(_navigationStore.CurrentViewModel != _navigationStore.PreviousViewModel) 
            {
                _navigationStore.CurrentViewModel = _navigationStore.PreviousViewModel;
                OnNavigate.Invoke();
            }
        }

        public void NavigateTo<T>(params object[] paramsArray) where T : Core.ViewModel
        {
            object[] parameters = new object[paramsArray.Length + 1];
            if(paramsArray.Length > 0 )
            {
                paramsArray.CopyTo(parameters, 1);
            }
            parameters[0] = this;

            _navigationStore.PreviousViewModel = _navigationStore.CurrentViewModel;
            _navigationStore.CurrentViewModel = (Core.ViewModel)Activator.CreateInstance(typeof(T), parameters)!;
            OnNavigate.Invoke();
        }

        public event Action OnNavigate;
    }
}
