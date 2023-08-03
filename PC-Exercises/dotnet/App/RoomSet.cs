using System.Collections.Concurrent;
using System.Collections.Generic;

namespace App
{
    /*
     * Manages a set of rooms, namely the creation and retrieval.
     */
    public class RoomSet
    {
        private readonly IDictionary<string, Room> _rooms = new ConcurrentDictionary<string, Room>();
        
        /**
         * Tries to get the room with the desired name, if it can't tries to add the room, if it returns false
         * means that a room with that name is already created, then TryGetValue is called again and so on.
         */
        public Room GetOrCreateRoom(string name)
        {
            while (true)
            {
                if (_rooms.TryGetValue(name, out var room)) return room;
                room = new Room(name);
                if (_rooms.TryAdd(name, room)) return room;
            }
        }

        public bool RemoveRoomIfEmpty(string name)
        {
            if (!_rooms.TryGetValue(name, out var room)) return false;
            return room.CheckEmptyRoom() && _rooms.Remove(name);
        }
    }
}