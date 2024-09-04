using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Models.Items
{
    abstract class Item
    {
        public abstract string Name { get; set; }
        public abstract string Created { get; set; }
        public abstract string Updated { get; set; }
    }
}
