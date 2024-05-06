using Passknight.Models;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Controls;
using System.Net.Http.Json;

namespace Passknight.Services.Firebase
{
    /// <summary>
    /// Class used to handle all firebase firestore processes.
    /// Wrapper around firestore REST Api. <br/>
    /// <a href="https://firebase.google.com/docs/firestore/reference/rest/v1beta1/projects.databases.documents"/>
    /// </summary>
    class Firestore
    { 
        private readonly HttpClient httpClient = new HttpClient();

        private const string BASE_URI = "https://firestore.googleapis.com/v1/projects/passknight-cd291/databases/(default)/documents/vaults";

        public async Task<string> GetDoc(string doc, string? ID_TOKEN = null)
        {
            var request = new HttpRequestMessage(HttpMethod.Get, $"{BASE_URI}/{doc}");
            if(ID_TOKEN is not null)
            {
                request.Headers.Add("Authorization", "Bearer " + ID_TOKEN);
            }

            var response = await httpClient.SendAsync(request);
            string body = await response.Content.ReadAsStringAsync();

            return body;
        }

        public async Task<string> SetDoc(string doc, string data, string ID_TOKEN)
        {
            var request = new HttpRequestMessage(HttpMethod.Post, $"{BASE_URI}?documentId={doc}");
            request.Headers.Add("Authorization", "Bearer " + ID_TOKEN);
            request.Content = new StringContent(data);

            var response = await httpClient.SendAsync(request);
            string body = await response.Content.ReadAsStringAsync();

            return body;
        }

        public async Task<string> UpdateDoc(string doc, string field, string data, string ID_TOKEN)
        {
            var request = new HttpRequestMessage(HttpMethod.Patch, $"{BASE_URI}/{doc}?updateMask.fieldPaths={field}");
            request.Headers.Add("Authorization", "Bearer " + ID_TOKEN);
            request.Content = new StringContent(data);

            var response = await httpClient.SendAsync(request);
            string body = await response.Content.ReadAsStringAsync();

            return body;
        }

        public async Task<string> DeleteDoc(string doc, string ID_TOKEN)
        {
            var request = new HttpRequestMessage(HttpMethod.Delete, $"{BASE_URI}/{doc}");
            request.Headers.Add("Authorization", "Bearer " + ID_TOKEN);

            var response = await httpClient.SendAsync(request);
            string body = await response.Content.ReadAsStringAsync();

            return body;
        }
    }
}
