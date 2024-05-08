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
        private Stack<Core.ViewModel> _viewModelStack;
        public Core.ViewModel? CurrentViewModel
        {
            get
            {
                if(_viewModelStack.Count == 0)
                {
                    return null;
                }

                return _viewModelStack.Peek();
            }
        }

        public event Action OnNavigate;

        public NavigationService()
        {
            _viewModelStack = new Stack<Core.ViewModel>();
        }

        public void SetDefaultViewModel(Core.ViewModel viewModel)
        {
            _viewModelStack.Push(viewModel);
        }

        public void NavigateBack()
        {
            if(_viewModelStack.Count > 0)
            {
                _viewModelStack.Pop();
                OnNavigate?.Invoke();
            }
        }

        public void InvalidateNavigateBack()
        {
            Core.ViewModel current = _viewModelStack.Pop();
            _viewModelStack.Clear();
            _viewModelStack.Push(current);  
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

            _viewModelStack.Push((Core.ViewModel)Activator.CreateInstance(typeof(T), parameters)!);
            OnNavigate.Invoke();
        }
    }
}
