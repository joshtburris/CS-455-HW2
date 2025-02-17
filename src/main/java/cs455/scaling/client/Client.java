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
    public final MessagingStatistics stats;
    
    public Client(String serverHost, int serverPort, int messageRate) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.messageRate = messageRate;
        this.stats  = new MessagingStatistics();
    }
    
    public void start() {
    
        SocketChannel socketChannel;
        
        try {
    
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(serverHost, serverPort));
            
            while (!socketChannel.finishConnect()) {
                // Do nothing. Wait for the socketChannel to finish connecting. We must do non-blocking I/O.
                // Turns out I don't know if this does what I thought it did. We exit this loop even before the server
                // calls serverSocket.accept()
            }
            
            System.out.println("Successfully connected to server: "+ socketChannel.getRemoteAddress());
        
        } catch (IOException ioe) {
            System.out.println("IOException: Unable to connect to server.");
            System.out.println(ioe.getMessage());
            return;
        }
    
        messageStream = new MessageStream(socketChannel);
    
        ArrayList<Timer> timers = new ArrayList<>(messageRate);
        double doubleRate = 1.0 / (double)messageRate;
        
        for (double i = doubleRate; i < 1.0 + doubleRate; i += doubleRate) {
            Timer t = new Timer();
            t.scheduleAtFixedRate(new SendMessageTimerTask(messageStream), Math.round(i * 1000), 1000L);
            timers.add(t);
        }
        
        try {
            while (true) {
        
                if (hashCodes.remove(messageStream.readString())) {
                    stats.incrementNumReceived();
                } else {
                    System.out.println("ERROR: Hash code received was not in our linked list.");
                }
        
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("UNKNOWN ERROR: Looping to read and remove hash codes failed. Attempting to shutdown " +
                    "gracefully.");
        }
        
        for (Timer t : timers) {
            t.cancel();
        }
        
        try {
            socketChannel.close();
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
            System.out.println("IOException: socketChannel failed to close.");
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
    
            Hashing hashing = new Hashing(byteArray);
            hashCodes.add(hashing.getHash());
            
            messageStream.writeByteArray(byteArray);
            
            stats.incrementNumSent();
            
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
        
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new PrintStatsTimerTask(client.stats), 20000L, 20000L);
        
        client.start();
        
    }
    
    private static class PrintStatsTimerTask extends TimerTask {
        
        private final MessagingStatistics stats;
        
        public PrintStatsTimerTask(MessagingStatistics stats) {
            this.stats = stats;
        }
    
        @Override public void run() {
            stats.print();
        }
    }
    
}
