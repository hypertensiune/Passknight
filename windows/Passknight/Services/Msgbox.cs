using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

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
    }
}
