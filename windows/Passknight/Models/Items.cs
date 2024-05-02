using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Media.Imaging;

namespace Passknight.Models
{
    public class PasswordItem
    {
        public string Name { get; set; }
        public string Password { get; set; } = String.Empty;
        public string Username { get; set; }
        public string Website { get; set; }
    }

    public class NoteItem
    {
        public string Name { get; set; }
        public string Content { get; set; }
    }
}
