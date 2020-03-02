package cs455.scaling.client;

import cs455.scaling.util.Constants;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Scanner;

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
    
        SocketChannel socketChannel;
        
        try {
    
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(serverHost, serverPort));
            
            while (!socketChannel.finishConnect()) {
                // Do nothing. Wait for the socketChannel to finish connecting. We must do non-blocking I/O.
            }
            
            System.out.println("Successfully connected to server: "+ socketChannel.getRemoteAddress());
        
        } catch (IOException ioe) {
            System.out.println("IOException: Unable to connect to server.");
            return;
        }
        
        ByteBuffer buffer;
        Scanner in = new Scanner(System.in);
        String line, response;
        
        while (!(line = in.nextLine()).isEmpty()) {
            try {
    
                buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
                for (int i = 0; i < Constants.BUFFER_SIZE; ++i) {
                    buffer.put((byte)'h');
                }
                buffer.flip();
                while (buffer.hasRemaining()) {
                    socketChannel.write(buffer);
                }
                
                buffer.clear();
                
                int bytesRead = 0;
                while (buffer.hasRemaining() && bytesRead != -1) {
                    bytesRead = socketChannel.read(buffer);
                }
                
                response = new String(buffer.array()).trim();
                System.out.println("Response: "+ response);
                buffer.clear();
                
            } catch (IOException ioe) {
                System.out.println("IOException: Unable to send message.");
            }
        }
        
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
