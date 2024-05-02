using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Stores
{
    class FirebaseStore
    {
        private string _currentUnlockedVaultID;
        private string _currentUnlockedVaultName;
        private string _currentUnlockedVaultSalt;

        public string CurrentUnlockedVaultID
        {
            get => _currentUnlockedVaultID;
            set => _currentUnlockedVaultID = value;
        }

        public string CurrentUnlockedVaultName
        {
            get => _currentUnlockedVaultName;
            set => _currentUnlockedVaultName = value;
        }

        public string CurrentUnlockedVaultSalt
        {
            get => _currentUnlockedVaultSalt;
            set => _currentUnlockedVaultSalt = value;
        }
    }
}
