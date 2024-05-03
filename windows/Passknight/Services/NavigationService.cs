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

        public Core.ViewModel? CurrentViewModel => _navigationStore.CurrentViewModel;
        public Core.ViewModel? PreviousViewModel => _navigationStore.PreviousViewModel;

        public event Action OnNavigate;

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
            if(_navigationStore.PreviousViewModel != null && _navigationStore.CurrentViewModel != _navigationStore.PreviousViewModel) 
            {
                _navigationStore.CurrentViewModel = _navigationStore.PreviousViewModel;
                OnNavigate.Invoke();
            }
        }

        public void InvalidateNavigateBack()
        {
            _navigationStore.PreviousViewModel = null;
        }

        /// <summary>
        /// Navigate to a different view based on the
        /// <typeparamref name="T"/> view model. <br/>
        /// The first dependency injected is the <see cref="NavigationService"/>  view model. 
        /// </summary>
        /// <param name="paramsArray">The dependencies to inject</param>
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
    }
}
