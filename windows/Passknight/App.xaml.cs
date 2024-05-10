using System.IO;
using System.Windows;
using Passknight.ViewModels;
using Passknight.Services.Firebase;
using Passknight.Services;

namespace Passknight
{
    /// <summary>
    /// Interaction logic for App.xaml
    /// </summary>
    public partial class App : Application
    {
        private Services.NavigationService navigationService;

        public App()
        {
            navigationService = new Services.NavigationService();
            navigationService.SetDefaultViewModel(new VaultListViewModel(navigationService));
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
