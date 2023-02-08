import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketAddress;

public class ResponseHandler implements Runnable {
    @Override
    public void run() {
        // Tunnel the response packets
        while (!Server.dnsSocket.isClosed()) {
            // Get the DNS response
            byte[] outBuffer = new byte[65536];
            DatagramPacket outPacket = new DatagramPacket(outBuffer, outBuffer.length);
            try {
                Server.dnsSocket.receive(outPacket);
            } catch (IOException e) {
                System.err.println("Error receiving packet from DNS");
                continue;
            }
            System.out.println("Response from " + Server.dnsAddr);

            // Get the correct address for the response ID
            short dnsId = (short)(outBuffer[0] << 8 | outBuffer[1]);
            SocketAddress responseAddr = Server.activeRequests.get(dnsId);

            // Send the response to the user
            outPacket.setSocketAddress(responseAddr);
            try {
                Server.userSocket.send(outPacket);
            } catch (IOException e) {
                System.out.println("Failed to send packet to user");
                continue;
            }
            System.out.println("Sent to " + responseAddr);
        }
    }
}
