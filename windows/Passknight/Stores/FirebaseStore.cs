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

        public void Clear()
        {
            _currentUnlockedVaultID = string.Empty;
            _currentUnlockedVaultName = string.Empty;
        }
    }
}
