# PC - Winter 21/22 - Exercise Series

## Observations

### Series 1
For all the synchronizers, unit tests were not performed to cover the following situation, among others: - a thread was blocked and was released because its task was completed, at the moment when it is waiting to acquire the mutual exclusion, there is an interruption of this thread.

This specific situation was not tested since this test is complicated to perform, and it is possible to verify, by looking at the code, if this reported event is properly controlled.

Regarding the tests, a very simple synchronizer called `CustomSync` was created to assist in the tests, ensuring, for example, that some threads were executed before others.

### Series 2
Regarding the last exercise, MessageQueue, it was not tested whether the insertion of messages in the Queue is really being done according to the FIFO criterion since this test is very complicated to perform naturally. Therefore, it is only confirmed by looking at the code implemented for the enqueue method. In this method, each new message is placed at the end of the queue as long as the tail is not "behind," adhering to the proposed criterion.

## Technical Documentation

### Series 1
For all synchronizers, the technique used corresponds to the `execution delegation/kernel style`. This technique was chosen because it is more efficient in the sense that when a release operation (e.g., in the case of a semaphore) can complete the task of delivering units to a thread waiting for them, it does so. This eliminates the need for the thread that was waiting to recomplete the task when it regains mutual exclusion. By using a flag, it is possible to know if the task has already been completed. In other words, **the holder of the mutual exclusion can dispatch work for future context switches if possible**.

### Series 2
Concerning the third exercise, `MessageQueue`, the `waiters` variable, used to know how many threads are waiting for a message in the queue, was declared with the keyword `volatile`. Since it is only modified under mutual exclusion, it does not need to be an `AtomicInteger`, and it also ensures the **Happens-Before** relationship between the threads that call the enqueue method to read `waiters` and those that call the dequeue method to write on it.

## Series 3 - Design aspects

* Each server instance has one thread to listen for new connections and creates a client instance for each. Most of the time, this thread will be blocked waiting for a new connection.

* Each client instance uses two threads:
    - A _main thread_ that reads and processes control messages from a queue. These control messages can be:
        - A text message posted to a room where the client is present.
        - A text line sent by the remote connected client.
        - An indication that the read stream from the remote connected client ended.
        - An indication that the handling of the client should end (e.g., because the server is ending).
    - A _read thread_ that reads lines from the remote client connection and transforms these into control messages sent to the main thread.

* Most interactions with the client are done by sending messages to the client control queue.

* The parsing of remote client lines (commands or messages) is done by the `Line` class.

![diagram](dotnet/imgs/diagram.png)

## Important Design and Implementation Aspects

In order to eliminate the presence of always two threads per client and to reduce the resource consumption associated with thread creation, the approach was changed to use threads from the `ThreadPool`. This change aims to minimize the overhead involved in creating threads. Previously, a separate thread was created for each client, but now there are two asynchronous loops responsible for handling clients:

1. The first loop is responsible for:
   - Retrieving messages from the queue and interpreting them.
   - Delivering a message to the specific `Room` where the client is present.
   - Sending a message to the remote client asynchronously.
   - Indicating when the remote client has finished its task.
   - Indicating to stop following this client and removing it from the queue that holds all connected clients, which is managed in the `Server` class.

2. The second auxiliary loop is managed by the first one and asynchronously waits for messages from the remote client, placing them in the created message queue.

This message queue, named `AsyncMessageQueue`, allows asynchronous waiting for a message in `DequeueAsync`. While there are no messages added to the queue, an incomplete Task will be returned, which will only be completed when a message is received. It also allows for cancellation. Once an active `CancellationToken` is received, the incomplete Task that was waiting for a message will be canceled.

In managing active clients (in the `Server` class) and created rooms (in the `RoomSet` class), a `ConcurrentDictionary` is used to facilitate concurrent access to the data structure. Instead of using a dictionary without concurrency control and implementing manual locking techniques, methods were created to remove objects from the dictionary whenever a client gives up or a room becomes empty.

Regarding the data structure that holds information about clients connected to a specific `RoomSet`, locking techniques have been used to safely perform changes to the `HashSet` (the chosen data structure to store this information about a specific Room) with concurrent access.

A functionality has been added to allow only a maximum number of clients to be simultaneously connected to the server. This was implemented using `SemaphoreSlim`. Additionally, it is possible to limit the number of clients that remain pending, waiting for space on the server when it is full. This was achieved using the `backlog` property provided by the `Start` method of `TcpListener`.

When the desire is to shut down the server, the `Stop` method will be called, and the `CancellationTokenSource` of all active clients will be activated, allowing for the cancellation of any ongoing Tasks, including the token associated with the semaphore. The `TcpListener` will also be stopped.