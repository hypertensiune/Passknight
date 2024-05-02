using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Media;

namespace Passknight.Converters
{
    class GeneratorPasswordStyleConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            string str = (string)value;

            string key = "symbolPasswordElement";
            if (str.All(char.IsLetter))
            {
                key = "letterPasswordElement";
            }
            else if(str.All(char.IsDigit))
            {
                key = "numberPasswordElement";
            }

            return (Style)Application.Current.FindResource(key);
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
