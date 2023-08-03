using System;
using System.Net;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;

namespace App
{
    public class Program
    {
        private static async Task Main()
        {
            var loggerFactory = Logging.CreateFactory();
            var logger = loggerFactory.CreateLogger<Program>();
            
            logger.LogInformation("Starting program");

            var server = new Server(loggerFactory, 10, 10);
            var port = 8080;
            var localAddr = IPAddress.Parse("127.0.0.1");
            server.Start(localAddr, port);
            Console.CancelKeyPress += (sender, eventArgs) =>
            {
                eventArgs.Cancel = true;
                logger.LogInformation("Stopping the server");
                server.Stop();
            };
            
            await server.JoinAsync();
            logger.LogInformation("Ending main");
        }
    }
}