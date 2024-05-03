using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace Passknight.Services.Firebase
{
    /// <summary>
    /// Class used to handle all firebase authentification processes.
    /// Wrapper around firebase REST Api. <br/>
    /// <a href="https://firebase.google.com/docs/reference/rest/auth"/>
    /// </summary>
    class Auth
    {
        private HttpClient httpClient;

        private string URL_SIGIN;
        private string URL_SIGNUP;

        public string? ID_TOKEN { get; private set; }

        public Auth(string API_KEY)
        {
            httpClient = new HttpClient();

            URL_SIGIN = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY;
            URL_SIGNUP = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + API_KEY;
        }

        public async Task<(bool, string?)> SignIn(string email, string password)
        {
            var request = new HttpRequestMessage(HttpMethod.Post, URL_SIGIN);

            string content = String.Format("{{\"email\":\"{0}\",\"password\":\"{1}\",\"returnSecureToken\":\"true\"}}", email, password);
            request.Content = new StringContent(content);

            request.Content.Headers.ContentType = new MediaTypeHeaderValue("application/json");

            var response = await httpClient.SendAsync(request);
            //response.EnsureSuccessStatusCode();

            string body = await response.Content.ReadAsStringAsync();

            var json = JObject.Parse(body);

            ID_TOKEN = (string?)json["idToken"];

            var localId = (string?)json["localId"];

            return (response.StatusCode == System.Net.HttpStatusCode.OK, localId);
        }

        public async Task<(bool, string?)> CreateUserWithEmailAndPassword(string email, string password)
        {
            var request = new HttpRequestMessage(HttpMethod.Post, URL_SIGNUP);

            string content = String.Format("{{\"email\":\"{0}\",\"password\":\"{1}\",\"returnSecureToken\":\"true\"}}", email, password);
            request.Content = new StringContent(content);

            request.Content.Headers.ContentType = new MediaTypeHeaderValue("application/json");

            var response = await httpClient.SendAsync(request);
            //response.EnsureSuccessStatusCode();

            string body = await response.Content.ReadAsStringAsync();

            var json = JObject.Parse(body);

            ID_TOKEN = (string?)json["idToken"];

            var UID = (string?)json["localId"];

            return (response.StatusCode == System.Net.HttpStatusCode.OK, UID);
        }

        public void SignOut()
        {
            ID_TOKEN = null;
        }
    }
}
