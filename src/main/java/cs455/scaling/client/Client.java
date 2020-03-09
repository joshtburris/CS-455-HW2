package cs455.scaling.client;

import cs455.scaling.server.MessageStream;
import cs455.scaling.util.*;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

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
    private final LinkedBlockingQueue<String> hashCodes = new LinkedBlockingQueue<>();
    private final Random rand = new Random();
    private MessageStream messageStream;
    
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
    
            messageStream = new MessageStream(socketChannel);
            
            System.out.println("Successfully connected to server: "+ socketChannel.getRemoteAddress());
        
        } catch (IOException ioe) {
            System.out.println("IOException: Unable to connect to server.");
            return;
        }
    
        ArrayList<Timer> timers = new ArrayList<>();
        double doubleRate = 1.0 / (double)messageRate;
        
        for (double i = doubleRate; i < 1.0+doubleRate; i += doubleRate) {
        
            Timer t = new Timer();
            t.scheduleAtFixedRate(new SendMessageTimerTask(messageStream), Math.round(i * 1000), 1L);
            timers.add(t);
        
        }
        
        while (true) {
            
            String hash = messageStream.readString();
            hashCodes.remove(hash);
        
        }
        
    }
    
    private class SendMessageTimerTask extends TimerTask {
    
        private final MessageStream messageStream;
        
        public SendMessageTimerTask(MessageStream messageStream) {
            this.messageStream = messageStream;
        }
        
        @Override public void run() {
            
            byte[] byteArray = new byte[Constants.BYTE_ARRAY_BUFFER_SIZE];
            rand.nextBytes(byteArray);
            
            String hash = Hashing.SHA1FromBytes(byteArray);
            hashCodes.add(hash);
            
            messageStream.writeByteArray(byteArray);
            
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
    
            if (serverPort < 1024 || serverPort > 65535 || messageRate < 1)
                throw new Exception();
        
        } catch (Exception e) {
            System.out.println("ERROR: Given arguments were unusable.");
            return;
        }

        Client client = new Client(serverHost, serverPort, messageRate);
        client.start();
        
    }
    
}
