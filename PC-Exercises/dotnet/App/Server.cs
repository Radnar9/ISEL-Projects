using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;

namespace App
{
    /*
     * Represents a server, listening and handling connections.
     */
    public class Server
    {
        public enum Status
        {
            NotStarted,
            Starting,
            Started,
            Ending,
            Ended
        }

        private readonly SemaphoreSlim _semaphore;
        private readonly CancellationTokenSource _semaphoreCts;
        private readonly int _maxClients;
        private readonly int _maxPendingConnections;

        private readonly ILoggerFactory _loggerFactory;
        private readonly ILogger _logger;

        private readonly RoomSet _rooms = new ();
        private readonly IDictionary<string, KeyValuePair<ConnectedClient, CancellationTokenSource>> _clients;

        private Task? _acceptTask;
        private Status _status = Status.NotStarted;
        private TcpListener? _listener;
        private int _nextClientId;

        public Server(ILoggerFactory loggerFactory, int maxActiveClients, int maxPendingConnections)
        {
            _maxClients = maxActiveClients;
            _maxPendingConnections = maxPendingConnections;
            _loggerFactory = loggerFactory;
            _logger = loggerFactory.CreateLogger<Server>();
            _semaphore = new SemaphoreSlim(_maxClients, _maxClients);
            _semaphoreCts = new CancellationTokenSource();
            _clients = new ConcurrentDictionary<string, KeyValuePair<ConnectedClient, CancellationTokenSource>>();
        }

        public Status State => _status;

        public void Start(IPAddress address, int port)
        {
            if (_status != Status.NotStarted)
            {
                // Can be started at most one once
                throw new Exception("Server has already started");
            }

            _status = Status.Starting;
            _logger.LogInformation("Starting");
            _listener = new TcpListener(address, port);
            _listener.Start(_maxPendingConnections);

            _acceptTask = AcceptLoopAsync(_listener);
        }

        public void Stop()
        {
            if (_status == Status.NotStarted)
            {
                _logger.LogError("Server has not started");
                throw new Exception("Server has not started");
            }

            if (_listener == null)
            {
                _logger.LogError("Unexpected state: listener is not set");
                throw new Exception("Unexpected state");
            }

            if (_status == Status.Starting)
            {
                _logger.LogError("Server is starting");
                throw new Exception("Server has not started so it can't be canceled yet");
            }

            _logger.LogInformation("Changing server status and stopping the listener");
            _status = Status.Ending;

            foreach (var (_, cts) in _clients.Values)
            {
                cts.Cancel();
            }
            _semaphoreCts.Cancel();
            _listener.Stop();
            _logger.LogInformation("Listener stopped");
        }

        public async Task JoinAsync()
        {
            if (_status == Status.NotStarted)
            {
                _logger.LogError("Server has not started");
                throw new Exception("Server has not started");
            }

            if (_acceptTask == null)
            {
                _logger.LogError("Unexpected state: acceptThread is not set ");
                throw new Exception("Unexpected state");
            }
            
            if (_status == Status.Starting)
            {
                _logger.LogError("Server is starting");
                throw new Exception("Server has not started");
            }

            await _acceptTask;
        }

        private async Task AcceptLoopAsync(TcpListener listener)
        {
            _logger.LogInformation("Accept thread started");
            _status = Status.Started;
            while (_status == Status.Started)
            {
                try
                {
                    _logger.LogInformation("Server has {} active clients and has space for more {} clients",
                        _maxClients - _semaphore.CurrentCount, _semaphore.CurrentCount);

                    await _semaphore.WaitAsync(_semaphoreCts.Token);
                    Debug.Assert((_maxClients - _semaphore.CurrentCount) <= _maxClients);

                    _logger.LogInformation("Waiting for client");
                    var tcpClient = await listener.AcceptTcpClientAsync();
                    var tid = Thread.CurrentThread.ManagedThreadId;

                    var clientName = $"client-{_nextClientId++}";
                    
                    _logger.LogInformation("New client accepted '{}' on thread {} from {}", clientName, tid,
                        Thread.CurrentThread.IsThreadPoolThread ? "pool" : "non-pool");
                    
                    var cts = new CancellationTokenSource();
                    var client = new ConnectedClient(clientName, tcpClient, _rooms, _loggerFactory, this, cts.Token);
                    _clients.TryAdd(clientName, new KeyValuePair<ConnectedClient, CancellationTokenSource>(client, cts));
                }
                catch (Exception e) when (e is ObjectDisposedException || e is OperationCanceledException)
                {
                    _logger.LogWarning(
                        "Expected exception caught '{}', which happen because the listener is closed since the server was stopped",
                        e.Message);
                }
                catch (Exception e)
                {
                    _logger.LogWarning("Unexpected exception caught '{}', proceeding to close the server", e.Message);
                }
            }

            _logger.LogInformation("Waiting for clients to end, before ending accept loop");
            foreach (var (client, _) in _clients.Values)
            {
                client.Exit();
                await client.Join();
            }

            _logger.LogInformation("Accept thread ending");
            _status = Status.Ended;
        }
        
        public void Remove(ConnectedClient client)
        {
            if (!_clients.Remove(client.Name)) return;
            _rooms.RemoveRoomIfEmpty(client.Name);
            _semaphore.Release();
        }
    }
}