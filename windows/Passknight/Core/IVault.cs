using Passknight.Models.Items;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Core
{
    interface IVault
    {
        public void AddItem<T>(T item)
        {
            if (typeof(T) == typeof(PasswordItem))
            {
                AddPasswordItem(item as PasswordItem);
            }
            else if (typeof(T) == typeof(NoteItem))
            {
                AddNoteItem(item as NoteItem);
            }
        }

        public void RemoveItem<T>(T item)
        {
            if (typeof(T) == typeof(PasswordItem))
            {
                RemovePasswordItem(item as PasswordItem);
            }
            else if (typeof(T) == typeof(NoteItem))
            {
                RemoveNoteItem(item as NoteItem);
            }
        }

        // public void EditItem<T>(T old, T new)
  
        
        abstract void AddPasswordItem(PasswordItem? item);
        abstract void AddNoteItem(NoteItem? item);

        abstract void RemovePasswordItem(PasswordItem? item);
        abstract void RemovePasswordItem(int index);
        abstract void RemoveNoteItem(NoteItem? item);
        abstract void RemoveNoteItem(int index);

        abstract void Unlock();
        abstract void Lock();

        // ??????
        abstract void GetContent();
    }
}
