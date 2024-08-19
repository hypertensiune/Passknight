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

            string Psk = (string)json.SelectToken("fields.psk.stringValue")!;

            return new VaultContent(PasswordList, NoteList, GeneratorHistory, Psk);
        }

        /// <summary>
        /// Encode the list of <see cref="PasswordItem"/> in JSON representation expected by firebase update doc API.
        /// </summary>
        public static string PasswordItems(List<PasswordItem> items)
        {
            string res = """
                {
                    "fields": {
                        "passwords": {
                            "arrayValue": {
                                "values": [
                """;
            
            foreach (var item in items)
            {
                res += $$"""
                    {
                        "mapValue": {
                            "fields": {
                                "name": {
                                    "stringValue": "{{item.Name}}"
                                },
                                "website": {
                                    "stringValue": "{{item.Website}}"
                                },
                                "username": {
                                    "stringValue": "{{item.Username}}"
                                },
                                "password": {
                                    "stringValue": "{{item.Password}}"
                                }
                            }
                        }
                    },
                    """;
            }

            res += """
                                ]
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
        public static string NoteItems(List<NoteItem> items)
        {
            string res = """
                {
                    "fields": {
                        "notes": {
                            "arrayValue": {
                                "values": [
                """;

            foreach (var item in items)
            {
                res += $$"""
                    {
                        "mapValue": {
                            "fields": {
                                "name": {
                                    "stringValue": "{{item.Name}}"
                                },
                                "content": {
                                    "stringValue": "{{item.Content}}"
                                }
                            }
                        }
                    },
                    """;
            }

            res += """
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
