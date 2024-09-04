using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Data;

namespace Passknight.Converters
{
    class TimestampConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            var timestamp = (string)value;
            var date = DateTimeOffset.FromUnixTimeMilliseconds(System.Convert.ToInt64(timestamp));

            return date.LocalDateTime.ToString("dd-MM-yyyy HH:mm"); ;
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
