using Newtonsoft.Json.Linq;
using Passknight.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Services.Firebase
{
    static class JSONConverter
    {
        public static List<string> VaultNames(string jsonString)
        {
            var json = JObject.Parse(jsonString)["fields"];

            List<string> vaults = new List<string>();
            if (json != null)
            {
                foreach (JProperty prop in json)
                {
                    //string key = prop.Name;
                    //string value = "";
                    //foreach(JProperty prop2 in prop.First)
                    //{
                    //    value = prop2.Value.ToString();
                    //}

                    vaults.Add(prop.Name);
                }
            }

            return vaults;
        }

        public static VaultContent VaultContent(string jsonString)
        {
            var json = JObject.Parse(jsonString);

            List<PasswordItem> PasswordList = new List<PasswordItem>();
            var passwords = json.SelectToken("fields.passwords.arrayValue.values");
            if (passwords != null)
            {
                foreach (var item in passwords)
                {
                    var fields = item.SelectToken("mapValue.fields")!;
                    PasswordList.Add(new PasswordItem()
                    {
                        Name = (string)fields.SelectToken("name.stringValue")!,
                        Password = (string)fields.SelectToken("password.stringValue")!,
                        Username = (string)fields.SelectToken("username.stringValue")!,
                        Website = (string)fields.SelectToken("website.stringValue")!
                    });
                }
            }

            List<NoteItem> NoteList = new List<NoteItem>();
            var notes = json.SelectToken("fields.notes.arrayValue.values");
            if (notes != null)
            {
                foreach (var item in notes)
                {
                    var fields = item.SelectToken("mapValue.fields")!;
                    NoteList.Add(new NoteItem()
                    {
                        Name = (string)fields.SelectToken("name.stringValue")!,
                        Content = (string)fields.SelectToken("content.stringValue")!,
                    });
                }
            }

            List<string> GeneratorHistory = new List<string>();
            var history = json.SelectToken("fields.history.arrayValue.values");
            if (history != null)
            {
                foreach (var item in history)
                {
                    GeneratorHistory.Add((string)item["stringValue"]!);
                }
            }

            string Salt = (string)json.SelectToken("fields.salt.stringValue")!;

            return new VaultContent(PasswordList, NoteList, GeneratorHistory, Salt);
        }
    }
}
