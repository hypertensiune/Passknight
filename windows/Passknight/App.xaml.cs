using System.IO;
using System.Windows;
using Passknight.ViewModels;
using Microsoft.Extensions.Configuration;
using Passknight.Services.Firebase;
using Passknight.Services;

namespace Passknight
{
    /// <summary>
    /// Interaction logic for App.xaml
    /// </summary>
    public partial class App : Application
    {
        private readonly IConfiguration configuration;

        private Services.NavigationService navigationService;
        private Firebase firebase;

        public App()
        {
            var cfgbuilder = new ConfigurationBuilder()
                .SetBasePath(Directory.GetCurrentDirectory())
                .AddJsonFile("appsettings.json", optional: false);

            configuration = cfgbuilder.Build();

            firebase = new Firebase(configuration.GetConnectionString("API_KEY")!);

            navigationService = new Services.NavigationService();
            navigationService.SetDefaultViewModel(new VaultListViewModel(navigationService, firebase));
        }

        protected override void OnStartup(StartupEventArgs e)
        {
            MainWindow = new MainWindow()
            {
                DataContext = new MainViewModel(navigationService)
            };

            MainWindow.Show();
            base.OnStartup(e);
        }
    }
}
