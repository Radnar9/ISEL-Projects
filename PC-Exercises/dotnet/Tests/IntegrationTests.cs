using System.Net;
using App;
using Microsoft.Extensions.Logging;
using Xunit;
using Xunit.Abstractions;

namespace Tests
{
    // Integration tests use the TCP/IP interface to test the server behaviour.
    public class IntegrationTests
    {
        private const int Port = 8080;
        private static readonly IPAddress LocalAddr = IPAddress.Parse("127.0.0.1");
        private const int Reps = 16;

        [Fact]
        public async void First()
        {
            var server = new Server(_loggerFactory, 10, 10);
            server.Start(LocalAddr, Port);
            var client0 = new TestClient(LocalAddr, Port, _loggerFactory);
            var client1 = new TestClient(LocalAddr, Port, _loggerFactory);
            Assert.Null(client0.EnterRoom("room"));
            Assert.Null(client1.EnterRoom("room"));
            for (var i = 0; i < Reps; ++i)
            {
                client0.WriteLine($"Hello from client0 {i}");
                client1.WriteLine($"Hello from client1 {i}");
                Assert.Equal($"[room]client-1 says 'Hello from client1 {i}'", client0.ReadLine());
                Assert.Equal($"[room]client-0 says 'Hello from client0 {i}'", client1.ReadLine());
            }
            Assert.Null(client0.LeaveRoom());
            Assert.Null(client1.LeaveRoom());
            Assert.Null(client0.Exit());
            server.Stop();
            await server.JoinAsync();
        }

        [Fact]
        public async void EnterLeaveAndEnterSameRoom()
        {
            var server = new Server(_loggerFactory, 10, 10);
            server.Start(LocalAddr, Port);
            var client0 = new TestClient(LocalAddr, Port, _loggerFactory);
            var client1 = new TestClient(LocalAddr, Port, _loggerFactory);
            Assert.Null(client0.EnterRoom("room"));
            Assert.Null(client1.EnterRoom("room"));
            for (var i = 0; i < Reps; ++i)
            {
                client0.WriteLine($"Hello from client0 {i}");
                client1.WriteLine($"Hello from client1 {i}");
                Assert.Equal($"[room]client-1 says 'Hello from client1 {i}'", client0.ReadLine());
                Assert.Equal($"[room]client-0 says 'Hello from client0 {i}'", client1.ReadLine());
            }
            Assert.Null(client1.LeaveRoom());
            client1.WriteLine("Hello from client1");
            Assert.Equal("[Error: Need to be inside a room to post a message]", client1.ReadLine());
            
            Assert.Null(client1.EnterRoom("room"));
            client0.WriteLine("Hello from client0");
            Assert.Equal("[room]client-0 says 'Hello from client0'", client1.ReadLine());
            server.Stop();
            await server.JoinAsync();
        }

        [Fact]
        public async void TestServerStatus()
        {
            var server = new Server(_loggerFactory, 10, 10);
            Assert.Equal(Server.Status.NotStarted, server.State);
            server.Start(LocalAddr, Port);
            Assert.Equal(Server.Status.Started, server.State);
            var client0 = new TestClient(LocalAddr, Port, _loggerFactory);
            var client1 = new TestClient(LocalAddr, Port, _loggerFactory);
            Assert.Null(client0.EnterRoom("room"));
            Assert.Null(client1.EnterRoom("room"));
            
            client0.WriteLine("Hello from client0");
            client1.WriteLine("Hello from client1");
            Assert.Equal("[room]client-1 says 'Hello from client1'", client0.ReadLine());
            Assert.Equal("[room]client-0 says 'Hello from client0'", client1.ReadLine());
            
            server.Stop();
            Assert.Equal(Server.Status.Ending, server.State);
            await server.JoinAsync();
            Assert.Equal(Server.Status.Ended, server.State);
        }

        [Fact]
        public async void LeaveFromNoRoom()
        {
            var server = new Server(_loggerFactory, 10, 10);
            server.Start(LocalAddr, Port);
            var client0 = new TestClient(LocalAddr, Port, _loggerFactory);
            var client1 = new TestClient(LocalAddr, Port, _loggerFactory);
            Assert.Null(client0.EnterRoom("room"));
            Assert.Equal("[Error: There is no room to leave from]", client1.LeaveRoom());
            server.Stop();
            await server.JoinAsync();
        }
        
        public IntegrationTests(ITestOutputHelper output)
        {
            _loggerFactory = Logging.CreateFactory(new XUnitLoggingProvider(output));
        }

        private readonly ILoggerFactory _loggerFactory;
    }
}