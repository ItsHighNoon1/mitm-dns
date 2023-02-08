import java.io.IOException;
import java.net.DatagramPacket;

public class RequestHandler implements Runnable {
    private void recordRequest(DatagramPacket requestPacket) {
        System.out.println(requestPacket.getSocketAddress());
    }

    @Override
    public void run() {
        // Respond to user packets
        while(!Server.userSocket.isClosed()) {
            // Get the user request
            byte[] inBuffer = new byte[Server.MAX_PACKET_SIZE];
            DatagramPacket inPacket = new DatagramPacket(inBuffer, inBuffer.length);
            try {
                Server.userSocket.receive(inPacket);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            // Save user info
            recordRequest(inPacket);

            // Record the request ID and user address
            short dnsId = (short)(inBuffer[0] << 8 | inBuffer[1]);
            Server.activeRequests.put(dnsId, inPacket.getSocketAddress());

            // Send the request to the DNS
            inPacket.setSocketAddress(Server.dnsAddr);
            try {
                Server.dnsSocket.send(inPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
