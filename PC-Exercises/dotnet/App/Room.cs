using System.Collections.Generic;

namespace App
{
    /*
     * Manages a room, namely the set of contained clients.
     */
    public class Room
    {
        private readonly ISet<ConnectedClient> _clients = new HashSet<ConnectedClient>();
        private bool _closingRoom;
        private readonly object _lock = new ();
        
        public Room(string name)
        {
            Name = name;
        }

        public string Name { get; }

        public bool CheckEmptyRoom()
        {
            lock (_lock)
            {
                if (_clients.Count > 0) return false;
                _closingRoom = true;
                return true;
            }
        }

        public void Enter(ConnectedClient client)
        {
            lock (_lock)
            {
                if (_closingRoom)
                {
                    SendClosingMessage(client);
                }
                _clients.Add(client);    
            }
        }

        public void Leave(ConnectedClient client)
        {
            lock (_lock)
            {
                _clients.Remove(client);
            }
        }

        public void Post(ConnectedClient client, string message)
        {
            lock (_lock)
            {
                var formattedMessage = $"[{Name}]{client.Name} says '{message}'";
                foreach (var receiver in _clients)
                {
                    if (receiver != client)
                    {
                        receiver.PostRoomMessage(formattedMessage, this);
                    }
                }   
            }
        }

        private void SendClosingMessage(ConnectedClient client)
        {
            client.PostRoomMessage("This room was empty and now is closing. Please wait a second or choose another room.", this);
        }
    }
}