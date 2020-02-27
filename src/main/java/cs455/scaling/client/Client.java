package cs455.scaling.client;

import cs455.scaling.server.Server;

import java.io.IOException;
import java.net.*;
import java.nio.channels.*;

/**
 * Unlike the server node, there are multiple Clients (minimum of 100) in the system. A client provides the following
 * functionalities:
 *      A. Connect and maintain an active connection to the server.
 *      B. Regularly send data packets to the server. The payloads for these data packets are 8 KB and the values for
 *      these bytes are randomly generated. The rate at which each connection will generate packets is R per-second;
 *      include a Thread.sleep(1000/R) in the client which ensures that you achieve the targeted production rate.
 *      The typical value of R is between 2-4.
 *      C. The client should track hash codes of the data packets that it has sent to the server. A server will
 *      acknowledge every packet that it has received by sending the computed hash code back to the client.
 */
public class Client {
    
    private final String serverHost;
    private final int serverPort, messageRate;
    
    public Client(String serverHost, int serverPort, int messageRate) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.messageRate = messageRate;
    }
    
    public void start() {
        
        try {
        
            Selector selector = Selector.open();
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(serverHost, serverPort));
        
        
        } catch (IOException ioe) { }
        
    }
    
    public static void main(String[] args) {
    
        // Extract our required arguments and assign them to the correct variables to be used in the server constructor.
        String serverHost;
        int serverPort, messageRate;
        try {
        
            serverHost = args[0];
            serverPort = Integer.parseInt(args[1]);
            messageRate = Integer.parseInt(args[2]);
        
        } catch (Exception e) {
            System.out.println("ERROR: Given arguments were incorrect.");
            return;
        }

        Client client = new Client(serverHost, serverPort, messageRate);
        client.start();
        
    }
    
}
