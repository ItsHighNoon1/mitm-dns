import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketAddress;

public class RequestHandler implements Runnable {
    @Override
    public void run() {
        // Respond to user packets
        while(!Server.userSocket.isClosed()) {
            // Get the user request
            byte[] inBuffer = new byte[65536];
            DatagramPacket inPacket = new DatagramPacket(inBuffer, inBuffer.length);
            try {
                Server.userSocket.receive(inPacket);
            } catch (IOException e) {
                System.err.println("Error receiving packet from user");
                continue;
            }

            // Save user info
            SocketAddress requestorAddress = inPacket.getSocketAddress();
            System.out.println("Request from " + requestorAddress);

            // Record the request ID and user address
            short dnsId = (short)(inBuffer[0] << 8 | inBuffer[1]);
            System.out.println("ID " + dnsId);
            Server.activeRequests.put(dnsId, requestorAddress);

            // Send the request to the DNS
            inPacket.setAddress(Server.dnsAddr);
            inPacket.setPort(53);
            try {
                Server.dnsSocket.send(inPacket);
            } catch (IOException e) {
                System.out.println("Failed to send packet to DNS");
                continue;
            }
            System.out.println("Sent to " + Server.dnsAddr);
        }

        Server.userSocket.close();
    }
}
