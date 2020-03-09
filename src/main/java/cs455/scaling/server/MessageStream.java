package cs455.scaling.server;

import cs455.scaling.util.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class MessageStream {
    
    private final SocketChannel socketChannel;
    private ByteBuffer readBuffer, writeBuffer;
    private final Object readLock = new Object();
    private final Object writeLock = new Object();
    
    public MessageStream(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
    
    private byte[] readByteArray(int bufferSize) {
        synchronized (readLock) {
    
            byte[] byteArray = new byte[0];
            readBuffer = ByteBuffer.allocate(bufferSize);
    
            try {
        
                // Continuously read from the socketChannel into the buffer until there is nothing more to read, AKA the
                // buffer gets full or we receive an EndOfTransmission byte (4).
                int bytesRead = 0;
                while (readBuffer.hasRemaining() && bytesRead >= 0) {
                    bytesRead += socketChannel.read(readBuffer);
                    if (bytesRead != 0 && readBuffer.get(bytesRead - 1) == 4)
                        break;
                }
        
                if (readBuffer.hasArray())
                    byteArray = Arrays.copyOf(readBuffer.array(), readBuffer.array().length);
                
                //System.out.println("Read: "+ byteArray.length);
        
            } catch (IOException ioe) {
                System.out.println("IOException: Unable to read from socketChannel.");
            }
    
            return byteArray;
        }
    }
    
    public byte[] readByteArray() {
        return readByteArray(Constants.BYTE_ARRAY_BUFFER_SIZE);
    }
    
    public String readString() {
        return new String(readByteArray(Constants.HASH_BUFFER_SIZE)).trim();
    }
    
    public void writeByteArray(byte[] arr) {
        synchronized (writeLock) {
            try {
    
                writeBuffer = ByteBuffer.wrap(arr);
        
                while (writeBuffer.hasRemaining()) {
                    socketChannel.write(writeBuffer);
                }
    
                //System.out.println("Wrote: "+ writeBuffer.array().length);
                
            } catch (IOException ioe) {
                System.out.println("IOException: Unable to write to socketChannel.");
            }
        }
    }
    
    public void writeString(String str) {
        writeByteArray(str.getBytes());
    }
    
}
