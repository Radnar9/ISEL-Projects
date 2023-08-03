using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;

namespace App
{
    public class AsyncMessageQueue<T>
    {
        private readonly object _lock = new ();

        private class Message : TaskCompletionSource<T>
        {
            public CancellationTokenRegistration? CancellationTokenRegistration;
            public bool Done;
            
            public Message(): base(TaskCreationOptions.RunContinuationsAsynchronously) {}
        }

        private readonly LinkedList<Message> _messages;
        private readonly Action<object?> _cancellationCallback;

        public AsyncMessageQueue()
        {
            _messages = new LinkedList<Message>();
            _cancellationCallback = node =>
            {
                TryCancel((LinkedListNode<Message>) node!);
            };
        }

        /*
         * -> Check if there is a Task with no message, always the last one
         * -> If there is complete the task with SetResult and remove it from the queue
         * -> If not create a completed Task to be returned when DequeueAsync is called
         */
        public void Add(T msgToAdd)
        {
            Message? message;
            lock (_lock)
            {
                if (_messages.Count == 0 || _messages.Last!.Value.Done)
                {
                    message = new Message();
                    _messages.AddLast(message);
                }
                else
                {
                    message = _messages.Last!.Value;
                    _messages.RemoveLast();
                }
            }
            message.SetResult(msgToAdd);
            message.Done = true;
            DisposeMessageAsync(message);
        }
        
        /*
         * -> Check if there are Tasks completed
         * -> If not create a Task with no message
         * -> If so return the Completed Task
         */
        public Task<T> DequeueAsync(CancellationToken ct)
        {
            lock (_lock)
            {
                Message? message;
                LinkedListNode<Message>? node; 
                if (_messages.Count == 0)
                {
                    message = new Message();
                    node = _messages.AddLast(message);
                }
                else
                {
                    node = _messages.First;
                    message = node?.Value;
                    _messages.RemoveFirst();
                }

                if (ct.CanBeCanceled)
                {
                    message!.CancellationTokenRegistration = ct.Register(_cancellationCallback, node);
                }
                
                return message!.Task;
            }
        }

        private void TryCancel(LinkedListNode<Message> node)
        {
            lock (_lock)
            {
                Message message = node.Value;
                if (message.Done) return;
                _messages.Remove(node);
                message.SetCanceled();
            }
        }

        private static void DisposeMessageAsync(Message message)
        {
            message.CancellationTokenRegistration?.DisposeAsync();
        }
    }
}