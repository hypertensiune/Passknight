using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using Passknight.Models;
using Passknight.Models.Items;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Services.Firebase
{
    /// <summary>
    /// Class used to convert the JSON response received from firebase to C# data and back to the JSON format expected by firebase REST Api. <br/>
    /// <a href="https://firebase.google.com/docs/firestore/reference/rest/v1beta1/projects.databases.documents"/>
    /// </summary>
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
                    vaults.Add(prop.Name);
                }
            }

            return vaults;
        }

        public static VaultContent CreateVaultContent(string jsonPasswords, string jsonNotes, string jsonHistory, string jsonPsk)
        {
            var passwords = JObject.Parse(jsonPasswords).SelectToken("fields");

            List<PasswordItem> PasswordList = new List<PasswordItem>();
            if (passwords != null)
            {
                foreach (JProperty item in passwords)
                {
                    var fields = item.SelectToken("$..mapValue.fields")!;
                    PasswordList.Add(new PasswordItem()
                    {
                        Name = item.Name,
                        Password = (string)fields.SelectToken("password.stringValue")!,
                        Username = (string)fields.SelectToken("username.stringValue")!,
                        Website = (string)fields.SelectToken("website.stringValue")!,
                        Created = (string)fields.SelectToken("created.stringValue")!,
                        Updated = (string)fields.SelectToken("updated.stringValue")!
                    });
                }
            }

            var notes = JObject.Parse(jsonNotes).SelectToken("fields");

            List<NoteItem> NoteList = new List<NoteItem>();
            if (notes != null)
            {
                foreach (JProperty item in notes)
                {
                    var fields = item.SelectToken("$..mapValue.fields")!;
                    NoteList.Add(new NoteItem()
                    {
                        Name = item.Name,
                        Content = (string)fields.SelectToken("content.stringValue")!,
                        Created = (string)fields.SelectToken("created.stringValue")!,
                        Updated = (string)fields.SelectToken("updated.stringValue")!
                    }); ;
                }
            }

            var history = JObject.Parse(jsonHistory).SelectToken("fields.history.arrayValue.values");

            List<string> GeneratorHistory = new List<string>();
            if (history != null)
            {
                foreach (var item in history)
                {
                    GeneratorHistory.Add((string)item.SelectToken("stringValue")!);
                }
            }

            string Psk = (string)JObject.Parse(jsonPsk).SelectToken("fields.psk.stringValue")!;

            return new VaultContent(PasswordList, NoteList, GeneratorHistory, Psk);
        }

        /// <summary>
        /// Encode the list of <see cref=PasswordItem"/> in JSON representation expected by firebase update doc API.
        /// </summary>
        public static string EncodePasswordItem(PasswordItem item)
        {
            string res = $$"""
            {
                "fields": {
                    "{{item.Name}}": {
                        "mapValue": {
                            "fields": {
                                "website": {
                                    "stringValue": "{{item.Website}}"
                                },
                                "username": {
                                    "stringValue": "{{item.Username}}"
                                },
                                "password": {
                                    "stringValue": "{{item.Password}}"
                                },
                                "created": {
                                    "stringValue": "{{item.Created}}"
                                },
                                "updated": {
                                    "stringValue": "{{item.Updated}}"
                                }
                            }
                        }
                    }
                }
            }
            """;

            return res;
        }

        /// <summary>
        /// Encode the list of <see cref="NoteItem"/> in JSON representation expected by firebase update doc API.
        /// </summary>
        public static string EncodeNoteItem(NoteItem item)
        {
            string res = $$"""
            {
                "fields": {
                    "{{item.Name}}": {
                        "mapValue": {
                            "fields": {
                                "content": {
                                    "stringValue": "{{item.Content}}"
                                },
                                "created": {
                                    "stringValue": "{{item.Created}}"
                                },
                                "updated": {
                                    "stringValue": "{{item.Updated}}"
                                }
                            }
                        }
                    }
                }
            }
            """;

            return res;
        }

        public static string EncodeHistory(List<string> history)
        {
            var arr = history.Select(h => $"{{\"stringValue\": \"{h}\"}}").ToList();
            var values = String.Join(", ", arr);

            string res = $$"""
            {
                "fields": {
                    "history": {
                        "arrayValue": {
                            "values": [
                                {{values}}    
                            ]
                        }
                    }
                }
            }
            """;

            return res;
        }
    }
}
