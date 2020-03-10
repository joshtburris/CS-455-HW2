# CS-455-HW2
**Title**: Scalable Server Design: Using Thread Pools &amp; Micro Batching to Manage and Load Balance Active Network Connections

**Author**: Joshua Burris (all code was written from scratch by me)

**Assignment page**: [Local PDF](CS455-Spring20-HW2-PC.pdf)

## Building

<code>**CS-455-HW2$** ./build.sh</code>

OR

<code>**CS-455-HW2$** gradle clean</code>

<code>**CS-455-HW2$** gradle tasks</code>

<code>**CS-455-HW2$** gradle assemble</code>

<code>**CS-455-HW2$** gradle build</code>

## Executing

### Server:

<code>**CS-455-HW2$** ./server.sh <port-number\> <thread-pool-size\> <batch-size\> <batch-time\></code>

OR

<code>**CS-455-HW2$** java -cp ./build/libs/CS-455-HW2.jar cs455.scaling.server.Server <port-number\> <thread-pool-size\> <batch-size\> <batch-time\></code>

OR

<code>**CS-455-HW2/out/production/classes$** java cs455.scaling.server.Server <port-number\> <thread-pool-size\> <batch-size\> <batch-time\></code>

### Client:

<code>**CS-455-HW2$** ./client.sh <server-host\> <server-port\> <message-rate\></code>

OR

<code>**CS-455-HW2$** java -cp ./build/libs/CS-455-HW2.jar cs455.scaling.client.Client <server-host\> <server-port\> <message-rate\></code>

OR

<code>**CS-455-HW2/out/production/classes$** java cs455.scaling.client.Client <server-host\> <server-port\> <message-rate\></code>

## Example

### Terminal 1:

<code>**CS-455-HW2$** ./server.sh 1024 16 16 100</code>

### Terminal 2:

<code>**CS-455-HW2$** ./start-clients.sh</code>

Both terminals must be on the same machine to work because the start-clients script uses the current hostname as the hostname for the clients to connect to.

## Helpful Scripts

<code>**CS-455-HW2$** ./start-clients.sh</code>

This script uses the file "conf/machine_list" as a way of spawning a large number of clients very quickly. It spawns one client per machine in the list and opens them all in a window of terminals. Currently there are 135 machines in the list, but often times there is one or more machines that fail. They all try to connect to the server simultaneously. This will however fail if the server didn't receive the port number 1024, which can be fixed in the script or possibly by restarting the server. This rarely happens, but the server will inform you of the port number it was assigned so that you can change the script accordingly.

