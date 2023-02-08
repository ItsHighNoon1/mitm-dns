import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public class Server {
    public static final int DNS_PORT = 53;
    public static final int RESOLVER_SRC_PORT = 6004;
    public static final int MAX_PACKET_SIZE = 65536;
    public static final String REAL_DNS_SERVER = "8.8.8.8";

    public static Map<Short, SocketAddress> activeRequests;
    public static DatagramSocket userSocket = null;
    public static SocketAddress dnsAddr = null;
    public static DatagramSocket dnsSocket = null;

    public static void main(String[] args) {
        // Set up the request ID tracker
        activeRequests = new HashMap<Short, SocketAddress>();

        // Open a UDP socket on the DNS port (usually 53)
        try {
            userSocket = new DatagramSocket(DNS_PORT);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // Get DNS address
        dnsAddr = new InetSocketAddress(REAL_DNS_SERVER, DNS_PORT);

        // Open a UDP socket for the DNS responses
        try {
            dnsSocket = new DatagramSocket(RESOLVER_SRC_PORT);
        } catch (IOException e) {
            e.printStackTrace();
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
            userSocket.close();
            dnsSocket.close();
            System.exit(0);
        }
    }
}