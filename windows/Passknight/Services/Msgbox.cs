using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using Wpf.Ui.Controls;

namespace Passknight.Services
{
    public static class Msgbox
    {
        public static void Show(string title, string message)
        {
            var box = new Wpf.Ui.Controls.MessageBox();
            box.Title = title;
            box.Content = message;
            box.ShowDialogAsync();
        }

        public static async Task<Wpf.Ui.Controls.MessageBoxResult> ShowYesNo(string title, string message)
        {
            var box = new Wpf.Ui.Controls.MessageBox();
            box.Title = title;
            box.Content = message;
            box.PrimaryButtonText = "Yes";
            box.CloseButtonText = "Cancel";
            return await box.ShowDialogAsync();
        }
    }
}
