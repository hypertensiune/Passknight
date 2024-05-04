using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace Passknight.Core
{
    public class RelayCommand : ICommand
    {
        private readonly Predicate<object?>? _canExecute;
        private readonly Action<object?> _execute;

        public RelayCommand(Action<object?> execute, Predicate<object?>? canExecute = null)
        {
            _execute = execute;
            _canExecute = canExecute;
        }

        public event EventHandler? CanExecuteChanged
        {
            add => CommandManager.RequerySuggested += value;
            remove => CommandManager.RequerySuggested -= value;
        }

        public bool CanExecute(object? parameter)
        {
            if(_canExecute != null)
            {
                return _canExecute(parameter);
            }

            return true;
        }

        public void Execute(object? parameter)
        {
            _execute(parameter);
        }
    }
}
