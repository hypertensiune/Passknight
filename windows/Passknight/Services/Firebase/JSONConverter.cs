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

            string Salt = (string)json.SelectToken("fields.salt.stringValue")!;

            return new VaultContent(PasswordList, NoteList, GeneratorHistory, Salt);
        }

        public static string PasswordItems(List<PasswordItem> items)
        {
            string res = "{\n" +
                         "   \"fields\": {\n" +
                         "       \"passwords\": {\n" +
                         "           \"arrayValue\": {\n" +
                         "              \"values\": [\n";

            foreach (var item in items)
            {
                res += "{\n" +
                       "    \"mapValue\": {" +
                       "        \"fields\": {" +
                       "            \"name\": {" +
                       "                \"stringValue\": \"" + item.Name + "\"" +
                       "            },\n" +
                       "            \"website\": {" +
                       "                \"stringValue\": \"" + item.Website + "\"" +
                       "            },\n" +
                       "            \"username\": {" +
                       "                \"stringValue\": \"" + item.Username + "\"" +
                       "            },\n" +
                       "            \"password\": {" +
                       "                \"stringValue\": \"" + item.Password + "\"" +
                       "            },\n" +
                       "        }\n" +
                       "    }\n" +
                       "},\n";
            }

            res += "                ]\n" +
                   "            }\n" +
                   "        }\n" +
                   "    }\n" +
                   "}\n";
            
            return res;
        }
    }
}
