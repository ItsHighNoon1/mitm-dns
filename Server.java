import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class Server {
    public static Map<Short, SocketAddress> activeRequests;
    public static DatagramSocket userSocket = null;
    public static InetAddress dnsAddr = null;
    public static DatagramSocket dnsSocket = null;

    public static void main(String[] args) {
        // Set up the request ID tracker
        activeRequests = new HashMap<Short, SocketAddress>();

        // Open a UDP socket on port 53
        try {
            userSocket = new DatagramSocket(53);
        } catch (IOException e) {
            System.err.println("Cannot bind to UDP port 53");
            System.exit(-1);
        }

        // Get the address of a real name server
        try {
            dnsAddr = InetAddress.getByName("8.8.8.8"); // There's a certain irony here
        } catch (UnknownHostException e) {
            System.err.println("Failed to get default DNS address");
            System.exit(-1);
        }

        // Open a connection to a real DNS server
        try {
            dnsSocket = new DatagramSocket(6004);
        } catch (IOException e) {
            System.err.println("Cannot bind to UDP port 6004");
            System.exit(-1);
        }

        // Start the request handler and response handler
        Thread userThread = new Thread(new RequestHandler());
        Thread dnsThread = new Thread(new ResponseHandler());
        userThread.start();
        dnsThread.start();

        // Wait for these threads to finish
        try {
            userThread.join();
            dnsThread.join();
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            userSocket.close();
            dnsSocket.close();
            System.exit(0);
        }
    }
}