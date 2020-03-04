package cs455.scaling.server;

import cs455.scaling.util.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class MessageStream {
    
    private SocketChannel socketChannel;
    private ByteBuffer buffer;
    private String remoteSocketAddress;
    
    public MessageStream(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        this.buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
        try {
            this.remoteSocketAddress = socketChannel.getRemoteAddress().toString();
        } catch (IOException ioe) {
            System.out.println("IOException: socketChannel could not get the remote address.");
            this.remoteSocketAddress = "UNKNOWN_HOST";
        }
    }
    
    public String getRemoteSocketAddress() {
        return remoteSocketAddress;
    }
    
    public byte[] readByteArray() {
    
        byte[] byteArray = new byte[0];
    
        try {
            
            // Continuously read from the socketChannel into the buffer until there is nothing more to read, AKA the
            // buffer gets full or we receive an EndOfTransmission byte (4).
            int bytesRead = 0;
            while (buffer.hasRemaining() && bytesRead != -1) {
                bytesRead += socketChannel.read(buffer);
                if (bytesRead != 0 && buffer.get(bytesRead - 1) == 4)
                    break;
            }
            
            if (buffer.hasArray())
                byteArray = buffer.array();
            buffer.clear();
            
        } catch (IOException ioe) {
            System.out.println("IOException: Unable to read from socketChannel.");
        }
        
        return byteArray;
    }
    
    public String readString() {
    
        String str = "";
    
        try {
        
            // Continuously read from the socketChannel into the buffer until there is nothing more to read, AKA the
            // buffer gets full or we receive an EndOfTransmission byte (4).
            int bytesRead = 0;
            while (buffer.hasRemaining() && bytesRead != -1) {
                bytesRead += socketChannel.read(buffer);
                if (bytesRead != 0 && buffer.get(bytesRead - 1) == 4)
                    break;
            }
        
            if (buffer.hasArray())
                str = new String(buffer.array()).trim();
            buffer.clear();
        
        } catch (IOException ioe) {
            System.out.println("IOException: Unable to read from socketChannel.");
        }
    
        return str;
    }
    
    public void writeString(String str) {
    
        try {
    
            buffer = ByteBuffer.wrap(str.getBytes());
    
            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }
    
            buffer.clear();
            
        } catch (IOException ioe) {
            System.out.println("IOException: Unable to write to socketChannel.");
        }
    
    }
    
}
