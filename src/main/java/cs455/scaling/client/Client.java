package cs455.scaling.client;


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
    
    public static void main(String[] args) {
    
    
    
    }
    
}
