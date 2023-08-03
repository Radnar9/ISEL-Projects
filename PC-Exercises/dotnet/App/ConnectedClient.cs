using System;
using System.IO;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;

namespace App
{
    /*
     * Represents a connected client.
     * Uses two async loops:
     *     - A main loop task that processes control messages sent to this client, which can be
     *         - A message delivered to the room where the client is.
     *         - A line sent by the remote client.
     *         - An indication the remote client stream has ended.
     *         - An indication to stop handling this client.
     *       This loop should only block for new control messages.
     *     - An auxiliary loop task asynchronously waiting for input from the remote client.
     * 
     * Some of its methods may be used by multiple threads.
     */
    
    public class ConnectedClient
    {
        private readonly ILogger _logger;

        private readonly TcpClient _tcpClient;
        private readonly RoomSet _rooms;
        private readonly StreamReader _reader;
        private readonly StreamWriter _writer;
        private readonly Task _mainTask;
        private readonly Server _server;

        private readonly AsyncMessageQueue<ControlMessage> _controlMessageQueue = new ();

        private Room? _currentRoom;
        private bool _exiting;

        public string Name { get; }

        public ConnectedClient(string name, TcpClient tcpClient, RoomSet rooms, ILoggerFactory loggerFactory, 
            Server server, CancellationToken ct)
        {
            _logger = loggerFactory.CreateLogger<ConnectedClient>();
            Name = name;
            _tcpClient = tcpClient;
            _rooms = rooms;
            _server = server;
            tcpClient.NoDelay = true;
            var networkStream = tcpClient.GetStream();
            _reader = new StreamReader(networkStream, Encoding.UTF8);
            _writer = new StreamWriter(networkStream, Encoding.UTF8)
            {
                AutoFlush = true
            };
            _mainTask = MainLoopAsync(ct);
        }

        // Sends a message to the client
        public void PostRoomMessage(string message, Room sender)
        {
            _controlMessageQueue.Add(new ControlMessage.RoomMessage(message, sender));
        }

        // Instructs the client to exit
        public void Exit()
        {
            _controlMessageQueue.Add(new ControlMessage.Stop());
        }

        // Synchronizes with the client termination
        public async Task Join()
        {
            await _mainTask;
        }

        private async Task MainLoopAsync(CancellationToken ct)
        {
            _logger.LogInformation("Main loop from {} on thread {}", 
                Thread.CurrentThread.IsThreadPoolThread ? "pool" : "non-pool",
                Thread.CurrentThread.ManagedThreadId);
            
            Task? readTask = null;
            try
            {
                readTask = RemoteReadLoopAsync();
                
                _logger.LogInformation("Main loop after await from {} on thread {}", 
                    Thread.CurrentThread.IsThreadPoolThread ? "pool" : "non-pool",
                    Thread.CurrentThread.ManagedThreadId);
                while (!_exiting)
                {
                    try
                    {
                        _logger.LogInformation("Before take from {} on thread {}",
                            Thread.CurrentThread.IsThreadPoolThread ? "pool" : "non-pool",
                            Thread.CurrentThread.ManagedThreadId);

                        var controlMessage = await _controlMessageQueue.DequeueAsync(ct);

                        _logger.LogInformation("Dequeue after await from {} on thread {}",
                            Thread.CurrentThread.IsThreadPoolThread ? "pool" : "non-pool",
                            Thread.CurrentThread.ManagedThreadId);

                        switch (controlMessage)
                        {
                            case ControlMessage.RoomMessage roomMessage:
                                // Condition to fix race between leaving a room and receiving messages from that room.
                                if (_currentRoom?.Name == roomMessage.Sender.Name)
                                {
                                    await WriteToRemote(roomMessage.Value);
                                }
                                break;
                            case ControlMessage.RemoteLine remoteLine:
                                await ExecuteCommand(remoteLine.Value);
                                break;
                            case ControlMessage.RemoteInputEnded:
                                await ClientExit();
                                break;
                            case ControlMessage.Stop:
                                await ServerExit();
                                break;
                            default:
                                _logger.LogWarning("Unknown message {}, ignoring it", controlMessage);
                                break;
                        }
                    }
                    catch (TaskCanceledException e)
                    {
                        _logger.LogError("Task canceled exception with message: '{}', ending connection", e.Message);
                        _exiting = true;
                    }
                    catch (Exception e)
                    {
                        _logger.LogError("Unexpected exception with handling message: '{}', ending connection", e.Message);
                        _exiting = true;
                    }
                }
            }
            finally
            {
                _currentRoom?.Leave(this);
                _tcpClient.Close();
                await readTask!;
                _logger.LogInformation("Exiting MainLoop");
            }
        }

        private async Task RemoteReadLoopAsync()
        {
            _logger.LogInformation("Remote loop from {} on thread {}", 
                Thread.CurrentThread.IsThreadPoolThread ? "pool" : "non-pool", 
                Thread.CurrentThread.ManagedThreadId);
            try
            {
                while (!_exiting)
                {
                    var line = await _reader.ReadLineAsync();
                    if (line == null)
                    {
                        break;
                    }

                    _controlMessageQueue.Add(new ControlMessage.RemoteLine(line));
                }
            }
            catch (Exception e)
            {
                // Unexpected exception, log and exit
                if (!_exiting)
                {
                    _logger.LogError("Exception while waiting for connection read: {}", e.Message);
                }
            }
            finally
            {
                if (!_exiting)
                {
                    _controlMessageQueue.Add(new ControlMessage.RemoteInputEnded());
                }
            }
            _logger.LogInformation("Exiting ReadLoop");
        }


        private Task WriteToRemote(string line)
        {
            return _writer.WriteLineAsync(line);
        }

        private async Task WriteErrorToRemote(string line) => await WriteToRemote($"[Error: {line}]");
        private Task WriteOkToRemote() => WriteToRemote("[OK]");

        private async Task ExecuteCommand(string lineText)
        {
            Line line = Line.Parse(lineText);

            switch (line)
            {
                case Line.InvalidLine invalidLine:
                    await WriteErrorToRemote(invalidLine.Reason);
                    break;
                case Line.Message message:
                    await PostMessageToRoom(message);
                    break;
                case Line.EnterRoomCommand enterRoomCommand:
                    await EnterRoom(enterRoomCommand);
                    break;
                case Line.LeaveRoomCommand:
                    await LeaveRoom();
                    break;
                case Line.ExitCommand:
                    await ClientExit();
                    break;
                default:
                    await WriteErrorToRemote("unable to process line");
                    break;
            }
        }

        private async Task PostMessageToRoom(Line.Message message)
        {
            if (_currentRoom == null)
            {
                await WriteErrorToRemote("Need to be inside a room to post a message");
            }
            else
            {
                _currentRoom.Post(this, message.Value);
            }
        }

        private async Task EnterRoom(Line.EnterRoomCommand enterRoomCommand)
        {
            _currentRoom?.Leave(this);

            _currentRoom = _rooms.GetOrCreateRoom(enterRoomCommand.Name);
            _currentRoom.Enter(this);
            await WriteOkToRemote();
        }

        private async Task LeaveRoom()
        {
            if (_currentRoom == null)
            {
                await WriteErrorToRemote("There is no room to leave from");
            }
            else
            {
                _currentRoom.Leave(this);
                _rooms.RemoveRoomIfEmpty(_currentRoom.Name);
                _currentRoom = null;
                await WriteOkToRemote();
            }
        }

        private async Task ClientExit()
        {
            _currentRoom?.Leave(this);
            _exiting = true;
            await WriteOkToRemote();
            _server.Remove(this);
        }

        private async Task ServerExit()
        {
            _currentRoom?.Leave(this);
            _exiting = true;
            await WriteErrorToRemote("Server is exiting");
            _server.Remove(this);
        }

        private abstract class ControlMessage
        {
            private ControlMessage()
            {
                // to make the hierarchy closed
            }

            // A message sent by to a room
            public class RoomMessage : ControlMessage
            {
                public Room Sender { get; }
                public string Value { get; }

                public RoomMessage(string value, Room sender)
                {
                    Value = value;
                    Sender = sender;
                }
            }

            // A line sent by the remote client.
            public class RemoteLine : ControlMessage
            {
                public string Value { get; }

                public RemoteLine(string value)
                {
                    Value = value;
                }
            }

            // The information that the remote client stream has ended, probably because the 
            // socket was closed.
            public class RemoteInputEnded : ControlMessage
            {
            }

            // An instruction to stop handling this remote client
            public class Stop : ControlMessage
            {
            }
        }
    }
}